package com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;

public final class IntegralCommon {
    public static class IntegralInfo { public NodoAST nodoIntegral; public NodoAST padre; public boolean definida; public String var; public NodoAST inf; public NodoAST sup; public NodoAST cuerpo; }

    public static IntegralInfo localizarIntegral(NodoAST n, boolean definida){ return localizarIntegralRec(null,n,definida); }

    private static IntegralInfo localizarIntegralRec(NodoAST parent, NodoAST n, boolean definida){
        if (n==null || n.token==null) return null;
        if ((!definida && n.token.type==LexToken.Type.INTEGRAL_INDEF) || (definida && n.token.type==LexToken.Type.INTEGRAL_DEF)){
            IntegralInfo ii = new IntegralInfo();
            ii.nodoIntegral=n; ii.padre=parent; ii.definida=definida;
            if (!definida){
                ii.cuerpo = n.hijos.size()>0 ? n.hijos.get(0) : null;
                NodoAST dx = n.hijos.size()>1 ? n.hijos.get(1) : null;
                ii.var = dx!=null && dx.token!=null && dx.token.value!=null && dx.token.value.startsWith("d") && dx.token.value.length()>1 ? dx.token.value.substring(1) : "x";
            }else{
                ii.inf = n.hijos.size()>0 ? n.hijos.get(0) : null;
                ii.sup = n.hijos.size()>1 ? n.hijos.get(1) : null;
                ii.cuerpo = n.hijos.size()>2 ? n.hijos.get(2) : null;
                NodoAST dx = n.hijos.size()>3 ? n.hijos.get(3) : null;
                ii.var = dx!=null && dx.token!=null && dx.token.value!=null && dx.token.value.startsWith("d") && dx.token.value.length()>1 ? dx.token.value.substring(1) : "x";
            }
            return ii;
        }
        for (NodoAST h: n.hijos){
            IntegralInfo z = localizarIntegralRec(n,h,definida);
            if (z!=null) return z;
        }
        return null;
    }

    public static NodoAST reemplazar(NodoAST objetivo, NodoAST valor, NodoAST padre, NodoAST raizSiTop){
        if (padre==null) return valor;
        int i = padre.hijos.indexOf(objetivo);
        if (i>=0){ padre.hijos.set(i, valor); valor.parent=padre; return root(padre); }
        return raizSiTop;
    }

    public static NodoAST root(NodoAST n){ NodoAST p=n; while(p!=null && p.parent!=null) p=p.parent; return p==null?n:p; }
}
