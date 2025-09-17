package com.example.a22100213_proyectointegrador_logarismos.Semantico;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AnalisisSemantico {

    public static ResultadoSemantico analizar(NodoAST raiz) {
        ResultadoSemantico rs = new ResultadoSemantico();
        if (raiz == null || raiz.token == null) {
            rs.tipoPrincipal = TipoExpresion.T3_EXPRESION_ALGEBRAICA;
            rs.subtipos.add(TipoExpresion.ST_SIMPLE);
            return rs;
        }

        if (raiz.token.type == LexToken.Type.EQUAL) {
            rs.tipoPrincipal = TipoExpresion.T4_ECUACION;
            agregarSubtipos(rs.subtipos, raiz);
            anexarErroresDivisionCero(rs, raiz);
            anexarErroresDominio(rs, raiz);
            anexarErroresFactorial(rs, raiz);
            anexarErroresIntegrales(rs, raiz);
            rs.subtipos.remove(rs.tipoPrincipal);
            if (rs.subtipos.isEmpty()) rs.subtipos.add(TipoExpresion.ST_SIMPLE);
            return rs;
        }

        if (raiz.token.type == LexToken.Type.INTEGRAL_DEF || raiz.token.type == LexToken.Type.INTEGRAL_INDEF) {
            rs.tipoPrincipal = TipoExpresion.T9_CALCULO;
            if (raiz.token.type == LexToken.Type.INTEGRAL_DEF) rs.subtipos.add(TipoExpresion.ST_INTEGRAL_DEFINIDA);
            else rs.subtipos.add(TipoExpresion.ST_INTEGRAL_INDEFINIDA);
            agregarSubtipos(rs.subtipos, raiz);
            anexarErroresDivisionCero(rs, raiz);
            anexarErroresDominio(rs, raiz);
            anexarErroresFactorial(rs, raiz);
            anexarErroresIntegrales(rs, raiz);
            rs.subtipos.remove(rs.tipoPrincipal);
            if (rs.subtipos.isEmpty()) rs.subtipos.add(TipoExpresion.ST_SIMPLE);
            return rs;
        }

        Double val = evaluarConstante(raiz);
        if (val != null) {
            rs.tipoPrincipal = TipoExpresion.T1_CONSTANTE;
            rs.subtipos.add(TipoExpresion.ST_SIMPLE);
            anexarErroresDivisionCero(rs, raiz);
            anexarErroresDominio(rs, raiz);
            anexarErroresFactorial(rs, raiz);
            return rs;
        }

        rs.tipoPrincipal = tipoPrincipalPorRaiz(raiz);
        agregarSubtipos(rs.subtipos, raiz);
        anexarErroresDivisionCero(rs, raiz);
        anexarErroresDominio(rs, raiz);
        anexarErroresFactorial(rs, raiz);
        anexarErroresIntegrales(rs, raiz);
        rs.subtipos.remove(rs.tipoPrincipal);
        if (rs.subtipos.isEmpty()) rs.subtipos.add(TipoExpresion.ST_SIMPLE);
        return rs;
    }

    private static TipoExpresion tipoPrincipalPorRaiz(NodoAST n) {
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.LOG || t == LexToken.Type.LN || t == LexToken.Type.LOG_BASE2 || t == LexToken.Type.LOG_BASE10) return TipoExpresion.T8_EXPONENCIAL_LOG;
        if (tiposTrig().contains(t)) return TipoExpresion.T7_TRIGONOMETRICA;
        if (t == LexToken.Type.RADICAL) return TipoExpresion.T6_RADICAL;
        if (t == LexToken.Type.DIV || t == LexToken.Type.MUL || t == LexToken.Type.SUM || t == LexToken.Type.SUB || t == LexToken.Type.EXP) return TipoExpresion.T3_EXPRESION_ALGEBRAICA;
        if (t == LexToken.Type.VARIABLE) return TipoExpresion.T2_VARIABLE;
        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL || t == LexToken.Type.CONST_E || t == LexToken.Type.CONST_PI) return TipoExpresion.T1_CONSTANTE;
        if (contieneAlguno(n, tiposLog())) return TipoExpresion.T8_EXPONENCIAL_LOG;
        if (contieneAlguno(n, tiposTrig())) return TipoExpresion.T7_TRIGONOMETRICA;
        if (contieneTipo(n, LexToken.Type.RADICAL)) return TipoExpresion.T6_RADICAL;
        return TipoExpresion.T3_EXPRESION_ALGEBRAICA;
    }

    private static void agregarSubtipos(Set<TipoExpresion> subtipos, NodoAST n) {
        if (contieneTipo(n, LexToken.Type.INTEGRAL_DEF)) subtipos.add(TipoExpresion.ST_INTEGRAL_DEFINIDA);
        if (contieneTipo(n, LexToken.Type.INTEGRAL_INDEF)) subtipos.add(TipoExpresion.ST_INTEGRAL_INDEFINIDA);
        if (esPolinomica(n)) subtipos.add(TipoExpresion.ST_POLINOMICA);
        if (contieneAlguno(n, tiposTrig())) subtipos.add(TipoExpresion.ST_TRIGONOMETRICA);
        if (contieneAlguno(n, tiposLog()) || esExpNoPolinomica(n)) subtipos.add(TipoExpresion.ST_EXPONENCIAL_LOG);
        if (contieneTipo(n, LexToken.Type.DIV)) subtipos.add(TipoExpresion.ST_RACIONAL);
        if (contieneTipo(n, LexToken.Type.RADICAL)) subtipos.add(TipoExpresion.ST_RADICAL);
        if (esOperacionAritmetica(n)) subtipos.add(TipoExpresion.ST_ARITMETICA);
    }

    private static boolean esOperacionAritmetica(NodoAST n) {
        if (n == null || n.token == null) return false;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.EQUAL || t == LexToken.Type.INTEGRAL_DEF || t == LexToken.Type.INTEGRAL_INDEF) return false;
        if (tiposLog().contains(t)) return false;
        if (tiposTrig().contains(t)) return false;
        if (t == LexToken.Type.RADICAL || t == LexToken.Type.FACTORIAL || t == LexToken.Type.SUM || t == LexToken.Type.SUB || t == LexToken.Type.MUL || t == LexToken.Type.DIV || t == LexToken.Type.EXP) {
            for (NodoAST h : n.hijos) if (!esOperacionAritmetica(h) && !esHojaNumericaOVariable(h)) return false;
            return true;
        }
        return esHojaNumericaOVariable(n);
    }

    private static boolean esHojaNumericaOVariable(NodoAST n) {
        LexToken.Type t = n.token.type;
        return t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL || t == LexToken.Type.CONST_E || t == LexToken.Type.CONST_PI || t == LexToken.Type.VARIABLE;
    }

    private static boolean esPolinomica(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (contieneTipo(n, LexToken.Type.DIV)) return false;
        if (contieneAlguno(n, tiposLog())) return false;
        if (contieneAlguno(n, tiposTrig())) return false;
        if (contieneTipo(n, LexToken.Type.RADICAL)) return false;
        return exponenteSiempreEnteroNoNegativo(n);
    }

    private static boolean exponenteSiempreEnteroNoNegativo(NodoAST n) {
        if (n == null) return true;
        if (n.token.type == LexToken.Type.EXP) {
            if (n.hijos.size() != 2) return false;
            Double v = evaluarConstante(n.hijos.get(1));
            if (v == null) return false;
            if (v.doubleValue() < 0) return false;
            if (Math.abs(v - Math.rint(v)) > 1e-9) return false;
        }
        for (NodoAST h : n.hijos) if (!exponenteSiempreEnteroNoNegativo(h)) return false;
        return true;
    }

    private static boolean esExpNoPolinomica(NodoAST n) {
        if (n == null) return false;
        if (n.token.type == LexToken.Type.EXP) {
            if (n.hijos.size() != 2) return true;
            Double v = evaluarConstante(n.hijos.get(1));
            if (v == null) return true;
            if (v.doubleValue() < 0) return true;
            if (Math.abs(v - Math.rint(v)) > 1e-9) return true;
            return false;
        }
        for (NodoAST h : n.hijos) if (esExpNoPolinomica(h)) return true;
        return false;
    }

    private static boolean contieneTipo(NodoAST n, LexToken.Type t) {
        if (n == null) return false;
        if (n.token != null && n.token.type == t) return true;
        for (NodoAST h : n.hijos) if (contieneTipo(h, t)) return true;
        return false;
    }

    private static boolean contieneAlguno(NodoAST n, Set<LexToken.Type> set) {
        if (n == null) return false;
        if (n.token != null && set.contains(n.token.type)) return true;
        for (NodoAST h : n.hijos) if (contieneAlguno(h, set)) return true;
        return false;
    }

    private static Set<LexToken.Type> tiposTrig() {
        Set<LexToken.Type> s = new LinkedHashSet<>();
        s.add(LexToken.Type.TRIG_SIN);
        s.add(LexToken.Type.TRIG_COS);
        s.add(LexToken.Type.TRIG_TAN);
        s.add(LexToken.Type.TRIG_COT);
        s.add(LexToken.Type.TRIG_SEC);
        s.add(LexToken.Type.TRIG_CSC);
        s.add(LexToken.Type.TRIG_ARCSIN);
        s.add(LexToken.Type.TRIG_ARCCOS);
        s.add(LexToken.Type.TRIG_ARCTAN);
        s.add(LexToken.Type.TRIG_ARCCOT);
        s.add(LexToken.Type.TRIG_ARCSEC);
        s.add(LexToken.Type.TRIG_ARCCSC);
        return s;
    }

    private static Set<LexToken.Type> tiposLog() {
        Set<LexToken.Type> s = new LinkedHashSet<>();
        s.add(LexToken.Type.LOG);
        s.add(LexToken.Type.LN);
        s.add(LexToken.Type.LOG_BASE2);
        s.add(LexToken.Type.LOG_BASE10);
        return s;
    }

    private static Set<String> recolectarVariables(NodoAST n) {
        Set<String> vars = new HashSet<>();
        recolectarVariablesRec(n, vars);
        return vars;
    }

    private static void recolectarVariablesRec(NodoAST n, Set<String> acc) {
        if (n == null || n.token == null) return;
        if (n.token.type == LexToken.Type.VARIABLE) acc.add(n.token.value);
        for (NodoAST h : n.hijos) recolectarVariablesRec(h, acc);
    }

    private static Double evaluarConstante(NodoAST n) {
        if (n == null || n.token == null) return null;
        LexToken.Type t = n.token.type;

        if (t == LexToken.Type.VARIABLE) return null;
        if (t == LexToken.Type.INTEGER) return toDouble(n.token.value);
        if (t == LexToken.Type.DECIMAL) return toDouble(n.token.value);
        if (t == LexToken.Type.CONST_PI) return Math.PI;
        if (t == LexToken.Type.CONST_E) return Math.E;

        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB || t == LexToken.Type.MUL || t == LexToken.Type.DIV || t == LexToken.Type.EXP) {
            if (n.hijos.size() < 1) return null;
            if (t == LexToken.Type.SUM || t == LexToken.Type.MUL) {
                Double acc = (t == LexToken.Type.SUM) ? 0.0 : 1.0;
                for (NodoAST h : n.hijos) {
                    Double v = evaluarConstante(h);
                    if (v == null) return null;
                    acc = (t == LexToken.Type.SUM) ? acc + v : acc * v;
                }
                return acc;
            }
            if (t == LexToken.Type.SUB) {
                if (n.hijos.size() != 2) return null;
                Double a = evaluarConstante(n.hijos.get(0));
                Double b = evaluarConstante(n.hijos.get(1));
                if (a == null || b == null) return null;
                return a - b;
            }
            if (t == LexToken.Type.DIV) {
                if (n.hijos.size() != 2) return null;
                Double a = evaluarConstante(n.hijos.get(0));
                Double b = evaluarConstante(n.hijos.get(1));
                if (a == null || b == null) return null;
                if (Math.abs(b) < 1e-15) return null;
                return a / b;
            }
            if (t == LexToken.Type.EXP) {
                if (n.hijos.size() != 2) return null;
                Double a = evaluarConstante(n.hijos.get(0));
                Double b = evaluarConstante(n.hijos.get(1));
                if (a == null || b == null) return null;
                return Math.pow(a, b);
            }
        }

        if (t == LexToken.Type.RADICAL) {
            if (n.hijos.size() != 1) return null;
            Double v = evaluarConstante(n.hijos.get(0));
            if (v == null) return null;
            if (v < 0) return null;
            return Math.sqrt(v);
        }

        if (t == LexToken.Type.FACTORIAL) {
            if (n.hijos.size() != 1) return null;
            Double v = evaluarConstante(n.hijos.get(0));
            if (v == null) return null;
            if (v < 0) return null;
            if (Math.abs(v - Math.rint(v)) > 1e-9) return null;
            long k = Math.round(v);
            double acc = 1.0;
            for (long i = 2; i <= k; i++) acc *= i;
            return acc;
        }

        if (t == LexToken.Type.LOG || t == LexToken.Type.LN || t == LexToken.Type.LOG_BASE2 || t == LexToken.Type.LOG_BASE10) {
            if (n.hijos.size() != 1) return null;
            Double a = evaluarConstante(n.hijos.get(0));
            if (a == null) return null;
            if (a <= 0) return null;
            if (t == LexToken.Type.LN) return Math.log(a);
            if (t == LexToken.Type.LOG) return Math.log10(a);
            if (t == LexToken.Type.LOG_BASE2) return Math.log(a) / Math.log(2.0);
            if (t == LexToken.Type.LOG_BASE10) return Math.log10(a);
        }

        if (tiposTrig().contains(t)) {
            if (n.hijos.size() != 1) return null;
            Double a = evaluarConstante(n.hijos.get(0));
            if (a == null) return null;
            switch (t) {
                case TRIG_SIN: return Math.sin(a);
                case TRIG_COS: return Math.cos(a);
                case TRIG_TAN: return Math.tan(a);
                case TRIG_COT: return 1.0 / Math.tan(a);
                case TRIG_SEC: return 1.0 / Math.cos(a);
                case TRIG_CSC: return 1.0 / Math.sin(a);
                case TRIG_ARCSIN: return Math.asin(a);
                case TRIG_ARCCOS: return Math.acos(a);
                case TRIG_ARCTAN: return Math.atan(a);
                case TRIG_ARCCOT: return Math.atan(1.0 / a);
                case TRIG_ARCSEC: return Math.acos(1.0 / a);
                case TRIG_ARCCSC: return Math.asin(1.0 / a);
                default: return null;
            }
        }

        return null;
    }

    private static Double toDouble(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return null; }
    }

    private static void anexarErroresDivisionCero(ResultadoSemantico rs, NodoAST n) {
        if (n == null) return;
        if (n.token != null && n.token.type == LexToken.Type.DIV) {
            if (n.hijos.size() == 2) {
                Double den = evaluarConstante(n.hijos.get(1));
                if (den != null && Math.abs(den) < 1e-15) rs.errores.add(SemanticoError.error("DIV_CERO", "División entre cero", ruta(n)));
            }
        }
        for (NodoAST h : n.hijos) anexarErroresDivisionCero(rs, h);
    }

    private static void anexarErroresDominio(ResultadoSemantico rs, NodoAST n) {
        if (n == null) return;
        if (n.token != null && (n.token.type == LexToken.Type.LOG || n.token.type == LexToken.Type.LN || n.token.type == LexToken.Type.LOG_BASE2 || n.token.type == LexToken.Type.LOG_BASE10)) {
            if (n.hijos.size() == 1) {
                Double a = evaluarConstante(n.hijos.get(0));
                if (a != null && a <= 0) rs.errores.add(SemanticoError.error("DOM_LOG", "Argumento no positivo en logaritmo", ruta(n)));
            }
        }
        if (n.token != null && n.token.type == LexToken.Type.RADICAL) {
            if (n.hijos.size() == 1) {
                Double a = evaluarConstante(n.hijos.get(0));
                if (a != null && a < 0) rs.errores.add(SemanticoError.error("DOM_RAD", "Radicando negativo en raíz", ruta(n)));
            }
        }
        for (NodoAST h : n.hijos) anexarErroresDominio(rs, h);
    }

    private static void anexarErroresFactorial(ResultadoSemantico rs, NodoAST n) {
        if (n == null) return;
        if (n.token != null && n.token.type == LexToken.Type.FACTORIAL) {
            if (n.hijos.size() == 1) {
                Double a = evaluarConstante(n.hijos.get(0));
                if (a != null) {
                    if (a < 0 || Math.abs(a - Math.rint(a)) > 1e-9) rs.errores.add(SemanticoError.error("DOM_FACT", "Factorial definido solo para enteros no negativos", ruta(n)));
                }
            }
        }
        for (NodoAST h : n.hijos) anexarErroresFactorial(rs, h);
    }

    private static void anexarErroresIntegrales(ResultadoSemantico rs, NodoAST n) {
        if (n == null) return;
        for (NodoAST h : n.hijos) anexarErroresIntegrales(rs, h);
    }

    private static List<Integer> ruta(NodoAST n) {
        List<Integer> r = new ArrayList<>();
        return r;
    }
}
