package com.example.a22100213_proyectointegrador_logarismos;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.judemanutd.katexview.KatexView;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public class GraficarActivity extends AppCompatActivity {
    private LineChart chart;
    private KatexView kvHeader;
    private TextView tvAux;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graficar);

        ((TextView) findViewById(R.id.title)).setText("Gráfica");
        kvHeader = findViewById(R.id.kvHeader);
        tvAux = findViewById(R.id.tvAux);
        chart = findViewById(R.id.lineChart);

        configurarChart();

        com.example.a22100213_proyectointegrador_logarismos.graf.GraphState gs =
                com.example.a22100213_proyectointegrador_logarismos.graf.GraphState.I;

        DoubleUnaryOperator f = x ->
                com.example.a22100213_proyectointegrador_logarismos.graf.EvalBridge.eval(
                        gs.ast, gs.var, x, gs.radians
                );

        String labelLatex = (gs.labelLatex == null || gs.labelLatex.isEmpty())
                ? "f(x)"
                : stripDisplay(gs.labelLatex);

        renderHeader(labelLatex, gs);

        if (gs.modo != null && gs.modo.startsWith("AREA_DEF_INTEGRAL:")) {
            double a = gs.limA != null ? gs.limA : 0.0;
            double b = gs.limB != null ? gs.limB : 1.0;
            if (a > b) { double t = a; a = b; b = t; }
            double span = Math.max(1.0, (b - a));
            double xmin = a - 2.0 * span;
            double xmax = b + 2.0 * span;
            plotCurveWithShadedArea(f, xmin, xmax, 0.02, plainLabel(labelLatex), a, b);
        } else {
            plotCurveWithShadedArea(f, -10, 10, 0.02, plainLabel(labelLatex), Double.NaN, Double.NaN);
        }

        Button btnIn = findViewById(R.id.btnZoomIn);
        Button btnOut = findViewById(R.id.btnZoomOut);
        Button btnReset = findViewById(R.id.btnResetZoom);
        if (btnIn != null) btnIn.setOnClickListener(v -> zoomAtCenter(1.25f));
        if (btnOut != null) btnOut.setOnClickListener(v -> zoomAtCenter(0.80f));
        if (btnReset != null) btnReset.setOnClickListener(v -> chart.fitScreen());
    }

    private void configurarChart() {
        chart.getDescription().setEnabled(false);
        chart.setNoDataText("Sin datos");
        chart.setTouchEnabled(true);
        chart.setPinchZoom(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);

        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularityEnabled(true);
        x.setGranularity(1f);

        YAxis yLeft = chart.getAxisLeft();
        yLeft.setGranularityEnabled(true);
        yLeft.setGranularity(1f);
        chart.getAxisRight().setEnabled(false);

        Legend l = chart.getLegend();
        l.setEnabled(false); // ocultamos la leyenda para no ver LaTeX como texto plano

        chart.setExtraTopOffset(4f);
        chart.setExtraBottomOffset(8f);
        chart.setViewPortOffsets(48f, 8f, 16f, 40f);
    }

    private void renderHeader(String labelLatex, com.example.a22100213_proyectointegrador_logarismos.graf.GraphState gs) {
        String header = "$$\\LARGE " + labelLatex + " $$";
        if (kvHeader != null) kvHeader.setText(header);

        if (tvAux != null) {
            if (gs.modo != null && gs.modo.startsWith("AREA_DEF_INTEGRAL:")
                    && gs.limA != null && gs.limB != null) {
                double a = Math.min(gs.limA, gs.limB);
                double b = Math.max(gs.limA, gs.limB);
                tvAux.setText("Área [" + trim(a) + ", " + trim(b) + "]");
            } else {
                tvAux.setText("");
            }
        }
    }

    private void zoomAtCenter(float factor) {
        float px = chart.getWidth() / 2f;
        float py = chart.getHeight() / 2f;
        chart.zoom(factor, factor, px, py);
    }

    private void plotCurveWithShadedArea(DoubleUnaryOperator f,
                                         double xmin, double xmax, double step,
                                         String label,
                                         double a, double b) {
        List<Entry> curve = new ArrayList<>();
        for (double x = xmin; x <= xmax; x += step) {
            float xf = (float) x;
            float yf = (float) f.applyAsDouble(x);
            if (!Float.isNaN(yf) && !Float.isInfinite(yf)) curve.add(new Entry(xf, yf));
        }
        LineDataSet dsCurve = new LineDataSet(curve, label);
        dsCurve.setDrawCircles(false);
        dsCurve.setLineWidth(2f);
        dsCurve.setMode(LineDataSet.Mode.LINEAR);

        LineDataSet dsArea = null;
        if (!Double.isNaN(a) && !Double.isNaN(b) && a < b) {
            List<Entry> area = new ArrayList<>();
            double x = a;
            while (x <= b + 1e-12) {
                float xf = (float) x;
                float yf = (float) f.applyAsDouble(x);
                if (!Float.isNaN(yf) && !Float.isInfinite(yf)) area.add(new Entry(xf, yf));
                x += step;
            }
            dsArea = new LineDataSet(area, "Área");
            dsArea.setDrawCircles(false);
            dsArea.setLineWidth(1.5f);
            dsArea.setMode(LineDataSet.Mode.LINEAR);
            dsArea.setDrawFilled(true);
            dsArea.setFillAlpha(80);
            dsArea.setFillFormatter((d, p) -> 0f);
        }

        LineData data = (dsArea != null) ? new LineData(dsCurve, dsArea) : new LineData(dsCurve);
        chart.setData(data);

        chart.getXAxis().setAxisMinimum((float) xmin);
        chart.getXAxis().setAxisMaximum((float) xmax);

        chart.getXAxis().removeAllLimitLines();
        if (!Double.isNaN(a) && !Double.isNaN(b) && a < b) {
            LimitLine la = new LimitLine((float) a, "a");
            LimitLine lb = new LimitLine((float) b, "b");
            la.setLineWidth(1f);
            lb.setLineWidth(1f);
            chart.getXAxis().addLimitLine(la);
            chart.getXAxis().addLimitLine(lb);
            chart.setVisibleXRangeMinimum((float) Math.max(2 * step, (b - a) / 4.0));
            chart.setVisibleXRangeMaximum((float) Math.max(4.0, (b - a) * 2.0));
            chart.moveViewToX((float) ((a + b) / 2.0));
        }

        chart.invalidate();
    }

    private String stripDisplay(String s) {
        if (s == null) return "";
        String t = s.trim();
        if (t.startsWith("$$")) t = t.substring(2);
        if (t.endsWith("$$")) t = t.substring(0, t.length() - 2);
        return t.trim();
    }

    private String plainLabel(String latex) {
        return latex.replace("\\Large", "")
                .replace("\\large", "")
                .replace("\\displaystyle", "")
                .replace("\\,", " ")
                .replace("\\;", " ")
                .replaceAll("\\\\int", "∫")
                .replaceAll("\\\\left", "")
                .replaceAll("\\\\right", "")
                .replaceAll("\\{", "")
                .replaceAll("\\}", "")
                .trim();
    }

    private String trim(double v) {
        return new DecimalFormat("0.######").format(v);
    }
}
