package com.example.a22100213_proyectointegrador_logarismos;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public class GraficarActivity extends AppCompatActivity {

    private LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graficar);
        ((TextView) findViewById(R.id.title)).setText("Gráfica");
        chart = findViewById(R.id.lineChart);
        configurarChart();
        plotFunction(Math::sin, -10, 10, 0.1, "f(x)=sin(x)");
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
        l.setWordWrapEnabled(true);
    }

    public void plotFunction(DoubleUnaryOperator f, double xmin, double xmax, double step, String label) {
        List<Entry> entries = new ArrayList<>();
        for (double x = xmin; x <= xmax; x += step) {
            float xf = (float) x;
            float yf = (float) f.applyAsDouble(x);
            if (!Float.isNaN(yf) && !Float.isInfinite(yf)) entries.add(new Entry(xf, yf));
        }
        LineDataSet set = new LineDataSet(entries, label);
        set.setDrawCircles(false);
        set.setLineWidth(2f);
        set.setMode(LineDataSet.Mode.LINEAR);
        LineData data = new LineData(set);
        chart.setData(data);
        chart.invalidate();
    }
}
