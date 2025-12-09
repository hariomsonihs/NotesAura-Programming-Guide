package com.hariomsonihs.notesaura.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.models.PracticeCategory;

public class AdminPracticeCategoryManageAdapter extends RecyclerView.Adapter<AdminPracticeCategoryManageAdapter.ViewHolder> {
    private List<PracticeCategory> categories;
    private OnCategoryClickListener clickListener;

    public interface OnCategoryClickListener {
        void onCategoryClick(PracticeCategory category);
        void onEditCategory(PracticeCategory category);
        void onDeleteCategory(PracticeCategory category);
    }

    public AdminPracticeCategoryManageAdapter(List<PracticeCategory> categories, OnCategoryClickListener clickListener) {
        this.categories = categories;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_practice_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PracticeCategory category = categories.get(position);
        holder.name.setText(category.getName());
        holder.itemView.setOnClickListener(v -> clickListener.onCategoryClick(category));
        holder.itemView.setOnLongClickListener(v -> {
            showOptionsDialog(v, category);
            return true;
        });
    }
    
    private void showOptionsDialog(View view, PracticeCategory category) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(view.getContext());
        builder.setTitle(category.getName());
        builder.setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
            if (which == 0) {
                clickListener.onEditCategory(category);
            } else {
                clickListener.onDeleteCategory(category);
            }
        });
        builder.show();
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.practice_category_name);
        }
    }
}