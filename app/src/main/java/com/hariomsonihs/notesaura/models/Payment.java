package com.hariomsonihs.notesaura.models;

import java.util.Date;

public class Payment {
    private String id;
    private String courseTitle;
    private double amount;
    private Date paymentDate;
    private String status;
    private String userId;
    private String transactionId;

    public Payment() {}

    public Payment(String courseTitle, double amount, Date paymentDate, String status, String userId) {
        this.courseTitle = courseTitle;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.status = status;
        this.userId = userId;
    }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}