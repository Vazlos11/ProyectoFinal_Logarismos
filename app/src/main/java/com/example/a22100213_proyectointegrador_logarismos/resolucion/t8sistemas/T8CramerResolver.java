package com.example.a22100213_proyectointegrador_logarismos.resolucion.t8sistemas;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.TipoExpresion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.Resolver;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

public final class T8CramerResolver implements Resolver {
    @Override
    public boolean supports(ResultadoSemantico rs) {
        return rs != null && rs.tipoPrincipal == TipoExpresion.T8_SISTEMA_EC;
    }

    @Override
    public ResultadoResolucion resolve(NodoAST raiz, ResultadoSemantico rs) {
        ResultadoResolucion rr = new ResultadoResolucion();
        rr.pasos.add(new PasoResolucion("Expresión inicial", AstUtils.toTeX(raiz)));

        List<NodoAST> eqs = ecuaciones(raiz);
        LinkedHashSet<String> vs = new LinkedHashSet<>(varsNoI(raiz));
        List<String> vars = new ArrayList<>(vs);
        Collections.sort(vars);
        int n = eqs.size();

        rr.pasos.add(new PasoResolucion("Variables detectadas", "\\{" + String.join(",", vars) + "\\}"));
        rr.pasos.add(new PasoResolucion("Número de ecuaciones", Integer.toString(n)));

        if (n < 2 || n > 3 || vars.size() != n) {
            String msg = "\\text{Cramer no aplicable}";
            rr.pasos.add(new PasoResolucion("Clasificación", msg));
            rr.resultado = raiz;
            rr.latexFinal = AstUtils.toTeX(raiz);
            rr.pasos.add(new PasoResolucion("Resultado", rr.latexFinal));
            return rr;
        }

        double[][] A = new double[n][n];
        double[] b = new double[n];

        for (int i = 0; i < n; i++) {
            NodoAST e = eqs.get(i);
            NodoAST L = sub(e, 0), R = sub(e, 1);
            NodoAST F = resta(L, R);
            Decomp d = decompose(F, vars);
            if (!d.ok) {
                String msg = "\\text{No lineal o no descomponible}";
                rr.pasos.add(new PasoResolucion("Clasificación", msg));
                rr.resultado = raiz;
                rr.latexFinal = AstUtils.toTeX(raiz);
                rr.pasos.add(new PasoResolucion("Resultado", rr.latexFinal));
                return rr;
            }
            for (int j = 0; j < n; j++) A[i][j] = d.coef.getOrDefault(vars.get(j), 0.0);
            b[i] = -d.c;
        }

        for (int i = 0; i < n; i++) {
            rr.pasos.add(new PasoResolucion("Ecuación normalizada " + (i + 1), AstUtils.toTeX(eqNormalizado(A[i], b[i], vars))));
        }

        if (n == 2) {
            double a1 = A[0][0], b1 = A[0][1], c1 = b[0];
            double a2 = A[1][0], b2 = A[1][1], c2 = b[1];
            double D = a1*b2 - a2*b1;
            double Dx = c1*b2 - c2*b1;
            double Dy = a1*c2 - a2*c1;

            rr.pasos.add(new PasoResolucion("Determinante D", "D=(" + num(a1) + ")(" + num(b2) + ")-(" + num(a2) + ")(" + num(b1) + ")=" + num(D)));
            rr.pasos.add(new PasoResolucion("Determinante " + vars.get(0), "D_{" + vars.get(0) + "}=(" + num(c1) + ")(" + num(b2) + ")-(" + num(c2) + ")(" + num(b1) + ")=" + num(Dx)));
            rr.pasos.add(new PasoResolucion("Determinante " + vars.get(1), "D_{" + vars.get(1) + "}=(" + num(a1) + ")(" + num(c2) + ")-(" + num(a2) + ")(" + num(c1) + ")=" + num(Dy)));

            if (Math.abs(D) < 1e-12) {
                String msg = "\\text{D=0, Cramer no aplica}";
                rr.pasos.add(new PasoResolucion("Conclusión", msg));
                rr.resultado = raiz;
                rr.latexFinal = msg;
                rr.pasos.add(new PasoResolucion("Resultado", rr.latexFinal));
                return rr;
            }

            double x = Dx/D, y = Dy/D;
            rr.pasos.add(new PasoResolucion("Despeje " + vars.get(0), vars.get(0) + "=" + num(Dx) + "/" + num(D) + "=" + num(x)));
            rr.pasos.add(new PasoResolucion("Despeje " + vars.get(1), vars.get(1) + "=" + num(Dy) + "/" + num(D) + "=" + num(y)));
            rr.resultado = raiz;
            rr.latexFinal = vars.get(0) + "=" + num(x) + "\\;," + vars.get(1) + "=" + num(y);
            rr.pasos.add(new PasoResolucion("Resultado", rr.latexFinal));
            return rr;
        }

        double a1 = A[0][0], b1c = A[0][1], c1c = A[0][2], d1 = b[0];
        double a2 = A[1][0], b2c = A[1][1], c2c = A[1][2], d2 = b[1];
        double a3 = A[2][0], b3c = A[2][1], c3c = A[2][2], d3 = b[2];

        double D = a1*(b2c*c3c - b3c*c2c) - b1c*(a2*c3c - a3*c2c) + c1c*(a2*b3c - a3*b2c);
        double Dx = d1*(b2c*c3c - b3c*c2c) - b1c*(d2*c3c - d3*c2c) + c1c*(d2*b3c - d3*b2c);
        double Dy = a1*(d2*c3c - d3*c2c) - d1*(a2*c3c - a3*c2c) + c1c*(a2*d3 - a3*d2);
        double Dz = a1*(b2c*d3 - b3c*d2) - b1c*(a2*d3 - a3*d2) + d1*(a2*b3c - a3*b2c);

        rr.pasos.add(new PasoResolucion("Determinante D", "D=" + num(a1) + "(" + num(b2c) + numSign(-b3c) + num(Math.abs(b3c)) + numSign(-c2c) + num(Math.abs(c2c)) + ")-" + num(b1c) + "(" + num(a2) + numSign(-a3) + num(Math.abs(a3)) + num(c3c) + ")+" + num(c1c) + "(" + num(a2) + numSign(-a3) + num(Math.abs(a3)) + num(b3c) + ")=" + num(D)));
        rr.pasos.add(new PasoResolucion("Determinante " + vars.get(0), "D_{" + vars.get(0) + "}=" + num(d1) + "(" + num(b2c) + numSign(-b3c) + num(Math.abs(b3c)) + numSign(-c2c) + num(Math.abs(c2c)) + ")-" + num(b1c) + "(" + num(d2) + numSign(-d3) + num(Math.abs(d3)) + num(c3c) + ")+" + num(c1c) + "(" + num(d2) + numSign(-d3) + num(Math.abs(d3)) + num(b3c) + ")=" + num(Dx)));
        rr.pasos.add(new PasoResolucion("Determinante " + vars.get(1), "D_{" + vars.get(1) + "}=" + num(a1) + "(" + num(d2) + numSign(-d3) + num(Math.abs(d3)) + num(c2c) + ")-" + num(d1) + "(" + num(a2) + numSign(-a3) + num(Math.abs(a3)) + num(c3c) + ")+" + num(c1c) + "(" + num(a2) + numSign(-a3) + num(Math.abs(a3)) + num(d2) + ")=" + num(Dy)));
        rr.pasos.add(new PasoResolucion("Determinante " + vars.get(2), "D_{" + vars.get(2) + "}=" + num(a1) + "(" + num(b2c) + numSign(-b3c) + num(Math.abs(b3c)) + num(d3) + ")-" + num(b1c) + "(" + num(a2) + numSign(-a3) + num(Math.abs(a3)) + num(d2) + ")+" + num(d1) + "(" + num(a2) + numSign(-a3) + num(Math.abs(a3)) + num(b2c) + ")=" + num(Dz)));

        if (Math.abs(D) < 1e-12) {
            String msg = "\\text{D=0, Cramer no aplica}";
            rr.pasos.add(new PasoResolucion("Conclusión", msg));
            rr.resultado = raiz;
            rr.latexFinal = msg;
            rr.pasos.add(new PasoResolucion("Resultado", rr.latexFinal));
            return rr;
        }

        double x = Dx/D, y = Dy/D, z = Dz/D;
        rr.pasos.add(new PasoResolucion("Despeje " + vars.get(0), vars.get(0) + "=" + num(Dx) + "/" + num(D) + "=" + num(x)));
        rr.pasos.add(new PasoResolucion("Despeje " + vars.get(1), vars.get(1) + "=" + num(Dy) + "/" + num(D) + "=" + num(y)));
        rr.pasos.add(new PasoResolucion("Despeje " + vars.get(2), vars.get(2) + "=" + num(Dz) + "/" + num(D) + "=" + num(z)));
        rr.resultado = raiz;
        rr.latexFinal = vars.get(0) + "=" + num(x) + "\\;," + vars.get(1) + "=" + num(y) + "\\;," + vars.get(2) + "=" + num(z);
        rr.pasos.add(new PasoResolucion("Resultado", rr.latexFinal));
        return rr;
    }

    private static class Decomp {
        final Map<String,Double> coef = new LinkedHashMap<>();
        double c;
        boolean ok = true;
    }

    private static Decomp decompose(NodoAST n, List<String> vars) {
        Decomp out = new Decomp();
        decomposeRec(n, vars, 1.0, out);
        return out;
    }

    private static void decomposeRec(NodoAST n, List<String> vars, double scale, Decomp out) {
        if (!out.ok || n == null || n.token == null) return;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL || t == LexToken.Type.CONST_E || t == LexToken.Type.CONST_PI) {
            Double v = evaluarConstante(n);
            if (v == null) { out.ok = false; return; }
            out.c += scale * v;
            return;
        }
        if (t == LexToken.Type.VARIABLE && vars.contains(n.token.value) && n.hijos.isEmpty()) {
            out.coef.put(n.token.value, out.coef.getOrDefault(n.token.value, 0.0) + scale);
            return;
        }
        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB) {
            if (n.hijos.size() == 0) { out.ok = false; return; }
            decomposeRec(n.hijos.get(0), vars, scale, out);
            for (int i = 1; i < n.hijos.size(); i++) {
                double s = t == LexToken.Type.SUB ? -scale : scale;
                decomposeRec(n.hijos.get(i), vars, s, out);
            }
            return;
        }
        if (t == LexToken.Type.MUL) {
            double cte = 1.0;
            NodoAST rest = null;
            for (NodoAST h : n.hijos) {
                Double ch = evaluarConstante(h);
                if (ch != null) cte *= ch; else rest = rest == null ? h : mul(rest, h);
            }
            if (rest == null) { out.c += scale * cte; return; }
            decomposeRec(rest, vars, scale * cte, out);
            return;
        }
        if (t == LexToken.Type.DIV && n.hijos.size() == 2) {
            Double den = evaluarConstante(n.hijos.get(1));
            if (den == null || Math.abs(den) < 1e-15) { out.ok = false; return; }
            decomposeRec(n.hijos.get(0), vars, scale/den, out);
            return;
        }
        if (t == LexToken.Type.EXP && n.hijos.size() == 2) {
            Double e = evaluarConstante(n.hijos.get(1));
            if (e != null && Math.abs(e - 1.0) < 1e-12) {
                decomposeRec(n.hijos.get(0), vars, scale, out);
                return;
            }
            out.ok = false;
            return;
        }
        out.ok = false;
    }

    private static List<NodoAST> ecuaciones(NodoAST n) {
        List<NodoAST> out = new ArrayList<>();
        collectEq(n, out);
        return out;
    }

    private static void collectEq(NodoAST n, List<NodoAST> out) {
        if (n == null) return;
        if (n.token != null && n.token.type == LexToken.Type.EQUAL && n.hijos.size() == 2) out.add(n);
        for (NodoAST h : n.hijos) collectEq(h, out);
    }

    private static Set<String> varsNoI(NodoAST n) {
        Set<String> s = new LinkedHashSet<>();
        if (n == null) return s;
        if (n.token != null && n.token.type == LexToken.Type.VARIABLE && n.hijos.isEmpty() && n.token.value != null && !n.token.value.equalsIgnoreCase("i")) s.add(n.token.value);
        for (NodoAST h : n.hijos) s.addAll(varsNoI(h));
        return s;
    }

    private static NodoAST eqNormalizado(double[] row, double rhs, List<String> vars) {
        NodoAST sum = null;
        for (int j = 0; j < row.length; j++) {
            double a = row[j];
            if (Math.abs(a) < 1e-15) continue;
            NodoAST term = mul(numNode(a), varNode(vars.get(j)));
            sum = sum == null ? term : add(sum, term);
        }
        if (sum == null) sum = numNode(0);
        NodoAST eq = new NodoAST(new LexToken(LexToken.Type.EQUAL, "=", 2));
        eq.addHijo(sum);
        eq.addHijo(numNode(rhs));
        return eq;
    }

    private static NodoAST add(NodoAST a, NodoAST b) {
        NodoAST n = new NodoAST(new LexToken(LexToken.Type.SUM, "+", 5));
        n.addHijo(a); n.addHijo(b); return n;
    }

    private static NodoAST mul(NodoAST a, NodoAST b) {
        NodoAST n = new NodoAST(new LexToken(LexToken.Type.MUL, "*", 6));
        n.addHijo(a); n.addHijo(b); return n;
    }

    private static NodoAST numNode(double v) {
        long r = Math.round(v);
        if (Math.abs(v - r) < 1e-10) return new NodoAST(new LexToken(LexToken.Type.INTEGER, Long.toString(r), 1));
        DecimalFormat df = new DecimalFormat("0.##########", DecimalFormatSymbols.getInstance(Locale.US));
        return new NodoAST(new LexToken(LexToken.Type.DECIMAL, df.format(v), 1));
    }

    private static NodoAST varNode(String v) {
        return new NodoAST(new LexToken(LexToken.Type.VARIABLE, v, 1));
    }

    private static NodoAST resta(NodoAST a, NodoAST b) {
        NodoAST n = new NodoAST(new LexToken(LexToken.Type.SUB, "-", 5));
        n.addHijo(cloneAST(a));
        n.addHijo(cloneAST(b));
        return n;
    }

    private static NodoAST sub(NodoAST n, int i) {
        return (n != null && i >= 0 && i < n.hijos.size()) ? n.hijos.get(i) : null;
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

    private static Double evaluarConstante(NodoAST n) {
        if (n == null || n.token == null) return null;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL) {
            try { return Double.valueOf(n.token.value); } catch (Exception e) { return null; }
        }
        if (t == LexToken.Type.CONST_E) return Math.E;
        if (t == LexToken.Type.CONST_PI) return Math.PI;
        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB || t == LexToken.Type.MUL || t == LexToken.Type.DIV || t == LexToken.Type.EXP) {
            if (n.hijos.size() == 2) {
                Double a = evaluarConstante(n.hijos.get(0));
                Double b = evaluarConstante(n.hijos.get(1));
                if (t == LexToken.Type.SUM) return a != null && b != null ? a + b : null;
                if (t == LexToken.Type.SUB) return a != null && b != null ? a - b : null;
                if (t == LexToken.Type.MUL) return a != null && b != null ? a * b : null;
                if (t == LexToken.Type.DIV) return a != null && b != null && Math.abs(b) >= 1e-15 ? a / b : null;
                if (t == LexToken.Type.EXP) return a != null && b != null ? Math.pow(a, b) : null;
            } else {
                double acc = t == LexToken.Type.MUL ? 1.0 : 0.0;
                if (t == LexToken.Type.SUM) {
                    for (NodoAST h : n.hijos) {
                        Double v = evaluarConstante(h);
                        if (v == null) return null;
                        acc += v;
                    }
                    return acc;
                }
                if (t == LexToken.Type.MUL) {
                    for (NodoAST h : n.hijos) {
                        Double v = evaluarConstante(h);
                        if (v == null) return null;
                        acc *= v;
                    }
                    return acc;
                }
                return null;
            }
        }
        return null;
    }

    private static String num(double x) {
        if (Double.isNaN(x)) return "\\text{NaN}";
        if (Double.isInfinite(x)) return x > 0 ? "\\infty" : "-\\infty";
        long r = Math.round(x);
        if (Math.abs(x - r) < 1e-10) return Long.toString(r);
        DecimalFormat df = new DecimalFormat("0.##########", DecimalFormatSymbols.getInstance(Locale.US));
        return df.format(x);
    }

    private static String numSign(double v) {
        return v >= 0 ? "+" : "-";
    }
}
