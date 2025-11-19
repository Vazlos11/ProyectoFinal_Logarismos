package com.example.a22100213_proyectointegrador_logarismos.adaptadorpasos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a22100213_proyectointegrador_logarismos.R;
import com.example.a22100213_proyectointegrador_logarismos.resolucion.PasoResolucion;
import com.judemanutd.katexview.KatexView;

import java.util.List;

public class PasosAdapter extends RecyclerView.Adapter<PasosAdapter.VH> {
    private final List<PasoResolucion> data;

    public PasosAdapter(List<PasoResolucion> data) {
        this.data = data;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_paso, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH h, int position) {
        PasoResolucion p = data.get(position);
        String d = p.descripcion == null ? "" : p.descripcion.trim();
        if (d.isEmpty()) {
            h.tvDesc.setVisibility(View.GONE);
        } else {
            h.tvDesc.setVisibility(View.VISIBLE);
            h.tvDesc.setText(d);
        }
        h.kvPaso.setText(p.latex == null ? "" : p.latex);
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        CardView card;
        TextView tvDesc;
        KatexView kvPaso;
        VH(View v) {
            super(v);
            card = v.findViewById(R.id.card_step);
            tvDesc = v.findViewById(R.id.tv_desc);
            kvPaso = v.findViewById(R.id.kv_paso);
        }
    }
}
