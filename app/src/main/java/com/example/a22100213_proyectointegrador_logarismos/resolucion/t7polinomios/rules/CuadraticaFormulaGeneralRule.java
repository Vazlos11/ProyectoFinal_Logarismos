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

public final class CuadraticaFormulaGeneralRule implements T7Rule {

    private static final double EPS = 1e-12;
    private String var;

    @Override
    public String name() { return "Ecuación cuadrática (fórmula general)"; }

    @Override
    public boolean applies(NodoAST raiz, ResultadoSemantico rs) {
        NodoAST eq = buscar(raiz, LexToken.Type.EQUAL);
        if (eq == null) return false;
        Set<String> vs = new LinkedHashSet<>();
        recolectarVars(eq, vs);
        if (vs.size() != 1) return false;
        var = vs.iterator().next();
        NodoAST d = nodoSub(eq.hijos.get(0), eq.hijos.get(1));
        double[] abc = coeficientesHasta(d, var, 2);
        double a = abc[2], b = abc[1], c = abc[0];
        if (Math.abs(a) < EPS) return false;
        return isPolinomioEnVar(d, var);
    }

    @Override
    public ResultadoResolucion solve(NodoAST raiz, ResultadoSemantico rs) {
        ResultadoResolucion rr = new ResultadoResolucion();
        rr.pasos = new LinkedList<>();

        NodoAST eq = buscar(raiz, LexToken.Type.EQUAL);
        rr.pasos.add(new PasoResolucion(AstUtils.toTeX(eq)));

        NodoAST d = nodoSub(eq.hijos.get(0), eq.hijos.get(1));
        double[] abc = coeficientesHasta(d, var, 2);
        double a = abc[2], b = abc[1], c = abc[0];

        NodoAST norm = nodoEqual(
                nodoAdd(nodoAdd(nodoMul(AstUtils.number(a), nodoExp(nodoVar(var), AstUtils.number(2))),
                                nodoMul(AstUtils.number(b), nodoVar(var))),
                        AstUtils.number(c)),
                AstUtils.number(0.0)
        );
        rr.pasos.add(new PasoResolucion(AstUtils.toTeX(norm)));

        double disc = b*b - 4*a*c;
        rr.pasos.add(new PasoResolucion("\\Delta = " + AstUtils.toTeX(AstUtils.number(disc))));

        NodoAST den = nodoMul(AstUtils.number(2.0), AstUtils.number(a));
        NodoAST sqrtDisc = nodoRad(AstUtils.number(disc));
        NodoAST x1 = nodoDiv(nodoAdd(AstUtils.number(-b), sqrtDisc), den);
        NodoAST x0 = nodoDiv(nodoSub(AstUtils.number(-b), sqrtDisc), den);

        rr.pasos.add(new PasoResolucion("x_1 = " + AstUtils.toTeX(x1)));
        rr.pasos.add(new PasoResolucion("x_2 = " + AstUtils.toTeX(x0)));

        if (disc < -EPS) {
            rr.latexFinal = "\\text{Sin solución real}";
            rr.resultado = eq;
            return rr;
        }

        Double v1 = AstUtils.evalConst(x1);
        Double v0 = AstUtils.evalConst(x0);
        if (v1 != null && v0 != null) {
            NodoAST s1 = nodoEqual(nodoVar(var), AstUtils.number(v1));
            NodoAST s0 = nodoEqual(nodoVar(var), AstUtils.number(v0));
            rr.pasos.add(new PasoResolucion(AstUtils.toTeX(s1)));
            rr.pasos.add(new PasoResolucion(AstUtils.toTeX(s0)));
            rr.latexFinal = AstUtils.toTeX(s1) + "\\quad " + AstUtils.toTeX(s0);
            rr.resultado = s1;
            return rr;
        }

        rr.latexFinal = "x = \\dfrac{-" + AstUtils.toTeX(AstUtils.number(b)) + " \\pm \\sqrt{" + AstUtils.toTeX(AstUtils.number(disc)) + "}}{" + AstUtils.toTeX(den) + "}";
        rr.resultado = eq;
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

    private static boolean isPolinomioEnVar(NodoAST n, String v) {
        if (n == null || n.token == null) return false;
        switch (n.token.type) {
            case INTEGER:
            case DECIMAL:
            case CONST_E:
            case CONST_PI:
                return true;
            case VARIABLE:
                return v.equals(n.token.value);
            case SUM:
            case SUB:
            case MUL:
                return isPolinomioEnVar(n.hijos.get(0), v) && isPolinomioEnVar(n.hijos.get(1), v);
            case EXP:
                Double ev = AstUtils.evalConst(n.hijos.get(1));
                if (ev == null) return false;
                int k = (int)Math.rint(ev);
                if (Math.abs(ev - k) > 1e-9 || k < 0) return false;
                if (n.hijos.get(0).token != null && n.hijos.get(0).token.type == LexToken.Type.VARIABLE)
                    return v.equals(n.hijos.get(0).token.value);
                Double base = AstUtils.evalConst(n.hijos.get(0));
                return base != null;
            case DIV:
                return false;
            default:
                return false;
        }
    }

    private static double[] coeficientesHasta(NodoAST expr, String v, int max) {
        double[] c = new double[max+1];
        acumCoef(expr, v, c, max);
        return c;
    }

    private static boolean acumCoef(NodoAST n, String v, double[] c, int max) {
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
                if (max < 1) return false;
                c[1] += 1.0;
                return true;
            }
            case SUM:
            case SUB: {
                boolean okL = acumCoef(n.hijos.get(0), v, c, max);
                double[] r = new double[c.length];
                boolean okR = acumCoefCopia(n.hijos.get(1), v, r, max);
                if (!okL || !okR) return false;
                if (n.token.type == LexToken.Type.SUM) for (int i = 0; i < c.length; i++) c[i] += r[i];
                else for (int i = 0; i < c.length; i++) c[i] -= r[i];
                return true;
            }
            case MUL: {
                double[] a = new double[c.length];
                double[] b = new double[c.length];
                if (!acumCoefCopia(n.hijos.get(0), v, a, max)) return false;
                if (!acumCoefCopia(n.hijos.get(1), v, b, max)) return false;
                double[] r = new double[c.length];
                for (int i = 0; i < a.length; i++)
                    for (int j = 0; j < b.length; j++)
                        if (i + j < r.length) r[i + j] += a[i] * b[j]; else return false;
                for (int i = 0; i < c.length; i++) c[i] += r[i];
                return true;
            }
            case EXP: {
                Double ev = AstUtils.evalConst(n.hijos.get(1));
                if (ev == null) return false;
                int k = (int)Math.rint(ev);
                if (Math.abs(ev - k) > 1e-9 || k < 0) return false;
                if (n.hijos.get(0).token != null && n.hijos.get(0).token.type == LexToken.Type.VARIABLE && v.equals(n.hijos.get(0).token.value)) {
                    if (k >= c.length) return false;
                    c[k] += 1.0;
                    return true;
                }
                Double base = AstUtils.evalConst(n.hijos.get(0));
                if (base != null) {
                    c[0] += Math.pow(base, k);
                    return true;
                }
                return false;
            }
            case DIV:
            default:
                return false;
        }
    }

    private static boolean acumCoefCopia(NodoAST n, String v, double[] out, int max) {
        double[] tmp = new double[out.length];
        boolean ok = acumCoef(n, v, tmp, max);
        if (ok) for (int i = 0; i < out.length; i++) out[i] += tmp[i];
        return ok;
    }

    private static NodoAST nodoAdd(NodoAST a, NodoAST b) {
        NodoAST n = new NodoAST(new LexToken(LexToken.Type.SUM, "+", 4));
        n.addHijo(a); n.addHijo(b);
        return n;
    }

    private static NodoAST nodoSub(NodoAST a, NodoAST b) {
        NodoAST n = new NodoAST(new LexToken(LexToken.Type.SUB, "-", 5));
        n.addHijo(a); n.addHijo(b);
        return n;
    }

    private static NodoAST nodoMul(NodoAST a, NodoAST b) {
        NodoAST n = new NodoAST(new LexToken(LexToken.Type.MUL, "*", 6));
        n.addHijo(a); n.addHijo(b);
        return n;
    }

    private static NodoAST nodoDiv(NodoAST a, NodoAST b) {
        NodoAST n = new NodoAST(new LexToken(LexToken.Type.DIV, "/", 6));
        n.addHijo(a); n.addHijo(b);
        return n;
    }

    private static NodoAST nodoExp(NodoAST a, NodoAST b) {
        NodoAST n = new NodoAST(new LexToken(LexToken.Type.EXP, "^", 7));
        n.addHijo(a); n.addHijo(b);
        return n;
    }

    private static NodoAST nodoVar(String v) {
        return new NodoAST(new LexToken(LexToken.Type.VARIABLE, v, 9));
    }

    private static NodoAST nodoRad(NodoAST inside) {
        NodoAST n = new NodoAST(new LexToken(LexToken.Type.RADICAL, "√", 9));
        n.addHijo(inside);
        return n;
    }

    private static NodoAST nodoEqual(NodoAST a, NodoAST b) {
        NodoAST n = new NodoAST(new LexToken(LexToken.Type.EQUAL, "=", 4));
        n.addHijo(a); n.addHijo(b);
        return n;
    }
}
