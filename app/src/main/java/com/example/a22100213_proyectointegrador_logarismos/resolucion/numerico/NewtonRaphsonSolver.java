package com.example.a22100213_proyectointegrador_logarismos.resolucion.numerico;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import java.util.*;

public final class NewtonRaphsonSolver {
    public static List<Double> solveAll(NodoAST eq) {
        return solveAll(eq, -50, 50, 400, 1e-9, 80, 1e-6);
    }

    public static List<Double> solveAll(NodoAST eq, double xmin, double xmax, int samples, double tol, int maxIt, double dedupTol) {
        if (eq == null || eq.token == null || eq.token.type != LexToken.Type.EQUAL || eq.hijos.size() != 2) return Collections.emptyList();
        String var = unicaVariable(eq);
        if (var == null) return Collections.emptyList();
        NodoAST F = resta(eq.hijos.get(0), eq.hijos.get(1));
        List<Double> roots = new ArrayList<>();
        double[] xs = new double[samples + 1];
        for (int i = 0; i <= samples; i++) xs[i] = xmin + (xmax - xmin) * i / samples;
        Double prevX = null, prevF = null;
        for (double x : xs) {
            double fx = f(F, var, x);
            if (Double.isFinite(fx)) {
                if (prevX != null && prevF != null && prevF * fx <= 0) {
                    Double r = rootInBracket(F, var, prevX, x, tol, maxIt);
                    if (r != null) addDedup(roots, r, dedupTol);
                }
                prevX = x; prevF = fx;
            } else {
                prevX = null; prevF = null;
            }
        }
        if (roots.isEmpty()) {
            double[] seeds = new double[]{-100,-50,-20,-10,-5,-3,-2,-1,-0.5,0,0.5,1,2,3,5,10,20,50,100};
            for (double x0 : seeds) {
                Double r = newtonFree(F, var, x0, tol, maxIt);
                if (r != null && r >= xmin - 1 && r <= xmax + 1) addDedup(roots, r, dedupTol);
            }
        }
        Collections.sort(roots);
        return roots;
    }

    public static Double solveOne(NodoAST eq, double x0, double tol, int maxIt) {
        if (eq == null || eq.token == null || eq.token.type != LexToken.Type.EQUAL || eq.hijos.size() != 2) return null;
        String var = unicaVariable(eq);
        if (var == null) return null;
        NodoAST F = resta(eq.hijos.get(0), eq.hijos.get(1));
        return newtonFree(F, var, x0, tol, maxIt);
    }

    private static Double rootInBracket(NodoAST F, String var, double a, double b, double tol, int maxIt) {
        double fa = f(F, var, a), fb = f(F, var, b);
        if (!Double.isFinite(fa) || !Double.isFinite(fb)) return null;
        if (fa == 0) return a;
        if (fb == 0) return b;
        if (fa * fb > 0) return null;
        double left = a, right = b;
        double xl = left, xr = right;
        double fl = fa, fr = fb;
        double x = 0.5 * (left + right);
        double fx = f(F, var, x);
        for (int it = 0; it < maxIt; it++) {
            double d = deriv(F, var, x);
            double xn;
            if (Double.isFinite(d) && Math.abs(d) > 1e-14) {
                xn = x - fx / d;
                if (!(xn > left && xn < right) || !Double.isFinite(xn)) xn = 0.5 * (left + right);
            } else {
                xn = secant(xl, fl, xr, fr);
                if (!(xn > left && xn < right) || !Double.isFinite(xn)) xn = 0.5 * (left + right);
            }
            double fn = f(F, var, xn);
            if (!Double.isFinite(fn)) {
                xn = 0.5 * (left + right);
                fn = f(F, var, xn);
                if (!Double.isFinite(fn)) return null;
            }
            if (Math.abs(fn) < tol || Math.abs(xn - x) < tol) return xn;
            if (fl * fn <= 0) { right = xn; xr = xn; fr = fn; }
            else { left = xn; xl = xn; fl = fn; }
            x = xn; fx = fn;
        }
        return Math.abs(fx) < tol ? x : null;
    }

    private static Double newtonFree(NodoAST F, String var, double x0, double tol, int maxIt) {
        double x = x0;
        for (int it = 0; it < maxIt; it++) {
            double fx = f(F, var, x);
            double d = deriv(F, var, x);
            double step;
            if (Double.isFinite(d) && Math.abs(d) > 1e-14) step = fx / d;
            else {
                double h = stepH(x);
                double f1 = f(F, var, x + h), f0 = f(F, var, x);
                if (!Double.isFinite(f1) || !Double.isFinite(f0) || Math.abs(f1 - f0) < 1e-14) break;
                step = h * f0 / (f1 - f0);
            }
            double xn = x - step;
            if (!Double.isFinite(xn)) break;
            if (Math.abs(xn - x) < tol && Math.abs(f(F, var, xn)) < tol) return xn;
            x = xn;
        }
        double fx = f(F, var, x);
        return Double.isFinite(fx) && Math.abs(fx) < tol ? x : null;
    }

    private static double f(NodoAST n, String var, double x) {
        if (n == null || n.token == null) return Double.NaN;
        LexToken.Type t = n.token.type;

        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL) {
            try { return Double.parseDouble(n.token.value); } catch (Exception e) { return Double.NaN; }
        }
        if (t == LexToken.Type.CONST_PI) return Math.PI;
        if (t == LexToken.Type.CONST_E) return Math.E;
        if (t == LexToken.Type.VARIABLE) return var.equals(n.token.value) ? x : Double.NaN;

        if (t == LexToken.Type.SUM) {
            double s = 0;
            for (NodoAST h : n.hijos) { double v = f(h, var, x); if (!Double.isFinite(v)) return Double.NaN; s += v; }
            return s;
        }
        if (t == LexToken.Type.SUB) {
            if (n.hijos.isEmpty()) return Double.NaN;
            if (n.hijos.size() == 1) {
                double v = f(n.hijos.get(0), var, x);
                return Double.isFinite(v) ? -v : Double.NaN;
            }
            double s = f(n.hijos.get(0), var, x);
            if (!Double.isFinite(s)) return Double.NaN;
            for (int i = 1; i < n.hijos.size(); i++) {
                double v = f(n.hijos.get(i), var, x); if (!Double.isFinite(v)) return Double.NaN; s -= v;
            }
            return s;
        }
        if (t == LexToken.Type.MUL) {
            double p = 1;
            for (NodoAST h : n.hijos) { double v = f(h, var, x); if (!Double.isFinite(v)) return Double.NaN; p *= v; }
            return p;
        }
        if (t == LexToken.Type.DIV) {
            if (n.hijos.size() != 2) return Double.NaN;
            double a = f(n.hijos.get(0), var, x), b = f(n.hijos.get(1), var, x);
            if (!Double.isFinite(a) || !Double.isFinite(b) || Math.abs(b) < 1e-15) return Double.NaN;
            return a / b;
        }
        if (t == LexToken.Type.EXP) {
            if (n.hijos.size() != 2) return Double.NaN;
            double a = f(n.hijos.get(0), var, x), b = f(n.hijos.get(1), var, x);
            if (!Double.isFinite(a) || !Double.isFinite(b)) return Double.NaN;
            if (a == 0.0 && b <= 0.0) return Double.NaN;
            double bi = Math.rint(b);
            if (a < 0 && Math.abs(b - bi) > 1e-10) return Double.NaN;
            return Math.pow(a, b);
        }
        if (t == LexToken.Type.RADICAL) {
            if (n.hijos.size() != 1) return Double.NaN;
            double a = f(n.hijos.get(0), var, x);
            if (!Double.isFinite(a) || a < 0) return Double.NaN;
            return Math.sqrt(a);
        }
        if (t == LexToken.Type.ABS) {
            if (n.hijos.size() != 1) return Double.NaN;
            double a = f(n.hijos.get(0), var, x);
            if (!Double.isFinite(a)) return Double.NaN;
            return Math.abs(a);
        }
        if (t == LexToken.Type.LN) {
            if (n.hijos.size() != 1) return Double.NaN;
            double a = f(n.hijos.get(0), var, x);
            if (!Double.isFinite(a) || a <= 0) return Double.NaN;
            return Math.log(a);
        }
        if (t == LexToken.Type.LOG || t == LexToken.Type.LOG_BASE10 || t == LexToken.Type.LOG_BASE2) {
            if (n.hijos.size() != 1) return Double.NaN;
            double a = f(n.hijos.get(0), var, x);
            if (!Double.isFinite(a) || a <= 0) return Double.NaN;
            if (t == LexToken.Type.LOG_BASE10) return Math.log10(a);
            if (t == LexToken.Type.LOG_BASE2) return Math.log(a) / Math.log(2.0);
            return Math.log(a);
        }
        if (t == LexToken.Type.TRIG_SIN) { double a = f(n.hijos.get(0), var, x); return Double.isFinite(a) ? Math.sin(a) : Double.NaN; }
        if (t == LexToken.Type.TRIG_COS) { double a = f(n.hijos.get(0), var, x); return Double.isFinite(a) ? Math.cos(a) : Double.NaN; }
        if (t == LexToken.Type.TRIG_TAN) {
            double a = f(n.hijos.get(0), var, x);
            if (!Double.isFinite(a)) return Double.NaN;
            double c = Math.cos(a); if (Math.abs(c) < 1e-15) return Double.NaN;
            return Math.tan(a);
        }
        if (t == LexToken.Type.TRIG_COT) {
            double a = f(n.hijos.get(0), var, x);
            if (!Double.isFinite(a)) return Double.NaN;
            double s = Math.sin(a); if (Math.abs(s) < 1e-15) return Double.NaN;
            return Math.cos(a) / s;
        }
        if (t == LexToken.Type.TRIG_SEC) {
            double a = f(n.hijos.get(0), var, x);
            if (!Double.isFinite(a)) return Double.NaN;
            double c = Math.cos(a); if (Math.abs(c) < 1e-15) return Double.NaN;
            return 1.0 / c;
        }
        if (t == LexToken.Type.TRIG_CSC) {
            double a = f(n.hijos.get(0), var, x);
            if (!Double.isFinite(a)) return Double.NaN;
            double s = Math.sin(a); if (Math.abs(s) < 1e-15) return Double.NaN;
            return 1.0 / s;
        }
        if (t == LexToken.Type.TRIG_ARCSIN) {
            double a = f(n.hijos.get(0), var, x);
            if (!Double.isFinite(a) || a < -1 || a > 1) return Double.NaN;
            return Math.asin(a);
        }
        if (t == LexToken.Type.TRIG_ARCCOS) {
            double a = f(n.hijos.get(0), var, x);
            if (!Double.isFinite(a) || a < -1 || a > 1) return Double.NaN;
            return Math.acos(a);
        }
        if (t == LexToken.Type.TRIG_ARCTAN) {
            double a = f(n.hijos.get(0), var, x);
            return Double.isFinite(a) ? Math.atan(a) : Double.NaN;
        }
        if (t == LexToken.Type.TRIG_ARCCOT) {
            double a = f(n.hijos.get(0), var, x);
            if (!Double.isFinite(a)) return Double.NaN;
            return Math.atan2(1.0, a);
        }
        if (t == LexToken.Type.TRIG_ARCSEC) {
            double a = f(n.hijos.get(0), var, x);
            if (!Double.isFinite(a) || Math.abs(a) < 1) return Double.NaN;
            return Math.acos(1.0 / a);
        }
        if (t == LexToken.Type.TRIG_ARCCSC) {
            double a = f(n.hijos.get(0), var, x);
            if (!Double.isFinite(a) || Math.abs(a) < 1) return Double.NaN;
            return Math.asin(1.0 / a);
        }

        if (t == LexToken.Type.IMAGINARY || t == LexToken.Type.FACTORIAL || t == LexToken.Type.DERIV || t == LexToken.Type.INTEGRAL_DEF || t == LexToken.Type.INTEGRAL_INDEF)
            return Double.NaN;

        double s = 0;
        for (NodoAST h : n.hijos) {
            double v = f(h, var, x);
            if (!Double.isFinite(v)) return Double.NaN;
            s += v;
        }
        return s;
    }

    private static double deriv(NodoAST F, String var, double x) {
        double h = stepH(x);
        double f1 = f(F, var, x + h), f2 = f(F, var, x - h);
        if (Double.isFinite(f1) && Double.isFinite(f2)) return (f1 - f2) / (2 * h);
        double fwd = f(F, var, x + h), f0 = f(F, var, x);
        if (Double.isFinite(fwd) && Double.isFinite(f0)) return (fwd - f0) / h;
        double bwd = f(F, var, x), bm = f(F, var, x - h);
        if (Double.isFinite(bwd) && Double.isFinite(bm)) return (bwd - bm) / h;
        return Double.NaN;
    }

    private static double stepH(double x) {
        double ax = Math.max(1.0, Math.abs(x));
        return 1e-6 * ax;
    }

    private static double secant(double x0, double f0, double x1, double f1) {
        double d = f1 - f0;
        if (Math.abs(d) < 1e-14) return 0.5 * (x0 + x1);
        return x1 - f1 * (x1 - x0) / d;
    }

    private static void addDedup(List<Double> roots, double r, double tol) {
        for (double z : roots) if (Math.abs(z - r) < tol) return;
        roots.add(r);
    }

    private static String unicaVariable(NodoAST n) {
        Set<String> s = new LinkedHashSet<>();
        vars(n, s);
        return s.size() == 1 ? s.iterator().next() : null;
    }

    private static void vars(NodoAST n, Set<String> s) {
        if (n == null) return;
        if (n.token != null && n.token.type == LexToken.Type.VARIABLE) s.add(n.token.value);
        for (NodoAST h : n.hijos) vars(h, s);
    }

    private static NodoAST resta(NodoAST a, NodoAST b) {
        NodoAST n = new NodoAST(new LexToken(LexToken.Type.SUB, "-", 5));
        n.addHijo(cloneAST(a));
        n.addHijo(cloneAST(b));
        return n;
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
}
