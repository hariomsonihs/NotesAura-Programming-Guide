package com.hariomsonihs.notesaura.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.adapters.EbookAdapter;
import com.hariomsonihs.notesaura.models.Ebook;
import java.util.ArrayList;
import java.util.List;

public class EbooksListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EbookAdapter adapter;
    private List<Ebook> ebooks;
    private FirebaseFirestore db;
    private String subcategoryId;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().setDecorFitsSystemWindows(false);
        
        setContentView(R.layout.activity_ebooks_list);
        
        subcategoryId = getIntent().getStringExtra("subcategory_id");
        String subcategoryName = getIntent().getStringExtra("subcategory_name");
        
        initViews(subcategoryName);
        setupRecyclerView();
        loadEbooks();
    }

    private void initViews(String subcategoryName) {
        recyclerView = findViewById(R.id.recycler_ebooks);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        TextView titleText = findViewById(R.id.title_text);
        titleText.setText(subcategoryName);
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        db = FirebaseFirestore.getInstance();
        
        setupSwipeRefresh();
    }
    
    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> {
            if (com.hariomsonihs.notesaura.utils.NetworkUtil.isNetworkAvailable(this)) {
                loadEbooks();
            } else {
                swipeRefresh.setRefreshing(false);
                com.hariomsonihs.notesaura.utils.OfflineHelper.showNoInternetMessage(this);
            }
        });
        swipeRefresh.setColorSchemeResources(
            R.color.primary_start,
            R.color.secondary_start,
            R.color.accent_green
        );
    }

    private void setupRecyclerView() {
        ebooks = new ArrayList<>();
        adapter = new EbookAdapter(ebooks, new EbookAdapter.OnEbookClickListener() {
            @Override
            public void onEbookClick(Ebook ebook) {
                openPdfViewer(ebook);
            }

            @Override
            public void onEbookOpen(Ebook ebook) {
                openPdfViewer(ebook);
            }

            @Override
            public void onEbookDelete(Ebook ebook) {
                showDeleteConfirmation(ebook);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadEbooks() {
        db.collection("ebooks")
                .whereEqualTo("subcategoryId", subcategoryId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ebooks.clear();
                    queryDocumentSnapshots.forEach(doc -> {
                        Ebook ebook = doc.toObject(Ebook.class);
                        ebook.setId(doc.getId());
                        ebooks.add(ebook);
                    });
                    adapter.notifyDataSetChanged();
                    swipeRefresh.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    swipeRefresh.setRefreshing(false);
                    if (!com.hariomsonihs.notesaura.utils.NetworkUtil.isNetworkAvailable(this)) {
                        com.hariomsonihs.notesaura.utils.OfflineHelper.showNoInternetMessage(this);
                    } else {
                        Toast.makeText(this, "Failed to load ebooks", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh adapter to update download icons
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void openPdfViewer(Ebook ebook) {
        Intent intent = new Intent(this, PdfViewerActivity.class);
        intent.putExtra("pdf_url", ebook.getPdfUrl());
        intent.putExtra("pdf_name", ebook.getTitle());
        startActivity(intent);
    }
    
    private void showDeleteConfirmation(Ebook ebook) {
        Dialog dialog = new Dialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_confirmation, null);
        dialog.setContentView(dialogView);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        TextView messageText = dialogView.findViewById(R.id.message_text);
        messageText.setText("Delete \"" + ebook.getTitle() + "\" from downloads?");
        
        dialogView.findViewById(R.id.cancel_button).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.delete_button).setOnClickListener(v -> {
            if (adapter.deleteCachedPdf(this, ebook.getPdfUrl())) {
                Toast.makeText(this, "PDF deleted successfully", Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Failed to delete PDF", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });
        
        dialog.show();
    }
}