package com.example.a22100213_proyectointegrador_logarismos.resolucion.t7nolineal;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.MetodoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import java.util.LinkedHashSet;
import java.util.Set;

final class NoLinealEspecialResolver {
    private NoLinealEspecialResolver() {}

    static ResultadoResolucion resolve(NodoAST raiz, ResultadoSemantico rs, MetodoResolucion metodo) {
        switch (metodo) {
            case ECUACION_EXPONENCIAL: return resolverExponencial(raiz);
            case ECUACION_LOGARITMICA: return resolverLogaritmica(raiz);
            case ECUACION_POTENCIA: return resolverPotenciaRadical(raiz);
            case ECUACION_VALOR_ABSOLUTO: return resolverValorAbsoluto(raiz);
            case ECUACION_TRIG_SIN: return resolverTrigSin(raiz);
            case ECUACION_TRIG_COS: return resolverTrigCos(raiz);
            case ECUACION_TRIG_TAN: return resolverTrigTan(raiz);
            case ECUACION_ARC_TRIG: return resolverArcTrig(raiz);
            case ECUACION_RACIONAL_LINEAL: return resolverRacionalLineal(raiz);
            case ECUACION_RECIPROCA_LINEAL: return resolverReciprocaLineal(raiz);
            default: return placeholder(raiz, "No lineal");
        }
    }

    private static ResultadoResolucion resolverExponencial(NodoAST eq) {
        ResultadoResolucion rr = base(eq, "Ecuación exponencial");
        String var = unicaVariable(eq);
        NodoAST L = eq.hijos.get(0), R = eq.hijos.get(1);
        Double cL = evaluarConstante(L), cR = evaluarConstante(R);
        if (isExpENode(L) && cR != null) {
            double k = cR;
            if (k <= 0) return sinSolucion(rr);
            rr.pasos.add(new PasoResolucion("\\ln\\left(" + AstUtils.toTeX(L) + "\\right)=" + num(Math.log(k))));
            return solveLinear(L.hijos.get(1), Math.log(k), var, rr);
        }
        if (isExpENode(R) && cL != null) {
            double k = cL;
            if (k <= 0) return sinSolucion(rr);
            rr.pasos.add(new PasoResolucion("\\ln\\left(" + AstUtils.toTeX(R) + "\\right)=" + num(Math.log(k))));
            return solveLinear(R.hijos.get(1), Math.log(k), var, rr);
        }
        return rr;
    }

    private static ResultadoResolucion resolverLogaritmica(NodoAST eq) {
        ResultadoResolucion rr = base(eq, "Ecuación logarítmica");
        String var = unicaVariable(eq);
        NodoAST L = eq.hijos.get(0), R = eq.hijos.get(1);
        Double cL = evaluarConstante(L), cR = evaluarConstante(R);
        if (isLnNode(L) && cR != null) {
            rr.pasos.add(new PasoResolucion("\\mathrm{e}^{" + AstUtils.toTeX(L) + "}=" + num(Math.exp(cR))));
            return solveLinear(L.hijos.get(0), Math.exp(cR), var, rr, true);
        }
        if (isLnNode(R) && cL != null) {
            rr.pasos.add(new PasoResolucion("\\mathrm{e}^{" + AstUtils.toTeX(R) + "}=" + num(Math.exp(cL))));
            return solveLinear(R.hijos.get(0), Math.exp(cL), var, rr, true);
        }
        if (isLogAnyNode(L) && cR != null) {
            double base = logBase(L);
            double val = Math.pow(base, cR);
            rr.pasos.add(new PasoResolucion(num(base) + "^{" + AstUtils.toTeX(L) + "}=" + num(val)));
            return solveLinear(L.hijos.get(0), val, var, rr, true);
        }
        if (isLogAnyNode(R) && cL != null) {
            double base = logBase(R);
            double val = Math.pow(base, cL);
            rr.pasos.add(new PasoResolucion(num(base) + "^{" + AstUtils.toTeX(R) + "}=" + num(val)));
            return solveLinear(R.hijos.get(0), val, var, rr, true);
        }
        return rr;
    }

    private static ResultadoResolucion resolverPotenciaRadical(NodoAST eq) {
        ResultadoResolucion rr = base(eq, "Ecuación de potencia/radical");
        String var = unicaVariable(eq);
        NodoAST L = eq.hijos.get(0), R = eq.hijos.get(1);
        Double cL = evaluarConstante(L), cR = evaluarConstante(R);
        if (isRadicalNode(L) && cR != null) {
            double k = cR;
            rr.pasos.add(new PasoResolucion("(" + AstUtils.toTeX(L) + ")^2=" + num(k * k)));
            return solveLinear(L.hijos.get(0), k * k, var, rr, true);
        }
        if (isRadicalNode(R) && cL != null) {
            double k = cL;
            rr.pasos.add(new PasoResolucion("(" + AstUtils.toTeX(R) + ")^2=" + num(k * k)));
            return solveLinear(R.hijos.get(0), k * k, var, rr, true);
        }
        if (isPowerOfLinear(L) && cR != null) {
            double p = evaluarConstante(L.hijos.get(1));
            if (isInteger(p) && ((int)Math.rint(p)) % 2 == 0) {
                double r = rootSafe(cR, p);
                rr.pasos.add(new PasoResolucion(AstUtils.toTeX(L.hijos.get(0)) + "=" + "\\pm" + num(r)));
                ResultadoResolucion rr1 = solveLinear(L.hijos.get(0), r, var, cloneBase(rr));
                ResultadoResolucion rr2 = solveLinear(L.hijos.get(0), -r, var, cloneBase(rr));
                return merge(rr, rr1, rr2);
            } else {
                double r = rootSafe(cR, p);
                rr.pasos.add(new PasoResolucion(AstUtils.toTeX(L.hijos.get(0)) + "=" + num(r)));
                return solveLinear(L.hijos.get(0), r, var, rr);
            }
        }
        if (isPowerOfLinear(R) && cL != null) {
            double p = evaluarConstante(R.hijos.get(1));
            if (isInteger(p) && ((int)Math.rint(p)) % 2 == 0) {
                double r = rootSafe(cL, p);
                rr.pasos.add(new PasoResolucion(AstUtils.toTeX(R.hijos.get(0)) + "=" + "\\pm" + num(r)));
                ResultadoResolucion rr1 = solveLinear(R.hijos.get(0), r, var, cloneBase(rr));
                ResultadoResolucion rr2 = solveLinear(R.hijos.get(0), -r, var, cloneBase(rr));
                return merge(rr, rr1, rr2);
            } else {
                double r = rootSafe(cL, p);
                rr.pasos.add(new PasoResolucion(AstUtils.toTeX(R.hijos.get(0)) + "=" + num(r)));
                return solveLinear(R.hijos.get(0), r, var, rr);
            }
        }
        return rr;
    }

    private static ResultadoResolucion resolverValorAbsoluto(NodoAST eq) {
        ResultadoResolucion rr = base(eq, "Ecuación con valor absoluto");
        String var = unicaVariable(eq);
        NodoAST L = eq.hijos.get(0), R = eq.hijos.get(1);
        Double cL = evaluarConstante(L), cR = evaluarConstante(R);
        if (isAbsNode(L) && cR != null) {
            double k = cR;
            if (k < 0) return sinSolucion(rr);
            ResultadoResolucion rr1 = solveLinear(L.hijos.get(0), k, var, cloneBase(rr));
            ResultadoResolucion rr2 = solveLinear(L.hijos.get(0), -k, var, cloneBase(rr));
            return merge(rr, rr1, rr2);
        }
        if (isAbsNode(R) && cL != null) {
            double k = cL;
            if (k < 0) return sinSolucion(rr);
            ResultadoResolucion rr1 = solveLinear(R.hijos.get(0), k, var, cloneBase(rr));
            ResultadoResolucion rr2 = solveLinear(R.hijos.get(0), -k, var, cloneBase(rr));
            return merge(rr, rr1, rr2);
        }
        return rr;
    }

    private static ResultadoResolucion resolverTrigSin(NodoAST eq) {
        ResultadoResolucion rr = base(eq, "Ecuación trigonométrica seno");
        String var = unicaVariable(eq);
        NodoAST L = eq.hijos.get(0), R = eq.hijos.get(1);
        Double cL = evaluarConstante(L), cR = evaluarConstante(R);
        if (isSinNode(L) && cR != null && Math.abs(cR) <= 1 + 1e-12) {
            double r = Math.asin(clamp(cR));
            String u = AstUtils.toTeX(L.hijos.get(0));
            rr.pasos.add(new PasoResolucion(u + "=" + num(r) + "+2\\pi n,\\;n\\in\\mathbb{Z}"));
            rr.pasos.add(new PasoResolucion(u + "=" + num(Math.PI - r) + "+2\\pi n,\\;n\\in\\mathbb{Z}"));
            return solveLinearFamilies(L.hijos.get(0), new double[]{r, Math.PI - r}, new double[]{2*Math.PI, 2*Math.PI}, var, rr);
        }
        if (isSinNode(R) && cL != null && Math.abs(cL) <= 1 + 1e-12) {
            double r = Math.asin(clamp(cL));
            String u = AstUtils.toTeX(R.hijos.get(0));
            rr.pasos.add(new PasoResolucion(u + "=" + num(r) + "+2\\pi n,\\;n\\in\\mathbb{Z}"));
            rr.pasos.add(new PasoResolucion(u + "=" + num(Math.PI - r) + "+2\\pi n,\\;n\\in\\mathbb{Z}"));
            return solveLinearFamilies(R.hijos.get(0), new double[]{r, Math.PI - r}, new double[]{2*Math.PI, 2*Math.PI}, var, rr);
        }
        return rr;
    }

    private static ResultadoResolucion resolverTrigCos(NodoAST eq) {
        ResultadoResolucion rr = base(eq, "Ecuación trigonométrica coseno");
        String var = unicaVariable(eq);
        NodoAST L = eq.hijos.get(0), R = eq.hijos.get(1);
        Double cL = evaluarConstante(L), cR = evaluarConstante(R);
        if (isCosNode(L) && cR != null && Math.abs(cR) <= 1 + 1e-12) {
            double r = Math.acos(clamp(cR));
            String u = AstUtils.toTeX(L.hijos.get(0));
            rr.pasos.add(new PasoResolucion(u + "=" + num(r) + "+2\\pi n,\\;n\\in\\mathbb{Z}"));
            rr.pasos.add(new PasoResolucion(u + "=-" + num(r) + "+2\\pi n,\\;n\\in\\mathbb{Z}"));
            return solveLinearFamilies(L.hijos.get(0), new double[]{ r, -r }, new double[]{2*Math.PI, 2*Math.PI}, var, rr);
        }
        if (isCosNode(R) && cL != null && Math.abs(cL) <= 1 + 1e-12) {
            double r = Math.acos(clamp(cL));
            String u = AstUtils.toTeX(R.hijos.get(0));
            rr.pasos.add(new PasoResolucion(u + "=" + num(r) + "+2\\pi n,\\;n\\in\\mathbb{Z}"));
            rr.pasos.add(new PasoResolucion(u + "=-" + num(r) + "+2\\pi n,\\;n\\in\\mathbb{Z}"));
            return solveLinearFamilies(R.hijos.get(0), new double[]{ r, -r }, new double[]{2*Math.PI, 2*Math.PI}, var, rr);
        }
        return rr;
    }

    private static ResultadoResolucion resolverTrigTan(NodoAST eq) {
        ResultadoResolucion rr = base(eq, "Ecuación trigonométrica tangente");
        String var = unicaVariable(eq);
        NodoAST L = eq.hijos.get(0), R = eq.hijos.get(1);
        Double cL = evaluarConstante(L), cR = evaluarConstante(R);
        if (isTanNode(L) && cR != null) {
            double r = Math.atan(cR);
            String u = AstUtils.toTeX(L.hijos.get(0));
            rr.pasos.add(new PasoResolucion(u + "=" + num(r) + "+\\pi n,\\;n\\in\\mathbb{Z}"));
            return solveLinearFamilies(L.hijos.get(0), new double[]{ r }, new double[]{ Math.PI }, var, rr);
        }
        if (isTanNode(R) && cL != null) {
            double r = Math.atan(cL);
            String u = AstUtils.toTeX(R.hijos.get(0));
            rr.pasos.add(new PasoResolucion(u + "=" + num(r) + "+\\pi n,\\;n\\in\\mathbb{Z}"));
            return solveLinearFamilies(R.hijos.get(0), new double[]{ r }, new double[]{ Math.PI }, var, rr);
        }
        return rr;
    }

    private static ResultadoResolucion resolverArcTrig(NodoAST eq) {
        ResultadoResolucion rr = base(eq, "Ecuación arcotrigonométrica");
        String var = unicaVariable(eq);
        NodoAST L = eq.hijos.get(0), R = eq.hijos.get(1);
        Double cL = evaluarConstante(L), cR = evaluarConstante(R);
        if (isArcTrigNode(L) && cR != null) {
            double v = arcTrigInvertValue(L.token.type, cR);
            return solveLinear(L.hijos.get(0), v, var, rr);
        }
        if (isArcTrigNode(R) && cL != null) {
            double v = arcTrigInvertValue(R.token.type, cL);
            return solveLinear(R.hijos.get(0), v, var, rr);
        }
        return rr;
    }

    private static ResultadoResolucion resolverRacionalLineal(NodoAST eq) {
        ResultadoResolucion rr = base(eq, "Ecuación racional lineal");
        String var = unicaVariable(eq);
        NodoAST L = eq.hijos.get(0), R = eq.hijos.get(1);
        Double kL = evaluarConstante(L), kR = evaluarConstante(R);
        if (isFrac(L) && kR != null) {
            double[] numAB = linearAB(L.hijos.get(0), var);
            double[] denAB = linearAB(L.hijos.get(1), var);
            if (numAB == null || denAB == null) return rr;
            double a = numAB[0], b = numAB[1], c = denAB[0], d = denAB[1], k = kR;
            double denx = a - k * c;
            if (Math.abs(denx) < 1e-15) return sinSolucion(rr);
            double x = (k * d - b) / denx;
            rr.pasos.add(new PasoResolucion("x=" + num(x)));
            rr.latexFinal = "x=" + num(x);
            rr.resultado = eq;
            return rr;
        }
        if (isFrac(R) && kL != null) {
            double[] numAB = linearAB(R.hijos.get(0), var);
            double[] denAB = linearAB(R.hijos.get(1), var);
            if (numAB == null || denAB == null) return rr;
            double a = numAB[0], b = numAB[1], c = denAB[0], d = denAB[1], k = kL;
            double denx = a - k * c;
            if (Math.abs(denx) < 1e-15) return sinSolucion(rr);
            double x = (k * d - b) / denx;
            rr.pasos.add(new PasoResolucion("x=" + num(x)));
            rr.latexFinal = "x=" + num(x);
            rr.resultado = eq;
            return rr;
        }
        return rr;
    }

    private static ResultadoResolucion resolverReciprocaLineal(NodoAST eq) {
        ResultadoResolucion rr = base(eq, "Ecuación recíproca lineal");
        String var = unicaVariable(eq);
        NodoAST L = eq.hijos.get(0), R = eq.hijos.get(1);
        Double kL = evaluarConstante(L), kR = evaluarConstante(R);
        if (isReciprocoDeLineal(L, var) && kR != null && Math.abs(kR) > 1e-15) {
            double v = 1.0 / kR;
            return solveLinear(L.hijos.get(1), v, var, rr);
        }
        if (isReciprocoDeLineal(R, var) && kL != null && Math.abs(kL) > 1e-15) {
            double v = 1.0 / kL;
            return solveLinear(R.hijos.get(1), v, var, rr);
        }
        return rr;
    }

    private static ResultadoResolucion solveLinear(NodoAST u, double K, String var, ResultadoResolucion rr) {
        return solveLinear(u, K, var, rr, false);
    }

    private static ResultadoResolucion solveLinear(NodoAST u, double K, String var, ResultadoResolucion rr, boolean domainNote) {
        double[] ab = linearAB(u, var);
        if (ab == null) return rr;
        double a = ab[0], b = ab[1];
        if (Math.abs(a) < 1e-15) return rr;
        double x = (K - b) / a;
        if (domainNote) rr.pasos.add(new PasoResolucion("\\text{Dominios válidos considerados}"));
        rr.pasos.add(new PasoResolucion(AstUtils.toTeX(u) + "=" + num(K)));
        rr.pasos.add(new PasoResolucion("x=" + num(x)));
        rr.latexFinal = "x=" + num(x);
        rr.resultado = u;
        return rr;
    }

    private static ResultadoResolucion solveLinearFamilies(NodoAST u, double[] r, double[] period, String var, ResultadoResolucion rr) {
        double[] ab = linearAB(u, var);
        if (ab == null) return rr;
        double a = ab[0], b = ab[1];
        if (Math.abs(a) < 1e-15) return rr;
        StringBuilder sol = new StringBuilder();
        for (int i = 0; i < r.length; i++) {
            String branch = "x=\\frac{" + num(r[i]) + "+(" + num(period[i]) + ")n-" + num(b) + "}{" + num(a) + "},\\;n\\in\\mathbb{Z}";
            rr.pasos.add(new PasoResolucion(branch));
            if (i > 0) sol.append("\\;\\text{o}\\; ");
            sol.append(branch);
        }
        rr.latexFinal = sol.toString();
        rr.resultado = u;
        return rr;
    }

    private static boolean isInteger(double v) {
        return Math.abs(v - Math.rint(v)) < 1e-12;
    }

    private static double rootSafe(double k, double p) {
        if (p == 0) return Double.NaN;
        return Math.copySign(Math.pow(Math.abs(k), 1.0 / p), k >= 0 ? 1.0 : (isInteger(p) && ((int)Math.rint(p)) % 2 == 1 ? -1.0 : 1.0));
    }

    private static double clamp(double v) {
        if (v > 1) return 1;
        if (v < -1) return -1;
        return v;
    }

    private static ResultadoResolucion placeholder(NodoAST raiz, String titulo) {
        ResultadoResolucion rr = new ResultadoResolucion();
        NodoAST eq = buscarNodo(raiz, LexToken.Type.EQUAL);
        String tex = eq != null ? AstUtils.toTeX(eq) : AstUtils.toTeX(raiz);
        rr.pasos.add(new PasoResolucion(tex));
        rr.pasos.add(new PasoResolucion("\\text{" + titulo + "}"));
        rr.resultado = eq != null ? eq : raiz;
        rr.latexFinal = tex;
        return rr;
    }

    private static ResultadoResolucion base(NodoAST eq, String titulo) {
        ResultadoResolucion rr = new ResultadoResolucion();
        rr.pasos.add(new PasoResolucion(AstUtils.toTeX(eq)));
        rr.pasos.add(new PasoResolucion("\\text{" + titulo + "}"));
        rr.resultado = eq;
        rr.latexFinal = AstUtils.toTeX(eq);
        return rr;
    }

    private static ResultadoResolucion cloneBase(ResultadoResolucion rr) {
        ResultadoResolucion c = new ResultadoResolucion();
        c.pasos.addAll(rr.pasos);
        c.resultado = rr.resultado;
        c.latexFinal = rr.latexFinal;
        return c;
    }

    private static ResultadoResolucion merge(ResultadoResolucion base, ResultadoResolucion... ramas) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ramas.length; i++) {
            if (ramas[i] == null) continue;
            base.pasos.addAll(ramas[i].pasos);
            if (i > 0) sb.append("\\;\\text{o}\\; ");
            sb.append(ramas[i].latexFinal);
        }
        base.latexFinal = sb.length() == 0 ? base.latexFinal : sb.toString();
        return base;
    }

    private static ResultadoResolucion sinSolucion(ResultadoResolucion rr) {
        rr.pasos.add(new PasoResolucion("\\text{Sin solución real}"));
        rr.latexFinal = "\\text{Sin solución real}";
        return rr;
    }

    private static boolean isExpENode(NodoAST n) {
        return n != null && n.token != null && n.token.type == LexToken.Type.EXP && n.hijos.size() == 2
                && n.hijos.get(0) != null && n.hijos.get(0).token != null && n.hijos.get(0).token.type == LexToken.Type.CONST_E;
    }
    private static boolean isLnNode(NodoAST n) {
        return n != null && n.token != null && n.token.type == LexToken.Type.LN && n.hijos.size() == 1;
    }
    private static boolean isLogAnyNode(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (n.hijos.size() != 1) return false;
        LexToken.Type t = n.token.type;
        return t == LexToken.Type.LOG || t == LexToken.Type.LOG_BASE2 || t == LexToken.Type.LOG_BASE10;
    }
    private static double logBase(NodoAST n) {
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.LOG_BASE2) return 2.0;
        if (t == LexToken.Type.LOG_BASE10) return 10.0;
        return Math.E;
    }
    private static boolean isRadicalNode(NodoAST n) {
        return n != null && n.token != null && n.token.type == LexToken.Type.RADICAL && n.hijos.size() == 1;
    }
    private static boolean isPowerOfLinear(NodoAST n) {
        if (n == null || n.token == null || n.token.type != LexToken.Type.EXP || n.hijos.size() != 2) return false;
        Double e = evaluarConstante(n.hijos.get(1));
        if (e == null) return false;
        return linearAB(n.hijos.get(0), unicaVariable(n)) != null;
    }
    private static boolean isAbsNode(NodoAST n) {
        return n != null && n.token != null && n.token.type == LexToken.Type.ABS && n.hijos.size() == 1;
    }
    private static boolean isSinNode(NodoAST n) {
        return n != null && n.token != null && n.token.type == LexToken.Type.TRIG_SIN && n.hijos.size() == 1;
    }
    private static boolean isCosNode(NodoAST n) {
        return n != null && n.token != null && n.token.type == LexToken.Type.TRIG_COS && n.hijos.size() == 1;
    }
    private static boolean isTanNode(NodoAST n) {
        return n != null && n.token != null && n.token.type == LexToken.Type.TRIG_TAN && n.hijos.size() == 1;
    }
    private static boolean isArcTrigNode(NodoAST n) {
        if (n == null || n.token == null || n.hijos.size() != 1) return false;
        switch (n.token.type) {
            case TRIG_ARCSIN:
            case TRIG_ARCCOS:
            case TRIG_ARCTAN:
            case TRIG_ARCCOT:
            case TRIG_ARCSEC:
            case TRIG_ARCCSC:
                return true;
            default:
                return false;
        }
    }
    private static boolean isFrac(NodoAST n) {
        return n != null && n.token != null && n.token.type == LexToken.Type.DIV && n.hijos.size() == 2;
    }
    private static boolean isReciprocoDeLineal(NodoAST n, String var) {
        if (!isFrac(n)) return false;
        Double num = evaluarConstante(n.hijos.get(0));
        if (num == null) return false;
        return linearAB(n.hijos.get(1), var) != null;
    }

    private static double arcTrigInvertValue(LexToken.Type t, double k) {
        switch (t) {
            case TRIG_ARCSIN: return Math.sin(k);
            case TRIG_ARCCOS: return Math.cos(k);
            case TRIG_ARCTAN: return Math.tan(k);
            case TRIG_ARCCOT: return Math.tan(Math.PI/2 - k);
            case TRIG_ARCSEC: return 1.0/Math.cos(k);
            case TRIG_ARCCSC: return 1.0/Math.sin(k);
            default: return k;
        }
    }

    private static double[] linearAB(NodoAST n, String var) {
        if (n == null) return null;
        if (n.token == null) return new double[]{0,0};
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.VARIABLE && var.equals(n.token.value) && n.hijos.isEmpty()) return new double[]{1,0};
        Double c = evaluarConstante(n);
        if (c != null) return new double[]{0,c};
        if ((t == LexToken.Type.SUM || t == LexToken.Type.SUB) && n.hijos.size() == 2) {
            double[] a = linearAB(n.hijos.get(0), var);
            double[] b = linearAB(n.hijos.get(1), var);
            if (a == null || b == null) return null;
            return t == LexToken.Type.SUM ? new double[]{a[0]+b[0], a[1]+b[1]} : new double[]{a[0]-b[0], a[1]-b[1]};
        }
        if ((t == LexToken.Type.MUL) && n.hijos.size() == 2) {
            Double cL = evaluarConstante(n.hijos.get(0));
            Double cR = evaluarConstante(n.hijos.get(1));
            if (cL != null) {
                double[] v = linearAB(n.hijos.get(1), var);
                return v == null ? null : new double[]{cL*v[0], cL*v[1]};
            }
            if (cR != null) {
                double[] v = linearAB(n.hijos.get(0), var);
                return v == null ? null : new double[]{cR*v[0], cR*v[1]};
            }
            return null;
        }
        if (t == LexToken.Type.DIV && n.hijos.size() == 2) {
            double[] v = linearAB(n.hijos.get(0), var);
            Double den = evaluarConstante(n.hijos.get(1));
            if (v == null || den == null || Math.abs(den) < 1e-15) return null;
            return new double[]{v[0]/den, v[1]/den};
        }
        return null;
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

    private static String num(double x) {
        if (Double.isNaN(x)) return "\\text{NaN}";
        if (Double.isInfinite(x)) return x > 0 ? "\\infty" : "-\\infty";
        long r = Math.round(x);
        if (Math.abs(x - r) < 1e-10) return Long.toString(r);
        java.text.DecimalFormat df = new java.text.DecimalFormat("0.##########", java.text.DecimalFormatSymbols.getInstance(java.util.Locale.US));
        return df.format(x);
    }

    private static String unicaVariable(NodoAST n) {
        Set<String> s = varsEnSubarbol(n);
        return s.size() == 1 ? s.iterator().next() : "x";
    }

    private static Set<String> varsEnSubarbol(NodoAST n) {
        Set<String> s = new LinkedHashSet<>();
        if (n == null) return s;
        if (n.token != null && n.token.type == LexToken.Type.VARIABLE) s.add(n.token.value);
        for (NodoAST h : n.hijos) s.addAll(varsEnSubarbol(h));
        return s;
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
}
