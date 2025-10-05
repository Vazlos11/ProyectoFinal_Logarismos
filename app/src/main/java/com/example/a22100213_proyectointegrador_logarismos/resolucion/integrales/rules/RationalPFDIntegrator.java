package com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.rules;

import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.SymjaBridge;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegratorRule;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.rules.PowerRuleIntegrator;

public class RationalPFDIntegrator implements IntegratorRule {
    private final boolean definida;

    public RationalPFDIntegrator(boolean definida) { this.definida = definida; }

    @Override
    public ResultadoResolucion apply(NodoAST raiz, ResultadoSemantico rs) {
        ResultadoResolucion rr = new ResultadoResolucion();
        IntegralUtils.IntegralInfo ii = IntegralUtils.localizarIntegral(raiz, definida);
        if (ii == null || ii.cuerpo == null) return new PowerRuleIntegrator(definida).apply(raiz, rs);
        rr.pasos.add(new PasoResolucion("\\text{Fracciones parciales}"));
        rr.pasos.add(new PasoResolucion("\\text{Forma: } \\frac{A}{x-r}+\\frac{B}{(x-r)^{2}}+\\frac{Cx+D}{x^{2}+px+q}+\\cdots"));
        String s = AstUtils.toSymja(ii.cuerpo);
        String var = ii.var;
        String exSym = !definida ? "Integrate[" + (s == null ? "0" : s) + "," + var + "]" : "Integrate[" + (s == null ? "0" : s) + ",{" + var + "," + AstUtils.toSymja(ii.inf) + "," + AstUtils.toSymja(ii.sup) + "}]";
        rr.resultado = raiz;
        rr.latexFinal = SymjaBridge.toTeX(exSym);
        rr.pasos.add(new PasoResolucion("\\Rightarrow " + rr.latexFinal));
        return rr;
    }
}
