package com.example.a22100213_proyectointegrador_logarismos.resolucion;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.AnalisisSemantico;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.MetodoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.PlanificadorResolucion;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;

import java.util.ArrayDeque;
import java.util.Deque;

public class MotorResolucion {

    public static ResultadoResolucion resolver(NodoAST raiz, ResultadoSemantico rs) {
        ResultadoResolucion out = new ResultadoResolucion();
        NodoAST curAst = AstUtils.cloneTree(raiz);
        ResultadoSemantico curRs = rs;
        for (int it = 0; it < 5; it++) {
            MetodoResolucion m = PlanificadorResolucion.metodo(curAst, curRs);
            ResultadoResolucion step = resolverSegunTipo(curAst, curRs, m);
            out.pasos.addAll(step.pasos);
            out.resultado = step.resultado == null ? curAst : step.resultado;
            out.latexFinal = step.latexFinal;
            if (resuelto(out.resultado, curRs, step)) break;
            curAst = out.resultado;
            curRs = AnalisisSemantico.analizar(curAst);
        }
        if (out.latexFinal == null || out.latexFinal.isEmpty()) out.latexFinal = AstUtils.toTeX(curAst);
        if (out.resultado == null) out.resultado = curAst;
        return out;
    }

    private static boolean resuelto(NodoAST n, ResultadoSemantico rs, ResultadoResolucion rr) {
        if (rs == null) return false;
        switch (rs.tipoPrincipal) {
            case T4_INTEGRAL_INDEFINIDA:
                return !AstUtils.contains(n, LexToken.Type.INTEGRAL_INDEF) || okLatex(rr);
            case T5_INTEGRAL_DEFINIDA:
                return !AstUtils.contains(n, LexToken.Type.INTEGRAL_DEF) || okLatex(rr);
            case T1_ARITMETICA:
                return okLatex(rr);
            default:
                return okLatex(rr);
        }
    }

    private static boolean okLatex(ResultadoResolucion rr) {
        return rr != null && rr.latexFinal != null && !rr.latexFinal.trim().isEmpty();
    }

    private static ResultadoResolucion resolverSegunTipo(NodoAST raiz, ResultadoSemantico rs, MetodoResolucion m) {
        switch (rs.tipoPrincipal) {
            case T1_ARITMETICA: return resolverAritmetica(raiz);
            case T4_INTEGRAL_INDEFINIDA: return resolverIntegral(raiz, false);
            case T5_INTEGRAL_DEFINIDA: return resolverIntegral(raiz, true);
            default: {
                ResultadoResolucion rr = new ResultadoResolucion();
                rr.resultado = raiz;
                rr.latexFinal = AstUtils.toTeX(raiz);
                rr.pasos.add(new PasoResolucion("\\text{Aún no implementado} \\Rightarrow " + rr.latexFinal));
                return rr;
            }
        }
    }

    private static ResultadoResolucion resolverAritmetica(NodoAST n) {
        ResultadoResolucion rr = new ResultadoResolucion();
        Double vc = AstUtils.evalConst(n);
        if (vc != null) {
            rr.resultado = AstUtils.number(vc);
            rr.latexFinal = AstUtils.toTeX(rr.resultado);
            rr.pasos.add(new PasoResolucion("\\text{Evaluación directa} \\Rightarrow " + rr.latexFinal));
            return rr;
        }
        String sDeg = AstUtils.toSymja(astInjectDegrees(n));
        if (sDeg != null) {
            Double vdeg = SymjaBridge.evalDouble(sDeg);
            if (vdeg != null) {
                rr.resultado = AstUtils.number(vdeg);
                rr.latexFinal = AstUtils.toTeX(rr.resultado);
                rr.pasos.add(new PasoResolucion("\\text{Trig en grados} \\Rightarrow " + rr.latexFinal));
                return rr;
            }
        }
        String s = AstUtils.toSymja(n);
        if (s != null) {
            Double v = SymjaBridge.evalDouble(s);
            if (v != null) {
                rr.resultado = AstUtils.number(v);
                rr.latexFinal = AstUtils.toTeX(rr.resultado);
                rr.pasos.add(new PasoResolucion("\\text{Evaluación directa} \\Rightarrow " + rr.latexFinal));
                return rr;
            }
        }
        rr.resultado = n;
        rr.latexFinal = AstUtils.toTeX(n);
        rr.pasos.add(new PasoResolucion("\\text{Sin cambio} \\Rightarrow " + rr.latexFinal));
        return rr;
    }

    private static boolean containsVar(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.VARIABLE) return true;
        for (NodoAST h : n.hijos) if (containsVar(h)) return true;
        return false;
    }

    private static boolean isNumericSubtree(NodoAST n) {
        Double v = AstUtils.evalConst(n);
        return v != null && !containsVar(n);
    }

    private static boolean containsDegreeSymbol(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.VARIABLE && "Degree".equals(n.token.value)) return true;
        for (NodoAST h : n.hijos) if (containsDegreeSymbol(h)) return true;
        return false;
    }

    private static NodoAST astInjectDegrees(NodoAST n) {
        NodoAST c = AstUtils.cloneTree(n);
        injectDegreesRec(c);
        return c;
    }

    private static void injectDegreesRec(NodoAST n) {
        if (n == null || n.token == null) return;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.TRIG_SIN || t == LexToken.Type.TRIG_COS || t == LexToken.Type.TRIG_TAN ||
                t == LexToken.Type.TRIG_SEC || t == LexToken.Type.TRIG_CSC || t == LexToken.Type.TRIG_COT) {
            if (n.hijos.size() == 1) {
                NodoAST arg = n.hijos.get(0);
                if (isNumericSubtree(arg) && !containsDegreeSymbol(arg)) {
                    NodoAST deg = AstUtils.atom(LexToken.Type.VARIABLE, "Degree", 1);
                    NodoAST mul = AstUtils.bin(LexToken.Type.MUL, arg, deg, "*", 6);
                    n.hijos.set(0, mul);
                    mul.parent = n;
                } else {
                    injectDegreesRec(arg);
                }
            }
        } else {
            for (NodoAST h : n.hijos) injectDegreesRec(h);
        }
    }


    private static ResultadoResolucion resolverIntegral(NodoAST raiz, boolean definida) {
        ResultadoResolucion rr = new ResultadoResolucion();
        IntegralInfo ii = localizarIntegral(raiz, definida);
        if (ii == null) {
            rr.resultado = raiz;
            rr.latexFinal = AstUtils.toTeX(raiz);
            rr.pasos.add(new PasoResolucion("\\text{Sin cambio} \\Rightarrow " + rr.latexFinal));
            return rr;
        }
        if (!definida) {
            NodoAST F = integralRec(ii.cuerpo, ii.var);
            if (F != null) {
                NodoAST C = AstUtils.atom(LexToken.Type.VARIABLE, "C", 1);
                NodoAST suma = AstUtils.bin(LexToken.Type.SUM, F, C, "+", 5);
                NodoAST nuevo = reemplazar(ii.nodoIntegral, suma, ii.padre, suma);
                rr.resultado = nuevo;
                rr.latexFinal = AstUtils.toTeX(nuevo);
                rr.pasos.add(new PasoResolucion("\\text{Integral indefinida} \\Rightarrow " + rr.latexFinal));
                return rr;
            }
            String s = AstUtils.toSymja(ii.cuerpo);
            if (s != null) {
                String r = "Integrate[" + s + "," + ii.var + "]";
                rr.resultado = raiz;
                rr.latexFinal = SymjaBridge.toTeX(r);
                rr.pasos.add(new PasoResolucion("\\text{Integrate} \\Rightarrow " + rr.latexFinal));
                return rr;
            }
            rr.resultado = raiz;
            rr.latexFinal = AstUtils.toTeX(raiz);
            rr.pasos.add(new PasoResolucion("\\text{Sin cambio} \\Rightarrow " + rr.latexFinal));
            return rr;
        } else {
            NodoAST F = integralRec(ii.cuerpo, ii.var);
            if (F != null) {
                Double vb = evalAt(F, ii.var, ii.sup);
                Double va = evalAt(F, ii.var, ii.inf);
                if (vb != null && va != null) {
                    double num = vb - va;
                    rr.resultado = AstUtils.number(num);
                    rr.latexFinal = AstUtils.toTeX(rr.resultado);
                    rr.pasos.add(new PasoResolucion("\\text{F(b)-F(a)} \\Rightarrow " + rr.latexFinal));
                    return rr;
                }
                String Fsym = AstUtils.toSymja(F);
                String aSym = AstUtils.toSymja(ii.inf);
                String bSym = AstUtils.toSymja(ii.sup);
                if (Fsym != null && aSym != null && bSym != null) {
                    String exNum = "N((ReplaceAll[" + Fsym + "," + ii.var + "->(" + bSym + ")]) - (ReplaceAll[" + Fsym + "," + ii.var + "->(" + aSym + ")]))";
                    Double v = SymjaBridge.evalDouble(exNum);
                    if (v != null) {
                        rr.resultado = AstUtils.number(v);
                        rr.latexFinal = AstUtils.toTeX(rr.resultado);
                        rr.pasos.add(new PasoResolucion("\\text{F(b)-F(a)} \\Rightarrow " + rr.latexFinal));
                        return rr;
                    }
                    String exSym = "(ReplaceAll[" + Fsym + "," + ii.var + "->" + bSym + "] - ReplaceAll[" + Fsym + "," + ii.var + "->" + aSym + "])";
                    rr.resultado = raiz;
                    rr.latexFinal = SymjaBridge.toTeX(exSym);
                    rr.pasos.add(new PasoResolucion("\\text{F(b)-F(a)} \\Rightarrow " + rr.latexFinal));
                    return rr;
                }
            }
            String s = AstUtils.toSymja(ii.cuerpo);
            if (s != null) {
                Double a = AstUtils.evalConst(ii.inf);
                Double b = AstUtils.evalConst(ii.sup);
                if (a != null && b != null) {
                    String ex = "N(Integrate[" + s + ",{" + ii.var + "," + a + "," + b + "}])";
                    Double v = SymjaBridge.evalDouble(ex);
                    if (v != null) {
                        rr.resultado = AstUtils.number(v);
                        rr.latexFinal = AstUtils.toTeX(rr.resultado);
                        rr.pasos.add(new PasoResolucion("\\text{Integral definida} \\Rightarrow " + rr.latexFinal));
                        return rr;
                    }
                } else {
                    String ex = "N(Integrate[" + s + ",{" + ii.var + "," + AstUtils.toSymja(ii.inf) + "," + AstUtils.toSymja(ii.sup) + "}])";
                    Double v3 = SymjaBridge.evalDouble(ex);
                    if (v3 != null) {
                        rr.resultado = AstUtils.number(v3);
                        rr.latexFinal = AstUtils.toTeX(rr.resultado);
                        rr.pasos.add(new PasoResolucion("\\text{Integral definida} \\Rightarrow " + rr.latexFinal));
                        return rr;
                    }
                }
                String r = "Integrate[" + s + ",{" + ii.var + "," + AstUtils.toSymja(ii.inf) + "," + AstUtils.toSymja(ii.sup) + "}]";
                rr.resultado = raiz;
                rr.latexFinal = SymjaBridge.toTeX(r);
                rr.pasos.add(new PasoResolucion("\\text{Integrate} \\Rightarrow " + rr.latexFinal));
                return rr;
            }
            rr.resultado = raiz;
            rr.latexFinal = AstUtils.toTeX(raiz);
            rr.pasos.add(new PasoResolucion("\\text{Sin cambio} \\Rightarrow " + rr.latexFinal));
            return rr;
        }
    }

    private static Double evalConstOrSymja(NodoAST n) {
        Double v = AstUtils.evalConst(n);
        if (v != null) return v;
        String s = AstUtils.toSymja(n);
        if (s != null) return SymjaBridge.evalDouble("N(" + s + ")");
        return null;
    }

    private static NodoAST substituteVar(NodoAST expr, String var, double value) {
        NodoAST rep = AstUtils.number(value);
        return substituteRec(AstUtils.cloneTree(expr), var, rep);
    }

    private static NodoAST substituteRec(NodoAST cur, String var, NodoAST rep) {
        if (cur == null || cur.token == null) return cur;
        if (cur.token.type == LexToken.Type.VARIABLE && var.equals(cur.token.value) && cur.hijos.isEmpty()) {
            return AstUtils.cloneTree(rep);
        }
        for (int i = 0; i < cur.hijos.size(); i++) {
            NodoAST ch = cur.hijos.get(i);
            NodoAST nn = substituteRec(ch, var, rep);
            if (nn != ch) {
                cur.hijos.set(i, nn);
                nn.parent = cur;
            }
        }
        return cur;
    }

    private static Double evalAt(NodoAST F, String var, NodoAST valueNode) {
        Double v = AstUtils.evalConst(valueNode);
        if (v == null) return null;
        NodoAST Fx = substituteVar(F, var, v);
        return evalConstOrSymja(Fx);
    }


    private static class IntegralInfo {
        NodoAST nodoIntegral;
        NodoAST padre;
        boolean definida;
        String var;
        NodoAST inf;
        NodoAST sup;
        NodoAST cuerpo;
    }

    private static IntegralInfo localizarIntegral(NodoAST n, boolean definida) {
        return localizarIntegralRec(null, n, definida);
    }

    private static IntegralInfo localizarIntegralRec(NodoAST parent, NodoAST n, boolean definida) {
        if (n == null || n.token == null) return null;
        if ((!definida && n.token.type == LexToken.Type.INTEGRAL_INDEF) || (definida && n.token.type == LexToken.Type.INTEGRAL_DEF)) {
            IntegralInfo ii = new IntegralInfo();
            ii.nodoIntegral = n;
            ii.padre = parent;
            ii.definida = definida;
            if (!definida) {
                ii.cuerpo = n.hijos.size() > 0 ? n.hijos.get(0) : null;
                NodoAST dx = n.hijos.size() > 1 ? n.hijos.get(1) : null;
                ii.var = dx != null && dx.token != null ? extraerDxVar(dx.token.value) : "x";
            } else {
                ii.inf = n.hijos.size() > 0 ? n.hijos.get(0) : null;
                ii.sup = n.hijos.size() > 1 ? n.hijos.get(1) : null;
                ii.cuerpo = n.hijos.size() > 2 ? n.hijos.get(2) : null;
                NodoAST dx = n.hijos.size() > 3 ? n.hijos.get(3) : null;
                ii.var = dx != null && dx.token != null ? extraerDxVar(dx.token.value) : "x";
            }
            return ii;
        }
        for (NodoAST h: n.hijos) {
            IntegralInfo x = localizarIntegralRec(n, h, definida);
            if (x != null) return x;
        }
        return null;
    }

    private static String extraerDxVar(String dx) {
        if (dx == null) return "x";
        String s = dx.trim();
        if (s.equals("dx")) return "x";
        if (s.equals("dy")) return "y";
        if (s.length() >= 2 && s.charAt(0) == 'd') return s.substring(1);
        return "x";
    }

    private static NodoAST reemplazar(NodoAST objetivo, NodoAST valor, NodoAST padre, NodoAST raizSiTop) {
        if (padre == null) return valor;
        int i = padre.hijos.indexOf(objetivo);
        if (i >= 0) {
            padre.hijos.set(i, valor);
            valor.parent = padre;
            return subirAlRoot(padre);
        }
        return raizSiTop;
    }

    private static NodoAST subirAlRoot(NodoAST n) {
        NodoAST p = n;
        while (p != null && p.parent != null) p = p.parent;
        return p == null ? n : p;
    }

    private static boolean isVar(NodoAST n, String v) {
        return n != null && n.token != null && n.token.type == LexToken.Type.VARIABLE && v.equals(n.token.value) && n.hijos.isEmpty();
    }

    private static Double evalIfConst(NodoAST n) {
        return AstUtils.evalConst(n);
    }

    private static Double linearCoeff(NodoAST n, String v) {
        if (n == null || n.token == null) return null;
        if (isVar(n, v)) return 1.0;
        if (AstUtils.isConst(n)) return 0.0;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.MUL) {
            Double a = evalIfConst(n.hijos.get(0));
            Double b = evalIfConst(n.hijos.get(1));
            if (a != null && isVar(n.hijos.get(1), v)) return a;
            if (b != null && isVar(n.hijos.get(0), v)) return b;
            Double c0 = linearCoeff(n.hijos.get(0), v);
            Double c1 = linearCoeff(n.hijos.get(1), v);
            if (c0 != null && c1 != null) {
                if (Math.abs(c0) < 1e-12 && evalIfConst(n.hijos.get(1)) != null) return 0.0;
                if (Math.abs(c1) < 1e-12 && evalIfConst(n.hijos.get(0)) != null) return 0.0;
            }
            return null;
        }
        if (t == LexToken.Type.DIV) {
            Double d = evalIfConst(n.hijos.get(1));
            if (d == null || Math.abs(d) < 1e-15) return null;
            Double c = linearCoeff(n.hijos.get(0), v);
            if (c == null) return null;
            return c / d;
        }
        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB) {
            Double c0 = linearCoeff(n.hijos.get(0), v);
            Double c1 = linearCoeff(n.hijos.get(1), v);
            if (c0 == null || c1 == null) {
                if (c0 != null && AstUtils.isConst(n.hijos.get(1))) return c0;
                if (c1 != null && AstUtils.isConst(n.hijos.get(0))) return t == LexToken.Type.SUM ? c1 : -c1;
                return null;
            }
            return t == LexToken.Type.SUM ? (c0 + c1) : (c0 - c1);
        }
        if (t == LexToken.Type.EXP) return null;
        if (t == LexToken.Type.RADICAL) return null;
        return null;
    }

    private static NodoAST mulConst(double c, NodoAST f) {
        if (Math.abs(c - 1.0) < 1e-12) return f;
        if (Math.abs(c + 1.0) < 1e-12) return AstUtils.bin(LexToken.Type.MUL, AstUtils.number(-1), f, "*", 6);
        return AstUtils.bin(LexToken.Type.MUL, AstUtils.number(c), f, "*", 6);
    }

    private static boolean isPowOf(NodoAST n, LexToken.Type baseType, double exp) {
        if (n == null || n.token == null || n.token.type != LexToken.Type.EXP) return false;
        NodoAST b = n.hijos.get(0);
        NodoAST e = n.hijos.get(1);
        if (b == null || b.token == null || b.token.type != baseType) return false;
        Double ee = AstUtils.evalConst(e);
        if (ee == null) return false;
        return Math.abs(ee - exp) < 1e-12;
    }

    private static boolean sameExpr(NodoAST a, NodoAST b) {
        String sa = AstUtils.toSymja(a);
        String sb = AstUtils.toSymja(b);
        return sa != null && sb != null && sa.equals(sb);
    }

    private static NodoAST sqrSinOn(NodoAST u) {
        NodoAST sinU = AstUtils.un(LexToken.Type.TRIG_SIN, AstUtils.cloneTree(u), "sin", 8);
        return AstUtils.bin(LexToken.Type.EXP, sinU, AstUtils.number(2), "^", 7);
    }

    private static NodoAST integralRec(NodoAST f, String v) {
        if (f == null || f.token == null) return null;
        LexToken.Type t = f.token.type;
        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL || t == LexToken.Type.CONST_E || t == LexToken.Type.CONST_PI) {
            NodoAST x = AstUtils.atom(LexToken.Type.VARIABLE, v, 1);
            return AstUtils.bin(LexToken.Type.MUL, f, x, "*", 6);
        }
        if (t == LexToken.Type.VARIABLE) {
            if (!f.token.value.equals(v)) return null;
            return AstUtils.bin(LexToken.Type.DIV,
                    AstUtils.bin(LexToken.Type.EXP, f, AstUtils.number(2), "^", 7),
                    AstUtils.number(2), "/", 6);
        }
        if (t == LexToken.Type.EXP && f.hijos.get(0).token.type == LexToken.Type.VARIABLE && f.hijos.get(0).token.value.equals(v)) {
            Double e = AstUtils.evalConst(f.hijos.get(1));
            if (e != null && Math.abs(e + 1.0) > 1e-12) {
                double n = e + 1.0;
                NodoAST xnp1 = AstUtils.bin(LexToken.Type.EXP, f.hijos.get(0), AstUtils.number(n), "^", 7);
                return AstUtils.bin(LexToken.Type.DIV, xnp1, AstUtils.number(n), "/", 6);
            }
            return null;
        }
        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB) {
            NodoAST a = integralRec(f.hijos.get(0), v);
            NodoAST b = integralRec(f.hijos.get(1), v);
            if (a == null || b == null) return null;
            return AstUtils.bin(t, a, b, f.token.value, f.token.prioridad);
        }
        if (t == LexToken.Type.MUL) {
            NodoAST a = f.hijos.get(0);
            NodoAST b = f.hijos.get(1);
            if (AstUtils.isConst(a)) {
                NodoAST Ib = integralRec(b, v);
                if (Ib != null) return AstUtils.bin(LexToken.Type.MUL, a, Ib, "*", 6);
            }
            if (AstUtils.isConst(b)) {
                NodoAST Ia = integralRec(a, v);
                if (Ia != null) return AstUtils.bin(LexToken.Type.MUL, b, Ia, "*", 6);
            }
            if (a.token != null && b.token != null) {
                if ((a.token.type == LexToken.Type.TRIG_SEC && b.token.type == LexToken.Type.TRIG_TAN) ||
                        (a.token.type == LexToken.Type.TRIG_TAN && b.token.type == LexToken.Type.TRIG_SEC)) {
                    NodoAST ua = a.hijos.get(0);
                    NodoAST ub = b.hijos.get(0);
                    if (sameExpr(ua, ub)) {
                        Double k = linearCoeff(ua, v);
                        if (k != null && Math.abs(k) > 1e-15) {
                            NodoAST secu = AstUtils.un(LexToken.Type.TRIG_SEC, AstUtils.cloneTree(ua), "sec", 8);
                            return mulConst(1.0 / k, secu);
                        }
                    }
                }
                if ((a.token.type == LexToken.Type.TRIG_CSC && b.token.type == LexToken.Type.TRIG_COT) ||
                        (a.token.type == LexToken.Type.TRIG_COT && b.token.type == LexToken.Type.TRIG_CSC)) {
                    NodoAST ua = a.hijos.get(0);
                    NodoAST ub = b.hijos.get(0);
                    if (sameExpr(ua, ub)) {
                        Double k = linearCoeff(ua, v);
                        if (k != null && Math.abs(k) > 1e-15) {
                            NodoAST cscu = AstUtils.un(LexToken.Type.TRIG_CSC, AstUtils.cloneTree(ua), "csc", 8);
                            return mulConst(-1.0 / k, cscu);
                        }
                    }
                }
                if ((a.token.type == LexToken.Type.TRIG_SIN && b.token.type == LexToken.Type.TRIG_COS) ||
                        (a.token.type == LexToken.Type.TRIG_COS && b.token.type == LexToken.Type.TRIG_SIN)) {
                    NodoAST ua = a.hijos.get(0);
                    NodoAST ub = b.hijos.get(0);
                    if (sameExpr(ua, ub)) {
                        Double k = linearCoeff(ua, v);
                        if (k != null && Math.abs(k) > 1e-15) {
                            NodoAST sin2 = sqrSinOn(ua);
                            return mulConst(1.0 / (2.0 * k), sin2);
                        }
                    }
                }
            }
            return null;
        }
        if (t == LexToken.Type.DIV) {
            if (AstUtils.isConst(f.hijos.get(1))) {
                NodoAST num = integralRec(f.hijos.get(0), v);
                if (num != null) return AstUtils.bin(LexToken.Type.DIV, num, f.hijos.get(1), "/", 6);
            }
            return null;
        }
        if (t == LexToken.Type.TRIG_SIN) {
            NodoAST u = f.hijos.get(0);
            Double k = linearCoeff(u, v);
            if (k == null || Math.abs(k) < 1e-15) return null;
            NodoAST cosu = AstUtils.un(LexToken.Type.TRIG_COS, u, "cos", 8);
            return mulConst(-1.0 / k, cosu);
        }
        if (t == LexToken.Type.TRIG_COS) {
            NodoAST u = f.hijos.get(0);
            Double k = linearCoeff(u, v);
            if (k == null || Math.abs(k) < 1e-15) return null;
            NodoAST sinu = AstUtils.un(LexToken.Type.TRIG_SIN, u, "sin", 8);
            return mulConst(1.0 / k, sinu);
        }
        if (t == LexToken.Type.TRIG_TAN) {
            NodoAST u = f.hijos.get(0);
            Double k = linearCoeff(u, v);
            if (k == null || Math.abs(k) < 1e-15) return null;
            NodoAST cosu = AstUtils.un(LexToken.Type.TRIG_COS, u, "cos", 8);
            NodoAST lnc = AstUtils.un(LexToken.Type.LN, cosu, "ln", 8);
            return mulConst(-1.0 / k, lnc);
        }
        if (isPowOf(f, LexToken.Type.TRIG_SEC, 2.0)) {
            NodoAST u = f.hijos.get(0).hijos.get(0);
            Double k = linearCoeff(u, v);
            if (k == null || Math.abs(k) < 1e-15) return null;
            NodoAST tanu = AstUtils.un(LexToken.Type.TRIG_TAN, u, "tan", 8);
            return mulConst(1.0 / k, tanu);
        }
        if (isPowOf(f, LexToken.Type.TRIG_CSC, 2.0)) {
            NodoAST u = f.hijos.get(0).hijos.get(0);
            Double k = linearCoeff(u, v);
            if (k == null || Math.abs(k) < 1e-15) return null;
            NodoAST cotu = AstUtils.un(LexToken.Type.TRIG_COT, u, "cot", 8);
            return mulConst(-1.0 / k, cotu);
        }
        return null;
    }
}
