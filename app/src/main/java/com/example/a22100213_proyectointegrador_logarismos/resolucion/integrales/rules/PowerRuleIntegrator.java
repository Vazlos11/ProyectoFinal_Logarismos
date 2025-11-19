package com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.rules;

import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegratorRule;
import com.example.a22100213_proyectointegrador_logarismos.LexToken;

import java.util.ArrayList;

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

        ArrayList<PasoResolucion> pasos = new ArrayList<>();

        NodoAST cuerpo0 = ii.cuerpo;
        NodoAST cuerpo = IntegralUtils.foldEConstTimesXIntoExpLinear(cuerpo0, x);
        if (cuerpo != cuerpo0) pasos.add(new PasoResolucion("Normalización exponencial lineal", AstUtils.toTeX(cuerpo)));

        IntegralUtils.Poly p = IntegralUtils.esMonomioEn(cuerpo, x);
        boolean inverse = IntegralUtils.esInversa(cuerpo, x);
        IntegralUtils.MulSplit ms = IntegralUtils.splitMul(cuerpo);

        Double aCoef = null;
        Integer nExp = null;

        if (p != null && (p.resto == null || AstUtils.isConst(p.resto))) {
            aCoef = p.coef;
            nExp = p.grado;
        } else if (inverse) {
            aCoef = 1.0;
            nExp = -1;
        } else if (ms != null && ms.nonconst.size() == 1 && IntegralUtils.esInversa(ms.nonconst.get(0), x)) {
            aCoef = ms.c;
            nExp = -1;
        }

        if (aCoef == null || nExp == null) return null;

        String integrandoTex = AstUtils.toTeX(cuerpo);
        pasos.add(new PasoResolucion("Detección", "\\int " + integrandoTex + "\\,d" + x));
        pasos.add(new PasoResolucion("Extracción de monomio", "a=" + trimNum(aCoef) + ",\\; n=" + nExp));

        NodoAST F;
        if (nExp == -1) {
            pasos.add(new PasoResolucion("Caso n=-1 (logarítmica)", "\\int \\frac{a}{x}\\,dx=a\\,\\ln(x)"));
            NodoAST ln = AstUtils.un(LexToken.Type.LN, AstUtils.atom(LexToken.Type.VARIABLE, x, 1), "ln", 9);
            F = IntegralUtils.mulC(ln, aCoef);
        } else {
            pasos.add(new PasoResolucion("Regla de potencia", "\\int a\\,x^{n}\\,dx=\\frac{a}{n+1}\\,x^{n+1}"));
            double c = aCoef / (nExp + 1.0);
            NodoAST xnp1 = IntegralUtils.xPow(x, nExp + 1);
            F = IntegralUtils.mulC(xnp1, c);
        }
        pasos.add(new PasoResolucion("Construcción de F(x)", AstUtils.toTeX(F)));

        if (!definida) {
            NodoAST fin = IntegralUtils.addC(AstUtils.cloneTree(F));
            pasos.add(new PasoResolucion("Antiderivada", AstUtils.toTeX(fin)));
            NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, AstUtils.cloneTree(fin), ii.padre, fin);
            ResultadoResolucion rr = new ResultadoResolucion();
            rr.pasos = pasos;
            rr.resultado = nuevo;
            rr.latexFinal = AstUtils.toTeX(nuevo);
            rr.pasos.add(new PasoResolucion(rr.latexFinal));
            return rr;
        } else {
            NodoAST supEval = IntegralUtils.sustituirVar(AstUtils.cloneTree(F), x, ii.sup);
            NodoAST infEval = IntegralUtils.sustituirVar(AstUtils.cloneTree(F), x, ii.inf);
            NodoAST val = IntegralUtils.evalDefinida(F, x, ii.inf, ii.sup);
            pasos.add(new PasoResolucion("F(b)", AstUtils.toTeX(supEval)));
            pasos.add(new PasoResolucion("F(a)", AstUtils.toTeX(infEval)));
            pasos.add(new PasoResolucion("F(b) - F(a)", AstUtils.toTeX(val)));
            NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, AstUtils.cloneTree(val), ii.padre, val);
            ResultadoResolucion rr = new ResultadoResolucion();
            rr.pasos = pasos;
            rr.resultado = nuevo;
            rr.latexFinal = AstUtils.toTeX(nuevo);
            rr.pasos.add(new PasoResolucion(rr.latexFinal));
            return rr;
        }
    }

    private static String trimNum(double v) {
        String s = Double.toString(v);
        if (s.endsWith(".0")) s = s.substring(0, s.length() - 2);
        return s;
    }
}
