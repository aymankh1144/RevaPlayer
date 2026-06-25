package com.revaplayer.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.revaplayer.R;
import com.revaplayer.domain.model.MediaItem;
import com.revaplayer.ui.adapters.HomeTabAdapter;
import com.revaplayer.ui.adapters.MediaListAdapter;
import com.revaplayer.ui.viewmodels.MainViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int PICK_VIDEO_REQUEST = 101;

    private MainViewModel viewModel;
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNav;
    private HomeTabAdapter tabAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        setupUI();
        requestPermissionsIfNeeded();
        observeViewModel();
    }

    private void setupUI() {
        viewPager = findViewById(R.id.viewPager);
        bottomNav = findViewById(R.id.bottomNavigation);

        tabAdapter = new HomeTabAdapter(this);
        viewPager.setAdapter(tabAdapter);
        viewPager.setOffscreenPageLimit(3);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0: bottomNav.setSelectedItemId(R.id.nav_home); break;
                    case 1: bottomNav.setSelectedItemId(R.id.nav_history); break;
                    case 2: bottomNav.setSelectedItemId(R.id.nav_bookmarks); break;
                    case 3: bottomNav.setSelectedItemId(R.id.nav_settings); break;
                }
            }
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) viewPager.setCurrentItem(0, true);
            else if (id == R.id.nav_history) viewPager.setCurrentItem(1, true);
            else if (id == R.id.nav_bookmarks) viewPager.setCurrentItem(2, true);
            else if (id == R.id.nav_settings) viewPager.setCurrentItem(3, true);
            return true;
        });

        FloatingActionButton fab = findViewById(R.id.fabOpenFile);
        fab.setOnClickListener(v -> openFilePicker());
    }

    private void observeViewModel() {
        viewModel.getResumeItem().observe(this, item -> {
            if (item != null) {
                View resumeCard = findViewById(R.id.resumeCard);
                if (resumeCard != null) {
                    resumeCard.setVisibility(View.VISIBLE);
                    TextView title = resumeCard.findViewById(R.id.resumeTitle);
                    TextView progress = resumeCard.findViewById(R.id.resumeProgress);
                    if (title != null) title.setText(item.getDisplayTitle());
                    if (progress != null) {
                        String progressText = formatProgress(item.getPositionSeconds(), item.getDurationSeconds());
                        progress.setText(progressText);
                    }
                    resumeCard.setOnClickListener(v -> openPlayer(item.getSource(), item.getPositionSeconds()));
                }
            }
        });
    }

    private String formatProgress(double position, double duration) {
        if (duration <= 0) return "";
        int pct = (int) ((position / duration) * 100);
        return "تم مشاهدة " + pct + "% · " + formatTime((long) position) + " / " + formatTime((long) duration);
    }

    private String formatTime(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        if (h > 0) return String.format("%d:%02d:%02d", h, m, s);
        return String.format("%d:%02d", m, s);
    }

    public void openFilePicker() {
        if (!hasStoragePermission()) {
            requestPermissionsIfNeeded();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_VIDEO_REQUEST);
    }

    private void openPlayer(String source, double resumePosition) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra(PlayerActivity.EXTRA_SOURCE, source);
        intent.putExtra(PlayerActivity.EXTRA_RESUME_POSITION, resumePosition);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK && data != null) {
            List<String> sources = new ArrayList<>();
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    sources.add(uri.toString());
                }
            } else if (data.getData() != null) {
                Uri uri = data.getData();
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                sources.add(uri.toString());
            }
            if (!sources.isEmpty()) {
                if (sources.size() == 1) {
                    openPlayer(sources.get(0), 0);
                } else {
                    Intent intent = new Intent(this, PlayerActivity.class);
                    intent.putStringArrayListExtra(PlayerActivity.EXTRA_PLAYLIST, new ArrayList<>(sources));
                    startActivity(intent);
                }
            }
        }
    }

    private boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermissionsIfNeeded() {
        if (!hasStoragePermission()) {
            String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    ? Manifest.permission.READ_MEDIA_VIDEO
                    : Manifest.permission.READ_EXTERNAL_STORAGE;
            ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewModel.refreshHistory();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.refreshHistory();
        viewModel.refreshResumeItem();
    }
}
