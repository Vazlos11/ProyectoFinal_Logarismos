package com.example.a22100213_proyectointegrador_logarismos;

public class LexToken {

    public enum Type {
        INTEGER,
        DECIMAL,
        VARIABLE,
        CONST_E,
        CONST_PI,
        IMAGINARY,
        SUM,
        SUB,
        MUL,
        DIV,
        EXP,
        RADICAL,
        EQUAL,
        DERIV,
        PRIME,
        FACTORIAL,
        PERCENT,
        INTEGRAL_INDEF,
        INTEGRAL_DEF,
        DIFFERENTIAL,
        TRIG_SIN,
        TRIG_COS,
        TRIG_TAN,
        TRIG_SEC,
        TRIG_CSC,
        TRIG_COT,
        TRIG_ARCSIN,
        TRIG_ARCCOS,
        TRIG_ARCTAN,
        TRIG_ARCCOT,
        TRIG_ARCSEC,
        TRIG_ARCCSC,
        LOG,
        LN,
        LOG_BASE10,
        LOG_BASE2,
        ABS_OPEN,
        ABS_CLOSE,
        ABS,
        PAREN_OPEN,
        PAREN_CLOSE,
        SYSTEM_BEGIN,
        SYSTEM_END,
        ROW_SEP,
        SYSTEM,
        EOF
    }

    public Type type;
    public String value;
    public int prioridad;
    public int start;
    public int end;

    public LexToken(Type type, String value, int prioridad) {
        this.type = type;
        this.value = value;
        this.prioridad = prioridad;
    }

    @Override
    public String toString() {
        String v = value == null ? "" : value;
        return type + "(" + v + "," + prioridad + ")";
    }
}
