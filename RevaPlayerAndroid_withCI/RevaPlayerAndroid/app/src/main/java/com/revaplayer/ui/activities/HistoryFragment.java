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

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.revaplayer.R;
import com.revaplayer.domain.model.MediaItem;
import com.revaplayer.ui.adapters.MediaListAdapter;
import com.revaplayer.ui.viewmodels.MainViewModel;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private MainViewModel viewModel;
    private MediaListAdapter adapter;
    private View emptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        emptyState = view.findViewById(R.id.emptyState);
        RecyclerView rv = view.findViewById(R.id.rvHistory);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MediaListAdapter(new ArrayList<>(), this::openPlayer,
                item -> showDeleteDialog(item));
        rv.setAdapter(adapter);

        View btnClearAll = view.findViewById(R.id.btnClearHistory);
        if (btnClearAll != null) {
            btnClearAll.setOnClickListener(v -> showClearAllDialog());
        }

        viewModel.getHistory().observe(getViewLifecycleOwner(), items -> {
            if (items == null || items.isEmpty()) {
                emptyState.setVisibility(View.VISIBLE);
                rv.setVisibility(View.GONE);
            } else {
                emptyState.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);
                adapter.updateData(items);
            }
        });
    }

    private void openPlayer(MediaItem item) {
        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        intent.putExtra(PlayerActivity.EXTRA_SOURCE, item.getSource());
        intent.putExtra(PlayerActivity.EXTRA_RESUME_POSITION, item.getPositionSeconds());
        startActivity(intent);
    }

    private void showDeleteDialog(MediaItem item) {
        new AlertDialog.Builder(requireContext(), R.style.RevaDialog)
                .setTitle("حذف من السجل")
                .setMessage("هل تريد حذف \"" + item.getDisplayTitle() + "\" من سجل المشاهدة؟")
                .setPositiveButton("حذف", (d, w) -> viewModel.deleteHistoryItem(item.getSource()))
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void showClearAllDialog() {
        new AlertDialog.Builder(requireContext(), R.style.RevaDialog)
                .setTitle("مسح السجل")
                .setMessage("سيتم حذف كامل سجل المشاهدة. هل أنت متأكد؟")
                .setPositiveButton("مسح الكل", (d, w) -> viewModel.clearHistory())
                .setNegativeButton("إلغاء", null)
                .show();
    }
}
