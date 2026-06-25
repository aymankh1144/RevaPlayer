package com.revaplayer.domain.model;

public class MediaItem {
    private String source;
    private String displayTitle;
    private double positionSeconds;
    private double durationSeconds;
    private boolean completed;
    private String lastOpenedAt;

    public MediaItem() {}

    public MediaItem(String source, String displayTitle, double positionSeconds, double durationSeconds) {
        this.source = source;
        this.displayTitle = displayTitle;
        this.positionSeconds = positionSeconds;
        this.durationSeconds = durationSeconds;
    }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDisplayTitle() { return displayTitle; }
    public void setDisplayTitle(String displayTitle) { this.displayTitle = displayTitle; }

    public double getPositionSeconds() { return positionSeconds; }
    public void setPositionSeconds(double positionSeconds) { this.positionSeconds = positionSeconds; }

    public double getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(double durationSeconds) { this.durationSeconds = durationSeconds; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getLastOpenedAt() { return lastOpenedAt; }
    public void setLastOpenedAt(String lastOpenedAt) { this.lastOpenedAt = lastOpenedAt; }

    public int getProgressPercent() {
        if (durationSeconds <= 0) return 0;
        return (int) ((positionSeconds / durationSeconds) * 100);
    }

    public String getFormattedProgress() {
        return formatTime((long) positionSeconds) + " / " + formatTime((long) durationSeconds);
    }

    private String formatTime(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        if (h > 0) return String.format("%d:%02d:%02d", h, m, s);
        return String.format("%d:%02d", m, s);
    }
}
