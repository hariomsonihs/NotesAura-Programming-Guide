package com.hariomsonihs.notesaura.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.models.User;
import com.hariomsonihs.notesaura.utils.FirebaseAuthHelper;
import com.hariomsonihs.notesaura.utils.FirebaseDBHelper;
import com.hariomsonihs.notesaura.utils.SharedPrefManager;
import java.util.List;

public class AuthActivity extends AppCompatActivity {
    private static final String TAG = "AuthActivity";
    private TabLayout tabLayout;
    private EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText, phoneEditText;
    private TextInputLayout nameLayout, confirmPasswordLayout, phoneLayout;
    private Button authButton;
    private TextView forgotPasswordText;
    private ProgressBar progressBar;
    private boolean isLoginMode = true;

    private FirebaseAuthHelper authHelper;
    private FirebaseDBHelper dbHelper;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        initializeViews();
        initializeFirebase();
        setupTabLayout();
        setupClickListeners();
        updateUI(); // Initial UI setup
    }

    private void initializeViews() {
    tabLayout = findViewById(R.id.tab_layout);
    nameEditText = findViewById(R.id.name_edit_text);
    nameLayout = findViewById(R.id.name_input_layout);
    phoneEditText = findViewById(R.id.phone_edit_text);
    phoneLayout = findViewById(R.id.phone_input_layout);
    emailEditText = findViewById(R.id.email_edit_text);
    passwordEditText = findViewById(R.id.password_edit_text);
    confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
    confirmPasswordLayout = findViewById(R.id.confirm_password_input_layout);
    authButton = findViewById(R.id.auth_button);
    forgotPasswordText = findViewById(R.id.forgot_password_text);
    progressBar = findViewById(R.id.progress_bar);
    }

    private void initializeFirebase() {
        authHelper = FirebaseAuthHelper.getInstance();
        dbHelper = FirebaseDBHelper.getInstance();
        prefManager = SharedPrefManager.getInstance(this);
    }

    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isLoginMode = tab.getPosition() == 0;
                Log.d(TAG, "Tab selected: " + (isLoginMode ? "Login" : "Signup"));
                updateUI();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void updateUI() {
        if (isLoginMode) {
            nameLayout.setVisibility(View.GONE);
            phoneLayout.setVisibility(View.GONE);
            confirmPasswordLayout.setVisibility(View.GONE);
            authButton.setText("🚀 Login");
            forgotPasswordText.setVisibility(View.VISIBLE);
        } else {
            nameLayout.setVisibility(View.VISIBLE);
            phoneLayout.setVisibility(View.VISIBLE);
            confirmPasswordLayout.setVisibility(View.VISIBLE);
            authButton.setText("👤 Sign Up");
            forgotPasswordText.setVisibility(View.GONE);
        }
        Log.d(TAG, "UI updated - isLoginMode: " + isLoginMode);
    }

    private void setupClickListeners() {
        authButton.setOnClickListener(v -> {
            Log.d(TAG, "Auth button clicked - isLoginMode: " + isLoginMode);
            if (isLoginMode) {
                loginUser();
            } else {
                signupUser();
            }
        });

        forgotPasswordText.setOnClickListener(v -> resetPassword());
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        com.hariomsonihs.notesaura.utils.PermissionManager.handlePermissionResult(this, requestCode, permissions, grantResults, new com.hariomsonihs.notesaura.utils.PermissionManager.PermissionCallback() {
            @Override
            public void onPermissionsGranted() {
                Log.d(TAG, "Permissions granted after request");
                startActivity(new Intent(AuthActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onPermissionsDenied(List<String> deniedPermissions) {
                Log.d(TAG, "Some permissions still denied after request");
                startActivity(new Intent(AuthActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        Log.d(TAG, "Login attempt - Email: " + email);
        
        if (!validateInput(email, password)) return;

        showProgress(true);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Login successful");
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            prefManager.saveUserData(user.getUid(), 
                                user.getDisplayName() != null ? user.getDisplayName() : "User", 
                                user.getEmail());
                            navigateToMain();
                        }
                    } else {
                        Log.e(TAG, "Login failed", task.getException());
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(this, "Login failed: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signupUser() {
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        Log.d(TAG, "Signup attempt - Email: " + email);

        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Name required");
            nameEditText.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(phone) || phone.length() < 10) {
            phoneEditText.setError("Valid phone number required");
            phoneEditText.requestFocus();
            return;
        }
        if (!validateSignupInput(name, email, password, confirmPassword)) return;

        showProgress(true);
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Signup successful");
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (firebaseUser != null) {
                            updateFirebaseProfile(firebaseUser, name, phone);
                        }
                    } else {
                        showProgress(false);
                        Log.e(TAG, "Signup failed", task.getException());
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(this, "Signup failed: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updateFirebaseProfile(FirebaseUser firebaseUser, String name, String phone) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        firebaseUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        createUserProfile(firebaseUser, name, phone);
                    } else {
                        showProgress(false);
                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createUserProfile(FirebaseUser firebaseUser, String name, String phone) {
        java.util.Map<String, Object> userData = new java.util.HashMap<>();
        userData.put("uid", firebaseUser.getUid());
        userData.put("name", name);
        userData.put("email", firebaseUser.getEmail());
        userData.put("phone", phone);
        userData.put("joinDate", new java.util.Date());
        userData.put("premium", false);
        userData.put("totalProgress", 0);
        userData.put("admin", "no"); // Default admin status

        Log.d(TAG, "Creating user with admin field: no");
        userData.put("achievements", null);
        userData.put("enrolledCourses", null);
        userData.put("profileImageUrl", null);

        dbHelper.getUserDocument(firebaseUser.getUid())
                .set(userData)
                .addOnCompleteListener(task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User profile created successfully");
                        prefManager.saveUserData(firebaseUser.getUid(), name, firebaseUser.getEmail());
                        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        Log.e(TAG, "Failed to create user profile", task.getException());
                        Toast.makeText(this, "Failed to create user profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resetPassword() {
        String email = emailEditText.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Enter email address");
            return;
        }

        showProgress(true);
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password reset email sent to " + email, Toast.LENGTH_LONG).show();
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Failed to send reset email";
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            return false;
        }
        return true;
    }

    private boolean validateSignupInput(String name, String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Name is required");
            return false;
        }
        if (!validateInput(email, password)) return false;
        
        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            return false;
        }
        return true;
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        authButton.setEnabled(!show);
    }

    private void navigateToMain() {
        // Check and request permissions before going to main activity
        com.hariomsonihs.notesaura.utils.PermissionManager.checkAndRequestPermissions(this, new com.hariomsonihs.notesaura.utils.PermissionManager.PermissionCallback() {
            @Override
            public void onPermissionsGranted() {
                Log.d(TAG, "All permissions granted, navigating to MainActivity");
                startActivity(new Intent(AuthActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onPermissionsDenied(List<String> deniedPermissions) {
                Log.d(TAG, "Some permissions denied, but continuing to MainActivity");
                // Continue to main activity even if some permissions are denied
                startActivity(new Intent(AuthActivity.this, MainActivity.class));
                finish();
            }
        });
    }
}