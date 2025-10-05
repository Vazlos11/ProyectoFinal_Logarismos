package com.example.a22100213_proyectointegrador_logarismos.resolucion;

import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.MetodoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;

public class ProblemContext {
    public final NodoAST raiz;
    public final ResultadoSemantico sem;
    public final MetodoResolucion metodo;
    public ProblemContext(NodoAST r, ResultadoSemantico s, MetodoResolucion m){ this.raiz=r; this.sem=s; this.metodo=m; }
}
