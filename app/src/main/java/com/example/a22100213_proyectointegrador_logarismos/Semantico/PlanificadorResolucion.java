package com.example.a22100213_proyectointegrador_logarismos.Semantico;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PlanificadorResolucion {

    public static String plan(NodoAST raiz, ResultadoSemantico rs) {
        MetodoResolucion m = metodo(raiz, rs);
        switch (m) {
            case EXPRESION_SIN_OBJETIVO: return "Expresión sin objetivo (nada que resolver)";
            case EVALUACION_DIRECTA: return "Evaluación directa";
            case SUSTITUCION_SIMBOLICA_EVALUACION: return "Sustitución simbólica y evaluación";
            case ALGEBRA_COMPLEJOS: return "Álgebra de complejos (a+bi)";
            case IDENTIDADES_TRIG: return "Identidades y simplificación";
            case NEWTON_RAPHSON: return "Newton-Raphson";
            case POTENCIAS: return "Reglas de potencia";
            case ECUACION_LINEAL: return "Aislamiento en ecuación lineal";
            case ECUACION_CUADRATICA: return "Fórmula general";
            case POLI_RUFFINI: return "Ruffini";
            case ECUACION_POLINOMICA: return "Polinómica general";
            case ECUACION_LOGARITMICA: return "Ecuación logarítmica";
            case ECUACION_TRIGONOMETRICA: return "Ecuación trigonométrica";
            case DERIVADA_REGLA_EXPONENTE: return "Derivada regla de potencia";
            case DERIVADA_CONSTANTE_CERO: return "Derivada de constante";
            case DERIVADA_CONST_POR_FUNCION: return "Constante por función";
            case DERIVADA_REGLA_SUMA: return "Regla de la suma";
            case DERIVADA_PRODUCTO: return "Regla del producto";
            case DERIVADA_COCIENTE: return "Regla del cociente";
            case DERIVADA_CADENA: return "Regla de la cadena";
            case DERIVADA_TRIG: return "Derivadas trigonométricas";
            case INTEGRAL_REGLA_POTENCIA: return "Integral regla de potencia";
            case INTEGRAL_SUSTITUCION: return "Sustitución de variable";
            case INTEGRAL_POR_PARTES: return "Integración por partes";
            case INTEGRAL_TRIGONOMETRICA: return "Sustitución trigonométrica";
            case INTEGRAL_RACIONAL_PFD: return "Fracciones parciales";
            case INTEGRAL_NUMERICA_SIMPSON_O_TRAPECIO: return "Integración numérica";
            case DESPEJE_AISLACION_DIRECTA: return "Aislación directa";
            case DESPEJE_BALANCE_INVERSAS: return "Balance de operaciones";
            case DESPEJE_SIMBOLICO_O_NUMERICO: return "Aislación simbólica/numérica";
            case SISTEMA_CRAMER: return "Cramer";
            case NINGUNO:
            default:
                return "Sin método asignado";
        }
    }

    public static MetodoResolucion metodo(NodoAST raiz, ResultadoSemantico rs) {
        NodoAST eq = buscarNodo(raiz, LexToken.Type.EQUAL);
        if (eq != null && eq.hijos.size() == 2) {
            String v = unicaVariable(eq);
            if (v != null) {
                NodoAST d = resta(eq.hijos.get(0), eq.hijos.get(1));
                int g = gradoEn(d, v);
                if (g >= 0 && g <= 1) {
                    return metodoDespejeLineal(eq);
                } else if (g >= 2) {
                    return metodoDespejePoli(eq);
                }
            }
            return MetodoResolucion.DESPEJE_SIMBOLICO_O_NUMERICO;
        }

        if (raiz == null || rs == null) return MetodoResolucion.NINGUNO;
        if (rs.errores != null && !rs.errores.isEmpty()) return MetodoResolucion.NINGUNO;

        if (esSoloExpresionSinObjetivo(raiz)) {
            return MetodoResolucion.EXPRESION_SIN_OBJETIVO;
        }

        switch (rs.tipoPrincipal) {
            case T8_SISTEMA_EC: return metodoSistema(raiz);
            case T3_DERIVADA: return metodoDerivada(raiz);
            case T4_INTEGRAL_INDEFINIDA: return metodoIntegral(raiz, false);
            case T5_INTEGRAL_DEFINIDA: {
                MetodoResolucion mi = metodoIntegral(raiz, true);
                return mi == MetodoResolucion.NINGUNO ? MetodoResolucion.INTEGRAL_NUMERICA_SIMPSON_O_TRAPECIO : mi;
            }
            case T1_ARITMETICA: return metodoAritmetica(raiz);
            case T2_ALGEBRA_FUNC: return metodoAlgebra(raiz);
            case T6_DESPEJE_LINEAL: return metodoDespejeLineal(raiz);
            case T7_DESPEJE_POLINOMICO: return metodoDespejePoli(raiz);
            case T9_IMAGINARIOS: return MetodoResolucion.ALGEBRA_COMPLEJOS;
            default: return MetodoResolucion.NINGUNO;
        }
    }

    private static boolean esSoloExpresionSinObjetivo(NodoAST n) {
        if (n == null) return false;
        boolean hayIgual = (buscarNodo(n, LexToken.Type.EQUAL) != null);
        boolean hayDeriv = (buscarNodo(n, LexToken.Type.DERIV) != null) || (buscarNodo(n, LexToken.Type.PRIME) != null);
        boolean hayIntegral = (buscarNodo(n, LexToken.Type.INTEGRAL_INDEF) != null) || (buscarNodo(n, LexToken.Type.INTEGRAL_DEF) != null);
        boolean haySistema = false; // si tienes tokens de sistema, puedes checar aquí
        boolean hayObjetivo = hayIgual || hayDeriv || hayIntegral || haySistema;

        // “Sin objetivo” sólo aplica si hay variables en la expresión; si es numérica, sí es evaluable.
        return !hayObjetivo && contieneVariable(n);
    }

    private static MetodoResolucion metodoSistema(NodoAST raiz) {
        List<NodoAST> eqs = extraerEcuaciones(raiz);
        Set<String> vars = recolectarVariablesSistema(raiz);
        int m = eqs.size();
        int n = vars.size();
        if (m == n && (n == 2 || n == 3) && esSistemaLineal(raiz, vars)) {
            return MetodoResolucion.SISTEMA_CRAMER;
        }
        return MetodoResolucion.NINGUNO;
    }

    private static MetodoResolucion metodoDerivada(NodoAST raiz) {
        NodoAST f = buscarArgumentoUnico(raiz, LexToken.Type.DERIV);
        if (f == null) return MetodoResolucion.NINGUNO;
        if (esConstantePura(f)) return MetodoResolucion.DERIVADA_CONSTANTE_CERO;
        if (esMonomioAxn(f)) return MetodoResolucion.DERIVADA_REGLA_EXPONENTE;
        if (esConstantePorFuncion(f)) return MetodoResolucion.DERIVADA_CONST_POR_FUNCION;
        if (tieneOperacion(f, LexToken.Type.SUM) || tieneOperacion(f, LexToken.Type.SUB)) return MetodoResolucion.DERIVADA_REGLA_SUMA;
        if (tieneOperacion(f, LexToken.Type.MUL)) return MetodoResolucion.DERIVADA_PRODUCTO;
        if (tieneOperacion(f, LexToken.Type.DIV)) return MetodoResolucion.DERIVADA_COCIENTE;
        if (esFuncionCompuesta(f)) return MetodoResolucion.DERIVADA_CADENA;
        if (contieneTrig(f)) return MetodoResolucion.DERIVADA_TRIG;
        return MetodoResolucion.NINGUNO;
    }

    private static MetodoResolucion metodoIntegral(NodoAST raiz, boolean definida) {
        NodoAST n = buscarNodo(raiz, definida ? LexToken.Type.INTEGRAL_DEF : LexToken.Type.INTEGRAL_INDEF);
        if (n == null) return MetodoResolucion.NINGUNO;
        String var = varIntegracion(n, definida);
        NodoAST cuerpo = extraerCuerpoIntegral(n, definida);
        if (cuerpo == null) return MetodoResolucion.NINGUNO;

        if (esMonomioAxnRespecto(cuerpo, var) && !esExponenteMenosUno(cuerpo, var))
            return MetodoResolucion.INTEGRAL_REGLA_POTENCIA;

        if (esProductoDeDosFactores(cuerpo) && prioridadLIATE(cuerpo) != 0)
            return MetodoResolucion.INTEGRAL_POR_PARTES;

        if (contieneTrig(cuerpo) && contieneRadical(cuerpo) && esPatronTrigSub(cuerpo, var))
            return MetodoResolucion.INTEGRAL_TRIGONOMETRICA;

        if (esFraccionRacionalPolinomialProper(cuerpo, var))
            return MetodoResolucion.INTEGRAL_RACIONAL_PFD;

        if (existeSustitucionSimple(cuerpo))
            return MetodoResolucion.INTEGRAL_SUSTITUCION;

        return MetodoResolucion.NINGUNO;
    }

    private static MetodoResolucion metodoAritmetica(NodoAST raiz) {
        if (contieneImaginarios(raiz)) return MetodoResolucion.ALGEBRA_COMPLEJOS;
        if (contienePiOE(raiz)) return MetodoResolucion.SUSTITUCION_SIMBOLICA_EVALUACION;
        return MetodoResolucion.EVALUACION_DIRECTA;
    }

    private static MetodoResolucion metodoAlgebra(NodoAST raiz) {
        // Nota: el caso “solo expresión” ya se intercepta arriba con esSoloExpresionSinObjetivo(...)
        if (contieneTrig(raiz) || contieneLog(raiz)) return MetodoResolucion.IDENTIDADES_TRIG;
        if (tieneExponenteFracONeg(raiz)) return MetodoResolucion.POTENCIAS;
        return MetodoResolucion.NEWTON_RAPHSON;
    }

    private static MetodoResolucion metodoDespejeLineal(NodoAST eq) {
        if (eq == null || eq.token == null || eq.token.type != LexToken.Type.EQUAL || eq.hijos.size() != 2)
            return MetodoResolucion.DESPEJE_SIMBOLICO_O_NUMERICO;

        if (esVariableIgualConstante(eq)) return MetodoResolucion.DESPEJE_AISLACION_DIRECTA;
        if (formaAxMasB(eq))             return MetodoResolucion.DESPEJE_BALANCE_INVERSAS;
        return MetodoResolucion.DESPEJE_SIMBOLICO_O_NUMERICO;
    }

    private static boolean esVariableIgualConstante(NodoAST eq) {
        if (eq == null || eq.token == null || eq.token.type != LexToken.Type.EQUAL || eq.hijos.size() != 2) return false;
        NodoAST L = eq.hijos.get(0), R = eq.hijos.get(1);
        boolean Lvar = (L != null && L.token != null && L.token.type == LexToken.Type.VARIABLE && L.hijos.isEmpty());
        boolean Rvar = (R != null && R.token != null && R.token.type == LexToken.Type.VARIABLE && R.hijos.isEmpty());
        Double Lc = evaluarConstante(L);
        Double Rc = evaluarConstante(R);
        return (Lvar && Rc != null) || (Rvar && Lc != null);
    }

    private static MetodoResolucion metodoDespejePoli(NodoAST raiz) {
        String var = unicaVariable(raiz);
        if (var == null) return MetodoResolucion.NEWTON_RAPHSON;
        int g = gradoPolinomioEn(raiz, var);
        if (g == 2 && esFormaCuadraticaIgualCero(raiz, var)) return MetodoResolucion.ECUACION_CUADRATICA;
        if (g == 4 && hayDivisionPorBinomioLineal(raiz, var)) return MetodoResolucion.POLI_RUFFINI;
        return MetodoResolucion.NEWTON_RAPHSON;
    }

    // ---------------- helpers y utilidades (igual que tenías) ----------------

    private static boolean esSistemaLineal(NodoAST raiz, Set<String> vars) {
        List<NodoAST> eqs = extraerEcuaciones(raiz);
        for (NodoAST eq : eqs) {
            if (eq == null || eq.token == null || eq.token.type != LexToken.Type.EQUAL || eq.hijos.size() != 2) return false;
            NodoAST L = eq.hijos.get(0), R = eq.hijos.get(1);
            if (!expresionLinealEnVars(L, vars) || !expresionLinealEnVars(R, vars)) return false;
        }
        return true;
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
            boolean leftConst = vL.isEmpty();
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
            if (vBase.isEmpty()) return e != null; // constante^constante
            return e != null && Math.abs(e - 1.0) < 1e-9 && expresionLinealEnVars(n.hijos.get(0), vars);
        }

        if (t == LexToken.Type.RADICAL || t == LexToken.Type.FACTORIAL ||
                t == LexToken.Type.LOG || t == LexToken.Type.LN ||
                t == LexToken.Type.LOG_BASE2 || t == LexToken.Type.LOG_BASE10) {
            return false;
        }

        for (NodoAST h : n.hijos) if (!expresionLinealEnVars(h, vars)) return false;
        return true;
    }

    private static boolean esConstantePura(NodoAST n) {
        if (n == null) return false;
        if (n.token == null) return true;
        LexToken.Type t = n.token.type;

        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL || t == LexToken.Type.CONST_E || t == LexToken.Type.CONST_PI) {
            for (NodoAST h : n.hijos) if (!esConstantePura(h)) return false;
            return true;
        }

        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB || t == LexToken.Type.MUL || t == LexToken.Type.DIV || t == LexToken.Type.EXP) {
            for (NodoAST h : n.hijos) if (!esConstantePura(h)) return false;
            return true;
        }
        return false;
    }

    private static boolean esMonomioAxn(NodoAST n) {
        if (n == null) return false;
        if (n.token != null && n.token.type == LexToken.Type.MUL && n.hijos.size() == 2) {
            NodoAST a = n.hijos.get(0), b = n.hijos.get(1);
            if (esConstantePura(a) && esXn(b)) return true;
            if (esConstantePura(b) && esXn(a)) return true;
        }
        return esXn(n);
    }

    private static boolean esXn(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.VARIABLE) return true;
        return n.token.type == LexToken.Type.EXP &&
                n.hijos.size() == 2 &&
                n.hijos.get(0) != null &&
                n.hijos.get(0).token != null &&
                n.hijos.get(0).token.type == LexToken.Type.VARIABLE &&
                evaluarConstante(n.hijos.get(1)) != null;
    }

    private static boolean esConstantePorFuncion(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.MUL && n.hijos.size() == 2) {
            return esConstantePura(n.hijos.get(0)) ^ esConstantePura(n.hijos.get(1));
        }
        return false;
    }

    private static boolean esFuncionCompuesta(NodoAST n) {
        if (n == null) return false;
        if (n.token != null) {
            LexToken.Type t = n.token.type;
            if (t == LexToken.Type.TRIG_SIN || t == LexToken.Type.TRIG_COS || t == LexToken.Type.TRIG_TAN ||
                    t == LexToken.Type.TRIG_COT || t == LexToken.Type.TRIG_SEC || t == LexToken.Type.TRIG_CSC ||
                    t == LexToken.Type.LOG || t == LexToken.Type.LN) return true;
        }
        for (NodoAST h : n.hijos) if (esFuncionCompuesta(h)) return true;
        return false;
    }

    private static boolean contieneTrig(NodoAST n) {
        if (n == null || n.token == null) return false;
        switch (n.token.type) {
            case TRIG_SIN: case TRIG_COS: case TRIG_TAN: case TRIG_COT: case TRIG_SEC: case TRIG_CSC:
            case TRIG_ARCSIN: case TRIG_ARCCOS: case TRIG_ARCTAN: case TRIG_ARCCOT: case TRIG_ARCSEC: case TRIG_ARCCSC:
                return true;
            default:
        }
        for (NodoAST h : n.hijos) if (contieneTrig(h)) return true;
        return false;
    }

    private static boolean contieneLog(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.LOG || n.token.type == LexToken.Type.LN ||
                n.token.type == LexToken.Type.LOG_BASE2 || n.token.type == LexToken.Type.LOG_BASE10) return true;
        for (NodoAST h : n.hijos) if (contieneLog(h)) return true;
        return false;
    }

    private static boolean contienePiOE(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.CONST_PI || n.token.type == LexToken.Type.CONST_E) return true;
        for (NodoAST h : n.hijos) if (contienePiOE(h)) return true;
        return false;
    }

    private static boolean contieneImaginarios(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.IMAGINARY) return true;
        for (NodoAST h : n.hijos) if (contieneImaginarios(h)) return true;
        return false;
    }

    private static boolean tieneOperacion(NodoAST n, LexToken.Type t) {
        if (n == null || n.token == null) return false;
        if (n.token.type == t) return true;
        for (NodoAST h : n.hijos) if (tieneOperacion(h, t)) return true;
        return false;
    }

    private static boolean esProductoDeDosFactores(NodoAST n) {
        return n != null && n.token != null && n.token.type == LexToken.Type.MUL && n.hijos.size() == 2;
    }

    private static boolean contieneRadical(NodoAST n) {
        if (n == null) return false;
        if (n.token != null && n.token.type == LexToken.Type.RADICAL) return true;
        for (NodoAST h : n.hijos) if (contieneRadical(h)) return true;
        return false;
    }

    private static boolean esPatronTrigSub(NodoAST n, String var) {
        NodoAST rad = buscarNodo(n, LexToken.Type.RADICAL);
        if (rad == null || rad.hijos.size() != 1) return false;
        NodoAST inside = rad.hijos.get(0);
        if (inside == null || inside.token == null) return false;
        if (inside.token.type != LexToken.Type.SUM && inside.token.type != LexToken.Type.SUB) return false;

        NodoAST a = inside.hijos.size() > 0 ? inside.hijos.get(0) : null;
        NodoAST b = inside.hijos.size() > 1 ? inside.hijos.get(1) : null;
        if (a == null || b == null) return false;

        if (esCuadradoConstante(a) && esCuadradoEnVariable(b, var)) return true;
        if (esCuadradoEnVariable(a, var) && esCuadradoConstante(b)) return true;
        return false;
    }

    private static boolean esCuadradoConstante(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.EXP && n.hijos.size() == 2) {
            Double e = evaluarConstante(n.hijos.get(1));
            if (e == null || Math.abs(e - 2.0) > 1e-9) return false;
            Double base = evaluarConstante(n.hijos.get(0));
            return base != null;
        }
        return false;
    }

    private static boolean esCuadradoEnVariable(NodoAST n, String var) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.EXP && n.hijos.size() == 2) {
            Double e = evaluarConstante(n.hijos.get(1));
            if (e == null || Math.abs(e - 2.0) > 1e-9) return false;
            return contieneVariableExacta(n.hijos.get(0), var);
        }
        return false;
    }

    private static boolean esFraccionRacionalPolinomialProper(NodoAST n, String var) {
        if (n == null || n.token == null || n.token.type != LexToken.Type.DIV || n.hijos.size() != 2) return false;
        GradoPolinomio gn = gradoSiPolinomio(n.hijos.get(0), var);
        GradoPolinomio gd = gradoSiPolinomio(n.hijos.get(1), var);
        return gn.ok && gd.ok && gn.grado < gd.grado;
    }

    private static boolean existeSustitucionSimple(NodoAST n) {
        if (n == null) return false;
        if (n.token != null) {
            LexToken.Type t = n.token.type;
            if ((t == LexToken.Type.EXP || t == LexToken.Type.LOG || t == LexToken.Type.LN) &&
                    !n.hijos.isEmpty() && contieneVariable(n.hijos.get(0))) return true;
        }
        for (NodoAST h : n.hijos) if (existeSustitucionSimple(h)) return true;
        return false;
    }

    private static boolean tieneExponenteFracONeg(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.EXP) {
            Double e = evaluarConstante(n.hijos.get(1));
            if (e != null && (e < 0 || Math.abs(e - Math.rint(e)) > 1e-9)) return true;
        }
        for (NodoAST h : n.hijos) if (tieneExponenteFracONeg(h)) return true;
        return false;
    }

    private static boolean aislableDirecto(NodoAST n) {
        return !tieneOperacion(n, LexToken.Type.MUL) &&
                !tieneOperacion(n, LexToken.Type.DIV) &&
                !tieneOperacion(n, LexToken.Type.EXP) &&
                !tieneOperacion(n, LexToken.Type.LOG) &&
                !tieneOperacion(n, LexToken.Type.LN);
    }

    private static boolean formaAxMasB(NodoAST eq) {
        if (eq == null || eq.token == null || eq.token.type != LexToken.Type.EQUAL || eq.hijos.size() != 2) return false;
        String v = unicaVariable(eq);
        if (v == null) return false;
        NodoAST d = resta(eq.hijos.get(0), eq.hijos.get(1));
        int g = gradoEn(d, v);
        return g >= 0 && g <= 1;
    }

    private static boolean variableRepetida(NodoAST eq) {
        Set<String> s = varsEnSubarbol(eq);
        return s.size() == 1 && contarAparicionesVar(eq, s.iterator().next()) > 1;
    }

    private static boolean variableDentroDeParentesisOperado(NodoAST n) {
        if (n == null) return false;
        if (n.token != null && (n.token.type == LexToken.Type.EXP || n.token.type == LexToken.Type.RADICAL) && contieneVariable(n)) return true;
        for (NodoAST h : n.hijos) if (variableDentroDeParentesisOperado(h)) return true;
        return false;
    }

    private static boolean esFormaCuadraticaIgualCero(NodoAST eq, String var) {
        if (eq == null || eq.token == null || eq.token.type != LexToken.Type.EQUAL || eq.hijos.size() != 2) return false;
        NodoAST d = resta(eq.hijos.get(0), eq.hijos.get(1));
        return gradoEn(d, var) == 2;
    }

    private static boolean hayDivisionPorBinomioLineal(NodoAST n, String var) {
        if (n == null) return false;
        if (n.token != null && n.token.type == LexToken.Type.DIV && n.hijos.size() == 2) {
            NodoAST den = n.hijos.get(1);
            if (esBinomioLineal(den, var)) return true;
        }
        for (NodoAST h : n.hijos) if (hayDivisionPorBinomioLineal(h, var)) return true;
        return false;
    }

    private static boolean esBinomioLineal(NodoAST n, String var) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.SUM || n.token.type == LexToken.Type.SUB) {
            boolean okVar = false, okConst = false;
            for (NodoAST h : n.hijos) {
                if (contieneVariableExacta(h, var) && gradoEn(h, var) == 1) okVar = true;
                Double c = evaluarConstante(h);
                if (c != null) okConst = true;
            }
            return okVar && okConst;
        }
        return n.token.type == LexToken.Type.VARIABLE && var.equals(n.token.value);
    }

    private static boolean esMonomioAxnRespecto(NodoAST n, String var) {
        if (n == null) return false;
        if (n.token != null && n.token.type == LexToken.Type.MUL && n.hijos.size() == 2) {
            NodoAST a = n.hijos.get(0), b = n.hijos.get(1);
            if (esConstantePura(a) && esXnRespecto(b, var)) return true;
            if (esConstantePura(b) && esXnRespecto(a, var)) return true;
        }
        return esXnRespecto(n, var);
    }

    private static boolean esXnRespecto(NodoAST n, String var) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.VARIABLE) return var.equals(n.token.value);
        if (n.token.type == LexToken.Type.EXP && n.hijos.size() == 2) {
            if (n.hijos.get(0) != null && n.hijos.get(0).token != null &&
                    n.hijos.get(0).token.type == LexToken.Type.VARIABLE &&
                    var.equals(n.hijos.get(0).token.value)) {
                Double e = evaluarConstante(n.hijos.get(1));
                return e != null;
            }
        }
        return false;
    }

    private static boolean esExponenteMenosUno(NodoAST n, String var) {
        if (n == null) return false;
        if (n.token != null && n.token.type == LexToken.Type.EXP && n.hijos.size() == 2) {
            if (contieneVariableExacta(n.hijos.get(0), var)) {
                Double e = evaluarConstante(n.hijos.get(1));
                return e != null && Math.abs(e + 1.0) < 1e-9;
            }
        }
        if (n.token != null && n.token.type == LexToken.Type.MUL) {
            for (NodoAST h : n.hijos) if (esExponenteMenosUno(h, var)) return true;
        }
        return false;
    }

    private static int prioridadLIATE(NodoAST prod) {
        if (prod == null || prod.token == null || prod.token.type != LexToken.Type.MUL || prod.hijos.size() != 2) return 0;
        int pA = prioridadTermino(prod.hijos.get(0));
        int pB = prioridadTermino(prod.hijos.get(1));
        return Math.max(pA, pB);
    }

    private static int prioridadTermino(NodoAST t) {
        if (t == null || t.token == null) return 0;
        if (esInversa(t)) return 5;
        if (contieneLog(t)) return 4;
        if (esPolinomioEnVariableCualquiera(t)) return 3;
        if (contieneTrig(t)) return 2;
        if (esExponencial(t)) return 1;
        return 0;
    }

    private static boolean esInversa(NodoAST t) {
        if (t == null) return false;
        if (t.token != null && t.token.type == LexToken.Type.DIV) {
            if (evaluarConstante(t.hijos.get(0)) != null && contieneVariable(t.hijos.get(1))) return true;
        }
        for (NodoAST h : t.hijos) if (esInversa(h)) return true;
        return false;
    }

    private static boolean esPolinomioEnVariableCualquiera(NodoAST n) {
        Set<String> s = varsEnSubarbol(n);
        if (s.size() != 1) return false;
        String v = s.iterator().next();
        return gradoSiPolinomio(n, v).ok;
    }

    private static boolean esExponencial(NodoAST t) {
        if (t == null || t.token == null) return false;
        if (t.token.type == LexToken.Type.EXP) {
            if (evaluarConstante(t.hijos.get(0)) != null && contieneVariable(t.hijos.get(1))) return true;
        }
        for (NodoAST h : t.hijos) if (esExponencial(h)) return true;
        return false;
    }

    private static String varIntegracion(NodoAST integ, boolean definida) {
        if (integ == null) return "x";
        if (!definida) {
            if (integ.hijos.size() >= 2) {
                NodoAST dif = integ.hijos.get(1);
                return extraerDxVar(dif != null && dif.token != null ? dif.token.value : "");
            }
        } else {
            if (integ.hijos.size() >= 4) {
                NodoAST dif = integ.hijos.get(3);
                return extraerDxVar(dif != null && dif.token != null ? dif.token.value : "");
            }
        }
        return "x";
    }

    private static String extraerDxVar(String dx) {
        if (dx == null) return "x";
        String s = dx.trim();
        if (s.equals("dx")) return "x";
        if (s.equals("dy")) return "y";
        if (s.length() >= 2 && s.charAt(0) == 'd') return s.substring(1);
        return "x";
    }

    private static NodoAST buscarArgumentoUnico(NodoAST n, LexToken.Type t) {
        NodoAST x = buscarNodo(n, t);
        if (x == null || x.hijos.isEmpty()) return null;
        return x.hijos.get(0);
    }

    private static NodoAST buscarNodo(NodoAST n, LexToken.Type t) {
        if (n == null) return null;
        if (n.token != null && n.token.type == t) return n;
        for (NodoAST h : n.hijos) {
            NodoAST r = buscarNodo(h, t);
            if (r != null) return r;
        }
        return null;
    }

    private static NodoAST extraerCuerpoIntegral(NodoAST integ, boolean definida) {
        if (integ == null) return null;
        if (!definida) {
            return !integ.hijos.isEmpty() ? integ.hijos.get(0) : null;
        } else {
            return integ.hijos.size() >= 3 ? integ.hijos.get(2) : null;
        }
    }

    private static List<NodoAST> extraerEcuaciones(NodoAST n) {
        List<NodoAST> r = new ArrayList<>();
        if (n == null) return r;
        if (n.token != null && n.token.type == LexToken.Type.EQUAL) r.add(n);
        for (NodoAST h : n.hijos) r.addAll(extraerEcuaciones(h));
        return r;
    }

    private static Set<String> recolectarVariablesSistema(NodoAST n) {
        Set<String> s = new LinkedHashSet<>();
        if (n == null) return s;
        if (n.token != null && n.token.type == LexToken.Type.VARIABLE) s.add(n.token.value);
        for (NodoAST h : n.hijos) s.addAll(recolectarVariablesSistema(h));
        return s;
    }

    public static Set<String> varsEnSubarbol(NodoAST n) {
        Set<String> s = new LinkedHashSet<>();
        if (n == null) return s;
        if (n.token != null && n.token.type == LexToken.Type.VARIABLE) s.add(n.token.value);
        for (NodoAST h : n.hijos) s.addAll(varsEnSubarbol(h));
        return s;
    }

    private static boolean contieneVariable(NodoAST n) {
        if (n == null) return false;
        if (n.token != null && n.token.type == LexToken.Type.VARIABLE) return true;
        for (NodoAST h : n.hijos) if (contieneVariable(h)) return true;
        return false;
    }

    private static boolean contieneVariableExacta(NodoAST n, String var) {
        if (n == null) return false;
        if (n.token != null && n.token.type == LexToken.Type.VARIABLE && var.equals(n.token.value)) return true;
        for (NodoAST h : n.hijos) if (contieneVariableExacta(h, var)) return true;
        return false;
    }

    private static int contarAparicionesVar(NodoAST n, String var) {
        if (n == null) return 0;
        int c = (n.token != null && n.token.type == LexToken.Type.VARIABLE && var.equals(n.token.value)) ? 1 : 0;
        for (NodoAST h : n.hijos) c += contarAparicionesVar(h, var);
        return c;
    }

    private static String unicaVariable(NodoAST n) {
        Set<String> s = varsEnSubarbol(n);
        return s.size() == 1 ? s.iterator().next() : null;
    }

    private static Double evaluarConstante(NodoAST n) {
        if (n == null || n.token == null) return null;
        LexToken.Type t = n.token.type;

        if (t == LexToken.Type.VARIABLE) return null;
        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL) {
            try { return Double.parseDouble(n.token.value); } catch (Exception e) { return null; }
        }
        if (t == LexToken.Type.CONST_PI) return Math.PI;
        if (t == LexToken.Type.CONST_E) return Math.E;

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
            if (a == null || b == null || Math.abs(b) < 1e-15) return null;
            return a / b;
        }
        if (t == LexToken.Type.EXP) {
            if (n.hijos.size() != 2) return null;
            Double a = evaluarConstante(n.hijos.get(0));
            Double b = evaluarConstante(n.hijos.get(1));
            if (a == null || b == null) return null;
            return Math.pow(a, b);
        }

        return null;
    }

    private static class GradoPolinomio {
        boolean ok;
        int grado;
        GradoPolinomio(boolean ok, int grado) { this.ok = ok; this.grado = grado; }
    }

    private static GradoPolinomio gradoSiPolinomio(NodoAST n, String var) {
        if (n == null || n.token == null) return new GradoPolinomio(false, 0);
        LexToken.Type t = n.token.type;

        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL) return new GradoPolinomio(true, 0);
        if (t == LexToken.Type.VARIABLE) return new GradoPolinomio(var.equals(n.token.value), var.equals(n.token.value) ? 1 : 0);

        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB) {
            int g = 0;
            for (NodoAST h : n.hijos) {
                GradoPolinomio gh = gradoSiPolinomio(h, var);
                if (!gh.ok) return new GradoPolinomio(false, 0);
                g = Math.max(g, gh.grado);
            }
            return new GradoPolinomio(true, g);
        }

        if (t == LexToken.Type.MUL) {
            int g = 0;
            for (NodoAST h : n.hijos) {
                GradoPolinomio gh = gradoSiPolinomio(h, var);
                if (!gh.ok) return new GradoPolinomio(false, 0);
                g += gh.grado;
            }
            return new GradoPolinomio(true, g);
        }

        if (t == LexToken.Type.EXP) {
            if (n.hijos.size() != 2) return new GradoPolinomio(false, 0);
            GradoPolinomio gb = gradoSiPolinomio(n.hijos.get(0), var);
            if (!gb.ok) return new GradoPolinomio(false, 0);
            Double e = evaluarConstante(n.hijos.get(1));
            if (e == null || e < 0 || Math.abs(e - Math.rint(e)) > 1e-9) return new GradoPolinomio(false, 0);
            return new GradoPolinomio(true, (int) Math.round(gb.grado * e));
        }

        return new GradoPolinomio(false, 0);
    }

    private static int gradoPolinomioEn(NodoAST n, String var) {
        GradoPolinomio g = gradoSiPolinomio(n, var);
        return g.ok ? g.grado : -1;
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
                g = Math.max(g, gh);
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
        NodoAST n = new NodoAST(new LexToken(LexToken.Type.SUB, "-", 5));
        n.addHijo(a);
        n.addHijo(b);
        return n;
    }

    @SuppressWarnings("unused")
    private static List<Integer> ruta(NodoAST n) {
        return new ArrayList<>();
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
            return e != null && e >= 0 && Math.abs(e - Math.rint(e)) < 1e-9;
        }

        if (t == LexToken.Type.CONST_E || t == LexToken.Type.CONST_PI || t == LexToken.Type.DIV) return false;
        if (t == LexToken.Type.LOG || t == LexToken.Type.LN || t == LexToken.Type.LOG_BASE2 || t == LexToken.Type.LOG_BASE10) return false;
        if (t == LexToken.Type.RADICAL || t == LexToken.Type.FACTORIAL ||
                t == LexToken.Type.DERIV || t == LexToken.Type.INTEGRAL_DEF || t == LexToken.Type.INTEGRAL_INDEF ||
                t == LexToken.Type.ABS || t == LexToken.Type.IMAGINARY) return false;

        for (NodoAST h : n.hijos) if (!esPolinomica(h)) return false;
        return true;
    }

    private static boolean esSimple(NodoAST n) {
        if (n == null || n.token == null) return false;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL || t == LexToken.Type.CONST_E || t == LexToken.Type.CONST_PI || t == LexToken.Type.VARIABLE)
            return n.hijos.isEmpty();
        return false;
    }
}
