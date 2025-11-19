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
        NodoAST cuerpo0 = ii.cuerpo;
        List<PasoResolucion> pasos = new ArrayList<>();

        NodoAST cuerpo = IntegralUtils.foldEConstTimesXIntoExpLinear(cuerpo0, x);
        if (cuerpo != cuerpo0) {
            pasos.add(new PasoResolucion("Normalización exponencial lineal", AstUtils.toTeX(cuerpo)));
        }

        IntegralUtils.MonoExpLin mel = matchMonomialTimesExpLinear(cuerpo, x);
        if (mel != null && Math.abs(mel.k) > 1e-15) {
            pasos.add(new PasoResolucion("Detección", "\\int x^{"+mel.n+"}\\,e^{"+AstUtils.toTeX(mel.u)+"}\\,dx"));
            pasos.add(new PasoResolucion("Elección ILATE", "u=x^{"+mel.n+"},\\; dv=e^{"+AstUtils.toTeX(mel.u)+"}\\,dx"));
            NodoAST vPreview = IntegralUtils.mulC(IntegralUtils.ePowClone(mel.u), 1.0/mel.k);
            pasos.add(new PasoResolucion("Cálculo de v", AstUtils.toTeX(vPreview)));
            NodoAST duPreview = AstUtils.bin(LexToken.Type.MUL, AstUtils.number(mel.n), IntegralUtils.xPow(x, mel.n-1), "*", 6);
            pasos.add(new PasoResolucion("Cálculo de du", AstUtils.toTeX(duPreview)));
            pasos.add(new PasoResolucion("Fórmula de reducción", "uv-\\int v\\,du"));

            NodoAST F = integrarReduccionExpIterConPasos(mel.n, mel.coef, mel.u, mel.k, x, pasos, "Término exp·poly");
            if (F == null) return null;

            ResultadoResolucion rr = cerrar(ii, x, F, pasos);
            return rr;
        }

        IntegralUtils.MonoTrigLin msl = matchMonomialTimesSinLinear(cuerpo, x);
        if (msl != null && Math.abs(msl.k) > 1e-15) {
            pasos.add(new PasoResolucion("Detección", "\\int x^{"+msl.n+"}\\,\\sin\\!\\left("+AstUtils.toTeX(msl.u)+"\\right)\\,dx"));
            pasos.add(new PasoResolucion("Elección ILATE", "u=x^{"+msl.n+"},\\; dv=\\sin\\!\\left("+AstUtils.toTeX(msl.u)+"\\right)\\,dx"));
            NodoAST vPreview = IntegralUtils.mulC(IntegralUtils.cosClone(msl.u), -1.0/msl.k);
            pasos.add(new PasoResolucion("Cálculo de v", AstUtils.toTeX(vPreview)));
            NodoAST duPreview = AstUtils.bin(LexToken.Type.MUL, AstUtils.number(msl.n), IntegralUtils.xPow(x, msl.n-1), "*", 6);
            pasos.add(new PasoResolucion("Cálculo de du", AstUtils.toTeX(duPreview)));
            pasos.add(new PasoResolucion("Fórmula de reducción", "uv-\\int v\\,du"));

            NodoAST F = integrarReduccionSinConPasos(msl.n, msl.coef, msl.u, msl.k, x, pasos, "Término sin·poly");
            if (F == null) return null;

            ResultadoResolucion rr = cerrar(ii, x, F, pasos);
            return rr;
        }

        IntegralUtils.MonoTrigLin mcl = matchMonomialTimesCosLinear(cuerpo, x);
        if (mcl != null && Math.abs(mcl.k) > 1e-15) {
            pasos.add(new PasoResolucion("Detección", "\\int x^{"+mcl.n+"}\\,\\cos\\!\\left("+AstUtils.toTeX(mcl.u)+"\\right)\\,dx"));
            pasos.add(new PasoResolucion("Elección ILATE", "u=x^{"+mcl.n+"},\\; dv=\\cos\\!\\left("+AstUtils.toTeX(mcl.u)+"\\right)\\,dx"));
            NodoAST vPreview = IntegralUtils.mulC(IntegralUtils.sinClone(mcl.u), 1.0/mcl.k);
            pasos.add(new PasoResolucion("Cálculo de v", AstUtils.toTeX(vPreview)));
            NodoAST duPreview = AstUtils.bin(LexToken.Type.MUL, AstUtils.number(mcl.n), IntegralUtils.xPow(x, mcl.n-1), "*", 6);
            pasos.add(new PasoResolucion("Cálculo de du", AstUtils.toTeX(duPreview)));
            pasos.add(new PasoResolucion("Fórmula de reducción", "uv-\\int v\\,du"));

            NodoAST F = integrarReduccionCosConPasos(mcl.n, mcl.coef, mcl.u, mcl.k, x, pasos, "Término cos·poly");
            if (F == null) return null;

            ResultadoResolucion rr = cerrar(ii, x, F, pasos);
            return rr;
        }

        if (IntegralUtils.esCicloExpTrig(cuerpo, x)) {
            pasos.add(new PasoResolucion("Detección", "\\text{Ciclo por partes con exponencial y trigonométrica}"));
            NodoAST F = IntegralUtils.resolverCicloExpTrig(cuerpo, x);
            pasos.add(new PasoResolucion("Resolución del sistema cíclico", AstUtils.toTeX(F)));
            ResultadoResolucion rr = cerrar(ii, x, F, pasos);
            return rr;
        }

        if (IntegralUtils.esLnPorMonomio(cuerpo, x)) {
            pasos.add(new PasoResolucion("Detección", "\\int x^n\\,\\ln(x)\\,dx"));
            NodoAST F = IntegralUtils.integrarLnPorMonomio(cuerpo, x);
            if (F != null) {
                int ndeg = IntegralUtils.degMonomio(cuerpo, x);
                NodoAST vPrev = AstUtils.bin(
                        LexToken.Type.DIV,
                        IntegralUtils.xPow(x, ndeg + 1),
                        AstUtils.number(ndeg + 1),
                        "/", 4
                );
                pasos.add(new PasoResolucion("Elección ILATE", "u=\\ln(x),\\; dv=x^{"+ndeg+"}\\,dx"));
                pasos.add(new PasoResolucion("Cálculo de v", AstUtils.toTeX(vPrev)));
                NodoAST duPrev = AstUtils.bin(
                        LexToken.Type.DIV,
                        AstUtils.number(1.0),
                        AstUtils.atom(LexToken.Type.VARIABLE, x, 1),
                        "/", 6
                );
                pasos.add(new PasoResolucion("Cálculo de du", AstUtils.toTeX(duPrev)));
                pasos.add(new PasoResolucion("Construcción por partes", "uv-\\int v\\,du"));
                pasos.add(new PasoResolucion("Integración término a término", AstUtils.toTeX(F)));
                ResultadoResolucion rr = cerrar(ii, x, F, pasos);
                return rr;
            }
        }


        ResultadoResolucion gen = aplicarPartesGeneral(ii, raiz, pasos);
        return gen;
    }

    private ResultadoResolucion cerrar(IntegralUtils.IntegralInfo ii, String x, NodoAST F, List<PasoResolucion> pasos) {
        ResultadoResolucion rr = new ResultadoResolucion();
        rr.pasos = new ArrayList<>(pasos);
        if (ii.definida) {
            NodoAST supEval = IntegralUtils.sustituirVar(AstUtils.cloneTree(F), x, ii.sup);
            NodoAST infEval = IntegralUtils.sustituirVar(AstUtils.cloneTree(F), x, ii.inf);
            NodoAST val = IntegralUtils.evalDefinida(F, x, ii.inf, ii.sup);
            rr.pasos.add(new PasoResolucion("F(b)", AstUtils.toTeX(supEval)));
            rr.pasos.add(new PasoResolucion("F(a)", AstUtils.toTeX(infEval)));
            rr.pasos.add(new PasoResolucion("F(b)-F(a)", AstUtils.toTeX(val)));
            NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, AstUtils.cloneTree(val), ii.padre, val);
            rr.resultado = nuevo;
            rr.latexFinal = AstUtils.toTeX(nuevo);
            rr.pasos.add(new PasoResolucion(rr.latexFinal));
            return rr;
        } else {
            NodoAST fin = IntegralUtils.addC(F);
            rr.pasos.add(new PasoResolucion("Antiderivada", AstUtils.toTeX(fin)));
            NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, AstUtils.cloneTree(fin), ii.padre, fin);
            rr.resultado = nuevo;
            rr.latexFinal = AstUtils.toTeX(nuevo);
            rr.pasos.add(new PasoResolucion(rr.latexFinal));
            return rr;
        }
    }

    private static Double coefLinealDe(NodoAST u, String var) {
        if (u == null || u.token == null) return null;
        LexToken.Type t = u.token.type;
        if (t == LexToken.Type.VARIABLE && var.equals(u.token.value) && u.hijos.isEmpty()) return 1.0;
        if (AstUtils.isConst(u)) return 0.0;
        if (t == LexToken.Type.MUL && u.hijos.size() == 2) {
            Double cL = AstUtils.evalConst(u.hijos.get(0));
            Double cR = AstUtils.evalConst(u.hijos.get(1));
            if (cL != null) {
                Double k = coefLinealDe(u.hijos.get(1), var);
                return (k == null) ? null : cL * k;
            }
            if (cR != null) {
                Double k = coefLinealDe(u.hijos.get(0), var);
                return (k == null) ? null : cR * k;
            }
            return null;
        }
        if ((t == LexToken.Type.SUM || t == LexToken.Type.SUB) && u.hijos.size() == 2) {
            Double k1 = coefLinealDe(u.hijos.get(0), var);
            Double k2 = coefLinealDe(u.hijos.get(1), var);
            if (k1 == null || k2 == null) return null;
            return (t == LexToken.Type.SUM) ? (k1 + k2) : (k1 - k2);
        }
        return null;
    }

    private ResultadoResolucion aplicarPartesGeneral(IntegralUtils.IntegralInfo ii, NodoAST raiz, List<PasoResolucion> pasos) {
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

        pasos.add(new PasoResolucion("Elección ILATE", "u="+AstUtils.toTeX(u)+",\\; dv="+AstUtils.toTeX(dvBody)));

        NodoAST v = IntegralUtils.integralRec(dvBody, x);
        if (v == null) return null;
        pasos.add(new PasoResolucion("Cálculo de v", AstUtils.toTeX(v)));

        NodoAST du = IntegralUtils.derivadaSimple(u, x);
        if (du == null) return null;
        pasos.add(new PasoResolucion("Cálculo de du", AstUtils.toTeX(du)));

        NodoAST uv = AstUtils.bin(LexToken.Type.MUL, AstUtils.cloneTree(u), AstUtils.cloneTree(v), "*", 6);
        NodoAST vdu = AstUtils.bin(LexToken.Type.MUL, AstUtils.cloneTree(v), du, "*", 6);
        pasos.add(new PasoResolucion("Construcción por partes", AstUtils.toTeX(uv)));

        NodoAST integralRestante = ii.definida
                ? IntegralUtils.integralDef(ii.inf, ii.sup, vdu, x)
                : IntegralUtils.integralIndef(vdu, x);

        NodoAST expr = AstUtils.bin(LexToken.Type.SUB, uv, integralRestante, "-", 5);
        pasos.add(new PasoResolucion("Integral restante", AstUtils.toTeX(integralRestante)));

        NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, expr, ii.padre, expr);
        ResultadoResolucion rr = new ResultadoResolucion();
        rr.resultado = nuevo;
        rr.latexFinal = AstUtils.toTeX(nuevo);
        rr.pasos = new ArrayList<>(pasos);
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
        Double k = coefLinealDe(out.u, v);
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
        Double k = coefLinealDe(out.u, v);
        out.k = (k == null) ? 0.0 : k;
        return out;
    }

    private IntegralUtils.MonoExpLin matchMonomialTimesExpLinear(NodoAST n, String v) {
        Acc acc = IntegralUtils.collectForParts(n, v);
        if (acc == null || !acc.ok) return null;

        if (acc.countExpLin == 1 && acc.countSinLin == 0 && acc.countCosLin == 0) {
            IntegralUtils.MonoExpLin out = new IntegralUtils.MonoExpLin();
            out.n = acc.deg;
            out.coef = acc.constCoef;
            out.u = acc.uExp;
            Double k = coefLinealDe(out.u, v);
            out.k = (k == null) ? 0.0 : k;
            return out;
        }

        if (acc.countExpLin == 0 && acc.countSinLin == 0 && acc.countCosLin == 0) {
            List<NodoAST> fs = new ArrayList<>();
            flattenMul(n, fs);
            Double kConst = null;
            int plainVar = 0;
            for (NodoAST f : fs) {
                if (f == null || f.token == null) continue;
                if (f.token.type == LexToken.Type.EXP && f.hijos.size() == 2 &&
                        f.hijos.get(0) != null && f.hijos.get(0).token != null &&
                        f.hijos.get(0).token.type == LexToken.Type.CONST_E &&
                        AstUtils.isConst(f.hijos.get(1))) {
                    Double k = AstUtils.evalConst(f.hijos.get(1));
                    if (k == null) return null;
                    if (kConst != null) { kConst = null; break; }
                    kConst = k;
                } else if (f.token.type == LexToken.Type.VARIABLE && v.equals(f.token.value) && f.hijos.isEmpty()) {
                    plainVar++;
                }
            }
            if (kConst != null && plainVar == 1) {
                IntegralUtils.MonoExpLin out = new IntegralUtils.MonoExpLin();
                out.n = Math.max(0, acc.deg - 1);
                out.coef = acc.constCoef;
                NodoAST u = AstUtils.bin(LexToken.Type.MUL, AstUtils.number(kConst), IntegralUtils.xPow(v, 1), "*", 6);
                out.u = u;
                out.k = kConst;
                return out;
            }
        }
        return null;
    }

    private void flattenMul(NodoAST n, List<NodoAST> out) {
        if (n == null || n.token == null) return;
        if (n.token.type == LexToken.Type.MUL && n.hijos.size() == 2) {
            flattenMul(n.hijos.get(0), out);
            flattenMul(n.hijos.get(1), out);
        } else {
            out.add(n);
        }
    }

    private NodoAST integrarReduccionExpIterConPasos(int n, double c, NodoAST u, double k, String v, List<PasoResolucion> pasos, String etiqueta) {
        NodoAST sum = null;
        for (int i = n; i >= 0; i--) {
            int j = n - i;
            double sgn = (j % 2 == 0) ? 1.0 : -1.0;
            double coef = c * sgn * IntegralUtils.fallingFactorial(n, j) / Math.pow(k, j + 1);
            NodoAST termCore = AstUtils.bin(LexToken.Type.MUL, IntegralUtils.xPow(v, i), IntegralUtils.ePowClone(u), "*", 6);
            NodoAST term = AstUtils.bin(LexToken.Type.MUL, AstUtils.number(coef), termCore, "*", 6);
            sum = (sum == null) ? term : AstUtils.bin(LexToken.Type.SUM, sum, term, "+", 5);
            pasos.add(new PasoResolucion(etiqueta+" j="+j, AstUtils.toTeX(term)));
        }
        pasos.add(new PasoResolucion("Suma de términos", AstUtils.toTeX(sum)));
        return sum;
    }

    private NodoAST integrarReduccionSinConPasos(int n, double c, NodoAST u, double k, String v, List<PasoResolucion> pasos, String etiqueta) {
        if (n % 2 == 0) {
            int m = n / 2;
            NodoAST sum = null;
            for (int j = 0; j <= m; j++) {
                double coef = c * IntegralUtils.binom(n, 2 * j) * Math.pow(-1, j) / Math.pow(k, 2 * j + 1);
                NodoAST termCore = AstUtils.bin(LexToken.Type.MUL, IntegralUtils.xPow(v, n - 2 * j), IntegralUtils.cosClone(u), "*", 6);
                NodoAST term = AstUtils.bin(LexToken.Type.MUL, AstUtils.number(coef), termCore, "*", 6);
                sum = (sum == null) ? term : AstUtils.bin(LexToken.Type.SUM, sum, term, "+", 5);
                pasos.add(new PasoResolucion(etiqueta+" j="+j, AstUtils.toTeX(term)));
            }
            pasos.add(new PasoResolucion("Suma de términos", AstUtils.toTeX(sum)));
            return sum;
        } else {
            int m = (n - 1) / 2;
            NodoAST sum = null;
            for (int j = 0; j <= m; j++) {
                double coef = c * IntegralUtils.binom(n, 2 * j + 1) * Math.pow(-1, j) / Math.pow(k, 2 * j + 2);
                NodoAST termCore = AstUtils.bin(LexToken.Type.MUL, IntegralUtils.xPow(v, n - 2 * j - 1), IntegralUtils.sinClone(u), "*", 6);
                NodoAST term = AstUtils.bin(LexToken.Type.MUL, AstUtils.number(coef), termCore, "*", 6);
                sum = (sum == null) ? term : AstUtils.bin(LexToken.Type.SUM, sum, term, "+", 5);
                pasos.add(new PasoResolucion(etiqueta+" j="+j, AstUtils.toTeX(term)));
            }
            pasos.add(new PasoResolucion("Suma de términos", AstUtils.toTeX(sum)));
            return sum;
        }
    }

    private NodoAST integrarReduccionCosConPasos(int n, double c, NodoAST u, double k, String v, List<PasoResolucion> pasos, String etiqueta) {
        if (n % 2 == 0) {
            int m = n / 2;
            NodoAST sum = null;
            for (int j = 0; j <= m; j++) {
                double coef = c * IntegralUtils.binom(n, 2 * j) * Math.pow(-1, j) / Math.pow(k, 2 * j + 2);
                NodoAST termCore = AstUtils.bin(LexToken.Type.MUL, IntegralUtils.xPow(v, n - 2 * j), IntegralUtils.sinClone(u), "*", 6);
                NodoAST term = AstUtils.bin(LexToken.Type.MUL, AstUtils.number(coef), termCore, "*", 6);
                sum = (sum == null) ? term : AstUtils.bin(LexToken.Type.SUM, sum, term, "+", 5);
                pasos.add(new PasoResolucion(etiqueta+" j="+j, AstUtils.toTeX(term)));
            }
            pasos.add(new PasoResolucion("Suma de términos", AstUtils.toTeX(sum)));
            return sum;
        } else {
            int m = (n - 1) / 2;
            NodoAST sum = null;
            for (int j = 0; j <= m; j++) {
                double coef = c * IntegralUtils.binom(n, 2 * j + 1) * Math.pow(-1, j) / Math.pow(k, 2 * j + 1);
                NodoAST termCore = AstUtils.bin(LexToken.Type.MUL, IntegralUtils.xPow(v, n - 2 * j - 1), IntegralUtils.cosClone(u), "*", 6);
                NodoAST term = AstUtils.bin(LexToken.Type.MUL, AstUtils.number(coef), termCore, "*", 6);
                sum = (sum == null) ? term : AstUtils.bin(LexToken.Type.SUM, sum, term, "+", 5);
                pasos.add(new PasoResolucion(etiqueta+" j="+j, AstUtils.toTeX(term)));
            }
            pasos.add(new PasoResolucion("Suma de términos", AstUtils.toTeX(sum)));
            return sum;
        }
    }
}
