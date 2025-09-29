package com.example.a22100213_proyectointegrador_logarismos.resolucion;

import com.example.a22100213_proyectointegrador_logarismos.NodoAST;

import java.util.ArrayList;
import java.util.List;

public class ResultadoResolucion {
    public List<PasoResolucion> pasos;
    public NodoAST resultado;
    public String latexFinal;

    public ResultadoResolucion() {
        this.pasos = new ArrayList<>();
        this.resultado = null;
        this.latexFinal = null;
    }
}
