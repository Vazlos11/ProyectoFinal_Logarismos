package com.example.a22100213_proyectointegrador_logarismos.resolucion;

public class PasoResolucion {
    public String descripcion;
    public String latex;

    public PasoResolucion(String descripcion, String latex) {
        this.descripcion = descripcion == null ? "" : descripcion.trim();
        this.latex = latex == null ? "" : latex.trim();
    }

    public PasoResolucion(String latex) {
        this("", latex);
    }
}
