package com.hariomsonihs.notesaura.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.utils.AdminHelper;
import com.hariomsonihs.notesaura.utils.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;

public class AdminPanelActivity extends AppCompatActivity {
    private TextView welcomeText;
    private View usersButton, coursesButton, paymentsButton, analyticsButton, managePracticesButton, managePracticeListsButton, manageQuizzesButton;
    private Button logoutButton;
    private AdminHelper adminHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Fix edge-to-edge issues
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.primary_start));
            getWindow().setNavigationBarColor(getResources().getColor(R.color.white));
        }
        
        setContentView(R.layout.activity_admin_panel);
        
        adminHelper = AdminHelper.getInstance();
        
        initializeViews();
        setupToolbar();
        setupClickListeners();
        loadAdminInfo();
    }
    
    private void initializeViews() {
    welcomeText = findViewById(R.id.admin_welcome);
    usersButton = findViewById(R.id.users_card);
    coursesButton = findViewById(R.id.courses_card);
    paymentsButton = findViewById(R.id.payments_card);
    analyticsButton = findViewById(R.id.analytics_card);
    logoutButton = findViewById(R.id.logout_button);
    managePracticesButton = findViewById(R.id.manage_practices_card);
    managePracticeListsButton = findViewById(R.id.manage_practice_lists_card);
    manageQuizzesButton = findViewById(R.id.manage_quizzes_card);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Panel");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        return true;
    }
    
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
    
    private void setupClickListeners() {
    // funSectionsButton and its click listener removed
        usersButton.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminUsersActivity.class));
        });
        
        coursesButton.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminCoursesActivity.class));
        });
        
        // Add new management options
        View addCourseButton = findViewById(R.id.add_course_card);
        View manageCategoriesButton = findViewById(R.id.manage_categories_card);
        
        if (addCourseButton != null) {
            addCourseButton.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminAddCourseActivity.class));
            });
        }
        
        if (manageCategoriesButton != null) {
            manageCategoriesButton.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminManageCategoriesActivity.class));
            });
        }
        
        // Featured courses management - card not in current layout
        
        View manageInterviewButton = findViewById(R.id.manage_interview_card);
        if (manageInterviewButton != null) {
            manageInterviewButton.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminManageInterviewActivity.class));
            });
        }
        
        paymentsButton.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminPaymentsActivity.class));
        });

        analyticsButton.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminAnalyticsActivity.class));
        });
        
        // Long press analytics for test notification
        analyticsButton.setOnLongClickListener(v -> {
            NotificationHelper.sendNotificationToAllUsers(
                "Test Notification", 
                "This is a test notification from admin panel", 
                "course", 
                "test_course", 
                null
            );
            
            android.widget.Toast.makeText(this, "Test notification sent!", android.widget.Toast.LENGTH_SHORT).show();
            return true;
        });

        if (managePracticesButton != null) {
            managePracticesButton.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminManagePracticeCategoriesActivity.class));
            });
        }
        if (managePracticeListsButton != null) {
            managePracticeListsButton.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminManagePracticeListsActivity.class));
            });
        }
        
        // Add Practice Exercises Management
        View managePracticeExercisesButton = findViewById(R.id.manage_practice_exercises_card);
        if (managePracticeExercisesButton != null) {
            managePracticeExercisesButton.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminManagePracticeActivity.class));
            });
        }
        
        if (manageQuizzesButton != null) {
            manageQuizzesButton.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminManageQuizzesActivity.class));
            });
        }

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
    
    private void loadAdminInfo() {
        welcomeText.setText("Welcome, Admin!");
    }
}