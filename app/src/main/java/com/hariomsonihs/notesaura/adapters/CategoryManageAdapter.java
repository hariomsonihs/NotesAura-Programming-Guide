package com.hariomsonihs.notesaura.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.activities.AdminManageCategoriesActivity;

public class CategoryManageAdapter extends RecyclerView.Adapter<CategoryManageAdapter.CategoryViewHolder> {
    private List<AdminManageCategoriesActivity.CategoryItem> categories;
    private OnCategoryActionListener listener;

    public interface OnCategoryActionListener {
        void onDeleteCategory(AdminManageCategoriesActivity.CategoryItem category);
    }

    public CategoryManageAdapter(List<AdminManageCategoriesActivity.CategoryItem> categories, OnCategoryActionListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_manage, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        AdminManageCategoriesActivity.CategoryItem category = categories.get(position);
        holder.bind(category, listener);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private TextView categoryName, categoryId;
        private ImageView deleteBtn;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.category_name);
            categoryId = itemView.findViewById(R.id.category_id);
            deleteBtn = itemView.findViewById(R.id.delete_btn);
        }

        public void bind(AdminManageCategoriesActivity.CategoryItem category, OnCategoryActionListener listener) {
            categoryName.setText(category.name);
            categoryId.setText("ID: " + category.id);
            
            deleteBtn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteCategory(category);
                }
            });
        }
    }
}