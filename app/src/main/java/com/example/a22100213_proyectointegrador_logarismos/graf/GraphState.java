package com.example.a22100213_proyectointegrador_logarismos.graf;

import com.example.a22100213_proyectointegrador_logarismos.NodoAST;

public final class GraphState {
    public static final GraphState I = new GraphState();
    public NodoAST ast;
    public String modo;
    public String var;
    public boolean radians;
    public String labelLatex;
    public Double limA;
    public Double limB;
    private GraphState() {}
}
