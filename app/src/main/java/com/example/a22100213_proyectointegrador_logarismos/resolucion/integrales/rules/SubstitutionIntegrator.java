package com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.rules;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegratorRule;

public final class SubstitutionIntegrator implements IntegratorRule {

    private final boolean definida;

    public SubstitutionIntegrator(boolean definida) {
        this.definida = definida;
    }

    @Override
    public ResultadoResolucion apply(NodoAST raiz, ResultadoSemantico rs) {
        IntegralUtils.IntegralInfo ii = IntegralUtils.localizarIntegral(raiz, definida);
        if (ii == null || ii.cuerpo == null || ii.var == null) return null;

        NodoAST F = IntegralUtils.integralRec(ii.cuerpo, ii.var);
        if (F == null) return null;

        NodoAST out = definida
                ? IntegralUtils.evalDefinida(F, ii.var, ii.inf, ii.sup)
                : IntegralUtils.addC(F);

        NodoAST nuevaRaiz = IntegralUtils.reemplazar(ii.nodoIntegral, AstUtils.cloneTree(out), ii.padre, out);

        ResultadoResolucion rr = new ResultadoResolucion();
        rr.resultado = nuevaRaiz;
        rr.latexFinal = AstUtils.toTeX(nuevaRaiz);

        if (definida) {
            NodoAST paso = IntegralUtils.evalDefinida(AstUtils.cloneTree(F), ii.var, ii.inf, ii.sup);
            rr.pasos.add(new PasoResolucion(AstUtils.toTeX(paso)));
        } else {
            rr.pasos.add(new PasoResolucion(AstUtils.toTeX(IntegralUtils.addC(AstUtils.cloneTree(F)))));
        }

        return rr;
    }
}
