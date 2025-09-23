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
            case EVALUACION_DIRECTA: return "Evaluación directa";
            case SUSTITUCION_SIMBOLICA_EVALUACION: return "Sustitución simbólica y evaluación";
            case ALGEBRA_COMPLEJOS: return "Álgebra de complejos (a+bi)";
            case IDENTIDADES_TRIG: return "Evaluación por identidades trigonométricas/logarítmicas";
            case NEWTON_RAPHSON: return "Newton-Raphson";
            case POTENCIAS: return "Reglas de potencia";
            case ECUACION_LINEAL: return "Aislamiento en ecuación lineal";
            case ECUACION_CUADRATICA: return "Fórmula general de segundo grado";
            case POLI_RUFFINI: return "Ruffini / factorización polinómica";
            case ECUACION_POLINOMICA: return "Resolución polinómica general";
            case ECUACION_LOGARITMICA: return "Ecuación logarítmica";
            case ECUACION_TRIGONOMETRICA: return "Ecuación trigonométrica";
            case DERIVADA_REGLA_EXPONENTE: return "Derivada por regla del exponente";
            case DERIVADA_CONSTANTE_CERO: return "Derivada de constante = 0";
            case DERIVADA_CONST_POR_FUNCION: return "Constante por función";
            case DERIVADA_REGLA_SUMA: return "Regla de la suma";
            case DERIVADA_PRODUCTO: return "Regla del producto";
            case DERIVADA_COCIENTE: return "Regla del cociente";
            case DERIVADA_CADENA: return "Regla de la cadena";
            case DERIVADA_TRIG: return "Derivadas trigonométricas";
            case INTEGRAL_REGLA_POTENCIA: return "Integral por regla de potencia";
            case INTEGRAL_SUSTITUCION: return "Sustitución de variable";
            case INTEGRAL_POR_PARTES: return "Integración por partes";
            case INTEGRAL_TRIGONOMETRICA: return "Sustitución trigonométrica";
            case INTEGRAL_RACIONAL_PFD: return "Fracciones parciales";
            case INTEGRAL_NUMERICA_SIMPSON_O_TRAPECIO: return "Integración numérica (Simpson/Trapecio)";
            case DESPEJE_AISLACION_DIRECTA: return "Aislación directa";
            case DESPEJE_BALANCE_INVERSAS: return "Balance y operaciones inversas";
            case DESPEJE_SIMBOLICO_O_NUMERICO: return "Aislación simbólica o numérica";
            case SISTEMA_CRAMER: return "Cramer";
            case NINGUNO: default: return "Sin método asignado";
        }
    }

    public static MetodoResolucion metodo(NodoAST raiz, ResultadoSemantico rs) {
        if (raiz == null || rs == null) return MetodoResolucion.NINGUNO;
        if (rs.errores != null && !rs.errores.isEmpty()) return MetodoResolucion.NINGUNO;
        switch (rs.tipoPrincipal) {
            case T8_SISTEMA_EC:
                return metodoSistema(raiz);
            case T3_DERIVADA:
                return metodoDerivada(raiz);
            case T4_INTEGRAL_INDEFINIDA:
                return metodoIntegral(raiz, false);
            case T5_INTEGRAL_DEFINIDA:
                MetodoResolucion mi = metodoIntegral(raiz, true);
                return mi == MetodoResolucion.NINGUNO ? MetodoResolucion.INTEGRAL_NUMERICA_SIMPSON_O_TRAPECIO : mi;
            case T1_ARITMETICA:
                return metodoAritmetica(raiz);
            case T2_ALGEBRA_FUNC:
                return metodoAlgebra(raiz);
            case T6_DESPEJE_LINEAL:
                return metodoDespejeLineal(raiz);
            case T7_DESPEJE_POLINOMICO:
                return metodoDespejePoli(raiz);
            case T9_IMAGINARIOS:
                return MetodoResolucion.ALGEBRA_COMPLEJOS;
            default:
                return MetodoResolucion.NINGUNO;
        }
    }

    private static MetodoResolucion metodoSistema(NodoAST raiz) {
        List<NodoAST> eqs = extraerEcuaciones(raiz);
        Set<String> vars = recolectarVariablesSistema(raiz);
        int m = eqs.size();
        int n = vars.size();
        if (m == n && (n == 2 || n == 3) && esSistemaLineal(raiz, vars)) return MetodoResolucion.SISTEMA_CRAMER;
        return MetodoResolucion.NINGUNO;
    }

    private static MetodoResolucion metodoDerivada(NodoAST raiz) {
        NodoAST f = buscarArgumentoUnico(raiz, LexToken.Type.DERIV);
        if (f == null) return MetodoResolucion.NINGUNO;
        if (esConstantePura(f)) return MetodoResolucion.DERIVADA_CONSTANTE_CERO;
        if (esMonomioAxn(f)) return MetodoResolucion.DERIVADA_REGLA_EXPONENTE;
        if (tieneOperacion(f, LexToken.Type.SUM) || tieneOperacion(f, LexToken.Type.SUB)) return MetodoResolucion.DERIVADA_REGLA_SUMA;
        if (tieneOperacion(f, LexToken.Type.MUL)) return MetodoResolucion.DERIVADA_PRODUCTO;
        if (tieneOperacion(f, LexToken.Type.DIV)) return MetodoResolucion.DERIVADA_COCIENTE;
        if (contieneTrig(f)) return MetodoResolucion.DERIVADA_TRIG;
        if (esConstantePorFuncion(f)) return MetodoResolucion.DERIVADA_CONST_POR_FUNCION;
        if (esFuncionCompuesta(f)) return MetodoResolucion.DERIVADA_CADENA;
        return MetodoResolucion.NINGUNO;
    }

    private static MetodoResolucion metodoIntegral(NodoAST raiz, boolean definida) {
        NodoAST n = buscarNodo(raiz, definida ? LexToken.Type.INTEGRAL_DEF : LexToken.Type.INTEGRAL_INDEF);
        if (n == null) return MetodoResolucion.NINGUNO;
        NodoAST cuerpo = extraerCuerpoIntegral(n, definida);
        if (cuerpo == null) return MetodoResolucion.NINGUNO;
        if (esMonomioAxn(cuerpo)) return MetodoResolucion.INTEGRAL_REGLA_POTENCIA;
        if (tieneOperacion(cuerpo, LexToken.Type.MUL) && cuentaFunciones(cuerpo) >= 2) return MetodoResolucion.INTEGRAL_POR_PARTES;
        if (contieneTrig(cuerpo)) return MetodoResolucion.INTEGRAL_TRIGONOMETRICA;
        if (esFraccionRacional(cuerpo)) return MetodoResolucion.INTEGRAL_RACIONAL_PFD;
        if (existeSustitucionSimple(cuerpo)) return MetodoResolucion.INTEGRAL_SUSTITUCION;
        return MetodoResolucion.NINGUNO;
    }

    private static int cuentaFunciones(NodoAST n) {
        if (n == null || n.token == null) return 0;
        int count = 0;

        switch (n.token.type) {
            case TRIG_SIN:
            case TRIG_COS:
            case TRIG_TAN:
            case TRIG_COT:
            case TRIG_SEC:
            case TRIG_CSC:
            case TRIG_ARCSIN:
            case TRIG_ARCCOS:
            case TRIG_ARCTAN:
            case TRIG_ARCCOT:
            case TRIG_ARCSEC:
            case TRIG_ARCCSC:
            case LOG:
            case LN:
            case LOG_BASE2:
            case LOG_BASE10:
                count++;
                break;
            default:
                break;
        }
        for (NodoAST h : n.hijos) {
            count += cuentaFunciones(h);
        }
        return count;
    }

    private static MetodoResolucion metodoAritmetica(NodoAST raiz) {
        if (contieneImaginarios(raiz)) return MetodoResolucion.ALGEBRA_COMPLEJOS;
        if (contienePiOE(raiz)) return MetodoResolucion.SUSTITUCION_SIMBOLICA_EVALUACION;
        return MetodoResolucion.EVALUACION_DIRECTA;
    }

    private static MetodoResolucion metodoAlgebra(NodoAST raiz) {
        if (contieneTrig(raiz) || contieneLog(raiz)) return MetodoResolucion.IDENTIDADES_TRIG;
        if (tieneExponenteFracONeg(raiz)) return MetodoResolucion.POTENCIAS;
        return MetodoResolucion.NEWTON_RAPHSON;
    }

    private static MetodoResolucion metodoDespejeLineal(NodoAST raiz) {
        if (aislableDirecto(raiz)) return MetodoResolucion.DESPEJE_AISLACION_DIRECTA;
        if (contieneTrig(raiz) || contieneLog(raiz)) return MetodoResolucion.DESPEJE_SIMBOLICO_O_NUMERICO;
        return MetodoResolucion.DESPEJE_BALANCE_INVERSAS;
    }

    private static MetodoResolucion metodoDespejePoli(NodoAST raiz) {
        int grado = gradoPolinomio(raiz);
        if (grado == 2) return MetodoResolucion.ECUACION_CUADRATICA;
        if (grado > 2) return MetodoResolucion.POLI_RUFFINI;
        return MetodoResolucion.NEWTON_RAPHSON;
    }

    private static boolean esSistemaLineal(NodoAST raiz, Set<String> vars) {
        List<NodoAST> eqs = extraerEcuaciones(raiz);
        for (NodoAST eq : eqs) {
            if (eq.token == null || eq.token.type != LexToken.Type.EQUAL || eq.hijos.size() != 2) return false;
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
            if (vBase.isEmpty()) return e != null;
            return e != null && Math.abs(e - 1.0) < 1e-9 && expresionLinealEnVars(n.hijos.get(0), vars);
        }
        if (t == LexToken.Type.RADICAL || t == LexToken.Type.FACTORIAL || t == LexToken.Type.LOG || t == LexToken.Type.LN || t == LexToken.Type.LOG_BASE2 || t == LexToken.Type.LOG_BASE10) {
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
        if (n.token.type == LexToken.Type.EXP && n.hijos.size() == 2 && n.hijos.get(0).token != null && n.hijos.get(0).token.type == LexToken.Type.VARIABLE && evaluarConstante(n.hijos.get(1)) != null) return true;
        return false;
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
        if (n.token != null && (n.token.type == LexToken.Type.TRIG_SIN || n.token.type == LexToken.Type.TRIG_COS || n.token.type == LexToken.Type.TRIG_TAN || n.token.type == LexToken.Type.TRIG_COT || n.token.type == LexToken.Type.TRIG_SEC || n.token.type == LexToken.Type.TRIG_CSC || n.token.type == LexToken.Type.LOG || n.token.type == LexToken.Type.LN)) return true;
        for (NodoAST h : n.hijos) if (esFuncionCompuesta(h)) return true;
        return false;
    }

    private static boolean contieneTrig(NodoAST n) {
        if (n == null || n.token == null) return false;
        switch (n.token.type) {
            case TRIG_SIN: case TRIG_COS: case TRIG_TAN: case TRIG_COT: case TRIG_SEC: case TRIG_CSC:
            case TRIG_ARCSIN: case TRIG_ARCCOS: case TRIG_ARCTAN: case TRIG_ARCCOT: case TRIG_ARCSEC: case TRIG_ARCCSC:
                return true;
        }
        for (NodoAST h : n.hijos) if (contieneTrig(h)) return true;
        return false;
    }

    private static boolean contieneLog(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.LOG || n.token.type == LexToken.Type.LN || n.token.type == LexToken.Type.LOG_BASE2 || n.token.type == LexToken.Type.LOG_BASE10) return true;
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

    private static boolean esFraccionRacional(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.DIV) {
            boolean numVars = !varsEnSubarbol(n.hijos.get(0)).isEmpty();
            boolean denVars = !varsEnSubarbol(n.hijos.get(1)).isEmpty();
            return numVars || denVars;
        }
        for (NodoAST h : n.hijos) if (esFraccionRacional(h)) return true;
        return false;
    }

    private static boolean existeSustitucionSimple(NodoAST n) {
        if (n == null) return false;
        if (contieneLog(n) || contieneTrig(n)) return true;
        if (n.token != null && n.token.type == LexToken.Type.EXP && n.hijos.size() == 2 && !varsEnSubarbol(n.hijos.get(0)).isEmpty()) return true;
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
        return !tieneOperacion(n, LexToken.Type.MUL) && !tieneOperacion(n, LexToken.Type.DIV) && !tieneOperacion(n, LexToken.Type.EXP) && !tieneOperacion(n, LexToken.Type.LOG) && !tieneOperacion(n, LexToken.Type.LN);
    }

    private static int gradoPolinomio(NodoAST n) {
        if (n == null) return 0;
        if (n.token != null && n.token.type == LexToken.Type.EXP && n.hijos.size() == 2 && n.hijos.get(0).token != null && n.hijos.get(0).token.type == LexToken.Type.VARIABLE) {
            Double e = evaluarConstante(n.hijos.get(1));
            if (e != null) return (int)Math.round(e);
        }
        int g = 0;
        for (NodoAST h : n.hijos) g = Math.max(g, gradoPolinomio(h));
        return g;
    }

    private static NodoAST buscarArgumentoUnico(NodoAST n, LexToken.Type t) {
        NodoAST x = buscarNodo(n, t);
        if (x == null) return null;
        if (x.hijos.isEmpty()) return null;
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
            if (!integ.hijos.isEmpty()) return integ.hijos.get(0);
            return null;
        } else {
            if (integ.hijos.size() >= 3) return integ.hijos.get(2);
            return null;
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

    private static Set<String> varsEnSubarbol(NodoAST n) {
        Set<String> s = new LinkedHashSet<>();
        if (n == null) return s;
        if (n.token != null && n.token.type == LexToken.Type.VARIABLE) s.add(n.token.value);
        for (NodoAST h : n.hijos) s.addAll(varsEnSubarbol(h));
        return s;
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
        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB || t == LexToken.Type.MUL || t == LexToken.Type.DIV || t == LexToken.Type.EXP) {
            if (t == LexToken.Type.SUM) {
                double a = 0.0; for (NodoAST h : n.hijos) { Double v = evaluarConstante(h); if (v == null) return null; a += v; } return a;
            }
            if (t == LexToken.Type.MUL) {
                double a = 1.0; for (NodoAST h : n.hijos) { Double v = evaluarConstante(h); if (v == null) return null; a *= v; } return a;
            }
            if (t == LexToken.Type.SUB) {
                if (n.hijos.size() != 2) return null;
                Double a = evaluarConstante(n.hijos.get(0)); Double b = evaluarConstante(n.hijos.get(1));
                if (a == null || b == null) return null; return a - b;
            }
            if (t == LexToken.Type.DIV) {
                if (n.hijos.size() != 2) return null;
                Double a = evaluarConstante(n.hijos.get(0)); Double b = evaluarConstante(n.hijos.get(1));
                if (a == null || b == null || Math.abs(b) < 1e-15) return null; return a / b;
            }
            if (t == LexToken.Type.EXP) {
                if (n.hijos.size() != 2) return null;
                Double a = evaluarConstante(n.hijos.get(0)); Double b = evaluarConstante(n.hijos.get(1));
                if (a == null || b == null) return null; return Math.pow(a, b);
            }
        }
        return null;
    }
}
