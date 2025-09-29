package com.example.a22100213_proyectointegrador_logarismos;

import java.util.ArrayList;
import java.util.List;

public class NodoAST {
    public LexToken token;
    public List<NodoAST> hijos = new ArrayList<>();
    public NodoAST parent;

    public NodoAST() {
    }

    public NodoAST(LexToken token) {
        this.token = token;
    }

    public NodoAST(LexToken token, NodoAST child) {
        this.token = token;
        if (child != null) {
            this.hijos.add(child);
            child.parent = this;
        }
    }

    public NodoAST(LexToken token, NodoAST left, NodoAST right) {
        this.token = token;
        if (left != null) {
            this.hijos.add(left);
            left.parent = this;
        }
        if (right != null) {
            this.hijos.add(right);
            right.parent = this;
        }
    }

    public void addHijo(NodoAST hijo) {
        if (hijo != null) {
            hijo.parent = this;
            this.hijos.add(hijo);
        }
    }
}
