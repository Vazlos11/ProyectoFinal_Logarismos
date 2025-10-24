package com.example.a22100213_proyectointegrador_logarismos.resolucion.t6lineal.rules;

import com.example.a22100213_proyectointegrador_logarismos.NodoAST;

public final class AislacionDirectaMatch {
    public final boolean ok;
    public final NodoAST a;
    public final NodoAST x;
    public final NodoAST b;
    public final NodoAST c;
    public final NodoAST axTerm;

    public AislacionDirectaMatch(boolean ok, NodoAST a, NodoAST x, NodoAST b, NodoAST c, NodoAST axTerm) {
        this.ok = ok; this.a = a; this.x = x; this.b = b; this.c = c; this.axTerm = axTerm;
    }

    public static AislacionDirectaMatch fail() {
        return new AislacionDirectaMatch(false, null, null, null, null, null);
    }
}
