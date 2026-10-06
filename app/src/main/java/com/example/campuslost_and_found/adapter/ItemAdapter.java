package com.example.campuslost_and_found.adapter;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.campuslost_and_found.R;
import com.example.campuslost_and_found.data.ItemEntity;
import com.example.campuslost_and_found.databinding.ItemCardBinding;
import com.example.campuslost_and_found.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for displaying Lost and Found item cards.
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ItemEntity item);
        void onMenuItemClick(ItemEntity item, int menuItemId);
    }

    private final Context context;
    private final List<ItemEntity> itemList = new ArrayList<>();
    private final OnItemClickListener listener;

    public ItemAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setItems(List<ItemEntity> newItems) {
        this.itemList.clear();
        if (newItems != null) {
            this.itemList.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCardBinding binding = ItemCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ItemEntity item = itemList.get(position);
        holder.bind(item, context, listener);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final ItemCardBinding binding;

        public ItemViewHolder(@NonNull ItemCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ItemEntity item, Context context, OnItemClickListener listener) {
            binding.tvItemTitle.setText(item.getTitle());
            binding.tvItemCategoryZone.setText(item.getCategory() + " • " + item.getZone());
            binding.tvItemDesc.setText(item.getDescription());

            // Type Badge Setup
            boolean isLost = "LOST".equalsIgnoreCase(item.getType());
            binding.tvTypeBadge.setText(isLost ? context.getString(R.string.type_lost) : context.getString(R.string.type_found));
            int typeBgColor = isLost ? ContextCompat.getColor(context, R.color.status_lost)
                    : ContextCompat.getColor(context, R.color.status_found);
            binding.tvTypeBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(typeBgColor));

            // Status Badge
            boolean isResolved = "RESOLVED".equalsIgnoreCase(item.getStatus());
            binding.tvStatusBadge.setText(isResolved ? context.getString(R.string.status_resolved) : context.getString(R.string.status_active));
            int statusTextColor = isResolved ? ContextCompat.getColor(context, R.color.status_resolved_text)
                    : ContextCompat.getColor(context, R.color.status_found_text);
            binding.tvStatusBadge.setTextColor(statusTextColor);

            // Aging Badge
            String relativeTime = DateUtils.getRelativeTimeString(item.getDateMillis(), context);
            binding.tvAgingBadge.setText(relativeTime);
            if (DateUtils.isAgingItem(item.getDateMillis())) {
                binding.tvAgingBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_aging));
            } else {
                binding.tvAgingBadge.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            }

            // Image Loading with Glide
            if (item.getImageUri() != null && !item.getImageUri().trim().isEmpty()) {
                Glide.with(context)
                        .load(item.getImageUri())
                        .placeholder(R.drawable.ic_placeholder_image)
                        .error(R.drawable.ic_placeholder_image)
                        .centerCrop()
                        .into(binding.imgItem);
            } else {
                binding.imgItem.setImageResource(R.drawable.ic_placeholder_image);
            }

            // Click Listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });

            binding.btnMoreMenu.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.inflate(R.menu.item_popup_menu);
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    if (listener != null) {
                        listener.onMenuItemClick(item, menuItem.getItemId());
                    }
                    return true;
                });
                popupMenu.show();
            });
        }
    }
}
