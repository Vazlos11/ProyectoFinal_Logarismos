package com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.rules;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegratorRule;

public final class PowerRuleIntegrator implements IntegratorRule {
    private final boolean definida;

    public PowerRuleIntegrator(boolean definida) {
        this.definida = definida;
    }

    @Override
    public ResultadoResolucion apply(NodoAST raiz, ResultadoSemantico rs) {
        com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils.IntegralInfo ii =
                com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils.localizarIntegral(raiz, definida);
        if (ii == null || ii.cuerpo == null || ii.var == null) return null;

        NodoAST cuerpo = com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils
                .foldEConstTimesXIntoExpLinear(ii.cuerpo, ii.var);
        NodoAST F = com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils.integralRec(cuerpo, ii.var);
        if (F == null) return null;

        if (!definida) {
            NodoAST fin = com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils.addC(F);
            NodoAST nuevo = com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils
                    .reemplazar(ii.nodoIntegral,
                            com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.cloneTree(fin),
                            ii.padre, fin);

            ResultadoResolucion rr = new ResultadoResolucion();
            rr.pasos = new java.util.ArrayList<>();
            rr.resultado = nuevo;
            rr.latexFinal = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.toTeX(nuevo);
            rr.pasos.add(new com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion(rr.latexFinal));
            return rr;
        } else {
            NodoAST val = com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils
                    .evalDefinida(F, ii.var, ii.inf, ii.sup);
            NodoAST nuevo = com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils
                    .reemplazar(ii.nodoIntegral,
                            com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.cloneTree(val),
                            ii.padre, val);

            ResultadoResolucion rr = new ResultadoResolucion();
            rr.pasos = new java.util.ArrayList<>();
            rr.pasos.add(new com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion(
                    com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.toTeX(
                            com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils
                                    .sustituirVar(com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.cloneTree(F), ii.var, ii.sup))));
            rr.pasos.add(new com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion(
                    com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.toTeX(
                            com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils
                                    .sustituirVar(com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.cloneTree(F), ii.var, ii.inf))));
            rr.pasos.add(new com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion(
                    com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.toTeX(val)));
            rr.resultado = nuevo;
            rr.latexFinal = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.toTeX(nuevo);
            rr.pasos.add(new com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion(rr.latexFinal));
            return rr;
        }
    }
}
