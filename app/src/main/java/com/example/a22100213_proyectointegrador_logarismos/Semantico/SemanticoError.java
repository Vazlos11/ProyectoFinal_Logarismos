package com.example.a22100213_proyectointegrador_logarismos.Semantico;

import java.util.ArrayList;
import java.util.List;

public class SemanticoError {
    public enum Severidad { ERROR, ADVERTENCIA }

    public String codigo;
    public String mensaje;
    public Severidad severidad;
    public List<Integer> ruta;

    public SemanticoError(String codigo, String mensaje, Severidad severidad, List<Integer> ruta) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.severidad = severidad;
        this.ruta = (ruta == null) ? new ArrayList<>() : new ArrayList<>(ruta);
    }

    public static SemanticoError error(String codigo, String mensaje, List<Integer> ruta) {
        return new SemanticoError(codigo, mensaje, Severidad.ERROR, ruta);
    }

    public static SemanticoError warn(String codigo, String mensaje, List<Integer> ruta) {
        return new SemanticoError(codigo, mensaje, Severidad.ADVERTENCIA, ruta);
    }
}
