package com.example.campuslost_and_found.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslost_and_found.data.ZoneCount;
import com.example.campuslost_and_found.databinding.ItemZoneChipBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for horizontal Chip list showing Zone-wise item counts.
 */
public class ZoneCountAdapter extends RecyclerView.Adapter<ZoneCountAdapter.ZoneViewHolder> {

    public interface OnZoneClickListener {
        void onZoneClick(String zoneName);
    }

    private final List<ZoneCount> zoneList = new ArrayList<>();
    private final OnZoneClickListener listener;

    public ZoneCountAdapter(OnZoneClickListener listener) {
        this.listener = listener;
    }

    public void setZoneList(List<ZoneCount> newZones) {
        this.zoneList.clear();
        if (newZones != null) {
            this.zoneList.addAll(newZones);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ZoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemZoneChipBinding binding = ItemZoneChipBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ZoneViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ZoneViewHolder holder, int position) {
        ZoneCount zone = zoneList.get(position);
        holder.bind(zone, listener);
    }

    @Override
    public int getItemCount() {
        return zoneList.size();
    }

    public static class ZoneViewHolder extends RecyclerView.ViewHolder {
        private final ItemZoneChipBinding binding;

        public ZoneViewHolder(@NonNull ItemZoneChipBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ZoneCount zone, OnZoneClickListener listener) {
            binding.chipZone.setText(zone.getZone() + " (" + zone.getCount() + ")");
            binding.chipZone.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onZoneClick(zone.getZone());
                }
            });
        }
    }
}
