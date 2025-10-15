package com.example.a22100213_proyectointegrador_logarismos.resolucion.aritmetica;

import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.PlanificadorResolucion;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.TipoExpresion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.Resolver;

public class AritmeticaResolver implements Resolver {
    @Override
    public boolean supports(ResultadoSemantico rs) {
        return rs != null && rs.tipoPrincipal == TipoExpresion.T1_ARITMETICA;
    }

    @Override
    public ResultadoResolucion resolve(NodoAST raiz, ResultadoSemantico rs) {
        ResultadoResolucion rr = new ResultadoResolucion();
        String before = AstUtils.toTeX(raiz);
        String etiqueta = PlanificadorResolucion.plan(raiz, rs);
        Double v = AstUtils.evalConst(raiz);
        if (v != null) {
            double w = (v == 0.0 ? 0.0 : v);
            rr.resultado = AstUtils.number(w);
            rr.latexFinal = AstUtils.toTeX(rr.resultado);
            rr.pasos.add(new PasoResolucion("\\text{" + etiqueta + "}\\; " + before + "\\;\\Rightarrow\\; " + rr.latexFinal));
            return rr;
        }
        rr.resultado = raiz;
        rr.latexFinal = before;
        rr.pasos.add(new PasoResolucion("\\text{Sin cambio (T1)}\\; " + before + "\\;\\Rightarrow\\; " + rr.latexFinal));
        return rr;
    }
}
