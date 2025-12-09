package com.hariomsonihs.notesaura.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.activities.NotificationsActivity;
import com.hariomsonihs.notesaura.activities.CourseDetailActivity;
import com.hariomsonihs.notesaura.activities.MainActivity;
import java.util.HashMap;
import java.util.Map;

public class NotificationService extends FirebaseMessagingService {
    private static final String TAG = "NotificationService";
    private static final String CHANNEL_ID = "notesaura_notifications";
    private static final String CHANNEL_NAME = "NotesAura Notifications";
    private static final String HIGH_PRIORITY_CHANNEL_ID = "notesaura_high_priority";
    private static final String HIGH_PRIORITY_CHANNEL_NAME = "NotesAura High Priority";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
        Log.d(TAG, "NotificationService created");
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "FCM Message received - From: " + remoteMessage.getFrom());
        Log.d(TAG, "FCM Data: " + remoteMessage.getData());
        
        // Force wake up device
        android.os.PowerManager powerManager = (android.os.PowerManager) getSystemService(Context.POWER_SERVICE);
        android.os.PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
            android.os.PowerManager.PARTIAL_WAKE_LOCK, "NotesAura:FCMWakeLock");
        wakeLock.acquire(10000); // 10 seconds
        
        try {
            String title = "NotesAura";
            String body = "";
            
            // Handle both notification and data payloads
            if (remoteMessage.getNotification() != null) {
                title = remoteMessage.getNotification().getTitle() != null ? 
                    remoteMessage.getNotification().getTitle() : title;
                body = remoteMessage.getNotification().getBody() != null ? 
                    remoteMessage.getNotification().getBody() : body;
            }
            
            // Get data from message
            Map<String, String> data = remoteMessage.getData();
            if (data.containsKey("title")) {
                title = data.get("title");
            }
            if (data.containsKey("body") || data.containsKey("message")) {
                body = data.containsKey("body") ? data.get("body") : data.get("message");
            }
            
            String type = data.get("type");
            String targetId = data.get("targetId");
            String priority = data.get("priority");
            
            Log.d(TAG, "Processing notification - Title: " + title + ", Body: " + body + ", Type: " + type);
            
            // Always show notification in system tray
            showNotification(title, body, type, targetId, priority);
            
            // Save to database
            saveNotificationToFirestore(title, body, type, targetId, data);
            
        } finally {
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM token: " + token);
        sendTokenToServer(token);
    }

    private void sendTokenToServer(String token) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            
            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("fcmToken", token);
            tokenData.put("lastUpdated", System.currentTimeMillis());
            tokenData.put("platform", "android");
            
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .update(tokenData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM token updated successfully"))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update FCM token", e);
                    // Try to create the document if it doesn't exist
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("fcmToken", token);
                    userData.put("lastUpdated", System.currentTimeMillis());
                    userData.put("platform", "android");
                    
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .set(userData, com.google.firebase.firestore.SetOptions.merge())
                        .addOnSuccessListener(aVoid2 -> Log.d(TAG, "FCM token created successfully"))
                        .addOnFailureListener(e2 -> Log.e(TAG, "Failed to create FCM token", e2));
                });
        }
    }

    private void showNotification(String title, String body, String type, String targetId, String priority) {
        try {
            Log.d(TAG, "Showing notification: " + title);
            
            Intent intent = getNotificationIntent(type, targetId);
            
            // Use unique request code to avoid conflicts
            int requestCode = (int) (System.currentTimeMillis() & 0x7fffffff);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                this, requestCode, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Force high priority and visibility
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(android.graphics.BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVibrate(new long[]{0, 500, 250, 500})
                .setLights(0xFF0000FF, 1000, 1000)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis())
                .setOnlyAlertOnce(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body));
            
            // Set app name as subtext
            builder.setSubText("NotesAura");

            // Add color for better visibility
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setColor(0xFF2196F3);
            }

            NotificationManager notificationManager = 
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            
            if (notificationManager != null) {
                // Force show notification
                android.app.Notification notification = builder.build();
                notification.flags |= android.app.Notification.FLAG_INSISTENT;
                notificationManager.notify(requestCode, notification);
                Log.d(TAG, "NOTIFICATION FORCED TO DISPLAY - ID: " + requestCode);
            } else {
                Log.e(TAG, "NotificationManager is null!");
            }
        } catch (Exception e) {
            Log.e(TAG, "CRITICAL ERROR showing notification", e);
        }
    }

    private Intent getNotificationIntent(String type, String targetId) {
        // ALWAYS open MainActivity with notification flag - ignore all type/targetId data
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("open_notifications", true);
        intent.putExtra("highlight_new", true);
        // Clear any existing data to prevent interference
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    private void saveNotificationOnly(RemoteMessage remoteMessage) {
        String title = "NotesAura";
        String body = "";
        
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle() != null ? 
                remoteMessage.getNotification().getTitle() : title;
            body = remoteMessage.getNotification().getBody() != null ? 
                remoteMessage.getNotification().getBody() : body;
        }
        
        Map<String, String> data = remoteMessage.getData();
        if (data.containsKey("title")) {
            title = data.get("title");
        }
        if (data.containsKey("body") || data.containsKey("message")) {
            body = data.containsKey("body") ? data.get("body") : data.get("message");
        }
        
        String type = data.get("type");
        String targetId = data.get("targetId");
        
        saveNotificationToFirestore(title, body, type, targetId, data);
    }

    private void saveNotificationToFirestore(String title, String body, String type, String targetId, Map<String, String> data) {
        try {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                
                Map<String, Object> notificationData = new HashMap<>();
                notificationData.put("userId", userId);
                notificationData.put("title", title);
                notificationData.put("message", body);
                notificationData.put("type", type != null ? type : "general");
                notificationData.put("targetId", targetId);
                notificationData.put("timestamp", System.currentTimeMillis());
                notificationData.put("isRead", false);
                notificationData.put("data", data);
                notificationData.put("platform", "android");
                
                FirebaseFirestore.getInstance()
                    .collection("app_notifications")
                    .add(notificationData)
                    .addOnSuccessListener(documentReference -> 
                        Log.d(TAG, "Notification saved to Firestore: " + documentReference.getId()))
                    .addOnFailureListener(e -> 
                        Log.e(TAG, "Failed to save notification to Firestore", e));
            } else {
                Log.w(TAG, "User not authenticated, cannot save notification");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving notification to Firestore", e);
        }
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            
            if (notificationManager != null) {
                // Maximum importance channel for guaranteed visibility
                NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_MAX
                );
                channel.setDescription("NotesAura notifications - Always show");
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{0, 500, 250, 500});
                channel.enableLights(true);
                channel.setLightColor(0xFF0000FF);
                channel.setShowBadge(true);
                channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
                channel.setBypassDnd(true);
                channel.setSound(android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION), null);
                
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "MAXIMUM importance notification channel created");
            }
        }
    }
}