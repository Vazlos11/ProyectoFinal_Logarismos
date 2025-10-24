package com.example.a22100213_proyectointegrador_logarismos.resolucion.t6lineal.rules;

import com.example.a22100213_proyectointegrador_logarismos.LexToken;
import com.example.a22100213_proyectointegrador_logarismos.NodoAST;
import com.example.a22100213_proyectointegrador_logarismos.Semantico.ResultadoSemantico;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.AstUtils;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.ResultadoResolucion;
import java.util.LinkedList;

public final class AislacionDirectaRule implements T6Rule {

    private AislacionDirectaMatch last;

    @Override
    public String name() { return "Aislación directa (ax+b=c)"; }

    @Override
    public boolean applies(NodoAST raiz, ResultadoSemantico rs) {
        last = null;
        if (!isEq(raiz)) return false;
        NodoAST L = left(raiz), R = right(raiz);
        AislacionDirectaMatch m = matchAxMasB(L, R);
        if (!m.ok) m = matchAxMasB(R, L);
        if (!m.ok) return false;
        if (!isNumeric(m.a) || !isNumericOrNull(m.b) || !isNumeric(m.c)) return false;
        if (!isSimpleVariable(m.x)) return false;
        last = m;
        return true;
    }

    @Override
    public ResultadoResolucion solve(NodoAST raiz, ResultadoSemantico rs) {
        AislacionDirectaMatch m = (last != null) ? last : matchOrThrow(raiz);
        String texAx = AstUtils.toTeX(m.axTerm);
        String texA  = AstUtils.toTeX(m.a);
        String texX  = AstUtils.toTeX(m.x);
        String texB  = (m.b != null) ? AstUtils.toTeX(m.b) : "0";
        String texC  = AstUtils.toTeX(m.c);

        ResultadoResolucion rr = new ResultadoResolucion();
        rr.pasos = new LinkedList<>();
        rr.pasos.add(new PasoResolucion(texAx + " + " + texB + " = " + texC));

        String rhsAfterSub = texC + " - " + texB;
        rr.pasos.add(new PasoResolucion(texAx + " = " + rhsAfterSub));

        String rhsFinal = isNumericOne(m.a) ? rhsAfterSub : "\\frac{" + rhsAfterSub + "}{" + texA + "}";
        rr.pasos.add(new PasoResolucion(texX + " = " + rhsFinal));
        rr.latexFinal = texX + " = " + rhsFinal;
        return rr;
    }

    private AislacionDirectaMatch matchOrThrow(NodoAST raiz) {
        if (!isEq(raiz)) throw new IllegalStateException("No es igualdad.");
        AislacionDirectaMatch m = matchAxMasB(left(raiz), right(raiz));
        if (!m.ok) m = matchAxMasB(right(raiz), left(raiz));
        if (!m.ok) throw new IllegalStateException("No coincide con ax+b=c.");
        return m;
    }

    private AislacionDirectaMatch matchAxMasB(NodoAST lhs, NodoAST rhs) {
        if (lhs == null || rhs == null) return AislacionDirectaMatch.fail();
        if (!isNumeric(rhs)) return AislacionDirectaMatch.fail();
        if (isOp(lhs, LexToken.Type.SUM)) {
            NodoAST a1 = left(lhs), b1 = right(lhs);
            AislacionAx ax = asAx(a1);
            if (ax.ok && isNumeric(b1)) return new AislacionDirectaMatch(true, ax.a, ax.x, b1, rhs, ax.axTerm);
            ax = asAx(b1);
            if (ax.ok && isNumeric(a1)) return new AislacionDirectaMatch(true, ax.a, ax.x, a1, rhs, ax.axTerm);
            return AislacionDirectaMatch.fail();
        }
        if (isOp(lhs, LexToken.Type.SUB)) {
            NodoAST a1 = left(lhs), b1 = right(lhs);
            AislacionAx ax = asAx(a1);
            if (ax.ok && isNumeric(b1)) return new AislacionDirectaMatch(true, ax.a, ax.x, b1, rhs, ax.axTerm);
            return AislacionDirectaMatch.fail();
        }
        AislacionAx ax = asAx(lhs);
        if (ax.ok) return new AislacionDirectaMatch(true, ax.a, ax.x, null, rhs, ax.axTerm);
        return AislacionDirectaMatch.fail();
    }

    private AislacionAx asAx(NodoAST n) {
        if (n == null) return AislacionAx.fail();
        if (isSimpleVariable(n)) {
            NodoAST uno = makeNumOne(n);
            return new AislacionAx(true, uno, n, n);
        }
        if (isOp(n, LexToken.Type.MUL)) {
            NodoAST L = left(n), R = right(n);
            if (isNumeric(L) && isSimpleVariable(R)) return new AislacionAx(true, L, R, n);
            if (isNumeric(R) && isSimpleVariable(L)) return new AislacionAx(true, R, L, n);
        }
        return AislacionAx.fail();
    }

    private boolean isEq(NodoAST n) { return isOp(n, LexToken.Type.EQUAL); }

    private boolean isOp(NodoAST n, LexToken.Type t) {
        return n != null && n.token != null && n.token.type == t;
    }

    private NodoAST left(NodoAST n) {
        return (n != null && n.hijos != null && n.hijos.size() > 0) ? n.hijos.get(0) : null;
    }

    private NodoAST right(NodoAST n) {
        return (n != null && n.hijos != null && n.hijos.size() > 1) ? n.hijos.get(1) : null;
    }

    private boolean isNumericOrNull(NodoAST n) { return n == null || isNumeric(n); }

    private boolean isNumeric(NodoAST n) {
        if (n == null || n.token == null) return false;
        LexToken.Type t = n.token.type;
        switch (t) {
            case INTEGER:
            case DECIMAL:
                return true;
            case SUM:
            case SUB:
            case MUL:
            case DIV:
            case EXP:
                return isNumeric(left(n)) && isNumeric(right(n));
            default:
                return false;
        }
    }

    private boolean isSimpleVariable(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (n.hijos != null && !n.hijos.isEmpty()) return false;
        return n.token.type == LexToken.Type.VARIABLE;
    }

    private boolean isNumericOne(NodoAST n) {
        if (n == null || n.token == null) return false;
        if (n.token.type != LexToken.Type.INTEGER && n.token.type != LexToken.Type.DECIMAL) return false;
        try {
            double v = Double.parseDouble(n.token.value);
            return Math.abs(v - 1.0) < 1e-12;
        } catch (Exception e) {
            return false;
        }
    }

    private NodoAST makeNumOne(NodoAST context) {
        return new NodoAST(new LexToken(LexToken.Type.INTEGER, "1", 1));
    }
}
