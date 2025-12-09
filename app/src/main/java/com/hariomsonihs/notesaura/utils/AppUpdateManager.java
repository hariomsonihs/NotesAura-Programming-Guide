package com.hariomsonihs.notesaura.utils;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import androidx.core.content.FileProvider;
import java.io.File;

public class AppUpdateManager {
    private static AppUpdateManager instance;
    private Context context;
    private DownloadManager downloadManager;
    private long downloadId;
    
    private AppUpdateManager(Context context) {
        this.context = context.getApplicationContext();
        this.downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    }
    
    public static AppUpdateManager getInstance(Context context) {
        if (instance == null) {
            instance = new AppUpdateManager(context);
        }
        return instance;
    }
    
    public String getCurrentVersion() {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "1.0.0";
        }
    }
    
    public boolean isUpdateRequired(String latestVersion) {
        String currentVersion = getCurrentVersion();
        return !currentVersion.equals(latestVersion);
    }
    
    public void downloadAndInstallUpdate(String apkUrl, UpdateCallback callback) {
        try {
            android.util.Log.d("AppUpdateManager", "Original URL: " + apkUrl);
            
            // Convert Google Drive share link to direct download link
            String downloadUrl = convertGoogleDriveUrl(apkUrl);
            android.util.Log.d("AppUpdateManager", "Converted URL: " + downloadUrl);
            
            // Create download request
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
            request.setTitle("NotesAura Update");
            request.setDescription("Downloading latest version...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "NotesAura_Update.apk");
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(true);
            request.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            
            // Start download
            downloadId = downloadManager.enqueue(request);
            android.util.Log.d("AppUpdateManager", "Download started with ID: " + downloadId);
            
            // Register receiver for download completion
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    android.util.Log.d("AppUpdateManager", "Download completed for ID: " + id);
                    if (id == downloadId) {
                        installApk();
                        context.unregisterReceiver(this);
                        if (callback != null) callback.onDownloadComplete();
                    }
                }
            };
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_NOT_EXPORTED);
            } else {
                context.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            }
            
            if (callback != null) {
                callback.onDownloadStarted();
                // Start progress tracking
                startProgressTracking(callback);
            }
            
        } catch (Exception e) {
            android.util.Log.e("AppUpdateManager", "Download error", e);
            if (callback != null) callback.onError(e.getMessage());
        }
    }
    
    private void startProgressTracking(UpdateCallback callback) {
        android.os.Handler handler = new android.os.Handler();
        Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                android.database.Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterById(downloadId));
                if (cursor != null && cursor.moveToFirst()) {
                    int bytesDownloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    int bytesTotal = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    
                    if (bytesTotal > 0) {
                        int progress = (int) ((bytesDownloaded * 100L) / bytesTotal);
                        if (callback != null) callback.onProgress(progress);
                    }
                    
                    cursor.close();
                    
                    // Continue tracking only if download is still in progress
                    if (status == DownloadManager.STATUS_RUNNING || status == DownloadManager.STATUS_PENDING) {
                        handler.postDelayed(this, 500);
                    }
                    // Don't continue tracking if download is complete or failed
                }
            }
        };
        handler.post(progressRunnable);
    }
    
    private void installApk() {
        try {
            File apkFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "NotesAura_Update.apk");
            
            if (apkFile.exists()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", apkFile);
                    intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } else {
                    intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                }
                
                context.startActivity(intent);
            }
        } catch (Exception e) {
            android.util.Log.e("AppUpdateManager", "Error installing APK", e);
        }
    }
    
    private String convertGoogleDriveUrl(String url) {
        // Convert Google Drive share URL to direct download URL
        if (url.contains("drive.google.com") && url.contains("/file/d/")) {
            // Extract file ID from share URL
            String fileId = url.substring(url.indexOf("/file/d/") + 8);
            if (fileId.contains("/")) {
                fileId = fileId.substring(0, fileId.indexOf("/"));
            }
            // Use direct download URL that bypasses confirmation page
            return "https://drive.usercontent.google.com/download?id=" + fileId + "&export=download&authuser=0&confirm=t";
        }
        
        // If it's already a direct download URL or other URL, return as is
        return url;
    }
    
    public interface UpdateCallback {
        void onDownloadStarted();
        void onProgress(int progress);
        void onDownloadComplete();
        void onError(String error);
    }
}