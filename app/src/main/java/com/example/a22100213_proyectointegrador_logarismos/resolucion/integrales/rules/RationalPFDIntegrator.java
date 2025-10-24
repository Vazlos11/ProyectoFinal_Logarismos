package com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.rules;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegratorRule;

import java.util.ArrayList;
import java.util.List;

public final class RationalPFDIntegrator implements IntegratorRule {
    private final boolean definida;

    public RationalPFDIntegrator(boolean definida) {
        this.definida = definida;
    }

    @Override
    public ResultadoResolucion apply(NodoAST raiz, ResultadoSemantico rs) {
        IntegralUtils.IntegralInfo ii = IntegralUtils.localizarIntegral(raiz, definida);
        if (ii == null || ii.cuerpo == null || ii.var == null) return null;
        String x = ii.var;

        Decomp d = matchRationalWithLinearDen(ii.cuerpo, x);
        if (d == null) return null;

        List<Term> terms = solveCoeffsDistinctLinear(d, x);
        if (terms == null || terms.isEmpty()) return null;

        NodoAST F = null;
        for (Term t : terms) {
            double c = t.A / t.a;
            NodoAST ln = IntegralUtils.lnClone(linearNode(t.a, t.b, x));
            NodoAST term = IntegralUtils.mulC(ln, c);
            F = (F == null) ? term : AstUtils.bin(LexToken.Type.SUM, F, term, "+", 5);
        }
        if (F == null) return null;

        NodoAST out = definida ? IntegralUtils.evalDefinida(F, x, ii.inf, ii.sup) : IntegralUtils.addC(F);
        NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, out, ii.padre, out);

        ResultadoResolucion rr = new ResultadoResolucion();
        rr.resultado = nuevo;
        rr.latexFinal = AstUtils.toTeX(nuevo);
        String tex = AstUtils.toTeX(ii.cuerpo);
        rr.pasos.add(new PasoResolucion("\\text{Fracciones parciales sobre } " + tex));
        rr.pasos.add(new PasoResolucion(rr.latexFinal));
        return rr;
    }

    private static class Decomp {
        NodoAST num;
        List<Lin> den;
        double constDen;
    }

    private static class Lin {
        double a;
        double b;
    }

    private static class Term {
        double A;
        double a;
        double b;
    }

    private Decomp matchRationalWithLinearDen(NodoAST n, String v) {
        if (n == null || n.token == null || n.token.type != LexToken.Type.DIV || n.hijos.size() != 2) return null;
        NodoAST num = n.hijos.get(0);
        NodoAST den = n.hijos.get(1);

        Integer dn = degreePoly(den, v);
        Integer nn = degreePoly(num, v);
        if (dn == null || nn == null || nn >= dn) return null;

        List<NodoAST> fs = IntegralUtils.flattenMul(den);
        double constDen = 1.0;
        List<Lin> factors = new ArrayList<>();

        if (fs.size() > 1) {
            for (NodoAST f : fs) {
                Double c = AstUtils.evalConst(f);
                if (c != null) {
                    constDen *= c;
                    continue;
                }
                Lin l = linearAB(f, v);
                if (l == null) return null;
                factors.add(l);
            }
        } else {
            Lin l = linearAB(den, v);
            if (l != null) {
                factors.add(l);
            } else {
                Quad q = quadCoeffs(den, v);
                if (q == null) return null;
                double D = q.b * q.b - 4.0 * q.a * q.c;
                if (!(D > 1e-12)) return null;
                double s = Math.sqrt(D);
                double r1 = (-q.b + s) / (2.0 * q.a);
                double r2 = (-q.b - s) / (2.0 * q.a);
                Lin l1 = new Lin(); l1.a = 1.0; l1.b = -r1;
                Lin l2 = new Lin(); l2.a = 1.0; l2.b = -r2;
                factors.add(l1);
                factors.add(l2);
                constDen *= q.a;
            }
        }

        for (int i = 0; i < factors.size(); i++) {
            for (int j = i + 1; j < factors.size(); j++) {
                double r1 = -factors.get(i).b / factors.get(i).a;
                double r2 = -factors.get(j).b / factors.get(j).a;
                if (Math.abs(r1 - r2) < 1e-12) return null;
            }
        }

        Decomp d = new Decomp();
        d.num = num;
        d.den = factors;
        d.constDen = constDen;
        return d;
    }

    private List<Term> solveCoeffsDistinctLinear(Decomp d, String v) {
        List<Term> out = new ArrayList<>();
        for (int i = 0; i < d.den.size(); i++) {
            Lin li = d.den.get(i);
            double ri = -li.b / li.a;
            Double pVal = evalAt(d.num, v, ri);
            if (pVal == null) return null;
            double prod = d.constDen;
            for (int j = 0; j < d.den.size(); j++) {
                if (j == i) continue;
                Lin lj = d.den.get(j);
                prod *= (lj.a * ri + lj.b);
            }
            if (Math.abs(prod) < 1e-15) return null;
            double Ai = pVal / prod;
            Term t = new Term();
            t.A = Ai;
            t.a = li.a;
            t.b = li.b;
            out.add(t);
        }
        return out;
    }

    private static Integer degreePoly(NodoAST n, String v) {
        if (n == null || n.token == null) return null;
        switch (n.token.type) {
            case INTEGER:
            case DECIMAL:
            case CONST_E:
            case CONST_PI:
                return 0;
            case VARIABLE:
                if (v.equals(n.token.value) && n.hijos.isEmpty()) return 1;
                return 0;
            case SUM:
            case SUB: {
                Integer a = degreePoly(n.hijos.get(0), v);
                Integer b = degreePoly(n.hijos.get(1), v);
                if (a == null || b == null) return null;
                return Math.max(a, b);
            }
            case MUL: {
                Integer a = degreePoly(n.hijos.get(0), v);
                Integer b = degreePoly(n.hijos.get(1), v);
                if (a == null || b == null) return null;
                return a + b;
            }
            case EXP: {
                NodoAST base = n.hijos.get(0);
                NodoAST ex = n.hijos.get(1);
                if (base != null && base.token != null && base.token.type == LexToken.Type.VARIABLE && v.equals(base.token.value)) {
                    Double e = AstUtils.evalConst(ex);
                    if (e == null) return null;
                    if (e < 0 || Math.abs(e - Math.rint(e)) > 1e-12) return null;
                    return (int) Math.rint(e);
                }
                Integer db = degreePoly(base, v);
                Integer de = degreePoly(ex, v);
                if (db != null && db == 0 && de != null && de == 0) return 0;
                return null;
            }
            default:
                return null;
        }
    }

    private static class Quad { double a, b, c; }
    private static Quad quadCoeffs(NodoAST n, String v) {
        double[] c = coeff012(n, v);
        if (c == null) return null;
        if (Math.abs(c[2]) < 1e-12) return null;
        Quad q = new Quad();
        q.a = c[2];
        q.b = c[1];
        q.c = c[0];
        return q;
    }

    private static double[] coeff012(NodoAST n, String v) {
        if (n == null || n.token == null) return null;
        switch (n.token.type) {
            case INTEGER:
            case DECIMAL:
            case CONST_E:
            case CONST_PI: {
                Double k = AstUtils.evalConst(n);
                return (k == null) ? null : new double[]{k, 0.0, 0.0};
            }
            case VARIABLE:
                if (v.equals(n.token.value) && n.hijos.isEmpty())
                    return new double[]{0.0, 1.0, 0.0};
                return new double[]{0.0, 0.0, 0.0};
            case SUM:
            case SUB: {
                double[] A = coeff012(n.hijos.get(0), v);
                double[] B = coeff012(n.hijos.get(1), v);
                if (A == null || B == null) return null;
                if (n.token.type == LexToken.Type.SUB) B = new double[]{-B[0], -B[1], -B[2]};
                return new double[]{A[0] + B[0], A[1] + B[1], A[2] + B[2]};
            }
            case MUL: {
                double[] A = coeff012(n.hijos.get(0), v);
                double[] B = coeff012(n.hijos.get(1), v);
                if (A == null || B == null) return null;
                double c0 = A[0]*B[0];
                double c1 = A[0]*B[1] + A[1]*B[0];
                double c2 = A[0]*B[2] + A[1]*B[1] + A[2]*B[0];
                return new double[]{c0, c1, c2};
            }
            case EXP: {
                NodoAST base = n.hijos.get(0);
                NodoAST ex   = n.hijos.get(1);
                Double e = AstUtils.evalConst(ex);
                if (e == null) return null;
                int k = (int)Math.rint(e);
                if (Math.abs(e - k) > 1e-12 || k < 0) return null;
                if (base.token != null && base.token.type == LexToken.Type.VARIABLE && v.equals(base.token.value)) {
                    if (k == 0) return new double[]{1.0,0.0,0.0};
                    if (k == 1) return new double[]{0.0,1.0,0.0};
                    if (k == 2) return new double[]{0.0,0.0,1.0};
                    return null;
                }
                Double cst = AstUtils.evalConst(base);
                if (cst != null) return new double[]{Math.pow(cst, k), 0.0, 0.0};
                return null;
            }
            default:
                return null;
        }
    }

    private static Lin linearAB(NodoAST n, String v) {
        Double a = IntegralUtils.linearCoeff(n, v);
        if (a == null) return null;
        Double b = AstUtils.evalConst(IntegralUtils.sustituirVar(n, v, AstUtils.number(0.0)));
        if (b == null) return null;
        Lin l = new Lin();
        l.a = a;
        l.b = b;
        return l;
    }

    private static Double evalAt(NodoAST n, String v, double x) {
        NodoAST sub = IntegralUtils.sustituirVar(n, v, AstUtils.number(x));
        return AstUtils.evalConst(sub);
    }

    private static NodoAST linearNode(double a, double b, String v) {
        NodoAST ax = AstUtils.bin(LexToken.Type.MUL, AstUtils.number(a), AstUtils.atom(LexToken.Type.VARIABLE, v, 1), "*", 6);
        return AstUtils.bin(LexToken.Type.SUM, ax, AstUtils.number(b), "+", 5);
    }
}
