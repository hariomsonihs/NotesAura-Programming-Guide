package com.hariomsonihs.notesaura.models;

public class Ebook {
    private String id;
    private String subcategoryId;
    private String title;
    private String description;
    private String author;
    private String pdfUrl;
    private String imageUrl;
    private int order;

    public Ebook() {}

    public Ebook(String subcategoryId, String title, String description, String author, String pdfUrl, String imageUrl, int order) {
        this.subcategoryId = subcategoryId;
        this.title = title;
        this.description = description;
        this.author = author;
        this.pdfUrl = pdfUrl;
        this.imageUrl = imageUrl;
        this.order = order;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSubcategoryId() { return subcategoryId; }
    public void setSubcategoryId(String subcategoryId) { this.subcategoryId = subcategoryId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}