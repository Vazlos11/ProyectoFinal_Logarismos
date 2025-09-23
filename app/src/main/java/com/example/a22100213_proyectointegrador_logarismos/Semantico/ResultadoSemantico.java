package com.example.a22100213_proyectointegrador_logarismos.Semantico;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ResultadoSemantico {
    public TipoExpresion tipoPrincipal;
    public Set<TipoExpresion> subtipos;
    public List<SemanticoError> errores;

    public ResultadoSemantico() {
        this.tipoPrincipal = TipoExpresion.T2_ALGEBRA_FUNC;
        this.subtipos = new LinkedHashSet<>();
        this.errores = new ArrayList<>();
    }
}
