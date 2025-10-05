package com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.rules;

import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.SymjaBridge;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegratorRule;

public class SimpsonDefiniteIntegrator implements IntegratorRule {
    @Override
    public ResultadoResolucion apply(NodoAST raiz, ResultadoSemantico rs) {
        ResultadoResolucion rr = new ResultadoResolucion();
        IntegralUtils.IntegralInfo ii = IntegralUtils.localizarIntegral(raiz, true);
        if (ii == null || ii.cuerpo == null) return new PowerRuleIntegrator(true).apply(raiz, rs);
        String f = AstUtils.toSymja(ii.cuerpo);
        String a = AstUtils.toSymja(ii.inf);
        String b = AstUtils.toSymja(ii.sup);
        rr.pasos.add(new PasoResolucion("\\text{Regla de Simpson}"));
        rr.pasos.add(new PasoResolucion("S_{n}=\\tfrac{h}{3}[f(x_{0})+4\\sum f(x_{2k-1})+2\\sum f(x_{2k})+f(x_{n})]"));
        rr.pasos.add(new PasoResolucion("h=\\tfrac{" + SymjaBridge.toTeX(b) + "-" + SymjaBridge.toTeX(a) + "}{n}"));
        rr.pasos.add(new PasoResolucion("f(x)=" + SymjaBridge.toTeX(f)));
        String approx = "Simpson[" + (f == null ? "0" : f) + ",{" + ii.var + "," + (a == null ? "a" : a) + "," + (b == null ? "b" : b) + "},n]";
        rr.resultado = raiz;
        rr.latexFinal = SymjaBridge.toTeX(approx);
        rr.pasos.add(new PasoResolucion("\\Rightarrow " + rr.latexFinal));
        return rr;
    }
}
