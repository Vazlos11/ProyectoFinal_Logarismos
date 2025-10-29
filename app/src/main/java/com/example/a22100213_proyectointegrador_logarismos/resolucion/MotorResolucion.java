package com.example.a22100213_proyectointegrador_logarismos.resolucion;

import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.AnalisisSemantico;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.MetodoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.PlanificadorResolucion;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.TipoExpresion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.aritmetica.AritmeticaResolver;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.format.ExpressionFormatter;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralResolver;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t3derivadas.DerivadasResolver;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t6lineal.T6LinearResolver;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t7nolineal.T7NoLinealResolver;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t8sistemas.T8CramerResolver;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t9imaginarios.ComplejosResolver;

import java.util.EnumSet;
import java.util.LinkedHashSet;
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
        ResultadoResolucion rrMaster = new ResultadoResolucion();
        rrMaster.pasos = new LinkedList<>();
        NodoAST actual = raiz;
        ResultadoSemantico cur = rs != null && rs.tipoPrincipal != null ? rs : AnalisisSemantico.analizar(actual);
        if (rs == null || rs.tipoPrincipal == null) {
            rrMaster.pasos.add(new PasoResolucion("Sin análisis semántico"));
        }
        LinkedHashSet<String> vistos = new LinkedHashSet<>();
        int maxIter = 6;
        for (int iter = 0; iter < maxIter; iter++) {
            String texAntes = AstUtils.toTeX(actual);
            if (!vistos.add(texAntes)) break;
            MetodoResolucion m = PlanificadorResolucion.metodo(actual, cur);
            ResultadoResolucion parcial;
            if (m == MetodoResolucion.NEWTON_RAPHSON) {
                parcial = new T7NoLinealResolver().resolve(actual, cur);
            } else if (METODOS_NO_LINEALES.contains(m)) {
                parcial = new T7NoLinealResolver().resolve(actual, cur);
            } else if (METODOS_LINEALES.contains(m)) {
                parcial = new T6LinearResolver().resolve(actual, cur);
            } else {
                switch (cur.tipoPrincipal) {
                    case T1_ARITMETICA:
                        parcial = new AritmeticaResolver().resolve(actual, cur);
                        break;
                    case T2_ALGEBRA_FUNC:
                        parcial = com.example.a22100213_proyectointegrador_logarismos.resolucion.t2algebra.T2AlgebraResolver.resolver(actual, cur);
                        break;
                    case T3_DERIVADA:
                        parcial = new DerivadasResolver().resolve(actual, cur);
                        break;
                    case T4_INTEGRAL_INDEFINIDA:
                    case T5_INTEGRAL_DEFINIDA:
                        parcial = new IntegralResolver().resolve(actual, cur);
                        break;
                    case T6_DESPEJE_LINEAL:
                        parcial = new T6LinearResolver().resolve(actual, cur);
                        break;
                    case T7_DESPEJE_POLINOMICO:
                        parcial = new T7NoLinealResolver().resolve(actual, cur);
                        break;
                    case T8_SISTEMA_EC:
                        parcial = new T8CramerResolver().resolve(actual, cur);
                        break;
                    case T9_IMAGINARIOS:
                        parcial = new ComplejosResolver().resolve(actual, cur);
                        break;
                    default:
                        parcial = new ResultadoResolucion();
                        parcial.pasos = new LinkedList<>();
                        parcial.pasos.add(new PasoResolucion("Expresión sin método de resolución implementado"));
                        parcial.latexFinal = AstUtils.toTeX(actual);
                        parcial.resultado = actual;
                        break;
                }
            }
            rrMaster.pasos.addAll(parcial.pasos);
            rrMaster.latexFinal = parcial.latexFinal != null ? parcial.latexFinal : AstUtils.toTeX(parcial.resultado != null ? parcial.resultado : actual);
            actual = parcial.resultado != null ? parcial.resultado : actual;
            String texDespues = AstUtils.toTeX(actual);
            if (texDespues.equals(texAntes)) break;
            ResultadoSemantico next = AnalisisSemantico.analizar(actual);
            if (cur.tipoPrincipal == TipoExpresion.T2_ALGEBRA_FUNC || next.tipoPrincipal == TipoExpresion.T2_ALGEBRA_FUNC) {
                cur = next;
                break;
            }
            if (!permitida(cur.tipoPrincipal, next.tipoPrincipal)) {
                cur = next;
                break;
            }
            rrMaster.pasos.add(new PasoResolucion("Replanificación - " + PlanificadorResolucion.metodo(actual, next).name()));
            cur = next;
        }
        rrMaster.resultado = actual;
        if (rrMaster.latexFinal == null) rrMaster.latexFinal = AstUtils.toTeX(actual);
        NodoAST fmt = ExpressionFormatter.format(rrMaster.resultado);
        String texBeforeFmt = AstUtils.toTeX(rrMaster.resultado);
        String texAfterFmt = AstUtils.toTeX(fmt);
        if (!texAfterFmt.equals(texBeforeFmt)) {
            rrMaster.pasos.add(new PasoResolucion("\\text{Formateo final}\\; " + texBeforeFmt + "\\;\\Rightarrow\\; " + texAfterFmt));
            rrMaster.resultado = fmt;
            rrMaster.latexFinal = texAfterFmt;
        }
        return rrMaster;
    }

    private static boolean permitida(TipoExpresion de, TipoExpresion a) {
        if (de == a) return true;
        if (de == TipoExpresion.T6_DESPEJE_LINEAL && (a == TipoExpresion.T1_ARITMETICA || a == TipoExpresion.T9_IMAGINARIOS)) return true;
        if (de == TipoExpresion.T7_DESPEJE_POLINOMICO && (a == TipoExpresion.T1_ARITMETICA || a == TipoExpresion.T9_IMAGINARIOS)) return true;
        if (de == TipoExpresion.T8_SISTEMA_EC && (a == TipoExpresion.T1_ARITMETICA || a == TipoExpresion.T9_IMAGINARIOS)) return true;
        if (de == TipoExpresion.T9_IMAGINARIOS && a == TipoExpresion.T1_ARITMETICA) return true;
        return false;
    }

    public static ResultadoResolucion resolver(NodoAST raiz, ResultadoSemantico rs) {
        return resolve(raiz, rs);
    }
}
