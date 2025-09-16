package com.example.a22100213_proyectointegrador_logarismos.Semantico;

import java.util.ArrayList;
import java.util.List;

public class SemanticoError {
    public String codigo;
    public String mensaje;
    public String severidad;
    public List<String> ruta = new ArrayList<>();

    public SemanticoError() {}

    public SemanticoError(String codigo, String mensaje, String severidad) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.severidad = severidad;
    }
}
