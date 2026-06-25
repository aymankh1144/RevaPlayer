package com.revaplayer.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.revaplayer.R;
import com.revaplayer.domain.model.MediaItem;
import com.revaplayer.ui.adapters.MediaListAdapter;
import com.revaplayer.ui.viewmodels.MainViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private MainViewModel viewModel;
    private View resumeCard;
    private RecyclerView rvRecent;
    private MediaListAdapter adapter;
    private View emptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        resumeCard = view.findViewById(R.id.resumeCard);
        rvRecent   = view.findViewById(R.id.rvRecent);
        emptyState = view.findViewById(R.id.emptyState);

        view.findViewById(R.id.btnSearch).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), SearchActivity.class)));

        view.findViewById(R.id.btnBrowseFolders).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), FolderBrowserActivity.class)));

        // زر "فتح ملف" الثاني في Quick Actions
        View btnOpenFile2 = view.findViewById(R.id.btnOpenFile2);
        if (btnOpenFile2 != null) {
            btnOpenFile2.setOnClickListener(v -> {
                // نرسل إشارة للـ MainActivity لفتح الـ file picker
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).openFilePicker();
                }
            });
        }

        rvRecent.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MediaListAdapter(new ArrayList<>(), this::openPlayer);
        rvRecent.setAdapter(adapter);

        observeData();
    }

    private void observeData() {
        viewModel.getResumeItem().observe(getViewLifecycleOwner(), item -> {
            if (item != null && item.getPositionSeconds() > 5) {
                resumeCard.setVisibility(View.VISIBLE);
                ((TextView) resumeCard.findViewById(R.id.resumeTitle))
                        .setText(item.getDisplayTitle());
                ((TextView) resumeCard.findViewById(R.id.resumeProgress))
                        .setText(item.getFormattedProgress());
                ((TextView) resumeCard.findViewById(R.id.resumePercent))
                        .setText(item.getProgressPercent() + "%");
                ((ProgressBar) resumeCard.findViewById(R.id.resumeProgressBar))
                        .setProgress(item.getProgressPercent());
                resumeCard.setOnClickListener(v -> openPlayer(item));
            } else {
                resumeCard.setVisibility(View.GONE);
            }
        });

        viewModel.getHistory().observe(getViewLifecycleOwner(), items -> {
            if (items == null || items.isEmpty()) {
                emptyState.setVisibility(View.VISIBLE);
                rvRecent.setVisibility(View.GONE);
            } else {
                emptyState.setVisibility(View.GONE);
                rvRecent.setVisibility(View.VISIBLE);
                List<MediaItem> recent = items.subList(0, Math.min(6, items.size()));
                adapter.updateData(recent);
            }
        });
    }

    private void openPlayer(MediaItem item) {
        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        intent.putExtra(PlayerActivity.EXTRA_SOURCE, item.getSource());
        intent.putExtra(PlayerActivity.EXTRA_RESUME_POSITION, item.getPositionSeconds());
        startActivity(intent);
    }
}
