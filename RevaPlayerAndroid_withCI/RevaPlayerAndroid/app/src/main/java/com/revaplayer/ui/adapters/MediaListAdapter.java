package com.revaplayer.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.revaplayer.R;
import com.revaplayer.domain.model.MediaItem;

import java.util.List;

public class MediaListAdapter extends RecyclerView.Adapter<MediaListAdapter.ViewHolder> {

    public interface OnItemClick { void onClick(MediaItem item); }
    public interface OnItemLongClick { void onLongClick(MediaItem item); }

    private List<MediaItem> items;
    private final OnItemClick onClick;
    private final OnItemLongClick onLongClick;

    public MediaListAdapter(List<MediaItem> items, OnItemClick onClick) {
        this(items, onClick, null);
    }

    public MediaListAdapter(List<MediaItem> items, OnItemClick onClick, OnItemLongClick onLongClick) {
        this.items = items;
        this.onClick = onClick;
        this.onLongClick = onLongClick;
    }

    public void updateData(List<MediaItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_media, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() { return items.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvProgress;
        private final TextView tvBadge;
        private final ProgressBar progressBar;
        private final ImageButton btnMore;

        ViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvItemTitle);
            tvProgress = v.findViewById(R.id.tvItemProgress);
            tvBadge = v.findViewById(R.id.tvItemBadge);
            progressBar = v.findViewById(R.id.itemProgressBar);
            btnMore = v.findViewById(R.id.btnItemMore);
        }

        void bind(MediaItem item) {
            tvTitle.setText(item.getDisplayTitle());
            tvProgress.setText(item.getFormattedProgress());

            if (progressBar != null) {
                progressBar.setProgress(item.getProgressPercent());
            }

            if (tvBadge != null) {
                if (item.isCompleted()) {
                    tvBadge.setVisibility(View.VISIBLE);
                    tvBadge.setText("✓");
                } else if (item.getProgressPercent() > 0) {
                    tvBadge.setVisibility(View.VISIBLE);
                    tvBadge.setText(item.getProgressPercent() + "%");
                } else {
                    tvBadge.setVisibility(View.GONE);
                }
            }

            itemView.setOnClickListener(v -> onClick.onClick(item));
            if (onLongClick != null && btnMore != null) {
                btnMore.setVisibility(View.VISIBLE);
                btnMore.setOnClickListener(v -> onLongClick.onLongClick(item));
            }
        }
    }
}
