package com.example.a22100213_proyectointegrador_logarismos.resolucion.t6lineal;

import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.MetodoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.PlanificadorResolucion;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t6lineal.rules.AislacionDirectaRule;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t6lineal.rules.BalanceOperacionesRule;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t6lineal.rules.SustitucionSimbolicaRule;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.t6lineal.rules.T6Rule;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class T6LinearResolver {

    public ResultadoResolucion resolve(NodoAST raiz, ResultadoSemantico rs) {
        MetodoResolucion hint = PlanificadorResolucion.metodo(raiz, rs);
        List<T6Rule> reglas = reglasOrdenadas(hint);
        for (T6Rule r : reglas) {
            try {
                if (r.applies(raiz, rs)) {
                    ResultadoResolucion out = r.solve(raiz, rs);
                    if (out == null) out = new ResultadoResolucion();
                    if (out.pasos == null) out.pasos = new LinkedList<>();
                    out.pasos.add(0, new PasoResolucion("Método T6: " + r.name()));
                    if (out.latexFinal == null || out.latexFinal.isEmpty()) out.latexFinal = AstUtils.toTeX(raiz);
                    return out;
                }
            } catch (Exception ex) {
                ResultadoResolucion err = new ResultadoResolucion();
                if (err.pasos == null) err.pasos = new LinkedList<>();
                err.pasos.add(new PasoResolucion("Error en T6: " + r.name()));
                err.pasos.add(new PasoResolucion(ex.getClass().getSimpleName() + ": " + ex.getMessage()));
                err.latexFinal = AstUtils.toTeX(raiz);
                return err;
            }
        }
        ResultadoResolucion rr = new ResultadoResolucion();
        rr.pasos = new LinkedList<>();
        rr.pasos.add(new PasoResolucion("Ninguna regla T6 aplicó."));
        rr.latexFinal = AstUtils.toTeX(raiz);
        rr.resultado = raiz;
        return rr;
    }

    private List<T6Rule> reglasOrdenadas(MetodoResolucion hint) {
        List<T6Rule> base = new ArrayList<>();
        base.add(new AislacionDirectaRule());
        base.add(new BalanceOperacionesRule());
        base.add(new SustitucionSimbolicaRule());
        if (hint == null) return base;

        List<T6Rule> reorden = new ArrayList<>(base.size());
        switch (hint) {
            case DESPEJE_AISLACION_DIRECTA: reorden.add(new AislacionDirectaRule()); break;
            case DESPEJE_BALANCE_INVERSAS:   reorden.add(new BalanceOperacionesRule()); break;
            case DESPEJE_SIMBOLICO_O_NUMERICO: reorden.add(new SustitucionSimbolicaRule()); break;
            default: return base;
        }
        for (T6Rule r : base) {
            boolean ya = false;
            for (T6Rule x : reorden) if (x.getClass().equals(r.getClass())) { ya = true; break; }
            if (!ya) reorden.add(r);
        }
        return reorden;
    }
}
