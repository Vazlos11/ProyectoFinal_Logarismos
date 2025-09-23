package com.example.a22100213_proyectointegrador_logarismos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalisisLexico {

    private static final Map<LexToken.Type, Integer> PREC = new HashMap<>();
    static {
        PREC.put(LexToken.Type.PAREN_OPEN, 9);
        PREC.put(LexToken.Type.PAREN_CLOSE, 9);
        PREC.put(LexToken.Type.TRIG_SIN, 8);
        PREC.put(LexToken.Type.TRIG_COS, 8);
        PREC.put(LexToken.Type.TRIG_TAN, 8);
        PREC.put(LexToken.Type.TRIG_COT, 8);
        PREC.put(LexToken.Type.TRIG_SEC, 8);
        PREC.put(LexToken.Type.TRIG_CSC, 8);
        PREC.put(LexToken.Type.TRIG_ARCSIN, 8);
        PREC.put(LexToken.Type.TRIG_ARCCOS, 8);
        PREC.put(LexToken.Type.TRIG_ARCTAN, 8);
        PREC.put(LexToken.Type.TRIG_ARCCOT, 8);
        PREC.put(LexToken.Type.TRIG_ARCSEC, 8);
        PREC.put(LexToken.Type.TRIG_ARCCSC, 8);
        PREC.put(LexToken.Type.LOG, 8);
        PREC.put(LexToken.Type.LN, 8);
        PREC.put(LexToken.Type.LOG_BASE2, 8);
        PREC.put(LexToken.Type.LOG_BASE10, 8);
        PREC.put(LexToken.Type.RADICAL, 8);
        PREC.put(LexToken.Type.FACTORIAL, 8);
        PREC.put(LexToken.Type.PERCENT, 8);
        PREC.put(LexToken.Type.PRIME, 8);
        PREC.put(LexToken.Type.DERIV, 9);
        PREC.put(LexToken.Type.ABS, 9);
        PREC.put(LexToken.Type.ABS_OPEN, 0);
        PREC.put(LexToken.Type.ABS_CLOSE, 0);
        PREC.put(LexToken.Type.EXP, 7);
        PREC.put(LexToken.Type.MUL, 6);
        PREC.put(LexToken.Type.DIV, 6);
        PREC.put(LexToken.Type.SUM, 5);
        PREC.put(LexToken.Type.SUB, 5);
        PREC.put(LexToken.Type.INTEGRAL_DEF, 4);
        PREC.put(LexToken.Type.INTEGRAL_INDEF, 4);
        PREC.put(LexToken.Type.DIFFERENTIAL, 3);
        PREC.put(LexToken.Type.EQUAL, 2);
        PREC.put(LexToken.Type.SYSTEM_BEGIN, 0);
        PREC.put(LexToken.Type.ROW_SEP, 0);
        PREC.put(LexToken.Type.SYSTEM_END, 0);
        PREC.put(LexToken.Type.INTEGER, 1);
        PREC.put(LexToken.Type.DECIMAL, 1);
        PREC.put(LexToken.Type.IMAGINARY, 1);
        PREC.put(LexToken.Type.VARIABLE, 1);
        PREC.put(LexToken.Type.CONST_PI, 1);
        PREC.put(LexToken.Type.CONST_E, 1);
    }

    public static List<LexToken> analizar(List<Token> rootTokens) {
        List<LexToken> out = new ArrayList<>();
        lexFromList(rootTokens, out);
        out.removeIf(t -> t == null || t.type == null);
        if (out.isEmpty() || out.get(out.size() - 1).type != LexToken.Type.EOF) {
            out.add(new LexToken(LexToken.Type.EOF, "", 0));
        }
        return out;
    }

    private static void lexFromList(List<Token> list, List<LexToken> out) {
        int i = 0;
        while (i < list.size()) {
            Token t = list.get(i);
            if (t != null && !t.isContainer && "\\frac{d}{d\\,}".equals(t.value)) {
                String var = "";
                int j = i + 1;
                if (j < list.size()) {
                    Token next = list.get(j);
                    if (next != null && !next.isContainer && next.value != null && next.value.matches("[a-zA-Z]")) {
                        var = next.value;
                        i = j + 1;
                        emit(out, LexToken.Type.DERIV, var);
                        continue;
                    }
                }
                emit(out, LexToken.Type.DERIV, var);
                i++;
                continue;
            }
            if (t != null && !t.isContainer && "\\frac{d}{dx}".equals(t.value)) {
                emit(out, LexToken.Type.DERIV, "x");
                i++;
                continue;
            }
            if (t != null && !t.isContainer && "\\frac{d}{dy}".equals(t.value)) {
                emit(out, LexToken.Type.DERIV, "y");
                i++;
                continue;
            }
            if (isDigitLeaf(t) || isDotLeaf(t)) {
                StringBuilder num = new StringBuilder();
                boolean hasDot = false;
                int j = i;
                while (j < list.size()) {
                    Token tj = list.get(j);
                    if (isDigitLeaf(tj)) {
                        num.append(tj.value);
                        j++;
                    } else if (isDotLeaf(tj) && !hasDot) {
                        num.append(".");
                        hasDot = true;
                        j++;
                    } else {
                        break;
                    }
                }
                if (j < list.size() && isImagUnitLeaf(list.get(j))) {
                    String lexeme = num.toString() + "i";
                    emit(out, LexToken.Type.IMAGINARY, lexeme);
                    i = j + 1;
                } else {
                    if (hasDot) emit(out, LexToken.Type.DECIMAL, num.toString());
                    else emit(out, LexToken.Type.INTEGER, num.toString());
                    i = j;
                }
                continue;
            }
            if (isImagUnitLeaf(t)) {
                emit(out, LexToken.Type.IMAGINARY, "i");
                i++;
                continue;
            }
            if (t.isContainer) {
                lexFromContainer(t, out);
                i++;
            } else {
                emitLeaf(t, out);
                i++;
            }
        }
    }

    private static void lexFromContainer(Token t, List<LexToken> out) {
        switch (t.value) {
            case "\\exp":
                emit(out, LexToken.Type.EXP, "^");
                if (!t.children.isEmpty()) {
                    lexFromList(t.children, out);
                }
                break;
            case "^group":
            case "{}":
                lexFromList(t.children, out);
                break;
            case "()":
                emit(out, LexToken.Type.PAREN_OPEN, "(");
                lexFromList(t.children, out);
                emit(out, LexToken.Type.PAREN_CLOSE, ")");
                break;
            case "[]":
                emit(out, LexToken.Type.PAREN_OPEN, "[");
                lexFromList(t.children, out);
                emit(out, LexToken.Type.PAREN_CLOSE, "]");
                break;
            case "\\lvert":
                emit(out, LexToken.Type.ABS_OPEN, "|");
                lexFromList(t.children, out);
                emit(out, LexToken.Type.ABS_CLOSE, "|");
                break;
            case "\\{ \\}":
                emit(out, LexToken.Type.PAREN_OPEN, "{");
                lexFromList(t.children, out);
                emit(out, LexToken.Type.PAREN_CLOSE, "}");
                break;
            case "\\sqrt":
                emit(out, LexToken.Type.RADICAL, "√");
                lexFromList(t.children, out);
                break;
            case "\\frac":
                Token num = t.children.size() > 0 ? t.children.get(0) : null;
                Token den = t.children.size() > 1 ? t.children.get(1) : null;
                emit(out, LexToken.Type.PAREN_OPEN, "(");
                if (num != null) lexFromToken(num, out);
                emit(out, LexToken.Type.PAREN_CLOSE, ")");
                emit(out, LexToken.Type.DIV, "/");
                emit(out, LexToken.Type.PAREN_OPEN, "(");
                if (den != null) lexFromToken(den, out);
                emit(out, LexToken.Type.PAREN_CLOSE, ")");
                break;
            case "\\int_def":
                emit(out, LexToken.Type.INTEGRAL_DEF, "∫");
                for (Token child : t.children) {
                    lexFromToken(child, out);
                }
                break;
            case "\\int":
                emit(out, LexToken.Type.INTEGRAL_INDEF, "∫");
                emit(out, LexToken.Type.PAREN_OPEN, "(");
                if (t.children.size() > 0) {
                    Token body = t.children.get(0);
                    if (body != null && body.isContainer && "()".equals(body.value)) {
                        lexFromList(body.children, out);
                    } else if (body != null) {
                        lexFromToken(body, out);
                    }
                }
                emit(out, LexToken.Type.PAREN_CLOSE, ")");
                if (t.children.size() > 1) {
                    Token dx = t.children.get(1);
                    if (dx != null) lexFromToken(dx, out);
                }
                break;
            case "\\system":
                emit(out, LexToken.Type.SYSTEM_BEGIN, "{");
                for (int r = 0; r < t.children.size(); r++) {
                    Token row = t.children.get(r);
                    if (row != null && row.isContainer && "()".equals(row.value)) {
                        for (Token cell : row.children) lexFromToken(cell, out);
                    } else if (row != null) {
                        lexFromToken(row, out);
                    }
                    if (r < t.children.size() - 1) {
                        emit(out, LexToken.Type.ROW_SEP, "\\\\");
                    }
                }
                emit(out, LexToken.Type.SYSTEM_END, "}");
                break;
            case "\\sin": emit(out, LexToken.Type.TRIG_SIN, "sin"); lexFromList(t.children, out); break;
            case "\\cos": emit(out, LexToken.Type.TRIG_COS, "cos"); lexFromList(t.children, out); break;
            case "\\tan": emit(out, LexToken.Type.TRIG_TAN, "tan"); lexFromList(t.children, out); break;
            case "\\cot": emit(out, LexToken.Type.TRIG_COT, "cot"); lexFromList(t.children, out); break;
            case "\\sec": emit(out, LexToken.Type.TRIG_SEC, "sec"); lexFromList(t.children, out); break;
            case "\\csc": emit(out, LexToken.Type.TRIG_CSC, "csc"); lexFromList(t.children, out); break;
            case "\\arcsin": emit(out, LexToken.Type.TRIG_ARCSIN, "arcsin"); lexFromList(t.children, out); break;
            case "\\arccos": emit(out, LexToken.Type.TRIG_ARCCOS, "arccos"); lexFromList(t.children, out); break;
            case "\\arctan": emit(out, LexToken.Type.TRIG_ARCTAN, "arctan"); lexFromList(t.children, out); break;
            case "\\arccot": emit(out, LexToken.Type.TRIG_ARCCOT, "arccot"); lexFromList(t.children, out); break;
            case "\\arcsec": emit(out, LexToken.Type.TRIG_ARCSEC, "arcsec"); lexFromList(t.children, out); break;
            case "\\arccsc": emit(out, LexToken.Type.TRIG_ARCCSC, "arccsc"); lexFromList(t.children, out); break;
            case "\\log": emit(out, LexToken.Type.LOG, "log"); lexFromList(t.children, out); break;
            case "\\ln":  emit(out, LexToken.Type.LN, "ln");  lexFromList(t.children, out); break;
            case "\\log_{2}":
                emit(out, LexToken.Type.LOG_BASE2, "log2");
                if (t.children.size() > 1) lexFromToken(t.children.get(1), out);
                break;
            case "\\log_{10}":
                emit(out, LexToken.Type.LOG_BASE10, "log10");
                if (t.children.size() > 1) lexFromToken(t.children.get(1), out);
                break;
            default:
                emit(out, LexToken.Type.PAREN_OPEN, "(");
                lexFromList(t.children, out);
                emit(out, LexToken.Type.PAREN_CLOSE, ")");
                break;
        }
    }

    private static void lexFromToken(Token t, List<LexToken> out) {
        if (t.isContainer) lexFromContainer(t, out);
        else emitLeaf(t, out);
    }

    private static void emitLeaf(Token t, List<LexToken> out) {
        String v  = (t.value == null) ? "" : t.value;
        String vs = v.trim();
        switch (vs) {
            case "+": emit(out, LexToken.Type.SUM, "+"); return;
            case "-": emit(out, LexToken.Type.SUB, "-"); return;
            case "\\cdot":
            case "\\cdot ":
            case "*": emit(out, LexToken.Type.MUL, "*"); return;
            case "/": emit(out, LexToken.Type.DIV, "/"); return;
            case "=": emit(out, LexToken.Type.EQUAL, "="); return;
            case "!": emit(out, LexToken.Type.FACTORIAL, "!"); return;
            case "\\%": emit(out, LexToken.Type.PERCENT, "%"); return;
            case "'":   emit(out, LexToken.Type.PRIME, "'");  return;
            case "\\pi": emit(out, LexToken.Type.CONST_PI, "π"); return;
            case "e":    emit(out, LexToken.Type.CONST_E, "e"); return;
            case "dx":
            case "dy": emit(out, LexToken.Type.DIFFERENTIAL, vs); return;
            case "(": emit(out, LexToken.Type.PAREN_OPEN, "("); return;
            case ")": emit(out, LexToken.Type.PAREN_CLOSE, ")"); return;
            case "[": emit(out, LexToken.Type.PAREN_OPEN, "["); return;
            case "]": emit(out, LexToken.Type.PAREN_CLOSE, "]"); return;
            case "{": emit(out, LexToken.Type.PAREN_OPEN, "{"); return;
            case "}": emit(out, LexToken.Type.PAREN_CLOSE, "}"); return;
            case "|": emit(out, LexToken.Type.PAREN_OPEN, "|"); return;
            case "^":
            case "^{}":
                emit(out, LexToken.Type.EXP, "^");
                return;
            case "\\frac{d}{dx}":
                emit(out, LexToken.Type.DERIV, "x");
                return;
            case "\\frac{d}{dy}":
                emit(out, LexToken.Type.DERIV, "y");
                return;
        }
        if (vs.startsWith("^{") && vs.endsWith("}")) {
            emit(out, LexToken.Type.EXP, "^");
            String inside = vs.substring(2, vs.length() - 1);
            if (!inside.isEmpty()) {
                lexFromToken(new Token(inside, false, false), out);
            }
            return;
        }
        if (vs.matches("\\d+"))          { emit(out, LexToken.Type.INTEGER, vs); return; }
        if (vs.matches("\\d+\\.\\d+"))   { emit(out, LexToken.Type.DECIMAL, vs); return; }
        if (vs.equals("log"))   { emit(out, LexToken.Type.LOG, "log"); return; }
        if (vs.equals("ln"))    { emit(out, LexToken.Type.LN, "ln"); return; }
        if (vs.equals("log2"))  { emit(out, LexToken.Type.LOG_BASE2, "log2"); return; }
        if (vs.equals("log10")) { emit(out, LexToken.Type.LOG_BASE10, "log10"); return; }
        if (vs.matches("[a-zA-Z]{2,}")) { emitVariableSequence(vs, out); return; }
        if (vs.matches("[a-zA-Z]"))     { emit(out, LexToken.Type.VARIABLE, vs); return; }
        emit(out, LexToken.Type.VARIABLE, vs);
    }

    private static void emitVariableSequence(String seq, List<LexToken> out) {
        char[] chars = seq.toCharArray();
        for (int k = 0; k < chars.length; k++) {
            emit(out, LexToken.Type.VARIABLE, String.valueOf(chars[k]));
            if (k < chars.length - 1) {
                emit(out, LexToken.Type.MUL, "*");
            }
        }
    }

    private static boolean isDigitLeaf(Token t) {
        return t != null && !t.isContainer && t.value != null && t.value.matches("\\d");
    }

    private static boolean isDotLeaf(Token t) {
        return t != null && !t.isContainer && ".".equals(t.value);
    }

    private static boolean isImagUnitLeaf(Token t) {
        return t != null && !t.isContainer && "i".equals(t.value);
    }

    private static void emit(List<LexToken> out, LexToken.Type type, String value) {
        int p = PREC.getOrDefault(type, 0);
        out.add(new LexToken(type, value, p));
    }
}
