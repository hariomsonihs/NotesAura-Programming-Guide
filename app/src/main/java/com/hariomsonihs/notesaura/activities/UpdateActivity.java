package com.hariomsonihs.notesaura.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.utils.AppUpdateManager;
import com.hariomsonihs.notesaura.utils.AppConfigManager;

public class UpdateActivity extends AppCompatActivity {
    
    private TextView updateMessage;
    private TextView currentVersion;
    private TextView latestVersion;
    private TextView statusText;
    private ProgressBar downloadProgress;
    private Button updateButton;
    private Button laterButton;
    
    private AppUpdateManager updateManager;
    private AppConfigManager configManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        
        // Hide status bar
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_FULLSCREEN
        );
        
        initializeViews();
        setupManagers();
        loadUpdateInfo();
        setupClickListeners();
        setupRealTimeListener();
        setupInstallReceiver();
    }
    
    private android.content.BroadcastReceiver installReceiver;
    
    private void setupInstallReceiver() {
        installReceiver = new android.content.BroadcastReceiver() {
            @Override
            public void onReceive(android.content.Context context, android.content.Intent intent) {
                if (android.content.Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
                    String packageName = intent.getData().getSchemeSpecificPart();
                    if (getPackageName().equals(packageName)) {
                        android.util.Log.d("UpdateActivity", "App updated, closing update screen");
                        finish();
                    }
                }
            }
        };
        
        android.content.IntentFilter filter = new android.content.IntentFilter();
        filter.addAction(android.content.Intent.ACTION_PACKAGE_REPLACED);
        filter.addDataScheme("package");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(installReceiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(installReceiver, filter);
        }
    }
    
    private void installDownloadedApk() {
        try {
            java.io.File apkFile = new java.io.File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), "NotesAura_Update.apk");
            
            if (apkFile.exists()) {
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    android.net.Uri apkUri = androidx.core.content.FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", apkFile);
                    intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } else {
                    intent.setDataAndType(android.net.Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                }
                
                startActivity(intent);
                statusText.setText("Installing update...");
                updateButton.setEnabled(false);
            } else {
                android.widget.Toast.makeText(this, "APK file not found. Please download again.", android.widget.Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            android.util.Log.e("UpdateActivity", "Error installing APK", e);
            android.widget.Toast.makeText(this, "Installation failed: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    
    private void initializeViews() {
        updateMessage = findViewById(R.id.update_message);
        currentVersion = findViewById(R.id.current_version);
        latestVersion = findViewById(R.id.latest_version);
        statusText = findViewById(R.id.status_text);
        downloadProgress = findViewById(R.id.download_progress);
        updateButton = findViewById(R.id.update_button);
        laterButton = findViewById(R.id.later_button);
    }
    
    private void setupManagers() {
        updateManager = AppUpdateManager.getInstance(this);
        configManager = AppConfigManager.getInstance();
    }
    
    private void loadUpdateInfo() {
        // Set current version
        currentVersion.setText(updateManager.getCurrentVersion());
        
        // Set latest version from config
        String latest = configManager.getLatestVersion();
        latestVersion.setText(latest);
        
        // Set update message
        String message = configManager.getUpdateMessage();
        if (message != null && !message.isEmpty()) {
            updateMessage.setText(message);
        }
    }
    
    private void setupClickListeners() {
        updateButton.setOnClickListener(v -> startUpdate());
        laterButton.setOnClickListener(v -> {
            // Check if force update is required
            if (configManager.isForceUpdateRequired()) {
                android.widget.Toast.makeText(this, "Update is required to continue", android.widget.Toast.LENGTH_SHORT).show();
            } else {
                finish();
            }
        });
    }
    
    private void startUpdate() {
        String apkUrl = configManager.getApkDownloadUrl();
        android.util.Log.d("UpdateActivity", "APK URL from config: " + apkUrl);
        
        if (apkUrl == null || apkUrl.isEmpty()) {
            android.util.Log.e("UpdateActivity", "APK URL is null or empty");
            // Fallback to Play Store if APK URL not available
            String playStoreUrl = configManager.getPlayStoreUrl();
            if (playStoreUrl != null && !playStoreUrl.isEmpty()) {
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(playStoreUrl));
                startActivity(intent);
            } else {
                android.widget.Toast.makeText(this, "Please set APK download URL in Firebase config", android.widget.Toast.LENGTH_LONG).show();
            }
            return;
        }
        
        updateManager.downloadAndInstallUpdate(apkUrl, new AppUpdateManager.UpdateCallback() {
            @Override
            public void onDownloadStarted() {
                runOnUiThread(() -> {
                    updateButton.setEnabled(false);
                    updateButton.setText("Downloading...");
                    downloadProgress.setVisibility(View.VISIBLE);
                    statusText.setVisibility(View.VISIBLE);
                    statusText.setText("Download started...");
                });
            }
            
            @Override
            public void onProgress(int progress) {
                runOnUiThread(() -> {
                    downloadProgress.setProgress(progress);
                    statusText.setText("Downloading... " + progress + "%");
                });
            }
            
            @Override
            public void onDownloadComplete() {
                runOnUiThread(() -> {
                    downloadProgress.setProgress(100);
                    downloadProgress.setVisibility(View.GONE);
                    statusText.setText("Download complete! Tap to install.");
                    updateButton.setEnabled(true);
                    updateButton.setText("📱 Install Update");
                    updateButton.setOnClickListener(v -> installDownloadedApk());
                });
            }
            
            @Override
            public void onError(String error) {
                android.util.Log.e("UpdateActivity", "Download error: " + error);
                runOnUiThread(() -> {
                    updateButton.setEnabled(true);
                    updateButton.setText("📥 Download Update");
                    downloadProgress.setVisibility(View.GONE);
                    statusText.setVisibility(View.VISIBLE);
                    statusText.setText("Error: " + error);
                    android.widget.Toast.makeText(UpdateActivity.this, "Update failed: " + error, android.widget.Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private void setupRealTimeListener() {
        // Listen for real-time config changes
        configManager.setConfigUpdateListener(config -> {
            runOnUiThread(() -> {
                try {
                    // If force update is turned off, go back to main app
                    if (!Boolean.TRUE.equals(config.get("forceUpdate"))) {
                        android.util.Log.d("UpdateActivity", "Force update turned off, returning to app");
                        finish();
                        return;
                    }
                    
                    // Check if user already has latest version
                    String latestVersion = (String) config.get("latestAppVersion");
                    if (latestVersion != null && !updateManager.isUpdateRequired(latestVersion)) {
                        android.util.Log.d("UpdateActivity", "User already has latest version, returning to app");
                        finish();
                    }
                } catch (Exception e) {
                    android.util.Log.e("UpdateActivity", "Error checking update status", e);
                }
            });
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (installReceiver != null) {
            try {
                unregisterReceiver(installReceiver);
            } catch (Exception e) {
                // Receiver might not be registered
            }
        }
    }
    
    @Override
    public void onBackPressed() {
        // Prevent going back if force update is required
        if (configManager.isForceUpdateRequired()) {
            android.widget.Toast.makeText(this, "Update is required to continue", android.widget.Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }
}