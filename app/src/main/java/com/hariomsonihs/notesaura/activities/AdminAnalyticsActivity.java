package com.hariomsonihs.notesaura.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.utils.AdminHelper;

public class AdminAnalyticsActivity extends AppCompatActivity {
    private TextView totalUsersText, totalCoursesText, totalPaymentsText, totalRevenueText;
    private AdminHelper adminHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_analytics);
        
        adminHelper = AdminHelper.getInstance();
        
        initializeViews();
        setupToolbar();
        loadAnalytics();
    }
    
    private void initializeViews() {
        totalUsersText = findViewById(R.id.total_users_text);
        totalCoursesText = findViewById(R.id.total_courses_text);
        totalPaymentsText = findViewById(R.id.total_payments_text);
        totalRevenueText = findViewById(R.id.total_revenue_text);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Analytics");
        }
    }
    
    private void loadAnalytics() {
        adminHelper.getTotalUsers(count -> {
            totalUsersText.setText(String.valueOf(count));
        });
        
        adminHelper.getTotalCourses(count -> {
            totalCoursesText.setText(String.valueOf(count));
        });
        
        adminHelper.getTotalPayments(count -> {
            totalPaymentsText.setText(String.valueOf(count));
        });
        
        adminHelper.getTotalRevenue(revenue -> {
            totalRevenueText.setText("₹" + String.format("%.2f", revenue));
        });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}