package com.example.a22100213_proyectointegrador_logarismos.resolucion.t7nolineal;

import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.MetodoResolucion;
import java.util.List;

public interface DespejeResolver {
    boolean matches(NodoAST eq, String var);
    List<String> solve(NodoAST eq, String var);
    MetodoResolucion tag();
}
