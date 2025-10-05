package com.example.a22100213_proyectointegrador_logarismos.resolucion;

import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;

public interface Resolver {
    boolean supports(ResultadoSemantico rs);
    ResultadoResolucion resolve(NodoAST raiz, ResultadoSemantico rs);
}
