package com.example.a22100213_proyectointegrador_logarismos.resolucion.t7polinomios.rules;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

public final class RuffiniRule implements T7Rule {

    private static final double EPS = 1e-9;
    private String var;
    private int deg;
    private long[] a;
    private double root;
    private double[] b;

    @Override
    public String name() { return "Ruffini (raíces racionales)"; }

    @Override
    public boolean applies(NodoAST raiz, ResultadoSemantico rs) {
        NodoAST eq = buscar(raiz, LexToken.Type.EQUAL);
        if (eq == null) return false;
        Set<String> vs = new LinkedHashSet<>();
        recolectarVars(eq, vs);
        if (vs.size() != 1) return false;
        var = vs.iterator().next();
        NodoAST f = nodoSub(eq.hijos.get(0), eq.hijos.get(1));
        double[] c = coefHasta(f, var, 4);
        int d = grado(c);
        if (d < 3 || d > 4) return false;
        long[] ci = new long[d + 1];
        for (int i = 0; i <= d; i++) {
            double v = c[i];
            long r = Math.round(v);
            if (Math.abs(v - r) > 1e-6) return false;
            ci[i] = r;
        }
        double r0 = encontrarRaizRacional(ci);
        if (Double.isNaN(r0)) return false;
        this.deg = d;
        this.a = ci;
        this.root = r0;
        this.b = sintetica(ci, r0);
        return true;
    }

    @Override
    public ResultadoResolucion solve(NodoAST raiz, ResultadoSemantico rs) {
        ResultadoResolucion rr = new ResultadoResolucion();
        rr.pasos = new LinkedList<>();
        NodoAST eq = buscar(raiz, LexToken.Type.EQUAL);
        rr.pasos.add(new PasoResolucion(AstUtils.toTeX(eq)));
        double[] coefs = new double[deg + 1];
        for (int i = 0; i <= deg; i++) coefs[i] = a[i];
        rr.pasos.add(new PasoResolucion("\\text{Coeficientes: } " + vectorTex(coefs)));
        rr.pasos.add(new PasoResolucion("\\text{Candidato: } x " + (root >= 0 ? "-" : "+") + AstUtils.toTeX(AstUtils.number(Math.abs(root)))));
        rr.pasos.add(new PasoResolucion("\\text{Fila inferior: } " + vectorTex(b)));
        double rem = b[deg];
        if (Math.abs(rem) > EPS) {
            rr.latexFinal = "\\text{No se encontró raíz racional}";
            rr.resultado = eq;
            return rr;
        }
        double[] q = new double[deg];
        for (int i = 0; i < deg; i++) q[i] = b[i];
        NodoAST qPoly = poly(var, q);
        rr.pasos.add(new PasoResolucion("\\text{Cociente: } " + AstUtils.toTeX(qPoly)));
        NodoAST s = nodoEqual(nodoVar(var), AstUtils.number(root));
        rr.pasos.add(new PasoResolucion(AstUtils.toTeX(s)));
        rr.latexFinal = AstUtils.toTeX(s) + "\\quad " + AstUtils.toTeX(qPoly) + " = 0";
        rr.resultado = s;
        return rr;
    }

    private static NodoAST buscar(NodoAST n, LexToken.Type t) {
        if (n == null) return null;
        if (n.token != null && n.token.type == t) return n;
        for (NodoAST h : n.hijos) {
            NodoAST r = buscar(h, t);
            if (r != null) return r;
        }
        return null;
    }

    private static void recolectarVars(NodoAST n, Set<String> out) {
        if (n == null) return;
        if (n.token != null && n.token.type == LexToken.Type.VARIABLE && n.token.value != null) out.add(n.token.value);
        for (NodoAST h : n.hijos) recolectarVars(h, out);
    }

    private static int grado(double[] c) {
        for (int i = c.length - 1; i >= 0; i--) if (Math.abs(c[i]) > EPS) return i;
        return 0;
    }

    private static double[] coefHasta(NodoAST expr, String v, int max) {
        double[] c = new double[max + 1];
        if (!acum(expr, v, c, max)) return c;
        return c;
    }

    private static boolean acum(NodoAST n, String v, double[] c, int max) {
        if (n == null || n.token == null) return false;
        switch (n.token.type) {
            case INTEGER:
            case DECIMAL:
            case CONST_E:
            case CONST_PI: {
                Double val = AstUtils.evalConst(n);
                c[0] += (val == null ? 0.0 : val);
                return true;
            }
            case VARIABLE: {
                if (!v.equals(n.token.value)) return false;
                if (1 > max) return false;
                c[1] += 1.0;
                return true;
            }
            case SUM:
            case SUB: {
                double[] L = new double[c.length];
                double[] R = new double[c.length];
                if (!acum(n.hijos.get(0), v, L, max)) return false;
                if (!acum(n.hijos.get(1), v, R, max)) return false;
                for (int i = 0; i < c.length; i++) c[i] += L[i] + (n.token.type == LexToken.Type.SUM ? R[i] : -R[i]);
                return true;
            }
            case MUL: {
                double[] L = new double[c.length];
                double[] R = new double[c.length];
                if (!acum(n.hijos.get(0), v, L, max)) return false;
                if (!acum(n.hijos.get(1), v, R, max)) return false;
                double[] T = new double[c.length];
                for (int i = 0; i < L.length; i++)
                    for (int j = 0; j < R.length; j++)
                        if (i + j < T.length) T[i + j] += L[i] * R[j]; else return false;
                for (int i = 0; i < c.length; i++) c[i] += T[i];
                return true;
            }
            case EXP: {
                Double e = AstUtils.evalConst(n.hijos.get(1));
                if (e == null) return false;
                int k = (int)Math.rint(e);
                if (Math.abs(e - k) > 1e-9 || k < 0) return false;
                if (n.hijos.get(0).token != null && n.hijos.get(0).token.type == LexToken.Type.VARIABLE && v.equals(n.hijos.get(0).token.value)) {
                    if (k >= c.length) return false;
                    c[k] += 1.0;
                    return true;
                }
                Double base = AstUtils.evalConst(n.hijos.get(0));
                if (base != null) { c[0] += Math.pow(base, k); return true; }
                return false;
            }
            default:
                return false;
        }
    }

    private static double[] sintetica(long[] coef, double r) {
        int n = coef.length - 1;
        double[] b = new double[n + 1];
        b[0] = coef[0];
        for (int i = 1; i <= n; i++) b[i] = coef[i] + r * b[i - 1];
        return b;
    }

    private static double encontrarRaizRacional(long[] coef) {
        long a0 = coef[coef.length - 1];
        long an = coef[0];
        long[] P = divisores(Math.abs(a0));
        long[] Q = divisores(Math.abs(an));
        for (long p : P) for (long q : Q) {
            double r = (double)p / (double)q;
            if (evalHorner(coef, r) == 0.0) return r;
            if (evalHorner(coef, -r) == 0.0) return -r;
        }
        return Double.NaN;
    }

    private static long[] divisores(long n) {
        if (n == 0) return new long[]{0,1};
        java.util.LinkedHashSet<Long> s = new java.util.LinkedHashSet<>();
        for (long i = 1; i * i <= n; i++) if (n % i == 0) { s.add(i); s.add(n / i); }
        long[] r = new long[s.size()];
        int k = 0; for (Long v : s) r[k++] = v;
        return r;
    }

    private static double evalHorner(long[] c, double x) {
        double acc = 0.0;
        for (int i = 0; i < c.length; i++) acc = acc * x + c[i];
        if (Math.abs(acc) < 1e-12) return 0.0;
        return acc;
    }

    private static String vectorTex(double[] v) {
        StringBuilder sb = new StringBuilder("\\left[");
        for (int i = 0; i < v.length; i++) {
            sb.append(AstUtils.toTeX(AstUtils.number(v[i])));
            if (i + 1 < v.length) sb.append(",\\;");
        }
        sb.append("\\right]");
        return sb.toString();
    }

    private static NodoAST poly(String var, double[] c) {
        NodoAST acc = AstUtils.number(0.0);
        for (int i = c.length - 1; i >= 0; i--) {
            if (Math.abs(c[i]) < 1e-12) continue;
            NodoAST term;
            if (i == 0) term = AstUtils.number(c[i]);
            else if (i == 1) term = nodoMul(AstUtils.number(c[i]), nodoVar(var));
            else term = nodoMul(AstUtils.number(c[i]), nodoExp(nodoVar(var), AstUtils.number(i)));
            acc = nodoAdd(acc, term);
        }
        return acc;
    }

    private static NodoAST nodoAdd(NodoAST a, NodoAST b) { NodoAST n = new NodoAST(new LexToken(LexToken.Type.SUM, "+", 4)); n.addHijo(a); n.addHijo(b); return n; }
    private static NodoAST nodoSub(NodoAST a, NodoAST b) { NodoAST n = new NodoAST(new LexToken(LexToken.Type.SUB, "-", 5)); n.addHijo(a); n.addHijo(b); return n; }
    private static NodoAST nodoMul(NodoAST a, NodoAST b) { NodoAST n = new NodoAST(new LexToken(LexToken.Type.MUL, "*", 6)); n.addHijo(a); n.addHijo(b); return n; }
    private static NodoAST nodoExp(NodoAST a, NodoAST b) { NodoAST n = new NodoAST(new LexToken(LexToken.Type.EXP, "^", 7)); n.addHijo(a); n.addHijo(b); return n; }
    private static NodoAST nodoVar(String v) { return new NodoAST(new LexToken(LexToken.Type.VARIABLE, v, 9)); }
    private static NodoAST nodoEqual(NodoAST a, NodoAST b) { NodoAST n = new NodoAST(new LexToken(LexToken.Type.EQUAL, "=", 4)); n.addHijo(a); n.addHijo(b); return n; }
}
