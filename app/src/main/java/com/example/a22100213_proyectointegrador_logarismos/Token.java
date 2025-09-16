package com.example.a22100213_proyectointegrador_logarismos;

import java.util.ArrayList;
import java.util.List;

public class Token {
    public String value;
    public boolean isContainer;
    public boolean isAtomic;
    public List<Token> children;
    public Token parent;

    public Token(String value, boolean isContainer) {
        this(value, isContainer, false);
    }

    public Token(String value, boolean isContainer, boolean isAtomic) {
        this.value = value;
        this.isContainer = isContainer;
        this.isAtomic = isAtomic;
        this.children = isContainer ? new ArrayList<>() : null;
        this.parent = null;
    }

    public static Token simple(String v)    { return new Token(v, false, false); }
    public static Token atomic(String v)    { return new Token(v, false, true); }
    public static Token container(String v) { return new Token(v, true, false); }

    public String toLatex() {
        if (!isContainer) {
            return value;
        }

        StringBuilder sb = new StringBuilder();

        switch (value) {
            case "\\int_def":
                sb.append("\\int_{");
                if (children.size() > 0) sb.append(children.get(0).toLatex());
                sb.append("}^{");
                if (children.size() > 1) sb.append(children.get(1).toLatex());
                sb.append("} ");
                if (children.size() > 2) sb.append(children.get(2).toLatex());
                sb.append(" dx");
                break;

            case "\\sqrt":
                sb.append("\\sqrt{");
                for (Token t : children) sb.append(t.toLatex());
                sb.append("}");
                break;

            case "\\lvert":
                sb.append("\\lvert ");
                for (Token t : children) sb.append(t.toLatex());
                sb.append(" \\rvert");
                break;

            case "\\frac":
                sb.append("\\frac{");
                if (children.size() > 0) sb.append(children.get(0).toLatex());
                sb.append("}{");
                if (children.size() > 1) sb.append(children.get(1).toLatex());
                sb.append("}");
                break;

            case "()":
                sb.append("(");
                for (Token t : children) sb.append(t.toLatex());
                sb.append(")");
                break;

            case "[]":
                sb.append("\\left[");
                for (Token t : children) sb.append(t.toLatex());
                sb.append("\\right]");
                break;

            case "\\{ \\}":
                sb.append("\\{ ");
                for (Token t : children) sb.append(t.toLatex());
                sb.append(" \\}");
                break;

            case "\\int": {
                sb.append("\\int ");
                if (children.size() >= 3 && children.get(0) != null && children.get(1) != null) {
                    sb.append("_{").append(children.get(0).toLatex()).append("}");
                    sb.append("^{").append(children.get(1).toLatex()).append("} ");
                    for (int i = 2; i < children.size(); i++) sb.append(children.get(i).toLatex());
                } else {
                    for (Token t : children) sb.append(t.toLatex());
                }
                break;
            }

            case "\\int_dx":
                sb.append("\\int ");
                for (Token t : children) sb.append(t.toLatex());
                sb.append(" dx");
                break;

            case "\\exp":
                sb.append("^{");
                for (Token t : children) sb.append(t.toLatex());
                sb.append("}");
                break;

            case "\\log_{2}":
            case "\\log_{10}":
                sb.append("\\log_{");
                if (children.size() > 0) sb.append(children.get(0).toLatex());
                sb.append("}(");
                if (children.size() > 1) sb.append(children.get(1).toLatex());
                sb.append(")");
                break;


            case "\\sin":
            case "\\cos":
            case "\\tan":
            case "\\cot":
            case "\\sec":
            case "\\csc":
            case "\\log":
            case "\\ln":
                sb.append(value);
                sb.append("(");
                for (Token t : children) sb.append(t.toLatex());
                sb.append(")");
                break;

            default:
                for (Token t : children) sb.append(t.toLatex());
        }
        return sb.toString();
    }

}
