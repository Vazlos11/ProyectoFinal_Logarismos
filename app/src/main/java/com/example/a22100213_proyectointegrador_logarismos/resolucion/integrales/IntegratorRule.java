package com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales;

import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;

public interface IntegratorRule {
    ResultadoResolucion apply(NodoAST raiz, ResultadoSemantico rs);
}
