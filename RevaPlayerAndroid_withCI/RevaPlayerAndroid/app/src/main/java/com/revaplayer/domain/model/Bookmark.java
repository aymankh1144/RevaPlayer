package com.revaplayer.domain.model;

public class Bookmark {
    private long id;
    private String source;
    private String title;
    private String note;
    private String category;
    private double positionSeconds;
    private String createdAt;

    public Bookmark() {}

    public Bookmark(String source, String title, double positionSeconds, String note, String category) {
        this.source = source;
        this.title = title;
        this.positionSeconds = positionSeconds;
        this.note = note;
        this.category = category;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPositionSeconds() { return positionSeconds; }
    public void setPositionSeconds(double positionSeconds) { this.positionSeconds = positionSeconds; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getFormattedPosition() {
        long s = (long) positionSeconds;
        long h = s / 3600;
        long m = (s % 3600) / 60;
        long sec = s % 60;
        if (h > 0) return String.format("%d:%02d:%02d", h, m, sec);
        return String.format("%d:%02d", m, sec);
    }
}
