package com.hariomsonihs.notesaura.utils;

import android.content.Context;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class NotificationTester {
    private static final String TAG = "NotificationTester";
    
    public static void sendTestNotifications(Context context) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User not authenticated");
            return;
        }
        
        String userId = currentUser.getUid();
        
        // Test 1: Course notification
        sendTestCourseNotification(context, userId);
        
        // Test 2: Ebook notification
        new android.os.Handler().postDelayed(() -> 
            sendTestEbookNotification(context, userId), 1500);
        
        // Test 3: Interview notification
        new android.os.Handler().postDelayed(() -> 
            sendTestInterviewNotification(context, userId), 3000);
        
        // Test 4: Practice notification
        new android.os.Handler().postDelayed(() -> 
            sendTestPracticeNotification(context, userId), 4500);
        
        // Test 5: Exercise notification
        new android.os.Handler().postDelayed(() -> 
            sendTestExerciseNotification(context, userId), 6000);
    }
    
    private static void sendTestCourseNotification(Context context, String userId) {
        // Save to database
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("userId", userId);
        notificationData.put("title", "🚀 New Course Added!");
        notificationData.put("message", "Check out our latest Java Programming course with hands-on exercises");
        notificationData.put("type", "course");
        notificationData.put("targetId", "java_basics_course");
        notificationData.put("timestamp", System.currentTimeMillis());
        notificationData.put("isRead", false);
        notificationData.put("platform", "android");
        
        FirebaseFirestore.getInstance()
            .collection("app_notifications")
            .add(notificationData)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Test course notification saved to database");
                // Show local notification
                NotificationHelper.showLocalNotification(context, 
                    "🚀 New Course Added!", 
                    "Check out our latest Java Programming course with hands-on exercises", 
                    "course", 
                    "java_basics_course");
            })
            .addOnFailureListener(e -> Log.e(TAG, "Failed to save test course notification", e));
    }
    
    private static void sendTestCustomNotification(Context context, String userId) {
        // Save to database
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("userId", userId);
        notificationData.put("title", "📢 Important Update");
        notificationData.put("message", "We've added new features to enhance your learning experience. Update the app now!");
        notificationData.put("type", "custom");
        notificationData.put("targetId", null);
        notificationData.put("timestamp", System.currentTimeMillis());
        notificationData.put("isRead", false);
        notificationData.put("platform", "android");
        
        FirebaseFirestore.getInstance()
            .collection("app_notifications")
            .add(notificationData)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Test custom notification saved to database");
                // Show local notification
                NotificationHelper.showLocalNotification(context, 
                    "📢 Important Update", 
                    "We've added new features to enhance your learning experience. Update the app now!", 
                    "custom", 
                    null);
            })
            .addOnFailureListener(e -> Log.e(TAG, "Failed to save test custom notification", e));
    }
    
    private static void sendTestEbookNotification(Context context, String userId) {
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("userId", userId);
        notificationData.put("title", "📚 New Ebook Added!");
        notificationData.put("message", "Check out the latest programming ebook - Advanced Java Concepts");
        notificationData.put("type", "ebook");
        notificationData.put("targetId", "java_advanced_ebook");
        notificationData.put("timestamp", System.currentTimeMillis());
        notificationData.put("isRead", false);
        notificationData.put("platform", "android");
        
        FirebaseFirestore.getInstance()
            .collection("app_notifications")
            .add(notificationData)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Test ebook notification saved to database");
                NotificationHelper.showLocalNotification(context, 
                    "📚 New Ebook Added!", 
                    "Check out the latest programming ebook - Advanced Java Concepts", 
                    "ebook", 
                    "java_advanced_ebook");
            })
            .addOnFailureListener(e -> Log.e(TAG, "Failed to save test ebook notification", e));
    }
    
    private static void sendTestInterviewNotification(Context context, String userId) {
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("userId", userId);
        notificationData.put("title", "🎯 New Interview Questions!");
        notificationData.put("message", "Fresh interview questions added for Java Developer positions");
        notificationData.put("type", "interview");
        notificationData.put("targetId", "java_interview_category");
        notificationData.put("timestamp", System.currentTimeMillis());
        notificationData.put("isRead", false);
        notificationData.put("platform", "android");
        
        FirebaseFirestore.getInstance()
            .collection("app_notifications")
            .add(notificationData)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Test interview notification saved to database");
                NotificationHelper.showLocalNotification(context, 
                    "🎯 New Interview Questions!", 
                    "Fresh interview questions added for Java Developer positions", 
                    "interview", 
                    "java_interview_category");
            })
            .addOnFailureListener(e -> Log.e(TAG, "Failed to save test interview notification", e));
    }
    
    private static void sendTestPracticeNotification(Context context, String userId) {
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("userId", userId);
        notificationData.put("title", "💻 New Practice Exercises!");
        notificationData.put("message", "New coding practice problems added - Test your skills now!");
        notificationData.put("type", "practice");
        notificationData.put("targetId", "coding_practice_set");
        notificationData.put("timestamp", System.currentTimeMillis());
        notificationData.put("isRead", false);
        notificationData.put("platform", "android");
        
        FirebaseFirestore.getInstance()
            .collection("app_notifications")
            .add(notificationData)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Test practice notification saved to database");
                NotificationHelper.showLocalNotification(context, 
                    "💻 New Practice Exercises!", 
                    "New coding practice problems added - Test your skills now!", 
                    "practice", 
                    "coding_practice_set");
            })
            .addOnFailureListener(e -> Log.e(TAG, "Failed to save test practice notification", e));
    }
    
    private static void sendTestExerciseNotification(Context context, String userId) {
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("userId", userId);
        notificationData.put("title", "📝 Course Updated!");
        notificationData.put("message", "New exercises added to Java Programming course - Check them out!");
        notificationData.put("type", "exercise");
        notificationData.put("targetId", "java_basics_course");
        notificationData.put("timestamp", System.currentTimeMillis());
        notificationData.put("isRead", false);
        notificationData.put("platform", "android");
        
        FirebaseFirestore.getInstance()
            .collection("app_notifications")
            .add(notificationData)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Test exercise notification saved to database");
                NotificationHelper.showLocalNotification(context, 
                    "📝 Course Updated!", 
                    "New exercises added to Java Programming course - Check them out!", 
                    "exercise", 
                    "java_basics_course");
            })
            .addOnFailureListener(e -> Log.e(TAG, "Failed to save test exercise notification", e));
    }
    
    public static void testNotificationPermissions(Context context) {
        boolean systemEnabled = androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled();
        android.content.SharedPreferences prefs = context.getSharedPreferences("NotificationSettings", Context.MODE_PRIVATE);
        boolean appEnabled = prefs.getBoolean("notifications_enabled", true);
        
        Log.d(TAG, "System notifications enabled: " + systemEnabled);
        Log.d(TAG, "App notifications enabled: " + appEnabled);
        
        if (!systemEnabled) {
            Log.w(TAG, "System notifications are disabled!");
        }
        if (!appEnabled) {
            Log.w(TAG, "App notifications are disabled!");
        }
        
        if (systemEnabled && appEnabled) {
            Log.d(TAG, "All notification permissions are good!");
        }
    }
}