    package com.example.a22100213_proyectointegrador_logarismos.resolucion;

    import com.example.a22100213_proyectointegrador_logarismos.LexToken;
    import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
    import com.example.a22100213_proyectointegrador_logarismos.Semantico.MetodoResolucion;
    import com.example.a22100213_proyectointegrador_logarismos.Semantico.PlanificadorResolucion;
    import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
    import com.example.a22100213_proyectointegrador_logarismos.resolucion.integrales.IntegralResolver;
    import com.example.a22100213_proyectointegrador_logarismos.resolucion.t2algebra.T2AlgebraResolver;

    public class MotorResolucion {

        public static ResultadoResolucion resolver(NodoAST raiz, ResultadoSemantico rs) {
            MetodoResolucion m = PlanificadorResolucion.metodo(raiz, rs);
            ResultadoResolucion rr = resolverSegunTipo(raiz, rs, m);
            if (ok(rr)) return rr;
            rr = resolverSegunTipo(raiz, rs, MetodoResolucion.NINGUNO);
            if (ok(rr)) return rr;
            ResultadoResolucion out = new ResultadoResolucion();
            out.resultado = raiz;
            out.latexFinal = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.toTeX(raiz);
            out.pasos.add(new PasoResolucion("\\text{Sin cambio} \\Rightarrow " + out.latexFinal));
            return out;
        }

        private static boolean ok(ResultadoResolucion r) {
            return r != null && r.latexFinal != null && !r.latexFinal.trim().isEmpty();
        }

        private static ResultadoResolucion resolverSegunTipo(NodoAST raiz, ResultadoSemantico rs, MetodoResolucion m) {
            switch (rs.tipoPrincipal) {
                case T1_ARITMETICA:
                    return resolverAritmetica(raiz);

                case T4_INTEGRAL_INDEFINIDA:
                    return IntegralResolver.resolverIndefinida(raiz, rs, m);

                case T5_INTEGRAL_DEFINIDA:
                    return IntegralResolver.resolverDefinida(raiz, rs, m);

                case T2_ALGEBRA_FUNC:
                    return T2AlgebraResolver.resolver(raiz, rs);

                default:
                    ResultadoResolucion rr = new ResultadoResolucion();
                    rr.resultado = raiz;
                    rr.latexFinal = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.toTeX(raiz);
                    rr.pasos.add(new PasoResolucion("\\text{Aun no implementado} \\Rightarrow " + rr.latexFinal));
                    return rr;
            }
        }


        private static ResultadoResolucion resolverAritmetica(NodoAST n) {
            ResultadoResolucion rr = new ResultadoResolucion();
            Double v = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.evalConst(n);
            if (v != null) {
                rr.resultado = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.number(v);
                rr.latexFinal = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.toTeX(rr.resultado);
                rr.pasos.add(new PasoResolucion("\\text{Evaluacion directa} \\Rightarrow " + rr.latexFinal));
                return rr;
            }
            rr.resultado = n;
            rr.latexFinal = com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils.toTeX(n);
            rr.pasos.add(new PasoResolucion("\\text{Sin cambio} \\Rightarrow " + rr.latexFinal));
            return rr;
        }
    }
