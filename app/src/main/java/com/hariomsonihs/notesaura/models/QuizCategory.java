package com.hariomsonihs.notesaura.models;

public class QuizCategory {
    private String id;
    private String name;
    private String iconUrl;
    private String color;
    private int order;

    public QuizCategory() {}

    public QuizCategory(String id, String name, String iconUrl, String color, int order) {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
        this.color = color;
        this.order = order;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}