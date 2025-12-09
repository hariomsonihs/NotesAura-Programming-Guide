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
import com.hariomsonihs.notesaura.models.EbookCategory;
import java.util.List;

public class EbookCategoryAdapter extends RecyclerView.Adapter<EbookCategoryAdapter.ViewHolder> {
    private List<EbookCategory> categories;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(EbookCategory category);
    }

    public EbookCategoryAdapter(List<EbookCategory> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ebook_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EbookCategory category = categories.get(position);
        holder.nameText.setText(category.getName());
        holder.descriptionText.setText(category.getDescription());

        if (category.getImageUrl() != null && !category.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(category.getImageUrl())
                    .centerCrop()
                    .into(holder.imageView);
        }

        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameText, descriptionText;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.category_image);
            nameText = itemView.findViewById(R.id.category_name);
            descriptionText = itemView.findViewById(R.id.category_description);
        }
    }
}