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

import java.util.ArrayList;
import java.util.List;

public class SubstitutionIntegrator implements IntegratorRule {
    private final boolean definida;

    public SubstitutionIntegrator(boolean definida) {
        this.definida = definida;
    }
    @Override
    public ResultadoResolucion apply(NodoAST raiz, ResultadoSemantico rs) {
        IntegralUtils.IntegralInfo ii = IntegralUtils.localizarIntegral(raiz, definida);
        if (ii == null || ii.cuerpo == null || ii.var == null) return null;
        String x = ii.var;
        NodoAST body = ii.cuerpo;

        if (body.token != null && body.token.type == LexToken.Type.TRIG_SIN && body.hijos.size() == 1) {
            NodoAST arg = body.hijos.get(0);
            double c = 1.0;
            NodoAST exp = null;

            if (arg.token != null && arg.token.type == LexToken.Type.MUL && arg.hijos.size() == 2) {
                NodoAST a = arg.hijos.get(0), b = arg.hijos.get(1);
                if (a.token != null && (a.token.type == LexToken.Type.INTEGER || a.token.type == LexToken.Type.DECIMAL) && a.hijos.isEmpty()
                        && b.token != null && b.token.type == LexToken.Type.EXP) {
                    c = Double.parseDouble(a.token.value);
                    exp = b;
                } else if (b.token != null && (b.token.type == LexToken.Type.INTEGER || b.token.type == LexToken.Type.DECIMAL) && b.hijos.isEmpty()
                        && a.token != null && a.token.type == LexToken.Type.EXP) {
                    c = Double.parseDouble(b.token.value);
                    exp = a;
                }
            } else if (arg.token != null && arg.token.type == LexToken.Type.EXP) {
                c = 1.0;
                exp = arg;
            }

            if (exp != null && exp.hijos.size() == 2
                    && exp.hijos.get(0) != null && exp.hijos.get(0).token != null
                    && exp.hijos.get(0).token.type == LexToken.Type.CONST_E) {

                NodoAST u = exp.hijos.get(1);
                Double k = IntegralUtils.linearCoeff(u, x);
                if (k != null && Math.abs(k) > 1e-15) {
                    NodoAST epow = IntegralUtils.ePowClone(u);
                    NodoAST siArg = (Math.abs(c - 1.0) < 1e-15)
                            ? epow
                            : AstUtils.bin(LexToken.Type.MUL, AstUtils.number(c), epow, "*", 6);

                    NodoAST siFun = new NodoAST(new LexToken(LexToken.Type.VARIABLE, "Si", 8));
                    siFun.addHijo(siArg);

                    NodoAST F = AstUtils.bin(LexToken.Type.MUL, AstUtils.number(1.0 / k), siFun, "*", 6);
                    NodoAST repl = definida ? evalDefinida(F, x, ii.inf, ii.sup) : addC(F);
                    NodoAST safe = AstUtils.cloneTree(repl);
                    NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, safe, ii.padre, safe);

                    ResultadoResolucion rr = new ResultadoResolucion();
                    rr.resultado = nuevo;
                    rr.latexFinal = AstUtils.toTeX(nuevo);
                    rr.pasos.add(new PasoResolucion("\\text{Sustituci\\'on } u=e^{"+AstUtils.toTeX(u)+"},\\; du="+k+"\\,e^{"+AstUtils.toTeX(u)+"}\\,dx"));
                    return rr;
                }
            }
        }

        return null;
    }
    private NodoAST evalDefinida(NodoAST F, String var, NodoAST a, NodoAST b) {
        if (F == null || a == null || b == null) return null;
        NodoAST Fb = sustituirVar(F, var, AstUtils.cloneTree(b));
        NodoAST Fa = sustituirVar(F, var, AstUtils.cloneTree(a));
        return AstUtils.bin(LexToken.Type.SUB, Fb, Fa, "-", 5);
    }
    private boolean isSinOfConstTimesExpLinear(NodoAST n, String v) {
        if (n == null || n.token == null || n.token.type != LexToken.Type.TRIG_SIN || n.hijos.size() != 1) return false;
        NodoAST arg = n.hijos.get(0);
        if (arg.token != null && arg.token.type == LexToken.Type.EXP && arg.hijos.size() == 2) {
            if (arg.hijos.get(0) != null && arg.hijos.get(0).token != null && arg.hijos.get(0).token.type == LexToken.Type.CONST_E) {
                Double k = IntegralUtils.linearCoeff(arg.hijos.get(1), v);
                return k != null && Math.abs(k) > 1e-15;
            }
        }
        if (arg.token != null && arg.token.type == LexToken.Type.MUL && arg.hijos.size() == 2) {
            NodoAST a = arg.hijos.get(0), b = arg.hijos.get(1);
            if (AstUtils.evalConst(a) != null && b.token != null && b.token.type == LexToken.Type.EXP && b.hijos.size() == 2 && b.hijos.get(0) != null && b.hijos.get(0).token != null && b.hijos.get(0).token.type == LexToken.Type.CONST_E) {
                Double k = IntegralUtils.linearCoeff(b.hijos.get(1), v);
                return k != null && Math.abs(k) > 1e-15;
            }
            if (AstUtils.evalConst(b) != null && a.token != null && a.token.type == LexToken.Type.EXP && a.hijos.size() == 2 && a.hijos.get(0) != null && a.hijos.get(0).token != null && a.hijos.get(0).token.type == LexToken.Type.CONST_E) {
                Double k = IntegralUtils.linearCoeff(a.hijos.get(1), v);
                return k != null && Math.abs(k) > 1e-15;
            }
        }
        return false;
    }

    private ResultadoResolucion finish(IntegralUtils.IntegralInfo ii, NodoAST raiz, NodoAST F, NodoAST u, double k) {
        if (!definida) {
            NodoAST conC = addC(F);
            NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, conC, ii.padre, conC);
            ResultadoResolucion rr = new ResultadoResolucion();
            rr.resultado = nuevo;
            rr.latexFinal = AstUtils.toTeX(nuevo);
            String msg = (Double.isNaN(k))
                    ? "\\text{Sustituci\\'on } u=" + AstUtils.toTeX(u) + " \\Rightarrow " + rr.latexFinal
                    : "\\text{Sustituci\\'on } u=" + AstUtils.toTeX(u) + ",\\; du=" + formatK(k) + "\\,dx \\Rightarrow " + rr.latexFinal;
            rr.pasos.add(new PasoResolucion(msg));
            return rr;
        } else {
            NodoAST Fb = sustituirVar(F, ii.var, AstUtils.cloneTree(ii.sup));
            NodoAST Fa = sustituirVar(F, ii.var, AstUtils.cloneTree(ii.inf));
            NodoAST eval = AstUtils.bin(LexToken.Type.SUB, Fb, Fa, "-", 5);
            NodoAST nuevo = IntegralUtils.reemplazar(ii.nodoIntegral, eval, ii.padre, eval);
            ResultadoResolucion rr = new ResultadoResolucion();
            rr.resultado = nuevo;
            rr.latexFinal = AstUtils.toTeX(nuevo);
            String msg = (Double.isNaN(k))
                    ? "\\text{Sustituci\\'on } u=" + AstUtils.toTeX(u) + " \\Rightarrow " + rr.latexFinal
                    : "\\text{Sustituci\\'on } u=" + AstUtils.toTeX(u) + ",\\; du=" + formatK(k) + "\\,dx \\Rightarrow " + rr.latexFinal;
            rr.pasos.add(new PasoResolucion(msg));
            return rr;
        }
    }

    private ResultadoResolucion fallback(NodoAST raiz, ResultadoSemantico rs) {
        IntegralUtils.IntegralInfo ii = IntegralUtils.localizarIntegral(raiz, definida);
        ResultadoResolucion rr = new ResultadoResolucion();
        if (ii == null) {
            rr.resultado = raiz;
            rr.latexFinal = AstUtils.toTeX(raiz);
            rr.pasos.add(new PasoResolucion("\\text{Sin cambio} \\Rightarrow " + rr.latexFinal));
            return rr;
        }
        if (!definida) {
            String s = AstUtils.toSymja(ii.cuerpo);
            String r = (s == null) ? "" : "Integrate[" + s + "," + ii.var + "]";
            rr.resultado = raiz;
            rr.latexFinal = SymjaBridge.toTeX(r);
            rr.pasos.add(new PasoResolucion("\\Rightarrow " + rr.latexFinal));
            return rr;
        } else {
            String s = AstUtils.toSymja(ii.cuerpo);
            String a = AstUtils.toSymja(ii.inf);
            String b = AstUtils.toSymja(ii.sup);
            String r = (s == null) ? "" : "Integrate[" + s + ",{" + ii.var + "," + (a == null ? "a" : a) + "," + (b == null ? "b" : b) + "}]";
            rr.resultado = raiz;
            rr.latexFinal = SymjaBridge.toTeX(r);
            rr.pasos.add(new PasoResolucion("\\Rightarrow " + rr.latexFinal));
            return rr;
        }
    }

    private static class UFun {
        final int kind;
        final NodoAST u;
        UFun(int kind, NodoAST u) { this.kind = kind; this.u = u; }
        NodoAST buildWith(NodoAST arg) {
            switch (kind) {
                case 0: {
                    NodoAST e = AstUtils.atom(LexToken.Type.CONST_E, "e", 1);
                    return AstUtils.bin(LexToken.Type.EXP, e, arg, "^", 7);
                }
                case 1: return AstUtils.un(LexToken.Type.TRIG_SIN, arg, "sin", 8);
                case 2: return AstUtils.un(LexToken.Type.TRIG_COS, arg, "cos", 8);
                case 3: return AstUtils.un(LexToken.Type.TRIG_TAN, arg, "tan", 8);
                default: return null;
            }
        }
    }

    private UFun parseUFun(NodoAST f) {
        if (f == null || f.token == null) return null;
        LexToken.Type t = f.token.type;
        if (t == LexToken.Type.EXP && f.hijos.size() == 2) {
            if (f.hijos.get(0) != null && f.hijos.get(0).token != null
                    && f.hijos.get(0).token.type == LexToken.Type.CONST_E) {
                return new UFun(0, f.hijos.get(1));
            }
        }
        if (t == LexToken.Type.TRIG_SIN && f.hijos.size() == 1) return new UFun(1, f.hijos.get(0));
        if (t == LexToken.Type.TRIG_COS && f.hijos.size() == 1) return new UFun(2, f.hijos.get(0));
        if (t == LexToken.Type.TRIG_TAN && f.hijos.size() == 1) return new UFun(3, f.hijos.get(0));
        return null;
    }

    private Integer powerOfVar(NodoAST u, String var) {
        if (u == null || u.token == null) return null;
        if (u.token.type == LexToken.Type.EXP && u.hijos.size() == 2) {
            if (u.hijos.get(0) != null && u.hijos.get(0).token != null
                    && u.hijos.get(0).token.type == LexToken.Type.VARIABLE
                    && var.equals(u.hijos.get(0).token.value)) {
                Double n = AstUtils.evalConst(u.hijos.get(1));
                if (n != null) {
                    int ni = (int)Math.round(n);
                    if (Math.abs(n - ni) < 1e-9) return ni;
                }
            }
        }
        return null;
    }

    private NodoAST rebuildMulExcluding(List<NodoAST> fs, int skipIndex) {
        NodoAST acc = null;
        for (int i = 0; i < fs.size(); i++) {
            if (i == skipIndex) continue;
            NodoAST f = fs.get(i);
            if (acc == null) acc = f;
            else acc = AstUtils.bin(LexToken.Type.MUL, acc, f, "*", 6);
        }
        if (acc == null) return AstUtils.number(1.0);
        return acc;
    }

    private NodoAST mulC(NodoAST a, double c) {
        if (Math.abs(c - 1.0) < 1e-15) return a;
        if (Math.abs(c) < 1e-15) return AstUtils.number(0.0);
        return AstUtils.bin(LexToken.Type.MUL, AstUtils.number(c), a, "*", 6);
    }

    private String formatK(double k) {
        if (Math.abs(k - 1.0) < 1e-15) return "1";
        if (Math.abs(k + 1.0) < 1e-15) return "-1";
        return String.valueOf(k);
    }

    private NodoAST addC(NodoAST F) {
        NodoAST C = AstUtils.atom(LexToken.Type.VARIABLE, "C", 1);
        return AstUtils.bin(LexToken.Type.SUM, F, C, "+", 5);
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
}
