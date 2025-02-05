package com.example.sukasolat;

public class PrayerTimeItem {
    private int iconResId;
    private String name;
    private String time;
    private boolean isHighlighted; // For next prayer time
    private boolean isCurrentHighlighted; // For current prayer time

    public PrayerTimeItem(int iconResId, String name, String time) {
        this.iconResId = iconResId;
        this.name = name;
        this.time = time;
        this.isHighlighted = false;
        this.isCurrentHighlighted = false;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
    }

    public boolean isCurrentHighlighted() {
        return isCurrentHighlighted;
    }

    public void setCurrentHighlighted(boolean currentHighlighted) {
        isCurrentHighlighted = currentHighlighted;
    }
}