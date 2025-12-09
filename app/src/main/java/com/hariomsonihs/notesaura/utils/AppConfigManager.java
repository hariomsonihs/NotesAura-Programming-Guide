package com.hariomsonihs.notesaura.utils;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Map;
import android.content.Context;

public class AppConfigManager {
    private static AppConfigManager instance;
    private FirebaseFirestore db;
    private Map<String, Object> config;
    private Map<String, Object> featureFlags;
    
    private AppConfigManager() {
        db = FirebaseFirestore.getInstance();
        loadConfig();
    }
    
    public static AppConfigManager getInstance() {
        if (instance == null) {
            instance = new AppConfigManager();
        }
        return instance;
    }
    
    public void loadConfig() {
        android.util.Log.d("AppConfigManager", "Loading config from Firebase...");
        db.collection("app_config").document("main")
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    config = documentSnapshot.getData();
                    featureFlags = (Map<String, Object>) config.get("featureFlags");
                    android.util.Log.d("AppConfigManager", "Config loaded: " + config);
                } else {
                    android.util.Log.d("AppConfigManager", "Config document does not exist");
                }
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("AppConfigManager", "Error loading config", e);
            });
    }
    
    public boolean isMaintenanceMode() {
        try {
            return config != null && Boolean.TRUE.equals(config.get("maintenanceMode"));
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isForceUpdateRequired() {
        return config != null && Boolean.TRUE.equals(config.get("forceUpdate"));
    }
    
    public String getLatestVersion() {
        return config != null ? (String) config.get("latestAppVersion") : "1.0.0";
    }
    
    public String getUpdateMessage() {
        return config != null ? (String) config.get("updateMessage") : "Please update to continue";
    }
    
    public String getPlayStoreUrl() {
        return config != null ? (String) config.get("playStoreUrl") : "";
    }
    
    public String getApkDownloadUrl() {
        return config != null ? (String) config.get("apkDownloadUrl") : "";
    }
    
    public boolean isFeatureEnabled(String featureName) {
        if (featureFlags == null) return true;
        return Boolean.TRUE.equals(featureFlags.get(featureName));
    }
    
    public Map<String, Object> getAnnouncement() {
        return config != null ? (Map<String, Object>) config.get("announcement") : null;
    }
    
    public void checkForAnnouncements(android.content.Context context) {
        Map<String, Object> announcement = getAnnouncement();
        if (announcement != null && Boolean.TRUE.equals(announcement.get("isActive"))) {
            String text = (String) announcement.get("text");
            String type = (String) announcement.get("type");
            
            if (text != null && !text.isEmpty()) {
                com.hariomsonihs.notesaura.activities.AnnouncementActivity.showAnnouncement(
                    context, "Important Announcement", text, type
                );
            }
        }
    }
    
    public boolean shouldShowMaintenanceScreen() {
        try {
            return isMaintenanceMode();
        } catch (Exception e) {
            return false;
        }
    }
    
    public void setConfigUpdateListener(ConfigUpdateListener listener) {
        // Listen for real-time config changes
        db.collection("app_config").document("main")
            .addSnapshotListener((documentSnapshot, e) -> {
                if (e != null) {
                    android.util.Log.w("AppConfigManager", "Listen failed.", e);
                    return;
                }
                
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    config = documentSnapshot.getData();
                    featureFlags = (Map<String, Object>) config.get("featureFlags");
                    
                    if (listener != null) {
                        listener.onConfigUpdated(config);
                    }
                }
            });
    }
    
    public void refreshConfig(Runnable callback) {
        db.collection("app_config").document("main")
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    config = documentSnapshot.getData();
                    featureFlags = (Map<String, Object>) config.get("featureFlags");
                }
                if (callback != null) {
                    callback.run();
                }
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("AppConfigManager", "Error refreshing config", e);
                if (callback != null) {
                    callback.run();
                }
            });
    }
    
    public interface ConfigUpdateListener {
        void onConfigUpdated(Map<String, Object> config);
    }
}