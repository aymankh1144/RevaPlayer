package com.revaplayer.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.revaplayer.R;
import com.revaplayer.data.database.RevaDatabase;
import com.revaplayer.domain.model.Bookmark;
import com.revaplayer.ui.adapters.BookmarkAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookmarksFragment extends Fragment {

    private BookmarkAdapter adapter;
    private View emptyState;
    private RevaDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookmarks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = RevaDatabase.getInstance(requireContext());

        emptyState = view.findViewById(R.id.emptyState);
        RecyclerView rv = view.findViewById(R.id.rvBookmarks);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BookmarkAdapter(new ArrayList<>(),
                bookmark -> openPlayerAtBookmark(bookmark),
                bookmark -> showDeleteDialog(bookmark));
        rv.setAdapter(adapter);

        loadBookmarks();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBookmarks();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void loadBookmarks() {
        executor.execute(() -> {
            List<Bookmark> bookmarks = db.loadAllBookmarks();
            requireActivity().runOnUiThread(() -> {
                if (bookmarks.isEmpty()) {
                    emptyState.setVisibility(View.VISIBLE);
                } else {
                    emptyState.setVisibility(View.GONE);
                    adapter.updateData(bookmarks);
                }
            });
        });
    }

    private void openPlayerAtBookmark(Bookmark bookmark) {
        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        intent.putExtra(PlayerActivity.EXTRA_SOURCE, bookmark.getSource());
        intent.putExtra(PlayerActivity.EXTRA_RESUME_POSITION, bookmark.getPositionSeconds());
        startActivity(intent);
    }

    private void showDeleteDialog(Bookmark bookmark) {
        new AlertDialog.Builder(requireContext(), R.style.RevaDialog)
                .setTitle("حذف العلامة")
                .setMessage("هل تريد حذف هذه العلامة؟")
                .setPositiveButton("حذف", (d, w) -> {
                    executor.execute(() -> {
                        db.deleteBookmark(bookmark.getId());
                        requireActivity().runOnUiThread(this::loadBookmarks);
                    });
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }
}
