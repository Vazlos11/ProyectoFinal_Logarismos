package com.example.a22100213_proyectointegrador_logarismos.solucion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a22100213_proyectointegrador_logarismos.GraficarActivity;
import com.example.a22100213_proyectointegrador_logarismos.R;
import com.example.a22100213_proyectointegrador_logarismos.adaptadorpasos.PasosAdapter;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.judemanutd.katexview.KatexView;

import java.util.ArrayList;
import java.util.Locale;

public class SolucionActivity extends AppCompatActivity {
    public static final String EXTRA_STEPS_LATEX = "steps_latex";
    public static final String EXTRA_STEPS_DESC = "steps_desc";
    public static final String EXTRA_EXPR_LATEX = "expr_latex";
    public static final String EXTRA_GRAFICABLE = "graficable";
    public static final String EXTRA_GRAF_MODO = "graf_modo";
    public static final String EXTRA_GRAF_VARX = "graf_varx";
    public static final String EXTRA_METODO = "metodo";

    KatexView kvProblema;
    RecyclerView rvPasos;
    Button btnGraficar;
    TextView tvMetodo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solucion);

        kvProblema = findViewById(R.id.kv_problema);
        rvPasos = findViewById(R.id.rv_pasos);
        btnGraficar = findViewById(R.id.btn_graficar);
        tvMetodo = findViewById(R.id.tv_metodo);

        String exprRaw = getIntent().getStringExtra(EXTRA_EXPR_LATEX);
        ArrayList<String> pasosLatexRaw = getIntent().getStringArrayListExtra(EXTRA_STEPS_LATEX);
        ArrayList<String> pasosDescRaw = getIntent().getStringArrayListExtra(EXTRA_STEPS_DESC);
        boolean graficable = getIntent().getBooleanExtra(EXTRA_GRAFICABLE, false);
        String metodo = getIntent().getStringExtra(EXTRA_METODO);
        final String grafModo = getIntent().getStringExtra(EXTRA_GRAF_MODO);
        final String grafVarX = getIntent().getStringExtra(EXTRA_GRAF_VARX);

        final String exprFinal = toDisplayMath(exprRaw);
        final ArrayList<PasoResolucion> pasosFinal = buildStepsModel(pasosDescRaw, pasosLatexRaw);
        final String metodoFinal = (metodo == null) ? "" : metodo.trim();

        if (tvMetodo != null) {
            tvMetodo.setText(metodoFinal.isEmpty() ? "Método aplicado: n/d" : ("Método aplicado: " + metodoFinal));
        }

        if (rvPasos != null) {
            rvPasos.setLayoutManager(new LinearLayoutManager(this));
            rvPasos.setItemAnimator(null);
            rvPasos.setAdapter(new PasosAdapter(pasosFinal));
        }

        if (kvProblema != null) {
            kvProblema.setText(exprFinal);
        }

        if (btnGraficar != null) {
            if (graficable) {
                btnGraficar.setVisibility(View.VISIBLE);
                btnGraficar.setOnClickListener(v -> {
                    Intent i = new Intent(SolucionActivity.this, GraficarActivity.class);
                    i.putExtra(EXTRA_EXPR_LATEX, exprFinal);
                    i.putExtra(EXTRA_GRAF_MODO, grafModo);
                    i.putExtra(EXTRA_GRAF_VARX, grafVarX);
                    startActivity(i);
                });
            } else {
                btnGraficar.setVisibility(View.GONE);
            }
        }
    }

    private ArrayList<PasoResolucion> buildStepsModel(ArrayList<String> descs, ArrayList<String> latexList) {
        ArrayList<PasoResolucion> out = new ArrayList<>();
        if (latexList == null) return out;
        int n = latexList.size();
        for (int i = 0; i < n; i++) {
            String rawLatex = latexList.get(i);
            if (rawLatex == null) rawLatex = "";
            String low = stripDelimiters(rawLatex).toLowerCase(Locale.ROOT);
            if (low.contains("formateo") && low.contains("final")) continue;
            String desc = "";
            if (descs != null && i < descs.size() && descs.get(i) != null) desc = descs.get(i).trim();
            if (desc.isEmpty()) desc = fallbackDescripcion(rawLatex);
            out.add(new PasoResolucion(desc, toDisplayMath(rawLatex)));
        }
        return out;
    }

    private String fallbackDescripcion(String latexRaw) {
        String t = stripDelimiters(latexRaw);
        String low = t.toLowerCase(Locale.ROOT);
        if (low.contains("replanificaci")) return "Replanificación";
        if (low.contains("\\int")) return "Integración";
        if (low.contains("\\frac{d}")) return "Derivación";
        if (low.contains("\\Rightarrow") || low.contains("\\to")) return "Transformación";
        return "";
    }

    private String toDisplayMath(String s) {
        if (s == null) return "$$ \\begin{array}{l} \\displaystyle \\; \\end{array} $$";
        String t = s.trim();
        t = stripDelimiters(t);
        t = t.replaceFirst("^\\\\Large\\s*", "")
                .replaceFirst("^\\\\large\\s*", "")
                .replaceFirst("^\\\\displaystyle\\s*", "");
        t = t.replace("\r", " ").replace("\n", " ").replaceAll("\\s{2,}", " ").trim();
        t = t.replaceAll(",\\s*(d[x|y])", "\\\\, $1");

        return "$$ \\begin{array}{l} \\displaystyle " + t + " \\end{array} $$";
    }

    private String stripDelimiters(String s) {
        String t = s == null ? "" : s.trim();
        if (t.startsWith("$$") && t.endsWith("$$") && t.length() >= 4) t = t.substring(2, t.length() - 2);
        else if (t.startsWith("$") && t.endsWith("$") && t.length() >= 2) t = t.substring(1, t.length() - 1);
        else if (t.startsWith("\\[") && t.endsWith("\\]") && t.length() >= 4) t = t.substring(2, t.length() - 2);
        return t;
    }
}
