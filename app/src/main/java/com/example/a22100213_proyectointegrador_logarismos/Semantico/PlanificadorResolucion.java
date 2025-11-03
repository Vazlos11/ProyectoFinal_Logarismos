package com.example.a22100213_proyectointegrador_logarismos.Semantico;

import static com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.cloneTree;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PlanificadorResolucion {

    public static String plan(NodoAST raiz, ResultadoSemantico rs) {
        MetodoResolucion m = metodo(raiz, rs);
        switch (m) {
            case EXPRESION_SIN_OBJETIVO:
                return "Expresión sin objetivo (nada que resolver)";
            case EVALUACION_DIRECTA:
                return "Evaluación directa";
            case SUSTITUCION_SIMBOLICA_EVALUACION:
                return "Sustitución simbólica y evaluación";
            case ALGEBRA_COMPLEJOS:
                return "Álgebra de complejos (a+bi)";
            case IDENTIDADES_TRIG:
                return "Identidades y simplificación";
            case NEWTON_RAPHSON:
                return "Newton-Raphson";
            case POTENCIAS:
                return "Reglas de potencia";
            case ECUACION_LINEAL:
                return "Aislamiento en ecuación lineal";
            case ECUACION_CUADRATICA:
                return "Fórmula general";
            case POLI_RUFFINI:
                return "Ruffini";
            case ECUACION_POLINOMICA:
                return "Polinómica general";
            case ECUACION_LOGARITMICA:
                return "Ecuación logarítmica";
            case ECUACION_TRIGONOMETRICA:
                return "Ecuación trigonométrica";
            case DERIVADA_REGLA_EXPONENTE:
                return "Derivada regla de potencia";
            case DERIVADA_CONSTANTE_CERO:
                return "Derivada de constante";
            case DERIVADA_CONST_POR_FUNCION:
                return "Constante por función";
            case DERIVADA_REGLA_SUMA:
                return "Regla de la suma";
            case DERIVADA_PRODUCTO:
                return "Regla del producto";
            case DERIVADA_COCIENTE:
                return "Regla del cociente";
            case DERIVADA_CADENA:
                return "Regla de la cadena";
            case DERIVADA_TRIG:
                return "Derivadas trigonométricas";
            case INTEGRAL_REGLA_POTENCIA:
                return "Integral regla de potencia";
            case INTEGRAL_SUSTITUCION:
                return "Sustitución de variable";
            case INTEGRAL_POR_PARTES:
                return "Integración por partes";
            case INTEGRAL_TRIGONOMETRICA:
                return "Sustitución trigonométrica";
            case INTEGRAL_RACIONAL_PFD:
                return "Fracciones parciales";
            case INTEGRAL_NUMERICA_SIMPSON_O_TRAPECIO:
                return "Integración numérica";
            case DESPEJE_AISLACION_DIRECTA:
                return "Aislación directa";
            case DESPEJE_BALANCE_INVERSAS:
                return "Balance de operaciones";
            case DESPEJE_SIMBOLICO_O_NUMERICO:
                return "Aislación simbólica/numérica";
            case SISTEMA_CRAMER:
                return "Cramer";
            case ECUACION_EXPONENCIAL:
                return "Ecuación exponencial";
            case ECUACION_POTENCIA:
                return "Ecuación de potencia / radical";
            case ECUACION_VALOR_ABSOLUTO:
                return "Ecuación con valor absoluto";
            case ECUACION_TRIG_SIN:
                return "Ecuación trigonométrica (seno)";
            case ECUACION_TRIG_COS:
                return "Ecuación trigonométrica (coseno)";
            case ECUACION_TRIG_TAN:
                return "Ecuación trigonométrica (tangente)";
            case ECUACION_ARC_TRIG:
                return "Ecuación con funciones arcotrigonométricas";
            case ECUACION_RACIONAL_LINEAL:
                return "Ecuación racional lineal (ax+b)/(cx+d)=k";
            case ECUACION_RECIPROCA_LINEAL:
                return "Ecuación recíproca lineal 1/(ax+b)=c";
            case DESPEJE_SUSTITUCION_SIMBOLICA:
                return "Aislación simbólica (lineal)";

            case NINGUNO:
            default:
                return "Sin método asignado";
        }
    }

    public static MetodoResolucion metodo(NodoAST raiz, ResultadoSemantico rs) {
        if (raiz == null || rs == null) return MetodoResolucion.NINGUNO;
        if (rs.errores != null && !rs.errores.isEmpty()) return MetodoResolucion.NINGUNO;

        List<NodoAST> eqs = extraerEcuaciones(raiz);
        if (rs.tipoPrincipal == TipoExpresion.T8_SISTEMA_EC || (eqs != null && eqs.size() >= 2)) {
            return metodoSistema(raiz);
        }

        NodoAST eq = (eqs != null && eqs.size() == 1) ? eqs.get(0) : buscarNodo(raiz, LexToken.Type.EQUAL);
        if (eq != null && eq.hijos.size() == 2) {
            String v = unicaVariable(eq);
            if (v != null) {
                NodoAST L = eq.hijos.get(0), R = eq.hijos.get(1);
                NodoAST d = resta(L, R);

                if (esExpDeLinealIgualConst(eq, v))          return MetodoResolucion.ECUACION_EXPONENCIAL;
                if (esLnDeLinealIgualConst(eq, v))           return MetodoResolucion.ECUACION_LOGARITMICA;
                if (esLogBaseDeLinealIgualConst(eq, v))      return MetodoResolucion.ECUACION_LOGARITMICA;
                if (esPotenciaDeLinealIgualConst(eq, v))     return MetodoResolucion.ECUACION_POTENCIA;
                if (esRadicalDeLinealIgualConst(eq, v))      return MetodoResolucion.ECUACION_POTENCIA;
                if (esAbsDeLinealIgualConst(eq, v))          return MetodoResolucion.ECUACION_VALOR_ABSOLUTO;
                if (esSinDeLinealIgualConst(eq, v))          return MetodoResolucion.ECUACION_TRIG_SIN;
                if (esCosDeLinealIgualConst(eq, v))          return MetodoResolucion.ECUACION_TRIG_COS;
                if (esTanDeLinealIgualConst(eq, v))          return MetodoResolucion.ECUACION_TRIG_TAN;
                if (esArcTrigDeLinealIgualConst(eq, v))      return MetodoResolucion.ECUACION_ARC_TRIG;
                if (esReciprocoDeLinealIgualConst(eq, v))    return MetodoResolucion.ECUACION_RECIPROCA_LINEAL;
                if (esFraccionLinealIgualConst(eq, v))       return MetodoResolucion.ECUACION_RACIONAL_LINEAL;

                int gp = gradoPolinomioEn(d, v);
                if (gp >= 2) return metodoDespejePoli(eq);
                if (!esPolinomica(d)) return MetodoResolucion.NEWTON_RAPHSON;
                return metodoDespejeLineal(eq);
            }
            return MetodoResolucion.DESPEJE_SUSTITUCION_SIMBOLICA;
        }

        if (esSoloExpresionSinObjetivo(raiz)) return MetodoResolucion.EXPRESION_SIN_OBJETIVO;

        switch (rs.tipoPrincipal) {
            case T8_SISTEMA_EC:         return metodoSistema(raiz);
            case T3_DERIVADA:           return metodoDerivada(raiz);
            case T4_INTEGRAL_INDEFINIDA:return metodoIntegral(raiz, false);
            case T5_INTEGRAL_DEFINIDA:  return metodoIntegral(raiz, true);
            case T1_ARITMETICA:         return metodoAritmetica(raiz);
            case T2_ALGEBRA_FUNC:       return metodoAlgebra(raiz);
            case T6_DESPEJE_LINEAL:     return metodoDespejeLineal(raiz);
            case T7_DESPEJE_POLINOMICO: return metodoDespejePoli(raiz);
            case T9_IMAGINARIOS:        return MetodoResolucion.ALGEBRA_COMPLEJOS;
            default:                    return MetodoResolucion.NINGUNO;
        }
    }

    private static boolean isLnNode(NodoAST n) {
        return n != null && n.token != null && n.token.type == LexToken.Type.LN && n.hijos.size() == 1;
    }
    private static boolean isSinNode(NodoAST n) {
        return n != null && n.token != null && n.token.type == LexToken.Type.TRIG_SIN && n.hijos.size() == 1;
    }

    private static boolean requiereDistribucionConstSobreSumaConVar(NodoAST n, String var){
        if(n==null || n.token==null) return false;
        if(n.token.type==LexToken.Type.MUL && n.hijos.size()==2){
            Double c = evaluarConstante(n.hijos.get(0));
            NodoAST other = n.hijos.get(1);
            if(c==null){ c = evaluarConstante(other); other = n.hijos.get(0); }
            if(c!=null && other!=null && other.token!=null &&
                    (other.token.type==LexToken.Type.SUM || other.token.type==LexToken.Type.SUB) &&
                    contieneVariableExacta(other, var)) return true;
        }
        for(NodoAST h:n.hijos) if(requiereDistribucionConstSobreSumaConVar(h,var)) return true;
        return false;
    }

    private static boolean requiereLimpiarDenominadoresNumericos(NodoAST n){
        if(n==null || n.token==null) return false;
        if(n.token.type==LexToken.Type.DIV && n.hijos.size()==2){
            Double den = evaluarConstante(n.hijos.get(1));
            if(den!=null) return true;
        }
        for(NodoAST h:n.hijos) if(requiereLimpiarDenominadoresNumericos(h)) return true;
        return false;
    }

    private static boolean requiereSustitucionSimbolicaLineal(NodoAST eq, String var){
        return variableRepetida(eq) ||
                requiereDistribucionConstSobreSumaConVar(eq,var) ||
                requiereLimpiarDenominadoresNumericos(resta(eq.hijos.get(0), eq.hijos.get(1)));
    }


    private static boolean esLnDeLinealIgualConst(NodoAST eq, String var) {
        NodoAST L = eq.hijos.get(0), R = eq.hijos.get(1);
        if (isLnNode(L) && evaluarConstante(R) != null) {
            return coefLineal(L.hijos.get(0), var) != null;
        }
        if (isLnNode(R) && evaluarConstante(L) != null) {
            return coefLineal(R.hijos.get(0), var) != null;
        }
        return false;
    }
    private static boolean isCosNode(NodoAST n){ return n!=null && n.token!=null &&
            n.token.type==LexToken.Type.TRIG_COS && n.hijos.size()==1; }
    private static boolean isTanNode(NodoAST n){ return n!=null && n.token!=null &&
            n.token.type==LexToken.Type.TRIG_TAN && n.hijos.size()==1; }
    private static boolean isAbsNode(NodoAST n){ return n!=null && n.token!=null &&
            n.token.type==LexToken.Type.ABS && n.hijos.size()==1; }
    private static boolean isRadicalNode(NodoAST n){ return n!=null && n.token!=null &&
            n.token.type==LexToken.Type.RADICAL && n.hijos.size()==1; }
    private static boolean isExpENode(NodoAST n){ return n!=null && n.token!=null &&
            n.token.type==LexToken.Type.EXP && n.hijos.size()==2 &&
            n.hijos.get(0)!=null && n.hijos.get(0).token!=null &&
            n.hijos.get(0).token.type==LexToken.Type.CONST_E; }

    private static boolean esExpDeLinealIgualConst(NodoAST eq,String var){
        NodoAST L=eq.hijos.get(0), R=eq.hijos.get(1);
        if(isExpENode(L) && evaluarConstante(R)!=null) return coefLineal(L.hijos.get(1),var)!=null;
        if(isExpENode(R) && evaluarConstante(L)!=null) return coefLineal(R.hijos.get(1),var)!=null;
        return false;
    }

    private static boolean esLogBaseDeLinealIgualConst(NodoAST eq,String var){
        NodoAST L=eq.hijos.get(0), R=eq.hijos.get(1);
        boolean Lok = (L!=null && L.token!=null &&
                (L.token.type==LexToken.Type.LOG || L.token.type==LexToken.Type.LOG_BASE2
                        || L.token.type==LexToken.Type.LOG_BASE10) && L.hijos.size()==1
                && coefLineal(L.hijos.get(0),var)!=null && evaluarConstante(R)!=null);
        boolean Rok = (R!=null && R.token!=null &&
                (R.token.type==LexToken.Type.LOG || R.token.type==LexToken.Type.LOG_BASE2
                        || R.token.type==LexToken.Type.LOG_BASE10) && R.hijos.size()==1
                && coefLineal(R.hijos.get(0),var)!=null && evaluarConstante(L)!=null);
        return Lok || Rok;
    }

    private static boolean esPotenciaDeLinealIgualConst(NodoAST eq,String var){
        NodoAST L=eq.hijos.get(0), R=eq.hijos.get(1);
        return (esPotenciaLineal(L,var) && evaluarConstante(R)!=null) ||
                (esPotenciaLineal(R,var) && evaluarConstante(L)!=null);
    }
    private static boolean esPotenciaLineal(NodoAST n,String var){
        if(n==null || n.token==null || n.token.type!=LexToken.Type.EXP || n.hijos.size()!=2) return false;
        Double e=evaluarConstante(n.hijos.get(1)); if(e==null) return false;
        return coefLineal(n.hijos.get(0),var)!=null;
    }

    private static boolean esRadicalDeLinealIgualConst(NodoAST eq,String var){
        NodoAST L=eq.hijos.get(0), R=eq.hijos.get(1);
        return (isRadicalNode(L) && coefLineal(L.hijos.get(0),var)!=null && evaluarConstante(R)!=null) ||
                (isRadicalNode(R) && coefLineal(R.hijos.get(0),var)!=null && evaluarConstante(L)!=null);
    }

    private static boolean esAbsDeLinealIgualConst(NodoAST eq,String var){
        NodoAST L=eq.hijos.get(0), R=eq.hijos.get(1);
        return (isAbsNode(L) && coefLineal(L.hijos.get(0),var)!=null && evaluarConstante(R)!=null) ||
                (isAbsNode(R) && coefLineal(R.hijos.get(0),var)!=null && evaluarConstante(L)!=null);
    }

    private static boolean esCosDeLinealIgualConst(NodoAST eq,String var){
        NodoAST L=eq.hijos.get(0), R=eq.hijos.get(1);
        Double cL=evaluarConstante(L), cR=evaluarConstante(R);
        if(isCosNode(L) && cR!=null && Math.abs(cR)<=1+1e-12) return coefLineal(L.hijos.get(0),var)!=null;
        if(isCosNode(R) && cL!=null && Math.abs(cL)<=1+1e-12) return coefLineal(R.hijos.get(0),var)!=null;
        return false;
    }
    private static boolean esTanDeLinealIgualConst(NodoAST eq,String var){
        NodoAST L=eq.hijos.get(0), R=eq.hijos.get(1);
        if(isTanNode(L) && evaluarConstante(R)!=null) return coefLineal(L.hijos.get(0),var)!=null;
        if(isTanNode(R) && evaluarConstante(L)!=null) return coefLineal(R.hijos.get(0),var)!=null;
        return false;
    }

    private static boolean esArcTrigDeLinealIgualConst(NodoAST eq,String var){
        NodoAST L=eq.hijos.get(0), R=eq.hijos.get(1);
        return isArcTrigLineal(L,var) && evaluarConstante(R)!=null
                || isArcTrigLineal(R,var) && evaluarConstante(L)!=null;
    }
    private static boolean isArcTrigLineal(NodoAST n,String var){
        if(n==null || n.token==null || n.hijos.size()!=1) return false;
        switch(n.token.type){
            case TRIG_ARCSIN: case TRIG_ARCCOS: case TRIG_ARCTAN:
            case TRIG_ARCCOT: case TRIG_ARCSEC: case TRIG_ARCCSC:
                return coefLineal(n.hijos.get(0),var)!=null;
            default: return false;
        }
    }

    private static boolean esReciprocoDeLinealIgualConst(NodoAST eq,String var){
        NodoAST L=eq.hijos.get(0), R=eq.hijos.get(1);
        return (esReciprocoDeLineal(L,var) && evaluarConstante(R)!=null) ||
                (esReciprocoDeLineal(R,var) && evaluarConstante(L)!=null);
    }

    private static boolean esFraccionLinealIgualConst(NodoAST eq,String var){
        if(eq==null || eq.hijos.size()!=2) return false;
        NodoAST L=eq.hijos.get(0), R=eq.hijos.get(1);
        Double cL=evaluarConstante(L), cR=evaluarConstante(R);
        if(cR!=null) return esCocienteLineal(L,var);
        if(cL!=null) return esCocienteLineal(R,var);
        return false;
    }
    private static boolean esCocienteLineal(NodoAST n,String var){
        if(n==null || n.token==null || n.token.type!=LexToken.Type.DIV || n.hijos.size()!=2) return false;
        return gradoPolinomioEn(n.hijos.get(0),var)==1 && gradoPolinomioEn(n.hijos.get(1),var)==1;
    }

    private static boolean esSinDeLinealIgualConst(NodoAST eq, String var) {
        NodoAST L = eq.hijos.get(0), R = eq.hijos.get(1);
        Double cL = evaluarConstante(L), cR = evaluarConstante(R);
        if (isSinNode(L) && cR != null && Math.abs(cR) <= 1.0 + 1e-12) {
            return coefLineal(L.hijos.get(0), var) != null;
        }
        if (isSinNode(R) && cL != null && Math.abs(cL) <= 1.0 + 1e-12) {
            return coefLineal(R.hijos.get(0), var) != null;
        }
        return false;
    }

    private static boolean esSoloExpresionSinObjetivo(NodoAST n) {
        if (n == null) return false;
        boolean hayIgual = (buscarNodo(n, LexToken.Type.EQUAL) != null);
        boolean hayDeriv = (buscarNodo(n, LexToken.Type.DERIV) != null) || (buscarNodo(n, LexToken.Type.PRIME) != null);
        boolean hayIntegral = (buscarNodo(n, LexToken.Type.INTEGRAL_INDEF) != null) || (buscarNodo(n, LexToken.Type.INTEGRAL_DEF) != null);
        boolean haySistema = false;
        boolean hayObjetivo = hayIgual || hayDeriv || hayIntegral || haySistema;

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
        if (f == null) {
            NodoAST p = buscarNodo(raiz, LexToken.Type.PRIME);
            if (p != null && !p.hijos.isEmpty()) f = p.hijos.get(0);
        }
        if (f == null) return MetodoResolucion.NINGUNO;

        if (esConstantePura(f)) return MetodoResolucion.DERIVADA_CONSTANTE_CERO;
        if (esMonomioAxn(f)) return MetodoResolucion.DERIVADA_REGLA_EXPONENTE;
        if (esConstantePorFuncion(f)) return MetodoResolucion.DERIVADA_CONST_POR_FUNCION;

        if (esEnvolventeUnariaTop(f)) {
            if (esTrigRaiz(f) && hijoEsVariableSimple(f)) return MetodoResolucion.DERIVADA_TRIG;
            return MetodoResolucion.DERIVADA_CADENA;
        }

        if (tieneOperacion(f, LexToken.Type.SUM) || tieneOperacion(f, LexToken.Type.SUB)) return MetodoResolucion.DERIVADA_REGLA_SUMA;
        if (tieneOperacion(f, LexToken.Type.MUL)) return MetodoResolucion.DERIVADA_PRODUCTO;
        if (tieneOperacion(f, LexToken.Type.DIV)) return MetodoResolucion.DERIVADA_COCIENTE;

        if (contieneTrig(f)) return MetodoResolucion.DERIVADA_TRIG;

        return MetodoResolucion.NINGUNO;
    }
    private static String varDerivacion(NodoAST raiz) {
        NodoAST d = buscarNodo(raiz, LexToken.Type.DERIV);
        if (d != null && d.hijos.size() > 1 && d.hijos.get(1) != null && d.hijos.get(1).token != null) {
            String s = d.hijos.get(1).token.value;
            if (s != null && s.length() >= 2 && (s.charAt(0) == 'd' || s.charAt(0) == 'D')) return s.substring(1);
        }
        return "x";
    }

    private static boolean esTrigCompuesta(NodoAST n, String var) {
        if (n == null || n.token == null) return false;
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
            case TRIG_ARCCOT: {
                if (n.hijos.size() != 1) return false;
                NodoAST u = n.hijos.get(0);
                if (u != null && u.token != null && u.token.type == LexToken.Type.VARIABLE && var.equals(u.token.value)) return false;
                return contieneVariable(u);
            }
            default:
                for (NodoAST h : n.hijos) if (esTrigCompuesta(h, var)) return true;
                return false;
        }
    }


    private static Double coefLineal(NodoAST u, String var) {
        if (u == null || u.token == null) return null;
        LexToken.Type t = u.token.type;

        if (t == LexToken.Type.VARIABLE && var.equals(u.token.value) && u.hijos.isEmpty())
            return 1.0;

        if (esConstantePura(u)) return 0.0;

        if (t == LexToken.Type.MUL && u.hijos.size() == 2) {
            Double cL = evaluarConstante(u.hijos.get(0));
            Double cR = evaluarConstante(u.hijos.get(1));
            if (cL != null) {
                Double k = coefLineal(u.hijos.get(1), var);
                return (k == null) ? null : cL * k;
            }
            if (cR != null) {
                Double k = coefLineal(u.hijos.get(0), var);
                return (k == null) ? null : cR * k;
            }
            return null;
        }

        if ((t == LexToken.Type.SUM || t == LexToken.Type.SUB) && u.hijos.size() == 2) {
            Double k1 = coefLineal(u.hijos.get(0), var);
            Double k2 = coefLineal(u.hijos.get(1), var);
            if (k1 == null || k2 == null) return null;
            return (t == LexToken.Type.SUM) ? (k1 + k2) : (k1 - k2);
        }

        return null;
    }

    private static boolean esTanDeLineal(NodoAST n, String var) {
        return n != null && n.token != null && n.token.type == LexToken.Type.TRIG_TAN &&
                coefLineal(n.hijos.get(0), var) != null &&
                Math.abs(coefLineal(n.hijos.get(0), var)) > 1e-15;
    }

    private static boolean esSoloLn(NodoAST n, String var) {
        return n != null && n.token != null && n.token.type == LexToken.Type.LN &&
                n.hijos.size() == 1 && contieneVariableExacta(n.hijos.get(0), var);
    }

    private static boolean esReciprocoDeLineal(NodoAST n, String var) {
        if (n == null || n.token == null || n.token.type != LexToken.Type.DIV || n.hijos.size() != 2)
            return false;
        Double num = evaluarConstante(n.hijos.get(0));
        if (num == null) return false;
        Double k = coefLineal(n.hijos.get(1), var);
        return k != null && Math.abs(k) > 1e-15;
    }

    private static Integer expoEnteroDeX(NodoAST u, String var) {
        if (u == null || u.token == null) return null;
        if (u.token.type == LexToken.Type.EXP && u.hijos.size() == 2) {
            NodoAST base = u.hijos.get(0), exp = u.hijos.get(1);
            if (base != null && base.token != null &&
                    base.token.type == LexToken.Type.VARIABLE && var.equals(base.token.value)) {
                Double e = evaluarConstante(exp);
                if (e != null) {
                    int p = (int) Math.rint(e);
                    if (Math.abs(e - p) < 1e-9) return p;
                }
            }
        }
        return null;
    }

    private static boolean esCadenaPotenciaConFactor(NodoAST n, String var) {
        if (n == null || n.token == null || n.token.type != LexToken.Type.MUL || n.hijos.size() != 2)
            return false;
        NodoAST a = n.hijos.get(0), b = n.hijos.get(1);
        return matchCadenaPot(a, b, var) || matchCadenaPot(b, a, var);
    }

    private static boolean matchCadenaPot(NodoAST factor, NodoAST fun, String var) {
        if (fun == null || fun.token == null) return false;
        LexToken.Type t = fun.token.type;
        NodoAST u = null;
        if (t == LexToken.Type.EXP && fun.hijos.size() == 2 &&
                fun.hijos.get(0) != null && fun.hijos.get(0).token != null &&
                fun.hijos.get(0).token.type == LexToken.Type.CONST_E) {
            u = fun.hijos.get(1);
        } else if ((t == LexToken.Type.TRIG_SIN || t == LexToken.Type.TRIG_COS || t == LexToken.Type.TRIG_TAN) &&
                fun.hijos.size() == 1) {
            u = fun.hijos.get(0);
        } else {
            return false;
        }
        Integer p = expoEnteroDeX(u, var);
        if (p == null || p < 2) return false;
        return esMonomioAxnRespecto(factor, var) && gradoEn(factor, var) == p - 1;
    }

    private static boolean esExpDeLineal(NodoAST n, String var) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.EXP && n.hijos.size() == 2) {
            NodoAST base = n.hijos.get(0), expo = n.hijos.get(1);
            if (base != null && base.token != null && base.token.type == LexToken.Type.CONST_E) {
                Double k = coefLineal(expo, var);
                return k != null && Math.abs(k) > 1e-15;
            }
        }
        return false;
    }

    private static boolean esSinDeLineal(NodoAST n, String var) {
        return n != null && n.token != null && n.token.type == LexToken.Type.TRIG_SIN &&
                coefLineal(n.hijos.get(0), var) != null && Math.abs(coefLineal(n.hijos.get(0), var)) > 1e-15;
    }

    private static boolean esCosDeLineal(NodoAST n, String var) {
        return n != null && n.token != null && n.token.type == LexToken.Type.TRIG_COS &&
                coefLineal(n.hijos.get(0), var) != null && Math.abs(coefLineal(n.hijos.get(0), var)) > 1e-15;
    }

    private static boolean esProductoMonomioPorExpLineal(NodoAST n, String var) {
        if (!esProductoDeDosFactores(n)) return false;
        NodoAST a = n.hijos.get(0), b = n.hijos.get(1);
        return (esMonomioAxnRespecto(a, var) && esExpDeLineal(b, var)) ||
                (esMonomioAxnRespecto(b, var) && esExpDeLineal(a, var));
    }

    private static boolean esProductoMonomioPorSinLineal(NodoAST n, String var) {
        if (!esProductoDeDosFactores(n)) return false;
        NodoAST a = n.hijos.get(0), b = n.hijos.get(1);
        return (esMonomioAxnRespecto(a, var) && esSinDeLineal(b, var)) ||
                (esMonomioAxnRespecto(b, var) && esSinDeLineal(a, var));
    }

    private static boolean esProductoMonomioPorCosLineal(NodoAST n, String var) {
        if (!esProductoDeDosFactores(n)) return false;
        NodoAST a = n.hijos.get(0), b = n.hijos.get(1);
        return (esMonomioAxnRespecto(a, var) && esCosDeLineal(b, var)) ||
                (esMonomioAxnRespecto(b, var) && esCosDeLineal(a, var));
    }

    private static boolean esLnPorMonomio(NodoAST n, String var) {
        if (!esProductoDeDosFactores(n)) return false;
        NodoAST a = n.hijos.get(0), b = n.hijos.get(1);
        boolean aLn = a != null && a.token != null && a.token.type == LexToken.Type.LN &&
                a.hijos.size() == 1 && contieneVariableExacta(a.hijos.get(0), var);
        boolean bLn = b != null && b.token != null && b.token.type == LexToken.Type.LN &&
                b.hijos.size() == 1 && contieneVariableExacta(b.hijos.get(0), var);
        return (aLn && esMonomioAxnRespecto(b, var)) || (bLn && esMonomioAxnRespecto(a, var));
    }

    private static boolean esCicloExpTrig(NodoAST n, String var) {
        if (!esProductoDeDosFactores(n)) return false;
        NodoAST a = n.hijos.get(0), b = n.hijos.get(1);
        boolean exp = esExpDeLineal(a, var) || esExpDeLineal(b, var);
        boolean trig = esSinDeLineal(a, var) || esCosDeLineal(a, var) ||
                esSinDeLineal(b, var) || esCosDeLineal(b, var);
        return exp && trig;
    }
    public static NodoAST normalizeSquaresForTrigSub(NodoAST n, String var) {
        if (n == null || n.token == null) return n;
        NodoAST r = cloneTree(n);
        normalizeSquaresForTrigSubInPlace(r, var);
        return r;
    }

    private static void normalizeSquaresForTrigSubInPlace(NodoAST n, String var) {
        if (n == null || n.token == null) return;
        for (NodoAST h : n.hijos) normalizeSquaresForTrigSubInPlace(h, var);

        if (n.token.type == LexToken.Type.RADICAL && n.hijos.size() == 1) {
            NodoAST inside = n.hijos.get(0);
            if (inside != null && inside.token != null &&
                    (inside.token.type == LexToken.Type.SUM || inside.token.type == LexToken.Type.SUB) &&
                    inside.hijos.size() == 2) {
                NodoAST A = inside.hijos.get(0);
                NodoAST B = inside.hijos.get(1);

                if (isPositiveConst(A) && isConstTimesSquareOfLinear(B, var)) {
                    inside.hijos.set(1, toSquareLinear(B, var));
                } else if (isPositiveConst(B) && isConstTimesSquareOfLinear(A, var)) {
                    inside.hijos.set(0, toSquareLinear(A, var));
                }
            }
        }
    }

    private static boolean isPositiveConst(NodoAST n) {
        Double c = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.evalConst(n);
        return c != null && c >= 0;
    }

    private static boolean isConstTimesSquareOfLinear(NodoAST n, String var) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.MUL && n.hijos.size() == 2) {
            Double cL = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.evalConst(n.hijos.get(0));
            Double cR = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.evalConst(n.hijos.get(1));
            NodoAST exp = (cL != null) ? n.hijos.get(1) : (cR != null ? n.hijos.get(0) : null);
            Double c = (cL != null) ? cL : cR;
            if (c != null && c > 0 && exp != null && exp.token != null &&
                    exp.token.type == LexToken.Type.EXP && exp.hijos.size() == 2) {
                Double e = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.evalConst(exp.hijos.get(1));
                if (e != null && Math.abs(e - 2.0) < 1e-9) return contieneVarLineal(exp.hijos.get(0), var);
            }
        }
        return false;
    }

    private static boolean contieneVarLineal(NodoAST u, String var) {
        if (u == null || u.token == null) return false;
        if (u.token.type == LexToken.Type.VARIABLE && var.equals(u.token.value)) return true;
        if (u.token.type == LexToken.Type.MUL && u.hijos.size() == 2) {
            return contieneVarLineal(u.hijos.get(0), var) || contieneVarLineal(u.hijos.get(1), var);
        }
        if ((u.token.type == LexToken.Type.SUM || u.token.type == LexToken.Type.SUB) && u.hijos.size() == 2) {
            return contieneVarLineal(u.hijos.get(0), var) || contieneVarLineal(u.hijos.get(1), var);
        }
        return false;
    }

    private static NodoAST toSquareLinear(NodoAST n, String var) {
        Double cL = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.evalConst(n.hijos.get(0));
        Double cR = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.evalConst(n.hijos.get(1));
        NodoAST exp = (cL != null) ? n.hijos.get(1) : n.hijos.get(0);
        double c = (cL != null) ? cL : cR;

        NodoAST base = exp.hijos.get(0);
        double s = Math.sqrt(Math.abs(c));
        NodoAST sNode = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.number(s);
        NodoAST newBase = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.bin(
                LexToken.Type.MUL, sNode,
                cloneTree(base), "*", 6
        );
        return com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.bin(
                LexToken.Type.EXP, newBase,
                com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.number(2.0), "^", 7
        );
    }

    private static MetodoResolucion metodoIntegral(NodoAST raiz, boolean definida) {
        NodoAST n = buscarNodo(raiz, definida ? LexToken.Type.INTEGRAL_DEF : LexToken.Type.INTEGRAL_INDEF);
        if (n == null) return MetodoResolucion.NINGUNO;

        String var = varIntegracion(n, definida);
        NodoAST cuerpo = extraerCuerpoIntegral(n, definida);
        if (cuerpo == null || var == null) return MetodoResolucion.NINGUNO;

        cuerpo = IntegralUtils.foldEConstTimesXIntoExpLinear(cuerpo, var);
        cuerpo = IntegralUtils.normalizeSquaresForTrigSub(cuerpo, var);

        if (gradoSiPolinomio(cuerpo, var).ok) return MetodoResolucion.INTEGRAL_REGLA_POTENCIA;

        if (esMonomioAxnRespecto(cuerpo, var) && !esExponenteMenosUno(cuerpo, var))
            return MetodoResolucion.INTEGRAL_REGLA_POTENCIA;

        if (requiereSustitucion(cuerpo, var)) return MetodoResolucion.INTEGRAL_SUSTITUCION;
        if (requierePorPartes(cuerpo, var))  return MetodoResolucion.INTEGRAL_POR_PARTES;
        if (requiereTrigSub(cuerpo, var))    return MetodoResolucion.INTEGRAL_TRIGONOMETRICA;
        if (esRacionalPropiaPolinomial(cuerpo, var)) return MetodoResolucion.INTEGRAL_RACIONAL_PFD;

        return MetodoResolucion.NINGUNO;
    }


    private static boolean requiereSustitucion(NodoAST n, String var) {
        if (n == null) return false;
        if (existeSustitucionSimple(n, var)) return true;
        if (esReciprocoDeLineal(n, var)) return true;
        if (esCadenaPotenciaConFactor(n, var)) return true;
        return false;
    }

    private static boolean requierePorPartes(NodoAST n, String var) {
        if (!esProductoDeDosFactores(n)) return false;
        if (esLnPorMonomio(n, var)) return true;
        if (esProductoMonomioPorExpLineal(n, var)) return true;
        if (esProductoMonomioPorSinLineal(n, var)) return true;
        if (esProductoMonomioPorCosLineal(n, var)) return true;
        if (esCicloExpTrig(n, var)) return true;
        return prioridadLIATE(n) > 0;
    }

    private static boolean requiereTrigSub(NodoAST n, String var) {
        return esPatronTrigSub(n, var);
    }

    private static boolean esRacionalPropiaPolinomial(NodoAST n, String var) {
        return esFraccionRacionalPolinomialProper(n, var);
    }

    private static MetodoResolucion metodoAritmetica(NodoAST raiz) {
        if (contieneImaginarios(raiz)) return MetodoResolucion.ALGEBRA_COMPLEJOS;
        if (contienePiOE(raiz)) return MetodoResolucion.SUSTITUCION_SIMBOLICA_EVALUACION;
        return MetodoResolucion.EVALUACION_DIRECTA;
    }

    private static MetodoResolucion metodoAlgebra(NodoAST raiz) {
        if (contieneImaginarios(raiz)) return MetodoResolucion.ALGEBRA_COMPLEJOS;
        if (contieneTrig(raiz) || contieneLog(raiz)) return MetodoResolucion.IDENTIDADES_TRIG;
        if (tieneExponenteFracONeg(raiz)) return MetodoResolucion.POTENCIAS;
        return MetodoResolucion.SUSTITUCION_SIMBOLICA_EVALUACION;
    }

    private static boolean varEnMulDivConFactorNoConst(NodoAST n, String var) {
        if (n == null || n.token == null) return false;
        if ((n.token.type == LexToken.Type.MUL || n.token.type == LexToken.Type.DIV) && n.hijos.size() == 2) {
            NodoAST a = n.hijos.get(0), b = n.hijos.get(1);
            boolean aVar = contieneVariableExacta(a, var);
            boolean bVar = contieneVariableExacta(b, var);
            Double aC = evaluarConstante(a);
            Double bC = evaluarConstante(b);
            if ((aVar && bC == null) || (bVar && aC == null)) return true;
        }
        for (NodoAST h : n.hijos) if (varEnMulDivConFactorNoConst(h, var)) return true;
        return false;
    }

    private static MetodoResolucion metodoDespejeLineal(NodoAST eq) {
        if (eq == null || eq.token == null || eq.token.type != LexToken.Type.EQUAL || eq.hijos.size() != 2)
            return MetodoResolucion.DESPEJE_SUSTITUCION_SIMBOLICA;

        if (contieneTrig(eq) || contieneLog(eq))
            return MetodoResolucion.NEWTON_RAPHSON;

        String v = unicaVariable(eq);
        if (v == null) return MetodoResolucion.DESPEJE_SUSTITUCION_SIMBOLICA;

        NodoAST L = eq.hijos.get(0), R = eq.hijos.get(1);
        int g = gradoPolinomioEn(resta(L, R), v);
        if (g > 1) return MetodoResolucion.DESPEJE_SUSTITUCION_SIMBOLICA;

        if (esRacional(eq) || varEnMulDivConFactorNoConst(eq, v))
            return MetodoResolucion.DESPEJE_BALANCE_INVERSAS;

        if (requiereSustitucionSimbolicaLineal(eq, v))
            return MetodoResolucion.DESPEJE_SUSTITUCION_SIMBOLICA;

        boolean Llin = (gradoEn(L, v) >= 0 && gradoEn(L, v) <= 1);
        boolean Rlin = (gradoEn(R, v) >= 0 && gradoEn(R, v) <= 1);
        Double Lc = evaluarConstante(L);
        Double Rc = evaluarConstante(R);

        if ((Llin && Rc != null) || (Rlin && Lc != null))
            return MetodoResolucion.DESPEJE_AISLACION_DIRECTA;

        if (Llin && Rlin) return MetodoResolucion.DESPEJE_SUSTITUCION_SIMBOLICA;

        return MetodoResolucion.DESPEJE_BALANCE_INVERSAS;
    }
    private static boolean esVariableIgualConstante(NodoAST eq) {
        if (eq == null || eq.token == null || eq.token.type != LexToken.Type.EQUAL || eq.hijos.size() != 2)
            return false;
        NodoAST L = eq.hijos.get(0), R = eq.hijos.get(1);
        boolean Lvar = (L != null && L.token != null && L.token.type == LexToken.Type.VARIABLE && L.hijos.isEmpty());
        boolean Rvar = (R != null && R.token != null && R.token.type == LexToken.Type.VARIABLE && R.hijos.isEmpty());
        Double Lc = evaluarConstante(L);
        Double Rc = evaluarConstante(R);
        return (Lvar && Rc != null) || (Rvar && Lc != null);
    }
    private static double[] coeficientesPolinomio(NodoAST n, String var) {
        GradoPolinomio g = gradoSiPolinomio(n, var);
        if (!g.ok || g.grado < 0) return null;
        double[] a = new double[g.grado + 1];
        acumCoeficientes(n, var, a);
        return a;
    }

    private static void acumCoeficientes(NodoAST n, String var, double[] a) {
        if (n == null || n.token == null) return;
        LexToken.Type t = n.token.type;

        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL) {
            a[0] += Double.parseDouble(n.token.value);
            return;
        }
        if (t == LexToken.Type.VARIABLE && var.equals(n.token.value) && n.hijos.isEmpty()) {
            if (a.length > 1) a[1] += 1.0;
            return;
        }
        if (t == LexToken.Type.SUM) {
            for (NodoAST h : n.hijos) acumCoeficientes(h, var, a);
            return;
        }
        if (t == LexToken.Type.SUB) {
            if (!n.hijos.isEmpty()) {
                acumCoeficientes(n.hijos.get(0), var, a);
                for (int i = 1; i < n.hijos.size(); i++) {
                    mulConstCoef(n.hijos.get(i), var, a, -1.0);
                }
            }
            return;
        }
        if (t == LexToken.Type.MUL) {
            if (n.hijos.size() == 2) {
                Double cL = evaluarConstante(n.hijos.get(0));
                Double cR = evaluarConstante(n.hijos.get(1));
                if (cL != null) { mulConstCoef(n.hijos.get(1), var, a, cL); return; }
                if (cR != null) { mulConstCoef(n.hijos.get(0), var, a, cR); return; }
            }
            double cacc = 1.0;
            NodoAST resto = null;
            for (NodoAST h : n.hijos) {
                Double ch = evaluarConstante(h);
                if (ch != null) cacc *= ch;
                else resto = (resto == null) ? h : mul(resto, h);
            }
            if (resto != null) mulConstCoef(resto, var, a, cacc);
            else a[0] += cacc;
            return;
        }
        if (t == LexToken.Type.EXP && n.hijos.size() == 2) {
            NodoAST base = n.hijos.get(0);
            Double e = evaluarConstante(n.hijos.get(1));
            if (e != null &&
                    base != null && base.token != null &&
                    base.token.type == LexToken.Type.VARIABLE && var.equals(base.token.value)) {
                int p = (int)Math.rint(e);
                if (p >= 0 && p < a.length) a[p] += 1.0;
                return;
            }
        }
        for (NodoAST h : n.hijos) acumCoeficientes(h, var, a);
    }

    private static void mulConstCoef(NodoAST n, String var, double[] a, double c) {
        if (n == null || n.token == null) return;
        LexToken.Type t = n.token.type;

        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL) {
            a[0] += c * Double.parseDouble(n.token.value);
            return;
        }
        if (t == LexToken.Type.VARIABLE && var.equals(n.token.value) && n.hijos.isEmpty()) {
            if (a.length > 1) a[1] += c;
            return;
        }
        if (t == LexToken.Type.SUM) { for (NodoAST h : n.hijos) mulConstCoef(h, var, a, c); return; }
        if (t == LexToken.Type.SUB) {
            if (!n.hijos.isEmpty()) {
                mulConstCoef(n.hijos.get(0), var, a, c);
                for (int i = 1; i < n.hijos.size(); i++) mulConstCoef(n.hijos.get(i), var, a, -c);
            }
            return;
        }
        if (t == LexToken.Type.MUL) {
            if (n.hijos.size() == 2) {
                Double cL = evaluarConstante(n.hijos.get(0));
                Double cR = evaluarConstante(n.hijos.get(1));
                if (cL != null) { mulConstCoef(n.hijos.get(1), var, a, c * cL); return; }
                if (cR != null) { mulConstCoef(n.hijos.get(0), var, a, c * cR); return; }
            }
            double cacc = c;
            NodoAST resto = null;
            for (NodoAST h : n.hijos) {
                Double ch = evaluarConstante(h);
                if (ch != null) cacc *= ch; else resto = (resto == null) ? h : mul(resto, h);
            }
            if (resto != null) mulConstCoef(resto, var, a, cacc);
            else a[0] += cacc;
            return;
        }
        if (t == LexToken.Type.EXP && n.hijos.size() == 2) {
            NodoAST base = n.hijos.get(0);
            Double e = evaluarConstante(n.hijos.get(1));
            if (e != null &&
                    base != null && base.token != null &&
                    base.token.type == LexToken.Type.VARIABLE && var.equals(base.token.value)) {
                int p = (int)Math.rint(e);
                if (p >= 0 && p < a.length) a[p] += c;
                return;
            }
        }
        for (NodoAST h : n.hijos) mulConstCoef(h, var, a, c);
    }

    private static boolean casiEntero(double x) {
        return Math.abs(x - Math.rint(x)) < 1e-9;
    }

    private static double evalPoli(double[] a, double x) {
        double s = 0.0;
        for (int i = a.length - 1; i >= 0; i--) s = s * x + a[i];
        return s;
    }

    private static java.util.Set<Double> candidatosRacionalesDe(double c0, double an) {
        java.util.Set<Double> s = new java.util.LinkedHashSet<>();
        int A = (int)Math.rint(Math.abs(an));
        int C = (int)Math.rint(Math.abs(c0));
        if (A == 0) A = 1;
        if (C == 0) C = 1;
        for (int p = 1; p <= C; p++) if (C % p == 0)
            for (int q = 1; q <= A; q++) if (A % q == 0) {
                s.add((double)p / q); s.add(-(double)p / q);
            }
        return s;
    }
    private static boolean tieneRaizRacional(NodoAST n, String var) {
        double[] a = coeficientesPolinomio(n, var);
        if (a == null || a.length < 2) return false;
        double an = a[a.length - 1], c0 = a[0];
        if (!casiEntero(an) || !casiEntero(c0)) return false;
        for (double r : candidatosRacionalesDe(c0, an)) {
            if (Math.abs(evalPoli(a, r)) < 1e-10) return true;
        }
        return false;
    }

    private static NodoAST mul(NodoAST a, NodoAST b) {
        NodoAST n = new NodoAST(new LexToken(LexToken.Type.MUL, "*", 6));
        n.addHijo(cloneAST(a));
        n.addHijo(cloneAST(b));
        return n;
    }

    private static MetodoResolucion metodoDespejePoli(NodoAST eq) {
        if (eq == null || eq.token == null || eq.token.type != LexToken.Type.EQUAL || eq.hijos.size() != 2)
            return MetodoResolucion.NEWTON_RAPHSON;
        String var = unicaVariable(eq);
        if (var == null) return MetodoResolucion.NEWTON_RAPHSON;
        NodoAST d = resta(eq.hijos.get(0), eq.hijos.get(1));
        int g = gradoPolinomioEn(d, var);
        if (g < 0) return MetodoResolucion.NEWTON_RAPHSON;
        if (g == 1) return MetodoResolucion.DESPEJE_SUSTITUCION_SIMBOLICA;
        if (g == 2) return MetodoResolucion.ECUACION_CUADRATICA;
        return MetodoResolucion.POLI_RUFFINI;
    }

    private static boolean esSistemaLineal(NodoAST raiz, Set<String> vars) {
        List<NodoAST> eqs = extraerEcuaciones(raiz);
        if (eqs.size() < 2 || eqs.size() > 3) return false;
        for (NodoAST eq : eqs) {
            if (eq == null || eq.token == null || eq.token.type != LexToken.Type.EQUAL || eq.hijos.size() != 2)
                return false;
            NodoAST L = eq.hijos.get(0), R = eq.hijos.get(1);
            if (!expresionLinealEnVars(L, vars) || !expresionLinealEnVars(R, vars)) return false;

            Set<String> veq = varsEnSubarbol(eq);
            veq.retainAll(vars);
            if (!veq.containsAll(vars)) return false;
        }
        return true;
    }
    private static boolean expresionLinealEnVars(NodoAST n, Set<String> vars) {
        if (n == null || n.token == null) return true;
        LexToken.Type t = n.token.type;

        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL || t == LexToken.Type.CONST_E || t == LexToken.Type.CONST_PI)
            return true;
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
    private static boolean esEnvolventeUnariaTop(NodoAST f) {
        if (f == null || f.token == null) return false;
        LexToken.Type t = f.token.type;
        if (t == LexToken.Type.LN || t == LexToken.Type.LOG || t == LexToken.Type.LOG_BASE2 || t == LexToken.Type.LOG_BASE10) {
            return f.hijos.size() == 1;
        }
        if (t == LexToken.Type.RADICAL) {
            return f.hijos.size() == 1;
        }
        if (t == LexToken.Type.TRIG_SIN || t == LexToken.Type.TRIG_COS || t == LexToken.Type.TRIG_TAN ||
                t == LexToken.Type.TRIG_COT || t == LexToken.Type.TRIG_SEC || t == LexToken.Type.TRIG_CSC ||
                t == LexToken.Type.TRIG_ARCSIN || t == LexToken.Type.TRIG_ARCCOS || t == LexToken.Type.TRIG_ARCTAN ||
                t == LexToken.Type.TRIG_ARCCOT || t == LexToken.Type.TRIG_ARCSEC || t == LexToken.Type.TRIG_ARCCSC) {
            return f.hijos.size() == 1;
        }
        if (t == LexToken.Type.EXP && f.hijos.size() == 2) {
            if (f.hijos.get(0) != null && f.hijos.get(0).token != null &&
                    f.hijos.get(0).token.type == LexToken.Type.CONST_E) return true;
            Double e = evaluarConstante(f.hijos.get(1));
            return e != null;
        }
        return false;
    }
    private static boolean esTrigRaiz(NodoAST f) {
        if (f == null || f.token == null) return false;
        switch (f.token.type) {
            case TRIG_SIN: case TRIG_COS: case TRIG_TAN:
            case TRIG_COT: case TRIG_SEC: case TRIG_CSC:
            case TRIG_ARCSIN: case TRIG_ARCCOS: case TRIG_ARCTAN:
            case TRIG_ARCCOT: case TRIG_ARCSEC: case TRIG_ARCCSC:
                return true;
            default: return false;
        }
    }

    private static boolean hijoEsVariableSimple(NodoAST f) {
        if (f == null || f.hijos.isEmpty()) return false;
        NodoAST u = f.hijos.get(0);
        return u != null && u.token != null && u.token.type == LexToken.Type.VARIABLE && u.hijos.isEmpty();
    }

    private static boolean esConstantePorFuncion(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.MUL && n.hijos.size() == 2) {
            return esConstantePura(n.hijos.get(0)) ^ esConstantePura(n.hijos.get(1));
        }
        return false;
    }

    private static boolean esFuncionCompuesta(NodoAST n) {
        if (n == null || n.token == null) return false;
        LexToken.Type t = n.token.type;

        if (t == LexToken.Type.LN || t == LexToken.Type.LOG || t == LexToken.Type.LOG_BASE2 || t == LexToken.Type.LOG_BASE10)
            return true;

        if (t == LexToken.Type.TRIG_SIN || t == LexToken.Type.TRIG_COS || t == LexToken.Type.TRIG_TAN ||
                t == LexToken.Type.TRIG_COT || t == LexToken.Type.TRIG_SEC || t == LexToken.Type.TRIG_CSC ||
                t == LexToken.Type.TRIG_ARCSIN || t == LexToken.Type.TRIG_ARCCOS || t == LexToken.Type.TRIG_ARCTAN ||
                t == LexToken.Type.TRIG_ARCCOT || t == LexToken.Type.TRIG_ARCSEC || t == LexToken.Type.TRIG_ARCCSC) {
            if (!n.hijos.isEmpty()) {
                NodoAST u = n.hijos.get(0);
                return !(u != null && u.token != null && u.token.type == LexToken.Type.VARIABLE && u.hijos.isEmpty());
            }
            return false;
        }

        for (NodoAST h : n.hijos) if (esFuncionCompuesta(h)) return true;
        return false;
    }


    private static boolean contieneTrig(NodoAST n) {
        if (n == null || n.token == null) return false;
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
                return true;
            default:
        }
        for (NodoAST h : n.hijos) if (contieneTrig(h)) return true;
        return false;
    }

    private static boolean contieneLog(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.LOG || n.token.type == LexToken.Type.LN ||
                n.token.type == LexToken.Type.LOG_BASE2 || n.token.type == LexToken.Type.LOG_BASE10)
            return true;
        for (NodoAST h : n.hijos) if (contieneLog(h)) return true;
        return false;
    }

    private static boolean contienePiOE(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.CONST_PI || n.token.type == LexToken.Type.CONST_E)
            return true;
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

    private static boolean esConstanteRespecto(NodoAST n, String var) {
        if (n == null) return true;
        if (n.token != null && n.token.type == LexToken.Type.VARIABLE && var.equals(n.token.value)) return false;
        for (NodoAST h : n.hijos) if (!esConstanteRespecto(h, var)) return false;
        return true;
    }

    private static boolean esX2Exacto(NodoAST n, String var) {
        if (n == null || n.token == null) return false;
        if (n.token.type != LexToken.Type.EXP || n.hijos.size() != 2) return false;
        NodoAST base = n.hijos.get(0), exp = n.hijos.get(1);
        if (base == null || base.token == null || base.token.type != LexToken.Type.VARIABLE) return false;
        if (!var.equals(base.token.value)) return false;
        Double e = evaluarConstante(exp);
        return e != null && Math.abs(e - 2.0) < 1e-9;
    }

    private static boolean esKx2(NodoAST n, String var) {
        if (esX2Exacto(n, var)) return true;
        if (n != null && n.token != null && n.token.type == LexToken.Type.MUL && n.hijos.size() == 2) {
            NodoAST a = n.hijos.get(0), b = n.hijos.get(1);
            if (esConstanteRespecto(a, var) && esX2Exacto(b, var)) return true;
            if (esConstanteRespecto(b, var) && esX2Exacto(a, var)) return true;
        }
        return false;
    }

    private static boolean esConstCuadradoOConst(NodoAST n, String var) {
        if (n == null || n.token == null) return false;
        if (evaluarConstante(n) != null) return true;
        if (esConstanteRespecto(n, var)) return true;
        if (n.token.type == LexToken.Type.EXP && n.hijos.size() == 2) {
            Double e = evaluarConstante(n.hijos.get(1));
            return (e != null && Math.abs(e - 2.0) < 1e-9) && esConstanteRespecto(n.hijos.get(0), var);
        }
        return false;
    }

    private static boolean esRacionalCadenaPotencia(NodoAST n, String var) {
        if (n == null || n.token == null || n.token.type != LexToken.Type.DIV || n.hijos.size() != 2)
            return false;

        NodoAST num = n.hijos.get(0);
        NodoAST den = n.hijos.get(1);
        if (den == null || den.token == null) return false;

        if (den.token.type != LexToken.Type.SUM && den.token.type != LexToken.Type.SUB) return false;
        if (den.hijos.size() != 2) return false;

        NodoAST A = den.hijos.get(0);
        NodoAST B = den.hijos.get(1);

        NodoAST constTerm = esConstanteRespecto(A, var) ? A : (esConstanteRespecto(B, var) ? B : null);
        NodoAST monoTerm  = constTerm == A ? B : (constTerm == B ? A : null);
        if (constTerm == null || monoTerm == null) return false;

        if (!esMonomioAxnRespecto(monoTerm, var)) return false;
        int p = gradoEn(monoTerm, var);
        if (p < 2) return false;

        if (!esMonomioAxnRespecto(num, var)) return false;
        int gNum = gradoEn(num, var);
        return gNum == p - 1;
    }

    private static boolean esPatronTrigSub(NodoAST n, String var) {
        NodoAST rad = buscarNodo(n, LexToken.Type.RADICAL);
        if (rad == null || rad.hijos.size() != 1) return false;

        NodoAST inside = rad.hijos.get(0);
        if (inside == null || inside.token == null) return false;

        if (inside.token.type != LexToken.Type.SUM && inside.token.type != LexToken.Type.SUB) return false;

        NodoAST A = inside.hijos.size() > 0 ? inside.hijos.get(0) : null;
        NodoAST B = inside.hijos.size() > 1 ? inside.hijos.get(1) : null;
        if (A == null || B == null) return false;

        boolean constA  = esConstanteNoNegativa(A);
        boolean constB  = esConstanteNoNegativa(B);
        boolean sqLinA  = esCuadradoDeLineal(A, var);
        boolean sqLinB  = esCuadradoDeLineal(B, var);

        return (constA && sqLinB) || (sqLinA && constB);
    }

    private static boolean esConstanteNoNegativa(NodoAST n) {
        Double c = evaluarConstante(n);
        return c != null && c >= 0;
    }

    private static boolean esCuadradoDeLineal(NodoAST n, String var) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.EXP && n.hijos.size() == 2) {
            Double e = evaluarConstante(n.hijos.get(1));
            return e != null && Math.abs(e - 2.0) < 1e-9 && coefLineal(n.hijos.get(0), var) != null;
        }
        if (n.token.type == LexToken.Type.MUL && n.hijos.size() == 2) {
            Double cL = evaluarConstante(n.hijos.get(0));
            Double cR = evaluarConstante(n.hijos.get(1));
            NodoAST exp = (cL != null) ? n.hijos.get(1) : (cR != null ? n.hijos.get(0) : null);
            Double c = (cL != null) ? cL : cR;
            if (c != null && c > 0 && exp != null && exp.token != null &&
                    exp.token.type == LexToken.Type.EXP && exp.hijos.size() == 2) {
                Double e = evaluarConstante(exp.hijos.get(1));
                if (e != null && Math.abs(e - 2.0) < 1e-9 && coefLineal(exp.hijos.get(0), var) != null) return true;
            }
        }
        return false;
    }

    private static boolean esFraccionRacionalPolinomialProper(NodoAST n, String var) {
        if (n == null || n.token == null || n.token.type != LexToken.Type.DIV || n.hijos.size() != 2)
            return false;
        GradoPolinomio gn = gradoSiPolinomio(n.hijos.get(0), var);
        GradoPolinomio gd = gradoSiPolinomio(n.hijos.get(1), var);
        return gn.ok && gd.ok && gn.grado < gd.grado;
    }

    private static boolean existeSustitucionSimple(NodoAST n, String var) {
        if (n == null) return false;
        if (esExpDeLineal(n, var)) return true;
        if ((esSinDeLineal(n, var) || esCosDeLineal(n, var) || esTanDeLineal(n, var))
                && !esProductoDeDosFactores(n)) return true;
        if (esReciprocoDeLineal(n, var)) return true;
        if (esCadenaPotenciaConFactor(n, var)) return true;
        if (esSinDeExpLineal(n, var)) return true;
        return false;
    }

    private static boolean esExpDeLinealONull(NodoAST n, String var) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.EXP && n.hijos.size() == 2) {
            NodoAST base = n.hijos.get(0), expo = n.hijos.get(1);
            if (base != null && base.token != null && base.token.type == LexToken.Type.CONST_E) {
                Double k = coefLineal(expo, var);
                return k != null && Math.abs(k) > 1e-15;
            }
        }
        return false;
    }

    private static boolean esSinDeExpLineal(NodoAST n, String var) {
        if (n == null || n.token == null) return false;
        if (n.token.type != LexToken.Type.TRIG_SIN || n.hijos.size() != 1) return false;
        NodoAST a = n.hijos.get(0);
        if (esExpDeLinealONull(a, var)) return true;
        if (a.token != null && a.token.type == LexToken.Type.MUL && a.hijos.size() == 2) {
            NodoAST u = a.hijos.get(0), v = a.hijos.get(1);
            if (evaluarConstante(u) != null && esExpDeLinealONull(v, var)) return true;
            if (evaluarConstante(v) != null && esExpDeLinealONull(u, var)) return true;
        }
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
        if (eq == null || eq.token == null || eq.token.type != LexToken.Type.EQUAL || eq.hijos.size() != 2)
            return false;
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
        if (n.token != null && (n.token.type == LexToken.Type.EXP || n.token.type == LexToken.Type.RADICAL) && contieneVariable(n))
            return true;
        for (NodoAST h : n.hijos) if (variableDentroDeParentesisOperado(h)) return true;
        return false;
    }

    private static boolean esFormaCuadraticaIgualCero(NodoAST eq, String var) {
        if (eq == null || eq.token == null || eq.token.type != LexToken.Type.EQUAL || eq.hijos.size() != 2)
            return false;
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
        if (prod == null || prod.token == null || prod.token.type != LexToken.Type.MUL || prod.hijos.size() != 2)
            return 0;
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
            if (evaluarConstante(t.hijos.get(0)) != null && contieneVariable(t.hijos.get(1)))
                return true;
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
            if (evaluarConstante(t.hijos.get(0)) != null && contieneVariable(t.hijos.get(1)))
                return true;
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
        if (n.token != null && n.token.type == LexToken.Type.VARIABLE && var.equals(n.token.value))
            return true;
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
            try {
                return Double.parseDouble(n.token.value);
            } catch (Exception e) {
                return null;
            }
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

        GradoPolinomio(boolean ok, int grado) {
            this.ok = ok;
            this.grado = grado;
        }
    }

    private static GradoPolinomio gradoSiPolinomio(NodoAST n, String var) {
        if (n == null || n.token == null) return new GradoPolinomio(false, 0);
        LexToken.Type t = n.token.type;

        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL)
            return new GradoPolinomio(true, 0);
        if (t == LexToken.Type.VARIABLE)
            return new GradoPolinomio(var.equals(n.token.value), var.equals(n.token.value) ? 1 : 0);

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
            if (e == null || e < 0 || Math.abs(e - Math.rint(e)) > 1e-9)
                return new GradoPolinomio(false, 0);
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

        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL || t == LexToken.Type.CONST_E || t == LexToken.Type.CONST_PI)
            return 0;
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

        if (t == LexToken.Type.DIV) {
            if (n.hijos.size() != 2) return -1;
            Double den = evaluarConstante(n.hijos.get(1));
            int gnum = gradoEn(n.hijos.get(0), var);
            return (den != null) ? gnum : -1;
        }
        return -1;
    }

    private static NodoAST cloneAST(NodoAST n) {
        if (n == null) return null;
        NodoAST c = new NodoAST(
                n.token == null ? null : new LexToken(n.token.type, n.token.value, n.token.prioridad)
        );
        for (NodoAST h : n.hijos) {
            NodoAST ch = cloneAST(h);
            if (ch != null) c.addHijo(ch);
        }
        return c;
    }

    private static NodoAST resta(NodoAST a, NodoAST b) {
        NodoAST n = new NodoAST(new LexToken(LexToken.Type.SUB, "-", 5));
        n.addHijo(cloneAST(a));
        n.addHijo(cloneAST(b));
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
        if (n == null || n.token == null) return false;
        LexToken.Type t = n.token.type;

        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL || t == LexToken.Type.VARIABLE)
            return n.hijos.isEmpty();

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

        if (t == LexToken.Type.DIV || t == LexToken.Type.CONST_E || t == LexToken.Type.CONST_PI)
            return false;

        switch (t) {
            case LOG: case LN: case LOG_BASE2: case LOG_BASE10:
            case RADICAL: case FACTORIAL:
            case TRIG_SIN: case TRIG_COS: case TRIG_TAN:
            case TRIG_COT: case TRIG_SEC: case TRIG_CSC:
            case TRIG_ARCSIN: case TRIG_ARCCOS: case TRIG_ARCTAN:
            case TRIG_ARCCOT: case TRIG_ARCSEC: case TRIG_ARCCSC:
            case DERIV: case INTEGRAL_DEF: case INTEGRAL_INDEF:
            case ABS: case IMAGINARY:
                return false;
            default:
        }

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
