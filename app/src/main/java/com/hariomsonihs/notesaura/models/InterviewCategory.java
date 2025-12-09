package com.hariomsonihs.notesaura.models;

public class InterviewCategory {
    private String id;
    private String name;
    private String description;
    private String color;
    private int order;

    public InterviewCategory() {}

    public InterviewCategory(String id, String name, String description, String color, int order) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.color = color;
        this.order = order;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}