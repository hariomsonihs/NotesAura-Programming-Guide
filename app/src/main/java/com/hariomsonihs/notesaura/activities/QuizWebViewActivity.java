package com.hariomsonihs.notesaura.activities;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.hariomsonihs.notesaura.R;

public class QuizWebViewActivity extends AppCompatActivity {
    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_webview);
        
        String quizUrl = getIntent().getStringExtra("quiz_url");
        String quizTitle = getIntent().getStringExtra("quiz_title");
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(quizTitle);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        webView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progress_bar);
        
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }
        });
        
        if (quizUrl != null && !quizUrl.isEmpty()) {
            webView.loadUrl(quizUrl);
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