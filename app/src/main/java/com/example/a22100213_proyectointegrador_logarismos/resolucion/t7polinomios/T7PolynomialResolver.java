package com.example.a22100213_proyectointegrador_logarismos.resolucion.t7polinomios;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.MetodoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.PlanificadorResolucion;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.TipoExpresion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.MotorResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.Resolver;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t7polinomios.rules.CuadraticaFormulaGeneralRule;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t7polinomios.rules.RuffiniRule;


import java.util.LinkedHashSet;
import java.util.Set;

public final class T7PolynomialResolver implements Resolver {

    private static final double EPS = 1e-12;

    @Override
    public boolean supports(ResultadoSemantico rs) {
        return rs != null && rs.tipoPrincipal == TipoExpresion.T7_DESPEJE_POLINOMICO;
    }

    @Override
    public ResultadoResolucion resolve(NodoAST raiz, ResultadoSemantico rs) {
        MetodoResolucion m = PlanificadorResolucion.metodo(raiz, rs);

        switch (m) {
            case ECUACION_CUADRATICA: {
                CuadraticaFormulaGeneralRule rule = new CuadraticaFormulaGeneralRule();
                if (rule.applies(raiz, rs)) {
                    return rule.solve(raiz, rs);
                }
                return resolverCuadratica(raiz);
            }

            case POLI_RUFFINI: {

                RuffiniRule r = new RuffiniRule();
                return resolverRuffini(raiz);
            }

            default:
                return resolverNewtonRaphson(raiz);
        }
    }


    private static ResultadoResolucion resolverCuadratica(NodoAST raiz) {
        ResultadoResolucion rr = new ResultadoResolucion();
        NodoAST eq = buscarNodo(raiz, LexToken.Type.EQUAL);
        String var = unicaVariable(eq);
        rr.pasos.add(new PasoResolucion(AstUtils.toTeX(eq)));
        NodoAST d = nodoSub(eq.hijos.get(0), eq.hijos.get(1));
        double[] abc = coeficientesHasta(d, var, 2);
        double a = abc[2], b = abc[1], c = abc[0];
        if (Math.abs(a) < EPS) {
            if (Math.abs(b) < EPS) {
                rr.latexFinal = "\\text{Sin solución}";
                rr.pasos.add(new PasoResolucion(rr.latexFinal));
                rr.resultado = eq;
                return rr;
            }
            NodoAST eq1 = nodoEqual(nodoVar(var), nodoDiv(nodoMul(AstUtils.number(-1.0), AstUtils.number(c)), AstUtils.number(b)));
            rr.pasos.add(new PasoResolucion(AstUtils.toTeX(eq1)));
            Double vx = AstUtils.evalConst(eq1.hijos.get(1));
            if (vx != null && Double.isFinite(vx)) {
                NodoAST eq2 = nodoEqual(nodoVar(var), AstUtils.number(vx));
                rr.latexFinal = AstUtils.toTeX(eq2);
                rr.pasos.add(new PasoResolucion(rr.latexFinal));
                rr.resultado = eq2;
                return rr;
            }
            rr.latexFinal = AstUtils.toTeX(eq1);
            rr.resultado = eq1;
            return rr;
        }
        double disc = b * b - 4 * a * c;
        NodoAST x0 = nodoDiv(nodoAdd(AstUtils.number(-b), nodoRad(AstUtils.number(disc))), nodoMul(AstUtils.number(2.0), AstUtils.number(a)));
        NodoAST x1 = nodoDiv(nodoSub(AstUtils.number(-b), nodoRad(AstUtils.number(disc))), nodoMul(AstUtils.number(2.0), AstUtils.number(a)));
        rr.pasos.add(new PasoResolucion("\\Delta = " + AstUtils.toTeX(AstUtils.number(disc))));
        rr.pasos.add(new PasoResolucion("x_{0} = " + AstUtils.toTeX(x0)));
        rr.pasos.add(new PasoResolucion("x_{1} = " + AstUtils.toTeX(x1)));
        if (disc < -EPS) {
            rr.latexFinal = "\\text{Sin solución real}";
            rr.pasos.add(new PasoResolucion(rr.latexFinal));
            rr.resultado = eq;
            return rr;
        }
        Double v0 = AstUtils.evalConst(x0);
        Double v1 = AstUtils.evalConst(x1);
        if (v0 != null && v1 != null) {
            NodoAST s0 = nodoEqual(nodoVar(var), AstUtils.number(v0));
            NodoAST s1 = nodoEqual(nodoVar(var), AstUtils.number(v1));
            rr.pasos.add(new PasoResolucion(AstUtils.toTeX(s0)));
            rr.pasos.add(new PasoResolucion(AstUtils.toTeX(s1)));
            rr.latexFinal = AstUtils.toTeX(s0) + "\\quad " + AstUtils.toTeX(s1);
            rr.resultado = s0;
            return rr;
        }
        rr.latexFinal = "x_{0} = " + AstUtils.toTeX(x0) + "\\quad x_{1} = " + AstUtils.toTeX(x1);
        rr.resultado = eq;
        return rr;
    }

    private static NodoAST nodoEqual(NodoAST a, NodoAST b) {
        NodoAST n = new NodoAST(new LexToken(LexToken.Type.EQUAL, "=", 4));
        n.addHijo(a);
        n.addHijo(b);
        return n;
    }

    private static ResultadoResolucion resolverRuffini(NodoAST raiz) {
        ResultadoResolucion rr = new ResultadoResolucion();
        NodoAST frac = buscarDivisionPrincipal(raiz);
        NodoAST num = frac.hijos.get(0);
        NodoAST den = frac.hijos.get(1);
        NodoAST eq = buscarNodo(raiz, LexToken.Type.EQUAL);
        String var = unicaVariable(raiz);
        rr.pasos.add(new PasoResolucion(eq != null ? AstUtils.toTeX(eq) : AstUtils.toTeX(frac)));
        double g = raizBinomio(den, var);
        double[] a4a0 = coeficientesHasta(num, var, 4);
        double[] coefs = new double[]{a4a0[4], a4a0[3], a4a0[2], a4a0[1], a4a0[0]};
        rr.pasos.add(new PasoResolucion("\\text{Coeficientes: } " + formatVector(coefs)));
        rr.pasos.add(new PasoResolucion("\\text{Divisor: } x" + (g >= 0 ? "-" : "+") + AstUtils.toTeX(AstUtils.number(Math.abs(g)))));
        double[] b = new double[5];
        b[0] = coefs[0];
        b[1] = coefs[1] + g * b[0];
        b[2] = coefs[2] + g * b[1];
        b[3] = coefs[3] + g * b[2];
        b[4] = coefs[4] + g * b[3];
        rr.pasos.add(new PasoResolucion("\\text{Fila inferior: } " + formatVector(b)));
        double rem = b[4];
        NodoAST qPoly = polyASTFromCoeffs(var, new double[]{b[0], b[1], b[2], b[3]});
        NodoAST resultado;
        if (Math.abs(rem) < EPS) {
            rr.pasos.add(new PasoResolucion("\\text{Residuo } = 0 \\Rightarrow x = " + AstUtils.toTeX(AstUtils.number(g)) + " \\text{ es raíz}"));
            resultado = qPoly;
            rr.latexFinal = AstUtils.toTeX(qPoly);
        } else {
            NodoAST resto = AstUtils.number(rem);
            NodoAST expr = nodoAdd(qPoly, nodoDiv(resto, den));
            resultado = expr;
            rr.latexFinal = AstUtils.toTeX(expr);
        }
        rr.resultado = resultado;
        return rr;
    }

    private static ResultadoResolucion resolverNewtonRaphson(NodoAST raiz) {
        ResultadoResolucion rr = new ResultadoResolucion();
        NodoAST eq = buscarNodo(raiz, LexToken.Type.EQUAL);
        NodoAST f = eq == null ? raiz : nodoSub(eq.hijos.get(0), eq.hijos.get(1));
        String var = unicaVariable(raiz);
        rr.pasos.add(new PasoResolucion(AstUtils.toTeX(eq != null ? eq : raiz)));
        NodoAST df = derivadaSimbolica(f, var);
        if (df != null) rr.pasos.add(new PasoResolucion("f'(x) = " + AstUtils.toTeX(df)));
        double x = 0.0;
        rr.pasos.add(new PasoResolucion("x_{0} = " + AstUtils.toTeX(AstUtils.number(x))));
        int it = 0;
        while (it < 30) {
            double fx = evalEn(f, var, x);
            double dfx = df != null ? evalEn(df, var, x) : derivNum(f, var, x);
            if (!Double.isFinite(fx) || !Double.isFinite(dfx) || Math.abs(dfx) < 1e-15) break;
            double x1 = x - fx / dfx;
            rr.pasos.add(new PasoResolucion("x_{" + (it + 1) + "} = " + AstUtils.toTeX(AstUtils.number(x)) + " - \\dfrac{" + AstUtils.toTeX(AstUtils.number(fx)) + "}{" + AstUtils.toTeX(AstUtils.number(dfx)) + "} = " + AstUtils.toTeX(AstUtils.number(x1))));
            if (Math.abs(x1 - x) < 1e-6) {
                x = x1;
                break;
            }
            x = x1;
            it++;
        }
        rr.latexFinal = AstUtils.toTeX(nodoEqual(nodoVar(var), AstUtils.number(x)));
        rr.pasos.add(new PasoResolucion(rr.latexFinal));
        rr.resultado = nodoEqual(nodoVar(var), AstUtils.number(x));
        return rr;
    }

    private static NodoAST derivadaSimbolica(NodoAST f, String var) {
        try {
            NodoAST d = new NodoAST(new LexToken(LexToken.Type.DERIV, "d", 9));
            d.addHijo(cloneAST(f));
            d.addHijo(new NodoAST(new LexToken(LexToken.Type.VARIABLE, "d" + var, 9)));
            ResultadoSemantico rsd = com.example.a22100213_proyectointegrador_logarismos.Semantico.AnalisisSemantico.analizar(d);
            ResultadoResolucion rr = MotorResolucion.resolver(d, rsd);
            return rr.resultado;
        } catch (Exception e) {
            return null;
        }
    }

    private static double evalEn(NodoAST n, String var, double x) {
        NodoAST s = sustituir(n, var, x);
        Double v = AstUtils.evalConst(s);
        return v == null ? Double.NaN : v;
    }

    private static double derivNum(NodoAST n, String var, double x) {
        double h = 1e-6 * (1.0 + Math.abs(x));
        double f1 = evalEn(n, var, x + h);
        double f2 = evalEn(n, var, x - h);
        return (f1 - f2) / (2.0 * h);
    }

    private static NodoAST sustituir(NodoAST n, String var, double x) {
        if (n == null) return null;
        if (n.token != null && n.token.type == LexToken.Type.VARIABLE && var.equals(n.token.value))
            return AstUtils.number(x);
        NodoAST c = new NodoAST(n.token == null ? null : new LexToken(n.token.type, n.token.value, n.token.prioridad));
        for (NodoAST h : n.hijos) c.addHijo(sustituir(h, var, x));
        return c;
    }

    private static double[] coeficientesHasta(NodoAST expr, String var, int max) {
        Poly p = toPoly(expr, var, max);
        double[] r = new double[max + 1];
        if (!p.ok) return r;
        int m = Math.min(max, p.deg());
        for (int i = 0; i <= m; i++) r[i] = i <= p.deg() ? p.c[i] : 0.0;
        return r;
    }

    private static Poly toPoly(NodoAST n, String var, int max) {
        if (n == null) return Poly.bad(max);
        if (n.token == null) return Poly.bad(max);
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL || t == LexToken.Type.CONST_E || t == LexToken.Type.CONST_PI) {
            Double v = AstUtils.evalConst(n);
            return Poly.constante(max, v == null ? 0.0 : v);
        }
        if (t == LexToken.Type.VARIABLE) {
            if (var.equals(n.token.value)) return Poly.monomio(max, 1, 1.0);
            return Poly.constante(max, 0.0);
        }
        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB) {
            Poly a = toPoly(n.hijos.get(0), var, max);
            Poly b = toPoly(n.hijos.get(1), var, max);
            if (!a.ok || !b.ok) return Poly.bad(max);
            return t == LexToken.Type.SUM ? a.add(b) : a.sub(b);
        }
        if (t == LexToken.Type.MUL) {
            Poly a = toPoly(n.hijos.get(0), var, max);
            Poly b = toPoly(n.hijos.get(1), var, max);
            if (!a.ok || !b.ok) return Poly.bad(max);
            return a.mul(b);
        }
        if (t == LexToken.Type.EXP) {
            NodoAST base = n.hijos.get(0);
            NodoAST expo = n.hijos.get(1);
            Double e = AstUtils.evalConst(expo);
            if (e == null) return Poly.bad(max);
            int k = (int) Math.rint(e);
            if (Math.abs(e - k) > 1e-9 || k < 0) return Poly.bad(max);
            if (base.token != null && base.token.type == LexToken.Type.VARIABLE && var.equals(base.token.value)) {
                return Poly.monomio(max, k, 1.0);
            }
            Double vb = AstUtils.evalConst(base);
            if (vb != null) return Poly.constante(max, Math.pow(vb, k));
            return Poly.bad(max);
        }
        if (t == LexToken.Type.DIV) return Poly.bad(max);
        return Poly.bad(max);
    }

    private static NodoAST polyASTFromCoeffs(String var, double[] c) {
        NodoAST acc = AstUtils.number(0.0);
        for (int i = c.length - 1; i >= 0; i--) {
            if (Math.abs(c[i]) < EPS) continue;
            NodoAST term;
            if (i == 0) term = AstUtils.number(c[i]);
            else if (i == 1) term = nodoMul(AstUtils.number(c[i]), nodoVar(var));
            else term = nodoMul(AstUtils.number(c[i]), nodoExp(nodoVar(var), AstUtils.number(i)));
            acc = nodoAdd(acc, term);
        }
        return acc;
    }

    private static double raizBinomio(NodoAST den, String var) {
        if (den == null || den.token == null) return 0.0;
        if (den.token.type == LexToken.Type.SUM || den.token.type == LexToken.Type.SUB) {
            NodoAST a = den.hijos.get(0), b = den.hijos.get(1);
            boolean aVar = a != null && a.token != null && a.token.type == LexToken.Type.VARIABLE && var.equals(a.token.value);
            boolean bVar = b != null && b.token != null && b.token.type == LexToken.Type.VARIABLE && var.equals(b.token.value);
            if (aVar && !bVar) {
                Double k = AstUtils.evalConst(b);
                if (k == null) return 0.0;
                return den.token.type == LexToken.Type.SUM ? -k : k;
            }
            if (bVar && !aVar) {
                Double k = AstUtils.evalConst(a);
                if (k == null) return 0.0;
                if (den.token.type == LexToken.Type.SUM) return -k;
                else return k;
            }
        }
        if (den.token.type == LexToken.Type.SUB && den.hijos.size() == 2) {
            NodoAST a = den.hijos.get(0), b = den.hijos.get(1);
            if (a != null && a.token != null && a.token.type == LexToken.Type.VARIABLE && var.equals(a.token.value)) {
                Double k = AstUtils.evalConst(b);
                if (k != null) return k;
            }
        }
        return 0.0;
    }

    private static String formatVector(double[] v) {
        StringBuilder sb = new StringBuilder("\\left[");
        for (int i = 0; i < v.length; i++) {
            sb.append(AstUtils.toTeX(AstUtils.number(v[i])));
            if (i + 1 < v.length) sb.append(",\\;");
        }
        sb.append("\\right]");
        return sb.toString();
    }

    private static NodoAST buscarDivisionPrincipal(NodoAST n) {
        if (n == null) return null;
        if (n.token != null && n.token.type == LexToken.Type.DIV) return n;
        for (NodoAST h : n.hijos) {
            NodoAST r = buscarDivisionPrincipal(h);
            if (r != null) return r;
        }
        return null;
    }

    private static NodoAST buscarNodo(NodoAST n, LexToken.Type t) {
        if (n == null) return null;
        if (n.token != null && n.token.type == t) return n;
        for (NodoAST h : n.hijos) {
            NodoAST r = buscarNodo(h, t);
            if (r != null) return r;
        }
        return null;
    }

    private static String unicaVariable(NodoAST n) {
        Set<String> s = new LinkedHashSet<>();
        recolectarVars(n, s);
        return s.isEmpty() ? "x" : s.iterator().next();
    }

    private static void recolectarVars(NodoAST n, Set<String> out) {
        if (n == null) return;
        if (n.token != null && n.token.type == LexToken.Type.VARIABLE && n.token.value != null)
            out.add(n.token.value);
        for (NodoAST h : n.hijos) recolectarVars(h, out);
    }

    private static NodoAST nodoAdd(NodoAST a, NodoAST b) {
        NodoAST n = new NodoAST(new LexToken(LexToken.Type.SUM, "+", 4));
        n.addHijo(a);
        n.addHijo(b);
        return n;
    }

    private static NodoAST nodoSub(NodoAST a, NodoAST b) {
        NodoAST n = new NodoAST(new LexToken(LexToken.Type.SUB, "-", 5));
        n.addHijo(a);
        n.addHijo(b);
        return n;
    }

    private static NodoAST nodoMul(NodoAST a, NodoAST b) {
        NodoAST n = new NodoAST(new LexToken(LexToken.Type.MUL, "*", 6));
        n.addHijo(a);
        n.addHijo(b);
        return n;
    }

    private static NodoAST nodoDiv(NodoAST a, NodoAST b) {
        NodoAST n = new NodoAST(new LexToken(LexToken.Type.DIV, "/", 6));
        n.addHijo(a);
        n.addHijo(b);
        return n;
    }

    private static NodoAST nodoExp(NodoAST a, NodoAST b) {
        NodoAST n = new NodoAST(new LexToken(LexToken.Type.EXP, "^", 7));
        n.addHijo(a);
        n.addHijo(b);
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

    private static NodoAST cloneAST(NodoAST n) {
        if (n == null) return null;
        NodoAST c = new NodoAST(n.token == null ? null : new LexToken(n.token.type, n.token.value, n.token.prioridad));
        for (NodoAST h : n.hijos) c.addHijo(cloneAST(h));
        return c;
    }

    private static final class Poly {
        final double[] c;
        final boolean ok;

        Poly(double[] c, boolean ok) {
            this.c = c;
            this.ok = ok;
        }

        static Poly bad(int max) {
            return new Poly(new double[max + 1], false);
        }

        static Poly constante(int max, double k) {
            double[] c = new double[max + 1];
            c[0] = k;
            return new Poly(c, true);
        }

        static Poly monomio(int max, int grado, double k) {
            if (grado > max) return bad(max);
            double[] c = new double[max + 1];
            c[grado] = k;
            return new Poly(c, true);
        }

        int deg() {
            for (int i = c.length - 1; i >= 0; i--) if (Math.abs(c[i]) > EPS) return i;
            return 0;
        }

        Poly add(Poly o) {
            int n = Math.max(c.length, o.c.length);
            double[] r = new double[n];
            for (int i = 0; i < n; i++) {
                double a = i < c.length ? c[i] : 0;
                double b = i < o.c.length ? o.c[i] : 0;
                r[i] = a + b;
            }
            return new Poly(r, true);
        }

        Poly sub(Poly o) {
            int n = Math.max(c.length, o.c.length);
            double[] r = new double[n];
            for (int i = 0; i < n; i++) {
                double a = i < c.length ? c[i] : 0;
                double b = i < o.c.length ? o.c[i] : 0;
                r[i] = a - b;
            }
            return new Poly(r, true);
        }

        Poly mul(Poly o) {
            int n = c.length;
            double[] r = new double[n];
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    if (i + j < n) r[i + j] += c[i] * o.c[j];
                    else return new Poly(r, false);
            return new Poly(r, true);
        }
    }
}
