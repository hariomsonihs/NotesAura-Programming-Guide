package com.hariomsonihs.notesaura.models;

public class PracticeList {
    private String id;
    private String name;
    private String description;
    private String categoryId;
    private int order;

    public PracticeList() {}

    public PracticeList(String id, String name, String description, String categoryId, int order) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.order = order;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}
