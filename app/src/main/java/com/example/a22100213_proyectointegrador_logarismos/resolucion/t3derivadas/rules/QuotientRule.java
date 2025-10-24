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

public final class QuotientRule implements DerivativeRule {
    @Override public ResultadoResolucion apply(NodoAST raiz, ResultadoSemantico rs) {
        DerivativeUtils.DerivInfo di = DerivativeUtils.localizarDerivada(raiz);
        ResultadoResolucion rr = new ResultadoResolucion();
        if (di == null || di.fun == null) { rr.resultado = raiz; rr.latexFinal = AstUtils.toTeX(raiz); return rr; }
        NodoAST f = di.fun;
        if (f.token.type != LexToken.Type.DIV || f.hijos.size() != 2) { rr.resultado = raiz; rr.latexFinal = AstUtils.toTeX(raiz); return rr; }
        NodoAST u = f.hijos.get(0), v = f.hijos.get(1);
        NodoAST duNode = DerivativeUtils.deriv(u, di.dif);
        NodoAST dvNode = DerivativeUtils.deriv(v, di.dif);
        ResultadoSemantico rsu = AnalisisSemantico.analizar(duNode);
        ResultadoSemantico rsv = AnalisisSemantico.analizar(dvNode);
        NodoAST du = new DerivadasResolver().resolve(duNode, rsu).resultado;
        NodoAST dv = new DerivadasResolver().resolve(dvNode, rsv).resultado;
        NodoAST num = DerivativeUtils.sub(DerivativeUtils.mul(du, v), DerivativeUtils.mul(u, dv));
        NodoAST den = DerivativeUtils.pow(v, AstUtils.number(2.0));
        NodoAST res = DerivativeUtils.div(num, den);
        NodoAST nuevo = IntegralUtils.reemplazarSubexp(raiz, di.nodoDeriv, res);
        rr.resultado = nuevo; rr.latexFinal = AstUtils.toTeX(nuevo);
        rr.pasos.add(new PasoResolucion(AstUtils.toTeX(res)));
        rr.pasos.add(new PasoResolucion(rr.latexFinal));
        return rr;
    }
}
