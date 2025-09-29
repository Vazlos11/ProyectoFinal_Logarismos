package com.example.a22100213_proyectointegrador_logarismos.resolucion;

public class SymjaBridge {
    public static Double evalDouble(String expr) {
        try {
            String s = expr.trim();
            if (s.matches("[-+]?[0-9]*\\.?[0-9]+")) return Double.parseDouble(s);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static String simplify(String expr) {
        return expr;
    }

    public static String toTeX(String expr) {
        if (expr == null) return "";
        return "$$" + expr + "$$";
    }
}
