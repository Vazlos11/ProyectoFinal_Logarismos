package com.example.a22100213_proyectointegrador_logarismos.resolucion;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;

import java.util.HashSet;
import java.util.Set;

public class AstUtils {

    public static NodoAST atom(LexToken.Type t, String v, int p) {
        return new NodoAST(new LexToken(t, v, p));
    }

    public static NodoAST number(double v) {
        if (Math.abs(v - Math.rint(v)) < 1e-12) {
            return new NodoAST(new LexToken(LexToken.Type.INTEGER, Long.toString(Math.round(v)), 0));
        }
        String s = Double.toString(v);
        return new NodoAST(new LexToken(LexToken.Type.DECIMAL, s, 0));
    }

    public static NodoAST bin(LexToken.Type t, NodoAST a, NodoAST b, String op, int pr) {
        NodoAST n = new NodoAST(new LexToken(t, op, pr));
        if (a != null) {
            n.hijos.add(a);
            a.parent = n;
        }
        if (b != null) {
            n.hijos.add(b);
            b.parent = n;
        }
        return n;
    }

    public static NodoAST un(LexToken.Type t, NodoAST a, String op, int pr) {
        NodoAST n = new NodoAST(new LexToken(t, op, pr));
        if (a != null) {
            n.hijos.add(a);
            a.parent = n;
        }
        return n;
    }

    public static boolean isConst(NodoAST n) {
        if (n == null || n.token == null) return true;
        if (n.token.type == LexToken.Type.VARIABLE) return false;
        if (n.token.type == LexToken.Type.INTEGRAL_DEF || n.token.type == LexToken.Type.INTEGRAL_INDEF) return false;
        for (NodoAST h : n.hijos) {
            if (!isConst(h)) return false;
        }
        return true;
    }

    public static void collectVars(NodoAST n, java.util.Set<String> out) {
        if (n == null || n.token == null) return;
        if (n.token.type == LexToken.Type.VARIABLE && n.token.value != null) out.add(n.token.value);
        for (NodoAST h : n.hijos) collectVars(h, out);
    }

    public static String singleVarName(NodoAST n) {
        Set<String> vs = new HashSet<>();
        collectVars(n, vs);
        if (vs.isEmpty()) return "x";
        return vs.iterator().next();
    }

    public static boolean contains(NodoAST n, LexToken.Type t) {
        if (n == null) return false;
        if (n.token != null && n.token.type == t) return true;
        for (NodoAST h : n.hijos) if (contains(h, t)) return true;
        return false;
    }

    public static Double evalConst(NodoAST n) {
        if (n == null || n.token == null) return null;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.INTEGER) {
            try { return Double.parseDouble(n.token.value); } catch (Exception e) { return null; }
        }
        if (t == LexToken.Type.DECIMAL) {
            try { return Double.parseDouble(n.token.value); } catch (Exception e) { return null; }
        }
        if (t == LexToken.Type.CONST_E) return Math.E;
        if (t == LexToken.Type.CONST_PI) return Math.PI;
        if (t == LexToken.Type.VARIABLE) return null;
        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB) {
            Double a = evalConst(n.hijos.get(0));
            Double b = evalConst(n.hijos.get(1));
            if (a == null || b == null) return null;
            if (t == LexToken.Type.SUM) return a + b;
            else return a - b;
        }
        if (t == LexToken.Type.MUL) {
            Double a = evalConst(n.hijos.get(0));
            Double b = evalConst(n.hijos.get(1));
            if (a == null || b == null) return null;
            return a * b;
        }
        if (t == LexToken.Type.DIV) {
            Double a = evalConst(n.hijos.get(0));
            Double b = evalConst(n.hijos.get(1));
            if (a == null || b == null) return null;
            if (Math.abs(b) < 1e-15) return null;
            return a / b;
        }
        if (t == LexToken.Type.EXP) {
            Double a = evalConst(n.hijos.get(0));
            Double b = evalConst(n.hijos.get(1));
            if (a == null || b == null) return null;
            return Math.pow(a, b);
        }
        if (t == LexToken.Type.RADICAL) {
            Double a = evalConst(n.hijos.get(0));
            if (a == null) return null;
            if (a < 0) return null;
            return Math.sqrt(a);
        }
        if (t == LexToken.Type.LN) {
            Double a = evalConst(n.hijos.get(0));
            if (a == null || a <= 0) return null;
            return Math.log(a);
        }
        if (t == LexToken.Type.LOG) {
            Double a = evalConst(n.hijos.get(0));
            if (a == null || a <= 0) return null;
            return Math.log(a);
        }
        if (t == LexToken.Type.LOG_BASE10) {
            Double a = evalConst(n.hijos.get(0));
            if (a == null || a <= 0) return null;
            return Math.log10(a);
        }
        if (t == LexToken.Type.LOG_BASE2) {
            Double a = evalConst(n.hijos.get(0));
            if (a == null || a <= 0) return null;
            return Math.log(a) / Math.log(2.0);
        }
        if (t == LexToken.Type.TRIG_SIN) {
            Double a = evalConst(n.hijos.get(0));
            if (a == null) return null;
            return Math.sin(a);
        }
        if (t == LexToken.Type.TRIG_COS) {
            Double a = evalConst(n.hijos.get(0));
            if (a == null) return null;
            return Math.cos(a);
        }
        if (t == LexToken.Type.TRIG_TAN) {
            Double a = evalConst(n.hijos.get(0));
            if (a == null) return null;
            return Math.tan(a);
        }
        return null;
    }

    public static String toSymja(NodoAST n) {
        if (n == null || n.token == null) return null;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL) return n.token.value;
        if (t == LexToken.Type.VARIABLE) return n.token.value == null ? "x" : n.token.value;
        if (t == LexToken.Type.CONST_E) return "E";
        if (t == LexToken.Type.CONST_PI) return "Pi";
        if (t == LexToken.Type.SUM) return "(" + toSymja(n.hijos.get(0)) + "+" + toSymja(n.hijos.get(1)) + ")";
        if (t == LexToken.Type.SUB) return "(" + toSymja(n.hijos.get(0)) + "-" + toSymja(n.hijos.get(1)) + ")";
        if (t == LexToken.Type.MUL) return "(" + toSymja(n.hijos.get(0)) + "*" + toSymja(n.hijos.get(1)) + ")";
        if (t == LexToken.Type.DIV) return "(" + toSymja(n.hijos.get(0)) + "/" + toSymja(n.hijos.get(1)) + ")";
        if (t == LexToken.Type.EXP) return "(" + toSymja(n.hijos.get(0)) + ")^(" + toSymja(n.hijos.get(1)) + ")";
        if (t == LexToken.Type.RADICAL) return "Sqrt[" + toSymja(n.hijos.get(0)) + "]";
        if (t == LexToken.Type.LN) return "Log[" + toSymja(n.hijos.get(0)) + "]";
        if (t == LexToken.Type.LOG_BASE10) return "Log[10," + toSymja(n.hijos.get(0)) + "]";
        if (t == LexToken.Type.LOG_BASE2) return "Log[2," + toSymja(n.hijos.get(0)) + "]";
        if (t == LexToken.Type.TRIG_SIN) return "Sin[" + toSymja(n.hijos.get(0)) + "]";
        if (t == LexToken.Type.TRIG_COS) return "Cos[" + toSymja(n.hijos.get(0)) + "]";
        if (t == LexToken.Type.TRIG_TAN) return "Tan[" + toSymja(n.hijos.get(0)) + "]";
        if (t == LexToken.Type.EQUAL) return toSymja(n.hijos.get(0)) + "==" + toSymja(n.hijos.get(1));
        if (t == LexToken.Type.INTEGRAL_INDEF) {
            String body = n.hijos.size() > 0 ? toSymja(n.hijos.get(0)) : "0";
            String dx = "x";
            if (n.hijos.size() > 1 && n.hijos.get(1) != null && n.hijos.get(1).token != null && n.hijos.get(1).token.value != null) {
                String s = n.hijos.get(1).token.value.trim();
                if (s.startsWith("d") && s.length() > 1) dx = s.substring(1);
            }
            return "Integrate[" + body + "," + dx + "]";
        }
        if (t == LexToken.Type.INTEGRAL_DEF) {
            String a = n.hijos.size() > 0 ? toSymja(n.hijos.get(0)) : "0";
            String b = n.hijos.size() > 1 ? toSymja(n.hijos.get(1)) : "1";
            String body = n.hijos.size() > 2 ? toSymja(n.hijos.get(2)) : "0";
            String dx = "x";
            if (n.hijos.size() > 3 && n.hijos.get(3) != null && n.hijos.get(3).token != null && n.hijos.get(3).token.value != null) {
                String s = n.hijos.get(3).token.value.trim();
                if (s.startsWith("d") && s.length() > 1) dx = s.substring(1);
            }
            return "Integrate[" + body + ",{" + dx + "," + a + "," + b + "}]";
        }
        return null;
    }

    public static String toTeX(NodoAST n) {
        if (n == null || n.token == null) return "";
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL) return n.token.value;
        if (t == LexToken.Type.VARIABLE) return n.token.value == null ? "x" : n.token.value;
        if (t == LexToken.Type.CONST_E) return "e";
        if (t == LexToken.Type.CONST_PI) return "\\pi";
        if (t == LexToken.Type.SUM) return toTeX(n.hijos.get(0)) + "+" + toTeX(n.hijos.get(1));
        if (t == LexToken.Type.SUB) return toTeX(n.hijos.get(0)) + "-" + toTeX(n.hijos.get(1));
        if (t == LexToken.Type.MUL) return toTeX(n.hijos.get(0)) + "\\cdot " + toTeX(n.hijos.get(1));
        if (t == LexToken.Type.DIV) return "\\frac{" + toTeX(n.hijos.get(0)) + "}{" + toTeX(n.hijos.get(1)) + "}";
        if (t == LexToken.Type.EXP) return "{" + toTeX(n.hijos.get(0)) + "}^{" + toTeX(n.hijos.get(1)) + "}";
        if (t == LexToken.Type.RADICAL) return "\\sqrt{" + toTeX(n.hijos.get(0)) + "}";
        if (t == LexToken.Type.LN) return "\\ln\\left(" + toTeX(n.hijos.get(0)) + "\\right)";
        if (t == LexToken.Type.LOG_BASE10) return "\\log_{10}\\left(" + toTeX(n.hijos.get(0)) + "\\right)";
        if (t == LexToken.Type.LOG_BASE2) return "\\log_{2}\\left(" + toTeX(n.hijos.get(0)) + "\\right)";
        if (t == LexToken.Type.TRIG_SIN) return "\\sin\\left(" + toTeX(n.hijos.get(0)) + "\\right)";
        if (t == LexToken.Type.TRIG_COS) return "\\cos\\left(" + toTeX(n.hijos.get(0)) + "\\right)";
        if (t == LexToken.Type.TRIG_TAN) return "\\tan\\left(" + toTeX(n.hijos.get(0)) + "\\right)";
        if (t == LexToken.Type.EQUAL) return toTeX(n.hijos.get(0)) + "=" + toTeX(n.hijos.get(1));
        if (t == LexToken.Type.INTEGRAL_INDEF) {
            String body = n.hijos.size() > 0 ? toTeX(n.hijos.get(0)) : "0";
            String dx = "x";
            if (n.hijos.size() > 1 && n.hijos.get(1) != null && n.hijos.get(1).token != null && n.hijos.get(1).token.value != null) {
                String s = n.hijos.get(1).token.value.trim();
                if (s.startsWith("d") && s.length() > 1) dx = s.substring(1);
            }
            return "\\int " + body + " \\\\, d" + dx;
        }
        if (t == LexToken.Type.INTEGRAL_DEF) {
            String a = n.hijos.size() > 0 ? toTeX(n.hijos.get(0)) : "0";
            String b = n.hijos.size() > 1 ? toTeX(n.hijos.get(1)) : "1";
            String body = n.hijos.size() > 2 ? toTeX(n.hijos.get(2)) : "0";
            String dx = "x";
            if (n.hijos.size() > 3 && n.hijos.get(3) != null && n.hijos.get(3).token != null && n.hijos.get(3).token.value != null) {
                String s = n.hijos.get(3).token.value.trim();
                if (s.startsWith("d") && s.length() > 1) dx = s.substring(1);
            }
            return "\\int_{" + a + "}^{" + b + "} " + body + " \\\\, d" + dx;
        }
        return "";
    }

    public static LexToken tok(LexToken.Type t, String v, int p) {
        return new LexToken(t, v, p);
    }

    public static LexToken tok(LexToken.Type t) {
        return new LexToken(t, null, 0);
    }

    public static NodoAST simplifyIfConst(NodoAST n) {
        Double v = evalConst(n);
        if (v != null) return number(v);
        return n;
    }

    public static NodoAST cloneTree(NodoAST n) {
        if (n == null) return null;
        LexToken tok = n.token == null ? null : new LexToken(n.token.type, n.token.value, n.token.prioridad);
        NodoAST c = new NodoAST(tok);
        for (NodoAST h : n.hijos) {
            NodoAST ch = cloneTree(h);
            if (ch != null) {
                c.hijos.add(ch);
                ch.parent = c;
            }
        }
        return c;
    }
}
