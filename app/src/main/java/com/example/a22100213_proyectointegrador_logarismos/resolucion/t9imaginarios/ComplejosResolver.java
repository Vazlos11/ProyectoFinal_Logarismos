package com.example.a22100213_proyectointegrador_logarismos.resolucion.t9imaginarios;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.TipoExpresion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.Resolver;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;

public class ComplejosResolver implements Resolver {
    private static final double EPS = 1e-12;

    private static final class C {
        final double re, im;
        C(double re, double im) { this.re = re; this.im = im; }
        static C add(C a, C b){ return new C(a.re+b.re, a.im+b.im); }
        static C sub(C a, C b){ return new C(a.re-b.re, a.im-b.im); }
        static C mul(C a, C b){ return new C(a.re*b.re - a.im*b.im, a.re*b.im + a.im*b.re); }
        static C div(C a, C b){
            double d = b.re*b.re + b.im*b.im;
            if (Math.abs(d) < EPS) return null;
            return new C((a.re*b.re + a.im*b.im)/d, (a.im*b.re - a.re*b.im)/d);
        }
        static C powInt(C a, long n){
            if (n==0) return new C(1,0);
            boolean neg = n<0;
            long k = Math.abs(n);
            C base = new C(a.re, a.im);
            C res = new C(1,0);
            while(k>0){
                if((k&1)==1) res = mul(res, base);
                base = mul(base, base);
                k >>= 1;
            }
            if (!neg) return res;
            return div(new C(1,0), res);
        }
    }

    @Override
    public boolean supports(ResultadoSemantico rs) {
        return rs != null && rs.tipoPrincipal == TipoExpresion.T9_IMAGINARIOS;
    }

    @Override
    public ResultadoResolucion resolve(NodoAST raiz, ResultadoSemantico rs) {
        ResultadoResolucion rr = new ResultadoResolucion();
        String texIn = AstUtils.toTeX(raiz);
        rr.pasos.add(new PasoResolucion("Expresión inicial", texIn));
        C v = evalC(raiz);
        if (v == null) {
            rr.pasos.add(new PasoResolucion("Evaluación compleja", "\\text{No evaluable en T9}"));
            rr.resultado = raiz;
            rr.latexFinal = texIn;
            rr.pasos.add(new PasoResolucion("Resultado", rr.latexFinal));
            return rr;
        }
        NodoAST result = buildAplusBi(v.re, v.im);
        String texOut = AstUtils.toTeX(result);
        rr.pasos.add(new PasoResolucion("Forma a+bi", texOut));
        rr.resultado = result;
        rr.latexFinal = texOut;
        rr.pasos.add(new PasoResolucion("Resultado", rr.latexFinal));
        return rr;
    }

    private static C evalC(NodoAST n){
        if (n==null || n.token==null) return null;
        LexToken.Type t = n.token.type;
        if (t == LexToken.Type.INTEGER || t == LexToken.Type.DECIMAL) {
            try { return new C(Double.parseDouble(n.token.value), 0); } catch(Exception e){ return null; }
        }
        if (t == LexToken.Type.CONST_E || t == LexToken.Type.CONST_PI) return null;
        if (t == LexToken.Type.IMAGINARY) {
            String s = n.token.value == null ? "i" : n.token.value;
            s = s.replace(" ", "");
            if (s.equals("i") || s.equals("+i")) return new C(0,1);
            if (s.equals("-i")) return new C(0,-1);
            if (s.endsWith("i")) {
                String c = s.substring(0, s.length()-1);
                try { return new C(0, Double.parseDouble(c)); } catch(Exception e){ return null; }
            }
            return null;
        }
        if (t == LexToken.Type.VARIABLE) return null;
        if (t == LexToken.Type.SUM || t == LexToken.Type.SUB || t == LexToken.Type.MUL || t == LexToken.Type.DIV) {
            C a = evalC(n.hijos.get(0));
            C b = evalC(n.hijos.get(1));
            if (a==null || b==null) return null;
            if (t == LexToken.Type.SUM) return C.add(a,b);
            if (t == LexToken.Type.SUB) return C.sub(a,b);
            if (t == LexToken.Type.MUL) return C.mul(a,b);
            return C.div(a,b);
        }
        if (t == LexToken.Type.EXP) {
            C a = evalC(n.hijos.get(0));
            if (a==null) return null;
            Double k = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.evalConst(n.hijos.get(1));
            if (k == null) return null;
            long ki = Math.round(k);
            if (Math.abs(k - ki) > EPS) return null;
            return C.powInt(a, ki);
        }
        return null;
    }

    private static NodoAST buildAplusBi(double re, double im){
        boolean zr = Math.abs(re) < EPS;
        boolean zi = Math.abs(im) < EPS;
        if (zr && zi) return AstUtils.number(0);
        if (zr) return imagNode(im);
        if (zi) return AstUtils.number(re);
        if (im >= 0) return AstUtils.bin(LexToken.Type.SUM, AstUtils.number(re), imagNode(im), "+", 5);
        return AstUtils.bin(LexToken.Type.SUB, AstUtils.number(re), imagNode(-im), "-", 5);
    }

    private static NodoAST imagNode(double b){
        String v;
        if (Math.abs(b-1.0) < EPS) v = "i";
        else if (Math.abs(b+1.0) < EPS) v = "-i";
        else v = trimDouble(b) + "i";
        return new NodoAST(new LexToken(LexToken.Type.IMAGINARY, v, 0));
    }

    private static String trimDouble(double x){
        double r = Math.rint(x);
        if (Math.abs(x - r) < 1e-12) return Long.toString(Math.round(r));
        String s = Double.toString(x);
        if (s.contains(".")) {
            while (s.endsWith("0")) s = s.substring(0, s.length()-1);
            if (s.endsWith(".")) s = s.substring(0, s.length()-1);
        }
        return s;
    }
}
