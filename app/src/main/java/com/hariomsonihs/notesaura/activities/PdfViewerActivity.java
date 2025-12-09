package com.hariomsonihs.notesaura.activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Bundle;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Environment;
import androidx.core.content.FileProvider;
import android.content.ContentValues;
import android.provider.MediaStore;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.adapters.PdfPageAdapter;
import com.hariomsonihs.notesaura.adapters.PageIndicatorAdapter;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class PdfViewerActivity extends AppCompatActivity {
    private RecyclerView pdfRecyclerView;
    private LinearLayout loadingLayout, searchLayout, fabMenuItems;
    private ProgressBar progressBar;
    private TextView loadingText;
    private EditText searchEditText;
    private ImageButton searchBtn, closeSearchBtn;
    private FloatingActionButton fabMenu, fabSearch, fabJump, fabSave, fabRotate;
    private PdfPageAdapter pdfAdapter;

    private String pdfUrl, pdfTitle;
    private File currentPdfFile;
    private boolean isFabMenuOpen = false;
    private int currentRotation = 0;
    private List<String> pdfTextPages = new ArrayList<>();
    private static final String CHANNEL_ID = "pdf_download_channel";
    private static final int NOTIFICATION_ID = 1001;
    private static final int PERMISSION_REQUEST_CODE = 1002;
    private static final int STORAGE_PERMISSION_CODE = 1003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setDecorFitsSystemWindows(false);

        setContentView(R.layout.activity_pdf_viewer);

        pdfUrl = getIntent().getStringExtra("pdf_url");
        pdfTitle = getIntent().getStringExtra("pdf_title");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(pdfTitle != null ? pdfTitle : "PDF Viewer");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();
        setupFabMenu();
        setupSearch();
        


        // Show loading initially
        showLoading();

        createNotificationChannel();
        checkNotificationPermission();

        // Initialize PDFBox
        PDFBoxResourceLoader.init(getApplicationContext());
        
        if (pdfUrl != null) {
            File cachedFile = getCachedPdfFile(pdfUrl);
            if (cachedFile.exists()) {
                // Load from cache
                currentPdfFile = cachedFile;
                loadPdfFromFile(cachedFile);
            } else {
                // Download and cache
                new DownloadPdf().execute(pdfUrl);
            }
        }
    }

    private void showLoading() {
        loadingLayout.setVisibility(View.VISIBLE);
        pdfRecyclerView.setVisibility(View.GONE);
        loadingText.setText("📚 Loading " + (pdfTitle != null ? pdfTitle : "PDF") + "...");
    }

    private void hideLoading() {
        loadingLayout.setVisibility(View.GONE);
        pdfRecyclerView.setVisibility(View.VISIBLE);
    }

    private File getCachedPdfFile(String url) {
        String fileName = generateFileName(url) + ".pdf";
        File cacheDir = new File(getFilesDir(), "pdf_cache");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return new File(cacheDir, fileName);
    }

    private String generateFileName(String url) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(url.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(url.hashCode());
        }
    }

    private class DownloadPdf extends AsyncTask<String, Integer, File> {
        private NotificationCompat.Builder notificationBuilder;
        private NotificationManagerCompat notificationManager;

        @Override
        protected void onPreExecute() {
            notificationManager = NotificationManagerCompat.from(PdfViewerActivity.this);
            notificationBuilder = new NotificationCompat.Builder(PdfViewerActivity.this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_download)
                    .setContentTitle("NotesAura - Downloading PDF")
                    .setContentText(pdfTitle != null ? pdfTitle : "PDF File")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setProgress(100, 0, false)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .setShowWhen(true);

            if (ActivityCompat.checkSelfPermission(PdfViewerActivity.this,
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
            }
        }

        @Override
        protected File doInBackground(String... urls) {
            try {
                String originalUrl = urls[0];
                File cachedFile = getCachedPdfFile(originalUrl);

                String directUrl = convertToDirectLink(originalUrl);
                URL url = new URL(directUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                int fileLength = connection.getContentLength();
                FileOutputStream fos = new FileOutputStream(cachedFile);
                InputStream is = connection.getInputStream();

                byte[] buffer = new byte[1024];
                int len;
                long total = 0;
                int lastProgress = 0;

                while ((len = is.read(buffer)) != -1) {
                    total += len;
                    fos.write(buffer, 0, len);

                    if (fileLength > 0) {
                        int progress = (int) ((total * 100) / fileLength);
                        if (progress != lastProgress && progress % 2 == 0) {
                            publishProgress(progress);
                            lastProgress = progress;
                        }
                    }
                }
                fos.close();
                is.close();

                return cachedFile;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            int currentProgress = progress[0];
            progressBar.setProgress(currentProgress);
            loadingText.setText("📚 Downloading " + currentProgress + "%");

            if (ActivityCompat.checkSelfPermission(PdfViewerActivity.this,
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {

                NotificationCompat.Builder updatedBuilder = new NotificationCompat.Builder(PdfViewerActivity.this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_download)
                        .setContentTitle("NotesAura - Downloading PDF")
                        .setContentText(pdfTitle + " - " + currentProgress + "%")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setProgress(100, currentProgress, false)
                        .setOngoing(true)
                        .setAutoCancel(false)
                        .setShowWhen(true);

                notificationManager.notify(NOTIFICATION_ID, updatedBuilder.build());
            }
        }

        @Override
        protected void onPostExecute(File pdfFile) {
            // Cancel progress notification first
            if (ActivityCompat.checkSelfPermission(PdfViewerActivity.this,
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.cancel(NOTIFICATION_ID);
            }

            if (pdfFile != null) {
                currentPdfFile = pdfFile;
                loadPdfFromFile(pdfFile);
                showDownloadCompleteNotification();
            } else {
                hideLoading();
                Toast.makeText(PdfViewerActivity.this, "Error loading PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadPdfFromFile(File file) {
        new RenderPdfTask().execute(file);
    }

    private class RenderPdfTask extends AsyncTask<File, Integer, List<Bitmap>> {
        @Override
        protected List<Bitmap> doInBackground(File... files) {
            List<Bitmap> pages = new ArrayList<>();
            try {
                ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(files[0], ParcelFileDescriptor.MODE_READ_ONLY);
                PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
                
                int pageCount = pdfRenderer.getPageCount();
                for (int i = 0; i < pageCount; i++) {
                    PdfRenderer.Page page = pdfRenderer.openPage(i);
                    
                    // Simple high-quality rendering
                    int width = page.getWidth() * 2;
                    int height = page.getHeight() * 2;
                    
                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    pages.add(bitmap);
                    page.close();
                    publishProgress((i + 1) * 100 / pageCount);
                }
                pdfRenderer.close();
                fileDescriptor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return pages;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            progressBar.setProgress(progress[0]);
            loadingText.setText("📚 Rendering " + progress[0] + "%");
        }

        @Override
        protected void onPostExecute(List<Bitmap> pages) {
            if (!pages.isEmpty()) {
                pdfAdapter = new PdfPageAdapter(pages, currentRotation);
                pdfRecyclerView.setAdapter(pdfAdapter);
                
                // Extract text for search
                extractPdfText();
                
                hideLoading();
                Toast.makeText(PdfViewerActivity.this, "PDF loaded successfully", Toast.LENGTH_SHORT).show();
            } else {
                hideLoading();
                Toast.makeText(PdfViewerActivity.this, "Error loading PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void initViews() {
        pdfRecyclerView = findViewById(R.id.pdfRecyclerView);
        pdfRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        loadingLayout = findViewById(R.id.loadingLayout);
        progressBar = findViewById(R.id.progressBar);
        loadingText = findViewById(R.id.loadingText);
        
        searchLayout = findViewById(R.id.searchLayout);
        searchEditText = findViewById(R.id.searchEditText);
        searchBtn = findViewById(R.id.searchBtn);
        closeSearchBtn = findViewById(R.id.closeSearchBtn);
        
        fabMenu = findViewById(R.id.fabMenu);
        fabMenuItems = findViewById(R.id.fabMenuItems);
        fabSearch = findViewById(R.id.fabSearch);
        fabJump = findViewById(R.id.fabJump);
        fabSave = findViewById(R.id.fabSave);
        fabRotate = findViewById(R.id.fabRotate);
    }
    
    private void setupFabMenu() {
        fabMenu.setOnClickListener(v -> toggleFabMenu());
        fabSearch.setOnClickListener(v -> showSearch());
        fabJump.setOnClickListener(v -> showJumpDialog());
        fabSave.setOnClickListener(v -> savePdfToDevice());
        fabRotate.setOnClickListener(v -> rotatePdf());
    }
    
    private void setupSearch() {
        searchBtn.setOnClickListener(v -> performSearch());
        closeSearchBtn.setOnClickListener(v -> hideSearch());
    }
    
    private void toggleFabMenu() {
        if (isFabMenuOpen) {
            fabMenuItems.setVisibility(View.GONE);
            fabMenu.setImageResource(R.drawable.ic_menu);
        } else {
            fabMenuItems.setVisibility(View.VISIBLE);
            fabMenu.setImageResource(R.drawable.ic_close);
        }
        isFabMenuOpen = !isFabMenuOpen;
    }
    
    private void showSearch() {
        searchLayout.setVisibility(View.VISIBLE);
        searchEditText.requestFocus();
        toggleFabMenu();
    }
    
    private void hideSearch() {
        searchLayout.setVisibility(View.GONE);
        searchEditText.setText("");
    }
    
    private void performSearch() {
        String query = searchEditText.getText().toString().trim();
        if (!query.isEmpty()) {
            searchInPdf(query);
        } else {
            Toast.makeText(this, "Please enter search text", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void extractPdfText() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    PDDocument document = PDDocument.load(currentPdfFile);
                    PDFTextStripper stripper = new PDFTextStripper();
                    
                    pdfTextPages.clear();
                    for (int i = 1; i <= document.getNumberOfPages(); i++) {
                        stripper.setStartPage(i);
                        stripper.setEndPage(i);
                        String pageText = stripper.getText(document);
                        pdfTextPages.add(pageText);
                    }
                    
                    document.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }
    
    private void searchInPdf(String query) {
        new AsyncTask<String, Void, List<Integer>>() {
            @Override
            protected List<Integer> doInBackground(String... queries) {
                List<Integer> foundPages = new ArrayList<>();
                String searchQuery = queries[0].toLowerCase();
                
                for (int i = 0; i < pdfTextPages.size(); i++) {
                    String pageText = pdfTextPages.get(i).toLowerCase();
                    if (pageText.contains(searchQuery)) {
                        foundPages.add(i);
                    }
                }
                return foundPages;
            }
            
            @Override
            protected void onPostExecute(List<Integer> foundPages) {
                if (!foundPages.isEmpty()) {
                    showSearchResults(foundPages, searchEditText.getText().toString());
                } else {
                    Toast.makeText(PdfViewerActivity.this, "No results found", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(query);
    }
    
    private void showSearchResults(List<Integer> foundPages, String query) {
        StringBuilder message = new StringBuilder();
        message.append("Found '").append(query).append("' on pages: ");
        
        for (int i = 0; i < foundPages.size(); i++) {
            message.append(foundPages.get(i) + 1);
            if (i < foundPages.size() - 1) {
                message.append(", ");
            }
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Search Results")
               .setMessage(message.toString())
               .setPositiveButton("Go to First", (dialog, which) -> {
                   pdfRecyclerView.scrollToPosition(foundPages.get(0));
                   hideSearch();
               })
               .setNegativeButton("Close", null)
               .show();
    }
    
    private void showJumpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Jump to Page");
        
        EditText input = new EditText(this);
        input.setHint("Enter page number");
        builder.setView(input);
        
        builder.setPositiveButton("Jump", (dialog, which) -> {
            try {
                int page = Integer.parseInt(input.getText().toString()) - 1;
                if (page >= 0 && pdfAdapter != null && page < pdfAdapter.getItemCount()) {
                    pdfRecyclerView.scrollToPosition(page);
                } else {
                    Toast.makeText(this, "Invalid page number", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
        toggleFabMenu();
    }
    
    private void savePdfToDevice() {
        if (currentPdfFile != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                savePdfToMediaStore();
            } else {
                if (checkStoragePermission()) {
                    savePdfToDownloads();
                } else {
                    requestStoragePermission();
                }
            }
        }
        toggleFabMenu();
    }
    
    private void savePdfToMediaStore() {
        try {
            String fileName = getOriginalFileName();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/NotesAura");
            
            android.net.Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            
            if (uri != null) {
                OutputStream outputStream = getContentResolver().openOutputStream(uri);
                FileInputStream fis = new FileInputStream(currentPdfFile);
                
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                
                fis.close();
                outputStream.close();
                
                Toast.makeText(this, "PDF saved: Downloads/NotesAura/" + fileName, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void savePdfToDownloads() {
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File notesAuraDir = new File(downloadsDir, "NotesAura");
            if (!notesAuraDir.exists()) {
                notesAuraDir.mkdirs();
            }
            
            String fileName = getOriginalFileName();
            File destFile = new File(notesAuraDir, fileName);
            
            FileInputStream fis = new FileInputStream(currentPdfFile);
            FileOutputStream fos = new FileOutputStream(destFile);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            
            fis.close();
            fos.close();
            
            Toast.makeText(this, "PDF saved: Downloads/NotesAura/" + fileName, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private String getOriginalFileName() {
        String fileName = pdfTitle;
        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = "PDF_Document";
        }
        
        // Clean filename - remove invalid characters
        fileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        // Add .pdf extension if not present
        if (!fileName.toLowerCase().endsWith(".pdf")) {
            fileName += ".pdf";
        }
        
        return fileName;
    }
    
    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
    
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }
    
    private void rotatePdf() {
        currentRotation = (currentRotation + 90) % 360;
        if (pdfAdapter != null) {
            pdfAdapter.setRotation(currentRotation);
        }
        toggleFabMenu();
    }


    private String convertToDirectLink(String shareUrl) {
        if (shareUrl.contains("drive.google.com")) {
            if (shareUrl.contains("/file/d/")) {
                String fileId = shareUrl.substring(shareUrl.indexOf("/file/d/") + 8);
                fileId = fileId.substring(0, fileId.indexOf("/"));
                return "https://drive.google.com/uc?export=download&id=" + fileId;
            }
        }
        return shareUrl;
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "PDF Downloads";
            String description = "Notifications for PDF download progress";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.enableVibration(false);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showDownloadCompleteNotification() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Intent intent = new Intent(this, PdfViewerActivity.class);
        intent.putExtra("pdf_url", pdfUrl);
        intent.putExtra("pdf_title", pdfTitle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_check)
                .setContentTitle("NotesAura - PDF Downloaded")
                .setContentText(pdfTitle != null ? pdfTitle : "PDF File")
                .setSubText("Tap to open")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_eye, "Open PDF", pendingIntent)
                .setAutoCancel(true)
                .setShowWhen(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID + 1, builder.build());
        notificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                savePdfToDownloads();
            } else {
                Toast.makeText(this, "Storage permission required to save PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
