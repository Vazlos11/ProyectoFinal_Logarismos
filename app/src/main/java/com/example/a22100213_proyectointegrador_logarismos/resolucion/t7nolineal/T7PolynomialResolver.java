package com.example.a22100213_proyectointegrador_logarismos.resolucion.t7nolineal;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.MetodoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.PlanificadorResolucion;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.TipoExpresion;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.Resolver;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import java.util.LinkedHashSet;
import java.util.Set;

public class T7PolynomialResolver implements Resolver {
    @Override
    public boolean supports(ResultadoSemantico rs) {
        if (rs == null) return false;
        return rs.tipoPrincipal == TipoExpresion.T7_DESPEJE_POLINOMICO;
    }

    @Override
    public ResultadoResolucion resolve(NodoAST raiz, ResultadoSemantico rs) {
        MetodoResolucion m = PlanificadorResolucion.metodo(raiz, rs);
        switch (m) {
            case ECUACION_CUADRATICA: { return resolverCuadratica(raiz); }
            case POLI_RUFFINI: { return resolverRuffini(raiz); }
            case ECUACION_POLINOMICA: { return resolverNewtonRaphson(raiz); }
            case NEWTON_RAPHSON: { return resolverNewtonRaphson(raiz); }
            default: {
                ResultadoResolucion rr = new ResultadoResolucion();
                rr.pasos.add(new PasoResolucion("\\text{Sin método polinómico reconocido}"));
                rr.resultado = raiz;
                rr.latexFinal = "\\text{Sin solución}";
                return rr;
            }
        }
    }

    private ResultadoResolucion resolverCuadratica(NodoAST eq) {
        String var = unicaVariable(eq);
        NodoAST L = eq.hijos.get(0);
        NodoAST R = eq.hijos.get(1);
        NodoAST d = resta(L, R);
        double a = coeficienteTermino(d, var, 2);
        double b = coeficienteTermino(d, var, 1);
        double c = coeficienteTermino(d, var, 0);
        double disc = b * b - 4 * a * c;
        ResultadoResolucion rr = new ResultadoResolucion();
        rr.pasos.add(new PasoResolucion(AstUtils.toTeX(eq)));
        rr.pasos.add(new PasoResolucion("\\text{Fórmula General: } x=\\frac{-b\\pm\\sqrt{b^2-4ac}}{2a}"));
        if (Math.abs(a) < 1e-12) {
            if (Math.abs(b) < 1e-12) {
                rr.pasos.add(new PasoResolucion("\\text{No es cuadrática}"));
                rr.resultado = eq;
                rr.latexFinal = AstUtils.toTeX(eq);
                return rr;
            }
            double x = -c / b;
            rr.pasos.add(new PasoResolucion("x=" + num(x)));
            rr.resultado = eq;
            rr.latexFinal = "x=" + num(x);
            return rr;
        }
        if (disc < 0) {
            rr.pasos.add(new PasoResolucion("\\text{Discriminante negativo}"));
            rr.resultado = eq;
            rr.latexFinal = "\\text{Sin solución real}";
            return rr;
        }
        double sqrt = Math.sqrt(disc);
        double x1 = (-b + sqrt) / (2 * a);
        double x2 = (-b - sqrt) / (2 * a);
        String sol = "x=" + num(x1) + "\\;\\text{o}\\;x=" + num(x2);
        rr.pasos.add(new PasoResolucion(sol));
        rr.resultado = eq;
        rr.latexFinal = sol;
        return rr;
    }

    private ResultadoResolucion resolverRuffini(NodoAST eq) {
        String var = unicaVariable(eq);
        NodoAST L = eq.hijos.get(0);
        NodoAST R = eq.hijos.get(1);
        NodoAST d = resta(L, R);
        Poly p = Poly.fromAST(d, var);

        ResultadoResolucion rr = new ResultadoResolucion();
        rr.pasos.add(new PasoResolucion(AstUtils.toTeX(eq)));

        if (p == null || p.deg() <= 0) {
            rr.pasos.add(new PasoResolucion("\\text{No es polinomio válido}"));
            rr.resultado = eq;
            rr.latexFinal = AstUtils.toTeX(eq);
            return rr;
        }

        final double EPS = 1e-10;
        java.util.List<Double> roots = new java.util.ArrayList<>();

        while (Math.abs(p.cte()) < EPS && p.deg() >= 1) {
            roots.add(0.0);
            rr.pasos.add(new PasoResolucion("\\text{Raíz racional } x=0"));
            p = p.dividePorBinomio(0.0);
        }

        while (p.deg() >= 3) {
            java.util.Set<Double> candidatos = divisoresRacionales(p.cte(), p.an());
            boolean extrajo = false;
            for (double r : candidatos) {
                if (Math.abs(p.eval(r)) < EPS) {
                    int mult = 0;
                    do { p = p.dividePorBinomio(r); mult++; }
                    while (p.deg() >= 1 && Math.abs(p.eval(r)) < EPS);
                    for (int i = 0; i < mult; i++) roots.add(r);
                    rr.pasos.add(new PasoResolucion("\\text{Raíz racional } x=" + num(r) + (mult > 1 ? ("\\ (\\text{multiplicidad }" + mult + ")") : "")));
                    rr.pasos.add(new PasoResolucion("\\text{Cociente: } " + p.toTeX(var)));
                    extrajo = true;
                    break;
                }
            }
            if (!extrajo) {
                Double rNum = newtonRealPrimeraRaiz(p);
                if (rNum == null) break;
                roots.add(rNum);
                rr.pasos.add(new PasoResolucion("\\text{Raíz numérica aproximada } x\\approx " + num(rNum)));
                p = p.dividePorBinomio(rNum);
            }
        }

        if (p.deg() == 2) {
            double a2 = p.a[2], a1 = p.a[1], a0 = p.a[0];
            double disc = a1 * a1 - 4 * a2 * a0;
            rr.pasos.add(new PasoResolucion("\\text{Cociente cuadrático: } " + p.toTeX(var)));
            if (disc >= 0) {
                double sqrt = Math.sqrt(disc);
                roots.add((-a1 + sqrt) / (2 * a2));
                roots.add((-a1 - sqrt) / (2 * a2));
                rr.pasos.add(new PasoResolucion("\\text{Fórmula general}"));
            } else {
                rr.pasos.add(new PasoResolucion("\\text{Discriminante negativo en el cociente}"));
            }
        } else if (p.deg() == 1) {
            roots.add(-p.a[0] / p.a[1]);
            rr.pasos.add(new PasoResolucion("\\text{Cociente lineal: } " + p.toTeX(var)));
        }

        if (roots.isEmpty()) {
            rr.pasos.add(new PasoResolucion("\\text{No se hallaron raíces explícitas}"));
            rr.resultado = eq;
            rr.latexFinal = AstUtils.toTeX(eq);
            return rr;
        }

        java.util.LinkedHashSet<String> uniq = new java.util.LinkedHashSet<>();
        for (double r : roots) uniq.add(num(r));
        StringBuilder sb = new StringBuilder("x=");
        int i = 0, n = uniq.size();
        for (String s : uniq) { if (i++ > 0) sb.append("\\;\\text{ó}\\;x="); sb.append(s); }

        rr.resultado = eq;
        rr.latexFinal = sb.toString();
        rr.pasos.add(new PasoResolucion(sb.toString()));
        return rr;
    }
    private Double newtonRealPrimeraRaiz(Poly p) {
        Poly dp = p.deriv();
        final double EPS = 1e-10;
        final int MAXIT = 50;

        double[] seeds = {-5,-3,-2,-1,0,1,2,3,5};
        for (double x0 : seeds) {
            double x = x0;
            for (int it = 0; it < MAXIT; it++) {
                double fx = p.eval(x), dfx = dp.eval(x);
                if (Math.abs(dfx) < 1e-14) break;
                double x1 = x - fx/dfx;
                if (Double.isFinite(x1) && Math.abs(p.eval(x1)) < EPS) return x1;
                x = x1;
            }
        }
        return null;
    }


    private ResultadoResolucion resolverNewtonRaphson(NodoAST eq) {
        java.util.List<Double> roots = com.example.a22100213_proyectointegrador_logarismos.resolucion.numerico.NewtonRaphsonSolver.solveAll(eq);
        ResultadoResolucion rr = new ResultadoResolucion();
        rr.pasos.add(new PasoResolucion(AstUtils.toTeX(eq)));
        rr.pasos.add(new PasoResolucion("\\text{Newton–Raphson sobre } F(x)=0"));
        if (roots.isEmpty()) {
            rr.pasos.add(new PasoResolucion("\\text{Sin raíces reales encontradas}"));
            rr.resultado = eq;
            rr.latexFinal = "\\text{Sin solución real}";
            return rr;
        }
        StringBuilder sb = new StringBuilder("x=");
        for (int i = 0; i < roots.size(); i++) {
            if (i > 0) sb.append("\\;\\text{ó}\\;x=");
            sb.append(num(roots.get(i)));
        }
        rr.pasos.add(new PasoResolucion(sb.toString()));
        rr.resultado = eq;
        rr.latexFinal = sb.toString();
        return rr;
    }


    private static class Poly {
        final double[] a;
        Poly(double[] a) { this.a = a; }
        int deg() {
            int d = a.length - 1;
            while (d > 0 && Math.abs(a[d]) < 1e-12) d--;
            return d;
        }

        Poly deriv() {
            int n = deg();
            if (n == 0) return new Poly(new double[]{0});
            double[] b = new double[n];
            for (int k = 1; k <= n; k++) b[k-1] = k * a[k];
            return new Poly(b);
        }

        double an() { return a[deg()]; }
        double cte() { return a[0]; }
        double eval(double x) {
            double s = 0, p = 1;
            for (double v : a) { s += v * p; p *= x; }
            return s;
        }
        Poly dividePorBinomio(double r) {
            int n = deg();
            double[] b = new double[n];
            b[n - 1] = a[n];
            for (int k = n - 2; k >= 0; k--) b[k] = a[k + 1] + r * b[k + 1];
            return new Poly(b);
        }
        String toTeX(String v) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (int i = deg(); i >= 0; i--) {
                double c = a[i];
                if (Math.abs(c) < 1e-12) continue;
                if (!first) sb.append(c >= 0 ? "+" : "-");
                else if (c < 0) sb.append("-");
                double abs = Math.abs(c);
                if (i == 0) sb.append(num(abs));
                else if (i == 1) {
                    if (Math.abs(abs - 1) > 1e-12) sb.append(num(abs));
                    sb.append(v);
                } else {
                    if (Math.abs(abs - 1) > 1e-12) sb.append(num(abs));
                    sb.append(v).append("^").append(i);
                }
                first = false;
            }
            if (first) return "0";
            return sb.toString();
        }
        static Poly fromAST(NodoAST n, String var) {
            GradoPolinomio g = gradoSiPolinomio(n, var);
            if (!g.ok || g.grado < 0) return null;
            int d = g.grado;
            double[] a = new double[d + 1];
            acumCoef(n, var, a);
            return new Poly(a);
        }
    }

    private static void acumCoef(NodoAST n, String var, double[] a) {
        if (n == null || n.token == null) return;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL) {
            double c = Double.parseDouble(n.token.value);
            a[0] += c;
            return;
        }
        if (t == LexToken.Type.VARIABLE && var.equals(n.token.value) && n.hijos.isEmpty()) {
            a[1] += 1.0;
            return;
        }
        if (t == LexToken.Type.SUM) {
            for (NodoAST h : n.hijos) acumCoef(h, var, a);
            return;
        }
        if (t == LexToken.Type.SUB) {
            if (!n.hijos.isEmpty()) {
                acumCoef(n.hijos.get(0), var, a);
                for (int i = 1; i < n.hijos.size(); i++) {
                    mulConst(n.hijos.get(i), var, a, -1.0);
                }
            }
            return;
        }

        if (t == LexToken.Type.MUL) {
            if (n.hijos.size() == 2) {
                Double cL = evaluarConstante(n.hijos.get(0));
                Double cR = evaluarConstante(n.hijos.get(1));
                if (cL != null) mulConst(n.hijos.get(1), var, a, cL);
                else if (cR != null) mulConst(n.hijos.get(0), var, a, cR);
            } else {
                double c = 1.0;
                NodoAST rest = null;
                for (NodoAST h : n.hijos) {
                    Double ch = evaluarConstante(h);
                    if (ch != null) c *= ch; else rest = rest == null ? h : mul(rest, h);
                }
                if (rest != null) mulConst(rest, var, a, c);
                else a[0] += c;
            }
            return;
        }
        if (t == LexToken.Type.EXP && n.hijos.size() == 2) {
            NodoAST base = n.hijos.get(0);
            Double e = evaluarConstante(n.hijos.get(1));
            if (e != null) {
                if (base != null && base.token != null && base.token.type == LexToken.Type.VARIABLE && var.equals(base.token.value)) {
                    int p = (int) Math.rint(e);
                    if (p >= 0 && p < a.length) a[p] += 1.0;
                }
                return;
            }
        }
        for (NodoAST h : n.hijos) acumCoef(h, var, a);
    }

    private static void mulConst(NodoAST n, String var, double[] a, double c) {
        if (n == null || n.token == null) return;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL) {
            double v = Double.parseDouble(n.token.value);
            a[0] += c * v;
            return;
        }
        if (t == LexToken.Type.VARIABLE && var.equals(n.token.value) && n.hijos.isEmpty()) {
            a[1] += c;
            return;
        }
        if (t == LexToken.Type.SUM) {
            for (NodoAST h : n.hijos) mulConst(h, var, a, c);
            return;
        }
        if (t == LexToken.Type.SUB) {
            if (!n.hijos.isEmpty()) {
                mulConst(n.hijos.get(0), var, a, c);
                for (int i = 1; i < n.hijos.size(); i++) {
                    mulConst(n.hijos.get(i), var, a, -c);
                }
            }
            return;
        }
        if (t == LexToken.Type.MUL) {
            if (n.hijos.size() == 2) {
                Double cL = evaluarConstante(n.hijos.get(0));
                Double cR = evaluarConstante(n.hijos.get(1));
                if (cL != null) mulConst(n.hijos.get(1), var, a, c * cL);
                else if (cR != null) mulConst(n.hijos.get(0), var, a, c * cR);
            } else {
                double cacc = c;
                NodoAST rest = null;
                for (NodoAST h : n.hijos) {
                    Double ch = evaluarConstante(h);
                    if (ch != null) cacc *= ch; else rest = rest == null ? h : mul(rest, h);
                }
                if (rest != null) mulConst(rest, var, a, cacc);
                else a[0] += cacc;
            }
            return;
        }
        if (t == LexToken.Type.EXP && n.hijos.size() == 2) {
            NodoAST base = n.hijos.get(0);
            Double e = evaluarConstante(n.hijos.get(1));
            if (e != null) {
                if (base != null && base.token != null && base.token.type == LexToken.Type.VARIABLE && var.equals(base.token.value)) {
                    int p = (int) Math.rint(e);
                    if (p >= 0 && p < a.length) a[p] += c;
                }
                return;
            }
        }
        for (NodoAST h : n.hijos) mulConst(h, var, a, c);
    }

    private static java.util.Set<Double> divisoresRacionales(double cte, double an) {
        java.util.Set<Double> s = new java.util.LinkedHashSet<>();
        int A = (int)Math.rint(Math.abs(an));
        int C = (int)Math.rint(Math.abs(cte));
        if (A == 0) A = 1;
        if (C == 0) C = 1;
        for (int p = 1; p <= C; p++) if (C % p == 0) for (int q = 1; q <= A; q++) if (A % q == 0) {
            s.add((double)p/q); s.add(-(double)p/q);
        }
        return s;
    }

    private static double coeficienteTermino(NodoAST n, String var, int grado) {
        if (grado < 0) return 0.0;
        GradoPolinomio g = gradoSiPolinomio(n, var);
        if (!g.ok || g.grado < grado) return 0.0;
        double[] a = new double[g.grado + 1];
        acumCoef(n, var, a);
        if (grado >= a.length) return 0.0;
        return a[grado];
    }

    private static NodoAST resta(NodoAST a, NodoAST b) {
        NodoAST n = new NodoAST(new LexToken(LexToken.Type.SUB, "-", 5));
        n.addHijo(cloneAST(a));
        n.addHijo(cloneAST(b));
        return n;
    }

    private static NodoAST mul(NodoAST a, NodoAST b) {
        NodoAST n = new NodoAST(new LexToken(LexToken.Type.MUL, "*", 6));
        n.addHijo(cloneAST(a));
        n.addHijo(cloneAST(b));
        return n;
    }

    private static NodoAST neg(NodoAST a) {
        NodoAST cero = new NodoAST(new LexToken(LexToken.Type.INTEGER, "0", 0));
        NodoAST n = new NodoAST(new LexToken(LexToken.Type.SUB, "-", 5));
        n.addHijo(cero);
        n.addHijo(cloneAST(a));
        return n;
    }

    private static Double evaluarConstante(NodoAST n) {
        if (n == null || n.token == null) return null;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.VARIABLE) return null;
        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL) {
            try { return Double.parseDouble(n.token.value); } catch (Exception e) { return null; }
        }
        if (t == LexToken.Type.CONST_PI) return Math.PI;
        if (t == LexToken.Type.CONST_E) return Math.E;
        if (t == LexToken.Type.SUM) {
            double a = 0.0;
            for (NodoAST h : n.hijos) { Double v = evaluarConstante(h); if (v == null) return null; a += v; }
            return a;
        }
        if (t == LexToken.Type.MUL) {
            double a = 1.0;
            for (NodoAST h : n.hijos) { Double v = evaluarConstante(h); if (v == null) return null; a *= v; }
            return a;
        }
        if (t == LexToken.Type.SUB) {
            if (n.hijos.size() != 2) return null;
            Double a = evaluarConstante(n.hijos.get(0));
            Double b = evaluarConstante(n.hijos.get(1));
            if (a == null || b == null) return null;
            return a - b;
        }
        if (t == LexToken.Type.DIV) {
            if (n.hijos.size() != 2) return null;
            Double a = evaluarConstante(n.hijos.get(0));
            Double b = evaluarConstante(n.hijos.get(1));
            if (a == null || b == null || Math.abs(b) < 1e-15) return null;
            return a / b;
        }
        if (t == LexToken.Type.EXP) {
            if (n.hijos.size() != 2) return null;
            Double a = evaluarConstante(n.hijos.get(0));
            Double b = evaluarConstante(n.hijos.get(1));
            if (a == null || b == null) return null;
            return Math.pow(a, b);
        }
        return null;
    }

    private static String unicaVariable(NodoAST n) {
        Set<String> s = varsEnSubarbol(n);
        return s.size() == 1 ? s.iterator().next() : null;
    }

    private static Set<String> varsEnSubarbol(NodoAST n) {
        Set<String> s = new LinkedHashSet<>();
        if (n == null) return s;
        if (n.token != null && n.token.type == LexToken.Type.VARIABLE) s.add(n.token.value);
        for (NodoAST h : n.hijos) s.addAll(varsEnSubarbol(h));
        return s;
    }

    private static class GradoPolinomio {
        boolean ok;
        int grado;
        GradoPolinomio(boolean ok, int grado) { this.ok = ok; this.grado = grado; }
    }

    private static GradoPolinomio gradoSiPolinomio(NodoAST n, String var) {
        if (n == null || n.token == null) return new GradoPolinomio(false, 0);
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL) return new GradoPolinomio(true, 0);
        if (t == LexToken.Type.VARIABLE) return new GradoPolinomio(var.equals(n.token.value), var.equals(n.token.value) ? 1 : 0);
        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB) {
            int g = 0;
            for (NodoAST h : n.hijos) {
                GradoPolinomio gh = gradoSiPolinomio(h, var);
                if (!gh.ok) return new GradoPolinomio(false, 0);
                g = Math.max(g, gh.grado);
            }
            return new GradoPolinomio(true, g);
        }
        if (t == LexToken.Type.MUL) {
            int g = 0;
            for (NodoAST h : n.hijos) {
                GradoPolinomio gh = gradoSiPolinomio(h, var);
                if (!gh.ok) return new GradoPolinomio(false, 0);
                g += gh.grado;
            }
            return new GradoPolinomio(true, g);
        }
        if (t == LexToken.Type.EXP) {
            if (n.hijos.size() != 2) return new GradoPolinomio(false, 0);
            GradoPolinomio gb = gradoSiPolinomio(n.hijos.get(0), var);
            if (!gb.ok) return new GradoPolinomio(false, 0);
            Double e = evaluarConstante(n.hijos.get(1));
            if (e == null || e < 0 || Math.abs(e - Math.rint(e)) > 1e-9) return new GradoPolinomio(false, 0);
            return new GradoPolinomio(true, (int) Math.round(gb.grado * e));
        }
        return new GradoPolinomio(false, 0);
    }

    private static NodoAST cloneAST(NodoAST n) {
        if (n == null) return null;
        NodoAST c = new NodoAST(n.token == null ? null : new LexToken(n.token.type, n.token.value, n.token.prioridad));
        for (NodoAST h : n.hijos) {
            NodoAST ch = cloneAST(h);
            if (ch != null) c.addHijo(ch);
        }
        return c;
    }

    private static String num(double x) {
        if (Double.isNaN(x)) return "\\text{NaN}";
        if (Double.isInfinite(x)) return x > 0 ? "\\infty" : "-\\infty";
        long r = Math.round(x);
        if (Math.abs(x - r) < 1e-10) return Long.toString(r);
        java.text.DecimalFormat df = new java.text.DecimalFormat("0.##########", java.text.DecimalFormatSymbols.getInstance(java.util.Locale.US));
        return df.format(x);
    }
}
