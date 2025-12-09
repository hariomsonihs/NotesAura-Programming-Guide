package com.hariomsonihs.notesaura.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.github.chrisbanes.photoview.PhotoView;
import com.hariomsonihs.notesaura.R;
import java.util.List;

public class PdfPageAdapter extends RecyclerView.Adapter<PdfPageAdapter.PageViewHolder> {
    private List<Bitmap> pages;
    private int rotation = 0;

    public PdfPageAdapter(List<Bitmap> pages, int rotation) {
        this.pages = pages;
        this.rotation = rotation;
    }
    
    public void setRotation(int rotation) {
        this.rotation = rotation;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pdf_page, parent, false);
        return new PageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        Bitmap page = pages.get(position);
        holder.bind(page, rotation);
        holder.pageNumberText.setText("Page " + (position + 1));
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    static class PageViewHolder extends RecyclerView.ViewHolder {
        PhotoView pageImageView;
        TextView pageNumberText;

        PageViewHolder(@NonNull View itemView) {
            super(itemView);
            pageImageView = itemView.findViewById(R.id.pageImageView);
            pageNumberText = itemView.findViewById(R.id.pageNumberText);
            
            // Configure zoom settings for proper cycling
            pageImageView.setMinimumScale(1.0f);  // Normal size
            pageImageView.setMediumScale(2.0f);   // Medium zoom
            pageImageView.setMaximumScale(4.0f);  // Maximum zoom
            
            // Allow parent to intercept touch events when at normal zoom
            pageImageView.setAllowParentInterceptOnEdge(true);
            pageImageView.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
            
            // Enable smooth scrolling when not zoomed
            pageImageView.setOnMatrixChangeListener(rect -> {
                float currentScale = pageImageView.getScale();
                // Allow RecyclerView to scroll when at minimum scale
                if (itemView.getParent() != null) {
                    itemView.getParent().requestDisallowInterceptTouchEvent(currentScale > 1.0f);
                }
            });
        }
        
        void bind(Bitmap page, int rotation) {
            pageImageView.setImageBitmap(page);
            pageImageView.setRotation(rotation);
        }
        

    }
}