package com.example.a22100213_proyectointegrador_logarismos;

import java.util.List;

public class AnalisisSintactico {
    private List<LexToken> tokens;
    private int pos = 0;

    public AnalisisSintactico(List<LexToken> tokens) {
        this.tokens = tokens;
    }

    private LexToken lookahead() {
        return tokens.get(pos);
    }

    private LexToken consume() {
        return tokens.get(pos++);
    }

    public NodoAST parse() {
        NodoAST expr = parseEcuacion();
        if (lookahead().type != LexToken.Type.EOF) {
            throw new RuntimeException("Error de sintaxis: tokens inesperados después de la expresión.");
        }
        return expr;
    }

    private NodoAST parseEcuacion() {
        NodoAST izquierda = parseExpresion();
        if (lookahead().type == LexToken.Type.EQUAL) {
            consume();
            if (lookahead().type == LexToken.Type.EOF) {
                throw new RuntimeException("Ecuación inválida: falta lado derecho.");
            }
            NodoAST derecha = parseExpresion();
            NodoAST nodo = new NodoAST(new LexToken(LexToken.Type.EQUAL, "=", 2));
            nodo.addHijo(izquierda);
            nodo.addHijo(derecha);
            return nodo;
        }
        return izquierda;
    }

    private NodoAST parseExpresion() {
        NodoAST nodo = parseTermino();
        while (lookahead().type == LexToken.Type.SUM || lookahead().type == LexToken.Type.SUB) {
            LexToken op = consume();
            NodoAST derecho = parseTermino();
            NodoAST padre = new NodoAST(op);
            padre.addHijo(nodo);
            padre.addHijo(derecho);
            nodo = padre;
        }
        return nodo;
    }

    private NodoAST parseTermino() {
        NodoAST nodo = parseFactor();
        while (lookahead().type == LexToken.Type.MUL || lookahead().type == LexToken.Type.DIV) {
            LexToken op = consume();
            NodoAST derecho = parseFactor();
            NodoAST padre = new NodoAST(op);
            padre.addHijo(nodo);
            padre.addHijo(derecho);
            nodo = padre;
        }
        return nodo;
    }

    private NodoAST parseFactor() {
        NodoAST nodo = parsePotencia();
        while (lookahead().type == LexToken.Type.FACTORIAL) {
            LexToken op = consume();
            NodoAST padre = new NodoAST(op);
            padre.addHijo(nodo);
            nodo = padre;
        }
        return nodo;
    }

    private NodoAST parsePotencia() {
        NodoAST base = parsePrimario();
        if (lookahead().type == LexToken.Type.EXP) {
            LexToken op = consume();
            if (lookahead().type == LexToken.Type.EOF || lookahead().type == LexToken.Type.PAREN_CLOSE || lookahead().type == LexToken.Type.ABS_CLOSE) {
                throw new RuntimeException("Falta exponente después de ^");
            }
            NodoAST exponente = parsePotencia();
            NodoAST padre = new NodoAST(op);
            padre.addHijo(base);
            padre.addHijo(exponente);
            return padre;
        }
        return base;
    }

    private NodoAST parsePrimario() {
        LexToken tok = lookahead();
        if (tok.type == LexToken.Type.SUB || tok.type == LexToken.Type.SUM) {
            LexToken op = consume();
            NodoAST rhs = parsePrimario();
            LexToken zero = new LexToken(LexToken.Type.INTEGER, "0", 1);
            NodoAST lhs = new NodoAST(zero);
            NodoAST padre = new NodoAST(op);
            padre.addHijo(lhs);
            padre.addHijo(rhs);
            return padre;
        }
        switch (tok.type) {
            case INTEGER:
            case DECIMAL:
            case IMAGINARY:
            case VARIABLE:
            case CONST_PI:
            case CONST_E:
                return new NodoAST(consume());
            case PAREN_OPEN:
                consume();
                NodoAST dentro = parseExpresion();
                if (lookahead().type != LexToken.Type.PAREN_CLOSE) {
                    throw new RuntimeException("Falta cerrar paréntesis.");
                }
                consume();
                return dentro;
            case ABS_OPEN:
                consume();
                NodoAST absArg = parseExpresion();
                if (lookahead().type != LexToken.Type.ABS_CLOSE) {
                    throw new RuntimeException("Falta cerrar valor absoluto.");
                }
                consume();
                NodoAST abs = new NodoAST(new LexToken(LexToken.Type.ABS, "| |", 8));
                abs.addHijo(absArg);
                return abs;
            case RADICAL:
                consume();
                if (lookahead().type == LexToken.Type.EOF) {
                    throw new RuntimeException("Error de sintaxis: raíz sin radicando.");
                }
                NodoAST radicando = parsePrimario();
                NodoAST raiz = new NodoAST(tok);
                raiz.addHijo(radicando);
                return raiz;
            case TRIG_SIN:
            case TRIG_COS:
            case TRIG_TAN:
            case TRIG_COT:
            case TRIG_SEC:
            case TRIG_CSC:
            case TRIG_ARCSIN:
            case TRIG_ARCCOS:
            case TRIG_ARCTAN:
            case TRIG_ARCCOT:
            case TRIG_ARCSEC:
            case TRIG_ARCCSC:
            case LOG:
            case LN:
            case LOG_BASE2:
            case LOG_BASE10:
                return parseFuncion();
            case INTEGRAL_DEF:
            case INTEGRAL_INDEF:
                return parseIntegral();
            default:
                throw new RuntimeException("Token inesperado: " + tok);
        }
    }

    private NodoAST parseFuncion() {
        LexToken func = consume();
        NodoAST nodo = new NodoAST(func);
        if (lookahead().type == LexToken.Type.PAREN_OPEN) {
            consume();
            if (lookahead().type == LexToken.Type.PAREN_CLOSE) {
                throw new RuntimeException("Función sin argumento.");
            }
            NodoAST arg = parseExpresion();
            if (lookahead().type != LexToken.Type.PAREN_CLOSE) {
                throw new RuntimeException("Falta cerrar paréntesis en función.");
            }
            consume();
            nodo.addHijo(arg);
        } else {
            throw new RuntimeException("Las funciones requieren argumento entre paréntesis.");
        }
        return nodo;
    }

    private NodoAST parseIntegral() {
        LexToken integral = consume();
        NodoAST nodo = new NodoAST(integral);
        if (integral.type == LexToken.Type.INTEGRAL_DEF) {
            if (lookahead().type == LexToken.Type.EOF) {
                throw new RuntimeException("Integral definida incompleta.");
            }
            NodoAST sup = parseExpresion();
            NodoAST inf = parseExpresion();
            NodoAST cuerpo = parseExpresion();
            if (lookahead().type != LexToken.Type.DIFFERENTIAL) {
                throw new RuntimeException("Integral definida sin diferencial.");
            }
            LexToken dx = consume();
            nodo.addHijo(inf);
            nodo.addHijo(sup);
            nodo.addHijo(cuerpo);
            nodo.addHijo(new NodoAST(dx));
        } else {
            NodoAST cuerpo = parseExpresion();
            if (lookahead().type != LexToken.Type.DIFFERENTIAL) {
                throw new RuntimeException("Integral indefinida sin diferencial.");
            }
            nodo.addHijo(cuerpo);
            nodo.addHijo(new NodoAST(consume()));
        }
        return nodo;
    }
}
