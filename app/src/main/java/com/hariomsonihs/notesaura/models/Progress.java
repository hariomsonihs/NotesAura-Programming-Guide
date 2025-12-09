package com.hariomsonihs.notesaura.models;

import java.util.Date;
import java.util.List;

public class Progress {
    private String id;
    private String userId;
    private String courseId;
    private List<String> completedExercises;
    private int progressPercentage;
    private Date lastAccessed;
    private long timeSpent; // in milliseconds
    private boolean isCompleted;
    private Date completionDate;

    public Progress() {}

    public Progress(String userId, String courseId) {
        this.userId = userId;
        this.courseId = courseId;
        this.progressPercentage = 0;
        this.lastAccessed = new Date();
        this.timeSpent = 0;
        this.isCompleted = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public List<String> getCompletedExercises() { return completedExercises; }
    public void setCompletedExercises(List<String> completedExercises) { this.completedExercises = completedExercises; }

    public int getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(int progressPercentage) { this.progressPercentage = progressPercentage; }

    public Date getLastAccessed() { return lastAccessed; }
    public void setLastAccessed(Date lastAccessed) { this.lastAccessed = lastAccessed; }

    public long getTimeSpent() { return timeSpent; }
    public void setTimeSpent(long timeSpent) { this.timeSpent = timeSpent; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public Date getCompletionDate() { return completionDate; }
    public void setCompletionDate(Date completionDate) { this.completionDate = completionDate; }
}