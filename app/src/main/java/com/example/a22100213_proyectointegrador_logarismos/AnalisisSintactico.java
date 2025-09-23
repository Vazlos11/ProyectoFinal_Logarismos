package com.example.a22100213_proyectointegrador_logarismos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnalisisSintactico {
    private final List<LexToken> tokens;
    private int current = 0;

    public AnalisisSintactico(List<LexToken> tokens) {
        this.tokens = new ArrayList<>(tokens == null ? Collections.emptyList() : tokens);
        if (this.tokens.isEmpty() || this.tokens.get(this.tokens.size() - 1).type != LexToken.Type.EOF) {
            this.tokens.add(new LexToken(LexToken.Type.EOF, "", 0));
        }
        current = 0;
    }

    public NodoAST parse() {
        NodoAST expr = parseExpression();
        consume(LexToken.Type.EOF, "Se esperaba fin de entrada.");
        return expr;
    }

    private NodoAST parseExpression() { return parseEquality(); }

    private NodoAST parseEquality() {
        NodoAST left = parseAddSub();
        if (match(LexToken.Type.EQUAL)) {
            LexToken op = previous();
            NodoAST right = parseAddSub();
            NodoAST node = new NodoAST(op);
            link(node, left);
            link(node, right);
            return node;
        }
        return left;
    }

    private NodoAST parseAddSub() {
        NodoAST left = parseMulDiv();
        while (match(LexToken.Type.SUM, LexToken.Type.SUB)) {
            LexToken op = previous();
            NodoAST right = parseMulDiv();
            NodoAST node = new NodoAST(op);
            link(node, left);
            link(node, right);
            left = node;
        }
        return left;
    }

    private NodoAST parseMulDiv() {
        NodoAST left = parsePower();
        while (match(LexToken.Type.MUL, LexToken.Type.DIV)) {
            LexToken op = previous();
            NodoAST right = parsePower();
            NodoAST node = new NodoAST(op);
            link(node, left);
            link(node, right);
            left = node;
        }
        return left;
    }

    private NodoAST parsePower() {
        NodoAST base = parseUnary();
        base = parsePostfix(base);
        if (match(LexToken.Type.EXP)) {
            LexToken op = previous();
            NodoAST exp = parsePower();
            NodoAST node = new NodoAST(op);
            link(node, base);
            link(node, exp);
            return node;
        }
        return base;
    }

    private NodoAST parseUnary() {
        if (match(LexToken.Type.SUB)) {
            LexToken op = previous();
            NodoAST zero = new NodoAST(new LexToken(LexToken.Type.INTEGER, "0", 1));
            NodoAST right = parseUnary();
            NodoAST node = new NodoAST(op);
            link(node, zero);
            link(node, right);
            return node;
        }
        if (match(LexToken.Type.SUM)) return parseUnary();
        return parsePrimary();
    }

    private NodoAST parsePostfix(NodoAST base) {
        while (match(LexToken.Type.FACTORIAL, LexToken.Type.PERCENT, LexToken.Type.PRIME)) {
            LexToken op = previous();
            NodoAST node = new NodoAST(op);
            link(node, base);
            base = node;
        }
        return base;
    }

    private NodoAST parsePrimary() {
        NodoAST sys = parseSystemIfAny();
        if (sys != null) return sys;

        if (match(LexToken.Type.PAREN_OPEN)) {
            NodoAST inside = parseExpression();
            consume(LexToken.Type.PAREN_CLOSE, "Se esperaba ')'.");
            return inside;
        }

        if (match(
                LexToken.Type.INTEGER, LexToken.Type.DECIMAL, LexToken.Type.IMAGINARY,
                LexToken.Type.VARIABLE, LexToken.Type.CONST_PI, LexToken.Type.CONST_E,
                LexToken.Type.DIFFERENTIAL
        )) {
            return new NodoAST(previous());
        }

        if (match(LexToken.Type.RADICAL)) {
            LexToken op = previous();
            NodoAST arg = parsePrimary();
            NodoAST node = new NodoAST(op);
            link(node, arg);
            return node;
        }

        if (match(LexToken.Type.LOG, LexToken.Type.LN, LexToken.Type.LOG_BASE2, LexToken.Type.LOG_BASE10)) {
            LexToken op = previous();
            NodoAST arg = parsePrimary();
            NodoAST node = new NodoAST(op);
            link(node, arg);
            return node;
        }

        if (match(
                LexToken.Type.TRIG_SIN, LexToken.Type.TRIG_COS, LexToken.Type.TRIG_TAN,
                LexToken.Type.TRIG_COT, LexToken.Type.TRIG_SEC, LexToken.Type.TRIG_CSC,
                LexToken.Type.TRIG_ARCSIN, LexToken.Type.TRIG_ARCCOS, LexToken.Type.TRIG_ARCTAN,
                LexToken.Type.TRIG_ARCCOT, LexToken.Type.TRIG_ARCSEC, LexToken.Type.TRIG_ARCCSC
        )) {
            LexToken op = previous();
            NodoAST arg = parsePrimary();
            NodoAST node = new NodoAST(op);
            link(node, arg);
            return node;
        }

        if (match(LexToken.Type.INTEGRAL_INDEF)) {
            LexToken op = previous();
            consume(LexToken.Type.PAREN_OPEN, "Se esperaba '(' después de ∫.");
            NodoAST body = parseExpression();
            consume(LexToken.Type.PAREN_CLOSE, "Se esperaba ')' al cerrar el integrando.");
            NodoAST node = new NodoAST(op);
            node.hijos.add(body);
            if (match(LexToken.Type.DIFFERENTIAL)) node.hijos.add(new NodoAST(previous()));
            return node;
        }

        if (match(LexToken.Type.DERIV)) {
            LexToken op = previous();
            NodoAST arg;
            if (match(LexToken.Type.PAREN_OPEN)) {
                arg = parseExpression();
                consume(LexToken.Type.PAREN_CLOSE, "Se esperaba ')' al cerrar el argumento del diferencial.");
            } else {
                arg = parsePower();
            }
            NodoAST node = new NodoAST(op);
            link(node, arg);
            return node;
        }

        if (match(LexToken.Type.ABS_OPEN)) {
            NodoAST inside = parseExpression();
            consume(LexToken.Type.ABS_CLOSE, "Se esperaba '|' de cierre en valor absoluto.");
            NodoAST node = new NodoAST(new LexToken(LexToken.Type.ABS, "| |", 9));
            link(node, inside);
            return node;
        }

        if (match(LexToken.Type.INTEGRAL_DEF)) {
            LexToken op = previous();
            NodoAST lower = parseGroupedOrAtomic("límite inferior");
            NodoAST upper = parseGroupedOrAtomic("límite superior");
            NodoAST body;
            if (check(LexToken.Type.PAREN_OPEN)) {
                advance();
                body = parseExpression();
                consume(LexToken.Type.PAREN_CLOSE, "Se esperaba ')' al cerrar el integrando.");
            } else {
                body = parsePrimary();
            }
            consume(LexToken.Type.DIFFERENTIAL, "Se esperaba diferencial (dx o dy) en integral definida.");
            NodoAST node = new NodoAST(op);
            link(node, lower);
            link(node, upper);
            link(node, body);
            link(node, new NodoAST(previous()));
            return node;
        }

        throw error(peek(), "Token inesperado: " + peek().type + " '" + peek().value + "'");
    }

    private void link(NodoAST parent, NodoAST child) {
        if (parent != null && child != null) {
            child.parent = parent;
            parent.hijos.add(child);
        }
    }

    private NodoAST parseSystemIfAny() {
        if (!match(LexToken.Type.SYSTEM_BEGIN)) return null;
        LexToken sysTok = new LexToken(LexToken.Type.SYSTEM, "SYSTEM", 0);
        NodoAST system = new NodoAST(sysTok);
        link(system, parseExpression());
        while (match(LexToken.Type.ROW_SEP)) link(system, parseExpression());
        consume(LexToken.Type.SYSTEM_END, "Se esperaba '}' al cerrar el sistema.");
        return system;
    }

    private NodoAST parseGroupedOrAtomic(String what) {
        if (match(LexToken.Type.PAREN_OPEN)) {
            NodoAST e = parseExpression();
            consume(LexToken.Type.PAREN_CLOSE, "Se esperaba ')' al cerrar " + what + ".");
            return e;
        }
        return parsePrimary();
    }

    private boolean match(LexToken.Type... types) {
        for (LexToken.Type t : types) {
            if (check(t)) { advance(); return true; }
        }
        return false;
    }

    private boolean check(LexToken.Type type) {
        return peek().type == type;
    }

    private LexToken advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return current >= tokens.size() || tokens.get(current).type == LexToken.Type.EOF;
    }

    private LexToken peek() {
        int idx = Math.min(current, tokens.size() - 1);
        return tokens.get(idx);
    }

    private LexToken previous() {
        int idx = Math.max(0, current - 1);
        return tokens.get(idx);
    }

    private void consume(LexToken.Type type, String message) {
        if (check(type)) { advance(); return; }
        throw error(peek(), message);
    }

    private RuntimeException error(LexToken tok, String msg) {
        return new RuntimeException(msg);
    }
}
