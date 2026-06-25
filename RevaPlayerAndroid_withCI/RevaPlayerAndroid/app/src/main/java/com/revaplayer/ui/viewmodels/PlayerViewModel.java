package com.revaplayer.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.revaplayer.data.database.RevaDatabase;
import com.revaplayer.domain.model.Bookmark;
import com.revaplayer.domain.model.MediaItem;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerViewModel extends AndroidViewModel {

    private final RevaDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private String currentSource;
    private String currentTitle;

    private final MutableLiveData<List<Bookmark>> bookmarksLive = new MutableLiveData<>();

    public PlayerViewModel(@NonNull Application application) {
        super(application);
        db = RevaDatabase.getInstance(application);
    }

    public void setCurrentSource(String source, String title) {
        this.currentSource = source;
        this.currentTitle = title;
        loadBookmarks();
    }

    public LiveData<List<Bookmark>> getBookmarks() { return bookmarksLive; }

    public void saveResumeState(String source, String title, double position, double duration, boolean completed) {
        executor.execute(() -> {
            db.saveResumeState(source, title, position, duration);
            db.saveHistory(source, title, position, duration, completed);
        });
    }

    public void addBookmark(Bookmark bookmark) {
        executor.execute(() -> {
            db.addBookmark(bookmark);
            loadBookmarks();
        });
    }

    public void deleteBookmark(long id) {
        executor.execute(() -> {
            db.deleteBookmark(id);
            loadBookmarks();
        });
    }

    private void loadBookmarks() {
        if (currentSource == null) return;
        executor.execute(() -> bookmarksLive.postValue(db.loadBookmarks(currentSource)));
    }
}
