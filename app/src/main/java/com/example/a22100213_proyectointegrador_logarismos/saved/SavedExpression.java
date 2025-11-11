package com.example.a22100213_proyectointegrador_logarismos.saved;

public final class SavedExpression {
    public final String id;
    public final String expr;
    public final String latex;
    public final long ts;

    public SavedExpression(String id, String expr, String latex, long ts) {
        this.id = id;
        this.expr = expr;
        this.latex = latex;
        this.ts = ts;
    }
}
