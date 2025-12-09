package com.hariomsonihs.notesaura.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.activities.MainActivity;
import com.hariomsonihs.notesaura.activities.NotificationsActivity;
import com.hariomsonihs.notesaura.models.AppNotification;
import java.util.HashMap;
import java.util.Map;

public class NotificationHelper {
    private static final String COLLECTION_NOTIFICATIONS = "notifications";
    private static final String CHANNEL_ID = "notesaura_notifications";
    
    public static void saveNotification(Context context, String userId, String title, String message, 
                                      String type, String targetId, String imageUrl) {
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("userId", userId);
        notificationData.put("title", title);
        notificationData.put("message", message);
        notificationData.put("type", type);
        notificationData.put("targetId", targetId);
        notificationData.put("imageUrl", imageUrl != null ? imageUrl : "");
        notificationData.put("timestamp", System.currentTimeMillis());
        notificationData.put("isRead", false);
        
        FirebaseFirestore.getInstance()
            .collection("app_notifications")
            .add(notificationData);
    }
    
    public static void sendNotificationToAllUsers(String title, String message, String type, 
                                                String targetId, String imageUrl) {
        // This method should be called from admin panel to send to all users
        // For now, we'll just log it - actual implementation would iterate through all users
        android.util.Log.d("NotificationHelper", "Sending notification to all users: " + title);
    }
    
    public static void sendTestNotification(String userId) {
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("userId", userId);
        notificationData.put("title", "Test Notification");
        notificationData.put("message", "This is a test notification from the app");
        notificationData.put("type", "general");
        notificationData.put("targetId", "");
        notificationData.put("imageUrl", "");
        notificationData.put("timestamp", System.currentTimeMillis());
        notificationData.put("isRead", false);
        
        FirebaseFirestore.getInstance()
            .collection("app_notifications")
            .add(notificationData)
            .addOnSuccessListener(documentReference -> {
                android.util.Log.d("NotificationHelper", "Test notification sent successfully");
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("NotificationHelper", "Failed to send test notification", e);
            });
    }
    
    public static void sendTestNotificationWithSystemNotification(Context context, String userId) {
        // Save to database first
        sendTestNotification(userId);
        
        // Show system notification
        showLocalNotification(context, "New Course Added!", "Check out the latest programming course", "course", "test_course_id");
    }
    
    public static void showLocalNotification(Context context, String title, String message, String type, String targetId) {
        try {
            createNotificationChannel(context);
            
            // Create intent based on notification type
            Intent intent = getNotificationIntent(context, type, targetId);
            
            // Use unique request code to avoid PendingIntent conflicts
            int requestCode = (int) (System.currentTimeMillis() & 0x7fffffff);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, requestCode, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            
            android.util.Log.d("NotificationHelper", "Creating local notification: " + title);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(android.graphics.BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setVibrate(new long[]{0, 250, 250, 250})
                    .setLights(0xFF0000FF, 500, 500)
                    .setShowWhen(true)
                    .setWhen(System.currentTimeMillis())
                    .setSubText("NotesAura")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.notify(requestCode, builder.build());
                android.util.Log.d("NotificationHelper", "Local notification displayed in system tray with ID: " + requestCode);
            }
        } catch (Exception e) {
            android.util.Log.e("NotificationHelper", "Error showing local notification", e);
        }
    }
    
    private static Intent getNotificationIntent(Context context, String type, String targetId) {
        // ALWAYS open MainActivity with notification flag - ignore all type/targetId data
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("open_notifications", true);
        intent.putExtra("highlight_new", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }
    
    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            
            if (notificationManager != null) {
                // Create default channel
                NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "NotesAura Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Notifications for new courses, ebooks and updates");
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{0, 250, 250, 250});
                channel.enableLights(true);
                channel.setLightColor(android.graphics.Color.BLUE);
                
                notificationManager.createNotificationChannel(channel);
                android.util.Log.d("NotificationHelper", "Notification channel created");
            }
        }
    }
}