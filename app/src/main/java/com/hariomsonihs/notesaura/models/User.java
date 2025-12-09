package com.hariomsonihs.notesaura.models;

import java.util.Date;

public class User {
    private String id;
    private String name;
    private String email;
    private String phone;
    private Date joiningDate;
    private boolean isAdmin;
    private boolean premium;
    private int totalProgress;

    public User() {
        // Default constructor required for Firestore
    }

    public User(String id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.joiningDate = new Date();
        this.isAdmin = false;
        this.premium = false;
        this.totalProgress = 0;
    }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Date getJoiningDate() { return joiningDate; }
    public void setJoiningDate(Date joiningDate) { this.joiningDate = joiningDate; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }

    public boolean isPremium() { return premium; }
    public void setPremium(boolean premium) { this.premium = premium; }

    public int getTotalProgress() { return totalProgress; }
    public void setTotalProgress(int totalProgress) { this.totalProgress = totalProgress; }
}