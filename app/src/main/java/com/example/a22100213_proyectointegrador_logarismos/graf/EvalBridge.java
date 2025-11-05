package com.example.a22100213_proyectointegrador_logarismos.graf;

import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.AnalisisSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t2algebra.T2AlgebraResolver;

public final class EvalBridge {
    public static double eval(NodoAST ast, String var, double x, boolean radians) {
        if (ast == null || var == null) return Double.NaN;
        if (radians) T2AlgebraResolver.setRadians(); else T2AlgebraResolver.setDegrees();
        NodoAST nx = AstUtils.number(x);
        NodoAST inst = IntegralUtils.sustituirVar(ast, var, nx);
        Double v = AstUtils.evalConst(inst);
        if (v != null && Double.isFinite(v)) return v;
        ResultadoResolucion rr = com.example.a22100213_proyectointegrador_logarismos.resolucion.t2algebra.T2AlgebraResolver.resolver(inst, AnalisisSemantico.analizar(inst));
        Double v2 = AstUtils.evalConst(rr.resultado);
        return v2 != null && Double.isFinite(v2) ? v2 : Double.NaN;
    }
    private EvalBridge() {}
}
