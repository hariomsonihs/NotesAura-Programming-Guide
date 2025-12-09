package com.hariomsonihs.notesaura.models;

import java.util.Date;

public class AppNotification {
    private String id;
    private String userId;
    private String title;
    private String message;
    private String type; // "course", "ebook", "practice", "interview", "category"
    private String targetId; // ID of the item to open
    private String imageUrl;
    private long timestamp;
    private boolean isRead;

    public AppNotification() {}

    public AppNotification(String title, String message, String type, String targetId, String imageUrl) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.targetId = targetId;
        this.imageUrl = imageUrl;
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public Date getTimestampAsDate() { return new Date(timestamp); }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public boolean getIsRead() { return isRead; }
    public void setIsRead(boolean isRead) { this.isRead = isRead; }
}