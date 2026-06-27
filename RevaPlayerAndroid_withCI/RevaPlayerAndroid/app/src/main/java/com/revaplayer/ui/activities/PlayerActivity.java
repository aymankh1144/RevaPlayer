package com.revaplayer.ui.activities;

import androidx.appcompat.app.AlertDialog;
import android.widget.LinearLayout;

import android.annotation.SuppressLint;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Rational;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.revaplayer.R;
import com.revaplayer.domain.model.Bookmark;
import com.revaplayer.ui.adapters.PlaylistAdapter;
import com.revaplayer.ui.dialogs.BookmarkDialog;
import com.revaplayer.ui.viewmodels.PlayerViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlayerActivity extends AppCompatActivity {

    public static final String EXTRA_SOURCE = "source";
    public static final String EXTRA_RESUME_POSITION = "resume_position";
    public static final String EXTRA_PLAYLIST = "playlist";
    public static final String EXTRA_PLAYLIST_INDEX = "playlist_index";

    private ExoPlayer player;
    private PlayerView playerView;
    private PlayerViewModel viewModel;

    // Controls
    private View controlsOverlay;
    private ImageButton btnPlayPause;
    private ImageButton btnPrev;
    private ImageButton btnNext;
    private ImageButton btnRotate;
    private ImageButton btnFullscreen;
    private ImageButton btnPlaylist;
    private ImageButton btnBookmark;
    private ImageButton btnSpeed;
    private ImageButton btnBack;
    private SeekBar seekBar;
    private TextView tvCurrentTime;
    private TextView tvDuration;
    private TextView tvTitle;
    private TextView tvSpeedOverlay;
    private ProgressBar progressSpinner;
    private LinearLayout volumeOverlay;
    private TextView tvVolumeValue;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private GestureDetector gestureDetector;
    private boolean controlsVisible = true;
    private boolean isFullscreen = false;
    private float currentSpeed = 1.0f;
    private List<String> playlist = new ArrayList<>();
    private int currentPlaylistIndex = 0;
    private String currentSource;
    private double resumePosition = 0;

    private final Runnable hideControlsRunnable = () -> {
        if (!seekBar.isPressed()) hideControls();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_player);

        viewModel = new ViewModelProvider(this).get(PlayerViewModel.class);

        parseIntent();
        initializeViews();
        setupPlayer();
        setupGestures();
        setupControls();
        observeViewModel();
        scheduleHideControls();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        currentSource = intent.getStringExtra(EXTRA_SOURCE);
        resumePosition = intent.getDoubleExtra(EXTRA_RESUME_POSITION, 0);
        ArrayList<String> pl = intent.getStringArrayListExtra(EXTRA_PLAYLIST);
        if (pl != null && !pl.isEmpty()) {
            playlist = pl;
            currentPlaylistIndex = intent.getIntExtra(EXTRA_PLAYLIST_INDEX, 0);
            currentSource = playlist.get(currentPlaylistIndex);
        }
    }

    private void initializeViews() {
        playerView = findViewById(R.id.playerView);
        controlsOverlay = findViewById(R.id.controlsOverlay);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnRotate = findViewById(R.id.btnRotate);
        btnFullscreen = findViewById(R.id.btnFullscreen);
        btnPlaylist = findViewById(R.id.btnPlaylist);
        btnBookmark = findViewById(R.id.btnBookmark);
        btnSpeed = findViewById(R.id.btnSpeed);
        btnBack = findViewById(R.id.btnBack);
        seekBar = findViewById(R.id.seekBar);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvDuration = findViewById(R.id.tvDuration);
        tvTitle = findViewById(R.id.tvTitle);
        tvSpeedOverlay = findViewById(R.id.tvSpeedOverlay);
        progressSpinner = findViewById(R.id.progressSpinner);
        volumeOverlay = findViewById(R.id.volumeOverlay);
        tvVolumeValue = findViewById(R.id.tvVolumeValue);
    }

    private void setupPlayer() {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        playerView.setUseController(false); // نستخدم controls مخصصة

        player.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                updatePlayPauseButton(isPlaying);
            }

            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_BUFFERING) {
                    progressSpinner.setVisibility(View.VISIBLE);
                } else {
                    progressSpinner.setVisibility(View.GONE);
                }
                if (state == Player.STATE_READY && resumePosition > 0) {
                    player.seekTo((long) (resumePosition * 1000));
                    resumePosition = 0;
                }
                if (state == Player.STATE_ENDED) {
                    onVideoEnded();
                }
            }

            @Override
            public void onMediaItemTransition(@androidx.annotation.Nullable androidx.media3.common.MediaItem mediaItem, int reason) {
                updateTitle();
                resumePosition = 0;
            }
        });

        loadMedia();
        startProgressUpdater();
    }

    private void loadMedia() {
        if (playlist.isEmpty() && currentSource != null) {
            MediaItem item = MediaItem.fromUri(Uri.parse(currentSource));
            player.setMediaItem(item);
        } else if (!playlist.isEmpty()) {
            List<MediaItem> items = new ArrayList<>();
            for (String src : playlist) {
                items.add(MediaItem.fromUri(Uri.parse(src)));
            }
            player.setMediaItems(items, currentPlaylistIndex, 0);
        }
        player.prepare();
        player.play();
        updateTitle();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupGestures() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                toggleControls();
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                float x = e.getX();
                float width = playerView.getWidth();
                if (x < width / 3f) {
                    seekBy(-10);
                    showSeekOverlay(-10);
                } else if (x > 2 * width / 3f) {
                    seekBy(10);
                    showSeekOverlay(10);
                } else {
                    togglePlayPause();
                }
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distX, float distY) {
                if (e1 == null) return false;
                float x = e1.getX();
                float width = playerView.getWidth();
                // يمين: صوت، يسار: سطوع (سنستخدم الصوت فقط حالياً)
                if (x > width / 2f) {
                    adjustVolume(distY);
                }
                return true;
            }
        });

        playerView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });
    }

    private void seekBy(int seconds) {
        if (player != null) {
            long newPos = player.getCurrentPosition() + (seconds * 1000L);
            newPos = Math.max(0, Math.min(newPos, player.getDuration()));
            player.seekTo(newPos);
        }
    }

    private void adjustVolume(float delta) {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int cur = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        int newVol = (int) (cur - delta / 30f);
        newVol = Math.max(0, Math.min(newVol, max));
        am.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0);
        int pct = (int) ((newVol * 100f) / max);
        tvVolumeValue.setText(pct + "%");
        volumeOverlay.setVisibility(View.VISIBLE);
        handler.removeCallbacks(hideVolumeOverlay);
        handler.postDelayed(hideVolumeOverlay, 1500);
    }

    private final Runnable hideVolumeOverlay = () -> volumeOverlay.setVisibility(View.GONE);

    private void showSeekOverlay(int seconds) {
        String msg = seconds > 0 ? "+" + seconds + " ثانية ⏩" : seconds + " ثانية ⏪";
        tvSpeedOverlay.setText(msg);
        tvSpeedOverlay.setVisibility(View.VISIBLE);
        handler.removeCallbacks(hideSpeedOverlay);
        handler.postDelayed(hideSpeedOverlay, 800);
    }

    private final Runnable hideSpeedOverlay = () -> tvSpeedOverlay.setVisibility(View.GONE);

    private void setupControls() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnPlayPause.setOnClickListener(v -> togglePlayPause());

        btnPrev.setOnClickListener(v -> {
            if (!playlist.isEmpty() && currentPlaylistIndex > 0) {
                player.seekToPreviousMediaItem();
                currentPlaylistIndex--;
            } else {
                seekBy(-30);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (!playlist.isEmpty() && currentPlaylistIndex < playlist.size() - 1) {
                player.seekToNextMediaItem();
                currentPlaylistIndex++;
            } else {
                seekBy(30);
            }
        });

        btnRotate.setOnClickListener(v -> {
            int orientation = getRequestedOrientation();
            if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ||
                    orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }
        });

        btnFullscreen.setOnClickListener(v -> toggleFullscreen());

        btnBookmark.setOnClickListener(v -> {
            if (player != null) {
                long pos = player.getCurrentPosition();
                BookmarkDialog dialog = new BookmarkDialog(this, currentSource,
                        getVideoTitle(), pos / 1000.0, bookmark -> {
                    viewModel.addBookmark(bookmark);
                    Toast.makeText(this, "تم حفظ العلامة ✓", Toast.LENGTH_SHORT).show();
                });
                dialog.show();
            }
        });

        btnSpeed.setOnClickListener(v -> showSpeedDialog());

        btnPlaylist.setOnClickListener(v -> showPlaylistSheet());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                if (fromUser && player != null) {
                    long duration = player.getDuration();
                    if (duration > 0) {
                        long newPos = (long) (progress / 1000f * duration);
                        tvCurrentTime.setText(formatTime(newPos));
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar bar) {
                handler.removeCallbacks(hideControlsRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar bar) {
                if (player != null) {
                    long duration = player.getDuration();
                    if (duration > 0) {
                        long newPos = (long) (bar.getProgress() / 1000f * duration);
                        player.seekTo(newPos);
                    }
                }
                scheduleHideControls();
            }
        });
    }

    private void togglePlayPause() {
        if (player == null) return;
        if (player.isPlaying()) player.pause();
        else player.play();
        resetHideControlsTimer();
    }

    private void updatePlayPauseButton(boolean isPlaying) {
        btnPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
    }

    private void toggleControls() {
        if (controlsVisible) hideControls();
        else showControls();
    }

    private void showControls() {
        controlsVisible = true;
        controlsOverlay.setVisibility(View.VISIBLE);
        controlsOverlay.animate().alpha(1f).setDuration(200).start();
        scheduleHideControls();
    }

    private void hideControls() {
        controlsVisible = false;
        controlsOverlay.animate().alpha(0f).setDuration(300)
                .withEndAction(() -> controlsOverlay.setVisibility(View.INVISIBLE)).start();
    }

    private void scheduleHideControls() {
        handler.removeCallbacks(hideControlsRunnable);
        handler.postDelayed(hideControlsRunnable, 3500);
    }

    private void resetHideControlsTimer() {
        scheduleHideControls();
    }

    private void toggleFullscreen() {
        isFullscreen = !isFullscreen;
        View decorView = getWindow().getDecorView();
        if (isFullscreen) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                getWindow().getInsetsController().hide(
                    android.view.WindowInsets.Type.statusBars() |
                    android.view.WindowInsets.Type.navigationBars());
                getWindow().getInsetsController().setSystemBarsBehavior(
                    android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            } else {
                decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
            btnFullscreen.setImageResource(R.drawable.ic_fullscreen_exit);
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                getWindow().getInsetsController().show(
                    android.view.WindowInsets.Type.statusBars() |
                    android.view.WindowInsets.Type.navigationBars());
            } else {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
            btnFullscreen.setImageResource(R.drawable.ic_fullscreen);
        }
    }

    private void showSpeedDialog() {
        float[] speeds = {0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 2.5f, 3.0f};
        String[] labels = {"0.25×", "0.5×", "0.75×", "عادي 1×", "1.25×", "1.5×", "2×", "2.5×", "3×"};
        int current = 3;
        for (int i = 0; i < speeds.length; i++) {
            if (Math.abs(speeds[i] - currentSpeed) < 0.01f) { current = i; break; }
        }
        final int[] selected = {current};
        new AlertDialog.Builder(this, R.style.RevaDialog)
                .setTitle("سرعة التشغيل")
                .setSingleChoiceItems(labels, current, (d, which) -> selected[0] = which)
                .setPositiveButton("تطبيق", (d, w) -> {
                    currentSpeed = speeds[selected[0]];
                    if (player != null) {
                        player.setPlaybackSpeed(currentSpeed);
                    }
                    String label = currentSpeed == 1f ? "" : currentSpeed + "×";
                    btnSpeed.setContentDescription(label.isEmpty() ? "السرعة" : label);
                    tvSpeedOverlay.setText("السرعة: " + labels[selected[0]]);
                    tvSpeedOverlay.setVisibility(View.VISIBLE);
                    handler.postDelayed(hideSpeedOverlay, 1200);
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void showPlaylistSheet() {
        if (playlist.isEmpty()) {
            Toast.makeText(this, "لا توجد قائمة تشغيل", Toast.LENGTH_SHORT).show();
            return;
        }
        BottomSheetDialog sheet = new BottomSheetDialog(this, R.style.RevaBottomSheet);
        View view = getLayoutInflater().inflate(R.layout.sheet_playlist, null);
        RecyclerView rv = view.findViewById(R.id.rvPlaylist);
        rv.setLayoutManager(new LinearLayoutManager(this));
        PlaylistAdapter adapter = new PlaylistAdapter(playlist, currentPlaylistIndex, index -> {
            currentPlaylistIndex = index;
            player.seekTo(index, 0);
            player.play();
            sheet.dismiss();
        });
        rv.setAdapter(adapter);
        sheet.setContentView(view);
        sheet.show();
    }

    private void onVideoEnded() {
        saveResumeState(true);
        if (!playlist.isEmpty() && currentPlaylistIndex < playlist.size() - 1) {
            currentPlaylistIndex++;
            player.seekToNextMediaItem();
            player.play();
        } else {
            showControls();
        }
    }

    private void updateTitle() {
        String title = getVideoTitle();
        tvTitle.setText(title);
        viewModel.setCurrentSource(currentSource, title);
    }

    private String getVideoTitle() {
        if (player == null) return "";
        androidx.media3.common.MediaItem item = player.getCurrentMediaItem();
        if (item != null && item.mediaMetadata.title != null) {
            return item.mediaMetadata.title.toString();
        }
        if (currentSource != null) {
            String path = Uri.parse(currentSource).getLastPathSegment();
            if (path != null) {
                int dot = path.lastIndexOf('.');
                return dot > 0 ? path.substring(0, dot) : path;
            }
        }
        return "Reva Player";
    }

    private final Runnable progressUpdater = new Runnable() {
        @Override
        public void run() {
            updateProgress();
            handler.postDelayed(this, 500);
        }
    };

    private void startProgressUpdater() {
        handler.post(progressUpdater);
    }

    private void updateProgress() {
        if (player == null) return;
        long pos = player.getCurrentPosition();
        long dur = player.getDuration();
        if (dur > 0) {
            int progress = (int) (pos * 1000L / dur);
            if (!seekBar.isPressed()) seekBar.setProgress(progress);
            tvCurrentTime.setText(formatTime(pos));
            tvDuration.setText(formatTime(dur));
        }
        saveResumeState(false);
    }

    private void saveResumeState(boolean completed) {
        if (currentSource == null || player == null) return;
        long pos = player.getCurrentPosition();
        long dur = player.getDuration();
        if (dur > 0) {
            viewModel.saveResumeState(currentSource, getVideoTitle(),
                    pos / 1000.0, dur / 1000.0, completed);
        }
    }

    private void observeViewModel() {
        // لاحق يمكن إضافة observers لـ bookmarks إلخ
    }

    private String formatTime(long ms) {
        long s = ms / 1000;
        long h = s / 3600;
        long m = (s % 3600) / 60;
        long sec = s % 60;
        if (h > 0) return String.format(Locale.US, "%d:%02d:%02d", h, m, sec);
        return String.format(Locale.US, "%d:%02d", m, sec);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null && player.isPlaying()) {
            player.pause();
            saveResumeState(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (player != null) {
            saveResumeState(false);
            player.release();
            player = null;
        }
    }

    @Override
    public void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && player != null && player.isPlaying()) {
            Rational ratio = new Rational(16, 9);
            PictureInPictureParams params = new PictureInPictureParams.Builder()
                    .setAspectRatio(ratio)
                    .build();
            enterPictureInPictureMode(params);
        }
    }
}
