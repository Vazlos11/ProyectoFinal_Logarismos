package com.example.a22100213_proyectointegrador_logarismos;

import java.util.ArrayList;
import java.util.List;

public class Token {

    public String value;
    public boolean isContainer;
    public boolean isAtomic;
    public List<Token> children;
    public Token parent;

    public Token() {
        this.value = "";
        this.isContainer = false;
        this.isAtomic = false;
        this.children = new ArrayList<>();
        this.parent = null;
    }

    public Token(String value, boolean isContainer, boolean isAtomic) {
        this.value = value;
        this.isContainer = isContainer;
        this.isAtomic = isAtomic;
        this.children = new ArrayList<>();
        this.parent = null;
    }

    public static Token container(String value) {
        return new Token(value, true, false);
    }

    public static Token atomic(String value) {
        return new Token(value, false, true);
    }

    public static Token simple(String value) {
        return new Token(value, false, false);
    }

    public static Token system(int rows) {
        Token sys = container("\\system");
        if (rows < 1) rows = 1;
        for (int i = 0; i < rows; i++) {
            Token row = container("()");
            row.parent = sys;
            sys.children.add(row);
        }
        return sys;
    }

    public String toLatex() {
        return value == null ? "" : value;
    }
}
