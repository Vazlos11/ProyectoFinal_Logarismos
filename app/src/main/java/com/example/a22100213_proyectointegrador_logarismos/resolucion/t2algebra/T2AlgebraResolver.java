package com.example.a22100213_proyectointegrador_logarismos.resolucion.t2algebra;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;

import java.text.DecimalFormat;

public final class T2AlgebraResolver {
    public enum AngleMode { RADIANS, DEGREES }
    private static AngleMode angleMode = AngleMode.RADIANS;

    private T2AlgebraResolver() {}

    public static void setAngleMode(AngleMode mode) { angleMode = mode; }
    public static void setDegrees() { angleMode = AngleMode.DEGREES; }
    public static void setRadians() { angleMode = AngleMode.RADIANS; }
    public static AngleMode getAngleMode() { return angleMode; }

    public static ResultadoResolucion resolver(NodoAST raiz, ResultadoSemantico rs) {
        ResultadoResolucion rr = new ResultadoResolucion();
        Double v = AstUtils.evalConst(raiz);
        if (v != null) {
            if (angleMode == AngleMode.DEGREES && esInversaTrig(raiz)) {
                double deg = Math.toDegrees(v);
                if (Math.abs(deg) < 1e-12) deg = 0.0;
                rr.resultado = AstUtils.number(deg);
                rr.latexFinal = toTeXGrados(deg);
                rr.pasos.add(new PasoResolucion("\\text{Evaluacion directa (T2, grados)} \\Rightarrow " + rr.latexFinal));
                return rr;
            } else {
                rr.resultado = AstUtils.number(v);
                rr.latexFinal = AstUtils.toTeX(rr.resultado);
                rr.pasos.add(new PasoResolucion("\\text{Evaluacion directa (T2)} \\Rightarrow " + rr.latexFinal));
                return rr;
            }
        }
        rr.resultado = raiz;
        rr.latexFinal = AstUtils.toTeX(raiz);
        rr.pasos.add(new PasoResolucion("\\text{Aun no implementado (T2)} \\Rightarrow " + rr.latexFinal));
        return rr;
    }

    private static boolean esInversaTrig(NodoAST n) {
        if (n == null || n.token == null) return false;
        LexToken.Type t = n.token.type;
        return t == LexToken.Type.TRIG_ARCSIN
                || t == LexToken.Type.TRIG_ARCCOS
                || t == LexToken.Type.TRIG_ARCTAN
                || t == LexToken.Type.TRIG_ARCCOT
                || t == LexToken.Type.TRIG_ARCSEC
                || t == LexToken.Type.TRIG_ARCCSC;
    }

    private static String toTeXGrados(double deg) {
        DecimalFormat df = new DecimalFormat("0.############");
        return df.format(deg) + "^{\\circ}";
    }
}
