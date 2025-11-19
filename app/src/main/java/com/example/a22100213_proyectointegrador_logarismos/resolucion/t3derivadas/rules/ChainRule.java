package com.example.a22100213_proyectointegrador_logarismos.resolucion.t3derivadas.rules;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.AnalisisSemantico;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t3derivadas.DerivativeUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t3derivadas.DerivadasResolver;

public final class ChainRule implements DerivativeRule {
    @Override
    public ResultadoResolucion apply(NodoAST raiz, ResultadoSemantico rs) {
        DerivativeUtils.DerivInfo di = DerivativeUtils.localizarDerivada(raiz);
        ResultadoResolucion rr = new ResultadoResolucion();
        if (di == null || di.fun == null) {
            rr.resultado = raiz;
            rr.latexFinal = AstUtils.toTeX(raiz);
            return rr;
        }

        NodoAST g = di.fun;
        NodoAST inner = null;
        NodoAST upr = null;
        NodoAST res = null;
        String var = diffVarName(di.dif);

        if (g.token.type == LexToken.Type.LN || g.token.type == LexToken.Type.LOG) {
            inner = g.hijos.get(0);
            upr = quickDeriv(inner, var);
            res = DerivativeUtils.div(upr, inner);
        } else if (g.token.type == LexToken.Type.LOG_BASE10) {
            inner = g.hijos.get(0);
            upr = quickDeriv(inner, var);
            NodoAST denom = DerivativeUtils.mul(inner, AstUtils.number(Math.log(10.0)));
            res = DerivativeUtils.div(upr, denom);
        } else if (g.token.type == LexToken.Type.LOG_BASE2) {
            inner = g.hijos.get(0);
            upr = quickDeriv(inner, var);
            NodoAST denom = DerivativeUtils.mul(inner, AstUtils.number(Math.log(2.0)));
            res = DerivativeUtils.div(upr, denom);
        } else if (g.token.type == LexToken.Type.TRIG_SIN) {
            inner = g.hijos.get(0);
            upr = quickDeriv(inner, var);
            res = DerivativeUtils.mul(DerivativeUtils.cos(inner), upr);
        } else if (g.token.type == LexToken.Type.TRIG_COS) {
            inner = g.hijos.get(0);
            upr = quickDeriv(inner, var);
            res = DerivativeUtils.mul(AstUtils.number(-1.0), DerivativeUtils.mul(DerivativeUtils.sin(inner), upr));
        } else if (g.token.type == LexToken.Type.TRIG_TAN) {
            inner = g.hijos.get(0);
            upr = quickDeriv(inner, var);
            NodoAST cos2 = DerivativeUtils.pow(DerivativeUtils.cos(inner), AstUtils.number(2.0));
            res = DerivativeUtils.mul(DerivativeUtils.div(AstUtils.number(1.0), cos2), upr);
        } else if (g.token.type == LexToken.Type.TRIG_COT) {
            inner = g.hijos.get(0);
            upr = quickDeriv(inner, var);
            NodoAST sin2 = DerivativeUtils.pow(DerivativeUtils.sin(inner), AstUtils.number(2.0));
            res = DerivativeUtils.mul(AstUtils.number(-1.0), DerivativeUtils.mul(DerivativeUtils.div(AstUtils.number(1.0), sin2), upr));
        } else if (g.token.type == LexToken.Type.TRIG_SEC) {
            inner = g.hijos.get(0);
            upr = quickDeriv(inner, var);
            NodoAST cos2 = DerivativeUtils.pow(DerivativeUtils.cos(inner), AstUtils.number(2.0));
            res = DerivativeUtils.mul(DerivativeUtils.div(DerivativeUtils.sin(inner), cos2), upr);
        } else if (g.token.type == LexToken.Type.TRIG_CSC) {
            inner = g.hijos.get(0);
            upr = quickDeriv(inner, var);
            NodoAST sin2 = DerivativeUtils.pow(DerivativeUtils.sin(inner), AstUtils.number(2.0));
            res = DerivativeUtils.mul(AstUtils.number(-1.0), DerivativeUtils.mul(DerivativeUtils.div(DerivativeUtils.cos(inner), sin2), upr));
        } else if (g.token.type == LexToken.Type.TRIG_ARCSIN) {
            inner = g.hijos.get(0);
            upr = quickDeriv(inner, var);
            NodoAST u2 = DerivativeUtils.pow(inner, AstUtils.number(2.0));
            NodoAST oneMinus = AstUtils.bin(LexToken.Type.SUB, AstUtils.number(1.0), u2, "-", 0);
            NodoAST denom = DerivativeUtils.sqrt(oneMinus);
            res = DerivativeUtils.div(upr, denom);
        } else if (g.token.type == LexToken.Type.TRIG_ARCCOS) {
            inner = g.hijos.get(0);
            upr = quickDeriv(inner, var);
            NodoAST u2 = DerivativeUtils.pow(inner, AstUtils.number(2.0));
            NodoAST oneMinus = AstUtils.bin(LexToken.Type.SUB, AstUtils.number(1.0), u2, "-", 0);
            NodoAST denom = DerivativeUtils.sqrt(oneMinus);
            res = DerivativeUtils.mul(AstUtils.number(-1.0), DerivativeUtils.div(upr, denom));
        } else if (g.token.type == LexToken.Type.TRIG_ARCTAN) {
            inner = g.hijos.get(0);
            upr = quickDeriv(inner, var);
            NodoAST u2 = DerivativeUtils.pow(inner, AstUtils.number(2.0));
            NodoAST onePlus = AstUtils.bin(LexToken.Type.SUM, AstUtils.number(1.0), u2, "+", 0);
            res = DerivativeUtils.div(upr, onePlus);
        } else if (g.token.type == LexToken.Type.TRIG_ARCCOT) {
            inner = g.hijos.get(0);
            upr = quickDeriv(inner, var);
            NodoAST u2 = DerivativeUtils.pow(inner, AstUtils.number(2.0));
            NodoAST onePlus = AstUtils.bin(LexToken.Type.SUM, AstUtils.number(1.0), u2, "+", 0);
            res = DerivativeUtils.mul(AstUtils.number(-1.0), DerivativeUtils.div(upr, onePlus));
        } else if (g.token.type == LexToken.Type.RADICAL) {
            inner = g.hijos.get(0);
            upr = quickDeriv(inner, var);
            NodoAST denom = DerivativeUtils.mul(AstUtils.number(2.0), DerivativeUtils.sqrt(inner));
            res = DerivativeUtils.div(upr, denom);
        } else if (g.token.type == LexToken.Type.EXP && g.hijos.size() == 2 && g.hijos.get(0).token.type == LexToken.Type.CONST_E) {
            inner = g.hijos.get(1);
            upr = quickDeriv(inner, var);
            res = DerivativeUtils.mul(DerivativeUtils.ePow(inner), upr);
        } else if (g.token.type == LexToken.Type.EXP && g.hijos.size() == 2) {
            NodoAST base = g.hijos.get(0);
            NodoAST expo = g.hijos.get(1);
            if (base != null && expo != null) {
                if (base.token.type == LexToken.Type.INTEGER || base.token.type == LexToken.Type.DECIMAL) {
                    Double a = AstUtils.evalConst(base);
                    if (a != null) {
                        inner = expo;
                        upr = quickDeriv(inner, var);
                        NodoAST aPowU = DerivativeUtils.pow(AstUtils.number(a), inner);
                        res = DerivativeUtils.mul(DerivativeUtils.mul(aPowU, AstUtils.number(Math.log(a))), upr);
                    }
                } else {
                    Double n = AstUtils.evalConst(expo);
                    if (n != null) {
                        inner = base;
                        upr = quickDeriv(inner, var);
                        res = DerivativeUtils.mul(DerivativeUtils.mul(AstUtils.number(n), DerivativeUtils.pow(inner, AstUtils.number(n - 1.0))), upr);
                    }
                }
            }
        }

        if (res == null) {
            rr.resultado = raiz;
            rr.latexFinal = AstUtils.toTeX(raiz);
            return rr;
        }

        NodoAST nuevo = IntegralUtils.reemplazarSubexp(raiz, di.nodoDeriv, res);
        rr.resultado = nuevo;
        rr.latexFinal = AstUtils.toTeX(nuevo);
        if (rr.pasos == null) rr.pasos = new java.util.ArrayList<>();
        if (upr != null) rr.pasos.add(new PasoResolucion("Derivada interna g'(x)", AstUtils.toTeX(upr)));
        rr.pasos.add(new PasoResolucion("Regla de la cadena", AstUtils.toTeX(res)));
        rr.pasos.add(new PasoResolucion("Sustitución en la expresión", rr.latexFinal));
        return rr;
    }

    private NodoAST quickDeriv(NodoAST u, String var) {
        if (u == null) return AstUtils.number(0.0);
        if (u.token == null) return AstUtils.number(0.0);

        LexToken.Type t = u.token.type;

        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL || t == LexToken.Type.CONST_E || t == LexToken.Type.CONST_PI)
            return AstUtils.number(0.0);

        if (t == LexToken.Type.VARIABLE)
            return (u.token.value != null && u.token.value.equals(var)) ? AstUtils.number(1.0) : AstUtils.number(0.0);

        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB) {
            NodoAST a = quickDeriv(u.hijos.get(0), var);
            NodoAST b = quickDeriv(u.hijos.get(1), var);
            return (t == LexToken.Type.SUM) ? AstUtils.bin(LexToken.Type.SUM, a, b, "+", 0) : AstUtils.bin(LexToken.Type.SUB, a, b, "-", 0);
        }

        if (t == LexToken.Type.MUL && u.hijos.size() == 2) {
            Double cL = AstUtils.evalConst(u.hijos.get(0));
            Double cR = AstUtils.evalConst(u.hijos.get(1));
            if (cL != null) return DerivativeUtils.mul(AstUtils.number(cL), quickDeriv(u.hijos.get(1), var));
            if (cR != null) return DerivativeUtils.mul(AstUtils.number(cR), quickDeriv(u.hijos.get(0), var));
        }

        if (t == LexToken.Type.EXP && u.hijos.size() == 2) {
            Double n = AstUtils.evalConst(u.hijos.get(1));
            if (n != null) {
                NodoAST base = u.hijos.get(0);
                return DerivativeUtils.mul(DerivativeUtils.mul(AstUtils.number(n), DerivativeUtils.pow(base, AstUtils.number(n - 1.0))), quickDeriv(base, var));
            }
        }

        if (t == LexToken.Type.LN || t == LexToken.Type.LOG) {
            NodoAST w = u.hijos.get(0);
            return DerivativeUtils.div(quickDeriv(w, var), w);
        }
        if (t == LexToken.Type.LOG_BASE10) {
            NodoAST w = u.hijos.get(0);
            return DerivativeUtils.div(quickDeriv(w, var), DerivativeUtils.mul(w, AstUtils.number(Math.log(10.0))));
        }
        if (t == LexToken.Type.LOG_BASE2) {
            NodoAST w = u.hijos.get(0);
            return DerivativeUtils.div(quickDeriv(w, var), DerivativeUtils.mul(w, AstUtils.number(Math.log(2.0))));
        }

        if (t == LexToken.Type.TRIG_SIN) {
            NodoAST w = u.hijos.get(0);
            return DerivativeUtils.mul(DerivativeUtils.cos(w), quickDeriv(w, var));
        }
        if (t == LexToken.Type.TRIG_COS) {
            NodoAST w = u.hijos.get(0);
            return DerivativeUtils.mul(AstUtils.number(-1.0), DerivativeUtils.mul(DerivativeUtils.sin(w), quickDeriv(w, var)));
        }
        if (t == LexToken.Type.TRIG_TAN) {
            NodoAST w = u.hijos.get(0);
            NodoAST cos2 = DerivativeUtils.pow(DerivativeUtils.cos(w), AstUtils.number(2.0));
            return DerivativeUtils.mul(DerivativeUtils.div(AstUtils.number(1.0), cos2), quickDeriv(w, var));
        }

        if (t == LexToken.Type.RADICAL) {
            NodoAST w = u.hijos.get(0);
            NodoAST sqrtw = DerivativeUtils.sqrt(w);
            return DerivativeUtils.mul(DerivativeUtils.div(AstUtils.number(1.0), DerivativeUtils.mul(AstUtils.number(2.0), sqrtw)), quickDeriv(w, var));
        }

        NodoAST dnode = DerivativeUtils.deriv(u, AstUtils.atom(LexToken.Type.DIFFERENTIAL, "d" + var, 0));
        ResultadoSemantico rsub = AnalisisSemantico.analizar(dnode);
        NodoAST r = new DerivadasResolver().resolve(dnode, rsub).resultado;
        return r == null ? dnode : r;
    }

    private String diffVarName(NodoAST difNode) {
        if (difNode == null || difNode.token == null || difNode.token.value == null) return "x";
        String s = difNode.token.value.trim();
        if (s.length() == 1 && Character.isLetter(s.charAt(0))) return s;
        if ((s.startsWith("d") || s.startsWith("D")) && s.length() > 1) return s.substring(1);
        return "x";
    }
}
