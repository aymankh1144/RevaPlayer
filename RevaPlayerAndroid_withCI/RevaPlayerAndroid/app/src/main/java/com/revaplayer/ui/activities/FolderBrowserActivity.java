package com.revaplayer.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.revaplayer.R;
import com.revaplayer.ui.adapters.FileAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FolderBrowserActivity extends AppCompatActivity {

    private FileAdapter adapter;
    private TextView tvPath;
    private File currentDir;

    private static final List<String> VIDEO_EXT = Arrays.asList(
        ".mp4", ".mkv", ".avi", ".mov", ".wmv", ".flv",
        ".webm", ".m4v", ".3gp", ".ts", ".m2ts"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_browser);

        tvPath = findViewById(R.id.tvCurrentPath);
        RecyclerView rv = findViewById(R.id.rvFiles);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FileAdapter(new ArrayList<>(),
            file -> {
                if (file.isDirectory()) browseDir(file);
                else openVideo(file);
            },
            file -> selectFolderAsPlaylist(file)
        );
        rv.setAdapter(adapter);

        View btnBack = findViewById(R.id.btnBrowserBack);
        btnBack.setOnClickListener(v -> {
            if (currentDir.getParentFile() != null) {
                browseDir(currentDir.getParentFile());
            } else {
                finish();
            }
        });

        View btnUp = findViewById(R.id.btnBrowserUp);
        btnUp.setOnClickListener(v -> {
            File parent = currentDir.getParentFile();
            if (parent != null) browseDir(parent);
        });

        // ابدأ من مجلد الفيديوهات
        File startDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        if (!startDir.exists()) startDir = Environment.getExternalStorageDirectory();
        browseDir(startDir);
    }

    private void browseDir(File dir) {
        currentDir = dir;
        tvPath.setText(dir.getAbsolutePath());

        File[] files = dir.listFiles();
        List<File> filtered = new ArrayList<>();
        if (files != null) {
            // المجلدات أولاً
            for (File f : files) {
                if (f.isDirectory() && !f.isHidden()) filtered.add(f);
            }
            // ثم الفيديوهات
            for (File f : files) {
                if (f.isFile() && isVideo(f)) filtered.add(f);
            }
        }
        adapter.updateData(filtered);
    }

    private boolean isVideo(File f) {
        String name = f.getName().toLowerCase();
        for (String ext : VIDEO_EXT) {
            if (name.endsWith(ext)) return true;
        }
        return false;
    }

    private void openVideo(File file) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra(PlayerActivity.EXTRA_SOURCE, Uri.fromFile(file).toString());
        startActivity(intent);
    }

    private void selectFolderAsPlaylist(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;
        ArrayList<String> playlist = new ArrayList<>();
        for (File f : files) {
            if (f.isFile() && isVideo(f)) {
                playlist.add(Uri.fromFile(f).toString());
            }
        }
        if (!playlist.isEmpty()) {
            Intent intent = new Intent(this, PlayerActivity.class);
            intent.putStringArrayListExtra(PlayerActivity.EXTRA_PLAYLIST, playlist);
            startActivity(intent);
        }
    }
}
