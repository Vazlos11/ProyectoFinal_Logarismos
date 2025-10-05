package com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.rules;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.Acc;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegratorRule;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.rules.PowerRuleIntegrator;

public class ByPartsExpPolyIntegrator implements IntegratorRule {
    private final boolean definida;

    public ByPartsExpPolyIntegrator(boolean definida) { this.definida = definida; }

    @Override
    public ResultadoResolucion apply(NodoAST raiz, ResultadoSemantico rs) {
        IntegralUtils.IntegralInfo ii = IntegralUtils.localizarIntegral(raiz, definida);
        if (ii == null || ii.cuerpo == null) return new PowerRuleIntegrator(definida).apply(raiz, rs);
        String x = ii.var;

        IntegralUtils.MonoExpLin mel = matchMonomialTimesExpLinear(ii.cuerpo, x);
        if (mel != null && Math.abs(mel.k) > 1e-15) {
            NodoAST res = integrarReduccionExpIter(mel.n, mel.coef, mel.u, mel.k, x);
            if (!definida) {
                NodoAST C = AstUtils.atom(LexToken.Type.VARIABLE, "C", 1);
                res = AstUtils.bin(LexToken.Type.SUM, res, C, "+", 5);
            }
            NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, res, ii.padre, res);
            ResultadoResolucion rr = new ResultadoResolucion();
            rr.resultado = nuevo;
            rr.latexFinal = AstUtils.toTeX(nuevo);
            rr.pasos.add(new PasoResolucion("\\text{Integracion por partes} \\Rightarrow " + rr.latexFinal));
            return rr;
        }

        IntegralUtils.MonoTrigLin msl = matchMonomialTimesSinLinear(ii.cuerpo, x);
        if (msl != null && Math.abs(msl.k) > 1e-15) {
            NodoAST res = integrarReduccionSin(msl.n, msl.coef, msl.u, msl.k, x);
            if (!definida) {
                NodoAST C = AstUtils.atom(LexToken.Type.VARIABLE, "C", 1);
                res = AstUtils.bin(LexToken.Type.SUM, res, C, "+", 5);
            }
            NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, res, ii.padre, res);
            ResultadoResolucion rr = new ResultadoResolucion();
            rr.resultado = nuevo;
            rr.latexFinal = AstUtils.toTeX(nuevo);
            rr.pasos.add(new PasoResolucion("\\text{Integracion por partes} \\Rightarrow " + rr.latexFinal));
            return rr;
        }

        IntegralUtils.MonoTrigLin mcl = matchMonomialTimesCosLinear(ii.cuerpo, x);
        if (mcl != null && Math.abs(mcl.k) > 1e-15) {
            NodoAST res = integrarReduccionCos(mcl.n, mcl.coef, mcl.u, mcl.k, x);
            if (!definida) {
                NodoAST C = AstUtils.atom(LexToken.Type.VARIABLE, "C", 1);
                res = AstUtils.bin(LexToken.Type.SUM, res, C, "+", 5);
            }
            NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, res, ii.padre, res);
            ResultadoResolucion rr = new ResultadoResolucion();
            rr.resultado = nuevo;
            rr.latexFinal = AstUtils.toTeX(nuevo);
            rr.pasos.add(new PasoResolucion("\\text{Integracion por partes} \\Rightarrow " + rr.latexFinal));
            return rr;
        }

        if (IntegralUtils.esCicloExpTrig(ii.cuerpo, x)) {
            NodoAST res = IntegralUtils.resolverCicloExpTrig(ii.cuerpo, x);
            if (!definida) {
                NodoAST C = AstUtils.atom(LexToken.Type.VARIABLE, "C", 1);
                res = AstUtils.bin(LexToken.Type.SUM, res, C, "+", 5);
            }
            NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, res, ii.padre, res);
            ResultadoResolucion rr = new ResultadoResolucion();
            rr.resultado = nuevo;
            rr.latexFinal = AstUtils.toTeX(nuevo);
            rr.pasos.add(new PasoResolucion("\\text{Integracion por partes} \\Rightarrow " + rr.latexFinal));
            return rr;
        }

        if (IntegralUtils.esLnPorMonomio(ii.cuerpo, x)) {
            NodoAST res = IntegralUtils.integrarLnPorMonomio(ii.cuerpo, x);
            if (res != null) {
                if (!definida) {
                    NodoAST C = AstUtils.atom(LexToken.Type.VARIABLE, "C", 1);
                    res = AstUtils.bin(LexToken.Type.SUM, res, C, "+", 5);
                }
                NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, res, ii.padre, res);
                ResultadoResolucion rr = new ResultadoResolucion();
                rr.resultado = nuevo;
                rr.latexFinal = AstUtils.toTeX(nuevo);
                rr.pasos.add(new PasoResolucion("\\text{Integracion por partes} \\Rightarrow " + rr.latexFinal));
                return rr;
            }
        }

        return new PowerRuleIntegrator(definida).apply(raiz, rs);
    }

    private IntegralUtils.MonoExpLin matchMonomialTimesExpLinear(NodoAST n, String v) {
        Acc acc = IntegralUtils.collectForParts(n, v);
        if (acc == null) return null;
        if (acc.countExpLin != 1) return null;
        IntegralUtils.MonoExpLin out = new IntegralUtils.MonoExpLin();
        out.n = acc.deg;
        out.coef = acc.constCoef;
        out.u = acc.uExp;
        out.k = IntegralUtils.linearCoeff(acc.uExp, v) == null ? 0.0 : IntegralUtils.linearCoeff(acc.uExp, v);
        return out;
    }
    private IntegralUtils.MonoTrigLin matchMonomialTimesSinLinear(NodoAST n, String v) {
        Acc acc = IntegralUtils.collectForParts(n, v);
        if (acc == null || acc.countSinLin != 1 || acc.countCosLin != 0 || acc.countExpLin != 0) return null;
        IntegralUtils.MonoTrigLin out = new IntegralUtils.MonoTrigLin();
        out.n = acc.deg;
        out.coef = acc.constCoef;
        out.u = acc.uSin;
        out.k = IntegralUtils.linearCoeff(acc.uSin, v) == null ? 0.0 : IntegralUtils.linearCoeff(acc.uSin, v);
        return out;
    }
    private IntegralUtils.MonoTrigLin matchMonomialTimesCosLinear(NodoAST n, String v) {
        Acc acc = IntegralUtils.collectForParts(n, v);
        if (acc == null || acc.countCosLin != 1 || acc.countSinLin != 0 || acc.countExpLin != 0) return null;
        IntegralUtils.MonoTrigLin out = new IntegralUtils.MonoTrigLin();
        out.n = acc.deg;
        out.coef = acc.constCoef;
        out.u = acc.uCos;
        out.k = IntegralUtils.linearCoeff(acc.uCos, v) == null ? 0.0 : IntegralUtils.linearCoeff(acc.uCos, v);
        return out;
    }

    private NodoAST integrarReduccionExpIter(int n, double c, NodoAST u, double k, String v) {
        NodoAST epu = IntegralUtils.ePowClone(u);
        NodoAST acc = AstUtils.number(0.0);
        int m = n;
        double coefFront = c / k;
        while (m >= 0) {
            NodoAST xm = IntegralUtils.xPow(v, m);
            NodoAST term = AstUtils.bin(LexToken.Type.MUL, xm, epu, "*", 6);
            double sign = ((n - m) % 2 == 0) ? 1.0 : -1.0;
            double mult = coefFront * combFactor(n, m) / Math.pow(k, n - m);
            term = IntegralUtils.mulC(term, sign * mult);
            acc = IntegralUtils.sum(acc, term);
            m--;
        }
        return acc;
    }
    private double combFactor(int n, int m) {
        double num = 1.0;
        for (int i = m + 1; i <= n; i++) num *= i;
        return num;
    }
    private NodoAST integrarReduccionSin(int n, double c, NodoAST u, double k, String v) {
        NodoAST acc = AstUtils.number(0.0);
        int m = n;
        while (m >= 0) {
            NodoAST xm = IntegralUtils.xPow(v, m);
            NodoAST trig = ((n - m) % 2 == 0) ? IntegralUtils.cosClone(u) : IntegralUtils.sinClone(u);
            double sign = ((n - m) % 4 == 0) ? -1.0 : ((n - m) % 4 == 1) ? 1.0 : ((n - m) % 4 == 2) ? 1.0 : -1.0;
            NodoAST term = AstUtils.bin(LexToken.Type.MUL, xm, trig, "*", 6);
            double mult = c * combFactor(n, m) * sign / Math.pow(k, n - m + 1);
            term = IntegralUtils.mulC(term, mult);
            acc = IntegralUtils.sum(acc, term);
            m--;
        }
        return acc;
    }
    private NodoAST integrarReduccionCos(int n, double c, NodoAST u, double k, String v) {
        NodoAST acc = AstUtils.number(0.0);
        int m = n;
        while (m >= 0) {
            NodoAST xm = IntegralUtils.xPow(v, m);
            NodoAST trig = ((n - m) % 2 == 0) ? IntegralUtils.sinClone(u) : IntegralUtils.cosClone(u);
            double sign = ((n - m) % 4 == 0) ? 1.0 : ((n - m) % 4 == 1) ? 1.0 : ((n - m) % 4 == 2) ? -1.0 : -1.0;
            NodoAST term = AstUtils.bin(LexToken.Type.MUL, xm, trig, "*", 6);
            double mult = c * combFactor(n, m) * sign / Math.pow(k, n - m + 1);
            term = IntegralUtils.mulC(term, mult);
            acc = IntegralUtils.sum(acc, term);
            m--;
        }
        return acc;
    }
}
