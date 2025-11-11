package com.example.a22100213_proyectointegrador_logarismos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import android.widget.ViewFlipper;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t2algebra.T2AlgebraResolver;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.AnalisisSemantico;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.SemanticoError;
import com.example.a22100213_proyectointegrador_logarismos.solucion.SolucionActivity;
import com.judemanutd.katexview.KatexView;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private Button btnViewSolution;
    private HorizontalScrollView hsInput, hsAnswer;

    private ArrayList<String> lastStepsLatex = new ArrayList<>();
    private String lastExprLatex = "";
    private boolean lastGraphable = false;
    TextView test;
    private static final int MAX_SYS_ROWS = 3;
    private List<Token> rootTokens = new ArrayList<>();
    private Token currentContainer = null;
    private int cursorIndex = 0;
    private String lastMetodo = "";
    private KatexView katexView;
    private KatexView answer;
    private String lastGrafModo = "";
    private String lastGrafVarX = "x";
    private ViewFlipper keyboardsFlipp;
    private boolean hasResultShown = false;
    private static final String PREFS = "logarismos_prefs";
    private static final String KEY_ANGLE_MODE = "angle_mode";
    private android.widget.Button btnAngle;
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
            "'", "''", "′",
            "=", "+", "-", "/"
    ));

    private static final Map<String, String> ERROR_EXPLANATIONS = new LinkedHashMap<>();
    static {
        ERROR_EXPLANATIONS.put("T1_TOK", "En aritmética solo se permiten números y +, −, ×, ÷, √, ^ y =.");
        ERROR_EXPLANATIONS.put("T2_ARG", "Hay funciones con argumento inválido. Usa, por ejemplo, sin(⋯), log(⋯), ln(⋯).");
        ERROR_EXPLANATIONS.put("T2_PARENS", "Paréntesis no balanceados. Cada '(' debe tener su ')'.");
        ERROR_EXPLANATIONS.put("DIV0", "Se detectó una división entre cero.");
        ERROR_EXPLANATIONS.put("RAD_NEG", "Raíz con argumento negativo.");
        ERROR_EXPLANATIONS.put("LOG_DOM", "El argumento de un logaritmo debe ser mayor que cero.");
        ERROR_EXPLANATIONS.put("ASIN_DOM", "arcsin fuera de dominio. El argumento debe estar en [-1, 1].");
        ERROR_EXPLANATIONS.put("ACOS_DOM", "arccos fuera de dominio. El argumento debe estar en [-1, 1].");
        ERROR_EXPLANATIONS.put("TAN_UNDEF", "tan es indefinida para ese argumento (coseno nulo).");
        ERROR_EXPLANATIONS.put("COT_UNDEF", "cot es indefinida para ese argumento (seno nulo).");
        ERROR_EXPLANATIONS.put("T3_VAR_DX", "La variable del diferencial no coincide con la variable de la función en la derivada.");
        ERROR_EXPLANATIONS.put("T4_DX", "En la integral indefinida falta el diferencial, por ejemplo dx.");
        ERROR_EXPLANATIONS.put("T4_VAR_DX", "La variable del diferencial no coincide en la integral indefinida.");
        ERROR_EXPLANATIONS.put("T5_DX", "En la integral definida falta el diferencial, por ejemplo dx.");
        ERROR_EXPLANATIONS.put("T5_VAR_DX", "La variable del diferencial no coincide en la integral definida.");
        ERROR_EXPLANATIONS.put("T5_LIMS", "Los límites de la integral definida deben ser constantes.");
        ERROR_EXPLANATIONS.put("T5_LIMS_IGUALES", "Los límites de la integral definida no pueden ser iguales.");
        ERROR_EXPLANATIONS.put("T6_EQ", "Para un despeje lineal debe existir una ecuación con '='.");
        ERROR_EXPLANATIONS.put("T6_VAR", "El despeje lineal debe involucrar exactamente una incógnita.");
        ERROR_EXPLANATIONS.put("T6_GRADO", "Para ser lineal el grado debe ser 1.");
        ERROR_EXPLANATIONS.put("T6_FUNC", "No se permiten funciones trigonométricas o logarítmicas en un despeje lineal.");
        ERROR_EXPLANATIONS.put("T7_EQ", "Para un despeje polinómico debe existir una ecuación con '='.");
        ERROR_EXPLANATIONS.put("T7_VAR", "El despeje polinómico debe involucrar exactamente una incógnita.");
        ERROR_EXPLANATIONS.put("T7_FUNC", "No se permiten funciones en un despeje polinómico; debe ser puramente polinómico.");
        ERROR_EXPLANATIONS.put("T7_POLI", "Se esperaba una expresión polinómica en ambos lados de la ecuación.");
        ERROR_EXPLANATIONS.put("T7_COEF", "Los coeficientes del polinomio deben ser enteros (sin π, e ni decimales no enteros).");
        ERROR_EXPLANATIONS.put("T7_GRADO", "Un despeje polinómico debe tener grado mayor o igual a 2.");
        ERROR_EXPLANATIONS.put("T8_CANT", "El sistema debe tener 2 o 3 ecuaciones.");
        ERROR_EXPLANATIONS.put("T8_VAR", "El número de variables debe coincidir con el de ecuaciones.");
        ERROR_EXPLANATIONS.put("T8_FUNC", "En un sistema lineal no se permiten funciones (trig, log, radical, etc.).");
        ERROR_EXPLANATIONS.put("T8_EQ", "Cada fila del sistema debe ser una ecuación con '='.");
        ERROR_EXPLANATIONS.put("T8_VAR_EC", "Cada ecuación del sistema debe incluir todas las variables.");
        ERROR_EXPLANATIONS.put("T8_LINEAL", "Cada ecuación del sistema debe ser de primer grado (lineal).");
        ERROR_EXPLANATIONS.put("T9_MEZCLA", "No mezcles la unidad imaginaria i con derivadas o integrales.");
    }

    private static final Set<String> BIN_OPS = new HashSet<>(Arrays.asList("+", "-", "\\cdot ", "/", "="));

    private static final Map<String, String> latexMap = new HashMap<>();

    static {
        for (int i = 0; i <= 9; i++) latexMap.put(String.valueOf(i), String.valueOf(i));
        latexMap.put("+", "+");
        latexMap.put("-", "-");
        latexMap.put("f()", "f()");
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
        btnViewSolution = findViewById(R.id.btn_view_solution);
        btnViewSolution.setVisibility(View.GONE);
        btnViewSolution.setOnClickListener(this::openSolution);
        katexView = findViewById(R.id.katex_text);
        answer = findViewById(R.id.katex_answer);
        cursorIndex = 0;
        keyboardsFlipp = findViewById(R.id.keyboardsFlipp);

        hsInput = findViewById(R.id.hs_katex_text);
        hsAnswer = findViewById(R.id.hs_katex_answer);

        test = findViewById(R.id.test);
        keyboardsFlipp.setInAnimation(this, android.R.anim.fade_in);
        keyboardsFlipp.setOutAnimation(this, android.R.anim.fade_out);
        updateView();
        btnAngle = findViewById(R.id.btn_angle_mode);
        android.content.SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        String saved = sp.getString(KEY_ANGLE_MODE, "RADIANS");
        applyAngleMode(saved);
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
        if (t.value != null && t.value.startsWith("\\func:")) {
            String name = t.value.substring("\\func:".length());
            sb.append(name);
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
                    if ("()".equals(arg.value)) {
                        appendTokenWithCursor(sb, arg);
                    } else {
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
            case "\\csc": {
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
                } else sb.append("()");
                break;
            }

            case "\\arcsin":
            case "\\arccos":
            case "\\arctan": {
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
                } else sb.append("()");
                break;
            }

            case "\\arcsec":
            case "\\arccsc":
            case "\\arccot": {
                String name = t.value.equals("\\arcsec") ? "\\operatorname{arcsec}"
                        : t.value.equals("\\arccsc") ? "\\operatorname{arccsc}"
                        : "\\operatorname{arccot}";
                sb.append(name);
                if (!t.children.isEmpty()) {
                    Token arg = t.children.get(0);
                    if ("()".equals(arg.value)) {
                        appendTokenWithCursor(sb, arg);
                    } else {
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
                    if ("()".equals(arg.value)) {
                        appendTokenWithCursor(sb, arg);
                    } else {
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

        if ("f(x)".equals(symbol)) {
            insertFunctionSequence(true);
            return;
        }
        if ("f()".equals(symbol)) {
            insertFunctionSequence(false);
            return;
        }

        String latexEquivalent = latexMap.getOrDefault(symbol, symbol);
        Token newToken;

        if ("-".equals(latexEquivalent)) {
            if (isUnaryMinusContext()) {
                Token par = Token.container("()");
                insertToken(par);
                currentContainer = par;
                cursorIndex = 0;
                Token minus = Token.simple("-");
                insertToken(minus);
                updateView();
                return;
            }
        }

        if (latexEquivalent.equals("\\sin") || latexEquivalent.equals("\\cos") ||
                latexEquivalent.equals("\\tan") || latexEquivalent.equals("\\cot") ||
                latexEquivalent.equals("\\sec") || latexEquivalent.equals("\\csc") ||
                latexEquivalent.equals("\\arcsin") || latexEquivalent.equals("\\arccos") ||
                latexEquivalent.equals("\\arctan") || latexEquivalent.equals("\\arccot") ||
                latexEquivalent.equals("\\arcsec") || latexEquivalent.equals("\\arccsc") ||
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
        if ("f(x)".equals(latexEquivalent) || "f()".equals(latexEquivalent)) {
            Token func = Token.container("\\func:f");
            Token parenGroup = Token.container("()");
            parenGroup.parent = func;
            func.children.add(parenGroup);
            insertToken(func);
            currentContainer = parenGroup;
            cursorIndex = 0;
            if ("f(x)".equals(latexEquivalent)) {
                Token xTok = Token.simple("x");
                xTok.parent = parenGroup;
                parenGroup.children.add(xTok);
                cursorIndex = parenGroup.children.size();
            }
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
        if ("\\frac{d}{dx}".equals(latexEquivalent)) {
            Token op = Token.atomic("\\frac{d}{dx}");
            insertToken(op);
            Token par = Token.container("()");
            insertToken(par);
            currentContainer = par;
            cursorIndex = 0;
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

    private boolean isUnaryMinusContext() {
        List<Token> targetList = (currentContainer == null) ? rootTokens : currentContainer.children;
        int pos = cursorIndex;
        if (pos == 0) return true;
        Token prev = targetList.get(pos - 1);
        if (prev == null) return true;
        if (prev.isAtomic && BIN_OPS.contains(prev.value)) return true;
        if (prev.isContainer) return true;
        return false;
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
        if (cont != null && cont.value != null && cont.value.startsWith("\\func:"))
            return cont.children.size() > 0 ? cont.children.get(0) : null;
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
            Token toRemove = targetList.get(cursorIndex - 1);
            targetList.remove(cursorIndex - 1);
            cursorIndex--;
            if (toRemove != null) pruneAroundCursorAfterRemoval(toRemove);
        } else {
            if (currentContainer != null) {
                Token parent = currentContainer.parent;
                List<Token> parentList = (parent == null) ? rootTokens : parent.children;
                int idxInParent = parentList.indexOf(currentContainer);

                if ("^group".equals(currentContainer.value) && currentContainer.children.isEmpty()) {
                    Token exp = currentContainer.parent;
                    if (exp != null && "\\exp".equals(exp.value)) {
                        List<Token> gpList = (exp.parent == null) ? rootTokens : exp.parent.children;
                        int idx = gpList.indexOf(exp);
                        gpList.remove(idx);
                        currentContainer = exp.parent;
                        cursorIndex = Math.max(0, idx);
                        updateView();
                        return;
                    }
                }

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
                        Token removed = currentContainer;
                        currentContainer = parent;
                        cursorIndex = idxInParent;
                        if (removed != null && "\\exp".equals(removed.value)) pruneEmptyExponent(parent, parentList, Math.max(0, idxInParent - 1));
                    } else {
                        currentContainer = parent;
                        cursorIndex = idxInParent;
                    }
                }
            }
        }
        updateView();
    }

    private void pruneAroundCursorAfterRemoval(Token removed) {
        List<Token> list = (currentContainer == null) ? rootTokens : currentContainer.children;
        if (cursorIndex > 0 && cursorIndex - 1 < list.size()) {
            Token prev = list.get(cursorIndex - 1);
            if (prev != null && "\\exp".equals(prev.value)) pruneEmptyExponent(prev.parent, list, cursorIndex - 1);
        }
        if (cursorIndex < list.size()) {
            Token next = list.get(cursorIndex);
            if (next != null && "\\exp".equals(next.value)) pruneEmptyExponent(next.parent, list, cursorIndex);
        }
        if (currentContainer != null && "^group".equals(currentContainer.value) && currentContainer.children.isEmpty()) {
            Token exp = currentContainer.parent;
            if (exp != null && "\\exp".equals(exp.value)) {
                List<Token> gpList = (exp.parent == null) ? rootTokens : exp.parent.children;
                int idx = gpList.indexOf(exp);
                gpList.remove(idx);
                currentContainer = exp.parent;
                cursorIndex = Math.max(0, idx);
            }
        }
    }

    private void pruneEmptyExponent(Token parent, List<Token> list, int idxOfExpCandidate) {
        if (idxOfExpCandidate < 0 || idxOfExpCandidate >= list.size()) return;
        Token exp = list.get(idxOfExpCandidate);
        if (exp == null || !exp.isContainer || !"\\exp".equals(exp.value)) return;
        boolean empty = exp.children.isEmpty()
                || (exp.children.size() == 1 && (exp.children.get(0) == null || exp.children.get(0).children.isEmpty()));
        if (empty) {
            list.remove(idxOfExpCandidate);
            if (cursorIndex > idxOfExpCandidate) cursorIndex--;
        }
    }

    private void moveCursorDelta(int delta) {
        if (currentContainer != null && currentContainer.isContainer && "\\system".equals(currentContainer.value)) {
            if (delta > 0) {
                if (!currentContainer.children.isEmpty()) {
                    currentContainer = currentContainer.children.get(0);
                    cursorIndex = 0;
                    updateViewAfterMove(delta);
                    return;
                }
            } else if (delta < 0) {
                Token p = currentContainer.parent;
                List<Token> pList = (p == null) ? rootTokens : p.children;
                int idx = pList.indexOf(currentContainer);
                currentContainer = p;
                cursorIndex = idx;
                updateViewAfterMove(delta);
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
                    updateViewAfterMove(delta);
                    return;
                }
                if (idxChild < sys.children.size() - 1) {
                    currentContainer = sys.children.get(idxChild + 1);
                    cursorIndex = 0;
                    updateViewAfterMove(delta);
                    return;
                }
                updateViewAfterMove(delta);
                return;
            }

            if (delta < 0) {
                if (cursorIndex > 0) {
                    cursorIndex--;
                    updateViewAfterMove(delta);
                    return;
                }
                if (idxChild > 0) {
                    currentContainer = sys.children.get(idxChild - 1);
                    cursorIndex = currentContainer.children.size();
                    updateViewAfterMove(delta);
                    return;
                }
                updateViewAfterMove(delta);
                return;
            }
        }

        if (currentContainer != null && currentContainer.isContainer && "\\int_def".equals(currentContainer.value)) {
            if (delta > 0) {
                if (currentContainer.children.size() > 1) {
                    currentContainer = currentContainer.children.get(1);
                    cursorIndex = 0;
                    updateViewAfterMove(delta);
                    return;
                }
            } else if (delta < 0) {
                if (currentContainer.children.size() > 2) {
                    currentContainer = currentContainer.children.get(2);
                    cursorIndex = currentContainer.children.size();
                    updateViewAfterMove(delta);
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
                    updateViewAfterMove(delta);
                    return;
                }
                if (idxChild == 1) {
                    currentContainer = p.children.get(0);
                    cursorIndex = 0;
                    updateViewAfterMove(delta);
                    return;
                }
                if (idxChild == 0) {
                    currentContainer = p.children.get(2);
                    cursorIndex = 0;
                    updateViewAfterMove(delta);
                    return;
                }
                if (idxChild == 2) {
                    Token gp = p.parent;
                    List<Token> gpList = (gp == null) ? rootTokens : gp.children;
                    int idx = gpList.indexOf(p);
                    currentContainer = gp;
                    cursorIndex = idx + 1;
                    updateViewAfterMove(delta);
                    return;
                }
            }

            if (delta < 0) {
                if (cursorIndex > 0) {
                    cursorIndex--;
                    updateViewAfterMove(delta);
                    return;
                }
                if (idxChild == 0) {
                    currentContainer = p.children.get(1);
                    cursorIndex = p.children.get(1).children.size();
                    updateViewAfterMove(delta);
                    return;
                }
                if (idxChild == 2) {
                    currentContainer = p.children.get(0);
                    cursorIndex = p.children.get(0).children.size();
                    updateViewAfterMove(delta);
                    return;
                }
                if (idxChild == 1) {
                    Token gp = p.parent;
                    List<Token> gpList = (gp == null) ? rootTokens : gp.children;
                    int idx = gpList.indexOf(p);
                    currentContainer = gp;
                    cursorIndex = idx;
                    updateViewAfterMove(delta);
                    return;
                }
            }
        }

        if (currentContainer != null && currentContainer.isContainer && "\\int".equals(currentContainer.value)) {
            if (delta > 0) {
                Token body = currentContainer.children.get(0);
                currentContainer = body;
                cursorIndex = body.children.size();
                updateViewAfterMove(delta);
                return;
            } else if (delta < 0) {
                Token body = currentContainer.children.get(0);
                currentContainer = body;
                cursorIndex = 0;
                updateViewAfterMove(delta);
                return;
            }
        }

        if (currentContainer != null && currentContainer.isContainer && "\\frac".equals(currentContainer.value)) {
            if (delta > 0) {
                if (currentContainer.children.size() > 0) {
                    currentContainer = currentContainer.children.get(0);
                    cursorIndex = 0;
                    updateViewAfterMove(delta);
                    return;
                }
            } else if (delta < 0) {
                Token p = currentContainer.parent;
                List<Token> pList = (p == null) ? rootTokens : p.children;
                int idx = pList.indexOf(currentContainer);
                currentContainer = p;
                cursorIndex = idx;
                updateViewAfterMove(delta);
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
                    updateViewAfterMove(delta);
                    return;
                }
                if (idxChild == 0 && frac.children.size() > 1) {
                    currentContainer = frac.children.get(1);
                    cursorIndex = 0;
                    updateViewAfterMove(delta);
                    return;
                }
                Token gp = frac.parent;
                List<Token> gpList = (gp == null) ? rootTokens : gp.children;
                int idx = gpList.indexOf(frac);
                currentContainer = gp;
                cursorIndex = idx + 1;
                updateViewAfterMove(delta);
                return;
            }

            if (delta < 0) {
                if (cursorIndex > 0) {
                    cursorIndex--;
                    updateViewAfterMove(delta);
                    return;
                }
                if (idxChild == 1) {
                    currentContainer = frac.children.get(0);
                    cursorIndex = currentContainer.children.size();
                    updateViewAfterMove(delta);
                    return;
                }
                Token gp = frac.parent;
                List<Token> gpList = (gp == null) ? rootTokens : gp.children;
                int idx = gpList.indexOf(frac);
                currentContainer = gp;
                cursorIndex = idx;
                updateViewAfterMove(delta);
                return;
            }
        }

        if (currentContainer != null && currentContainer.isContainer && "\\exp".equals(currentContainer.value)) {
            Token g = preferInnerEditable(currentContainer);
            if (g != null) {
                currentContainer = g;
                if (delta > 0) cursorIndex = g.children.size();
                else cursorIndex = 0;
                updateViewAfterMove(delta);
                return;
            }
        }

        if (currentContainer != null && currentContainer.parent != null && "\\exp".equals(currentContainer.parent.value)) {
            Token exp = currentContainer.parent;
            List<Token> childList = currentContainer.children;

            if (delta > 0) {
                if (cursorIndex < childList.size()) {
                    cursorIndex++;
                    updateViewAfterMove(delta);
                    return;
                }
                Token gp = exp.parent;
                List<Token> gpList = (gp == null) ? rootTokens : gp.children;
                int idx = gpList.indexOf(exp);
                currentContainer = gp;
                cursorIndex = idx + 1;
                updateViewAfterMove(delta);
                return;
            }

            if (delta < 0) {
                if (cursorIndex > 0) {
                    cursorIndex--;
                    updateViewAfterMove(delta);
                    return;
                }
                Token gp = exp.parent;
                List<Token> gpList = (gp == null) ? rootTokens : gp.children;
                int idx = gpList.indexOf(exp);
                currentContainer = gp;
                cursorIndex = idx;
                updateViewAfterMove(delta);
                return;
            }
        }

        if (currentContainer != null && currentContainer.isContainer && "()".equals(currentContainer.value)
                && currentContainer.parent != null && isFuncContainer(currentContainer.parent)) {
            Token arg = currentContainer;
            Token func = arg.parent;
            List<Token> argList = arg.children;

            if (delta > 0) {
                if (cursorIndex < argList.size()) {
                    cursorIndex++;
                    updateViewAfterMove(delta);
                    return;
                }
                Token gp = func.parent;
                List<Token> gpList = (gp == null) ? rootTokens : gp.children;
                int idx = gpList.indexOf(func);
                currentContainer = gp;
                cursorIndex = idx + 1;
                updateViewAfterMove(delta);
                return;
            }

            if (delta < 0) {
                if (cursorIndex > 0) {
                    cursorIndex--;
                    updateViewAfterMove(delta);
                    return;
                }
                Token gp = func.parent;
                List<Token> gpList = (gp == null) ? rootTokens : gp.children;
                int idx = gpList.indexOf(func);
                currentContainer = gp;
                cursorIndex = idx;
                updateViewAfterMove(delta);
                return;
            }
        }

        if (currentContainer != null && currentContainer.isContainer && "{}".equals(currentContainer.value)
                && currentContainer.parent != null
                && ("\\log_{2}".equals(currentContainer.parent.value) || "\\log_{10}".equals(currentContainer.parent.value))) {
            Token base = currentContainer;
            Token logc = base.parent;
            List<Token> baseList = base.children;

            if (delta > 0) {
                if (cursorIndex < baseList.size()) {
                    cursorIndex++;
                    updateViewAfterMove(delta);
                    return;
                }
                if (logc.children.size() > 1) {
                    currentContainer = logc.children.get(1);
                    cursorIndex = 0;
                    updateViewAfterMove(delta);
                    return;
                }
                Token gp = logc.parent;
                List<Token> gpList = (gp == null) ? rootTokens : gp.children;
                int idx = gpList.indexOf(logc);
                currentContainer = gp;
                cursorIndex = idx + 1;
                updateViewAfterMove(delta);
                return;
            }

            if (delta < 0) {
                if (cursorIndex > 0) {
                    cursorIndex--;
                    updateViewAfterMove(delta);
                    return;
                }
                Token gp = logc.parent;
                List<Token> gpList = (gp == null) ? rootTokens : gp.children;
                int idx = gpList.indexOf(logc);
                currentContainer = gp;
                cursorIndex = idx;
                updateViewAfterMove(delta);
                return;
            }
        }

        if (currentContainer != null && currentContainer.isContainer &&
                ("()".equals(currentContainer.value) || "[]".equals(currentContainer.value) ||
                        "\\{ \\}".equals(currentContainer.value) || "\\lvert".equals(currentContainer.value) ||
                        "^group".equals(currentContainer.value) || "{}".equals(currentContainer.value))) {
            if (delta > 0 && cursorIndex == currentContainer.children.size()) {
                Token p = currentContainer.parent;
                List<Token> pList = (p == null) ? rootTokens : p.children;
                int idx = pList.indexOf(currentContainer);
                currentContainer = p;
                cursorIndex = idx + 1;
                updateViewAfterMove(delta);
                return;
            } else if (delta < 0 && cursorIndex == 0) {
                Token p = currentContainer.parent;
                List<Token> pList = (p == null) ? rootTokens : p.children;
                int idx = pList.indexOf(currentContainer);
                currentContainer = p;
                cursorIndex = idx;
                updateViewAfterMove(delta);
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
                        } else if ("\\exp".equals(next.value)) {
                            Token g = preferInnerEditable(next);
                            currentContainer = (g != null) ? g : next;
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
                        updateViewAfterMove(delta);
                        return;
                    }
                }
            } else if (delta < 0) {
                if (cursorIndex > 0) {
                    Token prev = targetList.get(cursorIndex - 1);
                    if (prev.isContainer) {
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
                        } else if ("\\exp".equals(prev.value)) {
                            Token g = preferInnerEditable(prev);
                            currentContainer = (g != null) ? g : prev;
                            cursorIndex = currentContainer.children.size();
                        } else {
                            Token inner = preferInnerEditable(prev);
                            if (inner != null) {
                                currentContainer = inner;
                                cursorIndex = inner.children.size();
                            } else {
                                currentContainer = prev;
                                cursorIndex = prev.children.size();
                            }
                        }
                        updateViewAfterMove(delta);
                        return;
                    }
                }
            }
            updateViewAfterMove(delta);
            return;
        }
    }
    private String humanizeSyntaxMessage(String m) {
        if (m == null) return "Estructura inválida en la expresión. Revisa paréntesis, operadores o exponentes.";
        String s = m.trim();
        s = s.replaceAll("\\bat\\s*position\\s*\\d+", "");
        s = s.replaceAll("\\s+", " ").trim();
        return s.isEmpty() ? "Estructura inválida en la expresión. Revisa paréntesis, operadores o exponentes." : s;
    }

    private String explain(SemanticoError e) {
        String base = ERROR_EXPLANATIONS.getOrDefault(e.codigo, e.mensaje == null ? "" : e.mensaje);
        String pref = (e.severidad == SemanticoError.Severidad.ERROR) ? "Error: " : "Advertencia: ";
        return pref + base;
    }



    private void prepareGraphState(NodoAST arbol,
                                   com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion rr,
                                   com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico rs) {
        com.example.a22100213_proyectointegrador_logarismos.graf.GraphState gs =
                com.example.a22100213_proyectointegrador_logarismos.graf.GraphState.I;
        gs.var = (rs.varIndep == null || rs.varIndep.isEmpty()) ? "x" : rs.varIndep;
        gs.modo = (rs.modoGraf == null) ? "" : rs.modoGraf;
        String mode = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_ANGLE_MODE, "RADIANS");
        gs.radians = "RADIANS".equals(mode);
        String display = lastExprLatex == null ? "" : lastExprLatex;
        gs.labelLatex = display;

        com.example.a22100213_proyectointegrador_logarismos.LexToken.Type t = arbol != null && arbol.token != null ? arbol.token.type : null;

        if (gs.modo != null && gs.modo.startsWith("AREA_DEF_INTEGRAL:")) {
            String[] p = gs.modo.split(":");
            if (p.length == 3) {
                try {
                    gs.limA = Double.valueOf(p[1]);
                    gs.limB = Double.valueOf(p[2]);
                } catch (Exception ignored) {}
            }
            NodoAST cuerpo = localizarCuerpoIntegralDef(arbol);
            gs.ast = cuerpo != null ? cuerpo : arbol;
            return;
        }

        if (rr != null && rr.resultado != null && rs.tipoPrincipal == com.example.a22100213_proyectointegrador_logarismos.Semantico.TipoExpresion.T3_DERIVADA) {
            gs.ast = rr.resultado;
            return;
        }

        if (gs.modo != null && gs.modo.equals("Y_FX_EC_IGUAL_0")) {
            NodoAST eq = localizarEcuacion(arbol);
            if (eq != null && eq.hijos.size() == 2) {
                NodoAST rest = new NodoAST(null);
                rest.hijos.add(eq.hijos.get(0));
                rest.hijos.add(eq.hijos.get(1));
                if (eq.token != null) eq.token.type = com.example.a22100213_proyectointegrador_logarismos.LexToken.Type.SUB;
                gs.ast = rest;
                return;
            }
        }

        if (esAsignacionFuncion(arbol)) {
            gs.ast = arbol.hijos.get(1);
            return;
        }

        if (arbol != null && arbol.token != null
                && arbol.token.type == com.example.a22100213_proyectointegrador_logarismos.LexToken.Type.EQUAL
                && arbol.hijos.size() == 2
                && arbol.hijos.get(0) != null
                && arbol.hijos.get(0).token != null
                && arbol.hijos.get(0).token.type == com.example.a22100213_proyectointegrador_logarismos.LexToken.Type.VARIABLE
                && arbol.hijos.get(0).hijos.isEmpty()) {
            gs.ast = arbol.hijos.get(1);
            return;
        }

        gs.ast = (rr != null && rr.resultado != null) ? rr.resultado : arbol;
    }

    private static boolean esAsignacionFuncion(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (n.token.type != com.example.a22100213_proyectointegrador_logarismos.LexToken.Type.EQUAL) return false;
        NodoAST L = n.hijos.size() > 0 ? n.hijos.get(0) : null;
        if (L == null || L.token == null || L.token.type != com.example.a22100213_proyectointegrador_logarismos.LexToken.Type.VARIABLE) return false;
        String v = L.token.value == null ? "" : L.token.value.trim();
        return v.matches("[a-zA-Z][a-zA-Z0-9_]*\\([a-zA-Z]\\)");
    }

    private static NodoAST localizarCuerpoIntegralDef(NodoAST n) {
        if (n == null) return null;
        if (n.token != null && n.token.type == com.example.a22100213_proyectointegrador_logarismos.LexToken.Type.INTEGRAL_DEF) {
            return n.hijos.size() > 2 ? n.hijos.get(2) : null;
        }
        for (NodoAST h : n.hijos) {
            NodoAST r = localizarCuerpoIntegralDef(h);
            if (r != null) return r;
        }
        return null;
    }

    private static NodoAST localizarEcuacion(NodoAST n) {
        if (n == null) return null;
        if (n.token != null && n.token.type == com.example.a22100213_proyectointegrador_logarismos.LexToken.Type.EQUAL && n.hijos.size() == 2) return n;
        for (NodoAST h : n.hijos) {
            NodoAST r = localizarEcuacion(h);
            if (r != null) return r;
        }
        return null;
    }


    private boolean isFuncContainer(Token t) {
        if (t == null || !t.isContainer || t.value == null) return false;
        if (t.value.startsWith("\\func:")) return true;
        switch (t.value) {
            case "\\sin": case "\\cos": case "\\tan": case "\\cot": case "\\sec": case "\\csc":
            case "\\arcsin": case "\\arccos": case "\\arctan": case "\\arccot": case "\\arcsec": case "\\arccsc":
            case "\\log": case "\\ln": case "\\log_{2}": case "\\log_{10}":
                return true;
            default:
                return false;
        }
    }

    private void updateViewAfterMove(int delta) {
        updateView();
        if (hsInput != null) {
            hsInput.post(() -> hsInput.fullScroll(delta > 0 ? View.FOCUS_RIGHT : View.FOCUS_LEFT));
        }
    }

    private void resetAnswerScroll() {
        if (hsAnswer != null) {
            hsAnswer.post(() -> hsAnswer.fullScroll(View.FOCUS_LEFT));
        }
    }

    private void insertFunctionSequence(boolean withX) {
        List<Token> list = (currentContainer == null) ? rootTokens : currentContainer.children;
        int pos = cursorIndex;

        Token func = Token.container("\\func:f");
        Token arg = Token.container("()");
        arg.parent = func;
        func.children.add(arg);

        if (withX) {
            Token x = Token.simple("x");
            x.parent = arg;
            arg.children.add(x);
        }

        func.parent = currentContainer;
        list.add(pos, func);

        if (withX) {
            currentContainer = (currentContainer == null) ? null : currentContainer;
            cursorIndex = pos + 1;
        } else {
            currentContainer = arg;
            cursorIndex = 0;
        }
        updateViewAfterMove(+1);
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

            lastGraphable = rs.graficable;
            lastGrafModo  = (rs.modoGraf == null) ? "" : rs.modoGraf;
            lastGrafVarX  = (rs.varIndep == null || rs.varIndep.isEmpty()) ? "x" : rs.varIndep;

            if (rs != null && rs.errores != null && !rs.errores.isEmpty()) {
                String msg = formatErrores(rs.errores);
                test.setText(msg);
                answer.setText("");
                hasResultShown = false;
                if (btnViewSolution != null) btnViewSolution.setVisibility(View.GONE);
                return;
            }

            String plan = com.example.a22100213_proyectointegrador_logarismos.Semantico.PlanificadorResolucion.plan(arbol, rs);
            lastMetodo = (plan != null) ? plan.trim() : "";

            com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion rr =
                    com.example.a22100213_proyectointegrador_logarismos.resolucion.MotorResolucion.resolver(arbol, rs);

            if (rr != null) {
                String finalTex = (rr.latexFinal != null && !rr.latexFinal.trim().isEmpty())
                        ? rr.latexFinal.trim()
                        : com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.toTeX(
                        rr.resultado != null ? rr.resultado : arbol
                );
                if (finalTex == null) finalTex = "";
                if (!finalTex.startsWith("$$")) finalTex = "$$\\Large " + finalTex + " $$";
                answer.setText(finalTex);
                resetAnswerScroll();
                hasResultShown = finalTex.trim().length() > 0;

                String exprTex = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.toTeX(arbol);
                if (exprTex == null) exprTex = "";
                if (!exprTex.startsWith("$$")) exprTex = "$$\\Large " + exprTex + " $$";
                lastExprLatex = exprTex;

                prepareGraphState(arbol, rr, rs);

                lastStepsLatex = new ArrayList<>();
                List<com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion> pasos = rr.pasos;
                if (pasos != null) {
                    for (com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion p : pasos) {
                        String tex = p != null ? p.latex : "";
                        if (tex == null) tex = "";
                        String low = tex.toLowerCase(java.util.Locale.ROOT);
                        if (low.contains("formateo") && low.contains("final")) continue;
                        lastStepsLatex.add(tex);
                    }
                }

                lastGraphable = rs.graficable;
                if (btnViewSolution != null) btnViewSolution.setVisibility(hasResultShown ? View.VISIBLE : View.GONE);

                test.setText("");
            } else {
                answer.setText("");
                hasResultShown = false;
                if (btnViewSolution != null) btnViewSolution.setVisibility(View.GONE);
                test.setText("");
            }

        } catch (RuntimeException ex) {
            String m = humanizeSyntaxMessage(ex.getMessage());
            test.setText("Error de sintaxis: " + m);
            answer.setText("");
            hasResultShown = false;
            if (btnViewSolution != null) btnViewSolution.setVisibility(View.GONE);
        } catch (Exception ex) {
            test.setText("Error inesperado al procesar la expresión.");
            answer.setText("");
            hasResultShown = false;
            if (btnViewSolution != null) btnViewSolution.setVisibility(View.GONE);
        } catch (Throwable ex) {
            test.setText("Fallo crítico del motor: " + ex.getClass().getSimpleName());
            answer.setText("");
            hasResultShown = false;
            if (btnViewSolution != null) btnViewSolution.setVisibility(View.GONE);
        }
    }


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

    public void openSolution(View v) {
        if (lastStepsLatex == null) lastStepsLatex = new ArrayList<>();
        Intent i = new Intent(this, com.example.a22100213_proyectointegrador_logarismos.solucion.SolucionActivity.class);
        i.putStringArrayListExtra(com.example.a22100213_proyectointegrador_logarismos.solucion.SolucionActivity.EXTRA_STEPS_LATEX, lastStepsLatex);
        i.putExtra(com.example.a22100213_proyectointegrador_logarismos.solucion.SolucionActivity.EXTRA_EXPR_LATEX, lastExprLatex);
        i.putExtra(com.example.a22100213_proyectointegrador_logarismos.solucion.SolucionActivity.EXTRA_GRAFICABLE, lastGraphable);
        i.putExtra(com.example.a22100213_proyectointegrador_logarismos.solucion.SolucionActivity.EXTRA_GRAF_MODO, lastGrafModo);
        i.putExtra(com.example.a22100213_proyectointegrador_logarismos.solucion.SolucionActivity.EXTRA_GRAF_VARX, lastGrafVarX);
        i.putExtra(com.example.a22100213_proyectointegrador_logarismos.solucion.SolucionActivity.EXTRA_METODO, lastMetodo);
        startActivity(i);
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

    private String formatErrores(List<SemanticoError> errs) {
        LinkedHashSet<String> lines = new LinkedHashSet<>();
        for (SemanticoError e : errs) lines.add("• " + explain(e));
        StringBuilder sb = new StringBuilder();
        for (String line : lines) sb.append(line).append("\n");
        return sb.toString();
    }

    private void applyAngleMode(String mode) {
        if ("DEGREES".equals(mode)) {
            T2AlgebraResolver.setDegrees();
            if (btnAngle != null) btnAngle.setText("Deg");
        } else {
            T2AlgebraResolver.setRadians();
            if (btnAngle != null) btnAngle.setText("Rad");
        }
    }

    public void toggleAngleMode(android.view.View v) {
        android.content.SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        String cur = sp.getString(KEY_ANGLE_MODE, "RADIANS");
        String next = "RADIANS".equals(cur) ? "DEGREES" : "RADIANS";
        sp.edit().putString(KEY_ANGLE_MODE, next).apply();
        applyAngleMode(next);
        if (hasResultShown) {
            solve(null);
        }
    }
}
