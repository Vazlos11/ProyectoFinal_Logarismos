package com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.rules;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegratorRule;

public class TrigSubstitutionIntegrator implements IntegratorRule {
    private final boolean definida;

    public TrigSubstitutionIntegrator(boolean definida) { this.definida = definida; }

    @Override
    public ResultadoResolucion apply(NodoAST raiz, ResultadoSemantico rs) {
        IntegralUtils.IntegralInfo ii = IntegralUtils.localizarIntegral(raiz, definida);
        if (ii == null || ii.cuerpo == null || ii.var == null) return null;
        String x = ii.var;

        NodoAST f = ii.cuerpo;
        if (!isRadical(f)) return new PowerRuleIntegrator(definida).apply(raiz, rs);

        NodoAST inside = f.hijos.get(0);
        if (inside == null || inside.token == null || inside.hijos.size() != 2) return new PowerRuleIntegrator(definida).apply(raiz, rs);

        int mode = 0;
        NodoAST sq = null, B = null, uBase = null;

        if (isSquare(inside.hijos.get(0)) && !isSquare(inside.hijos.get(1))) {
            sq = inside.hijos.get(0);
            B = inside.hijos.get(1);
            uBase = sq.hijos.get(0);
            mode = 1;
        } else if (!isSquare(inside.hijos.get(0)) && isSquare(inside.hijos.get(1))) {
            B = inside.hijos.get(0);
            sq = inside.hijos.get(1);
            uBase = sq.hijos.get(0);
            mode = inside.token.type == LexToken.Type.SUM ? 2 : 3;
        } else if (isSquare(inside.hijos.get(0)) && isSquare(inside.hijos.get(1))) {
            uBase = inside.hijos.get(0).hijos.get(0);
            B = inside.hijos.get(1);
            mode = inside.token.type == LexToken.Type.SUM ? 2 : 3;
        } else {
            return new PowerRuleIntegrator(definida).apply(raiz, rs);
        }

        Linear lin = linearDe(uBase, x);
        if (!lin.ok || Math.abs(lin.a) < 1e-15) return new PowerRuleIntegrator(definida).apply(raiz, rs);

        Double A0 = AstUtils.evalConst(B);
        if (A0 == null) return new PowerRuleIntegrator(definida).apply(raiz, rs);
        double A = Math.abs(A0);

        if (mode == 1) {
            if (definida) return new SimpsonDefiniteIntegrator(true).apply(raiz, rs);
            return null;
        }

        NodoAST u = linearNode(lin.a, lin.b, x);
        NodoAST u2 = AstUtils.bin(LexToken.Type.EXP, AstUtils.cloneTree(u), AstUtils.number(2.0), "^", 7);
        NodoAST Aconst = AstUtils.number(A);

        NodoAST sqrtRad;
        NodoAST term1;
        NodoAST term2;
        double pref = 0.5 / lin.a;

        if (mode == 2) {
            NodoAST sumArg = AstUtils.bin(LexToken.Type.SUM, AstUtils.cloneTree(Aconst), AstUtils.cloneTree(u2), "+", 5);
            sqrtRad = sqrtOf(sumArg);
            term1 = AstUtils.bin(LexToken.Type.MUL, AstUtils.cloneTree(u), AstUtils.cloneTree(sqrtRad), "*", 6);
            NodoAST lnArg = AstUtils.bin(LexToken.Type.SUM, AstUtils.cloneTree(u), AstUtils.cloneTree(sqrtRad), "+", 5);
            NodoAST ln = IntegralUtils.lnClone(lnArg);
            term2 = AstUtils.bin(LexToken.Type.MUL, AstUtils.cloneTree(Aconst), ln, "*", 6);
        } else {
            NodoAST subArg = AstUtils.bin(LexToken.Type.SUB, AstUtils.cloneTree(u2), AstUtils.cloneTree(Aconst), "-", 5);
            sqrtRad = sqrtOf(subArg);
            term1 = AstUtils.bin(LexToken.Type.MUL, AstUtils.cloneTree(u), AstUtils.cloneTree(sqrtRad), "*", 6);
            NodoAST lnArg = AstUtils.bin(LexToken.Type.SUM, AstUtils.cloneTree(u), AstUtils.cloneTree(sqrtRad), "+", 5);
            NodoAST ln = IntegralUtils.lnClone(lnArg);
            NodoAST aLn = AstUtils.bin(LexToken.Type.MUL, AstUtils.cloneTree(Aconst), ln, "*", 6);
            term2 = AstUtils.bin(LexToken.Type.MUL, AstUtils.number(-1.0), aLn, "*", 6);
        }

        NodoAST sum = AstUtils.bin(LexToken.Type.SUM, term1, term2, "+", 5);
        NodoAST F = IntegralUtils.mulC(sum, pref);

        NodoAST out = definida ? IntegralUtils.evalDefinida(F, x, ii.inf, ii.sup) : IntegralUtils.addC(F);
        NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, AstUtils.cloneTree(out), ii.padre, out);

        ResultadoResolucion rr = new ResultadoResolucion();
        rr.resultado = nuevo;
        rr.latexFinal = AstUtils.toTeX(nuevo);
        if (definida) {
            NodoAST Fb = IntegralUtils.sustituirVar(AstUtils.cloneTree(F), x, AstUtils.cloneTree(ii.sup));
            NodoAST Fa = IntegralUtils.sustituirVar(AstUtils.cloneTree(F), x, AstUtils.cloneTree(ii.inf));
            rr.pasos.add(new PasoResolucion(AstUtils.toTeX(Fb)));
            rr.pasos.add(new PasoResolucion(AstUtils.toTeX(Fa)));
            rr.pasos.add(new PasoResolucion(AstUtils.toTeX(out)));
        } else {
            rr.pasos.add(new PasoResolucion(AstUtils.toTeX(IntegralUtils.addC(AstUtils.cloneTree(F)))));
        }
        rr.pasos.add(new PasoResolucion(rr.latexFinal));
        return rr;
    }

    private static boolean isRadical(NodoAST n) {
        return n != null && n.token != null && n.token.type == LexToken.Type.RADICAL && n.hijos.size() == 1;
    }

    private static boolean isSquare(NodoAST n) {
        if (n == null || n.token == null || n.token.type != LexToken.Type.EXP || n.hijos.size() != 2) return false;
        Double e = AstUtils.evalConst(n.hijos.get(1));
        return e != null && Math.abs(e - 2.0) < 1e-9;
    }

    private static class Linear {
        final boolean ok;
        final double a;
        final double b;
        Linear(boolean ok, double a, double b) { this.ok = ok; this.a = a; this.b = b; }
    }

    private static Linear linearDe(NodoAST u, String var) {
        if (u == null) return new Linear(false, 0, 0);

        if (!IntegralUtils.onlyVar(u, var)) return new Linear(false, 0, 0);

        Double a = IntegralUtils.linearCoeff(u, var);
        if (a == null) return new Linear(false, 0, 0);
        if (Math.abs(a) < 1e-15) return new Linear(false, 0, 0);

        Double b = AstUtils.evalConst(
                IntegralUtils.sustituirVar(AstUtils.cloneTree(u), var, AstUtils.number(0.0))
        );
        if (b == null) return new Linear(false, 0, 0);

        return new Linear(true, a, b);
    }

    private static boolean isVar(NodoAST n, String var) {
        return n != null && n.token != null && n.token.type == LexToken.Type.VARIABLE && var.equals(n.token.value) && n.hijos.isEmpty();
    }

    private static NodoAST sqrtOf(NodoAST expr) {
        return AstUtils.bin(LexToken.Type.EXP, expr, AstUtils.number(0.5), "^", 7);
    }

    private static NodoAST linearNode(double a, double b, String v) {
        NodoAST ax = AstUtils.bin(LexToken.Type.MUL, AstUtils.number(a), AstUtils.atom(LexToken.Type.VARIABLE, v, 1), "*", 6);
        return AstUtils.bin(LexToken.Type.SUM, ax, AstUtils.number(b), "+", 5);
    }
}
