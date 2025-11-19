package com.example.a22100213_proyectointegrador_logarismos.resolucion.t2algebra;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.MotorResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.SymjaBridge;

import java.text.DecimalFormat;
import java.util.List;

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
        if (rr.pasos == null) rr.pasos = new java.util.ArrayList<>();
        String before = AstUtils.toTeX(raiz);

        if (!contieneVariable(raiz)) {
            Double v = AstUtils.evalConst(raiz);
            if (v != null && Double.isFinite(v)) {
                double w = (v == 0.0 ? 0.0 : v);
                if (angleMode == AngleMode.DEGREES && esInversaTrig(raiz)) {
                    w = Math.toDegrees(w);
                    rr.resultado = AstUtils.number(w);
                    rr.latexFinal = toTeXGrados(w);
                } else {
                    rr.resultado = AstUtils.number(w);
                    rr.latexFinal = AstUtils.toTeX(rr.resultado);
                }
                rr.pasos.add(new PasoResolucion("Evaluación algebraica", rr.latexFinal));
                return rr;
            }
        }

        NodoAST simpl = SymjaBridge.simplify(raiz);
        if (simpl != null && simpl != raiz) {
            String after = AstUtils.toTeX(simpl);
            if (!after.equals(before)) {
                rr.pasos.add(new PasoResolucion("Simplificación (T2)", after));
                ResultadoResolucion sub = MotorResolucion.resolver(
                        simpl,
                        com.example.a22100213_proyectointegrador_logarismos.Semantico.AnalisisSemantico.analizar(simpl)
                );
                if (sub.pasos != null) rr.pasos.addAll(sub.pasos);
                rr.resultado = sub.resultado;
                rr.latexFinal = sub.latexFinal;
                return rr;
            }
        }

        if (!contieneVariable(raiz)) {
            Double v2 = AstUtils.evalConst(raiz);
            if (v2 != null && Double.isFinite(v2)) {
                double w2 = (v2 == 0.0 ? 0.0 : v2);
                if (angleMode == AngleMode.DEGREES && esInversaTrig(raiz)) {
                    w2 = Math.toDegrees(w2);
                    rr.resultado = AstUtils.number(w2);
                    rr.latexFinal = toTeXGrados(w2);
                } else {
                    rr.resultado = AstUtils.number(w2);
                    rr.latexFinal = AstUtils.toTeX(rr.resultado);
                }
                rr.pasos.add(new PasoResolucion("Evaluación algebraica", rr.latexFinal));
                return rr;
            }
        }

        rr.resultado = raiz;
        rr.latexFinal = before;
        rr.pasos.add(new PasoResolucion("Sin cambio (T2)", rr.latexFinal));
        return rr;
    }

    private static boolean contieneVariable(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.VARIABLE) return true;
        List<NodoAST> hijos = n.hijos;
        if (hijos != null) for (NodoAST h : hijos) if (contieneVariable(h)) return true;
        return false;
    }

    private static boolean esInversaTrig(NodoAST n) {
        if (n == null || n.token == null) return false;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.TRIG_ARCSIN
                || t == LexToken.Type.TRIG_ARCCOS
                || t == LexToken.Type.TRIG_ARCTAN
                || t == LexToken.Type.TRIG_ARCCOT
                || t == LexToken.Type.TRIG_ARCSEC
                || t == LexToken.Type.TRIG_ARCCSC) return true;
        if (n.hijos != null) for (NodoAST h : n.hijos) if (esInversaTrig(h)) return true;
        return false;
    }

    private static String toTeXGrados(double deg) {
        DecimalFormat df = new DecimalFormat("0.############");
        return df.format(deg) + "^{\\circ}";
    }
}
