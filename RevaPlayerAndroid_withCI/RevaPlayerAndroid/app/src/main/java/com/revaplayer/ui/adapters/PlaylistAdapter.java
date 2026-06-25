package com.revaplayer.ui.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.revaplayer.R;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    public interface OnItemClick { void onClick(int index); }

    private final List<String> sources;
    private int currentIndex;
    private final OnItemClick onClick;

    public PlaylistAdapter(List<String> sources, int currentIndex, OnItemClick onClick) {
        this.sources = sources;
        this.currentIndex = currentIndex;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_playlist, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String src = sources.get(position);
        String name = getFileName(src);
        holder.tvTitle.setText((position + 1) + ". " + name);
        holder.itemView.setSelected(position == currentIndex);
        holder.tvIndex.setText(position == currentIndex ? "▶" : String.valueOf(position + 1));
        holder.itemView.setOnClickListener(v -> onClick.onClick(position));
    }

    @Override
    public int getItemCount() { return sources.size(); }

    private String getFileName(String src) {
        try {
            String path = Uri.parse(src).getLastPathSegment();
            if (path == null) return src;
            int dot = path.lastIndexOf('.');
            return dot > 0 ? path.substring(0, dot) : path;
        } catch (Exception e) {
            return src;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvIndex;

        ViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvPlaylistTitle);
            tvIndex = v.findViewById(R.id.tvPlaylistIndex);
        }
    }
}
