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
import com.hariomsonihs.notesaura.models.InterviewCategory;

public class AdminInterviewCategoryAdapter extends RecyclerView.Adapter<AdminInterviewCategoryAdapter.ViewHolder> {
    private List<InterviewCategory> categories;
    private OnCategoryActionListener listener;

    public interface OnCategoryActionListener {
        void onCategoryClick(InterviewCategory category);
        void onEditCategory(InterviewCategory category);
        void onDeleteCategory(InterviewCategory category);
    }

    public AdminInterviewCategoryAdapter(List<InterviewCategory> categories, OnCategoryActionListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    public AdminInterviewCategoryAdapter(List<InterviewCategory> categories, 
                                       OnCategoryActionListener clickListener,
                                       OnCategoryActionListener editListener,
                                       OnCategoryActionListener deleteListener) {
        this.categories = categories;
        this.listener = new OnCategoryActionListener() {
            @Override
            public void onCategoryClick(InterviewCategory category) {
                clickListener.onCategoryClick(category);
            }
            
            @Override
            public void onEditCategory(InterviewCategory category) {
                editListener.onEditCategory(category);
            }
            
            @Override
            public void onDeleteCategory(InterviewCategory category) {
                deleteListener.onDeleteCategory(category);
            }
        };
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_interview_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InterviewCategory category = categories.get(position);
        holder.categoryName.setText(category.getName());
        holder.categoryDescription.setText(category.getDescription());
        
        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category));
        holder.editBtn.setOnClickListener(v -> listener.onEditCategory(category));
        holder.deleteBtn.setOnClickListener(v -> listener.onDeleteCategory(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName, categoryDescription;
        ImageView editBtn, deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.category_name);
            categoryDescription = itemView.findViewById(R.id.category_description);
            editBtn = itemView.findViewById(R.id.edit_btn);
            deleteBtn = itemView.findViewById(R.id.delete_btn);
        }
    }
}