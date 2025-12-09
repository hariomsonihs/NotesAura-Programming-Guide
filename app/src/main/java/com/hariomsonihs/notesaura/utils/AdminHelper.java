package com.hariomsonihs.notesaura.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.Task;
import java.util.List;
import java.util.ArrayList;
import com.hariomsonihs.notesaura.models.User;
import com.hariomsonihs.notesaura.models.Payment;
import com.hariomsonihs.notesaura.models.UserCourse;

public class AdminHelper {
    private static AdminHelper instance;
    private FirebaseFirestore db;
    
    private AdminHelper() {
        db = FirebaseFirestore.getInstance();
    }
    
    public static AdminHelper getInstance() {
        if (instance == null) {
            instance = new AdminHelper();
        }
        return instance;
    }
    
    public Task<DocumentSnapshot> checkAdminStatus(String userId) {
        return db.collection(Constants.COLLECTION_USERS).document(userId).get();
    }
    
    public boolean isCurrentUserAdmin(DocumentSnapshot userDoc) {
        if (userDoc.exists()) {
            String adminStatus = userDoc.getString("admin");
            return "yes".equalsIgnoreCase(adminStatus);
        }
        return false;
    }
    
    public Task<Void> setAdminStatus(String userId, boolean isAdmin) {
        return db.collection("users").document(userId)
                .update("admin", isAdmin ? "yes" : "no");
    }
    
    public interface CountCallback {
        void onCount(int count);
    }
    
    public interface RevenueCallback {
        void onRevenue(double revenue);
    }
    
    public interface UsersCallback {
        void onUsers(List<User> users);
    }
    
    public interface PaymentsCallback {
        void onPayments(List<Payment> payments);
    }
    
    public interface UserCoursesCallback {
        void onUserCourses(List<UserCourse> userCourses);
    }
    
    public void getTotalUsers(CountCallback callback) {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    callback.onCount(queryDocumentSnapshots.size());
                })
                .addOnFailureListener(e -> callback.onCount(0));
    }
    
    public void getTotalCourses(CountCallback callback) {
        // Using CourseDataManager to get course count
        CourseDataManager dataManager = CourseDataManager.getInstance();
        callback.onCount(dataManager.getAllCourses().size());
    }
    
    public void getTotalPayments(CountCallback callback) {
        db.collectionGroup("payments")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    callback.onCount(queryDocumentSnapshots.size());
                })
                .addOnFailureListener(e -> callback.onCount(0));
    }
    
    public void getTotalRevenue(RevenueCallback callback) {
        db.collectionGroup("payments")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalRevenue = 0.0;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String status = doc.getString("status");
                        Double amount = doc.getDouble("amountPaid");
                        if (amount != null && status != null && status.equalsIgnoreCase("success")) {
                            totalRevenue += amount;
                        }
                    }
                    callback.onRevenue(totalRevenue);
                })
                .addOnFailureListener(e -> callback.onRevenue(0.0));
    }
    
    public void getAllUsers(UsersCallback callback) {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> users = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            user.setId(doc.getId());
                            users.add(user);
                        }
                    }
                    callback.onUsers(users);
                })
                .addOnFailureListener(e -> callback.onUsers(new ArrayList<>()));
    }
    
    public void getAllPayments(PaymentsCallback callback) {
        db.collectionGroup("payments")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Payment> payments = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Payment payment = doc.toObject(Payment.class);
                        if (payment != null) {
                            payment.setId(doc.getId());
                            payments.add(payment);
                        }
                    }
                    callback.onPayments(payments);
                })
                .addOnFailureListener(e -> callback.onPayments(new ArrayList<>()));
    }
    
    public void getAllUserCourses(UserCoursesCallback callback) {
        db.collectionGroup("enrollments")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserCourse> userCourses = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        UserCourse userCourse = doc.toObject(UserCourse.class);
                        if (userCourse != null) {
                            userCourse.setId(doc.getId());
                            userCourses.add(userCourse);
                        }
                    }
                    callback.onUserCourses(userCourses);
                })
                .addOnFailureListener(e -> callback.onUserCourses(new ArrayList<>()));
    }
}