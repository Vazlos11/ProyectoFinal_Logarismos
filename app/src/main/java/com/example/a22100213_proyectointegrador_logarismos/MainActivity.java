package com.example.a22100213_proyectointegrador_logarismos;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.a22100213_proyectointegrador_logarismos.Semantico.AnalisisSemantico;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.SemanticoError;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.TipoExpresion;
import com.judemanutd.katexview.KatexView;

import java.util.*;

public class MainActivity extends AppCompatActivity {

    TextView test;
    private List<Token> rootTokens = new ArrayList<>();
    private Token currentContainer = null;
    private int cursorIndex = 0;
    private KatexView katexView;
    private ViewFlipper keyboardsFlipp;

    private static final String CURSOR = "\\textcolor{red}{\\vert}";

    private static final Set<String> ATOMIC_TOKENS = new HashSet<>(Arrays.asList(
            "\\sin","\\cos","\\tan","\\cot","\\sec","\\csc",
            "\\arcsin","\\arccos","\\arctan","\\arccot","\\arcsec","\\arccsc",
            "\\log","\\ln",
            "\\frac{d}{dx}","\\frac{dy}{dx}",
            "dx","dy",
            "\\cdot ",
            "\\log_{2}",
            "\\log_{10}"
    ));

    private static final Map<String, String> latexMap = new HashMap<>();
    static {
        for (int i = 0; i <= 9; i++) latexMap.put(String.valueOf(i), String.valueOf(i));
        latexMap.put("+", "+");
        latexMap.put("-", "-");
        latexMap.put("*", "\\cdot ");
        latexMap.put("/", "/");
        latexMap.put("%", "\\%");
        latexMap.put("=", "=");
        latexMap.put("^", "^");
        latexMap.put("^2", "^{2}");
        latexMap.put("^{}", "^{}");
        latexMap.put("π", "\\pi");
        latexMap.put("e", "e");
        latexMap.put("d/dx", "\\frac{d}{dx}");
        latexMap.put("dy/dx", "\\frac{dy}{dx}");
        latexMap.put("d/d_", "\\frac{d}{d\\,}");
        latexMap.put("dx", "dx");
        latexMap.put("dy", "dy");
        latexMap.put("∫   dx", "\\int_dx");
        latexMap.put("def ∫   dx", "\\int_def");
        latexMap.put("∫   d_", "\\int");
        latexMap.put("f(x)", "f(x)");
        latexMap.put("!", "!");
        latexMap.put("log", "\\log");
        latexMap.put("log2", "\\log_{2}");
        latexMap.put("log10", "\\log_{10}");
        latexMap.put("ln", "\\ln");
        latexMap.put("√", "\\sqrt{}");
        latexMap.put("sin", "\\sin");
        latexMap.put("cos", "\\cos");
        latexMap.put("tan", "\\tan");
        latexMap.put("cot", "\\cot");
        latexMap.put("sec", "\\sec");
        latexMap.put("csc", "\\csc");
        latexMap.put("arcsin", "\\arcsin");
        latexMap.put("arccos", "\\arccos");
        latexMap.put("arctan", "\\arctan");
        latexMap.put("arccot", "\\arccot");
        latexMap.put("arcsec", "\\arcsec");
        latexMap.put("arccsc", "\\arccsc");
        latexMap.put("α", "\\alpha");
        latexMap.put("β", "\\beta");
        latexMap.put("θ", "\\theta");
        latexMap.put("ρ", "\\rho");
        latexMap.put("φ", "\\varphi");
        latexMap.put("λ", "\\lambda");
        latexMap.put("a/b", "\\frac{}{}");
        for (char c = 'a'; c <= 'z'; c++) latexMap.put(String.valueOf(c), String.valueOf(c));
        latexMap.put("( )", "()");
        latexMap.put("[ ]", "[]");
        latexMap.put("{ ... }", "\\{ \\}");
        latexMap.put("|    |", "\\lvert \\rvert");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        katexView = findViewById(R.id.katex_text);
        cursorIndex = 0;
        keyboardsFlipp = findViewById(R.id.keyboardsFlipp);
        test = findViewById(R.id.test);
        keyboardsFlipp.setInAnimation(this, android.R.anim.fade_in);
        keyboardsFlipp.setOutAnimation(this, android.R.anim.fade_out);
        updateView();
    }

    private boolean isContainerSymbol(String symbol) {
        return symbol.equals("\\sqrt{}") ||
                symbol.equals("\\lvert \\rvert") ||
                symbol.equals("()") ||
                symbol.equals("[]") ||
                symbol.equals("\\{ \\}");
    }

    private void updateView() {
        StringBuilder sb = new StringBuilder();
        appendListWithCursor(sb, rootTokens);
        katexView.setText("$$\\Large " + sb + "$$");
    }

    private void appendListWithCursor(StringBuilder sb, List<Token> list) {
        for (int i = 0; i <= list.size(); i++) {
            if (currentContainer == null && i == cursorIndex) sb.append(CURSOR);
            if (i < list.size()) appendTokenWithCursor(sb, list.get(i));
        }
    }

    private void appendChildrenWithCursor(StringBuilder sb, Token container) {
        List<Token> children = container.children;
        for (int j = 0; j <= children.size(); j++) {
            if (container == currentContainer && j == cursorIndex) sb.append(CURSOR);
            if (j < children.size()) appendTokenWithCursor(sb, children.get(j));
        }
    }

    private void appendTokenWithCursor(StringBuilder sb, Token t) {
        if (!t.isContainer) {
            sb.append(t.toLatex());
            return;
        }
        switch (t.value) {
            case "()":
                sb.append("(");
                appendChildrenWithCursor(sb, t);
                sb.append(")");
                break;
            case "[]":
                sb.append("\\left[");
                appendChildrenWithCursor(sb, t);
                sb.append("\\right]");
                break;
            case "\\{ \\}":
                sb.append("\\{ ");
                appendChildrenWithCursor(sb, t);
                sb.append(" \\}");
                break;
            case "\\lvert":
                sb.append("\\lvert ");
                appendChildrenWithCursor(sb, t);
                sb.append(" \\rvert");
                break;
            case "\\sqrt":
                sb.append("\\sqrt{");
                appendChildrenWithCursor(sb, t);
                sb.append("}");
                break;
            case "\\frac":
                sb.append("\\frac{");
                if (t.children.size() > 0) appendTokenWithCursor(sb, t.children.get(0));
                sb.append("}{");
                if (t.children.size() > 1) appendTokenWithCursor(sb, t.children.get(1));
                sb.append("}");
                break;
            case "\\int_def":
                sb.append("\\int_{");
                if (t.children.size() > 0) appendTokenWithCursor(sb, t.children.get(0));
                sb.append("}^{");
                if (t.children.size() > 1) appendTokenWithCursor(sb, t.children.get(1));
                sb.append("} ");
                if (t.children.size() > 2) {
                    Token body = t.children.get(2);
                    if (body != null && body.isContainer && "()".equals(body.value)) {
                        appendChildrenWithCursor(sb, body);
                    } else if (body != null) {
                        appendTokenWithCursor(sb, body);
                    }
                }
                if (t.children.size() > 3) {
                    sb.append(" ");
                    appendTokenWithCursor(sb, t.children.get(3));
                }
                break;
            case "\\int":
                sb.append("\\int ");
                if (t.children.size() > 0) {
                    Token bodyI = t.children.get(0);
                    if (bodyI != null && bodyI.isContainer && "()".equals(bodyI.value)) {
                        appendChildrenWithCursor(sb, bodyI);
                    } else if (bodyI != null) {
                        appendTokenWithCursor(sb, bodyI);
                    }
                }
                if (t.children.size() > 1) {
                    sb.append(" ");
                    appendTokenWithCursor(sb, t.children.get(1));
                }
                break;
            case "\\log":
            case "\\ln": {
                sb.append(t.value);
                if (!t.children.isEmpty()) {
                    Token arg = t.children.get(0);
                    if ("()".equals(arg.value)) {
                        appendTokenWithCursor(sb, arg);
                    } else {
                        sb.append("(");
                        appendTokenWithCursor(sb, arg);
                        sb.append(")");
                    }
                } else {
                    sb.append("()");
                }
                break;
            }
            case "\\sin": case "\\cos": case "\\tan": case "\\cot":
            case "\\sec": case "\\csc":
            case "\\arcsin": case "\\arccos": case "\\arctan":
            case "\\arccot": case "\\arcsec": case "\\arccsc": {
                sb.append(t.value);
                if (!t.children.isEmpty()) {
                    Token arg = t.children.get(0);
                    if ("()".equals(arg.value)) {
                        appendTokenWithCursor(sb, arg);
                    } else {
                        sb.append("(");
                        appendTokenWithCursor(sb, arg);
                        sb.append(")");
                    }
                } else {
                    sb.append("()");
                }
                break;
            }
            case "\\log_{2}":
            case "\\log_{10}": {
                sb.append("\\log_{");
                if (t.children.size() > 0) appendTokenWithCursor(sb, t.children.get(0));
                sb.append("}");
                if (t.children.size() > 1) {
                    Token arg = t.children.get(1);
                    if ("()".equals(arg.value)) {
                        appendTokenWithCursor(sb, arg);
                    } else {
                        sb.append("(");
                        appendTokenWithCursor(sb, arg);
                        sb.append(")");
                    }
                } else {
                    sb.append("()");
                }
                break;
            }
            case "\\exp":
                sb.append("^{");
                appendChildrenWithCursor(sb, t);
                sb.append("}");
                break;
            case "{}":
            case "^group":
                appendChildrenWithCursor(sb, t);
                break;
            default:
                appendChildrenWithCursor(sb, t);
                break;
        }
    }

    public void ins_char(View view) {
        Button b = (Button) view;
        String symbol = b.getText().toString();
        String latexEquivalent = latexMap.getOrDefault(symbol, symbol);
        Token newToken;

        if (latexEquivalent.equals("\\sin") || latexEquivalent.equals("\\cos") ||
                latexEquivalent.equals("\\tan") || latexEquivalent.equals("\\cot") ||
                latexEquivalent.equals("\\sec") || latexEquivalent.equals("\\csc") ||
                latexEquivalent.equals("\\ln")  || latexEquivalent.equals("\\log")) {
            newToken = Token.container(latexEquivalent);
            Token parenGroup = Token.container("()");
            parenGroup.parent = newToken;
            newToken.children.add(parenGroup);
            insertToken(newToken);
            currentContainer = parenGroup;
            cursorIndex = 0;
            updateView();
            return;
        }

        if ("\\log_{2}".equals(latexEquivalent) || "\\log_{10}".equals(latexEquivalent)) {
            newToken = Token.container(latexEquivalent);
            Token base = Token.container("{}");
            Token baseNum = Token.atomic(latexEquivalent.equals("\\log_{2}") ? "2" : "10");
            baseNum.parent = base;
            base.children.add(baseNum);
            base.parent = newToken;
            Token arg = Token.container("()");
            arg.parent = newToken;
            newToken.children.add(base);
            newToken.children.add(arg);
            insertToken(newToken);
            currentContainer = arg;
            cursorIndex = 0;
            updateView();
            return;
        }

        if ("^".equals(latexEquivalent) || (latexEquivalent.startsWith("^{") && latexEquivalent.endsWith("}"))) {
            Token expToken = Token.container("\\exp");
            Token exponentGroup = Token.container("^group");
            exponentGroup.parent = expToken;
            if (latexEquivalent.startsWith("^{") && latexEquivalent.endsWith("}")) {
                String inside = latexEquivalent.substring(2, latexEquivalent.length() - 1);
                if (!inside.isEmpty()) {
                    Token insideTok = Token.simple(inside);
                    insideTok.parent = exponentGroup;
                    exponentGroup.children.add(insideTok);
                }
            }
            expToken.children.add(exponentGroup);
            insertToken(expToken);
            currentContainer = exponentGroup;
            cursorIndex = exponentGroup.children.size();
            updateView();
            return;
        }

        if ("\\int".equals(latexEquivalent) || "\\int_dx".equals(latexEquivalent)) {
            newToken = Token.container("\\int");
            Token body = Token.container("()");
            Token diff = Token.atomic("dx");
            body.parent = newToken;
            diff.parent = newToken;
            newToken.children.add(body);
            newToken.children.add(diff);
            insertToken(newToken);
            currentContainer = body;
            cursorIndex = 0;
            updateView();
            return;
        }

        if ("\\frac{}{}".equals(latexEquivalent)) {
            newToken = Token.container("\\frac");
            Token numerator = Token.container("()");
            Token denominator = Token.container("()");
            numerator.parent = newToken;
            denominator.parent = newToken;
            newToken.children.add(numerator);
            newToken.children.add(denominator);
            insertToken(newToken);
            currentContainer = numerator;
            cursorIndex = 0;
            updateView();
            return;
        }

        if ("\\int_def".equals(latexEquivalent)) {
            newToken = Token.container("\\int_def");
            Token inf  = Token.container("()");
            Token sup  = Token.container("()");
            Token body = Token.container("()");
            Token diff = Token.atomic("dx");
            inf.parent = newToken;
            sup.parent = newToken;
            body.parent = newToken;
            diff.parent = newToken;
            newToken.children.add(inf);
            newToken.children.add(sup);
            newToken.children.add(body);
            newToken.children.add(diff);
            insertToken(newToken);
            currentContainer = sup;
            cursorIndex = 0;
            updateView();
            return;
        }

        if (isContainerSymbol(latexEquivalent)) {
            switch (latexEquivalent) {
                case "\\sqrt{}":
                    newToken = Token.container("\\sqrt");
                    break;
                case "\\lvert \\rvert":
                    newToken = Token.container("\\lvert");
                    break;
                case "[]":
                    newToken = Token.container("[]");
                    break;
                case "\\{ \\}":
                    newToken = Token.container("\\{ \\}");
                    break;
                default:
                    newToken = Token.container("()");
            }
            insertToken(newToken);
            updateView();
            return;
        }

        if (ATOMIC_TOKENS.contains(latexEquivalent)) {
            newToken = Token.atomic(latexEquivalent);
            insertToken(newToken);
            updateView();
            return;
        }

        newToken = Token.simple(latexEquivalent);
        insertToken(newToken);
        updateView();
    }

    private boolean isNumberToken(Token t) {
        if (t == null || t.isContainer || t.isAtomic) return false;
        return t.value != null && t.value.matches("\\d+");
    }

    private boolean isSingleLetterVariable(Token t) {
        if (t == null || t.isContainer || t.isAtomic) return false;
        return t.value != null && t.value.matches("[a-zA-Z]");
    }

    private boolean isMulToken(Token t) {
        return t != null && t.isAtomic && "\\cdot ".equals(t.value);
    }

    private void insertToken(Token token) {
        List<Token> targetList = (currentContainer == null) ? rootTokens : currentContainer.children;
        Token parentToken = currentContainer;
        if (!token.isContainer && !token.isAtomic) {
            if (token.value != null && token.value.matches("\\d")) {
                if (cursorIndex > 0 && cursorIndex <= targetList.size()) {
                    Token prev = targetList.get(cursorIndex - 1);
                    if (!prev.isContainer && !prev.isAtomic && prev.value != null && prev.value.matches("\\d+")) {
                        prev.value = prev.value + token.value;
                        return;
                    }
                }
            }
        }
        int insertPos = cursorIndex;
        if (!token.isContainer && !token.isAtomic && token.value != null && token.value.matches("[a-zA-Z]")) {
            if (insertPos > 0) {
                Token prev = targetList.get(insertPos - 1);
                if (isNumberToken(prev) || isSingleLetterVariable(prev) || prev.isContainer) {
                    Token mul = Token.atomic("\\cdot ");
                    mul.parent = parentToken;
                    targetList.add(insertPos, mul);
                    insertPos++;
                }
            }
        }
        if (token.isContainer && !"\\exp".equals(token.value)) {
            if (insertPos > 0) {
                Token prev = targetList.get(insertPos - 1);
                if (isNumberToken(prev) || isSingleLetterVariable(prev) || prev.isContainer) {
                    Token mul = Token.atomic("\\cdot ");
                    mul.parent = parentToken;
                    targetList.add(insertPos, mul);
                    insertPos++;
                }
            }
        }
        if (!token.isContainer && token.isAtomic && "\\cdot ".equals(token.value)) {
            if (insertPos > 0) {
                Token prev = targetList.get(insertPos - 1);
                if (isMulToken(prev)) return;
            }
            if (insertPos < targetList.size()) {
                Token next = targetList.get(insertPos);
                if (isMulToken(next)) return;
            }
        }
        targetList.add(insertPos, token);
        token.parent = parentToken;
        if (token.isContainer) {
            currentContainer = token;
            cursorIndex = 0;
            Token inner = preferInnerEditable(currentContainer);
            if (inner != null) {
                currentContainer = inner;
                cursorIndex = 0;
            }
        } else {
            cursorIndex = insertPos + 1;
        }
    }

    private Token preferInnerEditable(Token cont) {
        if (cont == null || !cont.isContainer) return null;
        if ("\\int_def".equals(cont.value)) {
            return cont.children.size() > 1 ? cont.children.get(1) : null;
        }
        if ("\\int".equals(cont.value)) {
            return cont.children.size() > 0 ? cont.children.get(0) : null;
        }
        if ("\\exp".equals(cont.value)) {
            return cont.children.isEmpty() ? null : cont.children.get(0);
        }
        if ("\\log_{2}".equals(cont.value) || "\\log_{10}".equals(cont.value)) {
            return cont.children.size() > 1 ? cont.children.get(1) : null;
        }
        if (cont.children.size() == 1) {
            Token c0 = cont.children.get(0);
            if (c0 != null && c0.isContainer) {
                String v = c0.value;
                if ("()".equals(v) || "[]".equals(v) || "\\{ \\}".equals(v) || "\\lvert".equals(v) || "^group".equals(v)) {
                    return c0;
                }
            }
        }
        return null;
    }

    public void deleteChar(View view) {
        borrarToken();
    }

    private void borrarToken() {
        List<Token> targetList = (currentContainer == null) ? rootTokens : currentContainer.children;
        if (cursorIndex > 0) {
            targetList.remove(cursorIndex - 1);
            cursorIndex--;
        } else {
            if (currentContainer != null) {
                Token parent = currentContainer.parent;
                List<Token> parentList = (parent == null) ? rootTokens : parent.children;
                int idxInParent = parentList.indexOf(currentContainer);
                if (parent != null && "\\int_def".equals(parent.value)) {
                    currentContainer = parent;
                    cursorIndex = idxInParent;
                    Token nextSlot = preferInnerEditable(currentContainer);
                    if (nextSlot != null) {
                        currentContainer = nextSlot;
                        cursorIndex = 0;
                    }
                } else if (parent != null && "\\int".equals(parent.value)) {
                    currentContainer = parent;
                    cursorIndex = idxInParent;
                    Token slot = preferInnerEditable(currentContainer);
                    if (slot != null) {
                        currentContainer = slot;
                        cursorIndex = 0;
                    }
                } else {
                    if (currentContainer.children.isEmpty()) {
                        parentList.remove(idxInParent);
                        currentContainer = parent;
                        cursorIndex = idxInParent;
                    } else {
                        currentContainer = parent;
                        cursorIndex = idxInParent;
                    }
                }
            }
        }
        updateView();
    }

    private Token findParent(Token target) {
        return (target == null) ? null : target.parent;
    }

    private void moveCursorDelta(int delta) {
        if (currentContainer != null && currentContainer.isContainer && "\\int_def".equals(currentContainer.value)) {
            if (delta > 0) {
                if (currentContainer.children.size() > 1) {
                    currentContainer = currentContainer.children.get(1);
                    cursorIndex = 0;
                    updateView();
                    return;
                }
            } else if (delta < 0) {
                if (currentContainer.children.size() > 2) {
                    currentContainer = currentContainer.children.get(2);
                    cursorIndex = currentContainer.children.size();
                    updateView();
                    return;
                }
            }
        }

        if (currentContainer != null && currentContainer.parent != null && "\\int_def".equals(currentContainer.parent.value)) {
            Token p = currentContainer.parent;
            int idxChild = p.children.indexOf(currentContainer);
            List<Token> childList = currentContainer.children;

            if (delta > 0) {
                if (cursorIndex < childList.size()) {
                    cursorIndex++;
                    updateView();
                    return;
                }
                if (idxChild == 1) {
                    currentContainer = p.children.get(0);
                    cursorIndex = 0;
                    updateView();
                    return;
                }
                if (idxChild == 0) {
                    currentContainer = p.children.get(2);
                    cursorIndex = 0;
                    updateView();
                    return;
                }
                if (idxChild == 2) {
                    Token gp = p.parent;
                    List<Token> gpList = (gp == null) ? rootTokens : gp.children;
                    int idx = gpList.indexOf(p);
                    currentContainer = gp;
                    cursorIndex = (gp == null) ? (idx + 1) : (idx + 1);
                    updateView();
                    return;
                }
            }

            if (delta < 0) {
                if (cursorIndex > 0) {
                    cursorIndex--;
                    updateView();
                    return;
                }
                if (idxChild == 0) {
                    currentContainer = p.children.get(1);
                    cursorIndex = p.children.get(1).children.size();
                    updateView();
                    return;
                }
                if (idxChild == 2) {
                    currentContainer = p.children.get(0);
                    cursorIndex = p.children.get(0).children.size();
                    updateView();
                    return;
                }
                if (idxChild == 1) {
                    Token gp = p.parent;
                    List<Token> gpList = (gp == null) ? rootTokens : gp.children;
                    int idx = gpList.indexOf(p);
                    currentContainer = gp;
                    cursorIndex = (gp == null) ? idx : idx;
                    updateView();
                    return;
                }
            }
        }

        if (currentContainer != null && currentContainer.isContainer && "\\int".equals(currentContainer.value)) {
            if (delta > 0) {
                Token body = currentContainer.children.get(0);
                currentContainer = body;
                cursorIndex = body.children.size();
                updateView();
                return;
            } else if (delta < 0) {
                Token body = currentContainer.children.get(0);
                currentContainer = body;
                cursorIndex = 0;
                updateView();
                return;
            }
        }

        if (currentContainer != null && currentContainer.parent != null && "\\int".equals(currentContainer.parent.value)) {
            Token p = currentContainer.parent;
            int idxChild = p.children.indexOf(currentContainer);
            List<Token> childList = currentContainer.children;

            if (delta > 0) {
                if (cursorIndex < childList.size()) {
                    cursorIndex++;
                    updateView();
                    return;
                }
                Token gp = p.parent;
                List<Token> gpList = (gp == null) ? rootTokens : gp.children;
                int idx = gpList.indexOf(p);
                currentContainer = gp;
                cursorIndex = (gp == null) ? (idx + 1) : (idx + 1);
                updateView();
                return;
            }

            if (delta < 0) {
                if (cursorIndex > 0) {
                    cursorIndex--;
                    updateView();
                    return;
                }
                Token gp = p.parent;
                List<Token> gpList = (gp == null) ? rootTokens : gp.children;
                int idx = gpList.indexOf(p);
                currentContainer = gp;
                cursorIndex = (gp == null) ? idx : idx;
                updateView();
                return;
            }
        }

        List<Token> targetList = (currentContainer == null) ? rootTokens : currentContainer.children;
        int newIndex = cursorIndex + delta;

        if (newIndex >= 0 && newIndex <= targetList.size()) {
            cursorIndex = newIndex;
            if (delta > 0) {
                if (cursorIndex < targetList.size()) {
                    Token next = targetList.get(cursorIndex);
                    if (next.isContainer) {
                        currentContainer = next;
                        cursorIndex = 0;
                        Token inner = preferInnerEditable(currentContainer);
                        if (inner != null) {
                            currentContainer = inner;
                            cursorIndex = 0;
                        }
                        updateView();
                        return;
                    }
                }
            } else if (delta < 0) {
                if (cursorIndex > 0) {
                    Token prev = targetList.get(cursorIndex - 1);
                    if (prev.isContainer) {
                        currentContainer = prev;
                        cursorIndex = prev.children.size();
                        if ("\\int_def".equals(prev.value)) {
                            currentContainer = prev.children.get(2);
                            cursorIndex = currentContainer.children.size();
                        } else if ("\\int".equals(prev.value)) {
                            currentContainer = prev.children.get(0);
                            cursorIndex = currentContainer.children.size();
                        }
                        updateView();
                        return;
                    }
                }
            }
            updateView();
            return;
        }

        if (delta < 0) {
            if (currentContainer != null) {
                Token parent = currentContainer.parent;
                List<Token> parentList = (parent == null) ? rootTokens : parent.children;
                int idx = parentList.indexOf(currentContainer);
                if (parent != null && "\\int_def".equals(parent.value)) {
                    currentContainer = parent.children.get(1);
                    cursorIndex = currentContainer.children.size();
                } else if (parent != null && "\\int".equals(parent.value)) {
                    currentContainer = parent.children.get(0);
                    cursorIndex = currentContainer.children.size();
                } else {
                    currentContainer = parent;
                    cursorIndex = (currentContainer == null) ? idx : idx;
                }
            }
        } else {
            if (currentContainer != null) {
                Token parent = currentContainer.parent;
                List<Token> parentList = (parent == null) ? rootTokens : parent.children;
                int idx = parentList.indexOf(currentContainer);
                if (parent != null && "\\int_def".equals(parent.value)) {
                    currentContainer = parent.children.get(0);
                    cursorIndex = 0;
                } else if (parent != null && "\\int".equals(parent.value)) {
                    Token gp = parent.parent;
                    List<Token> gpList = (gp == null) ? rootTokens : gp.children;
                    int i = gpList.indexOf(parent);
                    currentContainer = gp;
                    cursorIndex = (gp == null) ? (i + 1) : (i + 1);
                } else {
                    currentContainer = parent;
                    cursorIndex = (currentContainer == null) ? (idx + 1) : (idx + 1);
                }
            }
        }
        updateView();
    }

    public void moveCursorLeft(View view) { moveCursorDelta(-1); }
    public void moveCursorRight(View view) { moveCursorDelta(1); }
    public void showNumKeyboard(View view) { keyboardsFlipp.setDisplayedChild(0); }
    public void showAlphaKeyboard(View view) { keyboardsFlipp.setDisplayedChild(1); }
    public void showNumAlgKeyboard(View view) { keyboardsFlipp.setDisplayedChild(2); }
    public void showTrigKeyboard(View view) { keyboardsFlipp.setDisplayedChild(3); }
    public void showAvKeyboard(View view) { keyboardsFlipp.setDisplayedChild(4); }

    public void solve(View view) {
        List<LexToken> lexTokens = AnalisisLexico.analizar(rootTokens);
        StringBuilder sb = new StringBuilder();
        sb.append("=== TOKENS ===\n");
        for (LexToken tok : lexTokens) sb.append(tok.toString()).append("\n");
        if (lexTokens.size() <= 1) {
            sb.append("\nNo hay expresión para analizar.");
            test.setText(sb.toString());
            return;
        }
        try {
            AnalisisSintactico parser = new AnalisisSintactico(lexTokens);
            NodoAST arbol = parser.parse();
            sb.append("\n=== ÁRBOL SINTÁCTICO ===\n");
            sb.append(prettyPrintAST(arbol, 0));
            ResultadoSemantico rs = AnalisisSemantico.analizar(arbol);
            test.setText(sb.toString());
        } catch (RuntimeException ex) {
            sb.append("\nERROR DE SINTAXIS:\n");
            sb.append(ex.getMessage());
            test.setText(sb.toString());
        }
    }

    private String prettyPrintAST(NodoAST nodo, int nivel) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nivel; i++) sb.append("  ");
        sb.append(nodo.token.toString()).append("\n");
        for (NodoAST hijo : nodo.hijos) {
            sb.append(prettyPrintAST(hijo, nivel + 1));
        }
        return sb.toString();
    }

    private String formatTipo(TipoExpresion t) {
        switch (t) {
            case T1_CONSTANTE: return "Constante";
            case T2_VARIABLE: return "Variable";
            case T3_EXPRESION_ALGEBRAICA: return "Expresión algebraica";
            case T4_ECUACION: return "Ecuación";
            case T5_FRACCION_ALGEBRAICA: return "Fracción algebraica";
            case T6_RADICAL: return "Radical";
            case T7_TRIGONOMETRICA: return "Trigonométrica";
            case T8_EXPONENCIAL_LOG: return "Exponencial/Logarítmica";
            case T9_CALCULO: return "Cálculo";
            case ST_INTEGRAL_DEFINIDA: return "Integral definida";
            case ST_INTEGRAL_INDEFINIDA: return "Integral indefinida";
            case ST_POLINOMICA: return "Polinómica";
            case ST_TRIGONOMETRICA: return "Trigonométrica";
            case ST_EXPONENCIAL_LOG: return "Exponencial/Logarítmica";
            case ST_RACIONAL: return "Racional";
            case ST_RADICAL: return "Radical";
            case ST_SIMPLE: return "Simple";
            default: return "Desconocido";
        }
    }


    private String formatSubtipos(java.util.Set<TipoExpresion> s) {
        if (s == null || s.isEmpty()) return "ninguno";
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (TipoExpresion t : s) {
            if (!first) sb.append(", ");
            sb.append(formatTipo(t));
            first = false;
        }
        return sb.toString();
    }

    private String formatErrores(java.util.List<SemanticoError> errs) {
        StringBuilder sb = new StringBuilder();
        for (SemanticoError e : errs) {
            sb.append("- ").append(e.severidad).append(" ").append(e.codigo).append(": ").append(e.mensaje);
            if (e.ruta != null && !e.ruta.isEmpty()) {
                sb.append(" @").append(e.ruta.toString());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private boolean astContiene(NodoAST n, LexToken.Type t) {
        if (n == null || n.token == null) return false;
        if (n.token.type == t) return true;
        for (NodoAST h : n.hijos) if (astContiene(h, t)) return true;
        return false;
    }

    private String planResolucion(ResultadoSemantico rs, NodoAST arbol) {
        if (rs == null) return "SIN_PLAN";
        if (rs.tipoPrincipal == TipoExpresion.T4_ECUACION) return "ECUACION";
        if (rs.tipoPrincipal == TipoExpresion.T9_CALCULO) {
            if (astContiene(arbol, LexToken.Type.INTEGRAL_DEF)) return "INTEGRAL_DEFINIDA";
            if (astContiene(arbol, LexToken.Type.INTEGRAL_INDEF)) return "INTEGRAL_INDEFINIDA";
            return "CALCULO";
        }
        if (rs.tipoPrincipal == TipoExpresion.T7_TRIGONOMETRICA) return "TRIGONOMETRICA";
        if (rs.tipoPrincipal == TipoExpresion.T8_EXPONENCIAL_LOG) return "EXP_LOG";
        if (rs.tipoPrincipal == TipoExpresion.T6_RADICAL) return "RADICAL";
        if (rs.tipoPrincipal == TipoExpresion.T5_FRACCION_ALGEBRAICA) return "FRACCION_ALGEBRAICA";
        if (rs.tipoPrincipal == TipoExpresion.T3_EXPRESION_ALGEBRAICA) return "EXPRESION_ALGEBRAICA";
        if (rs.tipoPrincipal == TipoExpresion.T2_VARIABLE) return "VARIABLE";
        if (rs.tipoPrincipal == TipoExpresion.T1_CONSTANTE) return "CONSTANTE";
        return "SIN_PLAN";
    }
}
