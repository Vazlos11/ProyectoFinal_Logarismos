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
        PREC.put(LexToken.Type.ABS, 9);

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
        PREC.put(LexToken.Type.DERIV, 8);

        PREC.put(LexToken.Type.EXP, 7);

        PREC.put(LexToken.Type.MUL, 6);
        PREC.put(LexToken.Type.DIV, 6);

        PREC.put(LexToken.Type.SUM, 5);
        PREC.put(LexToken.Type.SUB, 5);

        PREC.put(LexToken.Type.INTEGRAL_DEF, 4);
        PREC.put(LexToken.Type.INTEGRAL_INDEF, 4);

        PREC.put(LexToken.Type.DIFFERENTIAL, 3);

        PREC.put(LexToken.Type.EQUAL, 2);

        PREC.put(LexToken.Type.INTEGER, 1);
        PREC.put(LexToken.Type.DECIMAL, 1);
        PREC.put(LexToken.Type.IMAGINARY, 1);
        PREC.put(LexToken.Type.VARIABLE, 1);
        PREC.put(LexToken.Type.CONST_PI, 1);
        PREC.put(LexToken.Type.CONST_E, 1);

        PREC.put(LexToken.Type.SYSTEM_BEGIN, 0);
        PREC.put(LexToken.Type.ROW_SEP, 0);
        PREC.put(LexToken.Type.SYSTEM_END, 0);
        PREC.put(LexToken.Type.ABS_OPEN, 0);
        PREC.put(LexToken.Type.ABS_CLOSE, 0);
    }

    public static List<LexToken> analizar(List<Token> rootTokens) {
        List<LexToken> out = new ArrayList<>();
        lexFromList(rootTokens, out);
        out.removeIf(t -> t == null || t.type == null);
        out = insertImplicitMultiplication(out);
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
            if (t != null && !t.isContainer && "\\frac{d}{dx}".equals(t.value)) { emit(out, LexToken.Type.DERIV, "x"); i++; continue; }
            if (t != null && !t.isContainer && "\\frac{d}{dy}".equals(t.value)) { emit(out, LexToken.Type.DERIV, "y"); i++; continue; }

            if (isDigitLeaf(t) || isDotLeaf(t)) {
                StringBuilder num = new StringBuilder();
                boolean hasDot = false;
                int j = i;
                while (j < list.size()) {
                    Token tj = list.get(j);
                    if (isDigitLeaf(tj)) { num.append(tj.value); j++; }
                    else if (isDotLeaf(tj) && !hasDot) { num.append("."); hasDot = true; j++; }
                    else break;
                }
                if (j < list.size() && isImagUnitLeaf(list.get(j))) {
                    emit(out, LexToken.Type.IMAGINARY, num.toString() + "i");
                    i = j + 1;
                } else {
                    emit(out, hasDot ? LexToken.Type.DECIMAL : LexToken.Type.INTEGER, num.toString());
                    i = j;
                }
                continue;
            }

            if (isImagUnitLeaf(t)) { emit(out, LexToken.Type.IMAGINARY, "i"); i++; continue; }

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
        if (t.value != null && t.value.startsWith("\\func:")) {
            String name = t.value.substring("\\func:".length());
            emit(out, LexToken.Type.VARIABLE, name);
            emit(out, LexToken.Type.PAREN_OPEN, "(");
            if (!t.children.isEmpty()) {
                Token arg = t.children.get(0);
                if (arg != null && arg.isContainer && "()" .equals(arg.value)) {
                    lexFromList(arg.children, out);
                } else if (arg != null) {
                    lexFromToken(arg, out);
                }
            }
            emit(out, LexToken.Type.PAREN_CLOSE, ")");
            return;
        }

        switch (t.value) {
            case "\\exp":
                emit(out, LexToken.Type.EXP, "^");
                if (!t.children.isEmpty()) {
                    emit(out, LexToken.Type.PAREN_OPEN, "(");
                    lexFromList(t.children, out);
                    emit(out, LexToken.Type.PAREN_CLOSE, ")");
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
                if (!t.children.isEmpty()
                        && t.children.get(0) != null
                        && t.children.get(0).isContainer
                        && "[]".equals(t.children.get(0).value)) {
                    Token idx = t.children.get(0);
                    emit(out, LexToken.Type.RADICAL, "√");
                    emit(out, LexToken.Type.PAREN_OPEN, "(");
                    lexFromList(idx.children, out);
                    emit(out, LexToken.Type.PAREN_CLOSE, ")");

                    emit(out, LexToken.Type.PAREN_OPEN, "(");
                    for (int k = 1; k < t.children.size(); k++) {
                        lexFromToken(t.children.get(k), out);
                    }
                    emit(out, LexToken.Type.PAREN_CLOSE, ")");
                } else {
                    emit(out, LexToken.Type.RADICAL, "√");
                    emit(out, LexToken.Type.PAREN_OPEN, "(");
                    lexFromList(t.children, out);
                    emit(out, LexToken.Type.PAREN_CLOSE, ")");
                }
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

                emit(out, LexToken.Type.PAREN_OPEN, "(");
                if (t.children.size() > 0 && t.children.get(0) != null) {
                    lexFromToken(t.children.get(0), out);
                }
                emit(out, LexToken.Type.PAREN_CLOSE, ")");

                emit(out, LexToken.Type.PAREN_OPEN, "(");
                if (t.children.size() > 1 && t.children.get(1) != null) {
                    lexFromToken(t.children.get(1), out);
                }
                emit(out, LexToken.Type.PAREN_CLOSE, ")");

                emit(out, LexToken.Type.PAREN_OPEN, "(");
                if (t.children.size() > 2 && t.children.get(2) != null) {
                    Token body = t.children.get(2);
                    if (body.isContainer && "()" .equals(body.value)) {
                        lexFromList(body.children, out);
                    } else {
                        lexFromToken(body, out);
                    }
                }
                emit(out, LexToken.Type.PAREN_CLOSE, ")");

                if (t.children.size() > 3 && t.children.get(3) != null) {
                    lexFromToken(t.children.get(3), out);
                }
                break;

            case "\\int":
                emit(out, LexToken.Type.INTEGRAL_INDEF, "∫");
                emit(out, LexToken.Type.PAREN_OPEN, "(");
                if (t.children.size() > 0) {
                    Token body = t.children.get(0);
                    if (body != null && body.isContainer && "()" .equals(body.value)) {
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
                    if (row != null && row.isContainer && "()" .equals(row.value)) {
                        for (Token cell : row.children) lexFromToken(cell, out);
                    } else if (row != null) {
                        lexFromToken(row, out);
                    }
                    if (r < t.children.size() - 1) emit(out, LexToken.Type.ROW_SEP, "\\\\");
                }
                emit(out, LexToken.Type.SYSTEM_END, "}");
                break;

            case "\\cdot":
            case "\\cdot ":
            case "·":
            case "⋅":
            case "×":
            case "\\times":
                emit(out, LexToken.Type.MUL, "*");
                break;

            case "\\sin": emit(out, LexToken.Type.TRIG_SIN, "sin"); emit(out, LexToken.Type.PAREN_OPEN, "("); lexFromList(t.children, out); emit(out, LexToken.Type.PAREN_CLOSE, ")"); break;
            case "\\cos": emit(out, LexToken.Type.TRIG_COS, "cos"); emit(out, LexToken.Type.PAREN_OPEN, "("); lexFromList(t.children, out); emit(out, LexToken.Type.PAREN_CLOSE, ")"); break;
            case "\\tan": emit(out, LexToken.Type.TRIG_TAN, "tan"); emit(out, LexToken.Type.PAREN_OPEN, "("); lexFromList(t.children, out); emit(out, LexToken.Type.PAREN_CLOSE, ")"); break;
            case "\\cot": emit(out, LexToken.Type.TRIG_COT, "cot"); emit(out, LexToken.Type.PAREN_OPEN, "("); lexFromList(t.children, out); emit(out, LexToken.Type.PAREN_CLOSE, ")"); break;
            case "\\sec": emit(out, LexToken.Type.TRIG_SEC, "sec"); emit(out, LexToken.Type.PAREN_OPEN, "("); lexFromList(t.children, out); emit(out, LexToken.Type.PAREN_CLOSE, ")"); break;
            case "\\csc": emit(out, LexToken.Type.TRIG_CSC, "csc"); emit(out, LexToken.Type.PAREN_OPEN, "("); lexFromList(t.children, out); emit(out, LexToken.Type.PAREN_CLOSE, ")"); break;
            case "\\arcsin": emit(out, LexToken.Type.TRIG_ARCSIN, "arcsin"); emit(out, LexToken.Type.PAREN_OPEN, "("); lexFromList(t.children, out); emit(out, LexToken.Type.PAREN_CLOSE, ")"); break;
            case "\\arccos": emit(out, LexToken.Type.TRIG_ARCCOS, "arccos"); emit(out, LexToken.Type.PAREN_OPEN, "("); lexFromList(t.children, out); emit(out, LexToken.Type.PAREN_CLOSE, ")"); break;
            case "\\arctan": emit(out, LexToken.Type.TRIG_ARCTAN, "arctan"); emit(out, LexToken.Type.PAREN_OPEN, "("); lexFromList(t.children, out); emit(out, LexToken.Type.PAREN_CLOSE, ")"); break;
            case "\\arccot": emit(out, LexToken.Type.TRIG_ARCCOT, "arccot"); emit(out, LexToken.Type.PAREN_OPEN, "("); lexFromList(t.children, out); emit(out, LexToken.Type.PAREN_CLOSE, ")"); break;
            case "\\arcsec": emit(out, LexToken.Type.TRIG_ARCSEC, "arcsec"); emit(out, LexToken.Type.PAREN_OPEN, "("); lexFromList(t.children, out); emit(out, LexToken.Type.PAREN_CLOSE, ")"); break;
            case "\\arccsc": emit(out, LexToken.Type.TRIG_ARCCSC, "arccsc"); emit(out, LexToken.Type.PAREN_OPEN, "("); lexFromList(t.children, out); emit(out, LexToken.Type.PAREN_CLOSE, ")"); break;

            case "\\log": emit(out, LexToken.Type.LOG, "log"); emit(out, LexToken.Type.PAREN_OPEN, "("); lexFromList(t.children, out); emit(out, LexToken.Type.PAREN_CLOSE, ")"); break;
            case "\\ln":  emit(out, LexToken.Type.LN,  "ln");  emit(out, LexToken.Type.PAREN_OPEN, "("); lexFromList(t.children, out); emit(out, LexToken.Type.PAREN_CLOSE, ")"); break;

            case "\\log_{2}":
                emit(out, LexToken.Type.LOG_BASE2, "log2");
                emit(out, LexToken.Type.PAREN_OPEN, "(");
                if (t.children.size() > 1) lexFromToken(t.children.get(1), out);
                emit(out, LexToken.Type.PAREN_CLOSE, ")");
                break;

            case "\\log_{10}":
                emit(out, LexToken.Type.LOG_BASE10, "log10");
                emit(out, LexToken.Type.PAREN_OPEN, "(");
                if (t.children.size() > 1) lexFromToken(t.children.get(1), out);
                emit(out, LexToken.Type.PAREN_CLOSE, ")");
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
            case "·":
            case "⋅":
            case "×":
            case "\\times": emit(out, LexToken.Type.MUL, "*"); return;
            case "/": emit(out, LexToken.Type.DIV, "/"); return;
            case "=": emit(out, LexToken.Type.EQUAL, "="); return;
            case "!": emit(out, LexToken.Type.FACTORIAL, "!"); return;
            case "\\%": emit(out, LexToken.Type.PERCENT, "%"); return;
            case "'":   emit(out, LexToken.Type.PRIME, "'");  return;
            case "\\pi": emit(out, LexToken.Type.CONST_PI, "π"); return;
            case "E":    emit(out, LexToken.Type.CONST_E, "e"); return;
            case "e":    emit(out, LexToken.Type.CONST_E, "e"); return;
            case "dx":
            case "dy": emit(out, LexToken.Type.DIFFERENTIAL, vs); return;
            case "(": emit(out, LexToken.Type.PAREN_OPEN, "("); return;
            case ")": emit(out, LexToken.Type.PAREN_CLOSE, ")"); return;
            case "[": emit(out, LexToken.Type.PAREN_OPEN, "["); return;
            case "]": emit(out, LexToken.Type.PAREN_CLOSE, "]"); return;
            case "{": emit(out, LexToken.Type.PAREN_OPEN, "{"); return;
            case "}": emit(out, LexToken.Type.PAREN_CLOSE, "}"); return;
            case "^":
            case "^{}":
                emit(out, LexToken.Type.EXP, "^");
                return;
            case "\\frac{d}{dx}": emit(out, LexToken.Type.DERIV, "x"); return;
            case "\\frac{d}{dy}": emit(out, LexToken.Type.DERIV, "y"); return;
        }

        if (vs.startsWith("^{") && vs.endsWith("}")) {
            emit(out, LexToken.Type.EXP, "^");
            String inside = vs.substring(2, vs.length() - 1);
            if (!inside.isEmpty()) {
                lexFromToken(new Token(inside, false, false), out);
            }
            return;
        }

        if (vs.length() == 2 && vs.charAt(0) == 'd' && Character.isLetter(vs.charAt(1))) {
            emit(out, LexToken.Type.DIFFERENTIAL, vs);
            return;
        }
        if (vs.matches("\\d+"))        { emit(out, LexToken.Type.INTEGER, vs); return; }
        if (vs.matches("\\d+\\.\\d+")) { emit(out, LexToken.Type.DECIMAL, vs); return; }

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
            if (k < chars.length - 1) emit(out, LexToken.Type.MUL, "*");
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

    private static List<LexToken> insertImplicitMultiplication(List<LexToken> in) {
        if (in == null || in.isEmpty()) return in;
        List<LexToken> r = new ArrayList<>();

        boolean inIntDef = false;
        int groupDepth = 0;
        int groupsClosed = 0;

        for (int i = 0; i < in.size(); i++) {
            LexToken a = in.get(i);
            r.add(a);

            if (a.type == LexToken.Type.INTEGRAL_DEF) {
                inIntDef = true;
                groupDepth = 0;
                groupsClosed = 0;
            } else if (inIntDef) {
                if (a.type == LexToken.Type.PAREN_OPEN) {
                    groupDepth++;
                } else if (a.type == LexToken.Type.PAREN_CLOSE) {
                    if (groupDepth > 0) groupDepth--;
                    if (groupDepth == 0 && groupsClosed < 3) {
                        groupsClosed++;
                        if (groupsClosed >= 3) inIntDef = false;
                    }
                } else if (a.type == LexToken.Type.DIFFERENTIAL) {
                    inIntDef = false;
                }
            }

            if (i == in.size() - 1) break;
            LexToken b = in.get(i + 1);

            if (needsMulBetween(a, b)) {
                if (inIntDef
                        && a.type == LexToken.Type.PAREN_CLOSE
                        && b.type == LexToken.Type.PAREN_OPEN
                        && groupDepth == 0
                        && groupsClosed < 3) {
                } else {
                    r.add(new LexToken(LexToken.Type.MUL, "*", PREC.getOrDefault(LexToken.Type.MUL, 6)));
                }
            }
        }
        return r;
    }

    private static boolean needsMulBetween(LexToken a, LexToken b) {
        if (a == null || b == null) return false;

        if (a.type == LexToken.Type.VARIABLE && b.type == LexToken.Type.PAREN_OPEN)
            return false;

        if (!leftImplicitOk(a)) return false;
        if (!rightImplicitOk(b)) return false;

        if (b.type == LexToken.Type.EXP) return false;
        if (b.type == LexToken.Type.DIFFERENTIAL) return false;

        return true;
    }

    private static boolean leftImplicitOk(LexToken t) {
        switch (t.type) {
            case INTEGER: case DECIMAL: case VARIABLE: case IMAGINARY:
            case CONST_E: case CONST_PI:
            case PAREN_CLOSE: case ABS_CLOSE:
            case FACTORIAL: case PERCENT: case PRIME:
                return true;
            default:
                return false;
        }
    }

    private static boolean rightImplicitOk(LexToken t) {
        switch (t.type) {
            case INTEGER: case DECIMAL: case VARIABLE: case IMAGINARY:
            case CONST_E: case CONST_PI:
            case PAREN_OPEN: case ABS_OPEN: case RADICAL:
            case TRIG_SIN: case TRIG_COS: case TRIG_TAN:
            case TRIG_COT: case TRIG_SEC: case TRIG_CSC:
            case TRIG_ARCSIN: case TRIG_ARCCOS: case TRIG_ARCTAN:
            case TRIG_ARCCOT: case TRIG_ARCSEC: case TRIG_ARCCSC:
            case LOG: case LN: case LOG_BASE2: case LOG_BASE10:
                return true;
            default:
                return false;
        }
    }

}
