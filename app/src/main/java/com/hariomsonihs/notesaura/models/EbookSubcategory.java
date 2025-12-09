package com.hariomsonihs.notesaura.models;

public class EbookSubcategory {
    private String id;
    private String categoryId;
    private String name;
    private String description;
    private String imageUrl;
    private int order;

    public EbookSubcategory() {}

    public EbookSubcategory(String categoryId, String name, String description, String imageUrl, int order) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.order = order;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}