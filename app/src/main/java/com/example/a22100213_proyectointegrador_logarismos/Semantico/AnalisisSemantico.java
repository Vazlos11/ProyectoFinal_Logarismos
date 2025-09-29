package com.example.a22100213_proyectointegrador_logarismos.Semantico;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AnalisisSemantico {

    private static final EnumSet<LexToken.Type> NUM = EnumSet.of(
            LexToken.Type.INTEGER, LexToken.Type.DECIMAL, LexToken.Type.CONST_E, LexToken.Type.CONST_PI
    );

    private static final EnumSet<LexToken.Type> VAR = EnumSet.of(
            LexToken.Type.VARIABLE
    );

    private static final EnumSet<LexToken.Type> BIN = EnumSet.of(
            LexToken.Type.SUM, LexToken.Type.SUB, LexToken.Type.MUL, LexToken.Type.DIV, LexToken.Type.EXP, LexToken.Type.EQUAL
    );

    private static final EnumSet<LexToken.Type> TRIG = EnumSet.of(
            LexToken.Type.TRIG_SIN, LexToken.Type.TRIG_COS, LexToken.Type.TRIG_TAN,
            LexToken.Type.TRIG_SEC, LexToken.Type.TRIG_CSC, LexToken.Type.TRIG_COT
    );

    private static final EnumSet<LexToken.Type> LOGS = EnumSet.of(
            LexToken.Type.LOG, LexToken.Type.LN, LexToken.Type.LOG_BASE10, LexToken.Type.LOG_BASE2
    );

    private static final EnumSet<LexToken.Type> UNARY = EnumSet.of(
            LexToken.Type.RADICAL
    );

    private static boolean isNum(NodoAST n) {
        if (n == null || n.token == null) return false;
        return NUM.contains(n.token.type) && n.hijos.isEmpty();
    }

    private static boolean isVar(NodoAST n) {
        if (n == null || n.token == null) return false;
        return VAR.contains(n.token.type) && n.hijos.isEmpty();
    }

    private static boolean isImagI(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (n.token.type.name().equals("IMAGINARY")) return true;
        if (isVar(n) && n.token.value != null) {
            String v = n.token.value.trim().toLowerCase(Locale.ROOT);
            return v.equals("i");
        }
        return false;
    }

    private static boolean isBinary(NodoAST n, LexToken.Type t) {
        return n != null && n.token != null && n.token.type == t && n.hijos.size() == 2;
    }

    private static boolean isUnaryFunc(NodoAST n, EnumSet<LexToken.Type> set) {
        return n != null && n.token != null && set.contains(n.token.type) && n.hijos.size() == 1;
    }

    private static boolean isFunc(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (TRIG.contains(n.token.type) || LOGS.contains(n.token.type)) return n.hijos.size() == 1;
        if (n.token.type == LexToken.Type.RADICAL) return n.hijos.size() == 1;
        return false;
    }

    private static boolean contains(NodoAST n, LexToken.Type t) {
        if (n == null) return false;
        if (n.token != null && n.token.type == t) return true;
        for (NodoAST h : n.hijos) if (contains(h, t)) return true;
        return false;
    }

    private static int countType(NodoAST n, LexToken.Type t) {
        if (n == null) return 0;
        int c = (n.token != null && n.token.type == t) ? 1 : 0;
        for (NodoAST h : n.hijos) c += countType(h, t);
        return c;
    }

    private static Set<String> variables(NodoAST n) {
        Set<String> s = new LinkedHashSet<>();
        collectVars(n, s, true);
        return s;
    }

    private static Set<String> variablesNoI(NodoAST n) {
        Set<String> s = new LinkedHashSet<>();
        collectVars(n, s, false);
        return s;
    }

    private static void collectVars(NodoAST n, Set<String> out, boolean includeI) {
        if (n == null) return;
        if (isVar(n) && n.token.value != null) {
            String v = n.token.value.trim();
            if (includeI || !v.equalsIgnoreCase("i")) out.add(v);
        }
        if (n.token != null && n.token.type.name().equals("IMAGINARY")) out.add("i");
        for (NodoAST h : n.hijos) collectVars(h, out, includeI);
    }

    private static boolean hasTrigOrLog(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (TRIG.contains(n.token.type) || LOGS.contains(n.token.type)) return true;
        for (NodoAST h : n.hijos) if (hasTrigOrLog(h)) return true;
        return false;
    }

    private static boolean onlyAllowedAritOps(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (NUM.contains(n.token.type) || VAR.contains(n.token.type)) return n.hijos.isEmpty();
        if (BIN.contains(n.token.type) || UNARY.contains(n.token.type)) {
            for (NodoAST h : n.hijos) if (!onlyAllowedAritOps(h)) return false;
            return true;
        }
        if (n.token.type == LexToken.Type.PAREN_OPEN || n.token.type == LexToken.Type.PAREN_CLOSE) return true;
        return false;
    }

    private static Double evalConst(NodoAST n) {
        if (n == null || n.token == null) return null;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.INTEGER) {
            try { return Double.valueOf(n.token.value); } catch (Exception e) { return null; }
        }
        if (t == LexToken.Type.DECIMAL) {
            try { return Double.valueOf(n.token.value); } catch (Exception e) { return null; }
        }
        if (t == LexToken.Type.CONST_E) return Math.E;
        if (t == LexToken.Type.CONST_PI) return Math.PI;
        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB || t == LexToken.Type.MUL || t == LexToken.Type.DIV || t == LexToken.Type.EXP) {
            if (n.hijos.size() != 2) return null;
            Double a = evalConst(n.hijos.get(0));
            Double b = evalConst(n.hijos.get(1));
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
            Double a = evalConst(n.hijos.get(0));
            if (a == null) return null;
            if (a < 0) return null;
            return Math.sqrt(a);
        }
        return null;
    }

    private static boolean radicalArgNoNegativos(NodoAST n, List<SemanticoError> errs) {
        if (n == null) return true;
        if (n.token != null && n.token.type == LexToken.Type.RADICAL && n.hijos.size() == 1) {
            Double v = evalConst(n.hijos.get(0));
            if (v != null && v < 0) {
                errs.add(SemanticoError.error("RAD_ARG_NEG", "Raíz con argumento negativo", ruta(n)));
                return false;
            }
        }
        boolean ok = true;
        for (NodoAST h : n.hijos) ok &= radicalArgNoNegativos(h, errs);
        return ok;
    }

    private static boolean funcionesConArgumento(NodoAST n, List<SemanticoError> errs) {
        if (n == null) return true;
        if (isFunc(n)) {
            if (n.hijos.size() != 1) {
                errs.add(SemanticoError.error("FUNC_ARG", "Función sin argumento válido", ruta(n)));
                return false;
            }
        }
        boolean ok = true;
        for (NodoAST h : n.hijos) ok &= funcionesConArgumento(h, errs);
        return ok;
    }

    private static boolean parentesisBalanceados(NodoAST n) {
        if (n == null) return true;
        int opens = countType(n, LexToken.Type.PAREN_OPEN);
        int closes = countType(n, LexToken.Type.PAREN_CLOSE);
        return opens == closes;
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

    private static NodoAST childOrNull(NodoAST n, int i) {
        if (n == null) return null;
        if (i < 0 || i >= n.hijos.size()) return null;
        return n.hijos.get(i);
    }

    private static boolean isDerivRaiz(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (n.token.type == LexToken.Type.DERIV) return true;
        if (n.token.type == LexToken.Type.PRIME) return true;
        return false;
    }

    private static boolean eqPresente(NodoAST n) {
        return countType(n, LexToken.Type.EQUAL) >= 1;
    }

    private static boolean eqMultiples(NodoAST n) {
        return countType(n, LexToken.Type.EQUAL) >= 2;
    }

    private static boolean contieneIntegralIndef(NodoAST n) {
        return contains(n, LexToken.Type.INTEGRAL_INDEF);
    }

    private static boolean contieneIntegralDef(NodoAST n) {
        return contains(n, LexToken.Type.INTEGRAL_DEF);
    }

    private static Set<String> varsDeEc(NodoAST eq) {
        Set<String> s = variablesNoI(eq);
        return s;
    }

    private static int gradoEn(NodoAST n, String var) {
        if (n == null) return 0;
        if (isVar(n) && n.token.value != null && n.token.value.equals(var)) return 1;
        if (isNum(n)) return 0;
        if (n.token == null) return 0;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB) {
            int a = gradoEn(childOrNull(n, 0), var);
            int b = gradoEn(childOrNull(n, 1), var);
            return Math.max(a, b);
        }
        if (t == LexToken.Type.MUL) {
            int a = gradoEn(childOrNull(n, 0), var);
            int b = gradoEn(childOrNull(n, 1), var);
            return a + b;
        }
        if (t == LexToken.Type.EXP) {
            NodoAST base = childOrNull(n, 0);
            NodoAST ex = childOrNull(n, 1);
            Double ev = evalConst(ex);
            if (ev == null) return Integer.MAX_VALUE;
            int g = gradoEn(base, var);
            if (g == 0) return 0;
            double ee = Math.rint(ev);
            if (Math.abs(ee - ev) > 1e-9) return Integer.MAX_VALUE;
            if (ev < 0) return Integer.MAX_VALUE;
            return (int) (g * ee);
        }
        if (t == LexToken.Type.DIV) {
            int a = gradoEn(childOrNull(n, 0), var);
            int b = gradoEn(childOrNull(n, 1), var);
            if (b != 0) return Integer.MAX_VALUE;
            return a;
        }
        if (TRIG.contains(t) || LOGS.contains(t) || t == LexToken.Type.RADICAL) return Integer.MAX_VALUE;
        int g = 0;
        for (NodoAST h : n.hijos) g = Math.max(g, gradoEn(h, var));
        return g;
    }

    private static boolean esPolinomicaEn(NodoAST n, String var) {
        if (n == null || n.token == null) return false;
        LexToken.Type t = n.token.type;
        if (isNum(n)) return true;
        if (isVar(n) && var.equals(n.token.value)) return true;
        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB || t == LexToken.Type.MUL) {
            for (NodoAST h : n.hijos) if (!esPolinomicaEn(h, var)) return false;
            return true;
        }
        if (t == LexToken.Type.EXP) {
            Double e = evalConst(childOrNull(n, 1));
            if (e == null) return false;
            double r = Math.rint(e);
            if (Math.abs(r - e) > 1e-9) return false;
            if (e < 0) return false;
            return esPolinomicaEn(childOrNull(n, 0), var);
        }
        if (t == LexToken.Type.DIV) {
            NodoAST den = childOrNull(n, 1);
            int gden = gradoEn(den, var);
            if (gden != 0) return false;
            return esPolinomicaEn(childOrNull(n, 0), var);
        }
        if (TRIG.contains(t) || LOGS.contains(t) || t == LexToken.Type.RADICAL) return false;
        return false;
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

    private static boolean todasGradoUno(List<NodoAST> eqs, Set<String> vars) {
        for (NodoAST eq : eqs) {
            NodoAST L = childOrNull(eq, 0);
            NodoAST R = childOrNull(eq, 1);
            for (String v : vars) {
                int g = Math.max(gradoEn(L, v), gradoEn(R, v));
                if (g > 1) return false;
            }
        }
        return true;
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

    private static boolean contieneImaginarios(NodoAST n) {
        if (n == null) return false;
        if (isImagI(n)) return true;
        if (n.token != null && n.token.type.name().equals("IMAGINARY")) return true;
        for (NodoAST h : n.hijos) if (contieneImaginarios(h)) return true;
        return false;
    }

    private static boolean contieneDerivOIntegral(NodoAST n) {
        if (n == null) return false;
        if (n.token != null) {
            LexToken.Type t = n.token.type;
            if (t == LexToken.Type.DERIV || t == LexToken.Type.PRIME || t == LexToken.Type.INTEGRAL_DEF || t == LexToken.Type.INTEGRAL_INDEF) return true;
        }
        for (NodoAST h : n.hijos) if (contieneDerivOIntegral(h)) return true;
        return false;
    }

    private static boolean validaT1(NodoAST raiz, ResultadoSemantico rs) {
        boolean ok = onlyAllowedAritOps(raiz);
        if (!ok) rs.errores.add(SemanticoError.error("T1_TOKENS", "Operador o símbolo no permitido en aritmética", ruta(raiz)));
        radicalArgNoNegativos(raiz, rs.errores);
        return ok;
    }

    private static boolean validaT2(NodoAST raiz, ResultadoSemantico rs) {
        Set<String> vs = variablesNoI(raiz);
        if (vs.isEmpty()) rs.errores.add(SemanticoError.error("T2_SIN_VAR", "Se requiere al menos una variable", ruta(raiz)));
        funcionesConArgumento(raiz, rs.errores);
        if (!parentesisBalanceados(raiz)) rs.errores.add(SemanticoError.error("T2_PARENS", "Paréntesis no balanceados", ruta(raiz)));
        radicalArgNoNegativos(raiz, rs.errores);
        return !vs.isEmpty();
    }

    private static boolean validaT3(NodoAST raiz, ResultadoSemantico rs) {
        if (!isDerivRaiz(raiz)) rs.errores.add(SemanticoError.error("T3_NO_RAIZ", "Nodo raíz no es derivada", ruta(raiz)));
        NodoAST fun = childOrNull(raiz, 0);
        if (fun == null) rs.errores.add(SemanticoError.error("T3_ARG", "Derivada sin función", ruta(raiz)));
        NodoAST dif = childOrNull(raiz, 1);
        if (dif == null || dif.token == null || !dif.token.type.name().equals("DIFFERENTIAL")) {
            rs.errores.add(SemanticoError.error("T3_DX", "Falta diferencial", ruta(raiz)));
        } else {
            String v = extraerDxVar(dif.token.value);
            Set<String> funVars = variablesNoI(fun);
            if (!funVars.isEmpty() && !funVars.contains(v)) rs.errores.add(SemanticoError.error("T3_VAR_DX", "Variable del diferencial no coincide", ruta(raiz)));
            rs.varIndep = v;
        }
        return true;
    }

    private static boolean validaT4(NodoAST raiz, ResultadoSemantico rs) {
        if (!contieneIntegralIndef(raiz)) rs.errores.add(SemanticoError.error("T4_NO_INT", "No hay integral indefinida", ruta(raiz)));
        NodoAST cuerpo = null;
        NodoAST dif = null;
        Deque<NodoAST> q = new ArrayDeque<>();
        q.add(raiz);
        while (!q.isEmpty()) {
            NodoAST x = q.removeFirst();
            if (x.token != null && x.token.type == LexToken.Type.INTEGRAL_INDEF && x.hijos.size() >= 2) {
                cuerpo = x.hijos.get(0);
                dif = x.hijos.get(1);
                break;
            }
            q.addAll(x.hijos);
        }
        if (cuerpo == null) rs.errores.add(SemanticoError.error("T4_ARG", "Integral indefinida sin integrando", ruta(raiz)));
        if (dif == null || dif.token == null || !dif.token.type.name().equals("DIFFERENTIAL")) rs.errores.add(SemanticoError.error("T4_DX", "Falta diferencial", ruta(raiz)));
        if (dif != null && dif.token != null) {
            String v = extraerDxVar(dif.token.value);
            Set<String> vs = variablesNoI(cuerpo);
            if (!vs.isEmpty() && !vs.contains(v)) rs.errores.add(SemanticoError.error("T4_VAR_DX", "Variable del diferencial no coincide", ruta(raiz)));
            rs.varIndep = v;
        }
        if (contieneIntegralDef(raiz)) rs.errores.add(SemanticoError.error("T4_LIMS", "Aparecen límites en una indefinida", ruta(raiz)));
        return true;
    }

    private static boolean validaT5(NodoAST raiz, ResultadoSemantico rs) {
        if (!contieneIntegralDef(raiz)) rs.errores.add(SemanticoError.error("T5_NO_INT_DEF", "No hay integral definida", ruta(raiz)));
        NodoAST sup = null, inf = null, cuerpo = null, dif = null;
        Deque<NodoAST> q = new ArrayDeque<>();
        q.add(raiz);
        while (!q.isEmpty()) {
            NodoAST x = q.removeFirst();
            if (x.token != null && x.token.type == LexToken.Type.INTEGRAL_DEF && x.hijos.size() >= 4) {
                sup = x.hijos.get(0);
                inf = x.hijos.get(1);
                cuerpo = x.hijos.get(2);
                dif = x.hijos.get(3);
                break;
            }
            q.addAll(x.hijos);
        }
        if (dif == null || dif.token == null || !dif.token.type.name().equals("DIFFERENTIAL")) rs.errores.add(SemanticoError.error("T5_DX", "Falta diferencial", ruta(raiz)));
        if (dif != null && dif.token != null) {
            String v = extraerDxVar(dif.token.value);
            Set<String> vs = variablesNoI(cuerpo);
            if (!vs.isEmpty() && !vs.contains(v)) rs.errores.add(SemanticoError.error("T5_VAR_DX", "Variable del diferencial no coincide", ruta(raiz)));
            rs.varIndep = v;
        }
        Double a = evalConst(inf);
        Double b = evalConst(sup);
        if (a == null || b == null) rs.errores.add(SemanticoError.error("T5_LIMS", "Límites no constantes", ruta(raiz)));
        else if (Math.abs(a - b) < 1e-15) rs.errores.add(SemanticoError.error("T5_LIMS_IGUALES", "Límites iguales", ruta(raiz)));
        return true;
    }

    private static boolean validaT6(NodoAST raiz, ResultadoSemantico rs) {
        if (!eqPresente(raiz)) rs.errores.add(SemanticoError.error("T6_EQ", "Se requiere igualdad", ruta(raiz)));
        if (hasTrigOrLog(raiz)) rs.errores.add(SemanticoError.error("T6_FUNC", "No se permiten funciones trig/log", ruta(raiz)));
        Set<String> vs = variablesNoI(raiz);
        if (vs.size() != 1) rs.errores.add(SemanticoError.error("T6_VAR_UNICA", "Debe existir una sola incógnita", ruta(raiz)));
        String v = vs.isEmpty() ? "x" : vs.iterator().next();
        List<NodoAST> eqs = ecuaciones(raiz);
        for (NodoAST eq : eqs) {
            int g = Math.max(gradoEn(childOrNull(eq, 0), v), gradoEn(childOrNull(eq, 1), v));
            if (g != 1) rs.errores.add(SemanticoError.error("T6_GRADO", "La ecuación no es de primer grado", ruta(eq)));
        }
        rs.varIndep = v;
        return true;
    }

    private static boolean validaT7(NodoAST raiz, ResultadoSemantico rs) {
        if (!eqPresente(raiz)) rs.errores.add(SemanticoError.error("T7_EQ", "Se requiere igualdad", ruta(raiz)));
        if (hasTrigOrLog(raiz)) rs.errores.add(SemanticoError.error("T7_FUNC", "No se permiten funciones", ruta(raiz)));
        Set<String> vs = variablesNoI(raiz);
        if (vs.size() != 1) rs.errores.add(SemanticoError.error("T7_VAR_UNICA", "Debe existir una sola incógnita", ruta(raiz)));
        String v = vs.isEmpty() ? "x" : vs.iterator().next();
        List<NodoAST> eqs = ecuaciones(raiz);
        boolean gradoOk = false;
        for (NodoAST eq : eqs) {
            int g = Math.max(gradoEn(childOrNull(eq, 0), v), gradoEn(childOrNull(eq, 1), v));
            if (g >= 2 && g < Integer.MAX_VALUE) gradoOk = true;
        }
        if (!gradoOk) rs.errores.add(SemanticoError.error("T7_GRADO", "Se requiere grado ≥ 2", ruta(raiz)));
        if (!coefEnteros(raiz)) rs.errores.add(SemanticoError.error("T7_COEF", "Coeficientes no enteros", ruta(raiz)));
        rs.varIndep = v;
        return true;
    }

    private static boolean validaT8(NodoAST raiz, ResultadoSemantico rs) {
        List<NodoAST> eqs = ecuaciones(raiz);
        if (eqs.size() < 2 || eqs.size() > 3) rs.errores.add(SemanticoError.error("T8_CANT", "El sistema debe tener 2 o 3 ecuaciones", ruta(raiz)));
        Set<String> vs = new LinkedHashSet<>();
        for (NodoAST e : eqs) vs.addAll(varsDeEc(e));
        if (vs.size() > eqs.size()) rs.errores.add(SemanticoError.error("T8_VAR", "Variables exceden a ecuaciones", ruta(raiz)));
        if (!todasGradoUno(eqs, vs)) rs.errores.add(SemanticoError.error("T8_GRADO", "Todas las ecuaciones deben ser de primer grado", ruta(raiz)));
        return true;
    }

    private static boolean validaT9(NodoAST raiz, ResultadoSemantico rs) {
        if (!contieneImaginarios(raiz)) rs.errores.add(SemanticoError.error("T9_I", "No se detectó i", ruta(raiz)));
        if (contieneDerivOIntegral(raiz)) rs.errores.add(SemanticoError.error("T9_MEZCLA", "No mezclar con derivadas o integrales", ruta(raiz)));
        return true;
    }

    private static void marcarSubtipos(NodoAST raiz, ResultadoSemantico rs) {
        if (raiz == null || raiz.token == null) return;
        if (esSimple(raiz)) rs.subtipos.add(TipoExpresion.ST_SIMPLE);
        if (esPolinomica(raiz)) rs.subtipos.add(TipoExpresion.ST_POLINOMICA);
        if (hasTrigOrLog(raiz)) {
            rs.subtipos.add(TipoExpresion.ST_TRIGONOMETRICA);
            rs.subtipos.add(TipoExpresion.ST_EXPONENCIAL_LOG);
        }
        if (contieneFrac(raiz)) rs.subtipos.add(TipoExpresion.ST_RACIONAL);
        if (contains(raiz, LexToken.Type.RADICAL)) rs.subtipos.add(TipoExpresion.ST_RADICAL);
    }

    private static boolean esPolinomica(NodoAST n) {
        if (n == null || n.token == null) return false;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL || t == LexToken.Type.CONST_E || t == LexToken.Type.CONST_PI || t == LexToken.Type.VARIABLE)
            return n.hijos.isEmpty();
        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB || t == LexToken.Type.MUL) {
            for (NodoAST h : n.hijos) if (!esPolinomica(h)) return false;
            return true;
        }
        if (t == LexToken.Type.EXP) {
            Double e = evalConst(childOrNull(n, 1));
            if (e == null) return false;
            double r = Math.rint(e);
            if (Math.abs(r - e) > 1e-9) return false;
            if (e < 0) return false;
            return esPolinomica(childOrNull(n, 0));
        }
        if (t == LexToken.Type.DIV || t == LexToken.Type.RADICAL) return false;
        if (LOGS.contains(t) || TRIG.contains(t)) return false;
        return false;
    }

    private static boolean esSimple(NodoAST n) {
        if (n == null || n.token == null) return false;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL || t == LexToken.Type.CONST_E || t == LexToken.Type.CONST_PI || t == LexToken.Type.VARIABLE)
            return n.hijos.isEmpty();
        return false;
    }

    private static boolean contieneFrac(NodoAST n) {
        if (n == null) return false;
        if (n.token != null && n.token.type == LexToken.Type.DIV) return true;
        for (NodoAST h : n.hijos) if (contieneFrac(h)) return true;
        return false;
    }

    private static void marcarGraficable(NodoAST raiz, ResultadoSemantico rs) {
        if (rs.tipoPrincipal == TipoExpresion.T5_INTEGRAL_DEFINIDA ||
                rs.tipoPrincipal == TipoExpresion.T7_DESPEJE_POLINOMICO ||
                rs.tipoPrincipal == TipoExpresion.T2_ALGEBRA_FUNC ||
                rs.tipoPrincipal == TipoExpresion.T3_DERIVADA) {
            rs.graficable = true;
            Set<String> vs = variablesNoI(raiz);
            rs.varIndep = vs.isEmpty() ? "x" : vs.iterator().next();
            rs.varDep = "y";
            rs.modoGraf = "y=f(x)";
        } else {
            rs.graficable = false;
        }
    }

    public static ResultadoSemantico analizar(NodoAST raiz) {
        ResultadoSemantico rs = new ResultadoSemantico();
        if (raiz == null) {
            rs.errores.add(SemanticoError.error("AST_NULL", "Árbol vacío", new ArrayList<>()));
            return rs;
        }

        boolean der = isDerivRaiz(raiz);
        boolean intDef = contieneIntegralDef(raiz);
        boolean intIndef = contieneIntegralIndef(raiz);
        boolean sist = eqMultiples(raiz);
        boolean eq = eqPresente(raiz);
        boolean tieneVar = !variablesNoI(raiz).isEmpty();
        boolean tieneI = contieneImaginarios(raiz);

        if (der) rs.tipoPrincipal = TipoExpresion.T3_DERIVADA;
        else if (intDef) rs.tipoPrincipal = TipoExpresion.T5_INTEGRAL_DEFINIDA;
        else if (intIndef) rs.tipoPrincipal = TipoExpresion.T4_INTEGRAL_INDEFINIDA;
        else if (sist) rs.tipoPrincipal = TipoExpresion.T8_SISTEMA_EC;
        else if (eq) {
            Set<String> vs = variablesNoI(raiz);
            String v = vs.isEmpty() ? "x" : vs.iterator().next();
            int g = 0;
            for (NodoAST e : ecuaciones(raiz)) g = Math.max(g, Math.max(gradoEn(childOrNull(e,0), v), gradoEn(childOrNull(e,1), v)));
            if (vs.size() == 1 && g == 1 && !hasTrigOrLog(raiz)) rs.tipoPrincipal = TipoExpresion.T6_DESPEJE_LINEAL;
            else if (vs.size() == 1 && g >= 2 && g < Integer.MAX_VALUE && esPolinomicaEn(raiz, v) && coefEnteros(raiz)) rs.tipoPrincipal = TipoExpresion.T7_DESPEJE_POLINOMICO;
            else rs.tipoPrincipal = TipoExpresion.T2_ALGEBRA_FUNC;
        } else if (tieneVar) {
            rs.tipoPrincipal = TipoExpresion.T2_ALGEBRA_FUNC;
        } else if (tieneI && !contieneDerivOIntegral(raiz)) {
            rs.tipoPrincipal = TipoExpresion.T9_IMAGINARIOS;
        } else {
            rs.tipoPrincipal = TipoExpresion.T1_ARITMETICA;
        }

        switch (rs.tipoPrincipal) {
            case T1_ARITMETICA: validaT1(raiz, rs); break;
            case T2_ALGEBRA_FUNC: validaT2(raiz, rs); break;
            case T3_DERIVADA: validaT3(raiz, rs); break;
            case T4_INTEGRAL_INDEFINIDA: validaT4(raiz, rs); break;
            case T5_INTEGRAL_DEFINIDA: validaT5(raiz, rs); break;
            case T6_DESPEJE_LINEAL: validaT6(raiz, rs); break;
            case T7_DESPEJE_POLINOMICO: validaT7(raiz, rs); break;
            case T8_SISTEMA_EC: validaT8(raiz, rs); break;
            case T9_IMAGINARIOS: validaT9(raiz, rs); break;
        }

        marcarSubtipos(raiz, rs);
        marcarGraficable(raiz, rs);

        if (rs.tipoPrincipal == TipoExpresion.T1_ARITMETICA) {
            if (contieneImaginarios(raiz)) rs.subtipos.add(TipoExpresion.ST_RACIONAL);
        }

        return rs;
    }
}
