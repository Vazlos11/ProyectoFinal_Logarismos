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

import java.util.ArrayList;
import java.util.List;

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
            NodoAST repl = definida ? IntegralUtils.evalDefinida(F, x, ii.inf, ii.sup) : IntegralUtils.addC(F);
            NodoAST safe = AstUtils.cloneTree(repl);
            NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, safe, ii.padre, safe);
            ResultadoResolucion rr = new ResultadoResolucion();
            rr.resultado = nuevo;
            rr.latexFinal = AstUtils.toTeX(nuevo);
            rr.pasos.add(new PasoResolucion("\\int u\\,dv=uv-\\int v\\,du"));
            rr.pasos.add(new PasoResolucion(rr.latexFinal));
            return rr;
        }

        IntegralUtils.MonoTrigLin msl = matchMonomialTimesSinLinear(ii.cuerpo, x);
        if (msl != null && Math.abs(msl.k) > 1e-15) {
            NodoAST F = integrarReduccionSin(msl.n, msl.coef, msl.u, msl.k, x);
            NodoAST repl = definida ? IntegralUtils.evalDefinida(F, x, ii.inf, ii.sup) : IntegralUtils.addC(F);
            NodoAST safe = AstUtils.cloneTree(repl);
            NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, safe, ii.padre, safe);
            ResultadoResolucion rr = new ResultadoResolucion();
            rr.resultado = nuevo;
            rr.latexFinal = AstUtils.toTeX(nuevo);
            rr.pasos.add(new PasoResolucion("\\int u\\,dv=uv-\\int v\\,du"));
            rr.pasos.add(new PasoResolucion(rr.latexFinal));
            return rr;
        }

        IntegralUtils.MonoTrigLin mcl = matchMonomialTimesCosLinear(ii.cuerpo, x);
        if (mcl != null && Math.abs(mcl.k) > 1e-15) {
            NodoAST F = integrarReduccionCos(mcl.n, mcl.coef, mcl.u, mcl.k, x);
            NodoAST repl = definida ? IntegralUtils.evalDefinida(F, x, ii.inf, ii.sup) : IntegralUtils.addC(F);
            NodoAST safe = AstUtils.cloneTree(repl);
            NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, safe, ii.padre, safe);
            ResultadoResolucion rr = new ResultadoResolucion();
            rr.resultado = nuevo;
            rr.latexFinal = AstUtils.toTeX(nuevo);
            rr.pasos.add(new PasoResolucion("\\int u\\,dv=uv-\\int v\\,du"));
            rr.pasos.add(new PasoResolucion(rr.latexFinal));
            return rr;
        }

        if (IntegralUtils.esCicloExpTrig(ii.cuerpo, x)) {
            NodoAST F = IntegralUtils.resolverCicloExpTrig(ii.cuerpo, x);
            NodoAST repl = definida ? IntegralUtils.evalDefinida(F, x, ii.inf, ii.sup) : IntegralUtils.addC(F);
            NodoAST safe = AstUtils.cloneTree(repl);
            NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, safe, ii.padre, safe);
            ResultadoResolucion rr = new ResultadoResolucion();
            rr.resultado = nuevo;
            rr.latexFinal = AstUtils.toTeX(nuevo);
            rr.pasos.add(new PasoResolucion("\\int u\\,dv=uv-\\int v\\,du"));
            rr.pasos.add(new PasoResolucion(rr.latexFinal));
            return rr;
        }

        if (IntegralUtils.esLnPorMonomio(ii.cuerpo, x)) {
            NodoAST F = IntegralUtils.integrarLnPorMonomio(ii.cuerpo, x);
            if (F != null) {
                NodoAST repl = definida ? IntegralUtils.evalDefinida(F, x, ii.inf, ii.sup) : IntegralUtils.addC(F);
                NodoAST safe = AstUtils.cloneTree(repl);
                NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, safe, ii.padre, safe);
                ResultadoResolucion rr = new ResultadoResolucion();
                rr.resultado = nuevo;
                rr.latexFinal = AstUtils.toTeX(nuevo);
                rr.pasos.add(new PasoResolucion("\\int u\\,dv=uv-\\int v\\,du"));
                rr.pasos.add(new PasoResolucion(rr.latexFinal));
                return rr;
            }
        }

        ResultadoResolucion gen = aplicarPartesGeneral(ii, raiz);
        return gen;
    }

    private ResultadoResolucion aplicarPartesGeneral(IntegralUtils.IntegralInfo ii, NodoAST raiz) {
        String x = ii.var;
        IntegralUtils.MulSplit ms = IntegralUtils.splitMul(ii.cuerpo);
        if (ms == null || ms.nonconst.size() < 2) return null;

        List<NodoAST> fs = new ArrayList<>(ms.nonconst);
        int idxU = -1, best = 1000;
        for (int i = 0; i < fs.size(); i++) {
            int p = IntegralUtils.prioridadILATE(fs.get(i), x);
            if (p < best) { best = p; idxU = i; }
        }
        if (idxU < 0) return null;

        NodoAST u = fs.get(idxU);
        List<NodoAST> rest = new ArrayList<>();
        for (int i = 0; i < fs.size(); i++) if (i != idxU) rest.add(fs.get(i));
        NodoAST dvBody = IntegralUtils.rebuildMul(rest);
        if (Math.abs(ms.c - 1.0) > 1e-15) dvBody = IntegralUtils.mulC(dvBody, ms.c);

        NodoAST v = IntegralUtils.integralRec(dvBody, x);
        if (v == null) return null;

        NodoAST du = IntegralUtils.derivadaSimple(u, x);
        if (du == null) return null;

        NodoAST uv = AstUtils.bin(LexToken.Type.MUL, AstUtils.cloneTree(u), AstUtils.cloneTree(v), "*", 6);
        NodoAST vdu = AstUtils.bin(LexToken.Type.MUL, AstUtils.cloneTree(v), du, "*", 6);

        NodoAST integralRestante = ii.definida
                ? IntegralUtils.integralDef(ii.inf, ii.sup, vdu, x)
                : IntegralUtils.integralIndef(vdu, x);

        NodoAST expr = AstUtils.bin(LexToken.Type.SUB, uv, integralRestante, "-", 5);

        NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, expr, ii.padre, expr);
        ResultadoResolucion rr = new ResultadoResolucion();
        rr.resultado = nuevo;
        rr.latexFinal = AstUtils.toTeX(nuevo);
        rr.pasos.add(new PasoResolucion("\\int u\\,dv=uv-\\int v\\,du"));
        rr.pasos.add(new PasoResolucion(rr.latexFinal));
        return rr;
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

    private IntegralUtils.MonoExpLin matchMonomialTimesExpLinear(NodoAST n, String v) {
        Acc acc = IntegralUtils.collectForParts(n, v);
        if (acc == null || !acc.ok || acc.countExpLin != 1 || acc.countSinLin != 0 || acc.countCosLin != 0) return null;
        IntegralUtils.MonoExpLin out = new IntegralUtils.MonoExpLin();
        out.n = acc.deg;
        out.coef = acc.constCoef;
        out.u = acc.uExp;
        Double k = IntegralUtils.linearCoeff(acc.uExp, v);
        out.k = (k == null) ? 0.0 : k;
        return out;
    }

    private NodoAST integrarReduccionExpIter(int n, double c, NodoAST u, double k, String v) {
        NodoAST sum = null;
        for (int i = n; i >= 0; i--) {
            double coef = c * IntegralUtils.fallingFactorial(n, n - i) / Math.pow(k, n - i + 1);
            NodoAST term = AstUtils.bin(LexToken.Type.MUL, AstUtils.number(coef), AstUtils.bin(LexToken.Type.MUL, IntegralUtils.xPow(v, i), IntegralUtils.ePowClone(u), "*", 6), "*", 6);
            sum = (sum == null) ? term : AstUtils.bin(LexToken.Type.SUM, sum, term, "+", 5);
        }
        return sum;
    }

    private NodoAST integrarReduccionSin(int n, double c, NodoAST u, double k, String v) {
        if (n % 2 == 0) {
            int m = n / 2;
            NodoAST sum = null;
            for (int j = 0; j <= m; j++) {
                double coef = c * IntegralUtils.binom(n, 2 * j) * Math.pow(-1, j) / Math.pow(k, 2 * j + 1);
                NodoAST term = AstUtils.bin(LexToken.Type.MUL, AstUtils.number(coef), AstUtils.bin(LexToken.Type.MUL, IntegralUtils.xPow(v, n - 2 * j), IntegralUtils.cosClone(u), "*", 6), "*", 6);
                sum = (sum == null) ? term : AstUtils.bin(LexToken.Type.SUM, sum, term, "+", 5);
            }
            return sum;
        } else {
            int m = (n - 1) / 2;
            NodoAST sum = null;
            for (int j = 0; j <= m; j++) {
                double coef = c * IntegralUtils.binom(n, 2 * j + 1) * Math.pow(-1, j) / Math.pow(k, 2 * j + 2);
                NodoAST term = AstUtils.bin(LexToken.Type.MUL, AstUtils.number(coef), AstUtils.bin(LexToken.Type.MUL, IntegralUtils.xPow(v, n - 2 * j - 1), IntegralUtils.sinClone(u), "*", 6), "*", 6);
                sum = (sum == null) ? term : AstUtils.bin(LexToken.Type.SUM, sum, term, "+", 5);
            }
            return sum;
        }
    }

    private NodoAST integrarReduccionCos(int n, double c, NodoAST u, double k, String v) {
        if (n % 2 == 0) {
            int m = n / 2;
            NodoAST sum = null;
            for (int j = 0; j <= m; j++) {
                double coef = c * IntegralUtils.binom(n, 2 * j) * Math.pow(-1, j) / Math.pow(k, 2 * j + 2);
                NodoAST term = AstUtils.bin(LexToken.Type.MUL, AstUtils.number(coef), AstUtils.bin(LexToken.Type.MUL, IntegralUtils.xPow(v, n - 2 * j), IntegralUtils.sinClone(u), "*", 6), "*", 6);
                sum = (sum == null) ? term : AstUtils.bin(LexToken.Type.SUM, sum, term, "+", 5);
            }
            return sum;
        } else {
            int m = (n - 1) / 2;
            NodoAST sum = null;
            for (int j = 0; j <= m; j++) {
                double coef = c * IntegralUtils.binom(n, 2 * j + 1) * Math.pow(-1, j) / Math.pow(k, 2 * j + 1);
                NodoAST term = AstUtils.bin(LexToken.Type.MUL, AstUtils.number(coef), AstUtils.bin(LexToken.Type.MUL, IntegralUtils.xPow(v, n - 2 * j - 1), IntegralUtils.cosClone(u), "*", 6), "*", 6);
                sum = (sum == null) ? term : AstUtils.bin(LexToken.Type.SUM, sum, term, "+", 5);
            }
            return sum;
        }
    }
}
