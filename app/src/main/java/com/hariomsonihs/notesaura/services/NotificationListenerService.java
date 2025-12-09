package com.hariomsonihs.notesaura.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.hariomsonihs.notesaura.utils.NotificationHelper;

public class NotificationListenerService extends Service {
    private static final String TAG = "NotificationListener";
    private Handler handler;
    private Runnable checkNotifications;
    private FirebaseFirestore db;
    private String lastNotificationId = "";

    @Override
    public void onCreate() {
        super.onCreate();
        db = FirebaseFirestore.getInstance();
        handler = new Handler();
        
        checkNotifications = new Runnable() {
            @Override
            public void run() {
                checkForNewNotifications();
                handler.postDelayed(this, 30000); // Check every 30 seconds
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        handler.post(checkNotifications);
        return START_STICKY;
    }

    private void checkForNewNotifications() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        db.collection("app_notifications")
            .whereEqualTo("userId", currentUser.getUid())
            .whereEqualTo("isRead", false)
            .limit(5)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String notificationId = doc.getId();
                        
                        if (!notificationId.equals(lastNotificationId)) {
                            // New notification found
                            lastNotificationId = notificationId;
                            
                            String title = doc.getString("title");
                            String message = doc.getString("message");
                            String type = doc.getString("type");
                            
                            // Show local notification
                            NotificationHelper.showLocalNotification(
                                this,
                                title != null ? title : "New Notification",
                                message != null ? message : "You have a new notification",
                                type != null ? type : "general",
                                notificationId
                            );
                            
                            Log.d(TAG, "New notification: " + title);
                            break; // Only process the first new notification
                        }
                    }
                }
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error checking notifications", e));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null && checkNotifications != null) {
            handler.removeCallbacks(checkNotifications);
        }
        Log.d(TAG, "Service destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}