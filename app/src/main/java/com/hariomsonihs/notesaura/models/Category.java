package com.hariomsonihs.notesaura.models;

public class Category {
    private String id;
    private String name;
    private String description;
    private String iconUrl;
    private int courseCount;
    private String color;
    private int order; // For custom ordering

    public Category() {}

    public Category(String id, String name, String description, String color) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.color = color;
        this.courseCount = 0;
        this.order = 0;
    }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }

    public int getCourseCount() { return courseCount; }
    public void setCourseCount(int courseCount) { this.courseCount = courseCount; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}