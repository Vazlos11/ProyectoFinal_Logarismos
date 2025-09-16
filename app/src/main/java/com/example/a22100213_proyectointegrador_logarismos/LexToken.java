package com.example.a22100213_proyectointegrador_logarismos;

public class LexToken {
    public int prioridad;
    public enum Type {
        INTEGER, DECIMAL, IMAGINARY,
        VARIABLE,
        SUM, SUB, MUL, DIV, EXP, RADICAL,
        PAREN_OPEN, PAREN_CLOSE,
        TRIG_SIN, TRIG_COS, TRIG_TAN, TRIG_COT, TRIG_SEC, TRIG_CSC,
        TRIG_ARCSIN, TRIG_ARCCOS, TRIG_ARCTAN, TRIG_ARCCOT, TRIG_ARCSEC, TRIG_ARCCSC,
        LOG, LN, LOG_BASE2, LOG_BASE10,
        CONST_PI, CONST_E,
        FACTORIAL,
        INTEGRAL_DEF, INTEGRAL_INDEF,
        DIFFERENTIAL, EQUAL,
        EOF
    }

    public Type type;
    public String value;

    public LexToken(Type type, String value, int prioridad) {
        this.type = type;
        this.value = value;
        this.prioridad = prioridad;
    }

    @Override
    public String toString() {
        return type + "(" + value + ", prec=" + prioridad + ")";
    }
}
