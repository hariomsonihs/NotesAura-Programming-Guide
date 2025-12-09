package com.hariomsonihs.notesaura.activities;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.utils.AppConfigManager;

public class MaintenanceActivity extends AppCompatActivity {
    
    private Button retryButton;
    private Button exitButton;
    private AppConfigManager configManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_maintenance);
            
            // Hide status bar for full screen experience
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_FULLSCREEN
            );
            
            initializeViews();
            setupClickListeners();
            configManager = AppConfigManager.getInstance();
            setupRealTimeListener();
        } catch (Exception e) {
            // If layout fails, just finish and exit
            finishAffinity();
        }
    }
    
    private void initializeViews() {
        retryButton = findViewById(R.id.retry_button);
        exitButton = findViewById(R.id.exit_button);
    }
    
    private void setupClickListeners() {
        retryButton.setOnClickListener(v -> checkMaintenanceStatus());
        exitButton.setOnClickListener(v -> finishAffinity());
    }
    
    private void checkMaintenanceStatus() {
        retryButton.setEnabled(false);
        retryButton.setText("🔄 Checking...");
        
        try {
            // Check if maintenance mode is still active
            configManager.refreshConfig(() -> {
                runOnUiThread(() -> {
                    try {
                        if (!configManager.shouldShowMaintenanceScreen()) {
                            // Maintenance is over, go back to main app
                            finish();
                        } else {
                            // Still in maintenance
                            retryButton.setEnabled(true);
                            retryButton.setText("🔄 Check Again");
                            android.widget.Toast.makeText(this, 
                                "Still under maintenance. Please try again later.", 
                                android.widget.Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        retryButton.setEnabled(true);
                        retryButton.setText("🔄 Check Again");
                    }
                });
            });
        } catch (Exception e) {
            retryButton.setEnabled(true);
            retryButton.setText("🔄 Check Again");
        }
    }
    
    private void setupRealTimeListener() {
        // Listen for real-time config changes
        configManager.setConfigUpdateListener(config -> {
            runOnUiThread(() -> {
                try {
                    // If maintenance mode is turned off, go back to main app
                    if (!Boolean.TRUE.equals(config.get("maintenanceMode"))) {
                        android.util.Log.d("MaintenanceActivity", "Maintenance mode turned off, returning to app");
                        finish();
                    }
                } catch (Exception e) {
                    android.util.Log.e("MaintenanceActivity", "Error checking maintenance status", e);
                }
            });
        });
    }
    
    @Override
    public void onBackPressed() {
        // Prevent going back during maintenance
        finishAffinity();
    }
}