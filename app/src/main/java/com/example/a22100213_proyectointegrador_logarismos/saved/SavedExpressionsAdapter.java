package com.example.a22100213_proyectointegrador_logarismos.saved;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a22100213_proyectointegrador_logarismos.R;
import com.judemanutd.katexview.KatexView;

import java.util.ArrayList;
import java.util.List;

public final class SavedExpressionsAdapter extends RecyclerView.Adapter<SavedExpressionsAdapter.VH> {
    public interface Listener {
        void onInsert(SavedExpression e);
        void onDelete(SavedExpression e);
    }

    private final ArrayList<SavedExpression> data = new ArrayList<>();
    private final Listener listener;

    public SavedExpressionsAdapter(Listener l) {
        this.listener = l;
    }

    public void submit(List<SavedExpression> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved_expression, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        SavedExpression e = data.get(i);

        String raw = e.latex != null && !e.latex.isEmpty() ? e.latex : toTextFallback(e.expr);

        raw = raw.replace("$$", "").replace("$", "");

        String formatted = "$$ \\begin{array}{l} \\displaystyle " + raw + " \\end{array} $$";

        h.kv.setText(formatted);

        View.OnClickListener insertClick = v -> {
            if (listener != null) listener.onInsert(e);
        };

        h.itemView.setOnClickListener(insertClick);

        final int touchSlop = ViewConfiguration.get(h.kv.getContext()).getScaledTouchSlop();

        h.kv.setOnTouchListener(new View.OnTouchListener() {
            float downX, downY;
            long downTime;

            @Override
            public boolean onTouch(View v, MotionEvent ev) {
                switch (ev.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = ev.getX();
                        downY = ev.getY();
                        downTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_UP:
                        float dx = Math.abs(ev.getX() - downX);
                        float dy = Math.abs(ev.getY() - downY);
                        long dt = System.currentTimeMillis() - downTime;
                        if (dx < touchSlop && dy < touchSlop && dt < 200) {
                            insertClick.onClick(v);
                            return true;
                        }
                        break;
                }
                return false;
            }
        });

        h.btnDel.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(e);
        });
    }


    @Override
    public int getItemCount() { return data.size(); }

    static final class VH extends RecyclerView.ViewHolder {
        final KatexView kv;
        final ImageButton btnDel;
        VH(@NonNull View itemView) {
            super(itemView);
            kv = itemView.findViewById(R.id.kv_expr);
            btnDel = itemView.findViewById(R.id.btn_delete);
        }
    }

    private static String toTextFallback(String raw) {
        if (raw == null) return "\\text{}";
        String s = raw
                .replace("\\","\\textbackslash{}")
                .replace("{","\\{")
                .replace("}","\\}")
                .replace("#","\\#")
                .replace("$","\\$")
                .replace("%","\\%")
                .replace("&","\\&")
                .replace("_","\\_")
                .replace("^","\\textasciicircum{}")
                .replace("~","\\textasciitilde{}");
        return "\\text{" + s + "}";
    }
}
