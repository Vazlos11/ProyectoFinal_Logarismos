package com.example.a22100213_proyectointegrador_logarismos.Semantico;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AnalisisSemantico {

    public static ResultadoSemantico analizar(NodoAST raiz) {
        ResultadoSemantico rs = new ResultadoSemantico();
        if (raiz == null || raiz.token == null) {
            rs.tipoPrincipal = TipoExpresion.T2_ALGEBRA_FUNC;
            validarDominiosGenerales(rs, raiz);
            llenarSubtipos(rs, raiz);
            return rs;
        }
        if (contiene(raiz, LexToken.Type.DERIV) || contiene(raiz, LexToken.Type.PRIME)) {
            NodoAST d = localizarDeriv(raiz);
            rs.tipoPrincipal = TipoExpresion.T3_DERIVADA;
            validarDerivada(rs, d);
            validarImaginarios(rs, raiz);
            validarDominiosGenerales(rs, raiz);
            llenarSubtipos(rs, raiz);
            return rs;
        }
        if (contiene(raiz, LexToken.Type.INTEGRAL_DEF)) {
            rs.tipoPrincipal = TipoExpresion.T5_INTEGRAL_DEFINIDA;
            validarIntegrales(rs, raiz, true);
            validarImaginarios(rs, raiz);
            validarDominiosGenerales(rs, raiz);
            llenarSubtipos(rs, raiz);
            return rs;
        }
        if (contiene(raiz, LexToken.Type.INTEGRAL_INDEF)) {
            rs.tipoPrincipal = TipoExpresion.T4_INTEGRAL_INDEFINIDA;
            validarIntegrales(rs, raiz, false);
            validarImaginarios(rs, raiz);
            validarDominiosGenerales(rs, raiz);
            llenarSubtipos(rs, raiz);
            return rs;
        }
        if (raiz.token.type == LexToken.Type.SYSTEM || contarEcuaciones(raiz) > 1) {
            rs.tipoPrincipal = TipoExpresion.T8_SISTEMA_EC;
            validarSistema(rs, raiz);
            validarSistemaGrado(rs, raiz);
            validarImaginarios(rs, raiz);
            validarDominiosGenerales(rs, raiz);
            llenarSubtipos(rs, raiz);
            return rs;
        }
        if (raiz.token.type == LexToken.Type.EQUAL || contiene(raiz, LexToken.Type.EQUAL)) {
            Set<String> vs = varsNoI(raiz);
            String v = vs.isEmpty() ? "x" : vs.iterator().next();
            int g = 0;
            boolean todasPoli = true;

            for (NodoAST e : ecuaciones(raiz)) {
                NodoAST L = sub(e, 0), R = sub(e, 1);
                g = Math.max(g, Math.max(gradoEn(L, v), gradoEn(R, v)));
                todasPoli &= esPolinomicaEn(L, v) && esPolinomicaEn(R, v);
            }

            if (vs.size() == 1 && g == 1 && !contieneAlguno(raiz, tiposFunciones())) {
                rs.tipoPrincipal = TipoExpresion.T6_DESPEJE_LINEAL;
                validarLineal(rs, raiz);
            } else if (vs.size() == 1 && g >= 2 && g < Integer.MAX_VALUE && todasPoli && coefEnteros(raiz)) {
                rs.tipoPrincipal = TipoExpresion.T7_DESPEJE_POLINOMICO;
                validarPolinomico(rs, raiz);
            } else {
                rs.tipoPrincipal = TipoExpresion.T2_ALGEBRA_FUNC;
                validarT2(rs, raiz);
            }

            validarImaginarios(rs, raiz);
            validarDominiosGenerales(rs, raiz);
            llenarSubtipos(rs, raiz);
            return rs;
        }
        if (contiene(raiz, LexToken.Type.IMAGINARY)) {
            rs.tipoPrincipal = TipoExpresion.T9_IMAGINARIOS;
            validarImaginarios(rs, raiz);
            validarDominiosGenerales(rs, raiz);
            llenarSubtipos(rs, raiz);
            return rs;
        }
        if (esT1Aritmetica(raiz)) {
            rs.tipoPrincipal = TipoExpresion.T1_ARITMETICA;
            validarT1(rs, raiz);
            validarDominiosGenerales(rs, raiz);
            llenarSubtipos(rs, raiz);
            return rs;
        }
        rs.tipoPrincipal = TipoExpresion.T2_ALGEBRA_FUNC;
        validarT2(rs, raiz);
        validarImaginarios(rs, raiz);
        validarDominiosGenerales(rs, raiz);
        llenarSubtipos(rs, raiz);
        return rs;
    }
    private static boolean contiene(NodoAST n, LexToken.Type t) {
        if (n == null) return false;
        if (n.token != null && n.token.type == t) return true;
        for (NodoAST h : n.hijos) if (contiene(h, t)) return true;
        return false;
    }
    private static NodoAST localizarDeriv(NodoAST n) {
        if (n == null) return null;
        if (n.token != null && (n.token.type == LexToken.Type.DERIV || n.token.type == LexToken.Type.PRIME)) return n;
        for (NodoAST h : n.hijos) {
            NodoAST r = localizarDeriv(h);
            if (r != null) return r;
        }
        return null;
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
        s.add(LexToken.Type.ABS);
        return s;
    }
    private static void validarT1(ResultadoSemantico rs, NodoAST r) {
        if (!esT1Aritmetica(r)) rs.errores.add(SemanticoError.error("T1_TOK", "Sólo números y +,-,*,/,√,^,= permitidos", ruta(r)));
    }

    private static void validarT2(ResultadoSemantico rs, NodoAST r) {
        if (!funcionesConArgumentoValidas(r))
            rs.errores.add(SemanticoError.error("T2_ARG", "Funciones con argumento inválido", ruta(r)));
        if (!parentesisBalanceados(r))
            rs.errores.add(SemanticoError.error("T2_PARENS", "Paréntesis no balanceados", ruta(r)));
    }

    private static void validarLineal(ResultadoSemantico rs, NodoAST r) {
        List<NodoAST> eqs = ecuaciones(r);
        if (eqs.isEmpty()) rs.errores.add(SemanticoError.error("T6_EQ", "Debe ser ecuación con '='", ruta(r)));
        Set<String> vs = varsNoI(r);
        if (vs.size()!=1) rs.errores.add(SemanticoError.error("T6_VAR", "Una sola incógnita", ruta(r)));
        String v = vs.isEmpty() ? "x" : vs.iterator().next();
        for (NodoAST e: eqs) {
            int g = Math.max(gradoEn(sub(e,0), v), gradoEn(sub(e,1), v));
            if (g!=1) rs.errores.add(SemanticoError.error("T6_GRADO", "Grado igual a 1", ruta(e)));
            if (contieneAlguno(e, tiposFunciones())) rs.errores.add(SemanticoError.error("T6_FUNC", "Sin funciones trig/log", ruta(e)));
        }
    }

    private static void validarPolinomico(ResultadoSemantico rs, NodoAST r) {
        List<NodoAST> eqs = ecuaciones(r);
        if (eqs.isEmpty()) rs.errores.add(SemanticoError.error("T7_EQ", "Debe ser ecuación con '='", ruta(r)));
        Set<String> vs = varsNoI(r);
        if (vs.size()!=1) rs.errores.add(SemanticoError.error("T7_VAR", "Una sola incógnita", ruta(r)));
        String v = vs.isEmpty() ? "x" : vs.iterator().next();
        for (NodoAST e: eqs) {
            if (contieneAlguno(e, tiposFunciones())) rs.errores.add(SemanticoError.error("T7_FUNC", "Sin funciones", ruta(e)));
            if (!esPolinomicaEn(sub(e,0), v) || !esPolinomicaEn(sub(e,1), v)) rs.errores.add(SemanticoError.error("T7_POLI", "Debe ser polinómica", ruta(e)));
            if (!coefEnteros(e)) rs.errores.add(SemanticoError.error("T7_COEF", "Coeficientes enteros", ruta(e)));
            int g = Math.max(gradoEn(sub(e,0), v), gradoEn(sub(e,1), v));
            if (g < 2 || g == Integer.MAX_VALUE) rs.errores.add(SemanticoError.error("T7_GRADO", "Grado ≥ 2", ruta(e)));
        }
    }

    private static void validarSistemaGrado(ResultadoSemantico rs, NodoAST r) {
        List<NodoAST> eqs = ecuaciones(r);
        Set<String> vs = new LinkedHashSet<>(varsNoI(r));
        String v = vs.isEmpty() ? "x" : vs.iterator().next();
        for (NodoAST e: eqs) {
            if (!(e.token != null && e.token.type == LexToken.Type.EQUAL)) rs.errores.add(SemanticoError.error("T8_EQ", "Cada fila debe ser una ecuación", ruta(e)));
            int g = Math.max(gradoEn(sub(e,0), v), gradoEn(sub(e,1), v));
            if (g != 1) rs.errores.add(SemanticoError.error("T8_GRADO", "Todas de primer grado", ruta(e)));
        }
    }

    private static boolean funcionesConArgumentoValidas(NodoAST n) {
        if (n == null || n.token == null) return true;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.LOG || t == LexToken.Type.LN ||
                t == LexToken.Type.TRIG_SIN || t == LexToken.Type.TRIG_COS || t == LexToken.Type.TRIG_TAN ||
                t == LexToken.Type.TRIG_COT || t == LexToken.Type.TRIG_SEC || t == LexToken.Type.TRIG_CSC ||
                t == LexToken.Type.TRIG_ARCSIN || t == LexToken.Type.TRIG_ARCCOS || t == LexToken.Type.TRIG_ARCTAN ||
                t == LexToken.Type.TRIG_ARCCOT || t == LexToken.Type.TRIG_ARCSEC || t == LexToken.Type.TRIG_ARCCSC) {
            if (n.hijos.isEmpty()) return false;
        }
        if (t == LexToken.Type.LOG_BASE2 || t == LexToken.Type.LOG_BASE10) {
            if (n.hijos.isEmpty()) return false;
        }
        for (NodoAST h: n.hijos) if (!funcionesConArgumentoValidas(h)) return false;
        return true;
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

        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL
                || t == LexToken.Type.CONST_E || t == LexToken.Type.CONST_PI)
            return true;

        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB
                || t == LexToken.Type.MUL || t == LexToken.Type.DIV
                || t == LexToken.Type.EXP
                || t == LexToken.Type.PAREN_OPEN || t == LexToken.Type.PAREN_CLOSE) {
            for (NodoAST h : n.hijos) if (!soloArit(h)) return false;
            return true;
        }

        return false;
    }

    private static void validarDominiosGenerales(ResultadoSemantico rs, NodoAST n) {
        if (n == null || n.token == null) return;
        LexToken.Type t = n.token.type;

        if (t == LexToken.Type.DIV && n.hijos.size() == 2) {
            Double den = evalConst(sub(n,1));
            if (den != null && Math.abs(den) < 1e-15)
                rs.errores.add(SemanticoError.error("DIV0", "División entre cero", ruta(n)));
        }

        if (t == LexToken.Type.RADICAL && n.hijos.size() == 1) {
            Double a = evalConst(sub(n,0));
            if (a != null && a < 0)
                rs.errores.add(SemanticoError.error("RAD_NEG", "Raíz con argumento negativo", ruta(n)));
        }

        if ((t == LexToken.Type.LN || t == LexToken.Type.LOG
                || t == LexToken.Type.LOG_BASE2 || t == LexToken.Type.LOG_BASE10) && n.hijos.size() == 1) {
            Double a = evalConst(sub(n,0));
            if (a != null && a <= 0)
                rs.errores.add(SemanticoError.error("LOG_DOM", "Logaritmo con argumento ≤ 0", ruta(n)));
        }

        if ((t == LexToken.Type.TRIG_ARCSIN || t == LexToken.Type.TRIG_ARCCOS) && n.hijos.size() == 1) {
            Double a = evalConst(sub(n,0));
            if (a != null && (a < -1.0 || a > 1.0)) {
                String code = (t == LexToken.Type.TRIG_ARCSIN) ? "ASIN_DOM" : "ACOS_DOM";
                String msg  = (t == LexToken.Type.TRIG_ARCSIN) ? "arcsin fuera de dominio [-1,1]" : "arccos fuera de dominio [-1,1]";
                rs.errores.add(SemanticoError.error(code, msg, ruta(n)));
            }
        }

        if ((t == LexToken.Type.TRIG_TAN || t == LexToken.Type.TRIG_COT) && n.hijos.size() == 1) {
            Double a = evalConst(sub(n,0));
            if (a != null) {
                double c = Math.cos(a);
                double s = Math.sin(a);
                if (t == LexToken.Type.TRIG_TAN && Math.abs(c) < 1e-15)
                    rs.errores.add(SemanticoError.error("TAN_UNDEF", "tan indefinida para ese argumento", ruta(n)));
                if (t == LexToken.Type.TRIG_COT && Math.abs(s) < 1e-15)
                    rs.errores.add(SemanticoError.error("COT_UNDEF", "cot indefinida para ese argumento", ruta(n)));
            }
        }

        for (NodoAST h : n.hijos) validarDominiosGenerales(rs, h);
    }

    private static void validarDominiosArit(ResultadoSemantico rs, NodoAST n) {
        if (n == null) return;
        if (n.token != null && n.token.type == LexToken.Type.RADICAL && n.hijos.size() == 1) {
            Double v = evalConst(n.hijos.get(0));
            if (v != null && v < 0) rs.errores.add(SemanticoError.error("T1_RAD", "Raíz con argumento negativo", ruta(n)));
        }
        for (NodoAST h : n.hijos) validarDominiosArit(rs, h);
    }

    private static void validarAlgFunc(ResultadoSemantico rs, NodoAST n) {
        if (!parentesisBalanceados(n)) rs.errores.add(SemanticoError.error("T2_PARENS", "Paréntesis no balanceados", ruta(n)));
    }

    private static void validarDerivada(ResultadoSemantico rs, NodoAST r) {
        NodoAST fun = sub(r,0);
        NodoAST dif = sub(r,1);
        String v = "x";
        if (dif != null && dif.token != null && dif.token.type == LexToken.Type.DIFFERENTIAL) {
            String dv = dif.token.value == null ? "" : dif.token.value.trim();
            v = (dv.length() >= 2 && (dv.charAt(0) == 'd' || dv.charAt(0) == 'D')) ? dv.substring(1) : "x";
        } else if (r.token != null && r.token.type == LexToken.Type.DERIV && r.token.value != null && !r.token.value.isEmpty()) {
            v = r.token.value;
        }
        java.util.Set<String> fv = varsNoI(fun);
        if (!fv.isEmpty() && !fv.contains(v)) rs.errores.add(SemanticoError.error("T3_VAR_DX", "Variable del diferencial no coincide", ruta(r)));
        rs.varIndep = v;
    }

    private static void validarIntegrales(ResultadoSemantico rs, NodoAST r, boolean definida) {
        IntegralInfo ii = localizarIntegral(r, definida);
        if (ii == null) return;
        if (ii.dif == null || ii.dif.token == null || !ii.dif.token.type.name().equals("DIFFERENTIAL")) rs.errores.add(SemanticoError.error(definida?"T5_DX":"T4_DX", "Falta diferencial", ruta(r)));
        else {
            String v = extraerDxVar(ii.dif.token.value);
            Set<String> vs = varsNoI(ii.cuerpo);
            if (!vs.isEmpty() && !vs.contains(v)) rs.errores.add(SemanticoError.error(definida?"T5_VAR_DX":"T4_VAR_DX", "Variable del diferencial no coincide", ruta(r)));
            rs.varIndep = v;
        }
        if (definida) {
            Double a = evalConst(ii.inf);
            Double b = evalConst(ii.sup);
            if (a == null || b == null) rs.errores.add(SemanticoError.error("T5_LIMS", "Límites no constantes", ruta(r)));
            else if (Math.abs(a - b) < 1e-15) rs.errores.add(SemanticoError.error("T5_LIMS_IGUALES", "Límites iguales", ruta(r)));
        }
    }

    private static void validarSistema(ResultadoSemantico rs, NodoAST r) {
        List<NodoAST> eqs = ecuaciones(r);
        if (eqs.size() < 2 || eqs.size() > 3) rs.errores.add(SemanticoError.error("T8_CANT", "El sistema debe tener 2 o 3 ecuaciones", ruta(r)));
        Set<String> vs = new LinkedHashSet<>();
        for (NodoAST e : eqs) vs.addAll(varsNoI(e));
        if (vs.size() > eqs.size()) rs.errores.add(SemanticoError.error("T8_VAR", "Variables exceden a ecuaciones", ruta(r)));
        for (NodoAST eq : eqs) if (contieneAlguno(eq, tiposFunciones())) rs.errores.add(SemanticoError.error("T8_FUNC", "Solo lineales sin funciones", ruta(eq)));
    }

    private static void validarImaginarios(ResultadoSemantico rs, NodoAST r) {
        if (contiene(r, LexToken.Type.IMAGINARY) && (contiene(r, LexToken.Type.DERIV) || contiene(r, LexToken.Type.PRIME) || contiene(r, LexToken.Type.INTEGRAL_DEF) || contiene(r, LexToken.Type.INTEGRAL_INDEF))) {
            rs.errores.add(SemanticoError.error("T9_MEZCLA", "No mezclar i con derivadas o integrales", ruta(r)));
        }
    }

    private static void llenarSubtipos(ResultadoSemantico rs, NodoAST r) {
        if (contieneAlguno(r, tiposFunciones())) {
            rs.subtipos.add(TipoExpresion.ST_TRIGONOMETRICA);
            rs.subtipos.add(TipoExpresion.ST_EXPONENCIAL_LOG);
        }
        if (contiene(r, LexToken.Type.DIV)) rs.subtipos.add(TipoExpresion.ST_RACIONAL);
        if (contiene(r, LexToken.Type.RADICAL)) rs.subtipos.add(TipoExpresion.ST_RADICAL);
        if (esPolinomica(r)) rs.subtipos.add(TipoExpresion.ST_POLINOMICA);
    }

    private static List<Integer> ruta(NodoAST n) {
        List<Integer> p = new ArrayList<>();
        Deque<Integer> st = new ArrayDeque<>();
        NodoAST cur = n;
        while (cur != null && cur.parent != null) {
            int idx = cur.parent.hijos.indexOf(cur);
            st.push(idx);
            cur = cur.parent;
        }
        while (!st.isEmpty()) p.add(st.pop());
        return p;
    }

    private static String extraerDxVar(String dx) {
        if (dx == null) return "x";
        String s = dx.trim();
        if (s.startsWith("d") || s.startsWith("D")) {
            String t = s.substring(1).trim();
            if (!t.isEmpty()) return t;
        }
        return "x";
    }

    private static class IntegralInfo {
        NodoAST nodo;
        boolean definida;
        NodoAST inf;
        NodoAST sup;
        NodoAST cuerpo;
        NodoAST dif;
    }
    private static int contarEcuaciones(NodoAST n) {
        return ecuaciones(n).size();
    }

    private static boolean parentesisBalanceados(NodoAST n) {
        return true;
    }
    private static IntegralInfo localizarIntegral(NodoAST n, boolean def) {
        if (n == null) return null;
        if (!def && n.token != null && n.token.type == LexToken.Type.INTEGRAL_INDEF) {
            IntegralInfo ii = new IntegralInfo();
            ii.nodo = n;
            ii.definida = false;
            ii.cuerpo = sub(n,0);
            ii.dif = sub(n,1);
            return ii;
        }
        if (def && n.token != null && n.token.type == LexToken.Type.INTEGRAL_DEF) {
            IntegralInfo ii = new IntegralInfo();
            ii.nodo = n;
            ii.definida = true;
            ii.inf = sub(n,0);
            ii.sup = sub(n,1);
            ii.cuerpo = sub(n,2);
            ii.dif = sub(n,3);
            return ii;
        }
        for (NodoAST h : n.hijos) {
            IntegralInfo x = localizarIntegral(h, def);
            if (x != null) return x;
        }
        return null;
    }

    private static NodoAST sub(NodoAST n, int i) { return (n != null && i >= 0 && i < n.hijos.size()) ? n.hijos.get(i) : null; }

    private static Double evalConst(NodoAST n) {
        if (n == null || n.token == null) return null;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL) {
            try { return Double.valueOf(n.token.value); } catch (Exception e) { return null; }
        }
        if (t == LexToken.Type.CONST_E) return Math.E;
        if (t == LexToken.Type.CONST_PI) return Math.PI;
        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB || t == LexToken.Type.MUL || t == LexToken.Type.DIV || t == LexToken.Type.EXP) {
            if (n.hijos.size() != 2) return null;
            Double a = evalConst(sub(n,0));
            Double b = evalConst(sub(n,1));
            if (a == null || b == null) return null;
            switch (t) {
                case SUM: return a + b;
                case SUB: return a - b;
                case MUL: return a * b;
                case DIV: return Math.abs(b) < 1e-15 ? null : a / b;
                case EXP: return Math.pow(a, b);
            }
        }
        if (t == LexToken.Type.RADICAL && n.hijos.size() == 1) {
            Double a = evalConst(sub(n,0));
            if (a == null || a < 0) return null;
            return Math.sqrt(a);
        }
        return null;
    }

    private static Set<String> varsNoI(NodoAST n) {
        Set<String> s = new LinkedHashSet<>();
        if (n == null) return s;
        if (n.token != null && n.token.type == LexToken.Type.VARIABLE && n.hijos.isEmpty() && n.token.value != null && !n.token.value.equalsIgnoreCase("i")) {
            s.add(n.token.value);
        }
        for (NodoAST h : n.hijos) s.addAll(varsNoI(h));
        return s;
    }

    private static List<NodoAST> ecuaciones(NodoAST n) {
        List<NodoAST> out = new ArrayList<>();
        collectEq(n, out);
        return out;
    }

    private static void collectEq(NodoAST n, List<NodoAST> out) {
        if (n == null) return;
        if (n.token != null && n.token.type == LexToken.Type.EQUAL && n.hijos.size() == 2) out.add(n);
        for (NodoAST h : n.hijos) collectEq(h, out);
    }

    private static int gradoEn(NodoAST n, String var) {
        if (n == null) return 0;
        if (n.token != null && n.token.type == LexToken.Type.VARIABLE && var.equals(n.token.value) && n.hijos.isEmpty()) return 1;
        if (n.token != null && (n.token.type == LexToken.Type.INTEGER || n.token.type == LexToken.Type.DECIMAL || n.token.type == LexToken.Type.CONST_E || n.token.type == LexToken.Type.CONST_PI)) return 0;
        if (n.token == null) return 0;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB) {
            int a = gradoEn(sub(n,0), var);
            int b = gradoEn(sub(n,1), var);
            return Math.max(a, b);
        }
        if (t == LexToken.Type.MUL) {
            int a = gradoEn(sub(n,0), var);
            int b = gradoEn(sub(n,1), var);
            return a + b;
        }
        if (t == LexToken.Type.EXP) {
            Double e = evalConst(sub(n,1));
            if (e == null) return Integer.MAX_VALUE;
            int g = gradoEn(sub(n,0), var);
            if (g == 0) return 0;
            double ee = Math.rint(e);
            if (Math.abs(ee - e) > 1e-9 || e < 0) return Integer.MAX_VALUE;
            return (int) (g * ee);
        }
        if (t == LexToken.Type.DIV) {
            int a = gradoEn(sub(n,0), var);
            int b = gradoEn(sub(n,1), var);
            if (b != 0) return Integer.MAX_VALUE;
            return a;
        }
        if (t == LexToken.Type.RADICAL || t == LexToken.Type.LOG || t == LexToken.Type.LN || t == LexToken.Type.LOG_BASE2 || t == LexToken.Type.LOG_BASE10 ||
                t == LexToken.Type.TRIG_SIN || t == LexToken.Type.TRIG_COS || t == LexToken.Type.TRIG_TAN || t == LexToken.Type.TRIG_COT || t == LexToken.Type.TRIG_SEC || t == LexToken.Type.TRIG_CSC ||
                t == LexToken.Type.TRIG_ARCSIN || t == LexToken.Type.TRIG_ARCCOS || t == LexToken.Type.TRIG_ARCTAN || t == LexToken.Type.TRIG_ARCCOT || t == LexToken.Type.TRIG_ARCSEC || t == LexToken.Type.TRIG_ARCCSC) {
            return Integer.MAX_VALUE;
        }
        int g = 0;
        for (NodoAST h : n.hijos) g = Math.max(g, gradoEn(h, var));
        return g;
    }

    private static boolean esPolinomicaEn(NodoAST n, String var) {
        if (n == null || n.token == null) return false;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL || t == LexToken.Type.CONST_E || t == LexToken.Type.CONST_PI || (t == LexToken.Type.VARIABLE && var.equals(n.token.value) && n.hijos.isEmpty()))
            return true;
        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB || t == LexToken.Type.MUL) {
            for (NodoAST h : n.hijos) if (!esPolinomicaEn(h, var)) return false;
            return true;
        }
        if (t == LexToken.Type.EXP) {
            Double e = evalConst(sub(n,1));
            if (e == null) return false;
            double r = Math.rint(e);
            if (Math.abs(r - e) > 1e-9 || e < 0) return false;
            return esPolinomicaEn(sub(n,0), var);
        }
        if (t == LexToken.Type.EQUAL) {
            return esPolinomicaEn(sub(n,0), var) && esPolinomicaEn(sub(n,1), var);
        }
        if (t == LexToken.Type.DIV) {
            int gden = gradoEn(sub(n,1), var);
            if (gden != 0) return false;
            return esPolinomicaEn(sub(n,0), var);
        }
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
            Double e = evalConst(n.hijos.get(1));
            return e != null && e >= 0 && Math.abs(e - Math.rint(e)) < 1e-9;
        }

        switch (t) {
            case DIV: case CONST_E: case CONST_PI:
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

    private static boolean coefEnteros(NodoAST n) {
        if (n == null || n.token == null) return true;
        if (n.token.type == LexToken.Type.DECIMAL) {
            try {
                double v = Double.parseDouble(n.token.value);
                double r = Math.rint(v);
                if (Math.abs(v - r) > 1e-9) return false;
            } catch (Exception e) { return false; }
        }
        if (n.token.type == LexToken.Type.CONST_E || n.token.type == LexToken.Type.CONST_PI) return false;
        boolean ok = true;
        for (NodoAST h : n.hijos) ok &= coefEnteros(h);
        return ok;
    }
}
