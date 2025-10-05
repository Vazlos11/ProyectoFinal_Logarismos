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

public class PowerRuleIntegrator implements IntegratorRule {
    private final boolean definida;

    public PowerRuleIntegrator(boolean definida) { this.definida = definida; }

    @Override
    public ResultadoResolucion apply(NodoAST raiz, ResultadoSemantico rs) {
        ResultadoResolucion rr = new ResultadoResolucion();
        IntegralUtils.IntegralInfo ii = IntegralUtils.localizarIntegral(raiz, definida);
        if (ii == null || ii.cuerpo == null) {
            rr.resultado = raiz;
            rr.latexFinal = AstUtils.toTeX(raiz);
            rr.pasos.add(new PasoResolucion("\\text{Sin cambio} \\Rightarrow " + rr.latexFinal));
            return rr;
        }
        if (!definida) {
            NodoAST F = IntegralUtils.integralRec(ii.cuerpo, ii.var);
            if (F != null) {
                NodoAST C = AstUtils.atom(LexToken.Type.VARIABLE, "C", 1);
                NodoAST suma = AstUtils.bin(LexToken.Type.SUM, F, C, "+", 5);
                NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, suma, ii.padre, suma);
                rr.resultado = nuevo;
                rr.latexFinal = AstUtils.toTeX(nuevo);
                rr.pasos.add(new PasoResolucion("\\int " + AstUtils.toTeX(ii.cuerpo) + "\\,d" + ii.var + " = " + rr.latexFinal));
                return rr;
            }
            String s = AstUtils.toSymja(ii.cuerpo);
            String r = s == null ? "" : "Integrate[" + s + "," + ii.var + "]";
            rr.resultado = raiz;
            rr.latexFinal = SymjaBridge.toTeX(r);
            rr.pasos.add(new PasoResolucion("\\Rightarrow " + rr.latexFinal));
            return rr;
        } else {
            NodoAST F = IntegralUtils.integralRec(ii.cuerpo, ii.var);
            if (F != null) {
                String Fsym = AstUtils.toSymja(F);
                String a = AstUtils.toSymja(ii.inf);
                String b = AstUtils.toSymja(ii.sup);
                if (Fsym != null && a != null && b != null) {
                    String exSym = "(ReplaceAll[" + Fsym + "," + ii.var + "->(" + b + ")]) - (ReplaceAll[" + Fsym + "," + ii.var + "->(" + a + ")])";
                    rr.resultado = raiz;
                    rr.latexFinal = SymjaBridge.toTeX(exSym);
                    rr.pasos.add(new PasoResolucion("F(" + b + ")-F(" + a + ") = " + rr.latexFinal));
                    return rr;
                }
            }
            String s = AstUtils.toSymja(ii.cuerpo);
            String a = AstUtils.toSymja(ii.inf);
            String b = AstUtils.toSymja(ii.sup);
            String r = s == null ? "" : "Integrate[" + s + ",{" + ii.var + "," + (a == null ? "a" : a) + "," + (b == null ? "b" : b) + "}]";
            rr.resultado = raiz;
            rr.latexFinal = SymjaBridge.toTeX(r);
            rr.pasos.add(new PasoResolucion("\\Rightarrow " + rr.latexFinal));
            return rr;
        }
    }
}
