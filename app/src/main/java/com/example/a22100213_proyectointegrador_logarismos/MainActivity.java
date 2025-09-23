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
import com.judemanutd.katexview.KatexView;

import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;

import java.util.*;

public class MainActivity extends AppCompatActivity {

    TextView test;
    private static final int MAX_SYS_ROWS = 3;

    private List<Token> rootTokens = new ArrayList<>();
    private Token currentContainer = null;
    private int cursorIndex = 0;
    private KatexView katexView;
    private KatexView answer;
    private ViewFlipper keyboardsFlipp;

    private ExprEvaluator symja;
    private boolean symjaReady = false;

    private static final String CURSOR = "\\textcolor{red}{\\vert}";

    private static final Set<String> ATOMIC_TOKENS = new HashSet<>(Arrays.asList(
            "\\sin", "\\cos", "\\tan", "\\cot", "\\sec", "\\csc",
            "\\arcsin", "\\arccos", "\\arctan", "\\arccot", "\\arcsec", "\\arccsc",
            "\\log", "\\ln",
            "\\frac{d}{dx}", "\\frac{dy}{dx}",
            "dx", "dy",
            "\\cdot ",
            "\\log_{2}",
            "\\log_{10}",
            "\\%",
            "'"
    ));

    private static final Map<String, String> latexMap = new HashMap<>();

    static {
        for (int i = 0; i <= 9; i++) latexMap.put(String.valueOf(i), String.valueOf(i));
        latexMap.put("+", "+");
        latexMap.put("-", "-");
        latexMap.put("*", "\\cdot ");
        latexMap.put("/", "/");
        latexMap.put("%", "\\%");
        latexMap.put("'", "'");
        latexMap.put("=", "=");
        latexMap.put(".", ".");
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
        answer = findViewById(R.id.katex_answer);
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
                if (t.children.size() > 0) {
                    Token num = t.children.get(0);
                    if (num != null && num.isContainer && "()".equals(num.value)) appendChildrenWithCursor(sb, num);
                    else if (num != null) appendTokenWithCursor(sb, num);
                }
                sb.append("}{");
                if (t.children.size() > 1) {
                    Token den = t.children.get(1);
                    if (den != null && den.isContainer && "()".equals(den.value)) appendChildrenWithCursor(sb, den);
                    else if (den != null) appendTokenWithCursor(sb, den);
                }
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
                    if (body != null && body.isContainer && "()".equals(body.value)) appendChildrenWithCursor(sb, body);
                    else if (body != null) appendTokenWithCursor(sb, body);
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
                    if (bodyI != null && bodyI.isContainer && "()".equals(bodyI.value)) appendChildrenWithCursor(sb, bodyI);
                    else if (bodyI != null) appendTokenWithCursor(sb, bodyI);
                }
                if (t.children.size() > 1) {
                    sb.append(" ");
                    appendTokenWithCursor(sb, t.children.get(1));
                }
                break;
            case "\\system":
                sb.append("\\left\\{\\begin{array}{l}");
                for (int r = 0; r < t.children.size(); r++) {
                    if (r > 0) sb.append(" \\\\ ");
                    Token row = t.children.get(r);
                    if (row != null && row.isContainer && "()".equals(row.value)) appendChildrenWithCursor(sb, row);
                    else if (row != null) appendTokenWithCursor(sb, row);
                }
                sb.append("\\end{array}\\right.");
                break;
            case "\\log":
            case "\\ln": {
                sb.append(t.value);
                if (!t.children.isEmpty()) {
                    Token arg = t.children.get(0);
                    if ("()".equals(arg.value)) appendTokenWithCursor(sb, arg);
                    else {
                        sb.append("(");
                        appendTokenWithCursor(sb, arg);
                        sb.append(")");
                    }
                } else sb.append("()");
                break;
            }
            case "\\sin":
            case "\\cos":
            case "\\tan":
            case "\\cot":
            case "\\sec":
            case "\\csc":
            case "\\arcsin":
            case "\\arccos":
            case "\\arctan":
            case "\\arccot":
            case "\\arcsec":
            case "\\arccsc": {
                sb.append(t.value);
                if (!t.children.isEmpty()) {
                    Token arg = t.children.get(0);
                    if ("()".equals(arg.value)) appendTokenWithCursor(sb, arg);
                    else {
                        sb.append("(");
                        appendTokenWithCursor(sb, arg);
                        sb.append(")");
                    }
                } else sb.append("()");
                break;
            }
            case "\\log_{2}":
            case "\\log_{10}": {
                sb.append("\\log_{");
                if (t.children.size() > 0) appendTokenWithCursor(sb, t.children.get(0));
                sb.append("}");
                if (t.children.size() > 1) {
                    Token arg = t.children.get(1);
                    if ("()".equals(arg.value)) appendTokenWithCursor(sb, arg);
                    else {
                        sb.append("(");
                        appendTokenWithCursor(sb, arg);
                        sb.append(")");
                    }
                } else sb.append("()");
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

        if ("\\{ \\}".equals(latexEquivalent)) {
            Token sys = null;
            if (currentContainer != null && "\\system".equals(currentContainer.value)) sys = currentContainer;
            else if (currentContainer != null && currentContainer.parent != null && "\\system".equals(currentContainer.parent.value)) sys = currentContainer.parent;

            if (sys != null) {
                if (sys.children.size() < MAX_SYS_ROWS) {
                    Token row = Token.container("()");
                    row.parent = sys;
                    sys.children.add(row);
                    currentContainer = row;
                    cursorIndex = 0;
                    updateView();
                } else {
                    currentContainer = sys.children.get(sys.children.size() - 1);
                    cursorIndex = currentContainer.children.size();
                    updateView();
                }
                return;
            }

            newToken = Token.system(1);
            insertToken(newToken);
            currentContainer = newToken.children.get(0);
            cursorIndex = 0;
            updateView();
            return;
        }

        if (isContainerSymbol(latexEquivalent)) {
            Token cont;
            switch (latexEquivalent) {
                case "\\sqrt{}": cont = Token.container("\\sqrt"); break;
                case "\\lvert \\rvert": cont = Token.container("\\lvert"); break;
                case "[]": cont = Token.container("[]"); break;
                default: cont = Token.container("()");
            }
            insertToken(cont);
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

        if (token.isContainer && !"\\exp".equals(token.value) && !"\\system".equals(token.value)) {
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
        if ("\\system".equals(cont.value)) return cont.children.size() > 0 ? cont.children.get(0) : null;
        if ("\\int_def".equals(cont.value)) return cont.children.size() > 1 ? cont.children.get(1) : null;
        if ("\\int".equals(cont.value)) return cont.children.size() > 0 ? cont.children.get(0) : null;
        if ("\\frac".equals(cont.value)) return cont.children.size() > 0 ? cont.children.get(0) : null;
        if ("\\exp".equals(cont.value)) return cont.children.isEmpty() ? null : cont.children.get(0);
        if ("\\log_{2}".equals(cont.value) || "\\log_{10}".equals(cont.value)) return cont.children.size() > 1 ? cont.children.get(1) : null;
        if (cont.children.size() == 1) {
            Token c0 = cont.children.get(0);
            if (c0 != null && c0.isContainer) {
                String v = c0.value;
                if ("()".equals(v) || "[]".equals(v) || "\\{ \\}".equals(v) || "\\lvert".equals(v) || "^group".equals(v)) return c0;
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

                if (parent != null && "\\system".equals(parent.value)) {
                    if (targetList.isEmpty()) {
                        parentList.remove(idxInParent);
                        if (parent.children.isEmpty()) {
                            Token row = Token.container("()");
                            row.parent = parent;
                            parent.children.add(row);
                            currentContainer = row;
                            cursorIndex = 0;
                        } else {
                            int newIdx = Math.min(idxInParent, parent.children.size() - 1);
                            currentContainer = parent.children.get(newIdx);
                            cursorIndex = 0;
                        }
                    } else {
                        currentContainer = parent;
                        cursorIndex = idxInParent;
                    }
                } else if (parent != null && "\\int_def".equals(parent.value)) {
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

    private void moveCursorDelta(int delta) {
        if (currentContainer != null && currentContainer.isContainer && "\\system".equals(currentContainer.value)) {
            if (delta > 0) {
                if (!currentContainer.children.isEmpty()) {
                    currentContainer = currentContainer.children.get(0);
                    cursorIndex = 0;
                    updateView();
                    return;
                }
            } else if (delta < 0) {
                Token p = currentContainer.parent;
                List<Token> pList = (p == null) ? rootTokens : p.children;
                int idx = pList.indexOf(currentContainer);
                currentContainer = p;
                cursorIndex = idx;
                updateView();
                return;
            }
        }

        if (currentContainer != null && currentContainer.parent != null && "\\system".equals(currentContainer.parent.value)) {
            Token sys = currentContainer.parent;
            int idxChild = sys.children.indexOf(currentContainer);
            List<Token> childList = currentContainer.children;

            if (delta > 0) {
                if (cursorIndex < childList.size()) {
                    cursorIndex++;
                    updateView();
                    return;
                }
                if (idxChild < sys.children.size() - 1) {
                    currentContainer = sys.children.get(idxChild + 1);
                    cursorIndex = 0;
                    updateView();
                    return;
                }
                updateView();
                return;
            }

            if (delta < 0) {
                if (cursorIndex > 0) {
                    cursorIndex--;
                    updateView();
                    return;
                }
                if (idxChild > 0) {
                    currentContainer = sys.children.get(idxChild - 1);
                    cursorIndex = currentContainer.children.size();
                    updateView();
                    return;
                }
                updateView();
                return;
            }
        }

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

        if (currentContainer != null && currentContainer.isContainer && "\\frac".equals(currentContainer.value)) {
            if (delta > 0) {
                if (currentContainer.children.size() > 0) {
                    currentContainer = currentContainer.children.get(0);
                    cursorIndex = 0;
                    updateView();
                    return;
                }
            } else if (delta < 0) {
                Token p = currentContainer.parent;
                List<Token> pList = (p == null) ? rootTokens : p.children;
                int idx = pList.indexOf(currentContainer);
                currentContainer = p;
                cursorIndex = idx;
                updateView();
                return;
            }
        }

        if (currentContainer != null && currentContainer.parent != null && "\\frac".equals(currentContainer.parent.value)) {
            Token frac = currentContainer.parent;
            int idxChild = frac.children.indexOf(currentContainer);
            List<Token> childList = currentContainer.children;

            if (delta > 0) {
                if (cursorIndex < childList.size()) {
                    cursorIndex++;
                    updateView();
                    return;
                }
                if (idxChild == 0 && frac.children.size() > 1) {
                    currentContainer = frac.children.get(1);
                    cursorIndex = 0;
                    updateView();
                    return;
                }
                Token gp = frac.parent;
                List<Token> gpList = (gp == null) ? rootTokens : gp.children;
                int idx = gpList.indexOf(frac);
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
                if (idxChild == 1) {
                    currentContainer = frac.children.get(0);
                    cursorIndex = currentContainer.children.size();
                    updateView();
                    return;
                }
                Token gp = frac.parent;
                List<Token> gpList = (gp == null) ? rootTokens : gp.children;
                int idx = gpList.indexOf(frac);
                currentContainer = gp;
                cursorIndex = (gp == null) ? idx : idx;
                updateView();
                return;
            }
        }

        if (currentContainer != null && currentContainer.parent != null && "\\int".equals(currentContainer.parent.value)) {
            Token p = currentContainer.parent;
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
                        if ("\\frac".equals(next.value) && next.children.size() > 0) {
                            currentContainer = next.children.get(0);
                            cursorIndex = 0;
                        } else {
                            currentContainer = next;
                            cursorIndex = 0;
                            Token inner = preferInnerEditable(currentContainer);
                            if (inner != null) {
                                currentContainer = inner;
                                cursorIndex = 0;
                            }
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
                        } else if ("\\system".equals(prev.value)) {
                            if (!prev.children.isEmpty()) {
                                currentContainer = prev.children.get(prev.children.size() - 1);
                                cursorIndex = currentContainer.children.size();
                            }
                        } else if ("\\frac".equals(prev.value)) {
                            if (prev.children.size() > 1) {
                                currentContainer = prev.children.get(1);
                                cursorIndex = currentContainer.children.size();
                            } else {
                                currentContainer = prev;
                                cursorIndex = prev.children.size();
                            }
                        }
                        updateView();
                        return;
                    }
                }
            }
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
                } else if (parent != null && "\\system".equals(parent.value)) {
                    currentContainer = parent;
                    cursorIndex = idx;
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
                } else if (parent != null && "\\system".equals(parent.value)) {
                    currentContainer = parent;
                    cursorIndex = idx + 1;
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
        try {
            AnalisisSintactico parser = new AnalisisSintactico(lexTokens);
            NodoAST arbol = parser.parse();
            ResultadoSemantico rs = AnalisisSemantico.analizar(arbol);
            StringBuilder sb = new StringBuilder();
            sb.append("Tipo: ").append(formatTipo(rs.tipoPrincipal)).append("\n");
            if (rs.subtipos != null && !rs.subtipos.isEmpty()) sb.append("Subtipos: ").append(formatSubtipos(rs.subtipos)).append("\n");
            else sb.append("Subtipos: ninguno\n");
            if (rs.errores == null || rs.errores.isEmpty()) sb.append("Errores: ninguno\n");
            else sb.append("Errores:\n").append(formatErrores(rs.errores));
            String plan = com.example.a22100213_proyectointegrador_logarismos.Semantico.PlanificadorResolucion.plan(arbol, rs);
            if (plan != null && !plan.trim().isEmpty() && !"Sin objetivo de resolución".equalsIgnoreCase(plan.trim())) {
                sb.append("\nPlan: ").append(plan);
            }
            test.setText(sb.toString());
        } catch (RuntimeException ex) {
            test.setText("Error de sintaxis: " + ex.getMessage());
        } catch (Exception ignored) {
            test.setText("Ocurrió un problema inesperado al procesar la expresión.");
        }
    }

//    public void solve(View view) {
//        List<LexToken> lexTokens = AnalisisLexico.analizar(rootTokens);
//        try {
//            AnalisisSintactico parser = new AnalisisSintactico(lexTokens);
//            NodoAST arbol = parser.parse();
//            ResultadoSemantico rs = AnalisisSemantico.analizar(arbol);
//
//            StringBuilder sb = new StringBuilder();
//            sb.append("Tipo: ").append(formatTipo(rs.tipoPrincipal)).append("\n");
//            if (rs.subtipos != null && !rs.subtipos.isEmpty()) sb.append("Subtipos: ").append(formatSubtipos(rs.subtipos)).append("\n");
//            else sb.append("Subtipos: ninguno\n");
//            if (rs.errores == null || rs.errores.isEmpty()) sb.append("Errores: ninguno\n");
//            else sb.append("Errores:\n").append(formatErrores(rs.errores));
//            String plan = com.example.a22100213_proyectointegrador_logarismos.Semantico.PlanificadorResolucion.plan(arbol, rs);
//            if (plan != null && !plan.trim().isEmpty() && !"Sin objetivo de resolución".equalsIgnoreCase(plan.trim())) {
//                sb.append("\nPlan: ").append(plan);
//            }
//            test.setText(sb.toString());
//
//            String symjaExpr = toSymja(arbol);
//            boolean symjaOk = false;
//            if (symjaExpr != null && symjaExpr.contains("==")) {
//                String var = pickSingleVar(arbol);
//                if (var != null) {
//                    try {
//                        ExprEvaluator ev = new ExprEvaluator();
//                        IExpr tex = ev.evaluate("ToString(Solve[" + symjaExpr + "," + var + "], TeXForm)");
//                        String out = tex.toString();
//                        if (out != null && out.length() > 1 && out.charAt(0) == '\"' && out.charAt(out.length()-1) == '\"') {
//                            out = out.substring(1, out.length()-1);
//                        }
//                        if (out != null && !out.equals("Null") && !out.isEmpty()) {
//                            answer.setText("$$\\Large " + out + "$$");
//                            symjaOk = true;
//                        }
//                    } catch (Throwable t) {
//                        answer.setText("$$\\Large \\text{Symja falló: " + t.getClass().getSimpleName() + "} $$");
//                    }
//                }
//            }
//
//            if (!symjaOk) {
//                String var = pickSingleVar(arbol);
//                Double lin = trySolveLinear(arbol, var);
//                if (lin != null) {
//                    String latex = var + "=" + (lin % 1.0 == 0 ? String.valueOf(lin.longValue()) : lin.toString());
//                    answer.setText("$$\\Large " + latex + "$$");
//                }
//            }
//        } catch (RuntimeException ex) {
//            test.setText("Error de sintaxis: " + ex.getMessage());
//        } catch (Exception ignored) {
//            test.setText("Ocurrió un problema inesperado al procesar la expresión.");
//        }
//    }
//
//    private static class LinForm {
//        double a; // coeficiente de la variable
//        double b; // término independiente
//        boolean ok;
//        LinForm(double a, double b, boolean ok){ this.a=a; this.b=b; this.ok=ok; }
//    }
//
//    private Double trySolveLinear(NodoAST root, String varName) {
//        if (root == null || root.token == null || root.hijos.size()!=2 || root.token.type != LexToken.Type.EQUAL) return null;
//        LinForm L = toLinear(root.hijos.get(0), varName);
//        LinForm R = toLinear(root.hijos.get(1), varName);
//        if (!L.ok || !R.ok) return null;
//        double a = L.a - R.a;
//        double b = R.b - L.b;
//        if (Math.abs(a) < 1e-12) return null;
//        return b / a;
//    }
//
//    private LinForm toLinear(NodoAST n, String varName) {
//        if (n == null || n.token == null) return new LinForm(0,0,false);
//        LexToken.Type t = n.token.type;
//
//        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL) {
//            try { return new LinForm(0, Double.parseDouble(n.token.value), true); }
//            catch (Exception e) { return new LinForm(0,0,false); }
//        }
//        if (t == LexToken.Type.VARIABLE) {
//            return varName.equals(n.token.value) ? new LinForm(1,0,true) : new LinForm(0,0,false);
//        }
//        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB) {
//            if (n.hijos.size()!=2) return new LinForm(0,0,false);
//            LinForm L = toLinear(n.hijos.get(0), varName);
//            LinForm R = toLinear(n.hijos.get(1), varName);
//            if (!L.ok || !R.ok) return new LinForm(0,0,false);
//            if (t == LexToken.Type.SUM) return new LinForm(L.a+R.a, L.b+R.b, true);
//            return new LinForm(L.a-R.a, L.b-R.b, true);
//        }
//        if (t == LexToken.Type.MUL) {
//            if (n.hijos.size()!=2) return new LinForm(0,0,false);
//            LinForm A = toLinear(n.hijos.get(0), varName);
//            LinForm B = toLinear(n.hijos.get(1), varName);
//            if (A.ok && B.ok) {
//                if (Math.abs(A.a) < 1e-12) return new LinForm(B.a*A.b, B.b*A.b, true);
//                if (Math.abs(B.a) < 1e-12) return new LinForm(A.a*B.b, A.b*B.b, true);
//                return new LinForm(0,0,false);
//            }
//            if (A.ok && Math.abs(A.a) < 1e-12) return new LinForm(0,0,false);
//            if (B.ok && Math.abs(B.a) < 1e-12) return new LinForm(0,0,false);
//            return new LinForm(0,0,false);
//        }
//        if (t == LexToken.Type.DIV) {
//            if (n.hijos.size()!=2) return new LinForm(0,0,false);
//            LinForm A = toLinear(n.hijos.get(0), varName);
//            LinForm B = toLinear(n.hijos.get(1), varName);
//            if (!A.ok || !B.ok) return new LinForm(0,0,false);
//            if (Math.abs(B.a) > 1e-12) return new LinForm(0,0,false);
//            if (Math.abs(B.b) < 1e-12) return new LinForm(0,0,false);
//            return new LinForm(A.a / B.b, A.b / B.b, true);
//        }
//        if (t == LexToken.Type.EXP) {
//            if (n.hijos.size()!=2) return new LinForm(0,0,false);
//            LinForm base = toLinear(n.hijos.get(0), varName);
//            LinForm exp  = toLinear(n.hijos.get(1), varName);
//            if (!base.ok || !exp.ok) return new LinForm(0,0,false);
//            if (Math.abs(exp.a) > 1e-12) return new LinForm(0,0,false);
//            if (Math.abs(base.a) < 1e-12 && Math.abs(exp.b - 1.0) < 1e-12) return new LinForm(0, base.b, true);
//            if (Math.abs(base.a - 1.0) < 1e-12 && Math.abs(base.b) < 1e-12 && Math.abs(exp.b - 1.0) < 1e-12) return new LinForm(1,0,true);
//            return new LinForm(0,0,false);
//        }
//        return new LinForm(0,0,false);
//    }
//
//
//    private String toSymja(NodoAST n) {
//        if (n == null || n.token == null) return null;
//        LexToken.Type t = n.token.type;
//        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL) return n.token.value;
//        if (t == LexToken.Type.CONST_PI) return "Pi";
//        if (t == LexToken.Type.CONST_E) return "E";
//        if (t == LexToken.Type.IMAGINARY) return "I";
//        if (t == LexToken.Type.VARIABLE) return n.token.value;
//        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB || t == LexToken.Type.MUL || t == LexToken.Type.DIV || t == LexToken.Type.EXP || t == LexToken.Type.EQUAL) {
//            if (n.hijos.size() != 2) return null;
//            String a = toSymja(n.hijos.get(0));
//            String b = toSymja(n.hijos.get(1));
//            if (a == null || b == null) return null;
//            if (t == LexToken.Type.SUM) return "(" + a + "+" + b + ")";
//            if (t == LexToken.Type.SUB) return "(" + a + "-" + b + ")";
//            if (t == LexToken.Type.MUL) return "(" + a + "*" + b + ")";
//            if (t == LexToken.Type.DIV) return "(" + a + "/" + b + ")";
//            if (t == LexToken.Type.EXP) return "(" + a + "^" + b + ")";
//            if (t == LexToken.Type.EQUAL) return a + "==" + b;
//        }
//        if (!n.hijos.isEmpty()) {
//            List<String> parts = new ArrayList<>();
//            for (NodoAST h : n.hijos) {
//                String s = toSymja(h);
//                if (s == null) return null;
//                parts.add(s);
//            }
//            if (parts.size() == 1) return parts.get(0);
//            return "(" + String.join("*", parts) + ")";
//        }
//        return null;
//    }
//
//    private String pickSingleVar(NodoAST n) {
//        Set<String> s = new LinkedHashSet<>();
//        collectVars(n, s);
//        if (s.size() == 1) return s.iterator().next();
//        if (s.contains("x")) return "x";
//        if (!s.isEmpty()) return s.iterator().next();
//        return "x";
//    }
//
//    private void collectVars(NodoAST n, Set<String> out) {
//        if (n == null || n.token == null) return;
//        if (n.token.type == LexToken.Type.VARIABLE) out.add(n.token.value);
//        for (NodoAST h : n.hijos) collectVars(h, out);
//    }

    private String formatTipo(com.example.a22100213_proyectointegrador_logarismos.Semantico.TipoExpresion t) {
        switch (t) {
            case T1_ARITMETICA: return "Operación aritmética";
            case T2_ALGEBRA_FUNC: return "Operación algebraica con funciones";
            case T3_DERIVADA: return "Derivada";
            case T4_INTEGRAL_INDEFINIDA: return "Integral indefinida";
            case T5_INTEGRAL_DEFINIDA: return "Integral definida";
            case T6_DESPEJE_LINEAL: return "Despeje algebraico lineal";
            case T7_DESPEJE_POLINOMICO: return "Despeje algebraico polinómico";
            case T8_SISTEMA_EC: return "Sistema de ecuaciones lineales";
            case T9_IMAGINARIOS: return "Expresión con números imaginarios";
            case ST_SIMPLE: return "Simple";
            case ST_POLINOMICA: return "Polinómica";
            case ST_TRIGONOMETRICA: return "Trigonométrica";
            case ST_EXPONENCIAL_LOG: return "Exponencial/Logarítmica";
            case ST_RACIONAL: return "Racional";
            case ST_RADICAL: return "Radical";
            default: return "Desconocido";
        }
    }

    private String formatSubtipos(java.util.Set<com.example.a22100213_proyectointegrador_logarismos.Semantico.TipoExpresion> s) {
        if (s == null || s.isEmpty()) return "ninguno";
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (com.example.a22100213_proyectointegrador_logarismos.Semantico.TipoExpresion t : s) {
            if (!first) sb.append(", ");
            sb.append(formatTipo(t));
            first = false;
        }
        return sb.toString();
    }

    private String formatErrores(java.util.List<SemanticoError> errs) {
        StringBuilder sb = new StringBuilder();
        for (com.example.a22100213_proyectointegrador_logarismos.Semantico.SemanticoError e : errs) {
            sb.append("- ").append(e.severidad).append(" ").append(e.codigo).append(": ").append(e.mensaje);
            if (e.ruta != null && !e.ruta.isEmpty()) sb.append(" @").append(e.ruta.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
