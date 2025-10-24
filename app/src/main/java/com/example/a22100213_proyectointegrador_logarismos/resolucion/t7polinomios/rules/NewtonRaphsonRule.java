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

public final class NewtonRaphsonRule implements T7Rule {

    private static final double EPS = 1e-9;
    private String var;
    private double[] coeffs;
    private double[] dcoeffs;

    @Override
    public String name() { return "Newton–Raphson (polinomios)"; }

    @Override
    public boolean applies(NodoAST raiz, ResultadoSemantico rs) {
        NodoAST eq = buscar(raiz, LexToken.Type.EQUAL);
        if (eq == null) return false;
        Set<String> vs = new LinkedHashSet<>();
        recolectarVars(eq, vs);
        if (vs.size() != 1) return false;
        var = vs.iterator().next();
        NodoAST f = nodoSub(eq.hijos.get(0), eq.hijos.get(1));
        double[] c = coefHasta(f, var, 10);
        int d = grado(c);
        if (d < 1) return false;
        this.coeffs = recorta(c, d);
        this.dcoeffs = deriva(this.coeffs);
        return true;
    }

    @Override
    public ResultadoResolucion solve(NodoAST raiz, ResultadoSemantico rs) {
        ResultadoResolucion rr = new ResultadoResolucion();
        rr.pasos = new LinkedList<>();
        NodoAST eq = buscar(raiz, LexToken.Type.EQUAL);
        rr.pasos.add(new PasoResolucion(AstUtils.toTeX(eq)));
        double x = 0.0;
        rr.pasos.add(new PasoResolucion("x_{0} = " + AstUtils.toTeX(AstUtils.number(x))));
        int it = 0;
        while (it < 30) {
            double fx = horner(coeffs, x);
            double dfx = horner(dcoeffs, x);
            if (!Double.isFinite(fx) || !Double.isFinite(dfx) || Math.abs(dfx) < 1e-15) break;
            double x1 = x - fx / dfx;
            rr.pasos.add(new PasoResolucion("x_{"+(it+1)+"} = " + AstUtils.toTeX(AstUtils.number(x)) + " - \\dfrac{"+ AstUtils.toTeX(AstUtils.number(fx)) +"}{"+ AstUtils.toTeX(AstUtils.number(dfx)) +"} = " + AstUtils.toTeX(AstUtils.number(x1)) ));
            if (Math.abs(x1 - x) < 1e-6) { x = x1; break; }
            x = x1;
            it++;
        }
        NodoAST s = nodoEqual(nodoVar(var), AstUtils.number(x));
        rr.pasos.add(new PasoResolucion(AstUtils.toTeX(s)));
        rr.latexFinal = AstUtils.toTeX(s);
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

    private static double[] coefHasta(NodoAST expr, String v, int max) {
        double[] c = new double[max + 1];
        if (!acum(expr, v, c, max)) return c;
        return c;
    }
    private static NodoAST nodoSub(NodoAST a, NodoAST b) {
        NodoAST n = new NodoAST(new LexToken(LexToken.Type.SUB, "-", 5));
        n.addHijo(a);
        n.addHijo(b);
        return n;
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

    private static int grado(double[] c) {
        for (int i = c.length - 1; i >= 0; i--) if (Math.abs(c[i]) > EPS) return i;
        return 0;
    }

    private static double horner(double[] c, double x) {
        double acc = 0.0;
        for (int i = c.length - 1; i >= 0; i--) acc = acc * x + c[i];
        return acc;
    }

    private static double[] deriva(double[] c) {
        if (c.length <= 1) return new double[]{0.0};
        double[] d = new double[c.length - 1];
        for (int i = 1; i < c.length; i++) d[i - 1] = i * c[i];
        return d;
    }

    private static double[] recorta(double[] c, int deg) {
        double[] r = new double[deg + 1];
        for (int i = 0; i <= deg; i++) r[i] = c[i];
        return r;
    }

    private static NodoAST nodoEqual(NodoAST a, NodoAST b) { NodoAST n = new NodoAST(new LexToken(LexToken.Type.EQUAL, "=", 4)); n.addHijo(a); n.addHijo(b); return n; }
    private static NodoAST nodoVar(String v) { return new NodoAST(new LexToken(LexToken.Type.VARIABLE, v, 9)); }
}
