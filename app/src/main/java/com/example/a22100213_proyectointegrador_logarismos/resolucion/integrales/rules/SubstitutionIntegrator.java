package com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.rules;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.SymjaBridge;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegratorRule;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.rules.PowerRuleIntegrator;

public class SubstitutionIntegrator implements IntegratorRule {
    private final boolean definida;

    public SubstitutionIntegrator(boolean definida) { this.definida = definida; }

    @Override
    public ResultadoResolucion apply(NodoAST raiz, ResultadoSemantico rs) {
        ResultadoResolucion rr = new ResultadoResolucion();
        IntegralUtils.IntegralInfo ii = IntegralUtils.localizarIntegral(raiz, definida);
        if (ii == null || ii.cuerpo == null) return new PowerRuleIntegrator(definida).apply(raiz, rs);
        NodoAST g = IntegralUtils.candidatoInterior(ii.cuerpo);
        if (g == null) return new PowerRuleIntegrator(definida).apply(raiz, rs);
        String var = ii.var;
        String uSym = AstUtils.toSymja(g);
        String duSym = "D[" + (uSym == null ? "u" : uSym) + "," + var + "]";
        rr.pasos.add(new PasoResolucion("\\text{Sustitucion}"));
        rr.pasos.add(new PasoResolucion("u=" + SymjaBridge.toTeX(uSym)));
        rr.pasos.add(new PasoResolucion("du=" + SymjaBridge.toTeX(duSym)));
        String fSym = AstUtils.toSymja(IntegralUtils.reemplazarSubexp(ii.cuerpo, g, AstUtils.atom(LexToken.Type.VARIABLE, "u", 1)));
        String resSym;
        if (!definida) {
            resSym = "Integrate[" + (fSym == null ? "0" : fSym) + ",u]";
        } else {
            String a = AstUtils.toSymja(ii.inf);
            String b = AstUtils.toSymja(ii.sup);
            String au = "(" + "ReplaceAll[" + (uSym == null ? "u" : uSym) + "," + var + "->(" + (a == null ? "a" : a) + ")]" + ")";
            String bu = "(" + "ReplaceAll[" + (uSym == null ? "u" : uSym) + "," + var + "->(" + (b == null ? "b" : b) + ")]" + ")";
            resSym = "Integrate[" + (fSym == null ? "0" : fSym) + ",{u," + au + "," + bu + "}]";
        }
        rr.resultado = raiz;
        rr.latexFinal = SymjaBridge.toTeX(resSym);
        rr.pasos.add(new PasoResolucion("\\Rightarrow " + rr.latexFinal));
        return rr;
    }
}
