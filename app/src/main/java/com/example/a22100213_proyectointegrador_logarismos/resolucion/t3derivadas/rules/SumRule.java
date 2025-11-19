package com.example.a22100213_proyectointegrador_logarismos.resolucion.t3derivadas.rules;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.AnalisisSemantico;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t3derivadas.DerivadasResolver;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t3derivadas.DerivativeUtils;

public final class SumRule implements DerivativeRule {
    @Override
    public ResultadoResolucion apply(NodoAST raiz, ResultadoSemantico rs) {
        DerivativeUtils.DerivInfo di = DerivativeUtils.localizarDerivada(raiz);
        ResultadoResolucion rr = new ResultadoResolucion();
        if (di == null || di.fun == null) { rr.resultado = raiz; rr.latexFinal = AstUtils.toTeX(raiz); return rr; }

        if (rr.pasos == null) rr.pasos = new java.util.ArrayList<>();
        rr.pasos.add(new PasoResolucion("Descomponer en suma/resta", AstUtils.toTeX(di.fun)));

        java.util.ArrayList<NodoAST> terms = new java.util.ArrayList<>();
        java.util.ArrayList<Integer> signs = new java.util.ArrayList<>();
        flatten(di.fun, 1, terms, signs);

        NodoAST acc = null;
        java.util.ArrayList<NodoAST> derivadosFirmados = new java.util.ArrayList<>();

        for (int i = 0; i < terms.size(); i++) {
            NodoAST t = terms.get(i);
            int s = signs.get(i);
            NodoAST dt = subDerivSmart(t, di.dif);

            Double c = AstUtils.evalConst(dt);
            if (c != null && Math.abs(c) < 1e-15) continue;

            NodoAST dtSigned = (s < 0) ? DerivativeUtils.mul(AstUtils.number(-1.0), dt) : dt;
            derivadosFirmados.add(dtSigned);
            rr.pasos.add(new PasoResolucion("Derivar término " + (i + 1), AstUtils.toTeX(dtSigned)));

            acc = (acc == null) ? dtSigned : AstUtils.bin(LexToken.Type.SUM, acc, dtSigned, "+", 0);
        }

        if (acc == null) acc = AstUtils.number(0.0);

        rr.pasos.add(new PasoResolucion("Linealidad de la derivada", AstUtils.toTeX(acc)));

        NodoAST nuevo = IntegralUtils.reemplazarSubexp(raiz, di.nodoDeriv, acc);
        rr.resultado = nuevo;
        rr.latexFinal = AstUtils.toTeX(nuevo);
        rr.pasos.add(new PasoResolucion("Sustitución en la expresión", rr.latexFinal));
        return rr;
    }

    private void flatten(NodoAST n, int sign, java.util.List<NodoAST> out, java.util.List<Integer> signs) {
        if (n != null && n.token != null && n.token.type == LexToken.Type.SUM && n.hijos.size() == 2) {
            flatten(n.hijos.get(0), sign, out, signs);
            flatten(n.hijos.get(1), sign, out, signs);
        } else if (n != null && n.token != null && n.token.type == LexToken.Type.SUB && n.hijos.size() == 2) {
            flatten(n.hijos.get(0), sign, out, signs);
            flatten(n.hijos.get(1), -sign, out, signs);
        } else {
            out.add(n);
            signs.add(sign);
        }
    }

    private NodoAST subDerivSmart(NodoAST u, NodoAST difNode) {
        String v = diffVarName(difNode);
        if (!contieneVar(u, v)) return AstUtils.number(0.0);

        if (u.token != null && u.token.type == LexToken.Type.VARIABLE)
            return v.equals(u.token.value) ? AstUtils.number(1.0) : AstUtils.number(0.0);

        if (u.token != null && u.token.type == LexToken.Type.EXP && u.hijos.size() == 2) {
            NodoAST base = u.hijos.get(0), exp = u.hijos.get(1);
            if (base != null && base.token != null && base.token.type == LexToken.Type.VARIABLE && v.equals(base.token.value)) {
                Double n = AstUtils.evalConst(exp);
                if (n != null)
                    return DerivativeUtils.mul(AstUtils.number(n), DerivativeUtils.pow(base, AstUtils.number(n - 1.0)));
            }
        }

        if (u.token != null && u.token.type == LexToken.Type.MUL && u.hijos.size() == 2) {
            Double cL = AstUtils.evalConst(u.hijos.get(0));
            Double cR = AstUtils.evalConst(u.hijos.get(1));
            if (cL != null && Math.abs(cL) > 1e-15)
                return DerivativeUtils.mul(AstUtils.number(cL), subDerivSmart(u.hijos.get(1), difNode));
            if (cR != null && Math.abs(cR) > 1e-15)
                return DerivativeUtils.mul(AstUtils.number(cR), subDerivSmart(u.hijos.get(0), difNode));
        }

        NodoAST dnode = DerivativeUtils.deriv(u, difNode);
        ResultadoSemantico rsub = AnalisisSemantico.analizar(dnode);
        return new DerivadasResolver().resolve(dnode, rsub).resultado;
    }

    private boolean contieneVar(NodoAST n, String v) {
        java.util.HashSet<String> s = new java.util.HashSet<>();
        AstUtils.collectVars(n, s);
        return s.contains(v);
    }

    private String diffVarName(NodoAST difNode) {
        if (difNode == null || difNode.token == null || difNode.token.value == null) return "x";
        String s = difNode.token.value.trim();
        if (s.length() == 1) {
            char c = s.charAt(0);
            if (Character.isLetter(c) && Character.isLowerCase(c)) return s;
            return "x";
        }
        if ((s.startsWith("d") || s.startsWith("D")) && s.length() > 1) return s.substring(1);
        return s;
    }
}
