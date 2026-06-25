package com.revaplayer.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.revaplayer.R;

import java.io.File;
import java.util.List;
import java.text.DecimalFormat;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    public interface OnClick { void onClick(File f); }

    private List<File> files;
    private final OnClick onClick;
    private final OnClick onLongClick;

    public FileAdapter(List<File> files, OnClick onClick, OnClick onLongClick) {
        this.files = files;
        this.onClick = onClick;
        this.onLongClick = onLongClick;
    }

    public void updateData(List<File> newFiles) {
        this.files = newFiles;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        File f = files.get(position);
        h.tvName.setText(f.getName());

        if (f.isDirectory()) {
            h.tvIcon.setText("📁");
            File[] children = f.listFiles();
            int count = children == null ? 0 : children.length;
            h.tvMeta.setText(count + " عنصر");
            h.btnPlayFolder.setVisibility(View.VISIBLE);
            h.btnPlayFolder.setOnClickListener(v -> onLongClick.onClick(f));
        } else {
            h.tvIcon.setText("🎬");
            h.tvMeta.setText(formatSize(f.length()));
            h.btnPlayFolder.setVisibility(View.GONE);
        }
        h.itemView.setOnClickListener(v -> onClick.onClick(f));
    }

    @Override
    public int getItemCount() { return files.size(); }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return new DecimalFormat("0.0").format(bytes / 1024.0) + " KB";
        if (bytes < 1024L * 1024 * 1024) return new DecimalFormat("0.0").format(bytes / (1024.0 * 1024)) + " MB";
        return new DecimalFormat("0.0").format(bytes / (1024.0 * 1024 * 1024)) + " GB";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvIcon, tvName, tvMeta;
        final View btnPlayFolder;
        ViewHolder(View v) {
            super(v);
            tvIcon = v.findViewById(R.id.tvFileIcon);
            tvName = v.findViewById(R.id.tvFileName);
            tvMeta = v.findViewById(R.id.tvFileMeta);
            btnPlayFolder = v.findViewById(R.id.btnPlayFolder);
        }
    }
}
