package com.example.a22100213_proyectointegrador_logarismos;

import java.util.ArrayList;
import java.util.List;

public class NodoAST {
    public LexToken token;
    public List<NodoAST> hijos;

    public NodoAST(LexToken token) {
        this.token = token;
        this.hijos = new ArrayList<>();
    }

    public void addHijo(NodoAST hijo) {
        hijos.add(hijo);
    }

    @Override
    public String toString() {
        if (hijos.isEmpty()) return token.toString();
        StringBuilder sb = new StringBuilder(token.toString());
        sb.append(" -> [");
        for (int i = 0; i < hijos.size(); i++) {
            sb.append(hijos.get(i).toString());
            if (i < hijos.size() - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}
