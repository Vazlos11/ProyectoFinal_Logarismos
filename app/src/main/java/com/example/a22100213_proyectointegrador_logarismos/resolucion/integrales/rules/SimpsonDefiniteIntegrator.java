package com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.rules;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegratorRule;

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
        double val = (b - a) / 6.0 * (f0 + 4.0 * f1 + f2);
        NodoAST out = AstUtils.number(val);
        NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, out, ii.padre, out);
        ResultadoResolucion rr = new ResultadoResolucion();
        rr.resultado = nuevo;
        String aT = AstUtils.toTeX(ii.inf);
        String bT = AstUtils.toTeX(ii.sup);
        String fT = AstUtils.toTeX(ii.cuerpo);
        String step = "\\int_{" + aT + "}^{" + bT + "} " + fT + " \\\\, d" + ii.var + "\\approx \\frac{" + bT + "-" + aT + "}{6}\\left[f(" + aT + ")+4f\\!\\left(\\tfrac{" + aT + "+" + bT + "}{2}\\right)+f(" + bT + ")\\right]";
        rr.pasos.add(new PasoResolucion(step));
        rr.latexFinal = AstUtils.toTeX(nuevo);
        rr.pasos.add(new PasoResolucion(rr.latexFinal));
        return rr;
    }

    private Double evalFx(NodoAST body, String var, double x) {
        NodoAST sub = IntegralUtils.sustituirVar(body, var, AstUtils.number(x));
        return AstUtils.evalConst(sub);
    }
}
