package com.hariomsonihs.notesaura.models;

public class Exercise {
    private String id;
    private String title;
    private String description;
    private String htmlFilePath;
    private int order;
    private boolean isCompleted;
    private String courseId;
    private int estimatedTime; // in minutes

    public Exercise() {}

    public Exercise(String id, String title, String description, String htmlFilePath, String courseId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.htmlFilePath = htmlFilePath;
        this.courseId = courseId;
        this.isCompleted = false;
        this.estimatedTime = 30;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getHtmlFilePath() { return htmlFilePath; }
    public void setHtmlFilePath(String htmlFilePath) { this.htmlFilePath = htmlFilePath; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public int getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(int estimatedTime) { this.estimatedTime = estimatedTime; }
}