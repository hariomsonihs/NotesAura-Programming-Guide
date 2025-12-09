package com.hariomsonihs.notesaura.models;

import java.util.Date;

public class EnrolledCourse {
    private String courseId;
    private String courseTitle;
    private String category;
    private int progressPercentage;
    private Date enrollmentDate;
    private Date lastAccessed;
    private boolean isCompleted;

    public EnrolledCourse() {}

    public EnrolledCourse(String courseId, String courseTitle, String category) {
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.category = category;
        this.progressPercentage = 0;
        this.enrollmentDate = new Date();
        this.lastAccessed = new Date();
        this.isCompleted = false;
    }

    // Getters and Setters
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(int progressPercentage) { this.progressPercentage = progressPercentage; }

    public Date getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(Date enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    public Date getLastAccessed() { return lastAccessed; }
    public void setLastAccessed(Date lastAccessed) { this.lastAccessed = lastAccessed; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}