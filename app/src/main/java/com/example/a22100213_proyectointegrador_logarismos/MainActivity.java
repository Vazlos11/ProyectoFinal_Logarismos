package com.example.a22100213_proyectointegrador_logarismos;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.judemanutd.katexview.KatexView;

import java.util.*;

public class MainActivity extends AppCompatActivity {

    TextView test;
    private List<Token> rootTokens = new ArrayList<>();
    private Token currentContainer = null;   // container activo (o null si estamos en raíz)
    private int cursorIndex = 0;             // índice dentro de currentContainer (o root)
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

        // Exponente
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
                symbol.equals("\\{ \\}") ||
                symbol.equals("\\int") ||
                symbol.equals("\\int dx") ||
                symbol.equals("\\int_def dx");
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
                if (t.children.size() > 0) appendTokenWithCursor(sb, t.children.get(0)); // inf
                sb.append("}^{");
                if (t.children.size() > 1) appendTokenWithCursor(sb, t.children.get(1)); // sup
                sb.append("} ");
                if (t.children.size() > 2) appendTokenWithCursor(sb, t.children.get(2)); // integrando
                if (t.children.size() > 3) {
                    sb.append(" ");
                    appendTokenWithCursor(sb, t.children.get(3)); // dx
                }
                break;

            case "\\int":
                sb.append("\\int ");
                appendChildrenWithCursor(sb, t);
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

            // --- Trigonométricas ---
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
                if (t.children.size() > 0) appendTokenWithCursor(sb, t.children.get(0)); // solo su contenido
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

        // Funciones con argumento entre paréntesis
        if (latexEquivalent.equals("\\sin") || latexEquivalent.equals("\\cos") ||
                latexEquivalent.equals("\\tan") || latexEquivalent.equals("\\cot") ||
                latexEquivalent.equals("\\sec") || latexEquivalent.equals("\\csc") ||
                latexEquivalent.equals("\\ln")  || latexEquivalent.equals("\\log")) {

            newToken = Token.container(latexEquivalent);
            Token parenGroup = Token.container("()");
            parenGroup.parent = newToken;                  // <<<<<< IMPORTANTÍSIMO
            newToken.children.add(parenGroup);

            insertToken(newToken);

            // Colocar el cursor dentro del paréntesis del argumento
            currentContainer = parenGroup;
            cursorIndex = 0;
            updateView();
            return;
        }

        // Logaritmos con base numérica predefinida
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

            // Editar el argumento inmediatamente
            currentContainer = arg;
            cursorIndex = 0;
            updateView();
            return;
        }

        // Exponente: '^' vacío o macro '^{...}'
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

            // Entrar al grupo del exponente
            currentContainer = exponentGroup;
            cursorIndex = exponentGroup.children.size();
            updateView();
            return;
        }

        // Integrales
        if ("\\int".equals(latexEquivalent)) {
            newToken = Token.container("\\int");
            Token argGroup = Token.container("()");
            argGroup.parent = newToken;
            newToken.children.add(argGroup);

            insertToken(newToken);

            currentContainer = argGroup;
            cursorIndex = 0;
            updateView();
            return;
        }

        if ("\\int_dx".equals(latexEquivalent)) {
            newToken = Token.container("\\int");
            Token argGroup = Token.container("()");
            Token diff = Token.atomic("dx");
            argGroup.parent = newToken;
            diff.parent = newToken;
            newToken.children.add(argGroup);
            newToken.children.add(diff);

            insertToken(newToken);

            currentContainer = argGroup;
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

            currentContainer = inf; // comenzar en límite inferior
            cursorIndex = 0;
            updateView();
            return;
        }

        // Otros contenedores
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

        // Tokens atómicos directos
        if (ATOMIC_TOKENS.contains(latexEquivalent)) {
            newToken = Token.atomic(latexEquivalent);
            insertToken(newToken);
            updateView();
            return;
        }

        // Token simple (letra, dígito individual, etc.)
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

        // Unión de dígitos contiguos: 1 + 2 => "12"
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

        // Multiplicación implícita antes de variables/contenedores (excepto exponente)
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

        // Evitar duplicados de "·"
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
            // Al entrar a un contenedor, posicionarse en su "grupo interno" si aplica
            currentContainer = token;
            cursorIndex = 0;
            // Auto-entrar a grupo interno "de envoltura" ((), [], { }, | |, ^group) para edición
            Token inner = preferInnerEditable(currentContainer);
            if (inner != null) {
                currentContainer = inner;
                cursorIndex = 0;
            }
        } else {
            cursorIndex = insertPos + 1;
        }
    }

    // Si el contenedor tiene un único hijo que es grupo "envoltorio" o si es un caso especial retornar ese hijo
    private Token preferInnerEditable(Token cont) {
        if (cont == null || !cont.isContainer) return null;
        if ("\\exp".equals(cont.value)) {
            // exponente siempre tiene "^group"
            return cont.children.isEmpty() ? null : cont.children.get(0);
        }
        if ("\\log_{2}".equals(cont.value) || "\\log_{10}".equals(cont.value)) {
            // el argumento está en children.get(1) si existe
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
            // Borramos el token a la izquierda del cursor
            targetList.remove(cursorIndex - 1);
            cursorIndex--;
        } else {
            // Estamos al inicio del nivel actual: subimos de nivel y, opcionalmente, se puede eliminar el contenedor vacío
            if (currentContainer != null) {
                Token parent = currentContainer.parent;
                List<Token> parentList = (parent == null) ? rootTokens : parent.children;

                int idxInParent = parentList.indexOf(currentContainer);
                // Si el contenedor está vacío, lo eliminamos y posicionamos fuera de él
                if (currentContainer.children.isEmpty()) {
                    parentList.remove(idxInParent);
                    currentContainer = parent;
                    cursorIndex = (currentContainer == null) ? idxInParent : idxInParent;
                } else {
                    // Solo salimos al padre, sin eliminar
                    currentContainer = parent;
                    cursorIndex = (currentContainer == null) ? idxInParent : idxInParent;
                }
            }
        }
        updateView();
    }

    private Token findParent(Token target) {
        return (target == null) ? null : target.parent;
    }

    private void moveCursorDelta(int delta) {
        List<Token> targetList = (currentContainer == null) ? rootTokens : currentContainer.children;
        int newIndex = cursorIndex + delta;

        if (newIndex >= 0 && newIndex <= targetList.size()) {
            cursorIndex = newIndex;

            if (delta > 0) {
                // Si justo en la nueva posición hay un contenedor, entrar en él, y si tiene grupo interno, entrar también
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
                // Si a la izquierda hay un contenedor, entrar a él por su final
                if (cursorIndex > 0) {
                    Token prev = targetList.get(cursorIndex - 1);
                    if (prev.isContainer) {
                        currentContainer = prev;
                        cursorIndex = prev.children.size();
                        updateView();
                        return;
                    }
                }
            }

            updateView();
            return;
        }

        // Salir del contenedor si nos pasamos por izquierda/derecha
        if (delta < 0) {
            if (currentContainer != null) {
                Token parent = currentContainer.parent;
                List<Token> parentList = (parent == null) ? rootTokens : parent.children;
                int idx = parentList.indexOf(currentContainer);
                currentContainer = parent;
                cursorIndex = (currentContainer == null) ? idx : idx;
            }
        } else {
            if (currentContainer != null) {
                Token parent = currentContainer.parent;
                List<Token> parentList = (parent == null) ? rootTokens : parent.children;
                int idx = parentList.indexOf(currentContainer);
                currentContainer = parent;
                cursorIndex = (currentContainer == null) ? (idx + 1) : (idx + 1);
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
}