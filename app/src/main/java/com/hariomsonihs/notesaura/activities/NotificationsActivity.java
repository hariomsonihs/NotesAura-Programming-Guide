package com.hariomsonihs.notesaura.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.view.View;
import android.os.Handler;
import android.os.Looper;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.adapters.NotificationAdapter;
import com.hariomsonihs.notesaura.models.AppNotification;
import com.hariomsonihs.notesaura.utils.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NotificationsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<AppNotification> notifications;
    private TextView emptyText;
    private SwipeRefreshLayout swipeRefresh;
    private boolean notificationsEnabled = true;
    private MenuItem toggleMenuItem;
    private boolean shouldHighlightNew = false;
    private String latestNotificationId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            getWindow().setDecorFitsSystemWindows(false);
            
            setContentView(R.layout.activity_notifications);
            
            initViews();
            setupWindowInsets();
            setupRecyclerView();
            loadNotificationSettings();
            checkForHighlightIntent();
            loadNotifications();
        } catch (Exception e) {
            android.util.Log.e("NotificationsActivity", "Error in onCreate", e);
            finish();
        }
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Notifications");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // Set toolbar elevation for better visual separation
        toolbar.setElevation(4f);
        
        recyclerView = findViewById(R.id.recycler_notifications);
        emptyText = findViewById(R.id.empty_text);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        
        swipeRefresh.setOnRefreshListener(this::loadNotifications);
    }
    
    private void setupWindowInsets() {
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbar), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                v.getPaddingLeft(),
                systemBars.top,
                v.getPaddingRight(),
                v.getPaddingBottom()
            );
            return insets;
        });
    }

    private void checkForHighlightIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("highlight_new", false)) {
            shouldHighlightNew = true;
            getLatestNotificationId();
        }
    }
    
    private void getLatestNotificationId() {
        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore.getInstance()
                .collection("app_notifications")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        latestNotificationId = queryDocumentSnapshots.getDocuments().get(0).getId();
                    }
                });
        }
    }

    private void setupRecyclerView() {
        notifications = new ArrayList<>();
        adapter = new NotificationAdapter(notifications, new NotificationAdapter.OnNotificationClickListener() {
            @Override
            public void onNotificationClick(AppNotification notification) {
                handleNotificationClick(notification);
            }
            
            @Override
            public void onNotificationDelete(AppNotification notification, int position) {
                deleteNotification(notification, position);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadNotifications() {
        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            android.util.Log.e("NotificationsActivity", "No current user found");
            showEmptyState();
            return;
        }
        
        android.util.Log.d("NotificationsActivity", "Loading notifications for user: " + currentUser.getUid());
        
        FirebaseFirestore.getInstance()
            .collection("app_notifications")
            .whereEqualTo("userId", currentUser.getUid())
            .limit(50)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                android.util.Log.d("NotificationsActivity", "Query successful, found " + queryDocumentSnapshots.size() + " documents");
                
                notifications.clear();
                queryDocumentSnapshots.forEach(doc -> {
                    android.util.Log.d("NotificationsActivity", "Processing document: " + doc.getId());
                    try {
                        AppNotification notification = new AppNotification();
                        notification.setId(doc.getId());
                        notification.setUserId(doc.getString("userId"));
                        notification.setTitle(doc.getString("title"));
                        notification.setMessage(doc.getString("message"));
                        notification.setType(doc.getString("type"));
                        notification.setTargetId(doc.getString("targetId"));
                        notification.setImageUrl(doc.getString("imageUrl"));
                        
                        // Handle timestamp - could be Timestamp or Long
                        Object timestampObj = doc.get("timestamp");
                        if (timestampObj instanceof com.google.firebase.Timestamp) {
                            notification.setTimestamp(((com.google.firebase.Timestamp) timestampObj).toDate().getTime());
                        } else if (timestampObj instanceof Long) {
                            notification.setTimestamp((Long) timestampObj);
                        } else {
                            notification.setTimestamp(System.currentTimeMillis());
                        }
                        
                        // Handle isRead field
                        Boolean isRead = doc.getBoolean("isRead");
                        notification.setRead(isRead != null ? isRead : false);
                        
                        // Handle data field for rating replies
                        Object dataObj = doc.get("data");
                        if (dataObj instanceof java.util.Map) {
                            java.util.Map<String, Object> dataMap = (java.util.Map<String, Object>) dataObj;
                            if (dataMap.containsKey("ratingId")) {
                                notification.setTargetId((String) dataMap.get("ratingId"));
                            }
                        }
                        
                        notifications.add(notification);
                    } catch (Exception e) {
                        android.util.Log.e("NotificationsActivity", "Error processing notification: " + doc.getId(), e);
                    }
                });
                
                android.util.Log.d("NotificationsActivity", "Total notifications loaded: " + notifications.size());
                
                // Sort notifications by timestamp (newest first)
                notifications.sort((n1, n2) -> Long.compare(n2.getTimestamp(), n1.getTimestamp()));
                
                if (notifications.isEmpty()) {
                    showEmptyState();
                } else {
                    showNotificationsList();
                }
                
                adapter.notifyDataSetChanged();
                
                if (shouldHighlightNew && latestNotificationId != null) {
                    highlightNewNotification();
                    shouldHighlightNew = false;
                }
                
                swipeRefresh.setRefreshing(false);
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("NotificationsActivity", "Failed to load notifications", e);
                showEmptyState();
                swipeRefresh.setRefreshing(false);
            });
    }

    private void handleNotificationClick(AppNotification notification) {
        // Mark as read
        FirebaseFirestore.getInstance()
            .collection("app_notifications")
            .document(notification.getId())
            .update("isRead", true);
        
        // Navigate based on type
        Intent intent = null;
        switch (notification.getType()) {
            case "course":
                intent = new Intent(this, CourseDetailActivity.class);
                intent.putExtra(Constants.KEY_COURSE_ID, notification.getTargetId());
                break;
            case "ebook":
                intent = new Intent(this, EbooksListActivity.class);
                intent.putExtra("subcategory_id", notification.getTargetId());
                break;
            case "practice":
                intent = new Intent(this, PracticeActivity.class);
                break;
            case "interview":
                intent = new Intent(this, InterviewActivity.class);
                break;
            case "rating_reply":
                showRatingReplyDialog(notification);
                return;
            case "custom":
            case "admin":
            case "announcement":
            case "general":
                showCustomNotificationDialog(notification);
                return;
        }
        
        if (intent != null) {
            startActivity(intent);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.notifications_menu, menu);
            toggleMenuItem = menu.findItem(R.id.action_toggle_notifications);
            updateToggleMenuItem();
        } catch (Exception e) {
            android.util.Log.e("NotificationsActivity", "Error creating menu", e);
        }
        return true;
    }
    
    private void showEmptyState() {
        if (emptyText != null) {
            emptyText.setVisibility(View.VISIBLE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
    }
    
    private void showNotificationsList() {
        if (emptyText != null) {
            emptyText.setVisibility(View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_toggle_notifications) {
            toggleNotifications();
            return true;
        } else if (id == R.id.action_clear_all) {
            clearAllNotifications();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void clearAllNotifications() {
        new AlertDialog.Builder(this)
            .setTitle("Clear All Notifications")
            .setMessage("Delete all notifications? This cannot be undone.")
            .setPositiveButton("Clear All", (dialog, which) -> {
                com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    FirebaseFirestore.getInstance()
                        .collection("app_notifications")
                        .whereEqualTo("userId", currentUser.getUid())
                        .get()
                        .addOnSuccessListener(docs -> {
                            for (com.google.firebase.firestore.QueryDocumentSnapshot doc : docs) {
                                doc.getReference().delete();
                            }
                            loadNotifications();
                        });
                }
                loadNotifications();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void deleteNotification(AppNotification notification, int position) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Notification")
            .setMessage("Delete this notification?")
            .setPositiveButton("Delete", (dialog, which) -> {
                FirebaseFirestore.getInstance()
                    .collection("app_notifications")
                    .document(notification.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        notifications.remove(position);
                        adapter.notifyItemRemoved(position);
                        
                        if (notifications.isEmpty()) {
                            emptyText.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
                        
                        android.widget.Toast.makeText(this, "Notification deleted", android.widget.Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        android.widget.Toast.makeText(this, "Failed to delete notification", android.widget.Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void loadNotificationSettings() {
        android.content.SharedPreferences prefs = getSharedPreferences("NotificationSettings", MODE_PRIVATE);
        notificationsEnabled = prefs.getBoolean("notifications_enabled", true);
    }
    
    private void saveNotificationSettings() {
        android.content.SharedPreferences prefs = getSharedPreferences("NotificationSettings", MODE_PRIVATE);
        prefs.edit().putBoolean("notifications_enabled", notificationsEnabled).apply();
    }
    
    private void toggleNotifications() {
        notificationsEnabled = !notificationsEnabled;
        saveNotificationSettings();
        updateToggleMenuItem();
        
        if (notificationsEnabled) {
            android.widget.Toast.makeText(this, "Notifications enabled", android.widget.Toast.LENGTH_SHORT).show();
        } else {
            android.widget.Toast.makeText(this, "Notifications disabled", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateToggleMenuItem() {
        if (toggleMenuItem != null) {
            if (notificationsEnabled) {
                toggleMenuItem.setTitle("Turn Off Notifications");
                toggleMenuItem.setIcon(R.drawable.ic_notification);
            } else {
                toggleMenuItem.setTitle("Turn On Notifications");
                toggleMenuItem.setIcon(android.R.drawable.ic_lock_silent_mode_off);
            }
        }
    }
    
    private void highlightNewNotification() {
        int position = -1;
        for (int i = 0; i < notifications.size(); i++) {
            if (notifications.get(i).getId().equals(latestNotificationId)) {
                position = i;
                break;
            }
        }
        
        if (position != -1) {
            recyclerView.scrollToPosition(position);
            
            final int finalPosition = position;
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                startBlinkingAnimation(finalPosition);
            }, 300);
        }
    }
    
    private void startBlinkingAnimation(int position) {
        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
        if (viewHolder != null) {
            View itemView = viewHolder.itemView;
            
            android.animation.ObjectAnimator blinkAnimator = android.animation.ObjectAnimator.ofFloat(itemView, "alpha", 1f, 0.3f, 1f);
            blinkAnimator.setDuration(500);
            blinkAnimator.setRepeatCount(5);
            blinkAnimator.setRepeatMode(android.animation.ValueAnimator.RESTART);
            
            android.animation.ObjectAnimator colorAnimator = android.animation.ObjectAnimator.ofArgb(
                itemView, "backgroundColor", 
                android.graphics.Color.TRANSPARENT, 
                getResources().getColor(R.color.notification_highlight, null),
                android.graphics.Color.TRANSPARENT
            );
            colorAnimator.setDuration(500);
            colorAnimator.setRepeatCount(5);
            colorAnimator.setRepeatMode(android.animation.ValueAnimator.RESTART);
            
            android.animation.AnimatorSet animatorSet = new android.animation.AnimatorSet();
            animatorSet.playTogether(blinkAnimator, colorAnimator);
            animatorSet.start();
        }
    }
    
    private void showRatingReplyDialog(AppNotification notification) {
        try {
            if (notification.getTargetId() != null) {
                String ratingId = notification.getTargetId();
                FirebaseFirestore.getInstance()
                    .collection("course_ratings")
                    .document(ratingId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String userComment = doc.getString("comment");
                            String courseId = doc.getString("courseId");
                            int rating = doc.getLong("rating") != null ? doc.getLong("rating").intValue() : 0;
                            
                            StringBuilder dialogContent = new StringBuilder();
                            
                            // Show original rating context
                            dialogContent.append("Your Original Rating:\n");
                            dialogContent.append("Course: ").append(courseId != null ? courseId : "Unknown").append("\n");
                            dialogContent.append("Rating: ").append("★".repeat(rating)).append("☆".repeat(5-rating)).append("\n");
                            if (userComment != null && !userComment.isEmpty()) {
                                dialogContent.append("Comment: \"").append(userComment).append("\"\n\n");
                            } else {
                                dialogContent.append("No comment\n\n");
                            }
                            
                            dialogContent.append("Admin Replies:\n");
                            dialogContent.append("─────────────────\n");
                            
                            // Check for multiple replies first
                            List<Map<String, Object>> adminReplies = (List<Map<String, Object>>) doc.get("adminReplies");
                            if (adminReplies != null && !adminReplies.isEmpty()) {
                                for (int i = 0; i < adminReplies.size(); i++) {
                                    Map<String, Object> reply = adminReplies.get(i);
                                    String message = (String) reply.get("message");
                                    Long timestamp = (Long) reply.get("timestamp");
                                    String date = timestamp != null ? new java.text.SimpleDateFormat("MMM dd, yyyy").format(new java.util.Date(timestamp)) : "";
                                    
                                    dialogContent.append("Reply ").append(i + 1).append(" (").append(date).append("):\n")
                                             .append(message).append("\n\n");
                                }
                            } else {
                                // Fallback to single reply
                                String adminReply = doc.getString("adminReply");
                                if (adminReply != null) {
                                    dialogContent.append(adminReply);
                                } else {
                                    dialogContent.append("No reply found");
                                }
                            }
                            
                            displayReplyDialog(dialogContent.toString().trim());
                        } else {
                            displayReplyDialog("Reply not found");
                        }
                    })
                    .addOnFailureListener(e -> {
                        displayReplyDialog("Error loading reply");
                    });
            }
        } catch (Exception e) {
            displayReplyDialog("Error parsing reply data");
        }
    }
    
    private void displayReplyDialog(String replyText) {
        new AlertDialog.Builder(this)
            .setTitle("Admin Reply")
            .setMessage(replyText)
            .setPositiveButton("OK", null)
            .show();
    }
    
    private void showCustomNotificationDialog(AppNotification notification) {
        StringBuilder dialogContent = new StringBuilder();
        
        // Show notification details
        dialogContent.append("📢 Admin Notification\n");
        dialogContent.append("─────────────────\n\n");
        
        // Title
        if (notification.getTitle() != null && !notification.getTitle().isEmpty()) {
            dialogContent.append("📌 ").append(notification.getTitle()).append("\n\n");
        }
        
        // Full message
        if (notification.getMessage() != null && !notification.getMessage().isEmpty()) {
            dialogContent.append(notification.getMessage()).append("\n\n");
        }
        
        // Timestamp
        String date = new java.text.SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a")
            .format(new java.util.Date(notification.getTimestamp()));
        dialogContent.append("📅 Sent: ").append(date);
        
        // Show in dialog
        new AlertDialog.Builder(this)
            .setTitle("📢 Notification Details")
            .setMessage(dialogContent.toString())
            .setPositiveButton("OK", null)
            .setNeutralButton("Mark as Read", (dialog, which) -> {
                // Mark as read
                FirebaseFirestore.getInstance()
                    .collection("app_notifications")
                    .document(notification.getId())
                    .update("isRead", true)
                    .addOnSuccessListener(aVoid -> {
                        notification.setRead(true);
                        adapter.notifyDataSetChanged();
                        android.widget.Toast.makeText(this, "Marked as read", android.widget.Toast.LENGTH_SHORT).show();
                    });
            })
            .show();
    }
}