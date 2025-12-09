package com.hariomsonihs.notesaura.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.utils.FirebaseAuthHelper;
import com.hariomsonihs.notesaura.utils.SharedPrefManager;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DELAY = 3000; // 3 seconds
    private CardView logoContainer;
    private View appName, appSubtitle, progressSection;
    private View circle1, circle2;
    private android.widget.TextView versionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        initViews();
        startAnimations();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            checkUserAuthentication();
        }, SPLASH_DELAY);
    }
    
    private void initViews() {
        logoContainer = findViewById(R.id.logo_container);
        appName = findViewById(R.id.app_name);
        appSubtitle = findViewById(R.id.app_subtitle);
        progressSection = findViewById(R.id.progress_section);
        circle1 = findViewById(R.id.circle1);
        circle2 = findViewById(R.id.circle2);
        versionText = findViewById(R.id.version_text);
        
        // Set actual app version
        setAppVersion();
    }
    
    private void setAppVersion() {
        try {
            android.content.pm.PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionText.setText("Version " + pInfo.versionName);
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            versionText.setText("Version 1.0");
        }
    }
    
    private void startAnimations() {
        // Logo animation
        ObjectAnimator logoScale = ObjectAnimator.ofFloat(logoContainer, "scaleX", 0f, 1f);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(logoContainer, "scaleY", 0f, 1f);
        ObjectAnimator logoRotate = ObjectAnimator.ofFloat(logoContainer, "rotation", -180f, 0f);
        
        AnimatorSet logoSet = new AnimatorSet();
        logoSet.playTogether(logoScale, logoScaleY, logoRotate);
        logoSet.setDuration(800);
        logoSet.setInterpolator(new AccelerateDecelerateInterpolator());
        
        // Text animations
        ObjectAnimator nameAlpha = ObjectAnimator.ofFloat(appName, "alpha", 0f, 1f);
        ObjectAnimator nameTranslate = ObjectAnimator.ofFloat(appName, "translationY", 50f, 0f);
        
        AnimatorSet nameSet = new AnimatorSet();
        nameSet.playTogether(nameAlpha, nameTranslate);
        nameSet.setDuration(600);
        nameSet.setStartDelay(400);
        
        ObjectAnimator subtitleAlpha = ObjectAnimator.ofFloat(appSubtitle, "alpha", 0f, 1f);
        ObjectAnimator subtitleTranslate = ObjectAnimator.ofFloat(appSubtitle, "translationY", 30f, 0f);
        
        AnimatorSet subtitleSet = new AnimatorSet();
        subtitleSet.playTogether(subtitleAlpha, subtitleTranslate);
        subtitleSet.setDuration(600);
        subtitleSet.setStartDelay(600);
        
        // Progress animation
        ObjectAnimator progressAlpha = ObjectAnimator.ofFloat(progressSection, "alpha", 0f, 1f);
        progressAlpha.setDuration(400);
        progressAlpha.setStartDelay(1000);
        
        // Background circles animation
        RotateAnimation rotate1 = new RotateAnimation(0f, 360f, 
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate1.setDuration(20000);
        rotate1.setRepeatCount(Animation.INFINITE);
        circle1.startAnimation(rotate1);
        
        RotateAnimation rotate2 = new RotateAnimation(360f, 0f, 
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate2.setDuration(15000);
        rotate2.setRepeatCount(Animation.INFINITE);
        circle2.startAnimation(rotate2);
        
        // Start all animations
        logoSet.start();
        nameSet.start();
        subtitleSet.start();
        progressAlpha.start();
    }

    private void checkUserAuthentication() {
        FirebaseAuthHelper authHelper = FirebaseAuthHelper.getInstance();
        SharedPrefManager prefManager = SharedPrefManager.getInstance(this);

        if (authHelper.isUserLoggedIn() && prefManager.isLoggedIn()) {
            // User is logged in, go to main activity
            startActivity(new Intent(this, MainActivity.class));
        } else {
            // User is not logged in, go to auth activity
            startActivity(new Intent(this, AuthActivity.class));
        }
        finish();
    }
}