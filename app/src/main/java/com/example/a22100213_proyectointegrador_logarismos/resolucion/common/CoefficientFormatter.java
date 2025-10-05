package com.example.a22100213_proyectointegrador_logarismos.resolucion.common;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;

public final class CoefficientFormatter {
    public static NodoAST applyIfSimpleUnitFraction(NodoAST term, double c){
        long den = maybeIntegerReciprocal(c);
        if (den>1){
            return AstUtils.bin(LexToken.Type.DIV, term, AstUtils.number(den), "/", 6);
        }
        return AstUtils.bin(LexToken.Type.MUL, AstUtils.number(c), term, "*", 6);
    }
    public static long maybeIntegerReciprocal(double c){
        if (c==0.0) return -1;
        double inv = 1.0/Math.abs(c);
        long r = Math.round(inv);
        return Math.abs(inv-r)<1e-12 ? r : -1;
    }
}
