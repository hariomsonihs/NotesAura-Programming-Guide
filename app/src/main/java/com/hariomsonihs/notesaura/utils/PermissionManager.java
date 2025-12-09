package com.hariomsonihs.notesaura.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.appcompat.app.AlertDialog;
import java.util.ArrayList;
import java.util.List;

public class PermissionManager {
    private static final String TAG = "PermissionManager";
    public static final int PERMISSION_REQUEST_CODE = 1001;
    public static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1002;
    
    // Required permissions for the app
    private static final String[] REQUIRED_PERMISSIONS = {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    
    // Optional permissions
    private static final String[] OPTIONAL_PERMISSIONS = {
        Manifest.permission.READ_PHONE_STATE
    };

    public interface PermissionCallback {
        void onPermissionsGranted();
        void onPermissionsDenied(List<String> deniedPermissions);
    }

    public static void checkAndRequestPermissions(Activity activity, PermissionCallback callback) {
        List<String> permissionsToRequest = new ArrayList<>();
        
        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        
        // Check storage permissions
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        
        if (permissionsToRequest.isEmpty()) {
            Log.d(TAG, "All permissions already granted");
            callback.onPermissionsGranted();
        } else {
            Log.d(TAG, "Requesting permissions: " + permissionsToRequest);
            String[] permissionArray = permissionsToRequest.toArray(new String[0]);
            ActivityCompat.requestPermissions(activity, permissionArray, PERMISSION_REQUEST_CODE);
        }
    }
    

    
    public static void handlePermissionResult(Activity activity, int requestCode, String[] permissions, 
                                            int[] grantResults, PermissionCallback callback) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Always proceed to home screen regardless of permission result
            Log.d(TAG, "Permission result received, proceeding to home screen");
            callback.onPermissionsGranted();
        }
    }
    

    

    
    public static void checkNotificationPermission(Activity activity) {
        if (!NotificationManagerCompat.from(activity).areNotificationsEnabled()) {
            showNotificationPermissionDialog(activity);
        }
        
        // Check battery optimization
        checkBatteryOptimization(activity);
    }
    
    private static void checkBatteryOptimization(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.os.PowerManager powerManager = (android.os.PowerManager) activity.getSystemService(Context.POWER_SERVICE);
            if (powerManager != null && !powerManager.isIgnoringBatteryOptimizations(activity.getPackageName())) {
                new AlertDialog.Builder(activity)
                    .setTitle("🔋 Battery Optimization")
                    .setMessage("To receive notifications when app is closed, please disable battery optimization for NotesAura.")
                    .setPositiveButton("Disable", (dialog, which) -> {
                        try {
                            Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            intent.setData(android.net.Uri.parse("package:" + activity.getPackageName()));
                            activity.startActivity(intent);
                        } catch (Exception e) {
                            Log.e(TAG, "Error opening battery optimization settings", e);
                        }
                    })
                    .setNegativeButton("Later", null)
                    .show();
            }
        }
    }
    
    private static void showNotificationPermissionDialog(Activity activity) {
        new AlertDialog.Builder(activity)
            .setTitle("📢 Enable Notifications")
            .setMessage("Notifications are disabled. Enable them to receive:\n\n" +
                       "• New course updates\n" +
                       "• Important announcements\n" +
                       "• Rating replies from admin\n" +
                       "• Custom notifications\n\n" +
                       "You can enable notifications in Settings.")
            .setPositiveButton("Open Settings", (dialog, which) -> {
                openNotificationSettings(activity);
            })
            .setNegativeButton("Later", null)
            .show();
    }
    
    private static void openAppSettings(Activity activity) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
            intent.setData(uri);
            activity.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening app settings", e);
        }
    }
    
    private static void openNotificationSettings(Activity activity) {
        try {
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, activity.getPackageName());
            } else {
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
            }
            activity.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening notification settings", e);
            openAppSettings(activity);
        }
    }
    
    public static boolean areAllPermissionsGranted(Context context) {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        
        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        
        return true;
    }
    
    public static boolean isNotificationPermissionGranted(Context context) {
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }
}