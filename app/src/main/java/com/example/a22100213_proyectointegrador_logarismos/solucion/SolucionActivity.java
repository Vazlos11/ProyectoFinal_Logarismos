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
import com.judemanutd.katexview.KatexView;

import java.util.ArrayList;
import java.util.Locale;

public class SolucionActivity extends AppCompatActivity {
    public static final String EXTRA_EXPR_LATEX = "expr_latex";
    public static final String EXTRA_STEPS_LATEX = "steps_latex";
    public static final String EXTRA_GRAFICABLE = "graficable";
    public static final String EXTRA_METODO = "metodo_aplicado";

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
        ArrayList<String> pasosRaw = getIntent().getStringArrayListExtra(EXTRA_STEPS_LATEX);
        boolean graficable = getIntent().getBooleanExtra(EXTRA_GRAFICABLE, false);
        String metodo = getIntent().getStringExtra(EXTRA_METODO);

        final String exprFinal = toDisplayMath(exprRaw);
        final ArrayList<String> pasosFinal = buildNormalizedSteps(pasosRaw);
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
                    startActivity(i);
                });
            } else {
                btnGraficar.setVisibility(View.GONE);
            }
        }
    }

    private ArrayList<String> buildNormalizedSteps(ArrayList<String> in) {
        ArrayList<String> out = new ArrayList<>();
        if (in == null) return out;
        for (String s : in) {
            if (s == null) continue;
            if (looksLikeFormateoFinal(s)) continue;
            String tex = toDisplayMath(s);
            if (!isEffectivelyEmpty(tex)) out.add(tex);
        }
        return out;
    }

    private boolean looksLikeFormateoFinal(String s) {
        if (s == null) return false;
        String t = stripDelimiters(s).toLowerCase(Locale.ROOT);
        return t.contains("formateo final");
    }

    private boolean isEffectivelyEmpty(String s) {
        if (s == null) return true;
        String t = stripDelimiters(s).replace("\\,", " ").replace("\\;", " ").trim();
        return t.isEmpty();
    }

    private String toDisplayMath(String s) {
        if (s == null) return "$$\\displaystyle \\;$$";
        String t = s.trim();

        t = stripDelimiters(t);
        t = t.replaceFirst("^\\\\Large\\s*", "")
                .replaceFirst("^\\\\large\\s*", "")
                .replaceFirst("^\\\\displaystyle\\s*", "");

        t = t.replace("\r", " ").replace("\n", " ").replaceAll("\\s{2,}", " ").trim();

        t = t.replaceAll(",\\s*(d[x|y])", "\\\\, $1");

        return "$$\\displaystyle " + t + " $$";
    }

    private String stripDelimiters(String s) {
        String t = s.trim();

        if (t.startsWith("$$") && t.endsWith("$$") && t.length() >= 4) {
            t = t.substring(2, t.length() - 2).trim();
        }

        if (t.startsWith("\\[") && t.endsWith("\\]") && t.length() >= 4) {
            t = t.substring(2, t.length() - 2).trim();
        }

        if (t.startsWith("\\(") && t.endsWith("\\)") && t.length() >= 4) {
            t = t.substring(2, t.length() - 2).trim();
        }
        return t;
    }
}
