package com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.rules;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegratorRule;

public final class PowerRuleIntegrator implements IntegratorRule {
    private final boolean definida;

    public PowerRuleIntegrator(boolean definida) {
        this.definida = definida;
    }

    @Override
    public ResultadoResolucion apply(NodoAST raiz, ResultadoSemantico rs) {
        IntegralUtils.IntegralInfo ii = IntegralUtils.localizarIntegral(raiz, definida);
        if (ii == null || ii.cuerpo == null || ii.var == null) return null;
        String x = ii.var;

        IntegralUtils.MulSplit ms = IntegralUtils.splitMul(ii.cuerpo);
        if (ms == null) return null;
        double a = ms.c;

        if (ms.nonconst.isEmpty()) {
            NodoAST F = AstUtils.bin(LexToken.Type.MUL, AstUtils.number(a), AstUtils.atom(LexToken.Type.VARIABLE, x, 1), "*", 6);
            NodoAST out = definida ? IntegralUtils.evalDefinida(F, x, ii.inf, ii.sup) : IntegralUtils.addC(F);
            NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, AstUtils.cloneTree(out), ii.padre, out);

            ResultadoResolucion rr = new ResultadoResolucion();
            rr.resultado = nuevo;
            rr.latexFinal = AstUtils.toTeX(nuevo);
            rr.pasos.add(new PasoResolucion(definida ? "\\int a\\,x^{0}\\,dx= a\\,x" : "\\int a\\,x^{0}\\,dx= a\\,x + C"));
            if (definida) {
                NodoAST Fb = IntegralUtils.sustituirVar(AstUtils.cloneTree(F), x, AstUtils.cloneTree(ii.sup));
                NodoAST Fa = IntegralUtils.sustituirVar(AstUtils.cloneTree(F), x, AstUtils.cloneTree(ii.inf));
                rr.pasos.add(new PasoResolucion(AstUtils.toTeX(Fb)));
                rr.pasos.add(new PasoResolucion(AstUtils.toTeX(Fa)));
                rr.pasos.add(new PasoResolucion(AstUtils.toTeX(out)));
            }
            rr.pasos.add(new PasoResolucion(rr.latexFinal));
            return rr;
        }

        if (ms.nonconst.size() != 1) return null;
        NodoAST t = ms.nonconst.get(0);

        Double n = null;
        if (t.token != null && t.token.type == LexToken.Type.VARIABLE && x.equals(t.token.value)) {
            n = 1.0;
        } else if (t.token != null && t.token.type == LexToken.Type.EXP && t.hijos.size() == 2) {
            if (t.hijos.get(0) != null && t.hijos.get(0).token != null
                    && t.hijos.get(0).token.type == LexToken.Type.VARIABLE
                    && x.equals(t.hijos.get(0).token.value)) {
                Double e = AstUtils.evalConst(t.hijos.get(1));
                if (e != null) n = e;
            }
        } else return null;

        NodoAST F;
        if (Math.abs(n + 1.0) < 1e-15) {
            NodoAST vx = AstUtils.atom(LexToken.Type.VARIABLE, x, 1);
            NodoAST ln = IntegralUtils.lnClone(vx);
            F = AstUtils.bin(LexToken.Type.MUL, AstUtils.number(a), ln, "*", 6);
        } else {
            double coef = a / (n + 1.0);
            double expNext = n + 1.0;
            NodoAST vx = AstUtils.atom(LexToken.Type.VARIABLE, x, 1);
            NodoAST xPow = AstUtils.bin(LexToken.Type.EXP, vx, AstUtils.number(expNext), "^", 7);
            F = AstUtils.bin(LexToken.Type.MUL, AstUtils.number(coef), xPow, "*", 6);
        }

        NodoAST out = definida ? IntegralUtils.evalDefinida(F, x, ii.inf, ii.sup) : IntegralUtils.addC(F);
        NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, AstUtils.cloneTree(out), ii.padre, out);

        ResultadoResolucion rr = new ResultadoResolucion();
        rr.resultado = nuevo;
        rr.latexFinal = AstUtils.toTeX(nuevo);
        if (Math.abs(n + 1.0) < 1e-15) {
            rr.pasos.add(new PasoResolucion(definida ? "\\int a\\,x^{-1}\\,dx= a\\,\\ln|x|" : "\\int a\\,x^{-1}\\,dx= a\\,\\ln|x| + C"));
        } else {
            rr.pasos.add(new PasoResolucion(definida ? "\\int a\\,x^{n}\\,dx= \\frac{a}{n+1}x^{n+1}" : "\\int a\\,x^{n}\\,dx= \\frac{a}{n+1}x^{n+1}+C"));
        }
        if (definida) {
            NodoAST Fb = IntegralUtils.sustituirVar(AstUtils.cloneTree(F), x, AstUtils.cloneTree(ii.sup));
            NodoAST Fa = IntegralUtils.sustituirVar(AstUtils.cloneTree(F), x, AstUtils.cloneTree(ii.inf));
            rr.pasos.add(new PasoResolucion(AstUtils.toTeX(Fb)));
            rr.pasos.add(new PasoResolucion(AstUtils.toTeX(Fa)));
            rr.pasos.add(new PasoResolucion(AstUtils.toTeX(out)));
        }
        rr.pasos.add(new PasoResolucion(rr.latexFinal));
        return rr;
    }
}
