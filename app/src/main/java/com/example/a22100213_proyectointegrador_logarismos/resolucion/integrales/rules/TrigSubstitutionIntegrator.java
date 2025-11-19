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

public class TrigSubstitutionIntegrator implements IntegratorRule {
    private final boolean definida;

    public TrigSubstitutionIntegrator(boolean definida) { this.definida = definida; }

    @Override
    public ResultadoResolucion apply(NodoAST raiz, ResultadoSemantico rs) {
        IntegralUtils.IntegralInfo ii = IntegralUtils.localizarIntegral(raiz, definida);
        if (ii == null || ii.cuerpo == null || ii.var == null) return null;

        ArrayList<PasoResolucion> pasos = new ArrayList<>();
        String encabezado = definida
                ? "\\int_{" + AstUtils.toTeX(ii.inf) + "}^{" + AstUtils.toTeX(ii.sup) + "} " + AstUtils.toTeX(ii.cuerpo) + "\\,d" + ii.var
                : "\\int " + AstUtils.toTeX(ii.cuerpo) + "\\,d" + ii.var;
        pasos.add(new PasoResolucion("Detección (sustitución trigonométrica)", encabezado));

        NodoAST preFold = IntegralUtils.foldEConstTimesXIntoExpLinear(ii.cuerpo, ii.var);
        if (preFold != ii.cuerpo) {
            pasos.add(new PasoResolucion("Normalización exponencial", AstUtils.toTeX(preFold)));
        }

        NodoAST cuerpo = IntegralUtils.normalizeSquaresForTrigSub(preFold, ii.var);
        pasos.add(new PasoResolucion("Normalización de cuadrados", AstUtils.toTeX(cuerpo)));

        if (cuerpo == null || cuerpo.token == null || cuerpo.token.type != LexToken.Type.RADICAL || cuerpo.hijos.size() != 1)
            return null;

        NodoAST inside = cuerpo.hijos.get(0);
        if (inside == null || inside.token == null) return null;

        boolean isSum = inside.token.type == LexToken.Type.SUM;
        boolean isSub = inside.token.type == LexToken.Type.SUB;
        if ((!isSum && !isSub) || inside.hijos.size() != 2) return null;

        NodoAST A = inside.hijos.get(0);
        NodoAST B = inside.hijos.get(1);
        Double cA = AstUtils.evalConst(A);
        Double cB = AstUtils.evalConst(B);

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
            formA2minusU2 = isSub && inside.hijos.get(0) == A;
            formU2minusA2 = isSub && inside.hijos.get(1) == B;
            formA2plusU2  = isSum && inside.hijos.get(0) == A;
        } else {
            return null;
        }

        if (quad == null) return null;
        NodoAST u = quad.hijos.get(0);

        Linear lin = linearDe(u, ii.var);
        if (!lin.ok) return null;
        double alpha = lin.a;
        double beta = lin.b;
        if (Math.abs(alpha) < 1e-15) return null;

        String uTex = AstUtils.toTeX(u);
        String aTex = AstUtils.toTeX(AstUtils.number(Math.sqrt(a2)));
        String linTex = AstUtils.toTeX(linearNode(alpha, beta, ii.var));

        if (formA2minusU2) {
            pasos.add(new PasoResolucion("Identificación de forma", "\\sqrt{a^2-u^2}\\quad\\text{con}\\; u=" + uTex + ",\\; a=" + aTex));
            pasos.add(new PasoResolucion("Sustitución propuesta", "u=a\\,\\sin\\theta\\;\\Rightarrow\\; du=a\\cos\\theta\\,d\\theta"));
            pasos.add(new PasoResolucion("Relación con x", uTex + "=" + linTex + ",\\; du=" + trimNum(alpha) + "\\,dx\\;\\Rightarrow\\; dx=\\tfrac{1}{" + trimNum(alpha) + "}du"));
            NodoAST F = construirFA2MinusU2(u, a2, alpha);
            return construirSalida(ii, F, pasos);
        }

        if (formA2plusU2) {
            pasos.add(new PasoResolucion("Identificación de forma", "\\sqrt{a^2+u^2}\\quad\\text{con}\\; u=" + uTex + ",\\; a=" + aTex));
            pasos.add(new PasoResolucion("Sustitución propuesta", "u=a\\,\\tan\\theta\\;\\Rightarrow\\; du=a\\sec^2\\theta\\,d\\theta"));
            pasos.add(new PasoResolucion("Relación con x", uTex + "=" + linTex + ",\\; du=" + trimNum(alpha) + "\\,dx\\;\\Rightarrow\\; dx=\\tfrac{1}{" + trimNum(alpha) + "}du"));
            NodoAST F = construirFA2PlusU2(u, a2, alpha);
            return construirSalida(ii, F, pasos);
        }

        if (formU2minusA2) {
            pasos.add(new PasoResolucion("Identificación de forma", "\\sqrt{u^2-a^2}\\quad\\text{con}\\; u=" + uTex + ",\\; a=" + aTex));
            pasos.add(new PasoResolucion("Sustitución propuesta", "u=a\\,\\sec\\theta\\;\\Rightarrow\\; du=a\\sec\\theta\\tan\\theta\\,d\\theta"));
            pasos.add(new PasoResolucion("Relación con x", uTex + "=" + linTex + ",\\; du=" + trimNum(alpha) + "\\,dx\\;\\Rightarrow\\; dx=\\tfrac{1}{" + trimNum(alpha) + "}du"));
            NodoAST F = construirFU2MinusA2(u, a2, alpha);
            return construirSalida(ii, F, pasos);
        }

        return null;
    }

    private NodoAST construirFA2MinusU2(NodoAST u, double a2, double alpha) {
        NodoAST a = AstUtils.number(Math.sqrt(a2));
        NodoAST a2Node = AstUtils.bin(LexToken.Type.EXP, AstUtils.cloneTree(a), AstUtils.number(2.0), "^", 7);
        NodoAST u2 = AstUtils.bin(LexToken.Type.EXP, AstUtils.cloneTree(u), AstUtils.number(2.0), "^", 7);
        NodoAST insideRad = AstUtils.bin(LexToken.Type.SUB, a2Node, u2, "-", 5);
        NodoAST rad = AstUtils.un(LexToken.Type.RADICAL, insideRad, "sqrt", 11);
        NodoAST term1 = AstUtils.bin(LexToken.Type.MUL, AstUtils.cloneTree(u), rad, "*", 6);
        NodoAST uDivA = AstUtils.bin(LexToken.Type.DIV, AstUtils.cloneTree(u), AstUtils.cloneTree(a), "/", 6);
        NodoAST asin = AstUtils.un(LexToken.Type.TRIG_ARCSIN, uDivA, "arcsin", 12);
        NodoAST a2asin = AstUtils.bin(LexToken.Type.MUL, a2Node, asin, "*", 6);
        NodoAST suma = AstUtils.bin(LexToken.Type.SUM, term1, a2asin, "+", 4);
        NodoAST factor = AstUtils.number(1.0 / (2.0 * alpha));
        return AstUtils.bin(LexToken.Type.MUL, factor, suma, "*", 6);
    }

    private NodoAST construirFA2PlusU2(NodoAST u, double a2, double alpha) {
        NodoAST a = AstUtils.number(Math.sqrt(a2));
        NodoAST a2Node = AstUtils.bin(LexToken.Type.EXP, AstUtils.cloneTree(a), AstUtils.number(2.0), "^", 7);
        NodoAST u2 = AstUtils.bin(LexToken.Type.EXP, AstUtils.cloneTree(u), AstUtils.number(2.0), "^", 7);
        NodoAST u2pa2 = AstUtils.bin(LexToken.Type.SUM, u2, a2Node, "+", 4);
        NodoAST rad = AstUtils.un(LexToken.Type.RADICAL, u2pa2, "sqrt", 11);
        NodoAST term1 = AstUtils.bin(LexToken.Type.MUL, AstUtils.cloneTree(u), rad, "*", 6);
        NodoAST lnArg = AstUtils.bin(LexToken.Type.SUM, AstUtils.cloneTree(u), AstUtils.cloneTree(rad), "+", 4);
        NodoAST abs = AstUtils.un(LexToken.Type.ABS, lnArg, "|.|", 11);
        NodoAST ln = AstUtils.un(LexToken.Type.LN, abs, "ln", 12);
        NodoAST a2ln = AstUtils.bin(LexToken.Type.MUL, a2Node, ln, "*", 6);
        NodoAST suma = AstUtils.bin(LexToken.Type.SUM, term1, a2ln, "+", 4);
        NodoAST factor = AstUtils.number(1.0 / (2.0 * alpha));
        return AstUtils.bin(LexToken.Type.MUL, factor, suma, "*", 6);
    }

    private NodoAST construirFU2MinusA2(NodoAST u, double a2, double alpha) {
        NodoAST a = AstUtils.number(Math.sqrt(a2));
        NodoAST a2Node = AstUtils.bin(LexToken.Type.EXP, AstUtils.cloneTree(a), AstUtils.number(2.0), "^", 7);
        NodoAST u2 = AstUtils.bin(LexToken.Type.EXP, AstUtils.cloneTree(u), AstUtils.number(2.0), "^", 7);
        NodoAST u2ma2 = AstUtils.bin(LexToken.Type.SUB, u2, a2Node, "-", 5);
        NodoAST rad = AstUtils.un(LexToken.Type.RADICAL, u2ma2, "sqrt", 11);
        NodoAST term1 = AstUtils.bin(LexToken.Type.MUL, AstUtils.cloneTree(u), rad, "*", 6);
        NodoAST lnArg = AstUtils.bin(LexToken.Type.SUM, AstUtils.cloneTree(u), AstUtils.cloneTree(rad), "+", 4);
        NodoAST abs = AstUtils.un(LexToken.Type.ABS, lnArg, "|.|", 11);
        NodoAST ln = AstUtils.un(LexToken.Type.LN, abs, "ln", 12);
        NodoAST a2ln = AstUtils.bin(LexToken.Type.MUL, a2Node, ln, "*", 6);
        NodoAST resta = AstUtils.bin(LexToken.Type.SUB, term1, a2ln, "-", 5);
        NodoAST factor = AstUtils.number(1.0 / (2.0 * alpha));
        return AstUtils.bin(LexToken.Type.MUL, factor, resta, "*", 6);
    }

    private ResultadoResolucion construirSalida(IntegralUtils.IntegralInfo ii, NodoAST F, ArrayList<PasoResolucion> pasos) {
        ResultadoResolucion rr = new ResultadoResolucion();
        rr.pasos = pasos;

        if (!definida) {
            pasos.add(new PasoResolucion("Antiderivada cerrada en x", AstUtils.toTeX(F)));
            NodoAST fin = IntegralUtils.addC(AstUtils.cloneTree(F));
            pasos.add(new PasoResolucion("Constante de integración", AstUtils.toTeX(fin)));
            NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, AstUtils.cloneTree(fin), ii.padre, fin);
            rr.resultado = nuevo;
            rr.latexFinal = AstUtils.toTeX(nuevo);
            rr.pasos.add(new PasoResolucion(rr.latexFinal));
            return rr;
        } else {
            pasos.add(new PasoResolucion("Antiderivada cerrada en x", AstUtils.toTeX(F)));
            NodoAST supEval = IntegralUtils.sustituirVar(AstUtils.cloneTree(F), ii.var, ii.sup);
            NodoAST infEval = IntegralUtils.sustituirVar(AstUtils.cloneTree(F), ii.var, ii.inf);
            NodoAST val = IntegralUtils.evalDefinida(AstUtils.cloneTree(F), ii.var, ii.inf, ii.sup);
            pasos.add(new PasoResolucion("F(b)", AstUtils.toTeX(supEval)));
            pasos.add(new PasoResolucion("F(a)", AstUtils.toTeX(infEval)));
            pasos.add(new PasoResolucion("F(b)-F(a)", AstUtils.toTeX(val)));
            NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, AstUtils.cloneTree(val), ii.padre, val);
            rr.resultado = nuevo;
            rr.latexFinal = AstUtils.toTeX(nuevo);
            rr.pasos.add(new PasoResolucion(rr.latexFinal));
            return rr;
        }
    }

    private boolean isSquareOfLinear(NodoAST n, String var) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.EXP && n.hijos.size() == 2) {
            Double e = AstUtils.evalConst(n.hijos.get(1));
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
        if (a == null || Math.abs(a) < 1e-15) return new Linear(false, 0, 0);
        Double b = AstUtils.evalConst(IntegralUtils.sustituirVar(AstUtils.cloneTree(u), var, AstUtils.number(0.0)));
        if (b == null) return new Linear(false, 0, 0);
        return new Linear(true, a, b);
    }

    private static NodoAST linearNode(double a, double b, String v) {
        NodoAST ax = AstUtils.bin(LexToken.Type.MUL, AstUtils.number(a), AstUtils.atom(LexToken.Type.VARIABLE, v, 1), "*", 6);
        return AstUtils.bin(LexToken.Type.SUM, ax, AstUtils.number(b), "+", 5);
    }

    private static String trimNum(double v) {
        String s = Double.toString(v);
        if (s.endsWith(".0")) s = s.substring(0, s.length() - 2);
        return s;
    }
}
