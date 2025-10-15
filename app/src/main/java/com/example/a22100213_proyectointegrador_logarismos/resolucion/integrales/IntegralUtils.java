package com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.Acc;

import java.util.ArrayList;
import java.util.List;

public final class IntegralUtils {
    public static class IntegralInfo {
        public NodoAST nodoIntegral;
        public NodoAST padre;
        public boolean definida;
        public String var;
        public NodoAST inf;
        public NodoAST sup;
        public NodoAST cuerpo;
    }

    public static class Poly {
        public int grado;
        public double coef;
        public NodoAST resto;
    }

    public static class MulSplit {
        public double c;
        public List<NodoAST> nonconst;
    }

    public static class MonoExpLin {
        public int n;
        public double coef;
        public NodoAST u;
        public double k;
    }

    public static class MonoTrigLin {
        public int n;
        public double coef;
        public NodoAST u;
        public double k;
    }

    public static final int MAX_SERIALIZE_DEPTH = 2000;

    public static IntegralInfo localizarIntegral(NodoAST n, boolean definida) {
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
        NodoAST valorClonado = AstUtils.cloneTree(valor);

        if (padre == null) {
            return AstUtils.cloneTree(valorClonado);
        }

        int i = padre.hijos.indexOf(objetivo);
        if (i >= 0) {
            padre.hijos.set(i, valorClonado);
            valorClonado.parent = padre;

            if (objetivo != null) {
                objetivo.parent = null;
            }

            NodoAST r = root(padre);
            return AstUtils.cloneTree(r);
        }

        return AstUtils.cloneTree(raizSiTop != null ? raizSiTop : valorClonado);
    }

    private static NodoAST root(NodoAST n) {
        java.util.HashSet<NodoAST> seen = new java.util.HashSet<>();
        NodoAST p = n;
        while (p != null && p.parent != null && !seen.contains(p)) {
            seen.add(p);
            p = p.parent;
        }
        return (p == null) ? n : p;
    }

    public static NodoAST mulC(NodoAST a, double c) {
        if (Math.abs(c - 1.0) < 1e-15) return a;
        if (Math.abs(c) < 1e-15) return AstUtils.number(0.0);
        double inv = Math.rint(1.0 / c);
        boolean useFrac = Math.abs(c * inv - 1.0) < 1e-12 && Math.abs(inv) <= 100000;
        if (useFrac) return AstUtils.bin(LexToken.Type.DIV, a, AstUtils.number(inv), "/", 6);
        return AstUtils.bin(LexToken.Type.MUL, AstUtils.number(c), a, "*", 6);
    }

    public static NodoAST sum(NodoAST a, NodoAST b) {
        return AstUtils.bin(LexToken.Type.SUM, a, b, "+", 5);
    }

    public static NodoAST sub(NodoAST a, NodoAST b) {
        return AstUtils.bin(LexToken.Type.SUB, a, b, "-", 5);
    }

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

    public static NodoAST sinClone(NodoAST u) {
        return AstUtils.un(LexToken.Type.TRIG_SIN, AstUtils.cloneTree(u), "sin", 8);
    }

    public static NodoAST cosClone(NodoAST u) {
        return AstUtils.un(LexToken.Type.TRIG_COS, AstUtils.cloneTree(u), "cos", 8);
    }

    public static NodoAST lnClone(NodoAST n) {
        return AstUtils.un(LexToken.Type.LN, AstUtils.cloneTree(n), "ln", 9);
    }

    public static Double linearCoeff(NodoAST n, String v) {
        if (n == null || n.token == null) return null;
        LexToken.Type t = n.token.type;

        if (t == LexToken.Type.VARIABLE && v.equals(n.token.value) && n.hijos.isEmpty()) return 1.0;
        if (AstUtils.isConst(n)) return 0.0;

        if (t == LexToken.Type.MUL && n.hijos.size() == 2) {
            Double cL = AstUtils.evalConst(n.hijos.get(0));
            Double cR = AstUtils.evalConst(n.hijos.get(1));
            if (cL != null) {
                Double k = linearCoeff(n.hijos.get(1), v);
                return (k == null) ? null : cL * k;
            }
            if (cR != null) {
                Double k = linearCoeff(n.hijos.get(0), v);
                return (k == null) ? null : cR * k;
            }
            return null;
        }

        if ((t == LexToken.Type.SUM || t == LexToken.Type.SUB) && n.hijos.size() == 2) {
            Double k1 = linearCoeff(n.hijos.get(0), v);
            Double k2 = linearCoeff(n.hijos.get(1), v);
            if (k1 == null || k2 == null) return null;
            return (t == LexToken.Type.SUM) ? (k1 + k2) : (k1 - k2);
        }
        return null;
    }



    public static boolean esExpDeLineal(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (n.token.type != LexToken.Type.EXP || n.hijos.size() != 2) return false;
        if (n.hijos.get(0) == null || n.hijos.get(0).token == null) return false;
        return n.hijos.get(0).token.type == LexToken.Type.CONST_E;
    }

    public static boolean esTrigSin(NodoAST n) {
        return n != null && n.token != null && n.token.type == LexToken.Type.TRIG_SIN && n.hijos.size() == 1;
    }

    public static boolean esTrigCos(NodoAST n) {
        return n != null && n.token != null && n.token.type == LexToken.Type.TRIG_COS && n.hijos.size() == 1;
    }

    public static Poly esMonomioEn(NodoAST n, String v) {
        if (n == null || n.token == null) return null;
        Double c = AstUtils.evalConst(n);
        if (c != null) {
            Poly p = new Poly();
            p.grado = 0;
            p.coef = c;
            p.resto = AstUtils.number(1.0);
            return p;
        }
        if (n.token.type == LexToken.Type.VARIABLE && v.equals(n.token.value)) {
            Poly p = new Poly();
            p.grado = 1;
            p.coef = 1.0;
            p.resto = AstUtils.number(1.0);
            return p;
        }
        if (n.token.type == LexToken.Type.EXP && n.hijos.size() == 2 && n.hijos.get(0).token != null && n.hijos.get(0).token.type == LexToken.Type.VARIABLE && v.equals(n.hijos.get(0).token.value)) {
            Double e = AstUtils.evalConst(n.hijos.get(1));
            if (e != null && e >= 0 && Math.abs(e - Math.rint(e)) < 1e-12) {
                Poly p = new Poly();
                p.grado = (int) Math.rint(e);
                p.coef = 1.0;
                p.resto = AstUtils.number(1.0);
                return p;
            }
        }
        if (n.token.type == LexToken.Type.MUL && n.hijos.size() == 2) {
            Poly a = esMonomioEn(n.hijos.get(0), v);
            Poly b = esMonomioEn(n.hijos.get(1), v);
            if (a != null && b != null) {
                Poly p = new Poly();
                p.grado = a.grado + b.grado;
                p.coef = a.coef * b.coef;
                p.resto = AstUtils.number(1.0);
                return p;
            }
            if (a != null) {
                Double cb = AstUtils.evalConst(n.hijos.get(1));
                if (cb != null) {
                    Poly p = new Poly();
                    p.grado = a.grado;
                    p.coef = a.coef * cb;
                    p.resto = AstUtils.number(1.0);
                    return p;
                }
                if (esExpDeLineal(n.hijos.get(1)) || esTrigSin(n.hijos.get(1)) || esTrigCos(n.hijos.get(1))) {
                    Poly p = new Poly();
                    p.grado = a.grado;
                    p.coef = a.coef;
                    p.resto = n.hijos.get(1);
                    return p;
                }
            }
            if (b != null) {
                Double ca = AstUtils.evalConst(n.hijos.get(0));
                if (ca != null) {
                    Poly p = new Poly();
                    p.grado = b.grado;
                    p.coef = b.coef * ca;
                    p.resto = AstUtils.number(1.0);
                    return p;
                }
                if (esExpDeLineal(n.hijos.get(0)) || esTrigSin(n.hijos.get(0)) || esTrigCos(n.hijos.get(0))) {
                    Poly p = new Poly();
                    p.grado = b.grado;
                    p.coef = b.coef;
                    p.resto = n.hijos.get(0);
                    return p;
                }
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
            if (c != null) {
                a.constCoef *= c;
                continue;
            }
            Poly p = esMonomioEn(f, v);
            if (p != null && (p.resto == null || AstUtils.isConst(p.resto))) {
                a.deg += p.grado;
                a.constCoef *= p.coef;
                continue;
            }
            if (esExpDeLineal(f)) {
                Double k = linearCoeff(f.hijos.get(1), v);
                if (k == null) {
                    a.ok = false;
                    return a;
                }
                a.countExpLin += 1;
                a.uExp = f.hijos.get(1);
                continue;
            }
            if (esTrigSin(f)) {
                Double k = linearCoeff(f.hijos.get(0), v);
                if (k == null) {
                    a.ok = false;
                    return a;
                }
                a.countSinLin += 1;
                a.uSin = f.hijos.get(0);
                continue;
            }
            if (esTrigCos(f)) {
                Double k = linearCoeff(f.hijos.get(0), v);
                if (k == null) {
                    a.ok = false;
                    return a;
                }
                a.countCosLin += 1;
                a.uCos = f.hijos.get(0);
                continue;
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
        for (int i = 1; i < fs.size(); i++)
            acc = AstUtils.bin(LexToken.Type.MUL, acc, fs.get(i), "*", 6);
        return acc;
    }

    public static MulSplit splitMul(NodoAST n) {
        MulSplit r = new MulSplit();
        r.c = 1.0;
        r.nonconst = new ArrayList<>();
        for (NodoAST f : flattenMul(n)) {
            Double c = AstUtils.evalConst(f);
            if (c != null) r.c *= c;
            else r.nonconst.add(f);
        }
        return r;
    }

    public static boolean esCicloExpTrig(NodoAST n, String v) {
        if (n == null || n.token == null || n.token.type != LexToken.Type.MUL || n.hijos.size() != 2)
            return false;
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
        double denom = ka * ka + kb * kb;
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
        return mulC(prod, 1.0 / denom);
    }

    public static boolean esLnPorMonomio(NodoAST n, String v) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.MUL && n.hijos.size() == 2) {
            NodoAST a = n.hijos.get(0), b = n.hijos.get(1);
            if (a.token.type == LexToken.Type.LN && esMonomioEn(b, v) != null && a.hijos.size() == 1 && a.hijos.get(0).token.type == LexToken.Type.VARIABLE && v.equals(a.hijos.get(0).token.value))
                return true;
            if (b.token.type == LexToken.Type.LN && esMonomioEn(a, v) != null && b.hijos.size() == 1 && b.hijos.get(0).token.type == LexToken.Type.VARIABLE && v.equals(b.hijos.get(0).token.value))
                return true;
        }
        return false;
    }

    public static NodoAST integrarLnPorMonomio(NodoAST n, String v) {
        NodoAST ln;
        Poly p;
        if (n.hijos.get(0).token.type == LexToken.Type.LN) {
            ln = n.hijos.get(0);
            p = esMonomioEn(n.hijos.get(1), v);
        } else {
            ln = n.hijos.get(1);
            p = esMonomioEn(n.hijos.get(0), v);
        }
        if (p == null) return null;
        int ndeg = p.grado;
        double c = p.coef;
        double denom = ndeg + 1.0;
        NodoAST xnp1 = xPow(v, ndeg + 1);
        NodoAST term1 = AstUtils.bin(LexToken.Type.MUL, xnp1, lnClone(ln.hijos.get(0)), "*", 6);
        term1 = mulC(term1, c / denom);
        NodoAST term2 = mulC(xnp1, c / (denom * denom));
        return sub(term1, term2);
    }
    public static NodoAST integrarMonomioPorExpLineal(NodoAST prod, String v) {
        Acc acc = collectForParts(prod, v);
        if (acc == null || !acc.ok) return null;
        if (acc.countExpLin != 1 || acc.countSinLin != 0 || acc.countCosLin != 0) return null;

        Double k = linearCoeff(acc.uExp, v);
        if (k == null || Math.abs(k) < 1e-15) return null;

        return integrarReduccionExpIterPublic(acc.deg, acc.constCoef, acc.uExp, k, v);
    }

    public static NodoAST integrarReduccionExpIterPublic(int n, double c, NodoAST u, double k, String v) {
        if (Math.abs(k) < 1e-15) return null;

        NodoAST acc = AstUtils.number(0.0);
        int m = n;
        double coefFront = c / k;

        while (m >= 0) {
            NodoAST xm  = xPow(v, m);
            NodoAST epu = ePowClone(u);
            NodoAST term = AstUtils.bin(LexToken.Type.MUL, xm, epu, "*", 6);

            double sign = ((n - m) % 2 == 0) ? 1.0 : -1.0;
            double mult = coefFront * combFactor(n, m) / Math.pow(k, n - m);

            term = mulC(term, sign * mult);
            acc  = sum(acc, term);
            m--;
        }
        return acc;
    }

    private static double combFactor(int n, int m) {
        double num = 1.0;
        for (int i = m + 1; i <= n; i++) num *= i;
        return num;
    }
    public static NodoAST foldEConstTimesXIntoExpLinear(NodoAST mul, String v) {
        if (mul == null || mul.token == null || mul.token.type != LexToken.Type.MUL) return mul;

        List<NodoAST> fs = flattenMul(mul);
        int idxExpConst = -1;
        int idxX = -1;

        for (int i = 0; i < fs.size(); i++) {
            NodoAST f = fs.get(i);
            if (f != null && f.token != null) {
                if (f.token.type == LexToken.Type.EXP
                        && f.hijos.size() == 2
                        && f.hijos.get(0) != null
                        && f.hijos.get(0).token != null
                        && f.hijos.get(0).token.type == LexToken.Type.CONST_E) {
                    Double c = AstUtils.evalConst(f.hijos.get(1));
                    if (c != null) idxExpConst = i;
                }
                if (f.token.type == LexToken.Type.VARIABLE
                        && v.equals(f.token.value)
                        && f.hijos.isEmpty()) {
                    idxX = i;
                }
            }
        }

        if (idxExpConst >= 0 && idxX >= 0) {
            NodoAST exp = fs.get(idxExpConst);
            Double c = AstUtils.evalConst(exp.hijos.get(1));
            if (c == null) return mul;

            NodoAST cx = AstUtils.bin(
                    LexToken.Type.MUL,
                    AstUtils.number(c),
                    AstUtils.atom(LexToken.Type.VARIABLE, v, 1),
                    "*", 6
            );

            NodoAST newExp = AstUtils.bin(
                    LexToken.Type.EXP,
                    AstUtils.atom(LexToken.Type.CONST_E, "e", 1),
                    cx,
                    "^", 7
            );

            int a = Math.max(idxExpConst, idxX);
            int b = Math.min(idxExpConst, idxX);
            fs.remove(a);
            fs.remove(b);
            fs.add(newExp);

            return rebuildMul(fs);
        }
        return mul;
    }
    public static NodoAST integralRec(NodoAST f, String v) {

        if (f == null || f.token == null) return null;
        LexToken.Type t = f.token.type;

        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL
                || t == LexToken.Type.CONST_E || t == LexToken.Type.CONST_PI) {
            NodoAST cte = AstUtils.cloneTree(f);
            NodoAST x = AstUtils.atom(LexToken.Type.VARIABLE, v, 1);
            return AstUtils.bin(LexToken.Type.MUL, cte, x, "*", 6);
        }

        if (t == LexToken.Type.EXP
                && f.hijos.size() == 2
                && f.hijos.get(0) != null
                && f.hijos.get(0).token != null
                && f.hijos.get(0).token.type == LexToken.Type.CONST_E) {

            NodoAST u = f.hijos.get(1);
            Double k = linearCoeff(u, v);
            if (k == null || Math.abs(k) < 1e-15) return null;

            NodoAST ePowU = ePowClone(u);
            return mulC(ePowU, 1.0 / k);
        }

        if (t == LexToken.Type.VARIABLE) {
            if (!v.equals(f.token.value)) return null;
            NodoAST base = AstUtils.cloneTree(f);
            NodoAST x2 = AstUtils.bin(LexToken.Type.EXP, base, AstUtils.number(2), "^", 7);
            return AstUtils.bin(LexToken.Type.DIV, x2, AstUtils.number(2), "/", 6);
        }

        if (t == LexToken.Type.EXP
                && f.hijos.size() == 2
                && f.hijos.get(0) != null
                && f.hijos.get(0).token != null
                && f.hijos.get(0).token.type == LexToken.Type.VARIABLE
                && v.equals(f.hijos.get(0).token.value)) {

            Double e = AstUtils.evalConst(f.hijos.get(1));
            if (e != null) {
                if (Math.abs(e + 1.0) < 1e-12) {
                    NodoAST x = AstUtils.atom(LexToken.Type.VARIABLE, v, 1);
                    return AstUtils.un(LexToken.Type.LN, x, "ln", 9);
                } else {
                    double n = e + 1.0;
                    NodoAST base = AstUtils.cloneTree(f.hijos.get(0));
                    NodoAST xnp1 = AstUtils.bin(LexToken.Type.EXP, base, AstUtils.number(n), "^", 7);
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
            f = foldEConstTimesXIntoExpLinear(f, v);

            Acc acc = collectForParts(f, v);
            if (acc != null && acc.ok && acc.countExpLin == 1 && acc.countSinLin == 0 && acc.countCosLin == 0) {
                Double k = linearCoeff(acc.uExp, v);
                if (k != null && Math.abs(k) > 1e-15) {
                    int n = acc.deg;
                    double c = acc.constCoef;
                    NodoAST u = acc.uExp;

                    NodoAST suma = AstUtils.number(0.0);
                    for (int m = n; m >= 0; m--) {
                        NodoAST xm = xPow(v, m);
                        NodoAST epu = ePowClone(u);
                        NodoAST term = AstUtils.bin(LexToken.Type.MUL, xm, epu, "*", 6);

                        double fact = 1.0;
                        for (int i = m + 1; i <= n; i++) fact *= i;

                        double sign = ((n - m) % 2 == 0) ? 1.0 : -1.0;
                        double mult = (c / k) * fact / Math.pow(k, n - m);
                        term = mulC(term, sign * mult);
                        suma = sum(suma, term);
                    }
                    return suma;
                }
            }

            MulSplit ms = splitMul(f);
            if (ms.nonconst.isEmpty()) {
                NodoAST x = AstUtils.atom(LexToken.Type.VARIABLE, v, 1);
                return mulC(x, ms.c);
            }
            if (ms.nonconst.size() == 1) {
                NodoAST only = ms.nonconst.get(0);
                NodoAST I = integralRec(only, v);
                return (I != null) ? mulC(I, ms.c) : null;
            }
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
    public static NodoAST addC(NodoAST F) {
        NodoAST C = AstUtils.atom(LexToken.Type.VARIABLE, "C", 1);
        return AstUtils.bin(LexToken.Type.SUM, F, C, "+", 5);
    }

    public static NodoAST evalDefinida(NodoAST F, String var, NodoAST a, NodoAST b) {
        if (F == null || a == null || b == null) return null;
        NodoAST Fb = sustituirVar(F, var, AstUtils.cloneTree(b));
        NodoAST Fa = sustituirVar(F, var, AstUtils.cloneTree(a));
        return AstUtils.bin(LexToken.Type.SUB, Fb, Fa, "-", 5);
    }

    public static NodoAST sustituirVar(NodoAST n, String var, NodoAST con) {
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

    public static NodoAST candidatoInterior(NodoAST n) {
        if (n == null || n.token == null) return null;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.TRIG_SIN || t == LexToken.Type.TRIG_COS || t == LexToken.Type.TRIG_TAN || t == LexToken.Type.LOG || t == LexToken.Type.LN)
            return n.hijos.size() > 0 ? n.hijos.get(0) : null;
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
    public static double binom(int n, int k) {
        if (k < 0 || k > n) return 0.0;
        if (k == 0 || k == n) return 1.0;
        k = Math.min(k, n - k);
        double r = 1.0;
        for (int i = 1; i <= k; i++) {
            r = r * (n - k + i) / i;
        }
        return r;
    }

    public static double fallingFactorial(int n, int k) {
        if (k < 0) return 0.0;
        if (k == 0) return 1.0;
        double r = 1.0;
        for (int i = 0; i < k; i++) {
            r *= (n - i);
        }
        return r;
    }
    public static NodoAST integralIndef(NodoAST body, String v) {
        NodoAST dx = AstUtils.atom(LexToken.Type.VARIABLE, "d" + v, 0);
        NodoAST n = new NodoAST(new LexToken(LexToken.Type.INTEGRAL_INDEF, "∫", 0));
        if (body != null) { n.hijos.add(body); body.parent = n; }
        n.hijos.add(dx); dx.parent = n;
        return n;
    }

    public static NodoAST integralDef(NodoAST a, NodoAST b, NodoAST body, String v) {
        NodoAST dx = AstUtils.atom(LexToken.Type.VARIABLE, "d" + v, 0);
        NodoAST n = new NodoAST(new LexToken(LexToken.Type.INTEGRAL_DEF, "∫", 0));
        if (a != null) { n.hijos.add(a); a.parent = n; } else n.hijos.add(AstUtils.number(0.0));
        if (b != null) { n.hijos.add(b); b.parent = n; } else n.hijos.add(AstUtils.number(1.0));
        if (body != null) { n.hijos.add(body); body.parent = n; } else n.hijos.add(AstUtils.number(0.0));
        n.hijos.add(dx); dx.parent = n;
        return n;
    }

    public static int prioridadILATE(NodoAST f, String v) {
        if (esInversa(f, v)) return 1;
        if (f != null && f.token != null && f.token.type == LexToken.Type.LN) return 2;
        if (esAlgebraicoSimple(f, v)) return 3;
        if (esTrigSin(f) || esTrigCos(f)) return 4;
        if (esExpDeLineal(f)) return 5;
        return 100;
    }

    public static boolean esAlgebraicoSimple(NodoAST n, String v) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.VARIABLE && v.equals(n.token.value)) return true;
        if (n.token.type == LexToken.Type.EXP && n.hijos.size() == 2) {
            NodoAST base = n.hijos.get(0), ex = n.hijos.get(1);
            if (base != null && base.token != null && base.token.type == LexToken.Type.VARIABLE && v.equals(base.token.value)) {
                Double e = AstUtils.evalConst(ex);
                return e != null;
            }
        }
        return false;
    }

    public static boolean esInversa(NodoAST n, String v) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.DIV && n.hijos.size() == 2) {
            Double num = AstUtils.evalConst(n.hijos.get(0));
            if (num != null && Math.abs(num - 1.0) < 1e-15) {
                NodoAST d = n.hijos.get(1);
                return d != null && d.token != null && d.token.type == LexToken.Type.VARIABLE && v.equals(d.token.value);
            }
        }
        if (n.token.type == LexToken.Type.EXP && n.hijos.size() == 2) {
            NodoAST base = n.hijos.get(0), ex = n.hijos.get(1);
            if (base != null && base.token != null && base.token.type == LexToken.Type.VARIABLE && v.equals(base.token.value)) {
                Double e = AstUtils.evalConst(ex);
                return e != null && Math.abs(e + 1.0) < 1e-15;
            }
        }
        return false;
    }

    public static NodoAST derivadaSimple(NodoAST f, String v) {
        if (f == null || f.token == null) return null;
        LexToken.Type t = f.token.type;

        Double c = AstUtils.evalConst(f);
        if (c != null) return AstUtils.number(0.0);

        if (t == LexToken.Type.VARIABLE && v.equals(f.token.value)) return AstUtils.number(1.0);

        if (t == LexToken.Type.EXP && f.hijos.size() == 2) {
            NodoAST base = f.hijos.get(0), ex = f.hijos.get(1);
            if (base != null && base.token != null && base.token.type == LexToken.Type.VARIABLE && v.equals(base.token.value)) {
                Double e = AstUtils.evalConst(ex);
                if (e == null) return null;
                NodoAST vx = AstUtils.atom(LexToken.Type.VARIABLE, v, 1);
                if (Math.abs(e - 1.0) < 1e-15) return AstUtils.number(1.0);
                if (Math.abs(e) < 1e-15) return AstUtils.number(0.0);
                NodoAST eMinus1 = AstUtils.number(e - 1.0);
                NodoAST xPow = AstUtils.bin(LexToken.Type.EXP, vx, eMinus1, "^", 7);
                return AstUtils.bin(LexToken.Type.MUL, AstUtils.number(e), xPow, "*", 6);
            }
            if (base != null && base.token != null && base.token.type == LexToken.Type.CONST_E) {
                Double k = linearCoeff(ex, v);
                if (k == null) return null;
                NodoAST ePow = ePowClone(ex);
                return mulC(ePow, k);
            }
            return null;
        }

        if (t == LexToken.Type.LN && f.hijos.size() == 1) {
            NodoAST arg = f.hijos.get(0);
            if (arg != null && arg.token != null && arg.token.type == LexToken.Type.VARIABLE && v.equals(arg.token.value)) {
                NodoAST vx = AstUtils.atom(LexToken.Type.VARIABLE, v, 1);
                NodoAST inv = AstUtils.bin(LexToken.Type.DIV, AstUtils.number(1.0), vx, "/", 6);
                return inv;
            }
            return null;
        }

        if (t == LexToken.Type.DIV && f.hijos.size() == 2) {
            Double num = AstUtils.evalConst(f.hijos.get(0));
            NodoAST den = f.hijos.get(1);
            if (num != null && Math.abs(num - 1.0) < 1e-15 && den != null && den.token != null && den.token.type == LexToken.Type.VARIABLE && v.equals(den.token.value)) {
                NodoAST vx = AstUtils.atom(LexToken.Type.VARIABLE, v, 1);
                NodoAST x2 = AstUtils.bin(LexToken.Type.EXP, vx, AstUtils.number(2), "^", 7);
                NodoAST inv2 = AstUtils.bin(LexToken.Type.DIV, AstUtils.number(1.0), x2, "/", 6);
                return mulC(inv2, -1.0);
            }
        }

        if (t == LexToken.Type.TRIG_SIN && f.hijos.size() == 1) {
            NodoAST u = f.hijos.get(0);
            Double k = linearCoeff(u, v);
            if (k == null) return null;
            NodoAST cosu = cosClone(u);
            return mulC(cosu, k);
        }

        if (t == LexToken.Type.TRIG_COS && f.hijos.size() == 1) {
            NodoAST u = f.hijos.get(0);
            Double k = linearCoeff(u, v);
            if (k == null) return null;
            NodoAST sinu = sinClone(u);
            return mulC(sinu, -k);
        }

        if (t == LexToken.Type.MUL && f.hijos.size() == 2) {
            Double cL = AstUtils.evalConst(f.hijos.get(0));
            Double cR = AstUtils.evalConst(f.hijos.get(1));
            if (cL != null) {
                NodoAST d = derivadaSimple(f.hijos.get(1), v);
                return (d == null) ? null : mulC(d, cL);
            }
            if (cR != null) {
                NodoAST d = derivadaSimple(f.hijos.get(0), v);
                return (d == null) ? null : mulC(d, cR);
            }
            return null;
        }

        if ((t == LexToken.Type.SUM || t == LexToken.Type.SUB) && f.hijos.size() == 2) {
            NodoAST d1 = derivadaSimple(f.hijos.get(0), v);
            NodoAST d2 = derivadaSimple(f.hijos.get(1), v);
            if (d1 == null || d2 == null) return null;
            return AstUtils.bin(t, d1, d2, f.token.value, f.token.prioridad);
        }

        return null;
    }

    public static boolean equalsSymja(NodoAST a, NodoAST b) {
        String sa = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.toSymja(a);
        String sb = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.toSymja(b);
        return sa != null && sb != null && sa.equals(sb);
    }

    public static NodoAST replaceAllEqualByVar(NodoAST n, NodoAST target, String varName) {
        if (n == null) return null;
        if (equalsSymja(n, target)) return com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.atom(LexToken.Type.VARIABLE, varName, 1);
        NodoAST c = new NodoAST(n.token == null ? null : new LexToken(n.token.type, n.token.value, n.token.prioridad));
        for (NodoAST h : n.hijos) {
            NodoAST ch = replaceAllEqualByVar(h, target, varName);
            if (ch != null) {
                c.hijos.add(ch);
                ch.parent = c;
            }
        }
        return c;
    }

    public static boolean onlyVar(NodoAST n, String varName) {
        java.util.HashSet<String> s = new java.util.HashSet<>();
        com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.collectVars(n, s);
        if (s.isEmpty()) return true;
        if (s.size() == 1 && s.contains(varName)) return true;
        return false;
    }

    public static NodoAST reemplazarSubexp(NodoAST raiz, NodoAST objetivo, NodoAST con) {
        if (raiz == objetivo) return con;
        NodoAST c = new NodoAST(raiz.token == null ? null : new LexToken(raiz.token.type, raiz.token.value, raiz.token.prioridad));
        for (NodoAST h : raiz.hijos) c.addHijo(reemplazarSubexp(h, objetivo, con));
        return c;
    }
}
