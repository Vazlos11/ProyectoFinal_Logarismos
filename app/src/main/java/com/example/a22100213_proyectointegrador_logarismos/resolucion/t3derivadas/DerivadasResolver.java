package com.example.a22100213_proyectointegrador_logarismos.resolucion.t3derivadas;

import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.MetodoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.PlanificadorResolucion;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.TipoExpresion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.Resolver;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t3derivadas.rules.*;

public final class DerivadasResolver implements Resolver {
    @Override public boolean supports(ResultadoSemantico rs) { return rs != null && rs.tipoPrincipal == TipoExpresion.T3_DERIVADA; }
    @Override public ResultadoResolucion resolve(NodoAST raiz, ResultadoSemantico rs) {
        MetodoResolucion m = PlanificadorResolucion.metodo(raiz, rs);
        DerivativeRule rule;
        switch (m) {
            case DERIVADA_CONSTANTE_CERO:    rule = new ConstantRule(); break;
            case DERIVADA_REGLA_EXPONENTE:   rule = new PowerRule(); break;
            case DERIVADA_CONST_POR_FUNCION: return new ConstTimesFunctionRule().apply(raiz, rs);
            case DERIVADA_REGLA_SUMA:        rule = new SumRule(); break;
            case DERIVADA_PRODUCTO:          rule = new ProductRule(); break;
            case DERIVADA_COCIENTE:          rule = new QuotientRule(); break;
            case DERIVADA_CADENA:            rule = new ChainRule(); break;
            case DERIVADA_TRIG:              rule = new TrigRule(); break;
            default:                         rule = new PowerRule(); break;
        }
        ResultadoResolucion rr = rule.apply(raiz, rs);
        if (rr != null) {
            if (rr.pasos == null) rr.pasos = new java.util.ArrayList<>();
            if (rr.pasos.isEmpty()) rr.pasos.add(new PasoResolucion(rr.latexFinal));
        }
        return rr;
    }
}
