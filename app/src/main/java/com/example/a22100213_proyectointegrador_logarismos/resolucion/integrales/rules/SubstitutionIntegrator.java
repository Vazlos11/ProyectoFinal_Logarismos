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

public final class SubstitutionIntegrator implements IntegratorRule {

    private final boolean definida;

    public SubstitutionIntegrator(boolean definida) {
        this.definida = definida;
    }

    @Override
    public ResultadoResolucion apply(NodoAST raiz, ResultadoSemantico rs) {
        IntegralUtils.IntegralInfo ii = IntegralUtils.localizarIntegral(raiz, definida);
        if (ii == null || ii.cuerpo == null || ii.var == null) return null;

        String x = ii.var;
        ArrayList<PasoResolucion> pasos = new ArrayList<>();

        String intTex = (definida
                ? "\\int_{" + AstUtils.toTeX(ii.inf) + "}^{" + AstUtils.toTeX(ii.sup) + "} "
                : "\\int ") + AstUtils.toTeX(ii.cuerpo) + "\\, d" + x;
        pasos.add(new PasoResolucion("Detección (sustitución)", intTex));

        NodoAST u = IntegralUtils.candidatoInterior(ii.cuerpo);
        if (u == null) return null;

        pasos.add(new PasoResolucion("Elección de sustitución", "u=" + AstUtils.toTeX(u)));

        NodoAST du = IntegralUtils.derivadaSimple(u, x);
        if (du == null) return null;
        pasos.add(new PasoResolucion("Diferencial", "du=" + AstUtils.toTeX(du) + "\\,dx"));

        NodoAST F = IntegralUtils.integralRec(ii.cuerpo, x);
        if (F == null) return null;

        String cuerpoU = AstUtils.toTeX(IntegralUtils.replaceAllEqualByVar(ii.cuerpo, u, "u"));
        pasos.add(new PasoResolucion("Reexpresión del integrando", "\\;f(g(x))g'(x)\\,dx \\;\\Rightarrow\\; f(u)\\,du"));
        pasos.add(new PasoResolucion("Integral en u", "\\int f(u)\\,du \\;\\text{(con } f(u)\\text{ a partir de } " + cuerpoU + ")"));

        if (!definida) {
            String FuTex = AstUtils.toTeX(IntegralUtils.replaceAllEqualByVar(AstUtils.cloneTree(F), u, "u"));
            pasos.add(new PasoResolucion("Antiderivada en u", FuTex + "+C"));
            pasos.add(new PasoResolucion("Retro-sustitución", AstUtils.toTeX(IntegralUtils.addC(AstUtils.cloneTree(F)))));

            NodoAST out = IntegralUtils.addC(F);
            NodoAST nuevaRaiz = IntegralUtils.reemplazar(ii.nodoIntegral, AstUtils.cloneTree(out), ii.padre, out);

            ResultadoResolucion rr = new ResultadoResolucion();
            rr.pasos = pasos;
            rr.resultado = nuevaRaiz;
            rr.latexFinal = AstUtils.toTeX(nuevaRaiz);
            rr.pasos.add(new PasoResolucion(rr.latexFinal));
            return rr;
        } else {
            NodoAST ua = IntegralUtils.sustituirVar(AstUtils.cloneTree(u), x, AstUtils.cloneTree(ii.inf));
            NodoAST ub = IntegralUtils.sustituirVar(AstUtils.cloneTree(u), x, AstUtils.cloneTree(ii.sup));
            pasos.add(new PasoResolucion("Cambio de límites", "a'=" + AstUtils.toTeX(ua) + ",\\; b'=" + AstUtils.toTeX(ub)));

            pasos.add(new PasoResolucion("Integral definida en u",
                    "\\int_{" + AstUtils.toTeX(ua) + "}^{" + AstUtils.toTeX(ub) + "} f(u)\\,du"));

            NodoAST supEval = IntegralUtils.sustituirVar(AstUtils.cloneTree(F), x, ii.sup);
            NodoAST infEval = IntegralUtils.sustituirVar(AstUtils.cloneTree(F), x, ii.inf);
            NodoAST val = IntegralUtils.evalDefinida(F, x, ii.inf, ii.sup);

            pasos.add(new PasoResolucion("F(b)", AstUtils.toTeX(supEval)));
            pasos.add(new PasoResolucion("F(a)", AstUtils.toTeX(infEval)));
            pasos.add(new PasoResolucion("F(b)-F(a)", AstUtils.toTeX(val)));

            NodoAST nuevaRaiz = IntegralUtils.reemplazar(ii.nodoIntegral, AstUtils.cloneTree(val), ii.padre, val);

            ResultadoResolucion rr = new ResultadoResolucion();
            rr.pasos = pasos;
            rr.resultado = nuevaRaiz;
            rr.latexFinal = AstUtils.toTeX(nuevaRaiz);
            rr.pasos.add(new PasoResolucion(rr.latexFinal));
            return rr;
        }
    }
}
