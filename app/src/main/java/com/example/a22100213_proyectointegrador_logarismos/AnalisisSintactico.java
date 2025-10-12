package com.example.a22100213_proyectointegrador_logarismos;

import java.util.ArrayList;
import java.util.List;

public class AnalisisSintactico {
    private final List<LexToken> tokens;
    private int pos = 0;

    public AnalisisSintactico(List<LexToken> tokens) {
        this.tokens = (tokens == null) ? new ArrayList<>() : tokens;
    }

    public NodoAST parse() {
        NodoAST root = parseExpr(0);
        expect(LexToken.Type.EOF);
        return root;
    }

    private NodoAST parseExpr(int minPrec) {
        NodoAST left = parsePrefix();
        left = parsePostfix(left);
        while (true) {
            LexToken op = peek();
            if (!isBinary(op)) break;
            int prec = op.prioridad;
            if (prec < minPrec) break;
            next();
            boolean rightAssoc = (op.type == LexToken.Type.EXP);
            int nextMinPrec = rightAssoc ? prec : prec + 1;
            NodoAST right = (op.type == LexToken.Type.EXP) ? parseExponentRHS(nextMinPrec) : parseExpr(nextMinPrec);
            left = makeNode(op, left, right);
            left = parsePostfix(left);
        }
        return left;
    }

    private NodoAST parseExponentRHS(int minPrecForInner) {
        NodoAST a = parseExpr(minPrecForInner);
        while (true) {
            LexToken t = peek();
            if (t.type == LexToken.Type.MUL) {
                next();
                NodoAST b = parseExponentUnit();
                a = makeNode(new LexToken(LexToken.Type.MUL, "*", 6), a, b);
                continue;
            }
            if (canStartExponentUnit(t)) {
                NodoAST b = parseExponentUnit();
                a = makeNode(new LexToken(LexToken.Type.MUL, "*", 6), a, b);
                continue;
            }
            break;
        }
        return a;
    }

    private boolean canStartExponentUnit(LexToken t) {
        if (t == null) return false;
        LexToken.Type tt = t.type;
        if (tt == LexToken.Type.INTEGER) return true;
        if (tt == LexToken.Type.DECIMAL) return true;
        if (tt == LexToken.Type.VARIABLE) return true;
        if (tt == LexToken.Type.CONST_E) return true;
        if (tt == LexToken.Type.CONST_PI) return true;
        if (tt == LexToken.Type.IMAGINARY) return true;
        if (tt == LexToken.Type.PAREN_OPEN) return true;
        if (tt == LexToken.Type.ABS_OPEN) return true;
        if (isUnaryFunc(tt)) return true;
        if (tt == LexToken.Type.LOG_BASE10 || tt == LexToken.Type.LOG_BASE2 || tt == LexToken.Type.LN || tt == LexToken.Type.LOG) return true;
        return false;
    }

    private NodoAST parseExponentUnit() {
        int saved = pos;
        try {
            NodoAST u = parsePrefix();
            u = parsePostfix(u);
            return u;
        } catch (RuntimeException ex) {
            pos = saved;
            return parsePrefix();
        }
    }

    private NodoAST parsePrefix() {
        LexToken t = peek();
        if (t.type == LexToken.Type.SUB) {
            next();
            NodoAST rhs = parsePrefix();
            NodoAST zero = new NodoAST(new LexToken(LexToken.Type.INTEGER, "0", 1));
            return makeNode(new LexToken(LexToken.Type.SUB, "-", 5), zero, rhs);
        }
        if (t.type == LexToken.Type.PAREN_OPEN) {
            next();
            NodoAST e = parseExpr(0);
            expect(LexToken.Type.PAREN_CLOSE);
            return e;
        }
        if (t.type == LexToken.Type.ABS_OPEN) {
            next();
            NodoAST e = parseExpr(0);
            expect(LexToken.Type.ABS_CLOSE);
            return makeNode(new LexToken(LexToken.Type.ABS, "|·|", 0), e);
        }
        if (isAtom(t)) {
            next();
            return new NodoAST(t);
        }
        if (isUnaryFunc(t.type)) {
            next();
            NodoAST arg = parseFuncArg();
            return makeNode(t, arg);
        }
        if (t.type == LexToken.Type.LOG_BASE2 || t.type == LexToken.Type.LOG_BASE10) {
            next();
            NodoAST arg = parseFuncArg();
            return makeNode(t, arg);
        }
        if (t.type == LexToken.Type.RADICAL) {
            next();
            NodoAST arg = parseFuncArg();
            return makeNode(t, arg);
        }
        if (t.type == LexToken.Type.DERIV) {
            next();
            NodoAST arg = parsePrefix();
            return makeNode(t, arg);
        }
        if (t.type == LexToken.Type.INTEGRAL_INDEF) {
            next();
            expect(LexToken.Type.PAREN_OPEN);
            NodoAST body = parseExpr(0);
            expect(LexToken.Type.PAREN_CLOSE);
            NodoAST diff = (peek().type == LexToken.Type.DIFFERENTIAL)
                    ? new NodoAST(next())
                    : new NodoAST(new LexToken(LexToken.Type.DIFFERENTIAL, "dx", 3));
            return makeNode(t, body, diff);
        }
        if (t.type == LexToken.Type.INTEGRAL_DEF) {
            next();
            NodoAST lower = parsePrefix();
            NodoAST upper = parsePrefix();
            NodoAST body  = parsePrefix();
            NodoAST diff  = (peek().type == LexToken.Type.DIFFERENTIAL)
                    ? new NodoAST(next())
                    : new NodoAST(new LexToken(LexToken.Type.DIFFERENTIAL, "dx", 3));
            return makeNode(t, lower, upper, body, diff);
        }
        if (t.type == LexToken.Type.SYSTEM_BEGIN) {
            next();
            List<NodoAST> rows = new ArrayList<>();
            while (peek().type != LexToken.Type.SYSTEM_END && peek().type != LexToken.Type.EOF) {
                rows.add(parseExpr(0));
                if (peek().type == LexToken.Type.ROW_SEP) next();
            }
            expect(LexToken.Type.SYSTEM_END);
            return makeNode(new LexToken(LexToken.Type.SYSTEM, "{}", 0), rows.toArray(new NodoAST[0]));
        }
        throw error("Token inesperado: " + t.type + " '" + t.value + "'");
    }

    private NodoAST parsePostfix(NodoAST base) {
        while (true) {
            LexToken t = peek();
            if (t.type == LexToken.Type.FACTORIAL || t.type == LexToken.Type.PERCENT || t.type == LexToken.Type.PRIME) {
                next();
                base = makeNode(t, base);
                continue;
            }
            break;
        }
        return base;
    }

    private NodoAST parseFuncArg() {
        if (peek().type == LexToken.Type.PAREN_OPEN) {
            next();
            NodoAST e = parseExpr(0);
            expect(LexToken.Type.PAREN_CLOSE);
            return e;
        }
        return parsePrefix();
    }

    private boolean isBinary(LexToken t) {
        if (t == null) return false;
        switch (t.type) {
            case SUM: case SUB: case MUL: case DIV: case EXP: case EQUAL:
                return true;
            default: return false;
        }
    }

    private boolean isUnaryFunc(LexToken.Type tt) {
        switch (tt) {
            case TRIG_SIN: case TRIG_COS: case TRIG_TAN:
            case TRIG_COT: case TRIG_SEC: case TRIG_CSC:
            case TRIG_ARCSIN: case TRIG_ARCCOS: case TRIG_ARCTAN:
            case TRIG_ARCCOT: case TRIG_ARCSEC: case TRIG_ARCCSC:
            case LOG: case LN:
                return true;
            default: return false;
        }
    }

    private boolean isAtom(LexToken t) {
        if (t == null) return false;
        switch (t.type) {
            case INTEGER: case DECIMAL: case IMAGINARY:
            case VARIABLE: case CONST_PI: case CONST_E:
                return true;
            default: return false;
        }
    }

    private NodoAST makeNode(LexToken op, NodoAST... children) {
        NodoAST n = new NodoAST(new LexToken(op.type, op.value, op.prioridad));
        if (children != null) for (NodoAST c : children) n.addHijo(c);
        return n;
    }

    private LexToken peek() {
        if (pos >= tokens.size()) return new LexToken(LexToken.Type.EOF, "", 0);
        return tokens.get(pos);
    }

    private LexToken next() {
        LexToken t = peek();
        pos = Math.min(pos + 1, tokens.size());
        return t;
    }

    private void expect(LexToken.Type type) {
        LexToken t = peek();
        if (t.type != type) throw error("Se esperaba " + type + " y llegó " + t.type);
        next();
    }

    private RuntimeException error(String msg) {
        return new RuntimeException(msg);
    }
}
