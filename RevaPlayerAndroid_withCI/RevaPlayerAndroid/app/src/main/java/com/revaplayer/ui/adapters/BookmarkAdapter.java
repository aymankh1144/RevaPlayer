package com.revaplayer.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.revaplayer.R;
import com.revaplayer.domain.model.Bookmark;

import java.util.List;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.ViewHolder> {

    public interface OnClick { void onClick(Bookmark b); }

    private List<Bookmark> items;
    private final OnClick onClick;
    private final OnClick onDelete;

    public BookmarkAdapter(List<Bookmark> items, OnClick onClick, OnClick onDelete) {
        this.items = items;
        this.onClick = onClick;
        this.onDelete = onDelete;
    }

    public void updateData(List<Bookmark> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bookmark, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvPosition;
        private final TextView tvNote;
        private final TextView tvCategory;
        private final ImageButton btnDelete;

        ViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvBookmarkTitle);
            tvPosition = v.findViewById(R.id.tvBookmarkPosition);
            tvNote = v.findViewById(R.id.tvBookmarkNote);
            tvCategory = v.findViewById(R.id.tvBookmarkCategory);
            btnDelete = v.findViewById(R.id.btnDeleteBookmark);
        }

        void bind(Bookmark b) {
            tvTitle.setText(b.getTitle() != null && !b.getTitle().isEmpty()
                    ? b.getTitle() : "علامة بدون عنوان");
            tvPosition.setText("⏱ " + b.getFormattedPosition());

            if (tvNote != null && b.getNote() != null && !b.getNote().isEmpty()) {
                tvNote.setVisibility(View.VISIBLE);
                tvNote.setText(b.getNote());
            } else if (tvNote != null) {
                tvNote.setVisibility(View.GONE);
            }

            if (tvCategory != null && b.getCategory() != null) {
                tvCategory.setText(b.getCategory());
            }

            itemView.setOnClickListener(v -> onClick.onClick(b));
            if (btnDelete != null) {
                btnDelete.setOnClickListener(v -> onDelete.onClick(b));
            }
        }
    }
}
