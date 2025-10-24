package com.example.a22100213_proyectointegrador_logarismos.resolucion.t7polinomios.rules;

import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;

public interface T7Rule {
    String name();
    boolean applies(NodoAST raiz, ResultadoSemantico rs);
    ResultadoResolucion solve(NodoAST raiz, ResultadoSemantico rs);
}
