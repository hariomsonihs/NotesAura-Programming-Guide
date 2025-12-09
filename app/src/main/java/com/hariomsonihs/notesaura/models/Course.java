package com.hariomsonihs.notesaura.models;

public class Course {
    private String id;
    private String title;
    private String description;
    private String category;
    private String imageUrl;
    private boolean isFree;
    private String difficulty;
    private int duration; // in minutes
    private int enrolledCount;
    private float rating;
    private double price;

    public Course() {}

    public Course(String id, String title, String description, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.isFree = true;
        this.difficulty = "Beginner";
        this.duration = 30;
        this.enrolledCount = 0;
        this.rating = 4.5f;
        this.price = 0.0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isFree() { return isFree; }
    public void setFree(boolean free) { isFree = free; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public int getEnrolledCount() { return enrolledCount; }
    public void setEnrolledCount(int enrolledCount) { this.enrolledCount = enrolledCount; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}