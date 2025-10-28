package com.example.a22100213_proyectointegrador_logarismos.resolucion;

import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.MetodoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.PlanificadorResolucion;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.TipoExpresion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.aritmetica.AritmeticaResolver;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralResolver;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t3derivadas.DerivadasResolver;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t6lineal.T6LinearResolver;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t7nolineal.T7NoLinealResolver;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t7nolineal.T7PolynomialResolver;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t8sistemas.T8CramerResolver;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Set;

public class MotorResolucion {

    private static final Set<MetodoResolucion> METODOS_NO_LINEALES = EnumSet.of(
            MetodoResolucion.ECUACION_EXPONENCIAL,
            MetodoResolucion.ECUACION_LOGARITMICA,
            MetodoResolucion.ECUACION_POTENCIA,
            MetodoResolucion.ECUACION_VALOR_ABSOLUTO,
            MetodoResolucion.ECUACION_TRIG_SIN,
            MetodoResolucion.ECUACION_TRIG_COS,
            MetodoResolucion.ECUACION_TRIG_TAN,
            MetodoResolucion.ECUACION_ARC_TRIG,
            MetodoResolucion.ECUACION_RACIONAL_LINEAL,
            MetodoResolucion.ECUACION_RECIPROCA_LINEAL,
            MetodoResolucion.ECUACION_CUADRATICA,
            MetodoResolucion.POLI_RUFFINI,
            MetodoResolucion.ECUACION_POLINOMICA,
            MetodoResolucion.NEWTON_RAPHSON
    );

    private static final Set<MetodoResolucion> METODOS_LINEALES = EnumSet.of(
            MetodoResolucion.ECUACION_LINEAL,
            MetodoResolucion.DESPEJE_AISLACION_DIRECTA,
            MetodoResolucion.DESPEJE_BALANCE_INVERSAS,
            MetodoResolucion.DESPEJE_SIMBOLICO_O_NUMERICO,
            MetodoResolucion.DESPEJE_SUSTITUCION_SIMBOLICA
    );

    public static ResultadoResolucion resolve(NodoAST raiz, ResultadoSemantico rs) {
        if (rs == null || rs.tipoPrincipal == null) {
            ResultadoResolucion rr = new ResultadoResolucion();
            rr.pasos = new LinkedList<>();
            rr.pasos.add(new PasoResolucion("Sin análisis semántico"));
            rr.latexFinal = AstUtils.toTeX(raiz);
            return rr;
        }

        MetodoResolucion m = PlanificadorResolucion.metodo(raiz, rs);

        if (m == MetodoResolucion.NEWTON_RAPHSON) {
            return new T7NoLinealResolver().resolve(raiz, rs);
        }

        if (METODOS_NO_LINEALES.contains(m)) {
            return new T7NoLinealResolver().resolve(raiz, rs);
        }
        if (METODOS_LINEALES.contains(m)) {
            return new T6LinearResolver().resolve(raiz, rs);
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
                return new T7NoLinealResolver().resolve(raiz, rs);
            case T8_SISTEMA_EC:
                return new T8CramerResolver().resolve(raiz, rs);
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
