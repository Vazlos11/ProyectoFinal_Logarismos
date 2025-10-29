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
        NodoAST cur = cloneNode(root);
        for (int i = 0; i < 6; i++) {
            String before = AstUtils.toTeX(cur);
            cur = simplify(cur);
            String after = AstUtils.toTeX(cur);
            if (after.equals(before)) break;
        }
        return cur;
    }

    public static long maybeIntegerReciprocal(double c){
        if (Math.abs(c) < EPS) return -1;
        double inv = 1.0/Math.abs(c);
        long r = Math.round(inv);
        return Math.abs(inv - r) < 1e-12 ? r : -1;
    }

    public static NodoAST applyIfSimpleUnitFraction(NodoAST term, double c){
        long den = maybeIntegerReciprocal(c);
        if (den > 1){
            if (c > 0) {
                return AstUtils.bin(LexToken.Type.DIV, term, AstUtils.number(den), "/", 6);
            } else {
                NodoAST div = AstUtils.bin(LexToken.Type.DIV, term, AstUtils.number(den), "/", 6);
                return AstUtils.bin(LexToken.Type.MUL, AstUtils.number(-1), div, "*", 6);
            }
        }
        return AstUtils.bin(LexToken.Type.MUL, AstUtils.number(c), term, "*", 6);
    }

    private static NodoAST simplify(NodoAST n){
        if (n == null || n.token == null) return n;
        LexToken.Type t = n.token.type;

        if (n.hijos == null || n.hijos.isEmpty()) {
            if (t == LexToken.Type.DECIMAL) {
                Double v = parseNum(n);
                if (v == null) return n;
                return toNumberOrFraction(v);
            }
            return n;
        }

        if (n.hijos.size() == 1) {
            NodoAST a = simplify(n.hijos.get(0));
            n.hijos.set(0, a);
            return n;
        }

        NodoAST a = simplify(n.hijos.get(0));
        NodoAST b = simplify(n.hijos.get(1));
        LexToken.Type op = t;
        boolean simb = hasSymbolic(a) || hasSymbolic(b);

        if (op == LexToken.Type.SUM) {
            if (isZero(a)) return b;
            if (isZero(b)) return a;
            if (!simb && isNum(a) && isNum(b)) return toNumberOrFraction(val(a)+val(b));
            return AstUtils.bin(LexToken.Type.SUM, a, b, "+", 5);
        }

        if (op == LexToken.Type.SUB) {
            if (isZero(b)) return a;
            if (isZero(a)) return AstUtils.bin(LexToken.Type.MUL, AstUtils.number(-1), b, "*", 6);
            if (!simb && isNum(a) && isNum(b)) return toNumberOrFraction(val(a)-val(b));
            return AstUtils.bin(LexToken.Type.SUB, a, b, "-", 5);
        }

        if (op == LexToken.Type.MUL) {
            if (isZero(a) || isZero(b)) return AstUtils.number(0);
            if (isOne(a)) return b;
            if (isOne(b)) return a;
            if (isMinusOne(a)) return negate(b);
            if (isMinusOne(b)) return negate(a);
            if (!simb) {
                if (isNum(a) && !isNum(b)) {
                    double c = val(a);
                    long den = maybeIntegerReciprocal(c);
                    if (den > 1) return applyIfSimpleUnitFraction(b, c);
                }
                if (!isNum(a) && isNum(b)) {
                    double c = val(b);
                    long den = maybeIntegerReciprocal(c);
                    if (den > 1) return applyIfSimpleUnitFraction(a, c);
                }
                if (isNum(a) && isNum(b)) return toNumberOrFraction(val(a)*val(b));
            }
            return AstUtils.bin(LexToken.Type.MUL, a, b, "*", 6);
        }

        if (op == LexToken.Type.DIV) {
            if (isZero(a) && !isZero(b)) return AstUtils.number(0);
            if (isOne(b)) return a;
            if (isMinusOne(b)) return negate(a);
            if (isMinusOne(a)) {
                NodoAST inv = AstUtils.bin(LexToken.Type.DIV, AstUtils.number(1), b, "/", 6);
                return negate(inv);
            }
            if (!simb && isNum(a) && isNum(b)) {
                double dv = val(b);
                if (Math.abs(dv) < EPS) return AstUtils.bin(LexToken.Type.DIV, a, b, "/", 6);
                return toNumberOrFraction(val(a)/dv);
            }
            return AstUtils.bin(LexToken.Type.DIV, a, b, "/", 6);
        }

        if (op == LexToken.Type.EXP) {
            if (isNum(b)) {
                double k = val(b);
                long ki = Math.round(k);
                if (Math.abs(k - ki) < EPS) {
                    if (ki == 0) return AstUtils.number(1);
                    if (ki == 1) return a;
                    if (!hasSymbolic(a) && isNum(a)) return toNumberOrFraction(Math.pow(val(a), ki));
                }
            }
            return AstUtils.bin(LexToken.Type.EXP, a, b, "^", 7);
        }

        return AstUtils.bin(op, a, b, n.token.value, n.token.prioridad);
    }

    private static boolean hasSymbolic(NodoAST n){
        if (n == null || n.token == null) return false;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB || t == LexToken.Type.MUL || t == LexToken.Type.DIV || t == LexToken.Type.EXP) {
            return hasSymbolic(n.hijos.get(0)) || hasSymbolic(n.hijos.get(1));
        }
        return !(t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL);
    }

    private static boolean isZero(NodoAST n){
        if (!isNum(n)) return false;
        return Math.abs(val(n)) < EPS;
    }

    private static boolean isOne(NodoAST n){
        if (!isNum(n)) return false;
        return Math.abs(val(n) - 1.0) < EPS;
    }

    private static boolean isMinusOne(NodoAST n){
        if (!isNum(n)) return false;
        return Math.abs(val(n) + 1.0) < EPS;
    }

    private static boolean isNum(NodoAST n){
        if (n == null || n.token == null) return false;
        LexToken.Type t = n.token.type;
        return t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL;
    }

    private static Double parseNum(NodoAST n){
        try { return Double.parseDouble(n.token.value); } catch(Exception e){ return null; }
    }

    private static double val(NodoAST n){
        Double v = parseNum(n);
        return v == null ? Double.NaN : v;
    }

    private static NodoAST toNumberOrFraction(double v){
        double r = Math.rint(v);
        if (Math.abs(v - r) < EPS) return AstUtils.number((long)Math.round(r));
        long[] pq = toFraction(v, MAX_DEN, RAT_TOL);
        if (pq != null) {
            long num = pq[0], den = pq[1];
            if (den == 1) return AstUtils.number(num);
            return AstUtils.bin(LexToken.Type.DIV, AstUtils.number(num), AstUtils.number(den), "/", 6);
        }
        return AstUtils.number(v);
    }

    private static long[] toFraction(double x, int maxDen, double tol){
        if (Double.isNaN(x) || Double.isInfinite(x)) return null;
        int sgn = x < 0 ? -1 : 1;
        double v = Math.abs(x);
        long h1 = 1, h0 = 0, k1 = 0, k0 = 1;
        double b = v;
        long h = 1, k = 1;
        for (;;) {
            long a = (long)Math.floor(b);
            h = a*h1 + h0;
            k = a*k1 + k0;
            if (k > maxDen) {
                long prevH = h1, prevK = k1;
                if (prevK == 0) return null;
                double c1 = (double)prevH/prevK;
                double c2 = (double)h/k;
                double e1 = Math.abs(c1 - v);
                double e2 = Math.abs(c2 - v);
                long nn = e1 <= e2 ? prevH : h;
                long dd = e1 <= e2 ? prevK : k;
                long num = sgn*nn;
                return new long[]{num, dd};
            }
            double approx = (double)h/k;
            if (Math.abs(approx - v) <= tol) {
                long num = sgn*h;
                return new long[]{num, k};
            }
            h0 = h1; h1 = h;
            k0 = k1; k1 = k;
            double frac = b - a;
            if (frac < tol) {
                long num = sgn*h;
                return new long[]{num, k};
            }
            b = 1.0/frac;
        }
    }

    private static NodoAST negate(NodoAST n){
        if (isNum(n)) return toNumberOrFraction(-val(n));
        return AstUtils.bin(LexToken.Type.MUL, AstUtils.number(-1), n, "*", 6);
    }

    private static NodoAST cloneNode(NodoAST n){
        if (n == null) return null;
        NodoAST c = new NodoAST(new LexToken(n.token.type, n.token.value, n.token.prioridad));
        for (NodoAST h : n.hijos) c.hijos.add(cloneNode(h));
        return c;
    }
}
