package com.example.a22100213_proyectointegrador_logarismos.resolucion;

import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.TipoExpresion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.aritmetica.AritmeticaResolver;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralResolver;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t3derivadas.DerivadasResolver;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t6lineal.T6LinearResolver;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t7polinomios.T7PolynomialResolver;

import java.util.LinkedList;

public class MotorResolucion {

    public static ResultadoResolucion resolve(NodoAST raiz, ResultadoSemantico rs) {
        if (rs == null || rs.tipoPrincipal == null) {
            ResultadoResolucion rr = new ResultadoResolucion();
            rr.pasos = new LinkedList<>();
            rr.pasos.add(new PasoResolucion("Sin análisis semántico"));
            rr.latexFinal = AstUtils.toTeX(raiz);
            return rr;
        }

        TipoExpresion t = rs.tipoPrincipal;
        switch (t) {
            case T1_ARITMETICA:
                return new AritmeticaResolver().resolve(raiz, rs);

            case T2_ALGEBRA_FUNC:
                return com.example.a22100213_proyectointegrador_logarismos.resolucion.t2algebra.T2AlgebraResolver.resolver(raiz, rs);

            case T3_DERIVADA:
                return new DerivadasResolver().resolve(raiz, rs);

            case T4_INTEGRAL_INDEFINIDA:
            case T5_INTEGRAL_DEFINIDA:
                return new IntegralResolver().resolve(raiz, rs);

            case T6_DESPEJE_LINEAL:
                return new T6LinearResolver().resolve(raiz, rs);

            case T7_DESPEJE_POLINOMICO:
                return new T7PolynomialResolver().resolve(raiz, rs);

            case T8_SISTEMA_EC:
            case T9_IMAGINARIOS:
            default: {
                ResultadoResolucion rr = new ResultadoResolucion();
                rr.pasos = new LinkedList<>();
                rr.pasos.add(new PasoResolucion("Expresión sin método de resolución implementado"));
                rr.latexFinal = AstUtils.toTeX(raiz);
                return rr;
            }
        }
    }

    public static ResultadoResolucion resolver(NodoAST raiz, ResultadoSemantico rs) {
        return resolve(raiz, rs);
    }
}
