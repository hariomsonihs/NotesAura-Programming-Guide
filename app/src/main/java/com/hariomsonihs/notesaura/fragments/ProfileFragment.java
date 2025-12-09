package com.hariomsonihs.notesaura.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.ArrayList;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.activities.AuthActivity;
import com.hariomsonihs.notesaura.activities.CourseDetailActivity;
import com.hariomsonihs.notesaura.adapters.EnrolledCourseAdapter;
import com.hariomsonihs.notesaura.models.EnrolledCourse;
import com.hariomsonihs.notesaura.utils.Constants;
import com.hariomsonihs.notesaura.utils.SharedPrefManager;
import com.hariomsonihs.notesaura.utils.FirebaseDBHelper;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class ProfileFragment extends Fragment implements EnrolledCourseAdapter.OnEnrolledCourseClickListener {
    private CircleImageView profileImage;
    private TextView userName, userEmail, userPhone, joinDate, coursesCount, userUid, premiumStatus;
    private Button editProfileButton, logoutButton, forgotPasswordButton, myPaymentsButton;
    private android.widget.ImageView copyUidButton;
    private RecyclerView myCoursesRecyclerView, bookmarkedCoursesRecyclerView;
    private EnrolledCourseAdapter myCoursesAdapter;
    private com.hariomsonihs.notesaura.adapters.CourseAdapter bookmarkedCoursesAdapter;
    private List<EnrolledCourse> enrolledCourses;
    private List<com.hariomsonihs.notesaura.models.Course> bookmarkedCourses;
    private View favouritesSection;
    private SharedPrefManager prefManager;
    private FirebaseDBHelper dbHelper;
    private SwipeRefreshLayout swipeRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initializeViews(view);
        setupSwipeRefresh();
        loadUserData();
        setupClickListeners();
        loadEnrolledCoursesCount();
        loadEnrolledCourses();
        loadBookmarkedCourses();

        return view;
    }

    private void initializeViews(View view) {
        profileImage = view.findViewById(R.id.profile_image);
    userName = view.findViewById(R.id.user_name);
    userEmail = view.findViewById(R.id.user_email);
    userPhone = view.findViewById(R.id.user_phone);
        joinDate = view.findViewById(R.id.join_date);
        coursesCount = view.findViewById(R.id.courses_count);
        userUid = view.findViewById(R.id.user_uid);
        premiumStatus = view.findViewById(R.id.premium_status);
        copyUidButton = view.findViewById(R.id.copy_uid_button);
        // editProfileButton = view.findViewById(R.id.edit_profile_button);
        forgotPasswordButton = view.findViewById(R.id.forgot_password_button);
        myPaymentsButton = view.findViewById(R.id.my_payments_button);
        logoutButton = view.findViewById(R.id.logout_button);
        myCoursesRecyclerView = view.findViewById(R.id.my_courses_recycler_view);
        bookmarkedCoursesRecyclerView = view.findViewById(R.id.bookmarked_courses_recycler_view);
        favouritesSection = view.findViewById(R.id.favourites_section);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);

        prefManager = SharedPrefManager.getInstance(getContext());
        dbHelper = FirebaseDBHelper.getInstance();
        
        setupMyCoursesRecyclerView();
        setupBookmarkedCoursesRecyclerView();
        
        view.findViewById(R.id.view_all_courses).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new EnrolledCoursesFragment())
                .addToBackStack(null)
                .commit();
        });
    }

    private void loadUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            userUid.setText("UID: " + uid);
            
            // Load user data from Firebase Firestore
            dbHelper.getUserDocument(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String email = documentSnapshot.getString("email");
                            String phone = documentSnapshot.getString("phone");
                            Boolean premium = documentSnapshot.getBoolean("premium");

                            userName.setText(name != null ? name : "User");
                            userEmail.setText(email != null ? email : "No email");
                            userPhone.setText(phone != null ? "Phone: " + phone : "Phone: N/A");
                            premiumStatus.setText(premium != null && premium ? "Premium: True" : "Premium: False");

                            if (documentSnapshot.getDate("joinDate") != null) {
                                joinDate.setText("Member since " +
                                    new java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault())
                                        .format(documentSnapshot.getDate("joinDate")));
                            } else {
                                joinDate.setText("Member since 2024");
                            }
                        } else {
                            // Fallback to Firebase Auth data
                            userName.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User");
                            userEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "No email");
                            userPhone.setText("Phone: N/A");
                            premiumStatus.setText("Premium: False");
                            joinDate.setText("Member since 2024");
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Fallback to Firebase Auth data
                        userName.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User");
                        userEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "No email");
                        userPhone.setText("Phone: N/A");
                        premiumStatus.setText("Premium: False");
                        joinDate.setText("Member since 2024");
                    });
        }
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> {
            loadUserData();
            loadEnrolledCoursesCount();
            loadEnrolledCourses();
            loadBookmarkedCourses();
            swipeRefresh.setRefreshing(false);
        });
        swipeRefresh.setColorSchemeResources(
            R.color.primary_start,
            R.color.secondary_start,
            R.color.accent_green
        );
    }
    
    private void setupClickListeners() {
        // editProfileButton.setOnClickListener(v -> showEditProfileDialog());
        forgotPasswordButton.setOnClickListener(v -> showForgotPasswordDialog());
        myPaymentsButton.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new PaymentsFragment())
                .addToBackStack(null)
                .commit();
        });
        logoutButton.setOnClickListener(v -> showLogoutDialog());
        copyUidButton.setOnClickListener(v -> copyUserIdToClipboard());
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_profile, null);
        
        EditText nameEditText = dialogView.findViewById(R.id.edit_name);
        nameEditText.setText(userName.getText().toString());
        
        builder.setView(dialogView)
                .setTitle("Edit Profile")
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = nameEditText.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        updateUserProfile(newName);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateUserProfile(String newName) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            userName.setText(newName);
                            prefManager.saveUserData(user.getUid(), newName, user.getEmail());
                            Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> logout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_forgot_password, null);
        
        EditText emailEditText = dialogView.findViewById(R.id.email_edit_text);
        emailEditText.setText(userEmail.getText().toString());
        
        builder.setView(dialogView)
                .setTitle("Reset Password")
                .setMessage("Enter your email to receive password reset link")
                .setPositiveButton("Send Reset Email", (dialog, which) -> {
                    String email = emailEditText.getText().toString().trim();
                    if (!email.isEmpty()) {
                        sendPasswordResetEmail(email);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void sendPasswordResetEmail(String email) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Password reset email sent to " + email, Toast.LENGTH_LONG).show();
                        // Auto logout after sending reset email
                        new AlertDialog.Builder(getContext())
                                .setTitle("Password Reset Sent")
                                .setMessage("Please check your email and login again with new password.")
                                .setPositiveButton("OK", (dialog, which) -> logout())
                                .setCancelable(false)
                                .show();
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Failed to send reset email";
                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }
    
    private void logout() {
        FirebaseAuth.getInstance().signOut();
        prefManager.clearUserData();
        
        Intent intent = new Intent(getContext(), AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    private void loadEnrolledCoursesCount() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && coursesCount != null) {
            dbHelper.getUserEnrolledCourses(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (coursesCount != null) {
                            int count = queryDocumentSnapshots.size();
                            coursesCount.setText(String.valueOf(count));
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (coursesCount != null) {
                            coursesCount.setText("0");
                        }
                    });
        }
    }
    
    private void setupMyCoursesRecyclerView() {
        myCoursesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        enrolledCourses = new ArrayList<>();
        myCoursesAdapter = new EnrolledCourseAdapter(enrolledCourses, this);
        myCoursesRecyclerView.setAdapter(myCoursesAdapter);
    }
    
    private void setupBookmarkedCoursesRecyclerView() {
        bookmarkedCoursesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        bookmarkedCourses = new ArrayList<>();
        bookmarkedCoursesAdapter = new com.hariomsonihs.notesaura.adapters.CourseAdapter(bookmarkedCourses, new com.hariomsonihs.notesaura.interfaces.OnCourseClickListener() {
            @Override
            public void onCourseClick(com.hariomsonihs.notesaura.models.Course course) {
                Intent intent = new Intent(getContext(), CourseDetailActivity.class);
                intent.putExtra(Constants.KEY_COURSE_ID, course.getId());
                startActivity(intent);
            }
            
            @Override
            public void onEnrollClick(com.hariomsonihs.notesaura.models.Course course) {
                // Not needed for bookmarked courses
            }
        });
        bookmarkedCoursesRecyclerView.setAdapter(bookmarkedCoursesAdapter);
    }
    
    private void loadBookmarkedCourses() {
        bookmarkedCourses.clear();
        bookmarkedCourses.addAll(com.hariomsonihs.notesaura.utils.CourseDataManager.getInstance().getBookmarkedCourses());
        
        // Show/hide favourites section based on whether user has bookmarked courses
        if (favouritesSection != null) {
            favouritesSection.setVisibility(bookmarkedCourses.isEmpty() ? View.GONE : View.VISIBLE);
        }
        
        if (bookmarkedCoursesAdapter != null) {
            bookmarkedCoursesAdapter.notifyDataSetChanged();
        }
    }
    
    private void loadEnrolledCourses() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            dbHelper.getUserEnrolledCourses(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        enrolledCourses.clear();
                        queryDocumentSnapshots.forEach(document -> {
                            String courseId = document.getId();
                            String courseName = document.getString("courseName");
                            String category = document.getString("category");
                            Long progressLong = document.getLong("progressPercentage");
                            int progress = progressLong != null ? progressLong.intValue() : 0;
                            
                            if (courseName != null) {
                                EnrolledCourse course = new EnrolledCourse(courseId, courseName, category != null ? category : "General");
                                course.setProgressPercentage(progress);
                                if (document.getDate("enrollmentDate") != null) {
                                    course.setEnrollmentDate(document.getDate("enrollmentDate"));
                                }
                                if (document.getDate("lastAccessed") != null) {
                                    course.setLastAccessed(document.getDate("lastAccessed"));
                                }
                                enrolledCourses.add(course);
                            }
                        });
                        myCoursesAdapter.notifyDataSetChanged();
                    });
        }
    }
    
    @Override
    public void onEnrolledCourseClick(EnrolledCourse course) {
        Intent intent = new Intent(getContext(), CourseDetailActivity.class);
        intent.putExtra(Constants.KEY_COURSE_ID, course.getCourseId());
        startActivity(intent);
    }
    
    private void copyUserIdToClipboard() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && getContext() != null) {
            String userId = currentUser.getUid();
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("User ID", userId);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getContext(), "User ID copied to clipboard", Toast.LENGTH_SHORT).show();
        }
    }
}