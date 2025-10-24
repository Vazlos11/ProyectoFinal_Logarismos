package com.example.a22100213_proyectointegrador_logarismos.resolucion.t3derivadas.rules;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.AnalisisSemantico;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t3derivadas.DerivativeUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t3derivadas.DerivadasResolver;

public final class ConstTimesFunctionRule implements DerivativeRule {
    @Override
    public ResultadoResolucion apply(NodoAST raiz, ResultadoSemantico rs) {
        DerivativeUtils.DerivInfo di = DerivativeUtils.localizarDerivada(raiz);
        ResultadoResolucion rr = new ResultadoResolucion();
        if (di == null || di.fun == null) { rr.resultado = raiz; rr.latexFinal = AstUtils.toTeX(raiz); return rr; }

        NodoAST f = di.fun;
        if (f.token == null || f.token.type != LexToken.Type.MUL || f.hijos.size() != 2) {
            rr.resultado = raiz; rr.latexFinal = AstUtils.toTeX(raiz); return rr;
        }

        NodoAST a = f.hijos.get(0);
        NodoAST b = f.hijos.get(1);

        boolean aConst = AstUtils.isConst(a);
        boolean bConst = AstUtils.isConst(b);

        if (aConst == bConst) { rr.resultado = raiz; rr.latexFinal = AstUtils.toTeX(raiz); return rr; }

        NodoAST c = aConst ? a : b;
        NodoAST u = aConst ? b : a;

        NodoAST dnode = DerivativeUtils.deriv(u, AstUtils.atom(LexToken.Type.DIFFERENTIAL, "d" + di.var, 0));
        ResultadoSemantico rsub = AnalisisSemantico.analizar(dnode);
        NodoAST uprime = new DerivadasResolver().resolve(dnode, rsub).resultado;

        Double ku = AstUtils.evalConst(uprime);
        if (ku != null) {
            if (Math.abs(ku) < 1e-15) {
                NodoAST res0 = AstUtils.number(0.0);
                NodoAST nuevo0 = IntegralUtils.reemplazarSubexp(raiz, di.nodoDeriv, res0);
                rr.resultado = nuevo0; rr.latexFinal = AstUtils.toTeX(nuevo0);
                rr.pasos.add(new PasoResolucion(rr.latexFinal));
                return rr;
            }
            if (Math.abs(ku - 1.0) < 1e-15) {
                NodoAST resC = AstUtils.cloneTree(c);
                NodoAST nuevoC = IntegralUtils.reemplazarSubexp(raiz, di.nodoDeriv, resC);
                rr.resultado = nuevoC; rr.latexFinal = AstUtils.toTeX(nuevoC);
                rr.pasos.add(new PasoResolucion(rr.latexFinal));
                return rr;
            }
        }

        NodoAST res = AstUtils.bin(LexToken.Type.MUL, AstUtils.cloneTree(c), uprime, "*", 0);

        Double kc = AstUtils.evalConst(c);
        if (kc != null) {
            Double kv = AstUtils.evalConst(res);
            if (kv != null) res = AstUtils.number(kv);
        }

        NodoAST nuevo = IntegralUtils.reemplazarSubexp(raiz, di.nodoDeriv, res);
        rr.resultado = nuevo; rr.latexFinal = AstUtils.toTeX(nuevo);
        rr.pasos.add(new PasoResolucion(AstUtils.toTeX(res)));
        rr.pasos.add(new PasoResolucion(rr.latexFinal));
        return rr;
    }
}
