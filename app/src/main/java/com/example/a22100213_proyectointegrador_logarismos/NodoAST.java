package com.example.a22100213_proyectointegrador_logarismos;

import java.util.ArrayList;
import java.util.List;

public class NodoAST {

    public LexToken token;
    public List<NodoAST> hijos;
    public NodoAST parent;

    public NodoAST(LexToken token) {
        this.token = token;
        this.hijos = new ArrayList<>();
        this.parent = null;
    }

    public void addHijo(NodoAST hijo) {
        if (hijo != null) {
            hijo.parent = this;
            this.hijos.add(hijo);
        }
    }

    public NodoAST getHijo(int index) {
        return (index >= 0 && index < hijos.size()) ? hijos.get(index) : null;
    }

    public int numHijos() {
        return hijos.size();
    }

    @Override
    public String toString() {
        String tv = (token == null) ? "null" : (token.type + (token.value == null ? "" : "(" + token.value + ")"));
        return "NodoAST{" + tv + ", hijos=" + hijos.size() + "}";
    }
}
