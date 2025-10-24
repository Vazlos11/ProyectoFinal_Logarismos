package com.example.a22100213_proyectointegrador_logarismos.resolucion.t3derivadas;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;

public final class DerivativeUtils {
    public static final class DerivInfo {
        public NodoAST nodoDeriv;
        public NodoAST padre;
        public NodoAST fun;
        public NodoAST dif;
        public String var;
    }
    public static DerivInfo localizarDerivada(NodoAST n) { return localizarDerivadaRec(null, n); }
    private static DerivInfo localizarDerivadaRec(NodoAST parent, NodoAST n) {
        if (n == null || n.token == null) return null;
        if (n.token.type == LexToken.Type.DERIV || n.token.type == LexToken.Type.PRIME) {
            DerivInfo di = new DerivInfo();
            di.nodoDeriv = n;
            di.padre = parent;
            di.fun = n.hijos.size() > 0 ? n.hijos.get(0) : null;
            di.dif = n.hijos.size() > 1 ? n.hijos.get(1) : null;
            di.var = extraerDxVar(di.dif);
            return di;
        }
        for (NodoAST h : n.hijos) {
            DerivInfo di = localizarDerivadaRec(n, h);
            if (di != null) return di;
        }
        return null;
    }
    private static String extraerDxVar(NodoAST dif) {
        if (dif == null || dif.token == null) return "x";
        String s = dif.token.value == null ? "" : dif.token.value.trim();
        if (dif.token.type == LexToken.Type.DIFFERENTIAL) {
            if (s.length() >= 2 && (s.charAt(0) == 'd' || s.charAt(0) == 'D')) return s.substring(1);
            return "x";
        }
        if (dif.token.type == LexToken.Type.VARIABLE && !s.isEmpty()) return s;
        return "x";
    }
    public static boolean esVarPura(NodoAST n, String var) { return n != null && n.token != null && n.token.type == LexToken.Type.VARIABLE && var.equals(n.token.value) && n.hijos.isEmpty(); }
    public static boolean contieneVar(NodoAST n, String var) { java.util.HashSet<String> vs = new java.util.HashSet<>(); AstUtils.collectVars(n, vs); return vs.contains(var); }
    public static boolean esConstantePura(NodoAST n) { if (n == null) return false; Double v = AstUtils.evalConst(n); if (v == null) return false; java.util.HashSet<String> vs = new java.util.HashSet<>(); AstUtils.collectVars(n, vs); return vs.isEmpty(); }
    public static NodoAST var(String v) { return AstUtils.atom(LexToken.Type.VARIABLE, v, 0); }
    public static NodoAST sum(NodoAST a, NodoAST b) { return AstUtils.bin(LexToken.Type.SUM, a, b, "+", 5); }
    public static NodoAST sub(NodoAST a, NodoAST b) { return AstUtils.bin(LexToken.Type.SUB, a, b, "-", 5); }
    public static NodoAST mul(NodoAST a, NodoAST b) { return AstUtils.bin(LexToken.Type.MUL, a, b, "*", 6); }
    public static NodoAST div(NodoAST a, NodoAST b) { return AstUtils.bin(LexToken.Type.DIV, a, b, "/", 6); }
    public static NodoAST pow(NodoAST a, NodoAST b) { return AstUtils.bin(LexToken.Type.EXP, a, b, "^", 7); }
    public static NodoAST ln(NodoAST u) { NodoAST n = new NodoAST(new LexToken(LexToken.Type.LN, "ln", 0)); n.addHijo(u); return n; }
    public static NodoAST sin(NodoAST u) { NodoAST n = new NodoAST(new LexToken(LexToken.Type.TRIG_SIN, "sin", 0)); n.addHijo(u); return n; }
    public static NodoAST cos(NodoAST u) { NodoAST n = new NodoAST(new LexToken(LexToken.Type.TRIG_COS, "cos", 0)); n.addHijo(u); return n; }
    public static NodoAST tan(NodoAST u) { NodoAST n = new NodoAST(new LexToken(LexToken.Type.TRIG_TAN, "tan", 0)); n.addHijo(u); return n; }
    public static NodoAST sqrt(NodoAST u) { return AstUtils.un(LexToken.Type.RADICAL, AstUtils.cloneTree(u), "sqrt", 0); }
    public static NodoAST ePow(NodoAST u) { NodoAST e = new NodoAST(new LexToken(LexToken.Type.CONST_E, null, 0)); return pow(e, u); }
    public static NodoAST deriv(NodoAST u, NodoAST dif) {
        String var = "x";
        NodoAST d = dif == null ? null : AstUtils.cloneTree(dif);
        if (d == null || d.token == null) {
            d = AstUtils.atom(LexToken.Type.DIFFERENTIAL, "dx", 0);
            var = "x";
        } else if (d.token.type == LexToken.Type.DIFFERENTIAL) {
            String s = d.token.value == null ? "" : d.token.value.trim();
            var = (s.length() >= 2 && (s.charAt(0) == 'd' || s.charAt(0) == 'D')) ? s.substring(1) : "x";
            if (s.isEmpty() || !(s.startsWith("d") || s.startsWith("D"))) d.token.value = "d" + var;
        } else if (d.token.type == LexToken.Type.VARIABLE) {
            var = d.token.value == null ? "x" : d.token.value.trim();
            d = AstUtils.atom(LexToken.Type.DIFFERENTIAL, "d" + var, 0);
        } else {
            d = AstUtils.atom(LexToken.Type.DIFFERENTIAL, "dx", 0);
            var = "x";
        }
        return AstUtils.bin(LexToken.Type.DERIV, u, d, var, 0);
    }
}
