package com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales;

import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.MetodoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.PlanificadorResolucion;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.TipoExpresion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.Resolver;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.rules.ByPartsExpPolyIntegrator;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.rules.PowerRuleIntegrator;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.rules.RationalPFDIntegrator;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.rules.SimpsonDefiniteIntegrator;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.rules.SubstitutionIntegrator;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.rules.TrigSubstitutionIntegrator;

public final class IntegralResolver implements Resolver {

    @Override
    public boolean supports(ResultadoSemantico rs) {
        if (rs == null) return false;
        return rs.tipoPrincipal == TipoExpresion.T4_INTEGRAL_INDEFINIDA
                || rs.tipoPrincipal == TipoExpresion.T5_INTEGRAL_DEFINIDA;
    }

    @Override
    public ResultadoResolucion resolve(NodoAST raiz, ResultadoSemantico rs) {
        MetodoResolucion m = PlanificadorResolucion.metodo(raiz, rs);
        if (rs.tipoPrincipal == TipoExpresion.T4_INTEGRAL_INDEFINIDA) {
            return resolverIndefinida(raiz, rs, m);
        } else {
            return resolverDefinida(raiz, rs, m);
        }
    }

    public static ResultadoResolucion resolverIndefinida(NodoAST raiz, ResultadoSemantico rs, MetodoResolucion m) {
        IntegratorRule rule;
        switch (m) {
            case INTEGRAL_POR_PARTES:     rule = new ByPartsExpPolyIntegrator(false); break;
            case INTEGRAL_SUSTITUCION:    rule = new SubstitutionIntegrator(false);   break;
            case INTEGRAL_TRIGONOMETRICA: rule = new TrigSubstitutionIntegrator(false); break;
            case INTEGRAL_RACIONAL_PFD:   rule = new RationalPFDIntegrator(false);    break;
            case INTEGRAL_REGLA_POTENCIA:
            case NINGUNO:
            default:                      rule = new PowerRuleIntegrator(false);      break;
        }
        return rule.apply(raiz, rs);
    }

    public static ResultadoResolucion resolverDefinida(NodoAST raiz, ResultadoSemantico rs, MetodoResolucion m) {
        IntegratorRule rule;
        switch (m) {
            case INTEGRAL_POR_PARTES:                       rule = new ByPartsExpPolyIntegrator(true); break;
            case INTEGRAL_SUSTITUCION:                      rule = new SubstitutionIntegrator(true);    break;
            case INTEGRAL_TRIGONOMETRICA:                   rule = new TrigSubstitutionIntegrator(true); break;
            case INTEGRAL_RACIONAL_PFD:                     rule = new RationalPFDIntegrator(true);     break;
            case INTEGRAL_NUMERICA_SIMPSON_O_TRAPECIO:      rule = new SimpsonDefiniteIntegrator();     break;
            case INTEGRAL_REGLA_POTENCIA:
            case NINGUNO:
            default:                                        rule = new PowerRuleIntegrator(true);       break;
        }
        return rule.apply(raiz, rs);
    }
}
