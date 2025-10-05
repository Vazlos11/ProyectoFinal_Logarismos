package com.example.a22100213_proyectointegrador_logarismos.resolucion;

import java.util.Arrays;
import java.util.List;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.aritmetica.AritmeticaResolver;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralResolver;

public class ResolutionEngine {
    private final List<Resolver> resolvers = Arrays.asList(
            new AritmeticaResolver(),
            new IntegralResolver()
    );

    public ResultadoResolucion resolve(NodoAST raiz, ResultadoSemantico rs) {
        for (Resolver r : resolvers) {
            if (r.supports(rs)) return r.resolve(raiz, rs);
        }
        ResultadoResolucion out = new ResultadoResolucion();
        out.resultado = raiz;
        out.latexFinal = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.toTeX(raiz);
        out.pasos.add(new PasoResolucion("\\text{Sin cambio} \\Rightarrow " + out.latexFinal));
        return out;
    }
}
