package com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.rules;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegratorRule;

import java.util.ArrayList;

public final class SimpsonDefiniteIntegrator implements IntegratorRule {
    private final boolean definida;

    public SimpsonDefiniteIntegrator() {
        this.definida = true;
    }

    public SimpsonDefiniteIntegrator(boolean definida) {
        this.definida = definida;
    }

    @Override
    public ResultadoResolucion apply(NodoAST raiz, ResultadoSemantico rs) {
        if (!definida) return null;
        IntegralUtils.IntegralInfo ii = IntegralUtils.localizarIntegral(raiz, true);
        if (ii == null || ii.cuerpo == null || ii.var == null || ii.inf == null || ii.sup == null) return null;

        Double a = AstUtils.evalConst(ii.inf);
        Double b = AstUtils.evalConst(ii.sup);
        if (a == null || b == null) return null;

        Double f0 = evalFx(ii.cuerpo, ii.var, a);
        Double f2 = evalFx(ii.cuerpo, ii.var, b);
        if (f0 == null || f2 == null) return null;

        double xm = 0.5 * (a + b);
        Double f1 = evalFx(ii.cuerpo, ii.var, xm);
        if (f1 == null) return null;

        double h = (b - a) / 2.0;
        double val = (h / 3.0) * (f0 + 4.0 * f1 + f2);

        NodoAST out = AstUtils.number(val);
        NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, out, ii.padre, out);

        ResultadoResolucion rr = new ResultadoResolucion();
        rr.pasos = new ArrayList<>();
        rr.resultado = nuevo;

        String aT = AstUtils.toTeX(ii.inf);
        String bT = AstUtils.toTeX(ii.sup);
        String x1T = "\\tfrac{" + aT + "+" + bT + "}{2}";
        String fT = AstUtils.toTeX(ii.cuerpo);
        String var = ii.var;

        rr.pasos.add(new PasoResolucion("Detección", "\\int_{" + aT + "}^{" + bT + "} " + fT + "\\, d" + var));
        rr.pasos.add(new PasoResolucion("Selección del método", "\\text{Regla de Simpson }\\tfrac{1}{3}\\text{ con }n=2"));
        rr.pasos.add(new PasoResolucion("Tamaño de subintervalo", "h=\\frac{" + bT + "-" + aT + "}{2}"));
        rr.pasos.add(new PasoResolucion("Puntos de evaluación", "x_0=" + aT + ",\\; x_1=" + x1T + ",\\; x_2=" + bT));

        rr.pasos.add(new PasoResolucion("Evaluaciones",
                "f(x_0)=" + trimNum(f0) + ",\\; f(x_1)=" + trimNum(f1) + ",\\; f(x_2)=" + trimNum(f2)));

        rr.pasos.add(new PasoResolucion("Fórmula de Simpson",
                "\\int_{" + aT + "}^{" + bT + "} f(x)\\,dx\\approx \\frac{h}{3}\\,[f(x_0)+4f(x_1)+f(x_2)]"));

        rr.pasos.add(new PasoResolucion("Sustitución numérica",
                "\\frac{" + "\\frac{" + bT + "-" + aT + "}{2}" + "}{3}\\,\\left(" + trimNum(f0) + "+4\\cdot" + trimNum(f1) + "+" + trimNum(f2) + "\\right)"));

        rr.latexFinal = AstUtils.toTeX(nuevo);
        rr.pasos.add(new PasoResolucion("Resultado aproximado", rr.latexFinal));

        return rr;
    }

    private Double evalFx(NodoAST body, String var, double x) {
        NodoAST sub = IntegralUtils.sustituirVar(body, var, AstUtils.number(x));
        return AstUtils.evalConst(sub);
    }

    private static String trimNum(double v) {
        String s = Double.toString(v);
        if (s.endsWith(".0")) s = s.substring(0, s.length() - 2);
        return s;
    }
}
