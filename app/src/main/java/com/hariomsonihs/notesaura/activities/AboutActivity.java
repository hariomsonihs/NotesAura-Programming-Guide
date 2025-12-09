package com.hariomsonihs.notesaura.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.hariomsonihs.notesaura.R;

public class AboutActivity extends AppCompatActivity {
    
    private TextView versionText;
    private CardView emailCard, phoneCard, githubCard, instagramCard, linkedinCard;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        
        // Hide status bar for immersive experience
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_FULLSCREEN
        );
        
        initViews();
        setupClickListeners();
        setAppVersion();
    }
    
    private void initViews() {
        versionText = findViewById(R.id.version_text);
        emailCard = findViewById(R.id.email_card);
        phoneCard = findViewById(R.id.phone_card);
        githubCard = findViewById(R.id.github_card);
        instagramCard = findViewById(R.id.instagram_card);
        linkedinCard = findViewById(R.id.linkedin_card);
        
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
    }
    
    private void setupClickListeners() {
        emailCard.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:hshariomsoni@gmail.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "NotesAura App Feedback");
            startActivity(Intent.createChooser(emailIntent, "Send Email"));
        });
        
        phoneCard.setOnClickListener(v -> {
            Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
            phoneIntent.setData(Uri.parse("tel:+917667110195"));
            startActivity(phoneIntent);
        });
        
        githubCard.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/hariomsonihs"));
            startActivity(browserIntent);
        });
        
        instagramCard.setOnClickListener(v -> {
            try {
                Intent instagramIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/_u/hariomsonihs"));
                instagramIntent.setPackage("com.instagram.android");
                startActivity(instagramIntent);
            } catch (Exception e) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/hariomsonihs"));
                startActivity(browserIntent);
            }
        });
        
        linkedinCard.setOnClickListener(v -> {
            try {
                Intent linkedinIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("linkedin://profile/hariomsoni"));
                startActivity(linkedinIntent);
            } catch (Exception e) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://linkedin.com/in/hariomsonihs"));
                startActivity(browserIntent);
            }
        });
    }
    
    private void setAppVersion() {
        try {
            android.content.pm.PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionText.setText("Version " + pInfo.versionName);
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            versionText.setText("Version 1.0");
        }
    }
}