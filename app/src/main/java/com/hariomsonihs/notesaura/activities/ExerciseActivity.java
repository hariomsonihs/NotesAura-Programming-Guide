package com.hariomsonihs.notesaura.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceError;
import android.webkit.WebSettings;
import androidx.appcompat.app.AlertDialog;
import com.hariomsonihs.notesaura.utils.NetworkUtil;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.utils.Constants;
import com.hariomsonihs.notesaura.utils.FirebaseDBHelper;
import com.hariomsonihs.notesaura.utils.CourseDataManager;
import android.graphics.Color;
import android.view.View;
import android.content.Intent;

public class ExerciseActivity extends AppCompatActivity {
    private WebView webView;
    private Button markAsReadButton;
    private String htmlPath;
    private String courseId;
    private String exerciseId;
    private FirebaseDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Handle edge-to-edge display and status bar
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }
        
        setContentView(R.layout.activity_exercise);
        
        htmlPath = getIntent().getStringExtra(Constants.KEY_HTML_PATH);
        courseId = getIntent().getStringExtra(Constants.KEY_COURSE_ID);
        exerciseId = getIntent().getStringExtra("exercise_id");
        
        dbHelper = FirebaseDBHelper.getInstance();
        
        initializeViews();
        setupToolbar();
        loadContent();
        setupMarkAsReadButton();
    }
    
    private void initializeViews() {
        webView = findViewById(R.id.web_view);
        markAsReadButton = findViewById(R.id.mark_as_read_button);
        
        // Ensure WebView is visible and properly configured
        if (webView != null) {
            webView.setVisibility(View.VISIBLE);
            webView.setBackgroundColor(Color.WHITE);
        }
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Exercise");
        }
    }
    
    private void loadContent() {
        if (htmlPath != null && isPdfUrl(htmlPath)) {
            // Open PDF viewer for PDF links
            openPdfViewer(htmlPath);
            return;
        }
        
        // Show loading for web content
        showWebLoading();
        
        // Configure WebView settings with aggressive caching
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        
        // Enable modern caching for offline support
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        
        // Set cache mode based on network availability
        if (NetworkUtil.isNetworkAvailable(this)) {
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        } else {
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        
        // Make WebView visible
        webView.setVisibility(View.VISIBLE);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                // Don't show loading again if already showing
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                hideWebLoading();
            }
            
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                hideWebLoading();
                
                // Show error message
                String errorHtml = "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>" +
                        "body { font-family: Arial; text-align: center; padding: 40px; background: #f5f5f5; }" +
                        ".error-container { background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
                        "h2 { color: #e74c3c; margin-bottom: 20px; }" +
                        "p { color: #666; line-height: 1.6; }" +
                        "</style></head><body>" +
                        "<div class='error-container'>" +
                        "<h2>⚠️ Loading Error</h2>" +
                        "<p>Failed to load the exercise content.</p>" +
                        "<p>Please check your internet connection and try again.</p>" +
                        "</div></body></html>";
                
                view.loadDataWithBaseURL(null, errorHtml, "text/html", "UTF-8", null);
                
                showNoInternetDialog();
            }
            
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // Ensure all URLs load in the same WebView
                return false;
            }
        });

        // Load content after a small delay to ensure loading animation is visible
        webView.postDelayed(() -> {
            try {
                if (htmlPath != null && !htmlPath.isEmpty()) {
                    if (htmlPath.startsWith("http://") || htmlPath.startsWith("https://")) {
                        // Load web URL
                        if (NetworkUtil.isNetworkAvailable(this)) {
                            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
                        } else {
                            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                        }
                        webView.loadUrl(htmlPath);
                    } else {
                        // Load from assets
                        String assetPath = "file:///android_asset/" + htmlPath;
                        webView.loadUrl(assetPath);
                    }
                } else {
                    // Load default content
                    loadDefaultContent();
                }
            } catch (Exception e) {
                e.printStackTrace();
                loadDefaultContent();
            }
        }, 300); // Reduced delay for better UX
    }
    
    private boolean isPdfUrl(String url) {
        if (url == null) return false;
        
        // Check for Google Drive PDF links
        if (url.contains("drive.google.com") && (url.contains("/file/d/") || url.contains("view"))) {
            return true;
        }
        
        // Check for direct PDF links
        if (url.toLowerCase().endsWith(".pdf")) {
            return true;
        }
        
        // Check for other PDF hosting services
        if (url.contains("dropbox.com") && url.contains(".pdf")) {
            return true;
        }
        
        return false;
    }
    
    private void openPdfViewer(String pdfUrl) {
        Intent intent = new Intent(this, PdfViewerActivity.class);
        intent.putExtra("pdf_url", pdfUrl);
        
        // Extract title from exercise or use default
        String title = "Exercise PDF";
        if (getSupportActionBar() != null && getSupportActionBar().getTitle() != null) {
            title = getSupportActionBar().getTitle().toString();
        }
        
        intent.putExtra("pdf_title", title);
        startActivity(intent);
        
        // Close this activity since PDF is opened
        finish();
    }

    private void showWebLoading() {
        // Show loading overlay for web content
        String loadingHtml = "<!DOCTYPE html><html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'><style>" +
                "body { margin: 0; padding: 20px; font-family: 'Segoe UI', Arial, sans-serif; text-align: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; min-height: 100vh; display: flex; align-items: center; justify-content: center; }" +
                ".loading-container { background: rgba(255,255,255,0.1); padding: 40px; border-radius: 20px; backdrop-filter: blur(10px); }" +
                ".spinner { border: 4px solid rgba(255,255,255,0.3); border-top: 4px solid #ffffff; border-radius: 50%; width: 50px; height: 50px; animation: spin 1s linear infinite; margin: 0 auto 20px; }" +
                "@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }" +
                "h2 { margin: 10px 0; font-size: 24px; }" +
                "p { margin: 10px 0; opacity: 0.8; font-size: 16px; }" +
                "</style></head><body>" +
                "<div class='loading-container'>" +
                "<div class='spinner'></div>" +
                "<h2>📚 Loading Exercise</h2>" +
                "<p>Please wait while we load the content...</p>" +
                "</div></body></html>";
        
        webView.loadDataWithBaseURL(null, loadingHtml, "text/html", "UTF-8", null);
    }
    
    private void hideWebLoading() {
        // Loading will be hidden when actual content loads
    }
    
    private void loadDefaultContent() {
        String defaultHtml = "<!DOCTYPE html><html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'><style>" +
                "body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 20px; background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%); min-height: 100vh; }" +
                ".container { max-width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 15px; box-shadow: 0 10px 30px rgba(0,0,0,0.1); }" +
                "h1 { color: #2c3e50; text-align: center; margin-bottom: 30px; }" +
                "p { color: #666; line-height: 1.8; font-size: 16px; }" +
                ".icon { font-size: 48px; text-align: center; margin-bottom: 20px; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<div class='icon'>📚</div>" +
                "<h1>Exercise Content</h1>" +
                "<p>Welcome to the exercise section. The content for this exercise is being loaded.</p>" +
                "<p>If you're seeing this message, it means the exercise content path was not provided or there was an issue loading the content.</p>" +
                "<p>Please go back and try selecting the exercise again.</p>" +
                "</div></body></html>";
        
        webView.loadDataWithBaseURL(null, defaultHtml, "text/html", "UTF-8", null);
    }
    
    private void showNoInternetDialog() {
        com.hariomsonihs.notesaura.utils.OfflineHelper.showNoInternetDialog(this, this::loadContent);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    private void setupMarkAsReadButton() {
        checkIfExerciseCompleted();
        markAsReadButton.setOnClickListener(v -> markExerciseAsCompleted());
    }
    
    private void checkIfExerciseCompleted() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || courseId == null) return;
        
        String exerciseKey = exerciseId != null ? exerciseId : htmlPath;
        
        dbHelper.getUserEnrolledCourses(currentUser.getUid())
                .document(courseId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        java.util.List<String> completedExercises = (java.util.List<String>) documentSnapshot.get("completedExercises");
                        if (completedExercises != null && completedExercises.contains(exerciseKey)) {
                            markAsReadButton.setText("✓ Completed");
                            markAsReadButton.setEnabled(false);
                            markAsReadButton.setBackgroundTintList(getColorStateList(R.color.accent_green));
                        }
                    }
                });
    }
    
    private void markExerciseAsCompleted() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || courseId == null) {
            Toast.makeText(this, "Unable to track progress", Toast.LENGTH_SHORT).show();
            return;
        }
        
        final String userId = currentUser.getUid();
        final String exerciseKey = exerciseId != null ? exerciseId : htmlPath;
        final String finalCourseId = courseId;

        dbHelper.getUserEnrolledCourses(userId)
                .document(finalCourseId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        java.util.List<String> completedExercises = (java.util.List<String>) documentSnapshot.get("completedExercises");
                        if (completedExercises == null) {
                            completedExercises = new java.util.ArrayList<>();
                        }

                        // Check if already completed
                        if (completedExercises.contains(exerciseKey)) {
                            Toast.makeText(this, "Exercise already completed!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Add to completed list
                        completedExercises.add(exerciseKey);

                        // Calculate progress based on actual exercises in the course
                        final int totalExercisesRaw = CourseDataManager.getInstance().getExercises(finalCourseId).size();
                        final int totalExercises = totalExercisesRaw == 0 ? 1 : totalExercisesRaw; // Prevent divide by zero
                        final int completedCount = completedExercises.size();
                        final int newProgress = (int) ((completedCount * 100.0) / totalExercises);

                        // Update Firebase
                        Map<String, Object> progressData = new HashMap<>();
                        progressData.put("lastAccessed", new Date());
                        progressData.put("completedExercises", completedExercises);
                        progressData.put("progressPercentage", newProgress);

                        dbHelper.getUserEnrolledCourses(userId)
                                .document(finalCourseId)
                                .update(progressData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Exercise completed! Progress: " + newProgress + "% (" + completedCount + "/" + totalExercises + ")", Toast.LENGTH_LONG).show();
                                    markAsReadButton.setText("✓ Completed");
                                    markAsReadButton.setEnabled(false);
                                    markAsReadButton.setBackgroundTintList(getColorStateList(R.color.accent_green));
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to update progress", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to track progress", Toast.LENGTH_SHORT).show();
                });
    }
    
    private int getTotalExercisesForCourse(String courseId) {
        // Return total exercises count for each course
        switch (courseId) {
            case "c_programming": return 8;
            case "java_programming": return 3;
            case "python_programming": return 3;
            case "html_css": return 3;
            case "javascript": return 2;
            case "react": return 2;
            case "android_dev": return 2;
            case "flutter": return 2;
            case "machine_learning": return 2;
            case "data_analysis": return 2;
            case "java_cheat":
            case "python_cheat":
            case "html_cheat":
            case "css_cheat":
            case "js_cheat": return 1;
            case "c1":
            case "c2":
            case "c3":
            case "c4": return 2;
            default: return 1;
        }
    }
    

    
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}