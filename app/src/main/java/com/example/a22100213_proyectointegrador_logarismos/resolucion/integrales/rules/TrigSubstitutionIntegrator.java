package com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.rules;

import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.SymjaBridge;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegratorRule;

public class TrigSubstitutionIntegrator implements IntegratorRule {
    private final boolean definida;

    public TrigSubstitutionIntegrator(boolean definida) { this.definida = definida; }

    @Override
    public ResultadoResolucion apply(NodoAST raiz, ResultadoSemantico rs) {
        ResultadoResolucion rr = new ResultadoResolucion();
        IntegralUtils.IntegralInfo ii = IntegralUtils.localizarIntegral(raiz, definida);
        if (ii == null || ii.cuerpo == null) return new PowerRuleIntegrator(definida).apply(raiz, rs);
        rr.pasos.add(new PasoResolucion("\\text{Sustitucion trigonometrica}"));
        rr.pasos.add(new PasoResolucion("x=a\\sin\\theta \\Rightarrow dx=a\\cos\\theta\\,d\\theta"));
        rr.pasos.add(new PasoResolucion("x=a\\tan\\theta \\Rightarrow dx=a\\sec^{2}\\theta\\,d\\theta"));
        rr.pasos.add(new PasoResolucion("x=a\\sec\\theta \\Rightarrow dx=a\\sec\\theta\\tan\\theta\\,d\\theta"));
        String s = AstUtils.toSymja(ii.cuerpo);
        String var = ii.var;
        String exSym = !definida ? "Integrate[" + (s == null ? "0" : s) + "," + var + "]" : "Integrate[" + (s == null ? "0" : s) + ",{" + var + "," + AstUtils.toSymja(ii.inf) + "," + AstUtils.toSymja(ii.sup) + "}]";
        rr.resultado = raiz;
        rr.latexFinal = SymjaBridge.toTeX(exSym);
        rr.pasos.add(new PasoResolucion("\\Rightarrow " + rr.latexFinal));
        return rr;
    }
}
