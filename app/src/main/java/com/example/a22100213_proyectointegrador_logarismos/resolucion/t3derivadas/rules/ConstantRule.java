package com.example.a22100213_proyectointegrador_logarismos.resolucion.t3derivadas.rules;

import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t3derivadas.DerivativeUtils;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;

public final class ConstantRule implements DerivativeRule {
    @Override public ResultadoResolucion apply(NodoAST raiz, ResultadoSemantico rs) {
        DerivativeUtils.DerivInfo di = DerivativeUtils.localizarDerivada(raiz);
        ResultadoResolucion rr = new ResultadoResolucion();
        if (di == null || di.fun == null) { rr.resultado = raiz; rr.latexFinal = AstUtils.toTeX(raiz); return rr; }
        if (!DerivativeUtils.esConstantePura(di.fun)) { rr.resultado = raiz; rr.latexFinal = AstUtils.toTeX(raiz); return rr; }
        NodoAST d = AstUtils.number(0.0);
        NodoAST nuevo = IntegralUtils.reemplazarSubexp(raiz, di.nodoDeriv, d);
        rr.resultado = nuevo; rr.latexFinal = AstUtils.toTeX(nuevo);
        rr.pasos.add(new PasoResolucion("0"));
        rr.pasos.add(new PasoResolucion(rr.latexFinal));
        return rr;
    }
}
