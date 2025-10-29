package com.example.a22100213_proyectointegrador_logarismos.resolucion.common;

import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.format.ExpressionFormatter;

public final class CoefficientFormatter {
    public static NodoAST applyIfSimpleUnitFraction(NodoAST term, double c){
        return ExpressionFormatter.applyIfSimpleUnitFraction(term, c);
    }
    public static long maybeIntegerReciprocal(double c){
        return ExpressionFormatter.maybeIntegerReciprocal(c);
    }
}
