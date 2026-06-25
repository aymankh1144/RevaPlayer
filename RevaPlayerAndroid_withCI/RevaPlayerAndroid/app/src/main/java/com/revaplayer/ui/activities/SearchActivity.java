package com.revaplayer.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.revaplayer.R;
import com.revaplayer.data.database.RevaDatabase;
import com.revaplayer.domain.model.MediaItem;
import com.revaplayer.ui.adapters.MediaListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class SearchActivity extends AppCompatActivity {

    private MediaListAdapter adapter;
    private List<MediaItem> allItems = new ArrayList<>();
    private RevaDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        db = RevaDatabase.getInstance(this);

        ImageButton btnBack = findViewById(R.id.btnSearchBack);
        btnBack.setOnClickListener(v -> finish());

        EditText etSearch = findViewById(R.id.etSearch);
        RecyclerView rv = findViewById(R.id.rvSearchResults);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MediaListAdapter(new ArrayList<>(), item -> {
            Intent intent = new Intent(this, PlayerActivity.class);
            intent.putExtra(PlayerActivity.EXTRA_SOURCE, item.getSource());
            intent.putExtra(PlayerActivity.EXTRA_RESUME_POSITION, item.getPositionSeconds());
            startActivity(intent);
        });
        rv.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) { filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadAll();
        etSearch.requestFocus();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void loadAll() {
        executor.execute(() -> {
            allItems = db.loadHistory(500);
            runOnUiThread(() -> adapter.updateData(allItems));
        });
    }

    private void filter(String query) {
        if (query.isEmpty()) {
            adapter.updateData(allItems);
            return;
        }
        String q = query.toLowerCase().trim();
        List<MediaItem> filtered = allItems.stream()
                .filter(i -> i.getDisplayTitle() != null &&
                        i.getDisplayTitle().toLowerCase().contains(q))
                .collect(Collectors.toList());
        adapter.updateData(filtered);
    }
}
