package com.example.a22100213_proyectointegrador_logarismos.resolucion.format;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;

public final class ExpressionFormatter {

    private static final double EPS = 1e-12;
    private static final int MAX_DEN = 1000;
    private static final double RAT_TOL = 1e-10;

    public static NodoAST format(NodoAST root) {
        if (root == null) return null;
        NodoAST a = cloneNode(root);
        for (int i = 0; i < 25; i++) {
            NodoAST b = pass(a);
            if (equalsByTeX(a, b)) return b;
            a = b;
        }
        return a;
    }

    private static NodoAST pass(NodoAST n) {
        if (n == null || n.token == null) return n;
        LexToken.Type t = n.token.type;
        if (n.hijos == null || n.hijos.isEmpty()) {
            if (t == LexToken.Type.DECIMAL) return toNumberOrFraction(parseNum(n));
            return n;
        }
        int k = n.hijos.size();
        for (int i = 0; i < k; i++) n.hijos.set(i, pass(n.hijos.get(i)));
        if (t == LexToken.Type.SUM) return foldSum(n.hijos.get(0), n.hijos.get(1));
        if (t == LexToken.Type.SUB) return foldSub(n.hijos.get(0), n.hijos.get(1));
        if (t == LexToken.Type.MUL) return foldMul(n.hijos.get(0), n.hijos.get(1));
        if (t == LexToken.Type.DIV) return foldDiv(n.hijos.get(0), n.hijos.get(1));
        if (t == LexToken.Type.EXP) return foldPow(n.hijos.get(0), n.hijos.get(1));
        return n;
    }

    private static NodoAST foldSum(NodoAST a, NodoAST b) {
        if (isZero(a)) return b;
        if (isZero(b)) return a;
        if (isNum(a) && isNum(b)) return toNumberOrFraction(val(a) + val(b));
        if (isNegZero(a) && !isNum(b)) return b;
        if (isNegZero(b) && !isNum(a)) return a;
        return AstUtils.bin(LexToken.Type.SUM, a, b, "+", 5);
    }

    private static NodoAST foldSub(NodoAST a, NodoAST b) {
        if (isZero(b)) return a;
        if (isZero(a)) return negate(b);
        if (isNum(a) && isNum(b)) return toNumberOrFraction(val(a) - val(b));
        return AstUtils.bin(LexToken.Type.SUB, a, b, "-", 5);
    }

    private static NodoAST foldMul(NodoAST a, NodoAST b) {
        if (isZero(a) || isZero(b)) return AstUtils.number(0);
        if (isOne(a)) return b;
        if (isOne(b)) return a;
        if (isNum(a) && isNum(b)) return toNumberOrFraction(val(a) * val(b));
        if (isNum(a) && isFrac(b)) return mulNumFrac(val(a), b);
        if (isFrac(a) && isNum(b)) return mulNumFrac(val(b), a);
        if (isNum(a) && isSymNegOne(b)) return negate(AstUtils.number(val(a)));
        if (isNum(b) && isSymNegOne(a)) return negate(AstUtils.number(val(b)));
        if (isNum(a) && val(a) == -1) return negate(b);
        if (isNum(b) && val(b) == -1) return negate(a);
        return AstUtils.bin(LexToken.Type.MUL, a, b, "*", 6);
    }

    private static NodoAST foldDiv(NodoAST a, NodoAST b) {
        if (isZero(a)) return AstUtils.number(0);
        if (isOne(b)) return a;
        if (isNum(a) && isNum(b)) return toNumberOrFraction(val(a) / val(b));
        if (isNum(b) && val(b) == -1) return negate(a);
        if (isFrac(a) && isNum(b)) {
            int[] pq = asFrac(a);
            double c = val(b);
            if (Math.abs(c - Math.rint(c)) < EPS) return frac(pq[0], pq[1] * (int)Math.rint(c));
        }
        return AstUtils.bin(LexToken.Type.DIV, a, b, "/", 6);
    }

    private static NodoAST foldPow(NodoAST a, NodoAST b) {
        if (isOne(b)) return a;
        if (isZero(b)) return AstUtils.number(1);
        if (isNum(a) && isNum(b)) return toNumberOrFraction(Math.pow(val(a), val(b)));
        return AstUtils.bin(LexToken.Type.EXP, a, b, "^", 7);
    }

    private static boolean equalsByTeX(NodoAST a, NodoAST b) {
        String ta = AstUtils.toTeX(a);
        String tb = AstUtils.toTeX(b);
        return ta.equals(tb);
    }

    private static boolean isNum(NodoAST n) {
        return n != null && n.token != null && (n.token.type == LexToken.Type.DECIMAL || n.token.type == LexToken.Type.INTEGER);
    }

    private static double val(NodoAST n) {
        Double v = parseNum(n);
        return v == null ? Double.NaN : v;
    }

    private static Double parseNum(NodoAST n) {
        if (n == null || n.token == null) return null;
        try { return Double.parseDouble(n.token.value); } catch (Exception e) { return null; }
    }

    private static boolean isZero(NodoAST n) {
        return isNum(n) && Math.abs(val(n)) < EPS;
    }

    private static boolean isNegZero(NodoAST n) {
        return isNum(n) && Math.abs(val(n)) < EPS && Double.doubleToRawLongBits(val(n)) == Double.doubleToRawLongBits(-0.0);
    }

    private static boolean isOne(NodoAST n) {
        return isNum(n) && Math.abs(val(n) - 1) < EPS;
    }

    private static boolean isFrac(NodoAST n) {
        return n != null && n.token != null && n.token.type == LexToken.Type.DIV && n.hijos.size() == 2 && isInt(n.hijos.get(0)) && isInt(n.hijos.get(1));
    }

    private static boolean isInt(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.INTEGER) return true;
        if (n.token.type == LexToken.Type.DECIMAL) {
            double v = val(n);
            return Math.abs(v - Math.rint(v)) < EPS;
        }
        return false;
    }

    private static boolean isSymNegOne(NodoAST n) {
        return isNum(n) && Math.abs(val(n) + 1) < EPS;
    }

    private static int[] asFrac(NodoAST n) {
        if (isFrac(n)) {
            int p = (int)Math.rint(val(n.hijos.get(0)));
            int q = (int)Math.rint(val(n.hijos.get(1)));
            if (q < 0) { q = -q; p = -p; }
            int g = gcd(Math.abs(p), Math.abs(q));
            return new int[]{p / g, q / g};
        }
        double v = val(n);
        int[] pq = rat(v);
        return pq;
    }

    private static NodoAST mulNumFrac(double c, NodoAST frac) {
        int[] pq = asFrac(frac);
        double num = c * pq[0];
        int[] rs = rat(num);
        int p = rs[0];
        int q = rs[1] * pq[1];
        int g = gcd(Math.abs(p), Math.abs(q));
        p /= g; q /= g;
        if (q < 0) { q = -q; p = -p; }
        return frac(p, q);
    }

    private static NodoAST toNumberOrFraction(double v) {
        if (Math.abs(v - Math.rint(v)) < EPS) return AstUtils.number(Math.rint(v));
        int[] pq = rat(v);
        return frac(pq[0], pq[1]);
    }

    private static NodoAST frac(int p, int q) {
        if (q == 0) return AstUtils.number(Double.NaN);
        if (q < 0) { q = -q; p = -p; }
        int g = gcd(Math.abs(p), Math.abs(q));
        p /= g; q /= g;
        return AstUtils.bin(LexToken.Type.DIV, AstUtils.number(p), AstUtils.number(q), "/", 6);
    }

    private static int[] rat(double x) {
        int sign = x < 0 ? -1 : 1;
        x = Math.abs(x);
        if (Math.abs(x - Math.rint(x)) < RAT_TOL) return new int[]{sign * (int)Math.rint(x), 1};
        int h1 = 1, h0 = 0, k1 = 0, k0 = 1;
        double b = x, a = Math.floor(b);
        int iter = 0;
        while (iter++ < 64) {
            int h2 = (int)(a) * h1 + h0;
            int k2 = (int)(a) * k1 + k0;
            if (k2 > MAX_DEN) break;
            double r = (double) h2 / (double) k2;
            if (Math.abs(r - x) < RAT_TOL) {
                int p = sign * h2, q = k2;
                int g = gcd(Math.abs(p), Math.abs(q));
                return new int[]{p / g, q / g};
            }
            h0 = h1; k0 = k1; h1 = h2; k1 = k2;
            double frac = b - Math.floor(b);
            if (frac < RAT_TOL) break;
            b = 1.0 / frac;
            a = Math.floor(b);
        }
        int p = sign * h1, q = k1;
        int g = gcd(Math.abs(p), Math.abs(q));
        return new int[]{p / g, q / g};
    }

    private static int gcd(int a, int b) {
        while (b != 0) { int t = a % b; a = b; b = t; }
        return a == 0 ? 1 : a;
    }

    private static NodoAST negate(NodoAST n) {
        if (isNum(n)) return toNumberOrFraction(-val(n));
        return AstUtils.bin(LexToken.Type.MUL, AstUtils.number(-1), n, "*", 6);
    }

    private static NodoAST cloneNode(NodoAST n) {
        if (n == null) return null;
        NodoAST c = new NodoAST(new LexToken(n.token.type, n.token.value, n.token.prioridad));
        for (NodoAST h : n.hijos) c.hijos.add(cloneNode(h));
        return c;
    }
}
