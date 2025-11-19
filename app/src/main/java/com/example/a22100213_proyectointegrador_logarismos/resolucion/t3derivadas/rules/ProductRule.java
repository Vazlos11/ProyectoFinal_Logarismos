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

public final class ProductRule implements DerivativeRule {
    @Override
    public ResultadoResolucion apply(NodoAST raiz, ResultadoSemantico rs) {
        DerivativeUtils.DerivInfo di = DerivativeUtils.localizarDerivada(raiz);
        ResultadoResolucion rr = new ResultadoResolucion();
        if (di == null || di.fun == null) { rr.resultado = raiz; rr.latexFinal = AstUtils.toTeX(raiz); return rr; }

        NodoAST f = di.fun;
        if (f.token == null || f.token.type != LexToken.Type.MUL || f.hijos.size() != 2) { rr.resultado = raiz; rr.latexFinal = AstUtils.toTeX(raiz); return rr; }

        NodoAST u = f.hijos.get(0), v = f.hijos.get(1);

        NodoAST duNode = DerivativeUtils.deriv(u, AstUtils.atom(LexToken.Type.DIFFERENTIAL, "d" + di.var, 0));
        NodoAST dvNode = DerivativeUtils.deriv(v, AstUtils.atom(LexToken.Type.DIFFERENTIAL, "d" + di.var, 0));
        ResultadoSemantico rsu = AnalisisSemantico.analizar(duNode);
        ResultadoSemantico rsv = AnalisisSemantico.analizar(dvNode);
        NodoAST du = new DerivadasResolver().resolve(duNode, rsu).resultado;
        NodoAST dv = new DerivadasResolver().resolve(dvNode, rsv).resultado;

        NodoAST res = DerivativeUtils.sum(DerivativeUtils.mul(du, v), DerivativeUtils.mul(u, dv));

        NodoAST nuevo = IntegralUtils.reemplazarSubexp(raiz, di.nodoDeriv, res);
        rr.resultado = nuevo;
        rr.latexFinal = AstUtils.toTeX(nuevo);

        if (rr.pasos == null) rr.pasos = new java.util.ArrayList<>();
        rr.pasos.add(new PasoResolucion("Producto u·v", AstUtils.toTeX(f)));
        rr.pasos.add(new PasoResolucion("Derivada u'", AstUtils.toTeX(du)));
        rr.pasos.add(new PasoResolucion("Derivada v'", AstUtils.toTeX(dv)));
        rr.pasos.add(new PasoResolucion("Regla del producto: u'v + uv'", AstUtils.toTeX(res)));
        rr.pasos.add(new PasoResolucion("Sustitución en la expresión", rr.latexFinal));

        return rr;
    }
}
