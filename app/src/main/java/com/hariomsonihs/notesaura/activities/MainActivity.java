package com.hariomsonihs.notesaura.activities;

import androidx.appcompat.app.AppCompatDelegate;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.fragments.HomeFragment;
import com.hariomsonihs.notesaura.fragments.CategoriesFragment;
import com.hariomsonihs.notesaura.fragments.CoursesFragment;
import com.hariomsonihs.notesaura.fragments.PracticeFragment;
import com.hariomsonihs.notesaura.fragments.ProfileFragment;
import com.hariomsonihs.notesaura.utils.FirebaseAuthHelper;
import com.hariomsonihs.notesaura.utils.SharedPrefManager;
import com.hariomsonihs.notesaura.utils.CourseDataManager;
import com.hariomsonihs.notesaura.utils.Constants;
import com.hariomsonihs.notesaura.utils.NotificationHelper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigation;
    private NavigationView navigationView;
    private FirebaseAuthHelper authHelper;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        
        getWindow().setDecorFitsSystemWindows(false);
        
        setContentView(R.layout.activity_main_simple);
        initializeViews();
        setupWindowInsets();
        initializeFirebase();
        setupNavigation();
        CourseDataManager.getInstance().initialize(this);
        
        // Notification intents now go directly to NotificationsActivity
        

        
        checkAdminStatusAndLoadUI();
        
        // Check if opened from notification
        handleNotificationIntent(getIntent());
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateNavigationHeader();
        updateNotificationBadge();
        
        // Check permissions on resume
        checkPermissionsOnResume();
        
        // Check for new notifications every time app resumes
        checkForNewNotifications();
    }
    
    private void checkPermissionsOnResume() {
        // Check notification permission periodically
        new android.os.Handler().postDelayed(() -> {
            if (!com.hariomsonihs.notesaura.utils.PermissionManager.isNotificationPermissionGranted(this)) {
                com.hariomsonihs.notesaura.utils.PermissionManager.checkNotificationPermission(this);
            }
        }, 2000); // Check after 2 seconds
    }
    
    private void checkForNewNotifications() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore.getInstance()
                .collection("app_notifications")
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("isRead", false)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w("MainActivity", "Listen failed.", e);
                        return;
                    }
                    
                    updateNotificationBadge();
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        Log.d("MainActivity", "Found " + queryDocumentSnapshots.size() + " unread notifications");
                    }
                });
        }
    }
    
    private void updateNotificationBadge() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore.getInstance()
                .collection("app_notifications")
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    View badge = findViewById(R.id.notification_badge);
                    if (badge != null) {
                        badge.setVisibility(queryDocumentSnapshots.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                });
        }
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        
        findViewById(R.id.menu_icon).setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        
        findViewById(R.id.search_icon).setOnClickListener(v -> {
            Intent searchIntent = new Intent(this, SearchActivity.class);
            startActivity(searchIntent);
        });
        
        findViewById(R.id.notification_icon).setOnClickListener(v -> {
            Intent notificationIntent = new Intent(this, NotificationsActivity.class);
            startActivity(notificationIntent);
        });
        
        // Long click to send test notification (for debugging)
        findViewById(R.id.notification_icon).setOnLongClickListener(v -> {
            showNotificationTestDialog();
            return true;
        });
    }
    
    private void setupWindowInsets() {
        View headerLayout = findViewById(R.id.header_layout);
        View bottomNavContainer = findViewById(R.id.bottom_nav_container);
        
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(headerLayout, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return androidx.core.view.WindowInsetsCompat.CONSUMED;
        });
        
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(bottomNavContainer, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            int bottomPadding = systemBars.bottom + v.getPaddingTop();
            v.setPadding(
                v.getPaddingStart(),
                v.getPaddingTop(),
                v.getPaddingEnd(),
                systemBars.bottom + 16
            );
            return androidx.core.view.WindowInsetsCompat.CONSUMED;
        });
    }

    private void initializeFirebase() {
        authHelper = FirebaseAuthHelper.getInstance();
        prefManager = SharedPrefManager.getInstance(this);
        FirebaseApp.initializeApp(this);
    }

    private void setupNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (itemId == R.id.nav_categories) {
                fragment = new CategoriesFragment();
            } else if (itemId == R.id.nav_courses) {
                fragment = new CoursesFragment();
            } else if (itemId == R.id.nav_ebooks) {
                Intent ebooksIntent = new Intent(this, EbooksActivity.class);
                startActivity(ebooksIntent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }
            
            return loadFragment(fragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null && !isFinishing() && !isDestroyed()) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.nav_home) {
            loadFragment(new HomeFragment());
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        } else if (itemId == R.id.nav_categories) {
            loadFragment(new CategoriesFragment());
            bottomNavigation.setSelectedItemId(R.id.nav_categories);

        } else if (itemId == R.id.nav_quizzes) {
            Intent quizIntent = new Intent(this, QuizCategoriesActivity.class);
            startActivity(quizIntent);

        } else if (itemId == R.id.nav_ebooks) {
            Intent ebooksIntent = new Intent(this, EbooksActivity.class);
            startActivity(ebooksIntent);
        } else if (itemId == R.id.nav_contact) {
            openEmailApp();
        } else if (itemId == R.id.nav_about) {
            Intent aboutIntent = new Intent(this, AboutActivity.class);
            startActivity(aboutIntent);
        } else if (itemId == R.id.nav_share) {
            shareApp();
        }
        
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNotificationIntent(intent);
    }
    
    private void handleNotificationIntent(Intent intent) {
        if (intent != null && intent.getBooleanExtra("open_notifications", false)) {
            Intent notificationIntent = new Intent(this, NotificationsActivity.class);
            notificationIntent.putExtra("highlight_new", intent.getBooleanExtra("highlight_new", false));
            startActivity(notificationIntent);
        }
    }
    
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (!(currentFragment instanceof HomeFragment)) {
                loadFragment(new HomeFragment());
                bottomNavigation.setSelectedItemId(R.id.nav_home);
            } else {
                super.onBackPressed();
            }
        }
    }

    // Notification handling removed - notifications now go directly to NotificationsActivity
    
    private void checkAdminStatusAndLoadUI() {
        // Check app config first
        checkAppConfig();
        
        // Add delay to ensure config is loaded
        new android.os.Handler().postDelayed(() -> {
            checkAppConfig();
        }, 1000);
        
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            com.hariomsonihs.notesaura.utils.AdminHelper.getInstance()
                .checkAdminStatus(currentUser.getUid())
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String adminStatus = documentSnapshot.getString("admin");
                        Log.d("MainActivity", "Admin status: " + adminStatus);
                        Log.d("MainActivity", "Document data: " + documentSnapshot.getData());
                        if ("yes".equals(adminStatus)) {
                            Intent adminIntent = new Intent(this, com.hariomsonihs.notesaura.activities.AdminPanelActivity.class);
                            startActivity(adminIntent);
                            finish();
                        } else {
                            loadNormalUserUI();
                        }
                    } else {
                        loadNormalUserUI();
                    }
                })
                .addOnFailureListener(e -> loadNormalUserUI());
        } else {
            loadNormalUserUI();
        }
    }
    
    private void checkAppConfig() {
        com.hariomsonihs.notesaura.utils.AppConfigManager configManager = 
            com.hariomsonihs.notesaura.utils.AppConfigManager.getInstance();
        
        Log.d("MainActivity", "Checking app config...");
        
        // Check for maintenance mode
        boolean maintenanceMode = configManager.shouldShowMaintenanceScreen();
        Log.d("MainActivity", "Maintenance mode: " + maintenanceMode);
        
        if (maintenanceMode) {
            Log.d("MainActivity", "Showing maintenance screen");
            showMaintenanceDialog();
            return;
        }
        
        // Check for app update
        if (configManager.isForceUpdateRequired()) {
            String latestVersion = configManager.getLatestVersion();
            com.hariomsonihs.notesaura.utils.AppUpdateManager updateManager = 
                com.hariomsonihs.notesaura.utils.AppUpdateManager.getInstance(this);
            
            if (updateManager.isUpdateRequired(latestVersion)) {
                Log.d("MainActivity", "Update required: " + latestVersion);
                showUpdateDialog();
                return;
            }
        }
        
        // Check for announcements
        configManager.checkForAnnouncements(this);
        
        // Set up real-time config listener
        configManager.setConfigUpdateListener(config -> {
            runOnUiThread(() -> {
                // Check maintenance mode
                if (Boolean.TRUE.equals(config.get("maintenanceMode"))) {
                    Log.d("MainActivity", "Real-time maintenance mode detected");
                    showMaintenanceDialog();
                }
                
                // Check for force update
                if (Boolean.TRUE.equals(config.get("forceUpdate"))) {
                    String latestVersion = (String) config.get("latestAppVersion");
                    if (latestVersion != null) {
                        com.hariomsonihs.notesaura.utils.AppUpdateManager updateManager = 
                            com.hariomsonihs.notesaura.utils.AppUpdateManager.getInstance(MainActivity.this);
                        
                        if (updateManager.isUpdateRequired(latestVersion)) {
                            Log.d("MainActivity", "Real-time update required: " + latestVersion);
                            showUpdateDialog();
                        }
                    }
                }
                
                // Check for new announcements
                Map<String, Object> announcement = (Map<String, Object>) config.get("announcement");
                if (announcement != null && Boolean.TRUE.equals(announcement.get("isActive"))) {
                    String text = (String) announcement.get("text");
                    String type = (String) announcement.get("type");
                    
                    if (text != null && !text.isEmpty()) {
                        com.hariomsonihs.notesaura.activities.AnnouncementActivity.showAnnouncement(
                            this, "Live Update", text, type
                        );
                    }
                }
            });
        });
    }
    
    private void showMaintenanceDialog() {
        try {
            Intent maintenanceIntent = new Intent(this, com.hariomsonihs.notesaura.activities.MaintenanceActivity.class);
            startActivity(maintenanceIntent);
            finish();
        } catch (Exception e) {
            // Fallback to simple dialog if activity fails
            new android.app.AlertDialog.Builder(this)
                .setTitle("🔧 Maintenance Mode")
                .setMessage("The app is currently under maintenance. Please try again later.")
                .setCancelable(false)
                .setPositiveButton("Exit", (dialog, which) -> finishAffinity())
                .show();
        }
    }
    
    private void showUpdateDialog() {
        try {
            Intent updateIntent = new Intent(this, com.hariomsonihs.notesaura.activities.UpdateActivity.class);
            startActivity(updateIntent);
            finish();
        } catch (Exception e) {
            // Fallback to simple dialog if activity fails
            new android.app.AlertDialog.Builder(this)
                .setTitle("📱 Update Required")
                .setMessage("Please update the app to continue using NotesAura.")
                .setCancelable(false)
                .setPositiveButton("Exit", (dialog, which) -> finishAffinity())
                .show();
        }
    }

    private void loadNormalUserUI() {
        updateNavigationHeader();
        loadFragment(new HomeFragment());
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        setupFCMToken();
        
        // Notifications now handled directly by NotificationsActivity
        
        // Start notification listener service
        Intent serviceIntent = new Intent(this, com.hariomsonihs.notesaura.services.NotificationListenerService.class);
        startService(serviceIntent);
    }
    
    private void setupFCMToken() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Get FCM token with retry mechanism
            getFCMTokenWithRetry(currentUser, 0);
            
            // Subscribe to topics
            subscribeToFCMTopics(currentUser);
        }
    }
    
    private void getFCMTokenWithRetry(FirebaseUser currentUser, int retryCount) {
        if (retryCount >= 3) {
            Log.e("FCM", "Failed to get FCM token after 3 retries");
            return;
        }
        
        com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.w("FCM", "Fetching FCM registration token failed, retry " + (retryCount + 1), task.getException());
                    // Retry after delay
                    new android.os.Handler().postDelayed(() -> 
                        getFCMTokenWithRetry(currentUser, retryCount + 1), 2000);
                    return;
                }
                
                String token = task.getResult();
                Log.d("FCM", "FCM Registration Token obtained: " + token.substring(0, Math.min(20, token.length())) + "...");
                
                // Save token to user document with additional metadata
                Map<String, Object> tokenData = new HashMap<>();
                tokenData.put("fcmToken", token);
                tokenData.put("lastUpdated", System.currentTimeMillis());
                tokenData.put("platform", "android");
                tokenData.put("appVersion", getAppVersion());
                
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUser.getUid())
                    .update(tokenData)
                    .addOnSuccessListener(aVoid -> Log.d("FCM", "Token saved successfully"))
                    .addOnFailureListener(e -> {
                        Log.e("FCM", "Error updating token, trying to create document", e);
                        // Try to create/merge the document if update fails
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(currentUser.getUid())
                            .set(tokenData, com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener(aVoid2 -> Log.d("FCM", "Token created/merged successfully"))
                            .addOnFailureListener(e2 -> Log.e("FCM", "Failed to create/merge token", e2));
                    });
            });
    }
    
    private void subscribeToFCMTopics(FirebaseUser currentUser) {
        // Subscribe to all users topic
        com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("all_users")
            .addOnCompleteListener(task -> {
                String msg = task.isSuccessful() ? "Subscribed to all_users topic" : "Subscribe to all_users failed";
                Log.d("FCM", msg);
            });
            
        // Subscribe to user-specific topic
        String userTopic = "user_" + currentUser.getUid();
        com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic(userTopic)
            .addOnCompleteListener(task -> {
                String msg = task.isSuccessful() ? "Subscribed to user topic" : "Subscribe to user topic failed";
                Log.d("FCM", msg);
            });
    }
    
    private String getAppVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    private void showNotificationTestDialog() {
        String[] options = {
            "🧪 Send Test Notifications",
            "🔍 Check Notification Status", 
            "⚙️ Open Notification Settings",
            "📊 Test FCM Token"
        };
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🔔 Notification Debug Menu")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        com.hariomsonihs.notesaura.utils.NotificationTester.sendTestNotifications(this);
                        android.widget.Toast.makeText(this, "Test notifications sent! Check notification center.", android.widget.Toast.LENGTH_LONG).show();
                        break;
                    case 1:
                        com.hariomsonihs.notesaura.utils.NotificationTester.testNotificationPermissions(this);
                        boolean systemEnabled = androidx.core.app.NotificationManagerCompat.from(this).areNotificationsEnabled();
                        String status = systemEnabled ? "✅ Notifications Enabled" : "❌ Notifications Disabled";
                        android.widget.Toast.makeText(this, status, android.widget.Toast.LENGTH_LONG).show();
                        break;
                    case 2:
                        com.hariomsonihs.notesaura.utils.PermissionManager.checkNotificationPermission(this);
                        break;
                    case 3:
                        testFCMToken();
                        break;
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void testFCMToken() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        String shortToken = token.substring(0, Math.min(20, token.length())) + "...";
                        android.widget.Toast.makeText(this, "✅ FCM Token: " + shortToken, android.widget.Toast.LENGTH_LONG).show();
                        Log.d("FCM_TEST", "Full FCM Token: " + token);
                    } else {
                        android.widget.Toast.makeText(this, "❌ Failed to get FCM token", android.widget.Toast.LENGTH_LONG).show();
                        Log.e("FCM_TEST", "Failed to get FCM token", task.getException());
                    }
                });
        } else {
            android.widget.Toast.makeText(this, "❌ User not authenticated", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateNavigationHeader() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            View headerView = navigationView.getHeaderView(0);
            TextView navUserName = headerView.findViewById(R.id.nav_user_name);
            TextView navUserEmail = headerView.findViewById(R.id.nav_user_email);
            
            String displayName = currentUser.getDisplayName();
            String email = currentUser.getEmail();
            
            if (displayName != null && !displayName.isEmpty()) {
                navUserName.setText(displayName);
            } else if (email != null) {
                navUserName.setText(email.split("@")[0]);
            } else {
                navUserName.setText("User");
            }
            
            if (email != null) {
                navUserEmail.setText(email);
            } else {
                navUserEmail.setText("No email");
            }
        }
    }

    private void showAdminPasswordDialog() {
        android.widget.EditText passwordInput = new android.widget.EditText(this);
        passwordInput.setHint("Enter admin password");
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        passwordInput.setPadding(50, 30, 50, 30);
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🔒 Admin Access")
            .setMessage("Enter password to access admin panel")
            .setView(passwordInput)
            .setPositiveButton("Access", (dialog, which) -> {
                String password = passwordInput.getText().toString().trim();
                if (password.equals("0819")) {
                    Intent adminIntent = new Intent(this, com.hariomsonihs.notesaura.activities.AdminPanelActivity.class);
                    startActivity(adminIntent);
                } else {
                    android.widget.Toast.makeText(this, "Incorrect password!", android.widget.Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void shareApp() {
        String shareText = "🚀 Check out NotesAura - Programming Guide! \n\n" +
                "📚 Learn programming with interactive courses\n" +
                "💻 Practice coding exercises offline\n" +
                "🏆 Track your progress & achievements\n" +
                "🎯 Master Java, C, Python & more!\n\n" +
                "Download now: https://play.google.com/store/apps/details?id=com.hariomsonihs.notesaura";
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "NotesAura - Programming Guide App");
        
        Intent chooser = Intent.createChooser(shareIntent, "Share NotesAura via...");
        startActivity(chooser);
    }
    
    private void openEmailApp() {
        String phoneNumber = "+917667110195";
        String message = "Hi, I have a query regarding NotesAura app.";
        
        try {
            String url = "https://wa.me/" + phoneNumber + "?text=" + java.net.URLEncoder.encode(message, "UTF-8");
            Intent whatsappIntent = new Intent(Intent.ACTION_VIEW);
            whatsappIntent.setData(android.net.Uri.parse(url));
            startActivity(whatsappIntent);
        } catch (Exception e) {
            android.widget.Toast.makeText(this, "WhatsApp not found", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}