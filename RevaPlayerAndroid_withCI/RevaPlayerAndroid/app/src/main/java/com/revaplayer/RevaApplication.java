package com.revaplayer;

import android.app.Application;
import com.revaplayer.data.database.RevaDatabase;

public class RevaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize database on startup
        RevaDatabase.getInstance(this);
    }
}
