package com.example.a22100213_proyectointegrador_logarismos.resolucion.t6lineal.rules;

import com.example.a22100213_proyectointegrador_logarismos.NodoAST;

public final class AislacionAx {
    public final boolean ok;
    public final NodoAST a;
    public final NodoAST x;
    public final NodoAST axTerm;

    public AislacionAx(boolean ok, NodoAST a, NodoAST x, NodoAST axTerm) {
        this.ok = ok; this.a = a; this.x = x; this.axTerm = axTerm;
    }

    public static AislacionAx fail() {
        return new AislacionAx(false, null, null, null);
    }
}
