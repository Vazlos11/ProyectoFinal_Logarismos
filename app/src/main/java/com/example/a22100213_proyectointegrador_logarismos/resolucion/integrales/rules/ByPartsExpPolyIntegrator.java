package com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.rules;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.Acc;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegratorRule;

public final class ByPartsExpPolyIntegrator implements IntegratorRule {
    private final boolean definida;

    public ByPartsExpPolyIntegrator(boolean definida) {
        this.definida = definida;
    }

    @Override
    public ResultadoResolucion apply(NodoAST raiz, ResultadoSemantico rs) {
        IntegralUtils.IntegralInfo ii = IntegralUtils.localizarIntegral(raiz, definida);
        if (ii == null || ii.cuerpo == null || ii.var == null) return null;
        String x = ii.var;

        IntegralUtils.MonoExpLin mel = matchMonomialTimesExpLinear(ii.cuerpo, x);
        if (mel != null && Math.abs(mel.k) > 1e-15) {
            NodoAST F = integrarReduccionExpIter(mel.n, mel.coef, mel.u, mel.k, x);
            NodoAST repl = definida ? evalDefinida(F, x, ii.inf, ii.sup) : addC(F);
            NodoAST safe = AstUtils.cloneTree(repl);
            NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, safe, ii.padre, safe);
            ResultadoResolucion rr = new ResultadoResolucion();
            rr.resultado = nuevo;
            rr.latexFinal = AstUtils.toTeX(nuevo);
            rr.pasos.add(new PasoResolucion("\\text{Integración por partes} \\Rightarrow " + rr.latexFinal));
            return rr;
        }

        IntegralUtils.MonoTrigLin msl = matchMonomialTimesSinLinear(ii.cuerpo, x);
        if (msl != null && Math.abs(msl.k) > 1e-15) {
            NodoAST F = integrarReduccionSin(msl.n, msl.coef, msl.u, msl.k, x);
            NodoAST repl = definida ? evalDefinida(F, x, ii.inf, ii.sup) : addC(F);
            NodoAST safe = AstUtils.cloneTree(repl);
            NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, safe, ii.padre, safe);
            ResultadoResolucion rr = new ResultadoResolucion();
            rr.resultado = nuevo;
            rr.latexFinal = AstUtils.toTeX(nuevo);
            rr.pasos.add(new PasoResolucion("\\text{Integración por partes} \\Rightarrow " + rr.latexFinal));
            return rr;
        }

        IntegralUtils.MonoTrigLin mcl = matchMonomialTimesCosLinear(ii.cuerpo, x);
        if (mcl != null && Math.abs(mcl.k) > 1e-15) {
            NodoAST F = integrarReduccionCos(mcl.n, mcl.coef, mcl.u, mcl.k, x);
            NodoAST repl = definida ? evalDefinida(F, x, ii.inf, ii.sup) : addC(F);
            NodoAST safe = AstUtils.cloneTree(repl);
            NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, safe, ii.padre, safe);
            ResultadoResolucion rr = new ResultadoResolucion();
            rr.resultado = nuevo;
            rr.latexFinal = AstUtils.toTeX(nuevo);
            rr.pasos.add(new PasoResolucion("\\text{Integración por partes} \\Rightarrow " + rr.latexFinal));
            return rr;
        }

        if (IntegralUtils.esCicloExpTrig(ii.cuerpo, x)) {
            NodoAST F = IntegralUtils.resolverCicloExpTrig(ii.cuerpo, x);
            NodoAST repl = definida ? evalDefinida(F, x, ii.inf, ii.sup) : addC(F);
            NodoAST safe = AstUtils.cloneTree(repl);
            NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, safe, ii.padre, safe);
            ResultadoResolucion rr = new ResultadoResolucion();
            rr.resultado = nuevo;
            rr.latexFinal = AstUtils.toTeX(nuevo);
            rr.pasos.add(new PasoResolucion("\\text{Integración por partes} \\Rightarrow " + rr.latexFinal));
            return rr;
        }

        if (IntegralUtils.esLnPorMonomio(ii.cuerpo, x)) {
            NodoAST F = IntegralUtils.integrarLnPorMonomio(ii.cuerpo, x);
            if (F != null) {
                NodoAST repl = definida ? evalDefinida(F, x, ii.inf, ii.sup) : addC(F);
                NodoAST safe = AstUtils.cloneTree(repl);
                NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, safe, ii.padre, safe);
                ResultadoResolucion rr = new ResultadoResolucion();
                rr.resultado = nuevo;
                rr.latexFinal = AstUtils.toTeX(nuevo);
                rr.pasos.add(new PasoResolucion("\\text{Integración por partes} \\Rightarrow " + rr.latexFinal));
                return rr;
            }
        }

        return null;
    }

    private NodoAST addC(NodoAST F) {
        NodoAST C = AstUtils.atom(LexToken.Type.VARIABLE, "C", 1);
        return AstUtils.bin(LexToken.Type.SUM, F, C, "+", 5);
    }

    private NodoAST evalDefinida(NodoAST F, String var, NodoAST a, NodoAST b) {
        if (F == null || a == null || b == null) return null;
        NodoAST Fb = sustituirVar(F, var, AstUtils.cloneTree(b));
        NodoAST Fa = sustituirVar(F, var, AstUtils.cloneTree(a));
        return AstUtils.bin(LexToken.Type.SUB, Fb, Fa, "-", 5);
    }

    private NodoAST sustituirVar(NodoAST n, String var, NodoAST con) {
        if (n == null) return null;
        if (n.token != null && n.token.type == LexToken.Type.VARIABLE && var.equals(n.token.value) && n.hijos.isEmpty()) {
            return AstUtils.cloneTree(con);
        }
        NodoAST c = new NodoAST(n.token == null ? null : new LexToken(n.token.type, n.token.value, n.token.prioridad));
        for (NodoAST h : n.hijos) {
            NodoAST ch = sustituirVar(h, var, con);
            if (ch != null) {
                c.hijos.add(ch);
                ch.parent = c;
            }
        }
        return c;
    }

    private IntegralUtils.MonoExpLin matchMonomialTimesExpLinear(NodoAST n, String v) {
        Acc acc = IntegralUtils.collectForParts(n, v);
        if (acc == null || !acc.ok || acc.countExpLin != 1) return null;
        IntegralUtils.MonoExpLin out = new IntegralUtils.MonoExpLin();
        out.n = acc.deg;
        out.coef = acc.constCoef;
        out.u = acc.uExp;
        Double k = IntegralUtils.linearCoeff(acc.uExp, v);
        out.k = (k == null) ? 0.0 : k;
        return out;
    }

    private IntegralUtils.MonoTrigLin matchMonomialTimesSinLinear(NodoAST n, String v) {
        Acc acc = IntegralUtils.collectForParts(n, v);
        if (acc == null || !acc.ok || acc.countSinLin != 1 || acc.countCosLin != 0 || acc.countExpLin != 0) return null;
        IntegralUtils.MonoTrigLin out = new IntegralUtils.MonoTrigLin();
        out.n = acc.deg;
        out.coef = acc.constCoef;
        out.u = acc.uSin;
        Double k = IntegralUtils.linearCoeff(acc.uSin, v);
        out.k = (k == null) ? 0.0 : k;
        return out;
    }

    private IntegralUtils.MonoTrigLin matchMonomialTimesCosLinear(NodoAST n, String v) {
        Acc acc = IntegralUtils.collectForParts(n, v);
        if (acc == null || !acc.ok || acc.countCosLin != 1 || acc.countSinLin != 0 || acc.countExpLin != 0) return null;
        IntegralUtils.MonoTrigLin out = new IntegralUtils.MonoTrigLin();
        out.n = acc.deg;
        out.coef = acc.constCoef;
        out.u = acc.uCos;
        Double k = IntegralUtils.linearCoeff(acc.uCos, v);
        out.k = (k == null) ? 0.0 : k;
        return out;
    }

    private NodoAST integrarReduccionExpIter(int n, double c, NodoAST u, double k, String v) {
        if (Math.abs(k) < 1e-15) return null;
        NodoAST acc = AstUtils.number(0.0);
        int m = n;
        double coefFront = c / k;
        while (m >= 0) {
            NodoAST xm = IntegralUtils.xPow(v, m);
            NodoAST epu = IntegralUtils.ePowClone(u);
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
        if (Math.abs(k) < 1e-15) return null;
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
        if (Math.abs(k) < 1e-15) return null;
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
