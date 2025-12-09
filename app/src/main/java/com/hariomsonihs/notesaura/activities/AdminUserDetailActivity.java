package com.hariomsonihs.notesaura.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.adapters.UserCourseAdapter;
import com.hariomsonihs.notesaura.models.UserCourse;

public class AdminUserDetailActivity extends AppCompatActivity {
    private TextView userIdText, joinDateText, totalProgressText;
    private EditText userNameEdit, userEmailEdit, userPhoneEdit;
    private Switch adminSwitch, premiumSwitch;
    private Button saveButton;
    private RecyclerView coursesRecyclerView;
    private LinearLayout coursesSection;
    
    private String userId;
    private FirebaseFirestore db;
    private UserCourseAdapter courseAdapter;
    private List<UserCourse> userCourses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_detail);
        
        db = FirebaseFirestore.getInstance();
        
        initializeViews();
        setupToolbar();
        loadUserData();
        setupClickListeners();
    }
    
    private void initializeViews() {
    userIdText = findViewById(R.id.user_id_text);
    joinDateText = findViewById(R.id.join_date_text);
    totalProgressText = findViewById(R.id.total_progress_text);
    userNameEdit = findViewById(R.id.user_name_edit);
    userEmailEdit = findViewById(R.id.user_email_edit);
    userPhoneEdit = findViewById(R.id.user_phone_edit);
        adminSwitch = findViewById(R.id.admin_switch);
        premiumSwitch = findViewById(R.id.premium_switch);
        saveButton = findViewById(R.id.save_button);
        coursesRecyclerView = findViewById(R.id.courses_recycler_view);
        coursesSection = findViewById(R.id.courses_section);
        
        setupCoursesRecyclerView();
    }
    
    private void setupCoursesRecyclerView() {
        coursesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userCourses = new ArrayList<>();
        courseAdapter = new UserCourseAdapter(userCourses, course -> {
            // Show course options dialog
            showCourseOptionsDialog(course);
        });
        coursesRecyclerView.setAdapter(courseAdapter);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("👤 User Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void loadUserData() {
        userId = getIntent().getStringExtra("user_id");
    String userName = getIntent().getStringExtra("user_name");
    String userEmail = getIntent().getStringExtra("user_email");
    String userPhone = getIntent().getStringExtra("user_phone");
        
        if (userId != null) {
            userIdText.setText("ID: " + userId);
            userNameEdit.setText(userName);
            userEmailEdit.setText(userEmail);
            userPhoneEdit.setText(userPhone);
            
            // Load full user data from Firebase
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String adminStatus = documentSnapshot.getString("admin");
                        Boolean premium = documentSnapshot.getBoolean("premium");
                        Date joinDate = documentSnapshot.getDate("joinDate");
                        Long totalProgress = documentSnapshot.getLong("totalProgress");
                        String phone = documentSnapshot.getString("phone");

                        adminSwitch.setChecked("yes".equals(adminStatus));
                        premiumSwitch.setChecked(premium != null && premium);
                        userPhoneEdit.setText(phone);

                        if (joinDate != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
                            joinDateText.setText("Joined: " + sdf.format(joinDate));
                        }

                        totalProgressText.setText("Total Progress: " + (totalProgress != null ? totalProgress : 0) + "%");

                        loadUserCourses();
                    }
                });
        }
    }
    
    private void loadUserCourses() {
        db.collection("users").document(userId)
            .collection("enrolledCourses")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                userCourses.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    UserCourse course = new UserCourse();
                    course.setCourseId(document.getId());
                    course.setCourseName(document.getString("courseName"));
                    course.setCategory(document.getString("category"));
                    course.setEnrollmentDate(document.getDate("enrollmentDate"));
                    course.setProgressPercentage(document.getLong("progressPercentage"));
                    course.setAmountPaid(document.getLong("amountPaid"));
                    userCourses.add(course);
                }
                courseAdapter.notifyDataSetChanged();
                coursesSection.setVisibility(userCourses.isEmpty() ? android.view.View.GONE : android.view.View.VISIBLE);
            });
    }
    
    private void setupClickListeners() {
        saveButton.setOnClickListener(v -> saveUserData());
    }
    
    private void saveUserData() {
        String name = userNameEdit.getText().toString().trim();
        String email = userEmailEdit.getText().toString().trim();
        boolean isAdmin = adminSwitch.isChecked();
        boolean isPremium = premiumSwitch.isChecked();
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("email", email);
        updates.put("admin", isAdmin ? "yes" : "no");
        updates.put("premium", isPremium);
        
        db.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "User updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to update user", Toast.LENGTH_SHORT).show();
            });
    }
    
    private void showCourseOptionsDialog(UserCourse course) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Course: " + course.getCourseName());
        builder.setMessage("Progress: " + course.getProgressPercentage() + "%\nAmount Paid: ₹" + course.getAmountPaid());
        
        builder.setPositiveButton("Remove Course", (dialog, which) -> {
            removeCourseFromUser(course);
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void removeCourseFromUser(UserCourse course) {
        androidx.appcompat.app.AlertDialog.Builder confirmBuilder = new androidx.appcompat.app.AlertDialog.Builder(this);
        confirmBuilder.setTitle("Confirm Removal");
        confirmBuilder.setMessage("Are you sure you want to remove \"" + course.getCourseName() + "\" from this user?");
        
        confirmBuilder.setPositiveButton("Yes, Remove", (dialog, which) -> {
            db.collection("users").document(userId)
                .collection("enrolledCourses")
                .document(course.getCourseId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Course removed successfully!", Toast.LENGTH_SHORT).show();
                    loadUserCourses(); // Refresh the list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to remove course", Toast.LENGTH_SHORT).show();
                });
        });
        
        confirmBuilder.setNegativeButton("Cancel", null);
        confirmBuilder.show();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}