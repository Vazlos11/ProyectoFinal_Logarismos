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
        com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils.IntegralInfo ii =
                com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils.localizarIntegral(raiz, definida);
        if (ii == null || ii.cuerpo == null || ii.var == null) return null;

        NodoAST cuerpo = com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils
                .foldEConstTimesXIntoExpLinear(ii.cuerpo, ii.var);
        cuerpo = com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils
                .normalizeSquaresForTrigSub(cuerpo, ii.var);

        if (cuerpo == null || cuerpo.token == null || cuerpo.token.type != LexToken.Type.RADICAL || cuerpo.hijos.size() != 1)
            return null;

        NodoAST inside = cuerpo.hijos.get(0);
        if (inside == null || inside.token == null) return null;

        boolean isSum = inside.token.type == LexToken.Type.SUM;
        boolean isSub = inside.token.type == LexToken.Type.SUB;
        if ((!isSum && !isSub) || inside.hijos.size() != 2) return null;

        NodoAST A = inside.hijos.get(0);
        NodoAST B = inside.hijos.get(1);
        Double cA = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.evalConst(A);
        Double cB = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.evalConst(B);

        NodoAST quad = null;
        double a2 = 0.0;
        boolean formA2minusU2 = false, formA2plusU2 = false, formU2minusA2 = false;

        if (cA != null && cA >= 0 && isSquareOfLinear(B, ii.var)) {
            a2 = cA;
            quad = B;
            formA2minusU2 = isSub;
            formA2plusU2  = isSum;
        } else if (cB != null && cB >= 0 && isSquareOfLinear(A, ii.var)) {
            a2 = cB;
            quad = A;
            formA2minusU2 = isSub && cB != null && cB >= 0 && quad == A && inside.hijos.get(0) == quad;
            formU2minusA2 = isSub && cB != null && cB >= 0 && quad == A && inside.hijos.get(1) == B;
            formA2plusU2  = isSum && quad == A;
            if (!formA2minusU2 && !formU2minusA2 && !formA2plusU2) {
                if (inside.token.type == LexToken.Type.SUB && inside.hijos.get(0) == quad) formU2minusA2 = true;
            }
        } else {
            return null;
        }

        NodoAST u = quad.hijos.get(0);
        double alpha = slopeOfLinear(u, ii.var);
        if (Math.abs(alpha) < 1e-15) return null;

        NodoAST a = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.number(Math.sqrt(a2));
        NodoAST u2 = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.bin(
                LexToken.Type.EXP, com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.cloneTree(u),
                com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.number(2.0), "^", 7);

        NodoAST rad;
        if (formA2minusU2) {
            NodoAST a2Node = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.bin(
                    LexToken.Type.EXP, com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.cloneTree(a),
                    com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.number(2.0), "^", 7);
            NodoAST insideRad = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.bin(
                    LexToken.Type.SUB, a2Node, com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.cloneTree(u2), "-", 5);
            rad = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.un(LexToken.Type.RADICAL, insideRad, "sqrt", 11);

            NodoAST term1 = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.bin(
                    LexToken.Type.MUL, com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.cloneTree(u), rad, "*", 6);
            NodoAST uDivA = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.bin(
                    LexToken.Type.DIV, com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.cloneTree(u),
                    com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.cloneTree(a), "/", 6);
            NodoAST asin = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.un(LexToken.Type.TRIG_ARCSIN, uDivA, "arcsin", 12);
            NodoAST a2asin = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.bin(
                    LexToken.Type.MUL,
                    com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.bin(LexToken.Type.EXP, com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.cloneTree(a),
                            com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.number(2.0), "^", 7),
                    asin, "*", 6);
            NodoAST suma = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.bin(LexToken.Type.SUM, term1, a2asin, "+", 4);
            NodoAST factor = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.number(1.0 / (2.0 * alpha));
            NodoAST F = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.bin(LexToken.Type.MUL, factor, suma, "*", 6);

            return construirSalida(ii, F);
        }

        if (formA2plusU2) {
            NodoAST u2pa2 = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.bin(
                    LexToken.Type.SUM, com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.cloneTree(u2),
                    com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.bin(
                            LexToken.Type.EXP, com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.cloneTree(a),
                            com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.number(2.0), "^", 7), "+", 4);
            NodoAST rad2 = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.un(LexToken.Type.RADICAL, u2pa2, "sqrt", 11);

            NodoAST term1 = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.bin(
                    LexToken.Type.MUL, com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.cloneTree(u), rad2, "*", 6);

            NodoAST lnArg = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.bin(
                    LexToken.Type.SUM,
                    com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.cloneTree(u),
                    com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.cloneTree(rad2), "+", 4);
            NodoAST abs = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.un(LexToken.Type.ABS, lnArg, "|.|", 11);
            NodoAST ln = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.un(LexToken.Type.LN, abs, "ln", 12);

            NodoAST a2ln = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.bin(
                    LexToken.Type.MUL,
                    com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.bin(LexToken.Type.EXP, com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.cloneTree(a),
                            com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.number(2.0), "^", 7),
                    ln, "*", 6);

            NodoAST suma = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.bin(LexToken.Type.SUM, term1, a2ln, "+", 4);
            NodoAST factor = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.number(1.0 / (2.0 * alpha));
            NodoAST F = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.bin(LexToken.Type.MUL, factor, suma, "*", 6);

            return construirSalida(ii, F);
        }

        if (formU2minusA2) {
            NodoAST u2ma2 = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.bin(
                    LexToken.Type.SUB, com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.cloneTree(u2),
                    com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.bin(
                            LexToken.Type.EXP, com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.cloneTree(a),
                            com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.number(2.0), "^", 7), "-", 5);
            NodoAST rad3 = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.un(LexToken.Type.RADICAL, u2ma2, "sqrt", 11);

            NodoAST term1 = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.bin(
                    LexToken.Type.MUL, com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.cloneTree(u), rad3, "*", 6);

            NodoAST lnArg = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.bin(
                    LexToken.Type.SUM,
                    com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.cloneTree(u),
                    com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.cloneTree(rad3), "+", 4);
            NodoAST abs = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.un(LexToken.Type.ABS, lnArg, "|.|", 11);
            NodoAST ln = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.un(LexToken.Type.LN, abs, "ln", 12);

            NodoAST a2ln = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.bin(
                    LexToken.Type.MUL,
                    com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.bin(LexToken.Type.EXP, com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.cloneTree(a),
                            com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.number(2.0), "^", 7),
                    ln, "*", 6);

            NodoAST resta = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.bin(LexToken.Type.SUB, term1, a2ln, "-", 5);
            NodoAST factor = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.number(1.0 / (2.0 * alpha));
            NodoAST F = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.bin(LexToken.Type.MUL, factor, resta, "*", 6);

            return construirSalida(ii, F);
        }

        return null;
    }

    private boolean isSquareOfLinear(NodoAST n, String var) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.EXP && n.hijos.size() == 2) {
            Double e = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.evalConst(n.hijos.get(1));
            return e != null && Math.abs(e - 2.0) < 1e-9 && hasLinear(n.hijos.get(0), var);
        }
        return false;
    }

    private boolean hasLinear(NodoAST u, String var) {
        if (u == null || u.token == null) return false;
        if (u.token.type == LexToken.Type.VARIABLE && var.equals(u.token.value)) return true;
        if (u.token.type == LexToken.Type.MUL && u.hijos.size() == 2)
            return hasLinear(u.hijos.get(0), var) || hasLinear(u.hijos.get(1), var);
        if ((u.token.type == LexToken.Type.SUM || u.token.type == LexToken.Type.SUB) && u.hijos.size() == 2)
            return hasLinear(u.hijos.get(0), var) || hasLinear(u.hijos.get(1), var);
        return false;
    }

    private double slopeOfLinear(NodoAST u, String var) {
        if (u == null || u.token == null) return 0.0;
        if (u.token.type == LexToken.Type.VARIABLE && var.equals(u.token.value)) return 1.0;

        if (u.token.type == LexToken.Type.MUL && u.hijos.size() == 2) {
            Double cL = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.evalConst(u.hijos.get(0));
            Double cR = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.evalConst(u.hijos.get(1));
            if (cL != null && containsVar(u.hijos.get(1), var)) return cL;
            if (cR != null && containsVar(u.hijos.get(0), var)) return cR;
        }

        if ((u.token.type == LexToken.Type.SUM || u.token.type == LexToken.Type.SUB) && u.hijos.size() == 2) {
            return slopeOfLinear(u.hijos.get(0), var) + ((u.token.type == LexToken.Type.SUM) ? 1 : -1) * slopeOfLinear(u.hijos.get(1), var);
        }
        return 0.0;
    }

    private boolean containsVar(NodoAST n, String var) {
        if (n == null) return false;
        if (n.token != null && n.token.type == LexToken.Type.VARIABLE && var.equals(n.token.value)) return true;
        for (NodoAST h : n.hijos) if (containsVar(h, var)) return true;
        return false;
    }

    private ResultadoResolucion construirSalida(com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils.IntegralInfo ii, NodoAST F) {
        if (!definida) {
            NodoAST fin = com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils.addC(F);
            NodoAST nuevo = com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils
                    .reemplazar(ii.nodoIntegral,
                            com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.cloneTree(fin),
                            ii.padre, fin);
            ResultadoResolucion rr = new ResultadoResolucion();
            rr.pasos = new java.util.ArrayList<>();
            rr.resultado = nuevo;
            rr.latexFinal = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.toTeX(nuevo);
            rr.pasos.add(new PasoResolucion(rr.latexFinal));
            return rr;
        } else {
            NodoAST val = com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils
                    .evalDefinida(F, ii.var, ii.inf, ii.sup);
            NodoAST nuevo = com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils
                    .reemplazar(ii.nodoIntegral,
                            com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.cloneTree(val),
                            ii.padre, val);
            ResultadoResolucion rr = new ResultadoResolucion();
            rr.pasos = new java.util.ArrayList<>();
            rr.pasos.add(new PasoResolucion(
                    com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.toTeX(
                            com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils
                                    .sustituirVar(com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.cloneTree(F), ii.var, ii.sup))));
            rr.pasos.add(new PasoResolucion(
                    com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.toTeX(
                            com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils
                                    .sustituirVar(com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.cloneTree(F), ii.var, ii.inf))));
            rr.pasos.add(new PasoResolucion(com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.toTeX(val)));
            rr.resultado = nuevo;
            rr.latexFinal = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.toTeX(nuevo);
            rr.pasos.add(new PasoResolucion(rr.latexFinal));
            return rr;
        }
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
