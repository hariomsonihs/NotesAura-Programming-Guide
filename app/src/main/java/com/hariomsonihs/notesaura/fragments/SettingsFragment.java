package com.hariomsonihs.notesaura.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.io.File;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.activities.AuthActivity;
import com.hariomsonihs.notesaura.utils.SharedPrefManager;
import com.hariomsonihs.notesaura.utils.FirebaseDBHelper;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.TextView;

public class SettingsFragment extends Fragment {
    private SharedPrefManager prefManager;
    private FirebaseDBHelper dbHelper;
    private TextView pdfViewerStatus;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        
        initializeViews(view);
        setupClickListeners(view);
        
        return view;
    }
    
    private void initializeViews(View view) {
        prefManager = SharedPrefManager.getInstance(getContext());
        dbHelper = FirebaseDBHelper.getInstance();
        pdfViewerStatus = view.findViewById(R.id.pdf_viewer_status);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        updatePdfViewerStatus();
    }
    
    private void setupClickListeners(View view) {
        // PDF Viewer Option
        view.findViewById(R.id.pdf_viewer_option).setOnClickListener(v -> showPdfViewerDialog());
        
        // Clear Cache
        view.findViewById(R.id.clear_cache_option).setOnClickListener(v -> showClearCacheDialog());
        // Delete Account
        view.findViewById(R.id.delete_account_option).setOnClickListener(v -> showDeleteAccountDialog());
        
        // Contact Us
        view.findViewById(R.id.contact_us_option).setOnClickListener(v -> openContactUs());
        
        // Privacy Policy
        view.findViewById(R.id.privacy_policy_option).setOnClickListener(v -> openPrivacyPolicy());
        
        // Terms of Service
        view.findViewById(R.id.terms_service_option).setOnClickListener(v -> openTermsOfService());
        
        // Rate App
        view.findViewById(R.id.rate_app_option).setOnClickListener(v -> rateApp());
        
        // Share App
        view.findViewById(R.id.share_app_option).setOnClickListener(v -> shareApp());
    }
    
    private void showClearCacheDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Clear Cache")
                .setMessage("This will clear all cached data including downloaded course content. Continue?")
                .setPositiveButton("Clear", (dialog, which) -> clearCache())
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void clearCache() {
        try {
            File cacheDir = getContext().getCacheDir();
            deleteDir(cacheDir);
            
            // Clear SharedPreferences cache data
            prefManager.clearCache();
            
            Toast.makeText(getContext(), "Cache cleared successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to clear cache", Toast.LENGTH_SHORT).show();
        }
    }
    
    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) return false;
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        }
        return false;
    }
    
    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Account")
                .setMessage("This will permanently delete your account and all data. This action cannot be undone. Are you sure?")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void deleteAccount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            
            // Delete user data from Firestore
            dbHelper.getUserDocument(userId).delete()
                    .addOnSuccessListener(aVoid -> {
                        // Delete user enrollments
                        dbHelper.getUserEnrolledCourses(userId).get()
                                .addOnSuccessListener(querySnapshot -> {
                                    querySnapshot.getDocuments().forEach(doc -> doc.getReference().delete());
                                });
                        
                        // Delete Firebase Auth account
                        user.delete()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        prefManager.clearUserData();
                                        Toast.makeText(getContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                        
                                        Intent intent = new Intent(getContext(), AuthActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        getActivity().finish();
                                    } else {
                                        Toast.makeText(getContext(), "Failed to delete account", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to delete user data", Toast.LENGTH_SHORT).show();
                    });
        }
    }
    
    private void openContactUs() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userInfo = "";
        
        if (user != null) {
            userInfo = "\n\n--- User Information ---\n" +
                    "User ID: " + user.getUid() + "\n" +
                    "Email: " + user.getEmail() + "\n" +
                    "App Version: 1.0.0\n" +
                    "Device: Android";
        }
        
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:notesaura@gmail.com"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "NotesAura Support Request");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hi NotesAura Team,\n\nI need help with:" + userInfo);
        
        try {
            startActivity(Intent.createChooser(emailIntent, "Send Email"));
        } catch (Exception e) {
            Toast.makeText(getContext(), "No email app found", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void openPrivacyPolicy() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://notesaura.com/privacy"));
        try {
            startActivity(browserIntent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "No browser found", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void openTermsOfService() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://notesaura.com/terms"));
        try {
            startActivity(browserIntent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "No browser found", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void rateApp() {
        try {
            Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getContext().getPackageName()));
            startActivity(rateIntent);
        } catch (Exception e) {
            Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getContext().getPackageName()));
            try {
                startActivity(rateIntent);
            } catch (Exception ex) {
                Toast.makeText(getContext(), "Unable to open Play Store", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void shareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out NotesAura Programming Guide!");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Learn programming with NotesAura! Download now: https://play.google.com/store/apps/details?id=" + getContext().getPackageName());
        
        try {
            startActivity(Intent.createChooser(shareIntent, "Share App"));
        } catch (Exception e) {
            Toast.makeText(getContext(), "Unable to share", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showPdfViewerDialog() {
        String currentViewer = sharedPreferences.getString("pdf_viewer_type", "native");
        
        String[] options = {"Native Viewer (Better Quality)", "Web Viewer (Better Performance)"};
        int selectedIndex = "web".equals(currentViewer) ? 1 : 0;
        
        new AlertDialog.Builder(getContext())
                .setTitle("Choose PDF Viewer")
                .setSingleChoiceItems(options, selectedIndex, (dialog, which) -> {
                    String newViewer = (which == 1) ? "web" : "native";
                    sharedPreferences.edit().putString("pdf_viewer_type", newViewer).apply();
                    updatePdfViewerStatus();
                    dialog.dismiss();
                    
                    String message = (which == 1) ? 
                        "Web viewer selected. Better for large PDFs with smooth scrolling." :
                        "Native viewer selected. Better quality rendering with zoom support.";
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void updatePdfViewerStatus() {
        String currentViewer = sharedPreferences.getString("pdf_viewer_type", "native");
        if ("web".equals(currentViewer)) {
            pdfViewerStatus.setText("Web (Better Performance)");
        } else {
            pdfViewerStatus.setText("Native (Better Quality)");
        }
    }
}