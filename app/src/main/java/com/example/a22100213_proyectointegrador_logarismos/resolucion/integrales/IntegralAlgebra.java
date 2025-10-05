package com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales;

import java.util.ArrayList;
import java.util.List;
import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.common.CoefficientFormatter;

public final class IntegralAlgebra {
    public static Double linearCoeff(NodoAST n, String v){
        if (n==null || n.token==null) return null;
        if (n.token.type==LexToken.Type.VARIABLE && v.equals(n.token.value) && n.hijos.isEmpty()) return 1.0;
        if (AstUtils.isConst(n)) return 0.0;
        LexToken.Type t = n.token.type;
        if (t==LexToken.Type.MUL){
            Double a = AstUtils.evalConst(n.hijos.get(0));
            Double b = AstUtils.evalConst(n.hijos.get(1));
            if (a!=null && n.hijos.get(1).token.type==LexToken.Type.VARIABLE && v.equals(n.hijos.get(1).token.value)) return a;
            if (b!=null && n.hijos.get(0).token.type==LexToken.Type.VARIABLE && v.equals(n.hijos.get(0).token.value)) return b;
        }
        if (t==LexToken.Type.SUM || t==LexToken.Type.SUB){
            Double c0 = linearCoeff(n.hijos.get(0), v);
            Double c1 = linearCoeff(n.hijos.get(1), v);
            if (c0!=null && c1!=null) return c0 + (t==LexToken.Type.SUM ? c1 : -c1);
        }
        return null;
    }

    public static NodoAST xPow(String v, int n){
        if (n<=0) return AstUtils.number(1.0);
        NodoAST x = AstUtils.atom(LexToken.Type.VARIABLE, v, 1);
        return AstUtils.bin(LexToken.Type.EXP, x, AstUtils.number(n), "^", 7);
    }
    public static NodoAST ePowClone(NodoAST u){
        NodoAST uClone = AstUtils.cloneTree(u);
        NodoAST e = AstUtils.atom(LexToken.Type.CONST_E, "e", 1);
        return AstUtils.bin(LexToken.Type.EXP, e, uClone, "^", 7);
    }
    public static NodoAST sinClone(NodoAST u){
        return AstUtils.un(LexToken.Type.TRIG_SIN, AstUtils.cloneTree(u), "sin", 8);
    }
    public static NodoAST cosClone(NodoAST u){
        return AstUtils.un(LexToken.Type.TRIG_COS, AstUtils.cloneTree(u), "cos", 8);
    }
    public static NodoAST lnClone(NodoAST n){
        return AstUtils.un(LexToken.Type.LN, AstUtils.cloneTree(n), "ln", 9);
    }
    public static NodoAST sum(NodoAST a, NodoAST b){ return AstUtils.bin(LexToken.Type.SUM, a, b, "+", 5); }
    public static NodoAST sub(NodoAST a, NodoAST b){ return AstUtils.bin(LexToken.Type.SUB, a, b, "-", 5); }

    public static NodoAST mulC(NodoAST a, double c){
        if (Math.abs(c-1.0)<1e-15) return a;
        if (c<0) return AstUtils.bin(LexToken.Type.SUB, AstUtils.number(0), CoefficientFormatter.applyIfSimpleUnitFraction(a, -c), "-", 5);
        return CoefficientFormatter.applyIfSimpleUnitFraction(a, c);
    }

    public static class MulSplit { public double c; public List<NodoAST> nonconst; }

    public static List<NodoAST> flattenMul(NodoAST n){
        List<NodoAST> out = new ArrayList<>();
        flattenMulRec(n,out);
        return out;
    }
    private static void flattenMulRec(NodoAST n, List<NodoAST> out){
        if (n!=null && n.token!=null && n.token.type==LexToken.Type.MUL && n.hijos.size()==2){
            flattenMulRec(n.hijos.get(0), out);
            flattenMulRec(n.hijos.get(1), out);
        } else out.add(n);
    }
    public static NodoAST rebuildMul(List<NodoAST> fs){
        if (fs.isEmpty()) return AstUtils.number(1.0);
        NodoAST acc = fs.get(0);
        for (int i=1;i<fs.size();i++) acc = AstUtils.bin(LexToken.Type.MUL, acc, fs.get(i), "*", 6);
        return acc;
    }
    public static MulSplit splitMul(NodoAST n){
        MulSplit r = new MulSplit();
        r.c=1.0; r.nonconst=new ArrayList<>();
        for (NodoAST f: flattenMul(n)){
            Double c = AstUtils.evalConst(f);
            if (c!=null) r.c*=c; else r.nonconst.add(f);
        }
        return r;
    }
}
