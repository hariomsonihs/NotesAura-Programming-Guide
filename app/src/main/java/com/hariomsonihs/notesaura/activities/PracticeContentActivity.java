package com.hariomsonihs.notesaura.activities;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.models.PracticeExercise;
import com.hariomsonihs.notesaura.utils.PracticeDataManager;

public class PracticeContentActivity extends AppCompatActivity {
    private WebView webView;
    private PracticeDataManager dataManager;
    private String exerciseId;
    private String exerciseTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice_content);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        exerciseId = getIntent().getStringExtra("exercise_id");
        exerciseTitle = getIntent().getStringExtra("exercise_title");
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(exerciseTitle);
        webView = findViewById(R.id.practice_webview);
        webView.setWebViewClient(new WebViewClient());
        dataManager = PracticeDataManager.getInstance();
        PracticeExercise exercise = dataManager.getExercise(exerciseId);
        if (exercise != null && exercise.getFileLink() != null) {
            webView.loadUrl(exercise.getFileLink());
        }
    }
}
