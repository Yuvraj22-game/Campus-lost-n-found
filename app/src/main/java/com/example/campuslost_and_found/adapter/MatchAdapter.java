package com.example.campuslost_and_found.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslost_and_found.databinding.ItemMatchCardBinding;
import com.example.campuslost_and_found.util.MatchScorer.MatchCandidate;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for horizontal Smart Match Suggestions.
 */
public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchViewHolder> {

    public interface OnMatchClickListener {
        void onMatchClick(MatchCandidate candidate);
    }

    private final Context context;
    private final List<MatchCandidate> matches = new ArrayList<>();
    private final OnMatchClickListener listener;

    public MatchAdapter(Context context, OnMatchClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setMatches(List<MatchCandidate> newMatches) {
        this.matches.clear();
        if (newMatches != null) {
            this.matches.addAll(newMatches);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMatchCardBinding binding = ItemMatchCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MatchViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        MatchCandidate candidate = matches.get(position);
        holder.bind(candidate, listener);
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }

    public static class MatchViewHolder extends RecyclerView.ViewHolder {
        private final ItemMatchCardBinding binding;

        public MatchViewHolder(@NonNull ItemMatchCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(MatchCandidate candidate, OnMatchClickListener listener) {
            binding.tvMatchScore.setText(candidate.getScore() + "% Match");
            binding.tvMatchConfidence.setText(candidate.getConfidence());
            binding.tvMatchTitle.setText(candidate.getItem().getTitle());
            binding.tvMatchZoneCategory.setText(candidate.getItem().getCategory() + " • " + candidate.getItem().getZone());

            StringBuilder reasonsText = new StringBuilder();
            for (String reason : candidate.getReasons()) {
                reasonsText.append("• ").append(reason).append("\n");
            }
            binding.tvMatchReasons.setText(reasonsText.toString().trim());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMatchClick(candidate);
                }
            });
        }
    }
}
