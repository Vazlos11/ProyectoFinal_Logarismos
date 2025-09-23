package com.example.a22100213_proyectointegrador_logarismos.Semantico;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AnalisisSemantico {

    public static ResultadoSemantico analizar(NodoAST raiz) {
        ResultadoSemantico rs = new ResultadoSemantico();
        if (raiz == null || raiz.token == null) {
            rs.tipoPrincipal = TipoExpresion.T2_ALGEBRA_FUNC;
            llenarSubtipos(rs, raiz);
            return rs;
        }
        if (raiz.token.type == LexToken.Type.SYSTEM || contarEcuaciones(raiz) > 1) {
            rs.tipoPrincipal = TipoExpresion.T8_SISTEMA_EC;
            validarSistema(rs, raiz);
            validarImaginarios(rs, raiz);
            llenarSubtipos(rs, raiz);
            return rs;
        }
        if (raiz.token.type == LexToken.Type.EQUAL) {
            if (esLinealUnivariable(raiz)) {
                rs.tipoPrincipal = TipoExpresion.T6_DESPEJE_LINEAL;
            } else if (esPolinomicaUnivariableGradoMayorIgual2Enteros(raiz)) {
                rs.tipoPrincipal = TipoExpresion.T7_DESPEJE_POLINOMICO;
            } else {
                rs.tipoPrincipal = TipoExpresion.T2_ALGEBRA_FUNC;
            }
            validarImaginarios(rs, raiz);
            llenarSubtipos(rs, raiz);
            return rs;
        }
        if (raiz.token.type == LexToken.Type.DERIV || raiz.token.type == LexToken.Type.PRIME) {
            rs.tipoPrincipal = TipoExpresion.T3_DERIVADA;
            validarDerivada(rs, raiz);
            validarImaginarios(rs, raiz);
            llenarSubtipos(rs, raiz);
            return rs;
        }
        if (contiene(raiz, LexToken.Type.INTEGRAL_DEF)) {
            rs.tipoPrincipal = TipoExpresion.T5_INTEGRAL_DEFINIDA;
            validarIntegrales(rs, raiz, true);
            validarImaginarios(rs, raiz);
            llenarSubtipos(rs, raiz);
            return rs;
        }
        if (contiene(raiz, LexToken.Type.INTEGRAL_INDEF)) {
            rs.tipoPrincipal = TipoExpresion.T4_INTEGRAL_INDEFINIDA;
            validarIntegrales(rs, raiz, false);
            validarImaginarios(rs, raiz);
            llenarSubtipos(rs, raiz);
            return rs;
        }
        if (contiene(raiz, LexToken.Type.IMAGINARY)) {
            rs.tipoPrincipal = TipoExpresion.T9_IMAGINARIOS;
            llenarSubtipos(rs, raiz);
            return rs;
        }
        if (esT1Aritmetica(raiz)) {
            rs.tipoPrincipal = TipoExpresion.T1_ARITMETICA;
            validarDominiosArit(rs, raiz);
            llenarSubtipos(rs, raiz);
            return rs;
        }
        rs.tipoPrincipal = TipoExpresion.T2_ALGEBRA_FUNC;
        validarImaginarios(rs, raiz);
        llenarSubtipos(rs, raiz);
        return rs;
    }

    private static boolean esT1Aritmetica(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (contiene(n, LexToken.Type.VARIABLE)) return false;
        if (contieneAlguno(n, tiposFunciones())) return false;
        if (contiene(n, LexToken.Type.INTEGRAL_DEF) || contiene(n, LexToken.Type.INTEGRAL_INDEF)) return false;
        if (contiene(n, LexToken.Type.DERIV) || contiene(n, LexToken.Type.PRIME)) return false;
        if (contiene(n, LexToken.Type.EQUAL) || contiene(n, LexToken.Type.SYSTEM_BEGIN)) return false;
        if (contiene(n, LexToken.Type.IMAGINARY)) return false;
        return soloArit(n);
    }

    private static boolean soloArit(NodoAST n) {
        if (n == null || n.token == null) return true;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL || t == LexToken.Type.CONST_E || t == LexToken.Type.CONST_PI) return true;
        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB || t == LexToken.Type.MUL || t == LexToken.Type.DIV || t == LexToken.Type.EXP || t == LexToken.Type.FACTORIAL || t == LexToken.Type.PERCENT) {
            for (NodoAST h : n.hijos) if (!soloArit(h)) return false;
            return true;
        }
        return false;
    }

    private static void validarDominiosArit(ResultadoSemantico rs, NodoAST n) {
        if (n == null) return;
        if (n.token != null && n.token.type == LexToken.Type.DIV) {
            if (n.hijos.size() == 2) {
                Double den = evaluarConstante(n.hijos.get(1));
                if (den != null && Math.abs(den) < 1e-15) rs.errores.add(SemanticoError.error("DIV_CERO", "División entre cero", ruta(n)));
            }
        }
        for (NodoAST h : n.hijos) validarDominiosArit(rs, h);
    }

    private static void validarImaginarios(ResultadoSemantico rs, NodoAST n) {
        if (n == null) return;
        if (contiene(n, LexToken.Type.IMAGINARY) && (contiene(n, LexToken.Type.INTEGRAL_DEF) || contiene(n, LexToken.Type.INTEGRAL_INDEF))) {
            rs.errores.add(SemanticoError.error("IMAG_CALC", "Imaginarios no soportados en integrales", ruta(n)));
        }
        for (NodoAST h : n.hijos) validarImaginarios(rs, h);
    }

    private static void validarDerivada(ResultadoSemantico rs, NodoAST raiz) {
        if (raiz == null) return;
        String var = "x";
        NodoAST cuerpo = null;
        if (raiz.token.type == LexToken.Type.DERIV) {
            var = raiz.token.value == null ? "x" : raiz.token.value.trim();
            if (!raiz.hijos.isEmpty()) cuerpo = raiz.hijos.get(0);
        } else if (raiz.token.type == LexToken.Type.PRIME) {
            var = "x";
            if (!raiz.hijos.isEmpty()) cuerpo = raiz.hijos.get(0);
        }
        if (cuerpo == null) return;
        Set<String> vars = recolectarVariables(cuerpo);
        if (!vars.isEmpty() && !vars.contains(var)) {
            rs.errores.add(SemanticoError.error("DERIV_VAR", "La variable del diferencial no coincide con la función", ruta(raiz)));
        }
    }

    private static void validarIntegrales(ResultadoSemantico rs, NodoAST n, boolean definida) {
        if (n == null) return;
        if (n.token.type == LexToken.Type.INTEGRAL_INDEF && !definida) {
            if (n.hijos.size() < 2) rs.errores.add(SemanticoError.error("INT_INDEF_INCOMPLETA", "Integral indefinida incompleta", ruta(n)));
            else {
                NodoAST cuerpo = n.hijos.get(0);
                NodoAST dif = n.hijos.get(1);
                if (dif == null || dif.token == null || dif.token.type != LexToken.Type.DIFFERENTIAL) {
                    rs.errores.add(SemanticoError.error("INT_DX_FALTA", "Falta el diferencial", ruta(n)));
                } else {
                    String v = extraerDxVar(dif.token.value);
                    Set<String> vs = recolectarVariables(cuerpo);
                    if (!vs.isEmpty() && !vs.contains(v)) rs.errores.add(SemanticoError.error("INT_VAR_INCONSISTENTE", "Variable de integración inconsistente", ruta(n)));
                }
            }
        }
        if (n.token.type == LexToken.Type.INTEGRAL_DEF && definida) {
            if (n.hijos.size() < 4) rs.errores.add(SemanticoError.error("INT_DEF_INCOMPLETA", "Integral definida incompleta", ruta(n)));
            else {
                NodoAST sup = n.hijos.get(0);
                NodoAST inf = n.hijos.get(1);
                NodoAST cuerpo = n.hijos.get(2);
                NodoAST dif = n.hijos.get(3);
                if (dif == null || dif.token == null || dif.token.type != LexToken.Type.DIFFERENTIAL) {
                    rs.errores.add(SemanticoError.error("INT_DX_FALTA", "Falta el diferencial", ruta(n)));
                } else {
                    String v = extraerDxVar(dif.token.value);
                    Set<String> vs = recolectarVariables(cuerpo);
                    if (!vs.isEmpty() && !vs.contains(v)) rs.errores.add(SemanticoError.error("INT_VAR_INCONSISTENTE", "Variable de integración inconsistente", ruta(n)));
                }
                Double a = evaluarConstante(inf);
                Double b = evaluarConstante(sup);
                if (a == null || b == null) rs.errores.add(SemanticoError.error("INT_LIMS", "Límites no constantes", ruta(n)));
                else if (Math.abs(a - b) < 1e-15) rs.errores.add(SemanticoError.error("INT_LIMS_IGUALES", "Límites iguales", ruta(n)));
            }
        }
        for (NodoAST h : n.hijos) validarIntegrales(rs, h, definida);
    }

    private static void validarSistema(ResultadoSemantico rs, NodoAST raiz) {
        List<NodoAST> ecuaciones = extraerEcuaciones(raiz);
        if (ecuaciones.size() < 2) {
            rs.errores.add(SemanticoError.error("SYS_POCAS_EC", "Se requieren al menos 2 ecuaciones", ruta(raiz)));
            return;
        }
        if (ecuaciones.size() > 3) {
            rs.errores.add(SemanticoError.error("SYS_MUCHAS_EC", "Máximo 3 ecuaciones para Cramer", ruta(raiz)));
            return;
        }
        List<Set<String>> sets = new ArrayList<>();
        for (NodoAST eq : ecuaciones) {
            if (eq.token == null || eq.token.type != LexToken.Type.EQUAL || eq.hijos.size() != 2) {
                rs.errores.add(SemanticoError.error("SYS_NO_EQ", "Renglón sin ecuación válida", ruta(eq)));
                continue;
            }
            Set<String> v = new LinkedHashSet<>();
            recolectarVariablesRec(eq.hijos.get(0), v);
            recolectarVariablesRec(eq.hijos.get(1), v);
            sets.add(v);
        }
        if (sets.isEmpty()) return;
        Set<String> base = new LinkedHashSet<>(sets.get(0));
        for (Set<String> s : sets) {
            if (!s.equals(base)) {
                rs.errores.add(SemanticoError.error("SYS_VARS", "Todas las ecuaciones deben usar exactamente las mismas incógnitas", ruta(raiz)));
                break;
            }
        }
        if (!rs.errores.isEmpty()) return;
        int m = ecuaciones.size();
        int n = base.size();
        if (n == 0) {
            rs.errores.add(SemanticoError.error("SYS_SIN_INC", "No hay incógnitas en el sistema", ruta(raiz)));
            return;
        }
        if (n != m) {
            rs.errores.add(SemanticoError.error("SYS_DIM", "Para Cramer se requieren n ecuaciones y n incógnitas (n = 2 o 3)", ruta(raiz)));
            return;
        }
        if (!(n == 2 || n == 3)) {
            rs.errores.add(SemanticoError.error("SYS_DIM_RANGE", "Cramer solo soporta 2×2 o 3×3", ruta(raiz)));
            return;
        }
        for (NodoAST eq : ecuaciones) {
            NodoAST L = eq.hijos.get(0);
            NodoAST R = eq.hijos.get(1);
            if (contieneAlguno(eq, tiposFunciones())) {
                rs.errores.add(SemanticoError.error("SYS_FUNC", "No se permiten funciones (log, trig, raíces, etc.) en Cramer", ruta(eq)));
                continue;
            }
            if (!expresionLinealEnVars(L, base) || !expresionLinealEnVars(R, base)) {
                rs.errores.add(SemanticoError.error("SYS_LINEAR", "Las ecuaciones deben ser lineales (sin productos de variables, sin variables en denominadores, exponentes ≠ 1)", ruta(eq)));
                continue;
            }
            Set<String> varsEq = new LinkedHashSet<>();
            recolectarVariablesRec(L, varsEq);
            recolectarVariablesRec(R, varsEq);
            if (!varsEq.containsAll(base)) {
                rs.errores.add(SemanticoError.error("SYS_PRESENCIA", "En cada ecuación deben aparecer todas las incógnitas del sistema", ruta(eq)));
            }
        }
    }

    private static boolean expresionLinealEnVars(NodoAST n, Set<String> vars) {
        if (n == null || n.token == null) return true;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL || t == LexToken.Type.CONST_E || t == LexToken.Type.CONST_PI) return true;
        if (t == LexToken.Type.VARIABLE) return vars.contains(n.token.value);
        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB) {
            for (NodoAST h : n.hijos) if (!expresionLinealEnVars(h, vars)) return false;
            return true;
        }
        if (t == LexToken.Type.MUL) {
            Set<String> vL = varsEnSubarbol(n.hijos.get(0));
            Set<String> vR = varsEnSubarbol(n.hijos.get(1));
            boolean leftConst  = vL.isEmpty();
            boolean rightConst = vR.isEmpty();
            if (!leftConst && !rightConst) return false;
            return expresionLinealEnVars(n.hijos.get(0), vars) && expresionLinealEnVars(n.hijos.get(1), vars);
        }
        if (t == LexToken.Type.DIV) {
            if (!varsEnSubarbol(n.hijos.get(1)).isEmpty()) return false;
            return expresionLinealEnVars(n.hijos.get(0), vars) && expresionLinealEnVars(n.hijos.get(1), vars);
        }
        if (t == LexToken.Type.EXP) {
            if (n.hijos.size() != 2) return false;
            Set<String> vBase = varsEnSubarbol(n.hijos.get(0));
            Double e = evaluarConstante(n.hijos.get(1));
            if (vBase.isEmpty()) return e != null;
            return e != null && Math.abs(e - 1.0) < 1e-9 && expresionLinealEnVars(n.hijos.get(0), vars);
        }
        if (t == LexToken.Type.RADICAL || t == LexToken.Type.FACTORIAL || t == LexToken.Type.LOG || t == LexToken.Type.LN || t == LexToken.Type.LOG_BASE2 || t == LexToken.Type.LOG_BASE10) {
            return false;
        }
        for (NodoAST h : n.hijos) if (!expresionLinealEnVars(h, vars)) return false;
        return true;
    }

    private static Set<String> varsEnSubarbol(NodoAST n) {
        Set<String> s = new LinkedHashSet<>();
        recolectarVariablesRec(n, s);
        return s;
    }

    private static boolean esLinealUnivariable(NodoAST eq) {
        if (eq == null || eq.token == null || eq.token.type != LexToken.Type.EQUAL || eq.hijos.size() != 2) return false;
        Set<String> v = new LinkedHashSet<>();
        recolectarVariablesRec(eq.hijos.get(0), v);
        recolectarVariablesRec(eq.hijos.get(1), v);
        if (v.size() != 1) return false;
        String var = v.iterator().next();
        NodoAST diff = resta(eq.hijos.get(0), eq.hijos.get(1));
        int g = gradoEn(diff, var);
        if (g < 0) return false;
        return g <= 1;
    }

    private static boolean esPolinomicaUnivariableGradoMayorIgual2Enteros(NodoAST eq) {
        if (eq == null || eq.token == null || eq.token.type != LexToken.Type.EQUAL || eq.hijos.size() != 2) return false;
        Set<String> v = new LinkedHashSet<>();
        recolectarVariablesRec(eq.hijos.get(0), v);
        recolectarVariablesRec(eq.hijos.get(1), v);
        if (v.size() != 1) return false;
        String var = v.iterator().next();
        NodoAST diff = resta(eq.hijos.get(0), eq.hijos.get(1));
        int g = gradoEn(diff, var);
        if (g < 2) return false;
        return coeficientesEnteros(diff, var);
    }

    private static int contarEcuaciones(NodoAST n) {
        if (n == null) return 0;
        int c = (n.token != null && n.token.type == LexToken.Type.EQUAL) ? 1 : 0;
        for (NodoAST h : n.hijos) c += contarEcuaciones(h);
        return c;
    }

    private static boolean contiene(NodoAST n, LexToken.Type t) {
        if (n == null) return false;
        if (n.token != null && n.token.type == t) return true;
        for (NodoAST h : n.hijos) if (contiene(h, t)) return true;
        return false;
    }

    private static boolean contieneAlguno(NodoAST n, Set<LexToken.Type> set) {
        if (n == null) return false;
        if (n.token != null && set.contains(n.token.type)) return true;
        for (NodoAST h : n.hijos) if (contieneAlguno(h, set)) return true;
        return false;
    }

    private static Set<LexToken.Type> tiposFunciones() {
        Set<LexToken.Type> s = new LinkedHashSet<>();
        s.add(LexToken.Type.LOG);
        s.add(LexToken.Type.LN);
        s.add(LexToken.Type.LOG_BASE2);
        s.add(LexToken.Type.LOG_BASE10);
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
        s.add(LexToken.Type.RADICAL);
        s.add(LexToken.Type.ABS);
        return s;
    }

    private static String extraerDxVar(String dx) {
        if (dx == null) return "";
        String s = dx.trim();
        if (s.equals("dx")) return "x";
        if (s.equals("dy")) return "y";
        if (s.length() >= 2 && s.charAt(0) == 'd') return s.substring(1);
        return "";
    }

    private static Set<String> recolectarVariables(NodoAST n) {
        Set<String> vars = new LinkedHashSet<>();
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
            if (t == LexToken.Type.SUM) {
                double a = 0.0;
                for (NodoAST h : n.hijos) {
                    Double v = evaluarConstante(h);
                    if (v == null) return null;
                    a += v;
                }
                return a;
            }
            if (t == LexToken.Type.MUL) {
                double a = 1.0;
                for (NodoAST h : n.hijos) {
                    Double v = evaluarConstante(h);
                    if (v == null) return null;
                    a *= v;
                }
                return a;
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
            if (v == null || v < 0) return null;
            return Math.sqrt(v);
        }
        if (t == LexToken.Type.FACTORIAL) {
            if (n.hijos.size() != 1) return null;
            Double v = evaluarConstante(n.hijos.get(0));
            if (v == null) return null;
            if (v < 0 || Math.abs(v - Math.rint(v)) > 1e-9) return null;
            long k = Math.round(v);
            double acc = 1.0;
            for (long i = 2; i <= k; i++) acc *= i;
            return acc;
        }
        if (t == LexToken.Type.LOG || t == LexToken.Type.LN || t == LexToken.Type.LOG_BASE2 || t == LexToken.Type.LOG_BASE10) {
            if (n.hijos.size() != 1) return null;
            Double a = evaluarConstante(n.hijos.get(0));
            if (a == null || a <= 0) return null;
            if (t == LexToken.Type.LN) return Math.log(a);
            if (t == LexToken.Type.LOG) return Math.log10(a);
            if (t == LexToken.Type.LOG_BASE2) return Math.log(a) / Math.log(2.0);
            if (t == LexToken.Type.LOG_BASE10) return Math.log10(a);
        }
        return null;
    }

    private static Double toDouble(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return null; }
    }

    private static boolean coeficientesEnteros(NodoAST n, String var) {
        if (n == null || n.token == null) return true;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.INTEGER) return true;
        if (t == LexToken.Type.DECIMAL) {
            Double v = toDouble(n.token.value);
            return v != null && Math.abs(v - Math.rint(v)) < 1e-9;
        }
        if (t == LexToken.Type.CONST_PI || t == LexToken.Type.CONST_E) return false;
        if (t == LexToken.Type.VARIABLE) return true;
        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB || t == LexToken.Type.MUL) {
            for (NodoAST h : n.hijos) if (!coeficientesEnteros(h, var)) return false;
            return true;
        }
        if (t == LexToken.Type.EXP) {
            if (n.hijos.size() != 2) return false;
            Double e = evaluarConstante(n.hijos.get(1));
            if (e == null || e < 0 || Math.abs(e - Math.rint(e)) > 1e-9) return false;
            return coeficientesEnteros(n.hijos.get(0), var);
        }
        if (t == LexToken.Type.DIV) return false;
        return false;
    }

    private static int gradoEn(NodoAST n, String var) {
        if (n == null || n.token == null) return 0;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL || t == LexToken.Type.CONST_E || t == LexToken.Type.CONST_PI) return 0;
        if (t == LexToken.Type.VARIABLE) return var.equals(n.token.value) ? 1 : 0;
        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB) {
            int g = 0;
            for (NodoAST h : n.hijos) {
                int gh = gradoEn(h, var);
                if (gh < 0) return -1;
                if (gh > g) g = gh;
            }
            return g;
        }
        if (t == LexToken.Type.MUL) {
            int g = 0;
            for (NodoAST h : n.hijos) {
                int gh = gradoEn(h, var);
                if (gh < 0) return -1;
                g += gh;
            }
            return g;
        }
        if (t == LexToken.Type.EXP) {
            if (n.hijos.size() != 2) return -1;
            int gb = gradoEn(n.hijos.get(0), var);
            if (gb < 0) return -1;
            Double e = evaluarConstante(n.hijos.get(1));
            if (e == null || e < 0 || Math.abs(e - Math.rint(e)) > 1e-9) return -1;
            return (int) Math.round(gb * e);
        }
        if (t == LexToken.Type.DIV) return -1;
        return -1;
    }

    private static NodoAST resta(NodoAST a, NodoAST b) {
        NodoAST n = new NodoAST(new com.example.a22100213_proyectointegrador_logarismos.LexToken(LexToken.Type.SUB, "-", 5));
        n.addHijo(a);
        n.addHijo(b);
        return n;
    }

    private static List<NodoAST> extraerEcuaciones(NodoAST n) {
        List<NodoAST> r = new ArrayList<>();
        if (n == null) return r;
        if (n.token != null && n.token.type == LexToken.Type.EQUAL) r.add(n);
        for (NodoAST h : n.hijos) r.addAll(extraerEcuaciones(h));
        return r;
    }

    private static List<Integer> ruta(NodoAST n) {
        return new ArrayList<>();
    }

    private static void llenarSubtipos(ResultadoSemantico rs, NodoAST raiz) {
        if (raiz == null) return;
        if (contieneAlguno(raiz, trigSet())) rs.subtipos.add(TipoExpresion.ST_TRIGONOMETRICA);
        if (contiene(raiz, LexToken.Type.RADICAL)) rs.subtipos.add(TipoExpresion.ST_RADICAL);
        if (contiene(raiz, LexToken.Type.LOG) || contiene(raiz, LexToken.Type.LN) || contiene(raiz, LexToken.Type.LOG_BASE2) || contiene(raiz, LexToken.Type.LOG_BASE10) || contiene(raiz, LexToken.Type.EXP)) rs.subtipos.add(TipoExpresion.ST_EXPONENCIAL_LOG);
        if (esRacional(raiz)) rs.subtipos.add(TipoExpresion.ST_RACIONAL);
        if (esPolinomica(raiz)) rs.subtipos.add(TipoExpresion.ST_POLINOMICA);
        if (esSimple(raiz)) rs.subtipos.add(TipoExpresion.ST_SIMPLE);
    }

    private static Set<LexToken.Type> trigSet() {
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

    private static boolean esRacional(NodoAST n) {
        if (n == null) return false;
        if (n.token != null && n.token.type == LexToken.Type.DIV) return true;
        for (NodoAST h : n.hijos) if (esRacional(h)) return true;
        return false;
    }

    private static boolean esPolinomica(NodoAST n) {
        if (n == null || n.token == null) return true;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL || t == LexToken.Type.VARIABLE) return true;
        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB || t == LexToken.Type.MUL) {
            for (NodoAST h : n.hijos) if (!esPolinomica(h)) return false;
            return true;
        }
        if (t == LexToken.Type.EXP) {
            if (n.hijos.size() != 2) return false;
            if (!esPolinomica(n.hijos.get(0))) return false;
            Double e = evaluarConstante(n.hijos.get(1));
            if (e == null || e < 0 || Math.abs(e - Math.rint(e)) > 1e-9) return false;
            return true;
        }
        if (t == LexToken.Type.CONST_E || t == LexToken.Type.CONST_PI || t == LexToken.Type.DIV) return false;
        if (t == LexToken.Type.LOG || t == LexToken.Type.LN || t == LexToken.Type.LOG_BASE2 || t == LexToken.Type.LOG_BASE10) return false;
        if (t == LexToken.Type.RADICAL || t == LexToken.Type.FACTORIAL || t == LexToken.Type.DERIV || t == LexToken.Type.INTEGRAL_DEF || t == LexToken.Type.INTEGRAL_INDEF || t == LexToken.Type.ABS || t == LexToken.Type.IMAGINARY) return false;
        for (NodoAST h : n.hijos) if (!esPolinomica(h)) return false;
        return true;
    }

    private static boolean esSimple(NodoAST n) {
        if (n == null || n.token == null) return false;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL || t == LexToken.Type.CONST_E || t == LexToken.Type.CONST_PI || t == LexToken.Type.VARIABLE) return n.hijos.isEmpty();
        return false;
    }
}
