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

public final class TrigRule implements DerivativeRule {
    @Override
    public ResultadoResolucion apply(NodoAST raiz, ResultadoSemantico rs) {
        DerivativeUtils.DerivInfo di = DerivativeUtils.localizarDerivada(raiz);
        ResultadoResolucion rr = new ResultadoResolucion();
        if (di == null || di.fun == null) { rr.resultado = raiz; rr.latexFinal = AstUtils.toTeX(raiz); return rr; }

        NodoAST g = di.fun;
        if (g.hijos.isEmpty()) { rr.resultado = raiz; rr.latexFinal = AstUtils.toTeX(raiz); return rr; }
        NodoAST u = g.hijos.get(0);

        NodoAST uprime = subDerivOrConst(u, di.dif);

        NodoAST res = null;
        switch (g.token.type) {
            case TRIG_SIN: { NodoAST cosu = DerivativeUtils.cos(u); res = DerivativeUtils.mul(cosu, uprime); break; }
            case TRIG_COS: { NodoAST sinu = DerivativeUtils.sin(u); res = DerivativeUtils.mul(AstUtils.number(-1.0), DerivativeUtils.mul(sinu, uprime)); break; }
            case TRIG_TAN: { NodoAST cosu = DerivativeUtils.cos(u); NodoAST cos2 = DerivativeUtils.pow(cosu, AstUtils.number(2.0)); res = DerivativeUtils.mul(DerivativeUtils.div(AstUtils.number(1.0), cos2), uprime); break; }
            case TRIG_COT: { NodoAST sinu = DerivativeUtils.sin(u); NodoAST sin2 = DerivativeUtils.pow(sinu, AstUtils.number(2.0)); res = DerivativeUtils.mul(AstUtils.number(-1.0), DerivativeUtils.mul(DerivativeUtils.div(AstUtils.number(1.0), sin2), uprime)); break; }
            case TRIG_SEC: { NodoAST sinu = DerivativeUtils.sin(u); NodoAST cosu = DerivativeUtils.cos(u); NodoAST cos2 = DerivativeUtils.pow(cosu, AstUtils.number(2.0)); res = DerivativeUtils.mul(DerivativeUtils.div(sinu, cos2), uprime); break; }
            case TRIG_CSC: { NodoAST sinu = DerivativeUtils.sin(u); NodoAST cosu = DerivativeUtils.cos(u); NodoAST sin2 = DerivativeUtils.pow(sinu, AstUtils.number(2.0)); res = DerivativeUtils.mul(AstUtils.number(-1.0), DerivativeUtils.mul(DerivativeUtils.div(cosu, sin2), uprime)); break; }
            case TRIG_ARCSIN: { NodoAST u2 = DerivativeUtils.pow(u, AstUtils.number(2.0)); NodoAST oneMinus = AstUtils.bin(LexToken.Type.SUB, AstUtils.number(1.0), u2, "-", 0); NodoAST denom = DerivativeUtils.sqrt(oneMinus); res = DerivativeUtils.div(uprime, denom); break; }
            case TRIG_ARCCOS: { NodoAST u2 = DerivativeUtils.pow(u, AstUtils.number(2.0)); NodoAST oneMinus = AstUtils.bin(LexToken.Type.SUB, AstUtils.number(1.0), u2, "-", 0); NodoAST denom = DerivativeUtils.sqrt(oneMinus); res = DerivativeUtils.mul(AstUtils.number(-1.0), DerivativeUtils.div(uprime, denom)); break; }
            case TRIG_ARCTAN: { NodoAST u2 = DerivativeUtils.pow(u, AstUtils.number(2.0)); NodoAST onePlus = AstUtils.bin(LexToken.Type.SUM, AstUtils.number(1.0), u2, "+", 0); res = DerivativeUtils.div(uprime, onePlus); break; }
            case TRIG_ARCCOT: { NodoAST u2 = DerivativeUtils.pow(u, AstUtils.number(2.0)); NodoAST onePlus = AstUtils.bin(LexToken.Type.SUM, AstUtils.number(1.0), u2, "+", 0); res = DerivativeUtils.mul(AstUtils.number(-1.0), DerivativeUtils.div(uprime, onePlus)); break; }
            default: rr.resultado = raiz; rr.latexFinal = AstUtils.toTeX(raiz); return rr;
        }

        if (res == null) { rr.resultado = raiz; rr.latexFinal = AstUtils.toTeX(raiz); return rr; }
        NodoAST nuevo = IntegralUtils.reemplazarSubexp(raiz, di.nodoDeriv, res);
        rr.resultado = nuevo;
        rr.latexFinal = AstUtils.toTeX(nuevo);
        rr.pasos.add(new PasoResolucion(AstUtils.toTeX(res)));
        rr.pasos.add(new PasoResolucion(rr.latexFinal));
        return rr;
    }

    private NodoAST subDerivOrConst(NodoAST inner, NodoAST difNode) {
        String v = diffVarName(difNode);
        if (inner != null && inner.token != null && inner.token.type == LexToken.Type.VARIABLE) {
            String n = inner.token.value;
            if (n != null && n.equals(v)) return AstUtils.number(1.0);
            if (n != null && !n.equals(v)) return AstUtils.number(0.0);
        }
        java.util.HashSet<String> s = new java.util.HashSet<>();
        AstUtils.collectVars(inner, s);
        if (!s.contains(v)) return AstUtils.number(0.0);
        NodoAST dnode = DerivativeUtils.deriv(inner, AstUtils.atom(LexToken.Type.DIFFERENTIAL, "d" + v, 0));
        ResultadoSemantico rsub = AnalisisSemantico.analizar(dnode);
        return new DerivadasResolver().resolve(dnode, rsub).resultado;
    }

    private String diffVarName(NodoAST difNode) {
        if (difNode == null || difNode.token == null || difNode.token.value == null) return "x";
        String s = difNode.token.value.trim();
        if (s.length() == 1) return s;
        if ((s.startsWith("d") || s.startsWith("D")) && s.length() > 1) return s.substring(1);
        return s;
    }
}
