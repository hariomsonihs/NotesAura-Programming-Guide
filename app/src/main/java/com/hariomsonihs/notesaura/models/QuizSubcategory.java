package com.hariomsonihs.notesaura.models;

public class QuizSubcategory {
    private String id;
    private String name;
    private String categoryId;
    private String webUrl;
    private int order;

    public QuizSubcategory() {}

    public QuizSubcategory(String id, String name, String categoryId, String webUrl, int order) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.webUrl = webUrl;
        this.order = order;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getWebUrl() { return webUrl; }
    public void setWebUrl(String webUrl) { this.webUrl = webUrl; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}