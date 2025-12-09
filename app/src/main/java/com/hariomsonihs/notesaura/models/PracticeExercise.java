package com.hariomsonihs.notesaura.models;

public class PracticeExercise {
    private String id;
    private String title;
    private String name;
    private String webLink;
    private String practiceListId;
    private int order;

    public PracticeExercise() {}

    public PracticeExercise(String id, String title, String name, String webLink, String practiceListId, int order) {
        this.id = id;
        this.title = title;
        this.name = name;
        this.webLink = webLink;
        this.practiceListId = practiceListId;
        this.order = order;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getWebLink() { return webLink; }
    public void setWebLink(String webLink) { this.webLink = webLink; }
    
    // Keep backward compatibility
    public String getDescription() { return name; }
    public void setDescription(String description) { this.name = description; }
    public String getFileLink() { return webLink; }
    public void setFileLink(String fileLink) { this.webLink = fileLink; }
    
    public String getPracticeListId() { return practiceListId; }
    public void setPracticeListId(String practiceListId) { this.practiceListId = practiceListId; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}
