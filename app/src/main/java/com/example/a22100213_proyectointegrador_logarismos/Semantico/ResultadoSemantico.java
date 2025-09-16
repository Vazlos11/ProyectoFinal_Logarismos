package com.example.a22100213_proyectointegrador_logarismos.Semantico;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ResultadoSemantico {
    public TipoExpresion tipoPrincipal;
    public Set<TipoExpresion> subtipos = new LinkedHashSet<>();
    public List<SemanticoError> errores = new ArrayList<>();
    public List<SemanticoError> advertencias = new ArrayList<>();
}
