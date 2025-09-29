package com.example.a22100213_proyectointegrador_logarismos.Semantico;

import com.example.a22100213_proyectointegrador_logarismos.NodoAST;

/** Punto único de entrada al análisis para el motor de resolución. */
public final class AnalizadorExpresion {
    private AnalizadorExpresion() {}

    public static InformeAnalisis analizar(NodoAST raiz) {
        ResultadoSemantico rs = AnalisisSemantico.analizar(raiz);
        MetodoResolucion metodo = PlanificadorResolucion.metodo(raiz, rs);
        String plan = PlanificadorResolucion.plan(raiz, rs);
        return new InformeAnalisis(raiz, rs, metodo, plan);
    }

    public static MetodoResolucion siguienteMetodo(NodoAST raiz) {
        return analizar(raiz).metodo;
    }

    /** DTO con todo lo que el motor necesita para decidir el siguiente paso. */
    public static final class InformeAnalisis {
        public final NodoAST raiz;
        public final ResultadoSemantico resultado;
        public final MetodoResolucion metodo;
        public final String plan;

        public boolean hayErrores() {
            return resultado != null && resultado.errores != null && !resultado.errores.isEmpty();
        }

        InformeAnalisis(NodoAST raiz, ResultadoSemantico rs, MetodoResolucion metodo, String plan) {
            this.raiz = raiz;
            this.resultado = rs;
            this.metodo = metodo;
            this.plan = plan;
        }
    }
}
