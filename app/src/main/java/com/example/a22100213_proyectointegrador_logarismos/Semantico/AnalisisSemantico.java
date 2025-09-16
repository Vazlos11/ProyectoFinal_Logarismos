package com.example.a22100213_proyectointegrador_logarismos.Semantico;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;

import java.util.ArrayList;
import java.util.EnumSet;
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

        boolean hayIntegral = contieneAlguno(raiz, EnumSet.of(
                LexToken.Type.INTEGRAL_DEF, LexToken.Type.INTEGRAL_INDEF
        ));
        boolean hayIgualdad = contieneTipo(raiz, LexToken.Type.EQUAL);
        boolean hayDivision = contieneTipo(raiz, LexToken.Type.DIV);
        boolean hayRadical = contieneTipo(raiz, LexToken.Type.RADICAL);
        boolean hayTrig = contieneAlguno(raiz, tiposTrig());
        boolean hayExpLog = esExponencialOLogaritmica(raiz);

        if (hayIntegral) {
            rs.tipoPrincipal = TipoExpresion.T9_CALCULO;
            if (contieneTipo(raiz, LexToken.Type.INTEGRAL_DEF)) rs.subtipos.add(TipoExpresion.ST_INTEGRAL_DEFINIDA);
            if (contieneTipo(raiz, LexToken.Type.INTEGRAL_INDEF)) rs.subtipos.add(TipoExpresion.ST_INTEGRAL_INDEFINIDA);
            List<NodoAST> integrales = recolectarIntegrales(raiz);
            for (NodoAST integ : integrales) {
                NodoAST integrando = obtenerIntegrando(integ);
                if (integrando != null) agregarSubtiposPorContenido(rs.subtipos, integrando);
            }
        } else if (hayIgualdad) {
            rs.tipoPrincipal = TipoExpresion.T4_ECUACION;
            agregarSubtiposPorContenido(rs.subtipos, raiz);
        } else if (hayDivision) {
            rs.tipoPrincipal = TipoExpresion.T5_FRACCION_ALGEBRAICA;
            agregarSubtiposPorContenido(rs.subtipos, raiz);
        } else if (hayTrig) {
            rs.tipoPrincipal = TipoExpresion.T7_TRIGONOMETRICA;
            agregarSubtiposPorContenido(rs.subtipos, raiz);
        } else if (hayExpLog) {
            rs.tipoPrincipal = TipoExpresion.T8_EXPONENCIAL_LOG;
            agregarSubtiposPorContenido(rs.subtipos, raiz);
        } else if (hayRadical) {
            rs.tipoPrincipal = TipoExpresion.T6_RADICAL;
            agregarSubtiposPorContenido(rs.subtipos, raiz);
        } else if (esConstante(raiz)) {
            rs.tipoPrincipal = TipoExpresion.T1_CONSTANTE;
            rs.subtipos.add(TipoExpresion.ST_SIMPLE);
        } else if (esVariablePura(raiz)) {
            rs.tipoPrincipal = TipoExpresion.T2_VARIABLE;
            rs.subtipos.add(TipoExpresion.ST_SIMPLE);
        } else {
            rs.tipoPrincipal = TipoExpresion.T3_EXPRESION_ALGEBRAICA;
            agregarSubtiposPorContenido(rs.subtipos, raiz);
        }

        rs.subtipos.remove(rs.tipoPrincipal);
        if (rs.subtipos.isEmpty()) rs.subtipos.add(TipoExpresion.ST_SIMPLE);
        return rs;
    }

    private static void agregarSubtiposPorContenido(Set<TipoExpresion> subtipos, NodoAST n) {
        boolean poli = esPolinomica(n);
        boolean trig = contieneAlguno(n, tiposTrig());
        boolean explog = esExponencialOLogaritmica(n);
        boolean racional = contieneTipo(n, LexToken.Type.DIV);
        boolean radical = contieneTipo(n, LexToken.Type.RADICAL);
        if (poli) subtipos.add(TipoExpresion.ST_POLINOMICA);
        if (trig) subtipos.add(TipoExpresion.ST_TRIGONOMETRICA);
        if (explog) subtipos.add(TipoExpresion.ST_EXPONENCIAL_LOG);
        if (racional) subtipos.add(TipoExpresion.ST_RACIONAL);
        if (radical) subtipos.add(TipoExpresion.ST_RADICAL);
    }

    private static List<NodoAST> recolectarIntegrales(NodoAST n) {
        List<NodoAST> out = new ArrayList<>();
        recolectarIntegralesRec(n, out);
        return out;
    }

    private static void recolectarIntegralesRec(NodoAST n, List<NodoAST> acc) {
        if (n == null) return;
        if (n.token != null && (n.token.type == LexToken.Type.INTEGRAL_DEF || n.token.type == LexToken.Type.INTEGRAL_INDEF)) {
            acc.add(n);
        }
        for (NodoAST h : n.hijos) recolectarIntegralesRec(h, acc);
    }

    private static NodoAST obtenerIntegrando(NodoAST integral) {
        if (integral == null || integral.token == null) return null;
        if (integral.token.type == LexToken.Type.INTEGRAL_INDEF) {
            if (integral.hijos.size() >= 1) return integral.hijos.get(0);
            return null;
        }
        if (integral.token.type == LexToken.Type.INTEGRAL_DEF) {
            if (integral.hijos.size() >= 3) return integral.hijos.get(2);
            return null;
        }
        return null;
    }

    private static boolean contieneTipo(NodoAST n, LexToken.Type t) {
        if (n == null || n.token == null) return false;
        if (n.token.type == t) return true;
        for (NodoAST h : n.hijos) if (contieneTipo(h, t)) return true;
        return false;
    }

    private static boolean contieneAlguno(NodoAST n, Set<LexToken.Type> set) {
        if (n == null || n.token == null) return false;
        if (set.contains(n.token.type)) return true;
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

    private static boolean esPolinomica(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (contieneTipo(n, LexToken.Type.DIV)) return false;
        if (contieneAlguno(n, EnumSet.of(
                LexToken.Type.LOG, LexToken.Type.LN, LexToken.Type.LOG_BASE2, LexToken.Type.LOG_BASE10,
                LexToken.Type.RADICAL, LexToken.Type.INTEGRAL_DEF, LexToken.Type.INTEGRAL_INDEF
        ))) return false;
        return esPolinomicaRec(n);
    }

    private static boolean esPolinomicaRec(NodoAST n) {
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL || t == LexToken.Type.VARIABLE
                || t == LexToken.Type.CONST_PI || t == LexToken.Type.CONST_E) {
            return true;
        }
        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB || t == LexToken.Type.MUL) {
            for (NodoAST h : n.hijos) if (!esPolinomicaRec(h)) return false;
            return true;
        }
        if (t == LexToken.Type.EXP) {
            if (n.hijos.size() != 2) return false;
            NodoAST base = n.hijos.get(0);
            NodoAST exp = n.hijos.get(1);
            if (base == null || exp == null || base.token == null || exp.token == null) return false;
            if (base.token.type != LexToken.Type.VARIABLE && base.token.type != LexToken.Type.CONST_E && base.token.type != LexToken.Type.CONST_PI) {
                return false;
            }
            if (!esEnteroNoNegativo(exp)) return false;
            return true;
        }
        return false;
    }

    private static boolean esEnteroNoNegativo(NodoAST n) {
        if (n.token.type == LexToken.Type.INTEGER) {
            String v = n.token.value == null ? "" : n.token.value;
            if (v.startsWith("-")) return false;
            return v.matches("\\d+");
        }
        return false;
    }

    private static boolean esExponencialOLogaritmica(NodoAST n) {
        if (contieneAlguno(n, EnumSet.of(
                LexToken.Type.LOG, LexToken.Type.LN, LexToken.Type.LOG_BASE2, LexToken.Type.LOG_BASE10
        ))) return true;
        return contieneExpNoPoli(n);
    }

    private static boolean contieneExpNoPoli(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.EXP) {
            if (n.hijos.size() == 2) {
                NodoAST base = n.hijos.get(0);
                NodoAST exp = n.hijos.get(1);
                if (base != null && base.token != null && exp != null && exp.token != null) {
                    boolean poliPower = (base.token.type == LexToken.Type.VARIABLE || base.token.type == LexToken.Type.CONST_E || base.token.type == LexToken.Type.CONST_PI)
                            && esEnteroNoNegativo(exp);
                    if (!poliPower) return true;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        }
        for (NodoAST h : n.hijos) if (contieneExpNoPoli(h)) return true;
        return false;
    }

    private static boolean esConstante(NodoAST n) {
        if (n.hijos.isEmpty()) {
            return n.token.type == LexToken.Type.INTEGER || n.token.type == LexToken.Type.DECIMAL
                    || n.token.type == LexToken.Type.CONST_E || n.token.type == LexToken.Type.CONST_PI;
        }
        return false;
    }

    private static boolean esVariablePura(NodoAST n) {
        if (n.hijos.isEmpty()) {
            return n.token.type == LexToken.Type.VARIABLE;
        }
        return false;
    }
}
