package com.hariomsonihs.notesaura.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hariomsonihs.notesaura.R;
import java.util.List;

public class PageIndicatorAdapter extends RecyclerView.Adapter<PageIndicatorAdapter.IndicatorViewHolder> {
    private List<Bitmap> pages;
    private OnPageClickListener listener;

    public interface OnPageClickListener {
        void onPageClick(int position);
    }

    public PageIndicatorAdapter(List<Bitmap> pages, OnPageClickListener listener) {
        this.pages = pages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public IndicatorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_page_indicator, parent, false);
        return new IndicatorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IndicatorViewHolder holder, int position) {
        Bitmap page = pages.get(position);
        holder.pageThumbnail.setImageBitmap(page);
        holder.pageNumber.setText(String.valueOf(position + 1));
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPageClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    static class IndicatorViewHolder extends RecyclerView.ViewHolder {
        ImageView pageThumbnail;
        TextView pageNumber;

        IndicatorViewHolder(@NonNull View itemView) {
            super(itemView);
            pageThumbnail = itemView.findViewById(R.id.pageThumbnail);
            pageNumber = itemView.findViewById(R.id.pageNumber);
        }
    }
}