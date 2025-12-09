package com.hariomsonihs.notesaura.models;

import java.util.Date;

public class UserCourse {
    private String id;
    private String courseId;
    private String courseName;
    private String category;
    private Date enrollmentDate;
    private Long progressPercentage;
    private Long amountPaid;
    private String paymentId;

    public UserCourse() {}

    // Getters and Setters
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Date getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(Date enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    public Long getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(Long progressPercentage) { this.progressPercentage = progressPercentage; }

    public Long getAmountPaid() { return amountPaid; }
    public void setAmountPaid(Long amountPaid) { this.amountPaid = amountPaid; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}