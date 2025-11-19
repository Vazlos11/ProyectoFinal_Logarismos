package com.example.a22100213_proyectointegrador_logarismos.resolucion.t7nolineal;

import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.MetodoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.PlanificadorResolucion;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.TipoExpresion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.Resolver;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import java.util.EnumSet;
import java.util.Set;

public final class T7NoLinealResolver implements Resolver {
    private static final Set<MetodoResolucion> POLI = EnumSet.of(
            MetodoResolucion.ECUACION_CUADRATICA,
            MetodoResolucion.POLI_RUFFINI,
            MetodoResolucion.ECUACION_POLINOMICA,
            MetodoResolucion.NEWTON_RAPHSON
    );

    private static final Set<MetodoResolucion> ESPECIALES = EnumSet.of(
            MetodoResolucion.ECUACION_EXPONENCIAL,
            MetodoResolucion.ECUACION_LOGARITMICA,
            MetodoResolucion.ECUACION_POTENCIA,
            MetodoResolucion.ECUACION_VALOR_ABSOLUTO,
            MetodoResolucion.ECUACION_TRIG_SIN,
            MetodoResolucion.ECUACION_TRIG_COS,
            MetodoResolucion.ECUACION_TRIG_TAN,
            MetodoResolucion.ECUACION_ARC_TRIG,
            MetodoResolucion.ECUACION_RACIONAL_LINEAL,
            MetodoResolucion.ECUACION_RECIPROCA_LINEAL
    );

    @Override
    public boolean supports(ResultadoSemantico rs) {
        if (rs == null) return false;
        if (rs.tipoPrincipal == TipoExpresion.T7_DESPEJE_POLINOMICO) return true;
        return rs.tipoPrincipal == TipoExpresion.T6_DESPEJE_LINEAL;
    }

    @Override
    public ResultadoResolucion resolve(NodoAST raiz, ResultadoSemantico rs) {
        MetodoResolucion m = PlanificadorResolucion.metodo(raiz, rs);
        if (POLI.contains(m)) return new T7PolynomialResolver().resolve(raiz, rs);
        if (ESPECIALES.contains(m)) return NoLinealEspecialResolver.resolve(raiz, rs, m);
        ResultadoResolucion rr = new ResultadoResolucion();
        String tex = AstUtils.toTeX(raiz);
        rr.pasos.add(new PasoResolucion("Expresión inicial", tex));
        rr.pasos.add(new PasoResolucion("Planificador — método no soportado aquí", "\\text{" + m + "}"));
        rr.resultado = raiz;
        rr.latexFinal = tex;
        rr.pasos.add(new PasoResolucion("Resultado", rr.latexFinal));
        return rr;
    }
}
