package com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.rules;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.SymjaBridge;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegratorRule;

public class TrigSubstitutionIntegrator implements IntegratorRule {
    private final boolean definida;

    public TrigSubstitutionIntegrator(boolean definida) { this.definida = definida; }

    @Override
    public ResultadoResolucion apply(NodoAST raiz, ResultadoSemantico rs) {
        ResultadoResolucion rr = new ResultadoResolucion();
        IntegralUtils.IntegralInfo ii = IntegralUtils.localizarIntegral(raiz, definida);
        if (ii == null || ii.cuerpo == null) return new PowerRuleIntegrator(definida).apply(raiz, rs);

        String var = ii.var == null ? "x" : ii.var;
        NodoAST f = ii.cuerpo;

        NodoAST radical = null;
        double factorExterior = 1.0;

        if (f.token != null && f.token.type == LexToken.Type.RADICAL) {
            radical = f;
        } else if (f.token != null && f.token.type == LexToken.Type.MUL && f.hijos.size() == 2) {
            Double kL = evalConst(f.hijos.get(0));
            Double kR = evalConst(f.hijos.get(1));
            if (kL != null && isRadical(f.hijos.get(1))) { factorExterior = kL; radical = f.hijos.get(1); }
            else if (kR != null && isRadical(f.hijos.get(0))) { factorExterior = kR; radical = f.hijos.get(0); }
        }

        if (!isRadical(radical)) {
            return new PowerRuleIntegrator(definida).apply(raiz, rs);
        }

        NodoAST inside = radical.hijos.get(0);
        if (inside == null || inside.token == null) return new PowerRuleIntegrator(definida).apply(raiz, rs);

        int mode = 0;
        NodoAST uBase = null;
        NodoAST a2Node = null;

        if ((inside.token.type == LexToken.Type.SUM || inside.token.type == LexToken.Type.SUB) && inside.hijos.size() == 2) {
            NodoAST A = inside.hijos.get(0);
            NodoAST B = inside.hijos.get(1);

            NodoAST sq = isSquare(B) ? B : (isSquare(A) ? A : null);
            NodoAST other = (sq == B) ? A : (sq == A ? B : null);

            if (sq != null && other != null) {
                uBase = sq.hijos.get(0);
                a2Node = other;

                if (inside.token.type == LexToken.Type.SUM) {
                    mode = 2;
                } else {
                    mode = (sq == B) ? 1 : 3;
                }
            }
        }

        if (mode == 0 || uBase == null) {
            return new PowerRuleIntegrator(definida).apply(raiz, rs);
        }


        Linear lin = linearDe(uBase, var);
        if (!lin.ok) {

            return new PowerRuleIntegrator(definida).apply(raiz, rs);
        }

        String U = "(" + AstUtils.toSymja(uBase) + ")";
        String A = "(" + AstUtils.toSymja(a2Node) + ")";
        String SqrtA = "Sqrt[" + A + "]";
        String sqrtRad;
        String F;

        String prefactor = "(" + factorExterior + ")/(2*(" + lin.b + "))";

        switch (mode) {
            case 1:
                sqrtRad = "Sqrt[" + A + " - " + U + "^2]";
                F = prefactor + " * (" + U + "*" + sqrtRad + " + " + A + "*ArcSin[" + U + "/" + SqrtA + "])";
                rr.pasos.add(new PasoResolucion("\\text{Sea } u=" + escaparTeX(U) + ",\\; du=" + lin.b + "\\,dx"));
                rr.pasos.add(new PasoResolucion("\\int \\sqrt{a^{2}-u^{2}}\\,du=\\tfrac12\\left(u\\,\\sqrt{a^{2}-u^{2}}+a^{2}\\arcsin\\tfrac{u}{a}\\right)"));
                break;

            case 2:
                sqrtRad = "Sqrt[" + A + " + " + U + "^2]";
                F = prefactor + " * (" + U + "*" + sqrtRad + " + " + A + "*ArcSinh[" + U + "/" + SqrtA + "])";
                rr.pasos.add(new PasoResolucion("\\text{Sea } u=" + escaparTeX(U) + ",\\; du=" + lin.b + "\\,dx"));
                rr.pasos.add(new PasoResolucion("\\int \\sqrt{a^{2}+u^{2}}\\,du=\\tfrac12\\left(u\\,\\sqrt{a^{2}+u^{2}}+a^{2}\\operatorname{arsinh}\\tfrac{u}{a}\\right)"));
                break;

            case 3:
                sqrtRad = "Sqrt[" + U + "^2 - " + A + "]";
                F = prefactor + " * (" + U + "*" + sqrtRad + " - " + A + "*Log[Abs[" + U + " + " + sqrtRad + "]])";
                rr.pasos.add(new PasoResolucion("\\text{Sea } u=" + escaparTeX(U) + ",\\; du=" + lin.b + "\\,dx"));
                rr.pasos.add(new PasoResolucion("\\int \\sqrt{u^{2}-a^{2}}\\,du=\\tfrac12\\left(u\\,\\sqrt{u^{2}-a^{2}}-a^{2}\\ln\\left|u+\\sqrt{u^{2}-a^{2}}\\right|\\right)"));
                break;

            default:
                return new PowerRuleIntegrator(definida).apply(raiz, rs);
        }

        rr.resultado = raiz;
        rr.latexFinal = SymjaBridge.toTeX(F);
        rr.pasos.add(new PasoResolucion("\\Rightarrow " + rr.latexFinal));
        return rr;
    }

    private static boolean isRadical(NodoAST n) {
        return n != null && n.token != null && n.token.type == LexToken.Type.RADICAL
                && n.hijos.size() == 1;
    }
    private static boolean isSquare(NodoAST n) {
        if (n == null || n.token == null || n.token.type != LexToken.Type.EXP || n.hijos.size() != 2) return false;
        Double e = evalConst(n.hijos.get(1));
        return e != null && Math.abs(e - 2.0) < 1e-9;
    }
    private static Double evalConst(NodoAST n) {
        if (n == null || n.token == null) return null;
        switch (n.token.type) {
            case INTEGER:
            case DECIMAL:
                try { return Double.valueOf(n.token.value); } catch (Exception ignore) { return null; }
            case CONST_E: return Math.E;
            case CONST_PI: return Math.PI;
            case SUM:
            case MUL: {
                double acc = (n.token.type == LexToken.Type.SUM) ? 0.0 : 1.0;
                for (NodoAST h : n.hijos) {
                    Double v = evalConst(h);
                    if (v == null) return null;
                    acc = (n.token.type == LexToken.Type.SUM) ? acc + v : acc * v;
                }
                return acc;
            }
            case SUB: {
                if (n.hijos.size() != 2) return null;
                Double a = evalConst(n.hijos.get(0));
                Double b = evalConst(n.hijos.get(1));
                return (a == null || b == null) ? null : (a - b);
            }
            case DIV: {
                if (n.hijos.size() != 2) return null;
                Double a = evalConst(n.hijos.get(0));
                Double b = evalConst(n.hijos.get(1));
                return (a == null || b == null || Math.abs(b) < 1e-15) ? null : (a / b);
            }
            case EXP: {
                if (n.hijos.size() != 2) return null;
                Double a = evalConst(n.hijos.get(0));
                Double b = evalConst(n.hijos.get(1));
                return (a == null || b == null) ? null : Math.pow(a, b);
            }
            default: return null;
        }
    }

    private static class Linear {
        final boolean ok; final double b;
        Linear(boolean ok, double b) { this.ok = ok; this.b = b; }
    }

    private static Linear linearDe(NodoAST u, String var) {
        if (u == null || u.token == null) return new Linear(false, 0);
        // x
        if (u.token.type == LexToken.Type.VARIABLE && var.equals(u.token.value) && u.hijos.isEmpty())
            return new Linear(true, 1.0);

        if (u.token.type == LexToken.Type.MUL && u.hijos.size() == 2) {
            Double kL = evalConst(u.hijos.get(0));
            Double kR = evalConst(u.hijos.get(1));
            if (kL != null && isVar(u.hijos.get(1), var)) return new Linear(true, kL);
            if (kR != null && isVar(u.hijos.get(0), var)) return new Linear(true, kR);
        }

        if ((u.token.type == LexToken.Type.SUM || u.token.type == LexToken.Type.SUB) && u.hijos.size() == 2) {
            NodoAST L = u.hijos.get(0), R = u.hijos.get(1);
            Double cL = evalConst(L), cR = evalConst(R);
            Linear lL = linearDe(L, var), lR = linearDe(R, var);

            if (lL.ok && cR != null) return new Linear(true,  lL.b);
            if (cL != null && lR.ok) return new Linear(true, (u.token.type == LexToken.Type.SUM) ? lR.b : -lR.b);
        }

        return new Linear(false, 0);
    }
    private static boolean isVar(NodoAST n, String var) {
        return n != null && n.token != null && n.token.type == LexToken.Type.VARIABLE
                && var.equals(n.token.value) && n.hijos.isEmpty();
    }

    private static String escaparTeX(String symjaExpr) {
        return symjaExpr.replace("*", "\\cdot ").replace("[","(").replace("]",")");
    }
}
