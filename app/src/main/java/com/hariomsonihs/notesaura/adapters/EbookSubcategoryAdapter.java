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
import com.hariomsonihs.notesaura.models.EbookSubcategory;
import java.util.List;

public class EbookSubcategoryAdapter extends RecyclerView.Adapter<EbookSubcategoryAdapter.ViewHolder> {
    private List<EbookSubcategory> subcategories;
    private OnSubcategoryClickListener listener;

    public interface OnSubcategoryClickListener {
        void onSubcategoryClick(EbookSubcategory subcategory);
    }

    public EbookSubcategoryAdapter(List<EbookSubcategory> subcategories, OnSubcategoryClickListener listener) {
        this.subcategories = subcategories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ebook_subcategory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EbookSubcategory subcategory = subcategories.get(position);
        holder.nameText.setText(subcategory.getName());
        holder.descriptionText.setText(subcategory.getDescription());

        if (subcategory.getImageUrl() != null && !subcategory.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(subcategory.getImageUrl())
                    .centerCrop()
                    .into(holder.imageView);
        }

        holder.itemView.setOnClickListener(v -> listener.onSubcategoryClick(subcategory));
    }

    @Override
    public int getItemCount() {
        return subcategories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameText, descriptionText;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.subcategory_image);
            nameText = itemView.findViewById(R.id.subcategory_name);
            descriptionText = itemView.findViewById(R.id.subcategory_description);
        }
    }
}