package com.hariomsonihs.notesaura.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.models.Ebook;
import java.io.File;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EbookAdapter extends RecyclerView.Adapter<EbookAdapter.ViewHolder> {
    private List<Ebook> ebooks;
    private OnEbookClickListener listener;
    private Set<String> downloadedEbooks = new HashSet<>();

    public interface OnEbookClickListener {
        void onEbookClick(Ebook ebook);
        void onEbookOpen(Ebook ebook);
        void onEbookDelete(Ebook ebook);
    }

    public EbookAdapter(List<Ebook> ebooks, OnEbookClickListener listener) {
        this.ebooks = ebooks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ebook, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ebook ebook = ebooks.get(position);
        holder.titleText.setText(ebook.getTitle());
        holder.authorText.setText(ebook.getAuthor());
        holder.descriptionText.setText(ebook.getDescription());

        // Clear any background first
        holder.imageView.setBackground(null);
        
        if (ebook.getImageUrl() != null && !ebook.getImageUrl().trim().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(ebook.getImageUrl())
                    .placeholder(R.drawable.ic_book_placeholder)
                    .error(R.drawable.ic_book_placeholder)
                    .centerCrop()
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_book_placeholder);
            holder.imageView.setBackgroundResource(R.drawable.rounded_corner_background);
        }

        // Set icons based on cache status
        if (isPdfCached(holder.itemView.getContext(), ebook.getPdfUrl())) {
            holder.downloadIcon.setVisibility(View.GONE);
            holder.eyeIcon.setVisibility(View.VISIBLE);
            holder.deleteIcon.setVisibility(View.VISIBLE);
        } else {
            holder.downloadIcon.setVisibility(View.VISIBLE);
            holder.eyeIcon.setVisibility(View.GONE);
            holder.deleteIcon.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onEbookClick(ebook));
        holder.eyeIcon.setOnClickListener(v -> listener.onEbookOpen(ebook));
        holder.deleteIcon.setOnClickListener(v -> listener.onEbookDelete(ebook));
    }

    @Override
    public int getItemCount() {
        return ebooks.size();
    }
    
    private boolean isPdfCached(android.content.Context context, String url) {
        String fileName = generateFileName(url) + ".pdf";
        File cacheDir = new File(context.getFilesDir(), "pdf_cache");
        File cachedFile = new File(cacheDir, fileName);
        return cachedFile.exists();
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
    
    public boolean deleteCachedPdf(android.content.Context context, String url) {
        String fileName = generateFileName(url) + ".pdf";
        File cacheDir = new File(context.getFilesDir(), "pdf_cache");
        File cachedFile = new File(cacheDir, fileName);
        if (cachedFile.exists()) {
            return cachedFile.delete();
        }
        return false;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, downloadIcon, eyeIcon, deleteIcon;
        TextView titleText, authorText, descriptionText;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ebook_image);
            titleText = itemView.findViewById(R.id.ebook_title);
            authorText = itemView.findViewById(R.id.ebook_author);
            descriptionText = itemView.findViewById(R.id.ebook_description);
            downloadIcon = itemView.findViewById(R.id.download_icon);
            eyeIcon = itemView.findViewById(R.id.eye_icon);
            deleteIcon = itemView.findViewById(R.id.delete_icon);
        }
    }
}