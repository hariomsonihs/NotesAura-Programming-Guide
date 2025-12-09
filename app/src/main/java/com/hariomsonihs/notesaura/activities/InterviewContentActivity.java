package com.hariomsonihs.notesaura.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.models.InterviewQuestion;
import com.hariomsonihs.notesaura.utils.InterviewDataManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class InterviewContentActivity extends AppCompatActivity {
    private static final String TAG = "InterviewContentActivity";
    private WebView webView;
    private InterviewDataManager dataManager;
    private String questionId;
    private String questionTitle;
    private Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interview_content);
        
        questionId = getIntent().getStringExtra("question_id");
        questionTitle = getIntent().getStringExtra("question_title");
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(questionTitle);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        webView = findViewById(R.id.interview_webview);
        configureWebView();
        
        dataManager = InterviewDataManager.getInstance();
        InterviewQuestion question = dataManager.getQuestion(questionId);
        if (question != null && question.getWebLink() != null && !question.getWebLink().isEmpty()) {
            String url = question.getWebLink();
            if (url.contains("drive.google.com") && url.contains("/file/d/")) {
                // Use internal PDF viewer
                openPdfInInternalViewer(url);
                finish();
            } else {
                webView.loadUrl(url);
            }
        }
    }
    
    private void configureWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        
        webSettings.setCacheMode(isNetworkAvailable() ? 
                WebSettings.LOAD_DEFAULT : 
                WebSettings.LOAD_CACHE_ELSE_NETWORK);
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("drive.google.com") && url.contains("/file/d/")) {
                    openPdfInInternalViewer(url);
                    return true;
                }
                return false;
            }
            
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    Toast.makeText(InterviewContentActivity.this, "Error loading content", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    private void loadPdfWithCaching(String driveUrl) {
        String fileId = extractFileId(driveUrl);
        if (fileId == null) {
            webView.loadUrl(driveUrl);
            return;
        }

        File pdfDir = new File(getFilesDir(), "cached_pdfs");
        if (!pdfDir.exists()) {
            pdfDir.mkdirs();
        }
        File pdfFile = new File(pdfDir, fileId + ".pdf");

        if (pdfFile.exists()) {
            Log.d(TAG, "Loading cached PDF: " + fileId);
            openPdfInInternalViewer(driveUrl);
            return;
        }

        if (!isNetworkAvailable()) {
            Toast.makeText(this, "PDF not available offline. Please connect to internet first.", Toast.LENGTH_LONG).show();
            return;
        }

        showProgressDialog();
        new DownloadPdfTask(pdfFile, fileId).execute(driveUrl);
    }
    
    private String extractFileId(String url) {
        if (url.contains("/file/d/")) {
            int start = url.indexOf("/d/") + 3;
            int end = url.indexOf("/", start);
            if (end == -1) {
                end = url.indexOf("?", start);
            }
            if (end == -1) {
                end = url.length();
            }
            return url.substring(start, end);
        }
        return null;
    }
    
    private void openPdfInInternalViewer(String pdfUrl) {
        Intent intent = new Intent(this, PdfViewerActivity.class);
        intent.putExtra("pdf_url", pdfUrl);
        intent.putExtra("pdf_name", questionTitle);
        startActivity(intent);
    }
    
    private void showProgressDialog() {
        progressDialog = new Dialog(this);
        progressDialog.setContentView(R.layout.dialog_pdf_progress);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
    
    private void updateProgress(int progress) {
        if (progressDialog != null && progressDialog.isShowing()) {
            ProgressBar progressBar = progressDialog.findViewById(R.id.progress_bar);
            TextView progressText = progressDialog.findViewById(R.id.progress_text);
            TextView subtitle = progressDialog.findViewById(R.id.progress_subtitle);
            
            progressBar.setProgress(progress);
            progressText.setText(progress + "%");
            
            if (progress < 30) {
                subtitle.setText("Connecting to server...");
            } else if (progress < 70) {
                subtitle.setText("Downloading PDF content...");
            } else if (progress < 100) {
                subtitle.setText("Almost ready...");
            } else {
                subtitle.setText("Opening PDF...");
            }
        }
    }
    
    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    
    private class DownloadPdfTask extends AsyncTask<String, Integer, Boolean> {
        private File pdfFile;
        private String fileId;

        public DownloadPdfTask(File pf, String fid) {
            pdfFile = pf;
            fileId = fid;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                String downloadUrl = "https://drive.google.com/uc?export=download&id=" + fileId;
                URL urlObj = new URL(downloadUrl);
                HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(30000);
                conn.connect();

                if (conn.getResponseCode() == 200) {
                    int fileLength = conn.getContentLength();
                    InputStream is = conn.getInputStream();
                    FileOutputStream fos = new FileOutputStream(pdfFile);
                    
                    byte[] buffer = new byte[4096];
                    int len;
                    long total = 0;
                    
                    while ((len = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                        total += len;
                        
                        if (fileLength > 0) {
                            int progress = (int) ((total * 100) / fileLength);
                            publishProgress(progress);
                        }
                    }
                    
                    fos.close();
                    is.close();
                    return true;
                }
            } catch (Exception e) {
                Log.e(TAG, "PDF download error", e);
            }
            return false;
        }
        
        @Override
        protected void onProgressUpdate(Integer... progress) {
            updateProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            hideProgressDialog();
            
            if (success && pdfFile.exists()) {
                Toast.makeText(InterviewContentActivity.this, "PDF downloaded successfully!", Toast.LENGTH_SHORT).show();
                // Use internal PDF viewer with original URL
                String originalUrl = "https://drive.google.com/file/d/" + fileId + "/view";
                openPdfInInternalViewer(originalUrl);
            } else {
                String previewUrl = "https://drive.google.com/file/d/" + fileId + "/preview";
                webView.loadUrl(previewUrl);
                Toast.makeText(InterviewContentActivity.this, "Using online viewer", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ?
                connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}