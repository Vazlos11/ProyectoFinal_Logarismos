package com.example.a22100213_proyectointegrador_logarismos.resolucion.t3derivadas.rules;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t3derivadas.DerivativeUtils;

public final class PowerRule implements DerivativeRule {
    @Override
    public ResultadoResolucion apply(NodoAST raiz, ResultadoSemantico rs) {
        DerivativeUtils.DerivInfo di = DerivativeUtils.localizarDerivada(raiz);
        ResultadoResolucion rr = new ResultadoResolucion();
        if (di == null || di.fun == null) { rr.resultado = raiz; rr.latexFinal = AstUtils.toTeX(raiz); return rr; }

        String x = di.var;
        NodoAST f = di.fun;
        double a = 1.0;
        NodoAST base = null;
        Double nExp = null;

        if (f.token.type == LexToken.Type.EXP && f.hijos.size() == 2) {
            base = f.hijos.get(0);
            Double n = AstUtils.evalConst(f.hijos.get(1));
            if (DerivativeUtils.esVarPura(base, x) && n != null) { a = 1.0; nExp = n; }
        } else if (f.token.type == LexToken.Type.MUL && f.hijos.size() == 2) {
            NodoAST L = f.hijos.get(0), R = f.hijos.get(1);
            Double cL = AstUtils.evalConst(L), cR = AstUtils.evalConst(R);
            if (cL != null && R.token.type == LexToken.Type.EXP) {
                NodoAST b = R.hijos.get(0); Double n = AstUtils.evalConst(R.hijos.get(1));
                if (DerivativeUtils.esVarPura(b, x) && n != null) { a = cL; base = b; nExp = n; }
            } else if (cR != null && L.token.type == LexToken.Type.EXP) {
                NodoAST b = L.hijos.get(0); Double n = AstUtils.evalConst(L.hijos.get(1));
                if (DerivativeUtils.esVarPura(b, x) && n != null) { a = cR; base = b; nExp = n; }
            }
        }

        if (base == null || nExp == null) { rr.resultado = raiz; rr.latexFinal = AstUtils.toTeX(raiz); return rr; }

        if (rr.pasos == null) rr.pasos = new java.util.ArrayList<>();
        rr.pasos.add(new PasoResolucion("Monomio a·x^{n}", AstUtils.toTeX(f)));

        double coef = a * nExp;
        double newExp = nExp - 1.0;

        NodoAST term;
        if (Math.abs(coef) < 1e-15) {
            term = AstUtils.number(0.0);
        } else if (Math.abs(newExp) < 1e-15) {
            term = AstUtils.number(coef);
        } else {
            term = DerivativeUtils.mul(AstUtils.number(coef), DerivativeUtils.pow(DerivativeUtils.var(x), AstUtils.number(newExp)));
        }

        rr.pasos.add(new PasoResolucion("Regla de potencia", AstUtils.toTeX(term)));

        NodoAST nuevo = IntegralUtils.reemplazarSubexp(raiz, di.nodoDeriv, term);
        rr.resultado = nuevo;
        rr.latexFinal = AstUtils.toTeX(nuevo);
        rr.pasos.add(new PasoResolucion("Sustitución en la expresión", rr.latexFinal));
        return rr;
    }
}
