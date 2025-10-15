package com.example.a22100213_proyectointegrador_logarismos.resolucion;

import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.TipoExpresion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.aritmetica.AritmeticaResolver;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralResolver;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t2algebra.T2AlgebraResolver;

public final class MotorResolucion {

    private MotorResolucion() {}

    public static ResultadoResolucion resolver(NodoAST raiz, ResultadoSemantico rs) {
        if (rs == null || rs.tipoPrincipal == null) {
            return fallback(raiz);
        }
        switch (rs.tipoPrincipal) {
            case T1_ARITMETICA:
                return new AritmeticaResolver().resolve(raiz, rs);

            case T2_ALGEBRA_FUNC:
                return T2AlgebraResolver.resolver(raiz, rs);

            case T4_INTEGRAL_INDEFINIDA:
            case T5_INTEGRAL_DEFINIDA:
                return new IntegralResolver().resolve(raiz, rs);

            default:
                return fallback(raiz);
        }
    }

    private static ResultadoResolucion fallback(NodoAST n) {
        ResultadoResolucion rr = new ResultadoResolucion();
        Double v = AstUtils.evalConst(n);
        if (v != null) {
            rr.resultado = AstUtils.number(v);
            rr.latexFinal = AstUtils.toTeX(rr.resultado);
            rr.pasos.add(new PasoResolucion("\\text{Evaluacion directa} \\Rightarrow " + rr.latexFinal));
            return rr;
        }
        rr.resultado = n;
        rr.latexFinal = AstUtils.toTeX(n);
        rr.pasos.add(new PasoResolucion("\\text{Sin cambio} \\Rightarrow " + rr.latexFinal));
        return rr;
    }
}
