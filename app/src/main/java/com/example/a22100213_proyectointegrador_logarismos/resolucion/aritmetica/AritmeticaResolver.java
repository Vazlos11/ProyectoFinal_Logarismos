package com.example.a22100213_proyectointegrador_logarismos.resolucion.aritmetica;

import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.TipoExpresion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.*;

public class AritmeticaResolver implements Resolver {
    @Override
    public boolean supports(ResultadoSemantico rs) {
        return rs != null && rs.tipoPrincipal == TipoExpresion.T1_ARITMETICA;
    }

    @Override
    public ResultadoResolucion resolve(NodoAST raiz, ResultadoSemantico rs) {
        Double v = AstUtils.evalConst(raiz);
        ResultadoResolucion rr = new ResultadoResolucion();
        if (v != null) {
            rr.resultado = AstUtils.number(v);
            rr.latexFinal = AstUtils.toTeX(rr.resultado);
            rr.pasos.add(new PasoResolucion("\\text{Evaluacion directa} \\Rightarrow " + rr.latexFinal));
            return rr;
        }
        rr.resultado = raiz;
        rr.latexFinal = AstUtils.toTeX(raiz);
        rr.pasos.add(new PasoResolucion("\\text{Sin cambio} \\Rightarrow " + rr.latexFinal));
        return rr;
    }
}
