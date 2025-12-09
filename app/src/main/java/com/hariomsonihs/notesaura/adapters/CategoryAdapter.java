package com.hariomsonihs.notesaura.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Color;
import com.hariomsonihs.notesaura.R;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.hariomsonihs.notesaura.models.Category;
import com.bumptech.glide.Glide;
import android.text.TextUtils;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<Category> categories;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category, listener);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private ImageView categoryIcon;
        private TextView categoryName, categoryDescription, categoryCount;
        private View categoryBackground;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryIcon = itemView.findViewById(R.id.category_icon);
            categoryName = itemView.findViewById(R.id.category_name);
            categoryDescription = itemView.findViewById(R.id.category_description);
            categoryCount = itemView.findViewById(R.id.category_count);
            categoryBackground = itemView.findViewById(R.id.category_background);
        }

        public void bind(Category category, OnCategoryClickListener listener) {
            categoryName.setText(category.getName());
            categoryDescription.setText(category.getDescription());
            categoryCount.setText(category.getCourseCount() + " courses");

            // Dynamic icon based on category name/keywords
            String name = category.getName() != null ? category.getName().toLowerCase() : "";
            int iconRes;
            if (name.contains("java") || name.contains("python") || name.contains("programming") || name.contains("code")) {
                iconRes = R.drawable.ic_code;
            } else if (name.contains("web") || name.contains("html") || name.contains("css") || name.contains("javascript")) {
                iconRes = R.drawable.ic_web;
            } else if (name.contains("android") || name.contains("app")) {
                iconRes = R.drawable.ic_phone;
            } else if (name.contains("data") || name.contains("machine") || name.contains("ai") || name.contains("science")) {
                iconRes = R.drawable.ic_analytics;
            } else if (name.contains("c++") || name.contains("cpp") || name.contains("c ")) {
                iconRes = R.drawable.ic_note;
            } else if (name.contains("cheat") || name.contains("sheet")) {
                iconRes = R.drawable.ic_note;
            } else {
                iconRes = R.drawable.ic_categories;
            }
            // Set category image or default icon
            setCategoryImage(category, iconRes);

            // Dynamic background based on category name/keywords
            int backgroundRes;
            if (name.contains("java")) {
                backgroundRes = R.drawable.bg_gradient_java;
            } else if (name.contains("python")) {
                backgroundRes = R.drawable.bg_gradient_python;
            } else if (name.contains("web") || name.contains("html") || name.contains("css") || name.contains("javascript")) {
                backgroundRes = R.drawable.bg_gradient_web;
            } else if (name.contains("android") || name.contains("app")) {
                backgroundRes = R.drawable.bg_gradient_android;
            } else if (name.contains("data") || name.contains("machine") || name.contains("ai") || name.contains("science")) {
                backgroundRes = R.drawable.bg_gradient_data;
            } else if (name.contains("c++") || name.contains("cpp") || name.contains("c ")) {
                backgroundRes = R.drawable.bg_gradient_cpp;
            } else if (name.contains("cheat") || name.contains("sheet")) {
                backgroundRes = R.drawable.bg_gradient_secondary;
            } else {
                backgroundRes = R.drawable.bg_gradient_primary;
            }
            categoryBackground.setBackgroundResource(backgroundRes);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(category);
                }
            });
        }
        
        private void setCategoryImage(Category category, int defaultIcon) {
            String imageUrl = category.getIconUrl();
            
            if (!TextUtils.isEmpty(imageUrl)) {
                categoryIcon.setImageTintList(null);
                Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .centerCrop()
                    .into(categoryIcon);
                categoryBackground.setAlpha(0.3f);
            } else {
                categoryIcon.setImageDrawable(null);
                categoryBackground.setAlpha(1.0f);
            }
        }
    }
}