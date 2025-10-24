package com.example.a22100213_proyectointegrador_logarismos.resolucion.t6lineal.rules;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;

import java.util.*;

public final class SustitucionSimbolicaRule implements T6Rule {

    private String varName;
    private NodoAST varSample;

    @Override
    public String name() { return "Sustitución simbólica (agrupación de términos)"; }

    @Override
    public boolean applies(NodoAST raiz, ResultadoSemantico rs) {
        if (!isEq(raiz)) return false;
        Set<String> vars = new LinkedHashSet<>();
        countVars(raiz, vars);
        if (vars.size() != 1) return false;
        varName = vars.iterator().next();
        int ocurr = countVarOccurrences(raiz, varName);
        if (ocurr < 2) return false;
        varSample = firstVarNode(raiz, varName);
        return isLinearInVar(raiz, varName);
    }

    @Override
    public ResultadoResolucion solve(NodoAST raiz, ResultadoSemantico rs) {
        NodoAST L = left(raiz), R = right(raiz);
        NodoAST L1 = expand(L), R1 = expand(R);
        ResultadoResolucion rr = new ResultadoResolucion();
        rr.pasos = new LinkedList<>();
        rr.pasos.add(new PasoResolucion(AstUtils.toTeX(L) + " = " + AstUtils.toTeX(R)));
        if (!sameTree(L, L1) || !sameTree(R, R1)) rr.pasos.add(new PasoResolucion(AstUtils.toTeX(L1) + " = " + AstUtils.toTeX(R1)));

        double M = denominatorsProduct(L1) * denominatorsProduct(R1);
        if (M != 0 && Math.abs(M - 1.0) > 1e-12) {
            NodoAST ML = mul(num(M), L1);
            NodoAST MR = mul(num(M), R1);
            rr.pasos.add(new PasoResolucion(AstUtils.toTeX(ML) + " = " + AstUtils.toTeX(MR)));
            L1 = simplifyNumeric(ML);
            R1 = simplifyNumeric(MR);
        }

        NodoAST L2 = simplifyNumeric(L1);
        NodoAST R2 = simplifyNumeric(R1);
        if (!sameTree(L1, L2) || !sameTree(R1, R2)) rr.pasos.add(new PasoResolucion(AstUtils.toTeX(L2) + " = " + AstUtils.toTeX(R2)));

        Linear lL = linearize(L2, varName);
        Linear lR = linearize(R2, varName);
        double a = lL.a - lR.a;
        double c = lR.b - lL.b;

        String texVar = AstUtils.toTeX(varSample != null ? varSample : makeVar(varName));
        String texPaso = coefTex(a) + texVar + " = " + coefTex(c);
        rr.pasos.add(new PasoResolucion(texPaso));

        if (Math.abs(a) < 1e-12) {
            if (Math.abs(c) < 1e-12) {
                rr.pasos.add(new PasoResolucion("\\text{Infinitas soluciones}"));
                rr.latexFinal = "\\text{Infinitas soluciones}";
            } else {
                rr.pasos.add(new PasoResolucion("\\text{Sin solución}"));
                rr.latexFinal = "\\text{Sin solución}";
            }
            return rr;
        }

        String rhs = "\\frac{" + coefTex(c) + "}{" + coefTex(a) + "}";
        rr.pasos.add(new PasoResolucion(texVar + " = " + rhs));
        rr.latexFinal = texVar + " = " + rhs;
        return rr;
    }

    private boolean isEq(NodoAST n) { return isOp(n, LexToken.Type.EQUAL); }

    private boolean isOp(NodoAST n, LexToken.Type t) { return n != null && n.token != null && n.token.type == t; }

    private NodoAST left(NodoAST n) { return (n != null && n.hijos != null && n.hijos.size() > 0) ? n.hijos.get(0) : null; }

    private NodoAST right(NodoAST n) { return (n != null && n.hijos != null && n.hijos.size() > 1) ? n.hijos.get(1) : null; }

    private void countVars(NodoAST n, Set<String> vars) {
        if (n == null || n.token == null) return;
        if (n.token.type == LexToken.Type.VARIABLE) vars.add(n.token.value);
        if (n.hijos != null) for (NodoAST h : n.hijos) countVars(h, vars);
    }

    private int countVarOccurrences(NodoAST n, String v) {
        if (n == null || n.token == null) return 0;
        int c = (n.token.type == LexToken.Type.VARIABLE && v.equals(n.token.value)) ? 1 : 0;
        if (n.hijos != null) for (NodoAST h : n.hijos) c += countVarOccurrences(h, v);
        return c;
    }

    private NodoAST firstVarNode(NodoAST n, String v) {
        if (n == null || n.token == null) return null;
        if (n.token.type == LexToken.Type.VARIABLE && v.equals(n.token.value)) return n;
        if (n.hijos != null) for (NodoAST h : n.hijos) {
            NodoAST k = firstVarNode(h, v);
            if (k != null) return k;
        }
        return null;
    }

    private boolean isLinearInVar(NodoAST n, String v) {
        if (n == null) return true;
        if (n.token == null) return false;
        switch (n.token.type) {
            case VARIABLE: return v.equals(n.token.value);
            case INTEGER:
            case DECIMAL: return true;
            case SUM:
            case SUB: return isLinearInVar(left(n), v) && isLinearInVar(right(n), v);
            case MUL: return (isNumeric(left(n)) && isLinearInVar(right(n), v)) || (isNumeric(right(n)) && isLinearInVar(left(n), v));
            case DIV: return isLinearInVar(left(n), v) && isNumeric(right(n));
            case EXP:
                if (containsVar(left(n), v)) return false;
                return isNumeric(left(n)) && isNumeric(right(n));
            default: return false;
        }
    }

    private boolean containsVar(NodoAST n, String v) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.VARIABLE && v.equals(n.token.value)) return true;
        if (n.hijos != null) for (NodoAST h : n.hijos) if (containsVar(h, v)) return true;
        return false;
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

    private double evalNumeric(NodoAST n) {
        if (n == null || n.token == null) throw new IllegalStateException("No numérico");
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
                if (isNumeric(a) && isNumeric(b)) return num(evalNumeric(makeOp(a, b, n.token.type)));
                return node(n.token, a, b);
            default: return n;
        }
    }

    private NodoAST makeOp(NodoAST a, NodoAST b, LexToken.Type op) {
        return node(new LexToken(op, op.name(), prio(op)), a, b);
    }

    private NodoAST expand(NodoAST n) {
        if (n == null || n.token == null) return n;
        switch (n.token.type) {
            case MUL: {
                NodoAST A = expand(left(n));
                NodoAST B = expand(right(n));
                if (isOp(A, LexToken.Type.SUM) || isOp(A, LexToken.Type.SUB)) {
                    NodoAST a1 = left(A), a2 = right(A);
                    return node(A.token, expand(mul(a1, B)), expand(mul(a2, B)));
                }
                if (isOp(B, LexToken.Type.SUM) || isOp(B, LexToken.Type.SUB)) {
                    NodoAST b1 = left(B), b2 = right(B);
                    return node(B.token, expand(mul(A, b1)), expand(mul(A, b2)));
                }
                return node(n.token, A, B);
            }
            case SUM:
            case SUB:
            case DIV:
            case EXP:
                return node(n.token, expand(left(n)), expand(right(n)));
            default:
                return n;
        }
    }

    private double denominatorsProduct(NodoAST n) {
        if (n == null || n.token == null) return 1.0;
        if (n.token.type == LexToken.Type.DIV && isNumeric(right(n))) {
            double d = Math.abs(evalNumeric(right(n)));
            if (d == 0) return 1.0;
            return denominatorsProduct(left(n)) * denominatorsProduct(right(n)) * d;
        }
        double p = 1.0;
        if (n.hijos != null) for (NodoAST h : n.hijos) p *= denominatorsProduct(h);
        return p;
    }

    private Linear linearize(NodoAST n, String v) {
        if (n == null) return new Linear(true, 0, 0);
        switch (n.token.type) {
            case INTEGER:
            case DECIMAL:
                return new Linear(true, 0, Double.parseDouble(n.token.value));
            case VARIABLE:
                if (v.equals(n.token.value)) return new Linear(true, 1, 0);
                return new Linear(false, 0, 0);
            case SUM: {
                Linear L = linearize(left(n), v);
                Linear R = linearize(right(n), v);
                if (!L.ok || !R.ok) return new Linear(false, 0, 0);
                return new Linear(true, L.a + R.a, L.b + R.b);
            }
            case SUB: {
                Linear L = linearize(left(n), v);
                Linear R = linearize(right(n), v);
                if (!L.ok || !R.ok) return new Linear(false, 0, 0);
                return new Linear(true, L.a - R.a, L.b - R.b);
            }
            case MUL: {
                NodoAST A = left(n), B = right(n);
                if (isNumeric(A)) {
                    Linear L = linearize(B, v);
                    if (!L.ok) return new Linear(false, 0, 0);
                    double k = evalNumeric(A);
                    return new Linear(true, k * L.a, k * L.b);
                }
                if (isNumeric(B)) {
                    Linear L = linearize(A, v);
                    if (!L.ok) return new Linear(false, 0, 0);
                    double k = evalNumeric(B);
                    return new Linear(true, k * L.a, k * L.b);
                }
                return new Linear(false, 0, 0);
            }
            case DIV: {
                NodoAST A = left(n), B = right(n);
                if (!isNumeric(B)) return new Linear(false, 0, 0);
                Linear L = linearize(A, v);
                if (!L.ok) return new Linear(false, 0, 0);
                double k = evalNumeric(B);
                return new Linear(true, L.a / k, L.b / k);
            }
            case EXP:
                if (containsVar(n, v)) return new Linear(false, 0, 0);
                return new Linear(true, 0, evalNumeric(n));
            default:
                return new Linear(false, 0, 0);
        }
    }

    private String coefTex(double k) {
        if (Math.abs(k - Math.rint(k)) < 1e-12) return String.valueOf((long)Math.rint(k));
        String s = Double.toString(k);
        if (s.contains("E")) return String.format("%.10f", k);
        return s;
    }

    private boolean sameTree(NodoAST a, NodoAST b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (a.token == null || b.token == null) return false;
        if (a.token.type != b.token.type) return false;
        String va = a.token.value == null ? "" : a.token.value;
        String vb = b.token.value == null ? "" : b.token.value;
        if (!va.equals(vb)) return false;
        int sa = a.hijos == null ? 0 : a.hijos.size();
        int sb = b.hijos == null ? 0 : b.hijos.size();
        if (sa != sb) return false;
        for (int i = 0; i < sa; i++) if (!sameTree(a.hijos.get(i), b.hijos.get(i))) return false;
        return true;
    }

    private NodoAST node(LexToken tok, NodoAST a, NodoAST b) {
        List<NodoAST> hs = new ArrayList<>(2);
        hs.add(a); hs.add(b);
        NodoAST n = new NodoAST(tok);
        n.hijos = hs;
        return n;
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

    private NodoAST num(double v) {
        if (Math.abs(v - Math.rint(v)) < 1e-12) {
            long iv = (long)Math.rint(v);
            return new NodoAST(new LexToken(LexToken.Type.INTEGER, String.valueOf(iv), prio(LexToken.Type.INTEGER)));
        }
        return new NodoAST(new LexToken(LexToken.Type.DECIMAL, Double.toString(v), prio(LexToken.Type.DECIMAL)));
    }

    private NodoAST mul(NodoAST a, NodoAST b) { return node(new LexToken(LexToken.Type.MUL, "*", prio(LexToken.Type.MUL)), a, b); }

    private NodoAST makeVar(String name) { return new NodoAST(new LexToken(LexToken.Type.VARIABLE, name, prio(LexToken.Type.VARIABLE))); }

    private static final class Linear {
        final boolean ok; final double a; final double b;
        Linear(boolean ok, double a, double b) { this.ok = ok; this.a = a; this.b = b; }
    }
}
