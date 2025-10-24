package com.example.a22100213_proyectointegrador_logarismos.resolucion.t6lineal.rules;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;

import java.util.*;

public final class BalanceOperacionesRule implements T6Rule {

    private String varName;

    @Override
    public String name() { return "Balance de operaciones"; }

    @Override
    public boolean applies(NodoAST raiz, ResultadoSemantico rs) {
        if (!isEq(raiz)) return false;
        Set<String> vars = new LinkedHashSet<>();
        countVars(raiz, vars);
        if (vars.size() != 1) return false;
        varName = vars.iterator().next();
        NodoAST L = left(raiz), R = right(raiz);
        boolean inL = containsVar(L, varName);
        boolean inR = containsVar(R, varName);
        if (inL == inR) return false;
        NodoAST side = inL ? L : R;
        int depth = depthToVar(side, varName);
        return depth >= 1;
    }

    @Override
    public ResultadoResolucion solve(NodoAST raiz, ResultadoSemantico rs) {
        NodoAST L = left(raiz), R = right(raiz);
        boolean varLeft = containsVar(L, varName);
        NodoAST X = varLeft ? L : R;
        NodoAST K = varLeft ? R : L;

        ResultadoResolucion rr = new ResultadoResolucion();
        rr.pasos = new LinkedList<>();
        rr.pasos.add(new PasoResolucion(AstUtils.toTeX(L) + " = " + AstUtils.toTeX(R)));

        NodoAST factor = denominadoresFactor(X);
        if (!isOne(factor)) {
            X = simplifyNumeric(mul(factor, X));
            K = simplifyNumeric(mul(factor, K));
            rr.pasos.add(new PasoResolucion(AstUtils.toTeX(X) + " = " + AstUtils.toTeX(K)));
        }

        while (true) {
            X = simplifyNumeric(X);
            K = simplifyNumeric(K);
            if (isVariable(X) || isNumTimesVariable(X)) break;

            if (isOp(X, LexToken.Type.SUM)) {
                NodoAST A = left(X), B = right(X);
                boolean aVar = containsVar(A, varName), bVar = containsVar(B, varName);

                if (aVar && isNumeric(B)) {
                    K = simplifyNumeric(sub(K, B));
                    X = A;
                    rr.pasos.add(new PasoResolucion(AstUtils.toTeX(X) + " = " + AstUtils.toTeX(K)));
                    continue;
                }
                if (bVar && isNumeric(A)) {
                    K = simplifyNumeric(sub(K, A));
                    X = B;
                    rr.pasos.add(new PasoResolucion(AstUtils.toTeX(X) + " = " + AstUtils.toTeX(K)));
                    continue;
                }
                if (aVar && bVar) {
                    int da = depthToVar(A, varName), db = depthToVar(B, varName);
                    if (da <= db) {
                        K = simplifyNumeric(sub(K, B));
                        X = A;
                    } else {
                        K = simplifyNumeric(sub(K, A));
                        X = B;
                    }
                    rr.pasos.add(new PasoResolucion(AstUtils.toTeX(X) + " = " + AstUtils.toTeX(K)));
                    continue;
                }
                break;
            }

            if (isOp(X, LexToken.Type.SUB)) {
                NodoAST A = left(X), B = right(X);
                boolean aVar = containsVar(A, varName), bVar = containsVar(B, varName);

                if (aVar && isNumeric(B)) {
                    K = simplifyNumeric(add(K, B));
                    X = A;
                    rr.pasos.add(new PasoResolucion(AstUtils.toTeX(X) + " = " + AstUtils.toTeX(K)));
                    continue;
                }
                if (bVar && isNumeric(A)) {
                    X = B;
                    K = simplifyNumeric(sub(A, K));
                    rr.pasos.add(new PasoResolucion(AstUtils.toTeX(X) + " = " + AstUtils.toTeX(K)));
                    continue;
                }
                if (aVar && bVar) {
                    int da = depthToVar(A, varName), db = depthToVar(B, varName);
                    if (da <= db) {
                        K = simplifyNumeric(add(K, B));
                        X = A;
                    } else {
                        K = simplifyNumeric(sub(A, K));
                        X = B;
                    }
                    rr.pasos.add(new PasoResolucion(AstUtils.toTeX(X) + " = " + AstUtils.toTeX(K)));
                    continue;
                }
                break;
            }

            if (isOp(X, LexToken.Type.MUL)) {
                NodoAST A = left(X), B = right(X);
                boolean aVar = containsVar(A, varName), bVar = containsVar(B, varName);
                if (aVar && isNumeric(B)) {
                    if (!isZero(B)) K = simplifyNumeric(div(K, B));
                    X = A;
                    rr.pasos.add(new PasoResolucion(AstUtils.toTeX(X) + " = " + AstUtils.toTeX(K)));
                    continue;
                }
                if (bVar && isNumeric(A)) {
                    if (!isZero(A)) K = simplifyNumeric(div(K, A));
                    X = B;
                    rr.pasos.add(new PasoResolucion(AstUtils.toTeX(X) + " = " + AstUtils.toTeX(K)));
                    continue;
                }
                break;
            }

            if (isOp(X, LexToken.Type.DIV)) {
                NodoAST A = left(X), B = right(X);
                boolean aVar = containsVar(A, varName), bVar = containsVar(B, varName);
                if (aVar && !isZero(B) && isNumeric(B)) {
                    K = simplifyNumeric(mul(K, B));
                    X = A;
                    rr.pasos.add(new PasoResolucion(AstUtils.toTeX(X) + " = " + AstUtils.toTeX(K)));
                    continue;
                }
                if (bVar) {
                    if (!isZero(K)) {
                        X = B;
                        K = simplifyNumeric(div(A, K));
                        rr.pasos.add(new PasoResolucion(AstUtils.toTeX(X) + " = " + AstUtils.toTeX(K)));
                        continue;
                    }
                }
                break;
            }

            break;
        }

        X = simplifyNumeric(X);
        K = simplifyNumeric(K);

        if (isNumTimesVariable(X)) {
            NodoAST coef = numFactor(X);
            if (!isOne(coef) && !isZero(coef)) {
                K = simplifyNumeric(div(K, coef));
                X = varOf(X);
                rr.pasos.add(new PasoResolucion(AstUtils.toTeX(X) + " = " + AstUtils.toTeX(K)));
            } else {
                X = varOf(X);
            }
        }

        if (containsVar(K, varName)) {
            Linear LX = lin(X);
            Linear LK = lin(K);
            if (LX.ok && LK.ok) {
                double a = LX.a - LK.a;
                double b = LK.b - LX.b;

                NodoAST lhs = mul(num(a), varNode());
                NodoAST rhs = num(b);
                rr.pasos.add(new PasoResolucion(AstUtils.toTeX(lhs) + " = " + AstUtils.toTeX(rhs)));

                if (Math.abs(a) < 1e-12) {
                    if (Math.abs(b) < 1e-12) {
                        rr.pasos.add(new PasoResolucion("\\text{Identidad: infinitas soluciones}"));
                        rr.latexFinal = "\\text{Infinitas soluciones}";
                    } else {
                        rr.pasos.add(new PasoResolucion("\\text{Inconsistente: sin solución}"));
                        rr.latexFinal = "\\text{Sin solución}";
                    }
                    rr.resultado = node(new LexToken(LexToken.Type.EQUAL, "=", prio(LexToken.Type.EQUAL)), lhs, rhs);
                    return rr;
                }

                NodoAST sol = div(rhs, num(a));
                rr.pasos.add(new PasoResolucion("x = " + AstUtils.toTeX(sol)));
                rr.latexFinal = "x = " + AstUtils.toTeX(sol);
                rr.resultado = node(new LexToken(LexToken.Type.EQUAL, "=", prio(LexToken.Type.EQUAL)), varNode(), sol);
                return rr;
            }
        }

        if (!isVariable(X)) {
            rr.pasos.add(new PasoResolucion("\\text{No pudo aislarse completamente}"));
            rr.latexFinal = AstUtils.toTeX(X) + " = " + AstUtils.toTeX(K);
            return rr;
        }

        rr.latexFinal = AstUtils.toTeX(X) + " = " + AstUtils.toTeX(K);
        rr.resultado = node(new LexToken(LexToken.Type.EQUAL, "=", prio(LexToken.Type.EQUAL)), X, K);
        return rr;
    }

    private static class Linear { boolean ok; double a, b;
        Linear(boolean ok, double a, double b){ this.ok=ok; this.a=a; this.b=b; } }

    private Linear lin(NodoAST n) {
        if (n == null || n.token == null) return new Linear(false,0,0);
        switch (n.token.type) {
            case INTEGER:
            case DECIMAL:
                return new Linear(true, 0.0, Double.parseDouble(n.token.value));
            case VARIABLE:
                return new Linear(true, 1.0, 0.0);
            case SUM: {
                Linear L = lin(left(n)), R = lin(right(n));
                if (!L.ok || !R.ok) return new Linear(false,0,0);
                return new Linear(true, L.a + R.a, L.b + R.b);
            }
            case SUB: {
                Linear L = lin(left(n)), R = lin(right(n));
                if (!L.ok || !R.ok) return new Linear(false,0,0);
                return new Linear(true, L.a - R.a, L.b - R.b);
            }
            case MUL: {
                Linear L = lin(left(n)), R = lin(right(n));
                if (!L.ok || !R.ok) return new Linear(false,0,0);

                if (Math.abs(L.a) > 1e-12 && Math.abs(R.a) > 1e-12) return new Linear(false,0,0);
                double a = L.a*R.b + R.a*L.b;
                double b = L.b*R.b;
                return new Linear(true, a, b);
            }
            case DIV: {
                Linear L = lin(left(n)), R = lin(right(n));
                if (!L.ok || !R.ok) return new Linear(false,0,0);
                if (Math.abs(R.a) > 1e-12) return new Linear(false,0,0);
                double den = R.b;
                return new Linear(true, L.a/den, L.b/den);
            }
            default:
                return new Linear(false,0,0);
        }
    }

    private NodoAST varNode() {
        return new NodoAST(new LexToken(LexToken.Type.VARIABLE, varName, prio(LexToken.Type.VARIABLE)));
    }

    private NodoAST denominadoresFactor(NodoAST x) {
        List<Double> ds = new ArrayList<>();
        collectNumericDenoms(x, ds);
        if (ds.isEmpty()) return num(1);
        boolean allInt = true;
        long l = 1L;
        double prod = 1.0;
        for (double d : ds) {
            double ad = Math.abs(d);
            long r = Math.round(ad);
            if (Math.abs(ad - r) < 1e-9 && r != 0) {
                l = lcm(l, Math.abs(r));
            } else {
                allInt = false;
                prod *= ad;
            }
        }
        if (allInt) return num(l);
        return num(prod);
    }

    private void collectNumericDenoms(NodoAST n, List<Double> out) {
        if (n == null || n.token == null) return;
        if (n.token.type == LexToken.Type.DIV && n.hijos.size() == 2) {
            NodoAST A = left(n), B = right(n);
            if (containsVar(A, varName) && isNumeric(B) && !isZero(B)) out.add(Math.abs(evalNumeric(B)));
        }
        if (n.hijos != null) for (NodoAST h : n.hijos) collectNumericDenoms(h, out);
    }

    private long gcd(long a, long b) { a = Math.abs(a); b = Math.abs(b); while (b != 0){ long t = a % b; a = b; b = t; } return a; }
    private long lcm(long a, long b) { if (a == 0 || b == 0) return 0; return Math.abs(a / gcd(a, b) * b); }

    private boolean isEq(NodoAST n) { return isOp(n, LexToken.Type.EQUAL); }
    private boolean isOp(NodoAST n, LexToken.Type t) { return n != null && n.token != null && n.token.type == t; }
    private NodoAST left(NodoAST n) { return (n != null && n.hijos != null && n.hijos.size() > 0) ? n.hijos.get(0) : null; }
    private NodoAST right(NodoAST n) { return (n != null && n.hijos != null && n.hijos.size() > 1) ? n.hijos.get(1) : null; }

    private void countVars(NodoAST n, Set<String> vars) {
        if (n == null || n.token == null) return;
        if (n.token.type == LexToken.Type.VARIABLE) vars.add(n.token.value);
        if (n.hijos != null) for (NodoAST h : n.hijos) countVars(h, vars);
    }

    private boolean containsVar(NodoAST n, String v) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.VARIABLE && v.equals(n.token.value)) return true;
        if (n.hijos != null) for (NodoAST h : n.hijos) if (containsVar(h, v)) return true;
        return false;
    }

    private int depthToVar(NodoAST n, String v) {
        if (n == null || n.token == null) return -1;
        if (n.token.type == LexToken.Type.VARIABLE && v.equals(n.token.value)) return 0;
        int best = Integer.MAX_VALUE;
        if (n.hijos != null) for (NodoAST h : n.hijos) {
            int d = depthToVar(h, v);
            if (d >= 0) best = Math.min(best, d + 1);
        }
        return best == Integer.MAX_VALUE ? -1 : best;
    }

    private boolean isVariable(NodoAST n) {
        return n != null && n.token != null && n.token.type == LexToken.Type.VARIABLE;
    }

    private boolean isNumTimesVariable(NodoAST n) {
        if (!isOp(n, LexToken.Type.MUL)) return false;
        NodoAST a = left(n), b = right(n);
        return (isNumeric(a) && isVariable(b)) || (isNumeric(b) && isVariable(a));
    }

    private NodoAST numFactor(NodoAST mul) {
        NodoAST a = left(mul), b = right(mul);
        return isNumeric(a) ? a : b;
    }

    private NodoAST varOf(NodoAST mulOrVar) {
        if (isVariable(mulOrVar)) return mulOrVar;
        NodoAST a = left(mulOrVar), b = right(mulOrVar);
        return isVariable(a) ? a : b;
    }

    private boolean isNumeric(NodoAST n) {
        if (n == null || n.token == null) return false;
        switch (n.token.type) {
            case INTEGER:
            case DECIMAL: return true;
            case SUM:
            case SUB:
            case MUL:
            case DIV:
            case EXP: return isNumeric(left(n)) && isNumeric(right(n));
            default: return false;
        }
    }

    private boolean isZero(NodoAST n) {
        if (!isNumeric(n)) return false;
        try { return Math.abs(evalNumeric(n)) < 1e-12; } catch (Exception e) { return false; }
    }

    private boolean isOne(NodoAST n) {
        if (!isNumeric(n)) return false;
        try { return Math.abs(evalNumeric(n) - 1.0) < 1e-12; } catch (Exception e) { return false; }
    }

    private double evalNumeric(NodoAST n) {
        switch (n.token.type) {
            case INTEGER:
            case DECIMAL: return Double.parseDouble(n.token.value);
            case SUM: return evalNumeric(left(n)) + evalNumeric(right(n));
            case SUB: return evalNumeric(left(n)) - evalNumeric(right(n));
            case MUL: return evalNumeric(left(n)) * evalNumeric(right(n));
            case DIV: return evalNumeric(left(n)) / evalNumeric(right(n));
            case EXP: return Math.pow(evalNumeric(left(n)), evalNumeric(right(n)));
            default: throw new IllegalStateException("No numérico");
        }
    }

    private NodoAST simplifyNumeric(NodoAST n) {
        if (n == null || n.token == null) return n;
        switch (n.token.type) {
            case INTEGER:
            case DECIMAL:
            case VARIABLE: return n;
            case SUM:
            case SUB:
            case MUL:
            case DIV:
            case EXP:
                NodoAST a = simplifyNumeric(left(n));
                NodoAST b = simplifyNumeric(right(n));
                if (isNumeric(a) && isNumeric(b)) return num(evalNumeric(node(new LexToken(n.token.type, n.token.value, n.token.prioridad), a, b)));
                return node(n.token, a, b);
            default: return n;
        }
    }

    private NodoAST add(NodoAST a, NodoAST b) { return node(new LexToken(LexToken.Type.SUM, "+", prio(LexToken.Type.SUM)), a, b); }
    private NodoAST sub(NodoAST a, NodoAST b) { return node(new LexToken(LexToken.Type.SUB, "-", prio(LexToken.Type.SUB)), a, b); }
    private NodoAST mul(NodoAST a, NodoAST b) { return node(new LexToken(LexToken.Type.MUL, "*", prio(LexToken.Type.MUL)), a, b); }
    private NodoAST div(NodoAST a, NodoAST b) { return node(new LexToken(LexToken.Type.DIV, "/", prio(LexToken.Type.DIV)), a, b); }

    private NodoAST node(LexToken tok, NodoAST a, NodoAST b) {
        List<NodoAST> hs = new ArrayList<>(2);
        hs.add(a); hs.add(b);
        NodoAST n = new NodoAST(tok);
        n.hijos = hs;
        return n;
    }

    private NodoAST num(double v) {
        if (Math.abs(v - Math.rint(v)) < 1e-12) {
            long iv = (long)Math.rint(v);
            return new NodoAST(new LexToken(LexToken.Type.INTEGER, String.valueOf(iv), prio(LexToken.Type.INTEGER)));
        }
        return new NodoAST(new LexToken(LexToken.Type.DECIMAL, Double.toString(v), prio(LexToken.Type.DECIMAL)));
    }

    private int prio(LexToken.Type t) {
        switch (t) {
            case EXP: return 7;
            case MUL:
            case DIV: return 6;
            case SUM:
            case SUB: return 5;
            case EQUAL: return 2;
            case INTEGER:
            case DECIMAL:
            case VARIABLE:
            default: return 0;
        }
    }
}
