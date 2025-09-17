package com.example.a22100213_proyectointegrador_logarismos.Semantico;

import java.util.List;

public class SemanticoError {
    public enum Severidad { ADVERTENCIA, ERROR }

    public final String codigo;
    public final String mensaje;
    public final Severidad severidad;
    public final List<Integer> ruta;

    public SemanticoError(String codigo, String mensaje, Severidad severidad, List<Integer> ruta) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.severidad = severidad;
        this.ruta = ruta;
    }

    public static SemanticoError error(String codigo, String mensaje, List<Integer> ruta) {
        return new SemanticoError(codigo, mensaje, Severidad.ERROR, ruta);
    }

    public static SemanticoError advertencia(String codigo, String mensaje, List<Integer> ruta) {
        return new SemanticoError(codigo, mensaje, Severidad.ADVERTENCIA, ruta);
    }

    @Override
    public String toString() {
        return severidad + " " + codigo + ": " + mensaje +
                (ruta != null && !ruta.isEmpty() ? " @" + ruta.toString() : "");
    }
}
