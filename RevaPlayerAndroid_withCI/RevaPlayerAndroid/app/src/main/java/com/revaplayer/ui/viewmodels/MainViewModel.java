package com.revaplayer.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.revaplayer.data.database.RevaDatabase;
import com.revaplayer.domain.model.MediaItem;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainViewModel extends AndroidViewModel {

    private final RevaDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<List<MediaItem>> historyLive = new MutableLiveData<>();
    private final MutableLiveData<MediaItem> resumeItemLive = new MutableLiveData<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
        db = RevaDatabase.getInstance(application);
        refreshHistory();
        refreshResumeItem();
    }

    public LiveData<List<MediaItem>> getHistory() { return historyLive; }
    public LiveData<MediaItem> getResumeItem() { return resumeItemLive; }

    public void refreshHistory() {
        executor.execute(() -> historyLive.postValue(db.loadHistory(50)));
    }

    public void refreshResumeItem() {
        executor.execute(() -> resumeItemLive.postValue(db.loadLastResumeState()));
    }

    public void deleteHistoryItem(String source) {
        executor.execute(() -> {
            db.deleteHistoryItem(source);
            refreshHistory();
        });
    }

    public void clearHistory() {
        executor.execute(() -> {
            db.clearHistory();
            refreshHistory();
        });
    }
}
