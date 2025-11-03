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
        boolean definida = rs != null && rs.tipoPrincipal == TipoExpresion.T5_INTEGRAL_DEFINIDA;
        MetodoResolucion m = PlanificadorResolucion.metodo(raiz, rs);

        if (m == MetodoResolucion.NINGUNO) {
            IntegratorRule pr = new PowerRuleIntegrator(definida);
            ResultadoResolucion rrTry = pr.apply(raiz, rs);
            if (rrTry != null && rrTry.resultado != null) return rrTry;

            ResultadoResolucion out = new ResultadoResolucion();
            out.pasos = new java.util.ArrayList<>();
            out.resultado = raiz;
            out.latexFinal = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.toTeX(raiz);
            out.pasos.add(new com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion("Sin método asignado"));
            out.pasos.add(new com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion(out.latexFinal));
            return out;
        }

        IntegratorRule rule;
        switch (m) {
            case INTEGRAL_POR_PARTES:                  rule = new ByPartsExpPolyIntegrator(definida); break;
            case INTEGRAL_SUSTITUCION:                 rule = new SubstitutionIntegrator(definida);   break;
            case INTEGRAL_TRIGONOMETRICA:              rule = new TrigSubstitutionIntegrator(definida); break;
            case INTEGRAL_RACIONAL_PFD:                rule = new RationalPFDIntegrator(definida);    break;
            case INTEGRAL_NUMERICA_SIMPSON_O_TRAPECIO: rule = new SimpsonDefiniteIntegrator();        break;
            case INTEGRAL_REGLA_POTENCIA:              rule = new PowerRuleIntegrator(definida);      break;
            default:                                   rule = null;                                   break;
        }

        if (rule == null) {
            ResultadoResolucion out = new ResultadoResolucion();
            out.pasos = new java.util.ArrayList<>();
            out.resultado = raiz;
            out.latexFinal = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.toTeX(raiz);
            out.pasos.add(new com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion("Método no aplicable"));
            out.pasos.add(new com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion(out.latexFinal));
            return out;
        }

        ResultadoResolucion rr = rule.apply(raiz, rs);
        if (rr != null && rr.resultado != null) return rr;

        IntegratorRule pr = new PowerRuleIntegrator(definida);
        ResultadoResolucion rrTry = pr.apply(raiz, rs);
        if (rrTry != null && rrTry.resultado != null) return rrTry;

        ResultadoResolucion out = new ResultadoResolucion();
        out.pasos = new java.util.ArrayList<>();
        out.resultado = raiz;
        out.latexFinal = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.toTeX(raiz);
        out.pasos.add(new com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion("Método no aplicable"));
        out.pasos.add(new com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion(out.latexFinal));
        return out;
    }

}
