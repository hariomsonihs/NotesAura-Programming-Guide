package com.hariomsonihs.notesaura.models;

public class InterviewQuestion {
    private String id;
    private String title;
    private String description;
    private String webLink;
    private String categoryId;
    private int order;

    public InterviewQuestion() {}

    public InterviewQuestion(String id, String title, String description, String webLink, String categoryId, int order) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.webLink = webLink;
        this.categoryId = categoryId;
        this.order = order;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getWebLink() { return webLink; }
    public void setWebLink(String webLink) { this.webLink = webLink; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}