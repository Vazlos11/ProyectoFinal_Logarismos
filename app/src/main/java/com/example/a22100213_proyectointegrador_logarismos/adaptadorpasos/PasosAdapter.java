package com.example.a22100213_proyectointegrador_logarismos.adaptadorpasos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a22100213_proyectointegrador_logarismos.R;
import com.judemanutd.katexview.KatexView;

import java.util.List;

public class PasosAdapter extends RecyclerView.Adapter<PasosAdapter.VH> {
    private final List<String> data;

    public PasosAdapter(List<String> data) {
        this.data = data;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_paso, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        h.tvIdx.setText("Paso " + (position + 1));

        String tex = data.get(position);
        h.kvPaso.setText(tex);
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvIdx;
        KatexView kvPaso;
        VH(@NonNull View itemView) {
            super(itemView);
            tvIdx = itemView.findViewById(R.id.tv_idx);
            kvPaso = itemView.findViewById(R.id.kv_paso);
        }
    }
}
