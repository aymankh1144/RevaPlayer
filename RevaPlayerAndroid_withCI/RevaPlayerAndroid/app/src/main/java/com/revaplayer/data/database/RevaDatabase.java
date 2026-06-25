package com.revaplayer.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.revaplayer.domain.model.Bookmark;
import com.revaplayer.domain.model.MediaItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RevaDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "reva_player.db";
    private static final int DB_VERSION = 1;

    // Tables
    private static final String TABLE_HISTORY = "playback_history";
    private static final String TABLE_RESUME = "resume_state";
    private static final String TABLE_BOOKMARKS = "bookmarks";
    private static final String TABLE_SETTINGS = "settings";

    // Columns - History
    private static final String COL_ID = "id";
    private static final String COL_SOURCE = "source";
    private static final String COL_TITLE = "title";
    private static final String COL_POSITION = "position_seconds";
    private static final String COL_DURATION = "duration_seconds";
    private static final String COL_COMPLETED = "completed";
    private static final String COL_LAST_OPENED = "last_opened_at";
    private static final String COL_UPDATED_AT = "updated_at";

    // Columns - Bookmarks
    private static final String COL_NOTE = "note";
    private static final String COL_CATEGORY = "category";
    private static final String COL_CREATED_AT = "created_at";

    // Columns - Settings
    private static final String COL_KEY = "key";
    private static final String COL_VALUE = "value";

    private static RevaDatabase instance;

    public static synchronized RevaDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new RevaDatabase(context.getApplicationContext());
        }
        return instance;
    }

    private RevaDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // History table
        db.execSQL("CREATE TABLE " + TABLE_HISTORY + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_SOURCE + " TEXT UNIQUE NOT NULL," +
                COL_TITLE + " TEXT," +
                COL_POSITION + " REAL DEFAULT 0," +
                COL_DURATION + " REAL DEFAULT 0," +
                COL_COMPLETED + " INTEGER DEFAULT 0," +
                COL_LAST_OPENED + " TEXT," +
                COL_UPDATED_AT + " TEXT" +
                ")");

        // Resume state table
        db.execSQL("CREATE TABLE " + TABLE_RESUME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_SOURCE + " TEXT UNIQUE NOT NULL," +
                COL_TITLE + " TEXT," +
                COL_POSITION + " REAL DEFAULT 0," +
                COL_DURATION + " REAL DEFAULT 0," +
                COL_UPDATED_AT + " TEXT" +
                ")");

        // Bookmarks table
        db.execSQL("CREATE TABLE " + TABLE_BOOKMARKS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_SOURCE + " TEXT NOT NULL," +
                COL_TITLE + " TEXT," +
                COL_POSITION + " REAL DEFAULT 0," +
                COL_NOTE + " TEXT," +
                COL_CATEGORY + " TEXT DEFAULT 'عام'," +
                COL_CREATED_AT + " TEXT" +
                ")");

        // Settings table
        db.execSQL("CREATE TABLE " + TABLE_SETTINGS + " (" +
                COL_KEY + " TEXT PRIMARY KEY," +
                COL_VALUE + " TEXT" +
                ")");

        // Insert defaults
        insertDefaultSettings(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESUME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKMARKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
        onCreate(db);
    }

    private void insertDefaultSettings(SQLiteDatabase db) {
        insertSetting(db, "theme", "dark");
        insertSetting(db, "speed", "1.0");
        insertSetting(db, "history_limit", "100");
        insertSetting(db, "auto_resume", "true");
        insertSetting(db, "pip_enabled", "true");
    }

    private void insertSetting(SQLiteDatabase db, String key, String value) {
        ContentValues cv = new ContentValues();
        cv.put(COL_KEY, key);
        cv.put(COL_VALUE, value);
        db.insertWithOnConflict(TABLE_SETTINGS, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
    }

    private String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
    }

    // ─── History ────────────────────────────────────────────────────────────────

    public void saveHistory(String source, String title, double position, double duration, boolean completed) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_SOURCE, source);
        cv.put(COL_TITLE, title);
        cv.put(COL_POSITION, position);
        cv.put(COL_DURATION, duration);
        cv.put(COL_COMPLETED, completed ? 1 : 0);
        cv.put(COL_LAST_OPENED, now());
        cv.put(COL_UPDATED_AT, now());
        db.insertWithOnConflict(TABLE_HISTORY, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        pruneHistory(100);
    }

    public List<MediaItem> loadHistory(int limit) {
        List<MediaItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_HISTORY, null, null, null, null, null,
                COL_LAST_OPENED + " DESC", String.valueOf(limit));
        while (c.moveToNext()) {
            MediaItem item = new MediaItem();
            item.setSource(c.getString(c.getColumnIndexOrThrow(COL_SOURCE)));
            item.setDisplayTitle(c.getString(c.getColumnIndexOrThrow(COL_TITLE)));
            item.setPositionSeconds(c.getDouble(c.getColumnIndexOrThrow(COL_POSITION)));
            item.setDurationSeconds(c.getDouble(c.getColumnIndexOrThrow(COL_DURATION)));
            item.setCompleted(c.getInt(c.getColumnIndexOrThrow(COL_COMPLETED)) == 1);
            item.setLastOpenedAt(c.getString(c.getColumnIndexOrThrow(COL_LAST_OPENED)));
            items.add(item);
        }
        c.close();
        return items;
    }

    public void deleteHistoryItem(String source) {
        getWritableDatabase().delete(TABLE_HISTORY, COL_SOURCE + "=?", new String[]{source});
    }

    public void clearHistory() {
        getWritableDatabase().delete(TABLE_HISTORY, null, null);
    }

    private void pruneHistory(int max) {
        getWritableDatabase().execSQL(
                "DELETE FROM " + TABLE_HISTORY + " WHERE " + COL_ID + " NOT IN " +
                "(SELECT " + COL_ID + " FROM " + TABLE_HISTORY + " ORDER BY " + COL_LAST_OPENED + " DESC LIMIT " + max + ")");
    }

    // ─── Resume State ────────────────────────────────────────────────────────────

    public void saveResumeState(String source, String title, double position, double duration) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_SOURCE, source);
        cv.put(COL_TITLE, title);
        cv.put(COL_POSITION, position);
        cv.put(COL_DURATION, duration);
        cv.put(COL_UPDATED_AT, now());
        db.insertWithOnConflict(TABLE_RESUME, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public MediaItem loadResumeState(String source) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RESUME, null, COL_SOURCE + "=?",
                new String[]{source}, null, null, null);
        if (c.moveToFirst()) {
            MediaItem item = new MediaItem();
            item.setSource(c.getString(c.getColumnIndexOrThrow(COL_SOURCE)));
            item.setDisplayTitle(c.getString(c.getColumnIndexOrThrow(COL_TITLE)));
            item.setPositionSeconds(c.getDouble(c.getColumnIndexOrThrow(COL_POSITION)));
            item.setDurationSeconds(c.getDouble(c.getColumnIndexOrThrow(COL_DURATION)));
            c.close();
            return item;
        }
        c.close();
        return null;
    }

    public MediaItem loadLastResumeState() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RESUME, null, null, null, null, null,
                COL_UPDATED_AT + " DESC", "1");
        if (c.moveToFirst()) {
            MediaItem item = new MediaItem();
            item.setSource(c.getString(c.getColumnIndexOrThrow(COL_SOURCE)));
            item.setDisplayTitle(c.getString(c.getColumnIndexOrThrow(COL_TITLE)));
            item.setPositionSeconds(c.getDouble(c.getColumnIndexOrThrow(COL_POSITION)));
            item.setDurationSeconds(c.getDouble(c.getColumnIndexOrThrow(COL_DURATION)));
            c.close();
            return item;
        }
        c.close();
        return null;
    }

    public void clearResumeState(String source) {
        getWritableDatabase().delete(TABLE_RESUME, COL_SOURCE + "=?", new String[]{source});
    }

    // ─── Bookmarks ────────────────────────────────────────────────────────────────

    public long addBookmark(Bookmark bookmark) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_SOURCE, bookmark.getSource());
        cv.put(COL_TITLE, bookmark.getTitle());
        cv.put(COL_POSITION, bookmark.getPositionSeconds());
        cv.put(COL_NOTE, bookmark.getNote());
        cv.put(COL_CATEGORY, bookmark.getCategory() != null ? bookmark.getCategory() : "عام");
        cv.put(COL_CREATED_AT, now());
        return db.insert(TABLE_BOOKMARKS, null, cv);
    }

    public List<Bookmark> loadBookmarks(String source) {
        List<Bookmark> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_BOOKMARKS, null, COL_SOURCE + "=?",
                new String[]{source}, null, null, COL_POSITION + " ASC");
        while (c.moveToNext()) {
            Bookmark b = new Bookmark();
            b.setId(c.getLong(c.getColumnIndexOrThrow(COL_ID)));
            b.setSource(c.getString(c.getColumnIndexOrThrow(COL_SOURCE)));
            b.setTitle(c.getString(c.getColumnIndexOrThrow(COL_TITLE)));
            b.setPositionSeconds(c.getDouble(c.getColumnIndexOrThrow(COL_POSITION)));
            b.setNote(c.getString(c.getColumnIndexOrThrow(COL_NOTE)));
            b.setCategory(c.getString(c.getColumnIndexOrThrow(COL_CATEGORY)));
            b.setCreatedAt(c.getString(c.getColumnIndexOrThrow(COL_CREATED_AT)));
            list.add(b);
        }
        c.close();
        return list;
    }

    public List<Bookmark> loadAllBookmarks() {
        List<Bookmark> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_BOOKMARKS, null, null, null, null, null,
                COL_CREATED_AT + " DESC");
        while (c.moveToNext()) {
            Bookmark b = new Bookmark();
            b.setId(c.getLong(c.getColumnIndexOrThrow(COL_ID)));
            b.setSource(c.getString(c.getColumnIndexOrThrow(COL_SOURCE)));
            b.setTitle(c.getString(c.getColumnIndexOrThrow(COL_TITLE)));
            b.setPositionSeconds(c.getDouble(c.getColumnIndexOrThrow(COL_POSITION)));
            b.setNote(c.getString(c.getColumnIndexOrThrow(COL_NOTE)));
            b.setCategory(c.getString(c.getColumnIndexOrThrow(COL_CATEGORY)));
            b.setCreatedAt(c.getString(c.getColumnIndexOrThrow(COL_CREATED_AT)));
            list.add(b);
        }
        c.close();
        return list;
    }

    public void deleteBookmark(long id) {
        getWritableDatabase().delete(TABLE_BOOKMARKS, COL_ID + "=?",
                new String[]{String.valueOf(id)});
    }

    // ─── Settings ────────────────────────────────────────────────────────────────

    public String getSetting(String key, String defaultValue) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_SETTINGS, new String[]{COL_VALUE},
                COL_KEY + "=?", new String[]{key}, null, null, null);
        if (c.moveToFirst()) {
            String val = c.getString(0);
            c.close();
            return val != null ? val : defaultValue;
        }
        c.close();
        return defaultValue;
    }

    public void setSetting(String key, String value) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_KEY, key);
        cv.put(COL_VALUE, value);
        db.insertWithOnConflict(TABLE_SETTINGS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void resetAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_HISTORY, null, null);
        db.delete(TABLE_RESUME, null, null);
        db.delete(TABLE_BOOKMARKS, null, null);
        insertDefaultSettings(db);
    }
}
