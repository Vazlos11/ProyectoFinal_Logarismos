package com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.Acc;

import java.util.ArrayList;
import java.util.List;

public final class IntegralUtils {
    public static class IntegralInfo { public NodoAST nodoIntegral; public NodoAST padre; public boolean definida; public String var; public NodoAST inf; public NodoAST sup; public NodoAST cuerpo; }
    public static class Poly { public int grado; public double coef; public NodoAST resto; }
    public static class MulSplit { public double c; public List<NodoAST> nonconst; }
    public static class MonoExpLin { public int n; public double coef; public NodoAST u; public double k; }
    public static class MonoTrigLin { public int n; public double coef; public NodoAST u; public double k; }

    public static IntegralInfo localizarIntegral(NodoAST n, boolean definida) { return localizarIntegralRec(null, n, definida); }

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
                ii.var = dx != null && dx.token != null && dx.token.value != null && dx.token.value.startsWith("d") && dx.token.value.length() > 1 ? dx.token.value.substring(1) : "x";
            } else {
                ii.inf = n.hijos.size() > 0 ? n.hijos.get(0) : null;
                ii.sup = n.hijos.size() > 1 ? n.hijos.get(1) : null;
                ii.cuerpo = n.hijos.size() > 2 ? n.hijos.get(2) : null;
                NodoAST dx = n.hijos.size() > 3 ? n.hijos.get(3) : null;
                ii.var = dx != null && dx.token != null && dx.token.value != null && dx.token.value.startsWith("d") && dx.token.value.length() > 1 ? dx.token.value.substring(1) : "x";
            }
            return ii;
        }
        for (NodoAST h : n.hijos) {
            IntegralInfo z = localizarIntegralRec(n, h, definida);
            if (z != null) return z;
        }
        return null;
    }

    public static NodoAST reemplazar(NodoAST objetivo, NodoAST valor, NodoAST padre, NodoAST raizSiTop) {
        if (padre == null) return valor;
        int i = padre.hijos.indexOf(objetivo);
        if (i >= 0) { padre.hijos.set(i, valor); valor.parent = padre; return root(padre); }
        return raizSiTop;
    }

    private static NodoAST root(NodoAST n) { NodoAST p = n; while (p != null && p.parent != null) p = p.parent; return p == null ? n : p; }

    public static NodoAST mulC(NodoAST a, double c) {
        if (Math.abs(c-1.0)<1e-15) return a;
        if (Math.abs(c) < 1e-15) return AstUtils.number(0.0);
        double inv = Math.rint(1.0/c);
        boolean useFrac = Math.abs(c*inv - 1.0) < 1e-12 && Math.abs(inv) <= 100000;
        if (useFrac) return AstUtils.bin(LexToken.Type.DIV, a, AstUtils.number(inv), "/", 6);
        return AstUtils.bin(LexToken.Type.MUL, AstUtils.number(c), a, "*", 6);
    }

    public static NodoAST sum(NodoAST a, NodoAST b) { return AstUtils.bin(LexToken.Type.SUM, a, b, "+", 5); }
    public static NodoAST sub(NodoAST a, NodoAST b) { return AstUtils.bin(LexToken.Type.SUB, a, b, "-", 5); }

    public static NodoAST xPow(String v, int n) {
        if (n <= 0) return AstUtils.number(1.0);
        NodoAST x = AstUtils.atom(LexToken.Type.VARIABLE, v, 1);
        return AstUtils.bin(LexToken.Type.EXP, x, AstUtils.number(n), "^", 7);
    }

    public static NodoAST ePowClone(NodoAST u) {
        NodoAST uClone = AstUtils.cloneTree(u);
        NodoAST e = AstUtils.atom(LexToken.Type.CONST_E, "e", 1);
        return AstUtils.bin(LexToken.Type.EXP, e, uClone, "^", 7);
    }
    public static NodoAST sinClone(NodoAST u) { return AstUtils.un(LexToken.Type.TRIG_SIN, AstUtils.cloneTree(u), "sin", 8); }
    public static NodoAST cosClone(NodoAST u) { return AstUtils.un(LexToken.Type.TRIG_COS, AstUtils.cloneTree(u), "cos", 8); }
    public static NodoAST lnClone(NodoAST n) { return AstUtils.un(LexToken.Type.LN, AstUtils.cloneTree(n), "ln", 9); }

    public static Double linearCoeff(NodoAST n, String v) {
        if (n == null || n.token == null) return null;
        if (n.token.type == LexToken.Type.VARIABLE && v.equals(n.token.value) && n.hijos.isEmpty()) return 1.0;
        if (AstUtils.isConst(n)) return 0.0;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.MUL) {
            Double a = AstUtils.evalConst(n.hijos.get(0));
            Double b = AstUtils.evalConst(n.hijos.get(1));
            if (a != null && n.hijos.get(1).token.type == LexToken.Type.VARIABLE && v.equals(n.hijos.get(1).token.value)) return a;
            if (b != null && n.hijos.get(0).token.type == LexToken.Type.VARIABLE && v.equals(n.hijos.get(0).token.value)) return b;
        }
        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB) {
            Double c0 = linearCoeff(n.hijos.get(0), v);
            Double c1 = linearCoeff(n.hijos.get(1), v);
            if (c0 != null && c1 != null) return c0 + (t == LexToken.Type.SUM ? c1 : -c1);
        }
        return null;
    }

    public static boolean esExpDeLineal(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (n.token.type != LexToken.Type.EXP || n.hijos.size() != 2) return false;
        if (n.hijos.get(0) == null || n.hijos.get(0).token == null) return false;
        return n.hijos.get(0).token.type == LexToken.Type.CONST_E;
    }
    public static boolean esTrigSin(NodoAST n) { return n != null && n.token != null && n.token.type == LexToken.Type.TRIG_SIN && n.hijos.size() == 1; }
    public static boolean esTrigCos(NodoAST n) { return n != null && n.token != null && n.token.type == LexToken.Type.TRIG_COS && n.hijos.size() == 1; }

    public static Poly esMonomioEn(NodoAST n, String v) {
        if (n == null || n.token == null) return null;
        Double c = AstUtils.evalConst(n);
        if (c != null) { Poly p = new Poly(); p.grado = 0; p.coef = c; p.resto = AstUtils.number(1.0); return p; }
        if (n.token.type == LexToken.Type.VARIABLE && v.equals(n.token.value)) { Poly p = new Poly(); p.grado = 1; p.coef = 1.0; p.resto = AstUtils.number(1.0); return p; }
        if (n.token.type == LexToken.Type.EXP && n.hijos.size() == 2 && n.hijos.get(0).token != null && n.hijos.get(0).token.type == LexToken.Type.VARIABLE && v.equals(n.hijos.get(0).token.value)) {
            Double e = AstUtils.evalConst(n.hijos.get(1));
            if (e != null && e >= 0 && Math.abs(e - Math.rint(e)) < 1e-12) { Poly p = new Poly(); p.grado = (int)Math.rint(e); p.coef = 1.0; p.resto = AstUtils.number(1.0); return p; }
        }
        if (n.token.type == LexToken.Type.MUL && n.hijos.size() == 2) {
            Poly a = esMonomioEn(n.hijos.get(0), v);
            Poly b = esMonomioEn(n.hijos.get(1), v);
            if (a != null && b != null) { Poly p = new Poly(); p.grado = a.grado + b.grado; p.coef = a.coef * b.coef; p.resto = AstUtils.number(1.0); return p; }
            if (a != null) {
                Double cb = AstUtils.evalConst(n.hijos.get(1));
                if (cb != null) { Poly p = new Poly(); p.grado = a.grado; p.coef = a.coef * cb; p.resto = AstUtils.number(1.0); return p; }
                if (esExpDeLineal(n.hijos.get(1)) || esTrigSin(n.hijos.get(1)) || esTrigCos(n.hijos.get(1))) { Poly p = new Poly(); p.grado = a.grado; p.coef = a.coef; p.resto = n.hijos.get(1); return p; }
            }
            if (b != null) {
                Double ca = AstUtils.evalConst(n.hijos.get(0));
                if (ca != null) { Poly p = new Poly(); p.grado = b.grado; p.coef = b.coef * ca; p.resto = AstUtils.number(1.0); return p; }
                if (esExpDeLineal(n.hijos.get(0)) || esTrigSin(n.hijos.get(0)) || esTrigCos(n.hijos.get(0))) { Poly p = new Poly(); p.grado = b.grado; p.coef = b.coef; p.resto = n.hijos.get(0); return p; }
            }
        }
        return null;
    }

    public static Acc collectForParts(NodoAST n, String v) {
        Acc a = new Acc();
        a.ok = true;
        a.constCoef = 1.0;
        for (NodoAST f : flattenMul(n)) {
            Double c = AstUtils.evalConst(f);
            if (c != null) { a.constCoef *= c; continue; }
            Poly p = esMonomioEn(f, v);
            if (p != null && (p.resto == null || AstUtils.isConst(p.resto))) { a.deg += p.grado; a.constCoef *= p.coef; continue; }
            if (esExpDeLineal(f)) {
                Double k = linearCoeff(f.hijos.get(1), v);
                if (k == null) { a.ok = false; return a; }
                a.countExpLin += 1; a.uExp = f.hijos.get(1); continue;
            }
            if (esTrigSin(f)) {
                Double k = linearCoeff(f.hijos.get(0), v);
                if (k == null) { a.ok = false; return a; }
                a.countSinLin += 1; a.uSin = f.hijos.get(0); continue;
            }
            if (esTrigCos(f)) {
                Double k = linearCoeff(f.hijos.get(0), v);
                if (k == null) { a.ok = false; return a; }
                a.countCosLin += 1; a.uCos = f.hijos.get(0); continue;
            }
            a.ok = false;
            return a;
        }
        return a;
    }

    public static List<NodoAST> flattenMul(NodoAST n) {
        List<NodoAST> out = new ArrayList<>();
        flattenMulRec(n, out);
        return out;
    }
    private static void flattenMulRec(NodoAST n, List<NodoAST> out) {
        if (n != null && n.token != null && n.token.type == LexToken.Type.MUL && n.hijos.size() == 2) {
            flattenMulRec(n.hijos.get(0), out);
            flattenMulRec(n.hijos.get(1), out);
        } else {
            out.add(n);
        }
    }
    public static NodoAST rebuildMul(List<NodoAST> fs) {
        if (fs.isEmpty()) return AstUtils.number(1.0);
        NodoAST acc = fs.get(0);
        for (int i = 1; i < fs.size(); i++) acc = AstUtils.bin(LexToken.Type.MUL, acc, fs.get(i), "*", 6);
        return acc;
    }
    public static MulSplit splitMul(NodoAST n) {
        MulSplit r = new MulSplit();
        r.c = 1.0;
        r.nonconst = new ArrayList<>();
        for (NodoAST f : flattenMul(n)) {
            Double c = AstUtils.evalConst(f);
            if (c != null) r.c *= c; else r.nonconst.add(f);
        }
        return r;
    }

    public static boolean esCicloExpTrig(NodoAST n, String v) {
        if (n == null || n.token == null || n.token.type != LexToken.Type.MUL || n.hijos.size()!=2) return false;
        NodoAST a = n.hijos.get(0), b = n.hijos.get(1);
        NodoAST exp = esExpDeLineal(a) ? a : esExpDeLineal(b) ? b : null;
        NodoAST trg = esTrigSin(a) || esTrigCos(a) ? a : (esTrigSin(b) || esTrigCos(b) ? b : null);
        if (exp == null || trg == null) return false;
        Double ka = linearCoeff(exp.hijos.get(1), v);
        Double kb = linearCoeff(trg.hijos.get(0), v);
        return ka != null && kb != null && Math.abs(ka) > 0 && Math.abs(kb) > 0;
    }
    public static NodoAST resolverCicloExpTrig(NodoAST n, String v) {
        NodoAST a = n.hijos.get(0), b = n.hijos.get(1);
        NodoAST exp = esExpDeLineal(a) ? a : b;
        NodoAST trg = esTrigSin(a) || esTrigCos(a) ? a : b;
        NodoAST u = exp.hijos.get(1);
        NodoAST w = trg.hijos.get(0);
        Double ka = linearCoeff(u, v);
        Double kb = linearCoeff(w, v);
        double denom = ka*ka + kb*kb;
        NodoAST numer;
        if (esTrigSin(trg)) {
            NodoAST sinw = sinClone(w);
            NodoAST cosw = cosClone(w);
            NodoAST t1 = mulC(sinw, ka);
            NodoAST t2 = mulC(cosw, -kb);
            numer = sum(t1, t2);
        } else {
            NodoAST cosw = cosClone(w);
            NodoAST sinw = sinClone(w);
            NodoAST t1 = mulC(cosw, ka);
            NodoAST t2 = mulC(sinw, kb);
            numer = sum(t1, t2);
        }
        NodoAST prod = AstUtils.bin(LexToken.Type.MUL, ePowClone(u), numer, "*", 6);
        return mulC(prod, 1.0/denom);
    }

    public static boolean esLnPorMonomio(NodoAST n, String v) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.MUL && n.hijos.size() == 2) {
            NodoAST a = n.hijos.get(0), b = n.hijos.get(1);
            if (a.token.type == LexToken.Type.LN && esMonomioEn(b, v) != null && a.hijos.size()==1 && a.hijos.get(0).token.type==LexToken.Type.VARIABLE && v.equals(a.hijos.get(0).token.value)) return true;
            if (b.token.type == LexToken.Type.LN && esMonomioEn(a, v) != null && b.hijos.size()==1 && b.hijos.get(0).token.type==LexToken.Type.VARIABLE && v.equals(b.hijos.get(0).token.value)) return true;
        }
        return false;
    }
    public static NodoAST integrarLnPorMonomio(NodoAST n, String v) {
        NodoAST ln; Poly p;
        if (n.hijos.get(0).token.type == LexToken.Type.LN) { ln = n.hijos.get(0); p = esMonomioEn(n.hijos.get(1), v); }
        else { ln = n.hijos.get(1); p = esMonomioEn(n.hijos.get(0), v); }
        if (p == null) return null;
        int ndeg = p.grado;
        double c = p.coef;
        double denom = ndeg + 1.0;
        NodoAST xnp1 = xPow(v, ndeg+1);
        NodoAST term1 = AstUtils.bin(LexToken.Type.MUL, xnp1, lnClone(ln.hijos.get(0)), "*", 6);
        term1 = mulC(term1, c/denom);
        NodoAST term2 = mulC(xnp1, c/(denom*denom));
        return sub(term1, term2);
    }

    public static NodoAST integralRec(NodoAST f, String v) {
        if (f == null || f.token == null) return null;
        LexToken.Type t = f.token.type;
        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL || t == LexToken.Type.CONST_E || t == LexToken.Type.CONST_PI) {
            NodoAST x = AstUtils.atom(LexToken.Type.VARIABLE, v, 1);
            return AstUtils.bin(LexToken.Type.MUL, f, x, "*", 6);
        }
        if (t == LexToken.Type.VARIABLE) {
            if (!v.equals(f.token.value)) return null;
            return AstUtils.bin(LexToken.Type.DIV, AstUtils.bin(LexToken.Type.EXP, f, AstUtils.number(2), "^", 7), AstUtils.number(2), "/", 6);
        }
        if (t == LexToken.Type.EXP && f.hijos.size() == 2 && f.hijos.get(0) != null && f.hijos.get(0).token != null && f.hijos.get(0).token.type == LexToken.Type.CONST_E) {
            Double k = linearCoeff(f.hijos.get(1), v);
            if (k == null || Math.abs(k) < 1e-15) return null;
            NodoAST eu = ePowClone(f.hijos.get(1));
            return mulC(eu, 1.0 / k);
        }
        if (t == LexToken.Type.EXP && f.hijos.size() == 2 && f.hijos.get(0).token != null && f.hijos.get(0).token.type == LexToken.Type.VARIABLE && v.equals(f.hijos.get(0).token.value)) {
            Double e = AstUtils.evalConst(f.hijos.get(1));
            if (e != null) {
                if (Math.abs(e + 1.0) < 1e-12) {
                    NodoAST x = AstUtils.atom(LexToken.Type.VARIABLE, v, 1);
                    return AstUtils.un(LexToken.Type.LN, x, "ln", 9);
                } else {
                    double n = e + 1.0;
                    NodoAST xnp1 = AstUtils.bin(LexToken.Type.EXP, f.hijos.get(0), AstUtils.number(n), "^", 7);
                    return AstUtils.bin(LexToken.Type.DIV, xnp1, AstUtils.number(n), "/", 6);
                }
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
            MulSplit ms = splitMul(f);
            if (ms.nonconst.isEmpty()) {
                NodoAST x = AstUtils.atom(LexToken.Type.VARIABLE, v, 1);
                return mulC(x, ms.c);
            }
            NodoAST rest = rebuildMul(ms.nonconst);
            NodoAST Irest = integralRec(rest, v);
            if (Irest != null) return mulC(Irest, ms.c);
            return null;
        }
        if (t == LexToken.Type.TRIG_SIN) {
            NodoAST u = f.hijos.get(0);
            Double k = linearCoeff(u, v);
            if (k == null || Math.abs(k) < 1e-15) return null;
            NodoAST cosu = cosClone(u);
            return mulC(cosu, -1.0 / k);
        }
        if (t == LexToken.Type.TRIG_COS) {
            NodoAST u = f.hijos.get(0);
            Double k = linearCoeff(u, v);
            if (k == null || Math.abs(k) < 1e-15) return null;
            NodoAST sinu = sinClone(u);
            return mulC(sinu, 1.0 / k);
        }
        if (t == LexToken.Type.TRIG_TAN && f.hijos.size() == 1) {
            NodoAST u = f.hijos.get(0);
            Double k = linearCoeff(u, v);
            if (k == null || Math.abs(k) < 1e-15) return null;
            NodoAST cosu = cosClone(u);
            NodoAST lncos = lnClone(cosu);
            return mulC(lncos, -1.0 / k);
        }
        return null;
    }

    public static NodoAST candidatoInterior(NodoAST n) {
        if (n == null || n.token == null) return null;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.TRIG_SIN || t == LexToken.Type.TRIG_COS || t == LexToken.Type.TRIG_TAN || t == LexToken.Type.LOG || t == LexToken.Type.LN) return n.hijos.size() > 0 ? n.hijos.get(0) : null;
        if (t == LexToken.Type.EXP && n.hijos.size() == 2) {
            NodoAST base = n.hijos.get(0);
            Double e = AstUtils.evalConst(n.hijos.get(1));
            if (e == null) return base;
        }
        if (t == LexToken.Type.MUL) {
            for (NodoAST h : n.hijos) {
                NodoAST c = candidatoInterior(h);
                if (c != null) return c;
            }
        }
        return null;
    }

    public static NodoAST reemplazarSubexp(NodoAST raiz, NodoAST objetivo, NodoAST con) {
        if (raiz == objetivo) return con;
        NodoAST c = new NodoAST(raiz.token == null ? null : new LexToken(raiz.token.type, raiz.token.value, raiz.token.prioridad));
        for (NodoAST h : raiz.hijos) c.addHijo(reemplazarSubexp(h, objetivo, con));
        return c;
    }
}
