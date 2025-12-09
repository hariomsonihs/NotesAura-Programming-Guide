package com.hariomsonihs.notesaura.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.models.InterviewCategory;

public class InterviewCategoryAdapter extends RecyclerView.Adapter<InterviewCategoryAdapter.ViewHolder> {
    private List<InterviewCategory> categories;
    private OnCategoryClickListener clickListener;

    public interface OnCategoryClickListener {
        void onCategoryClick(InterviewCategory category);
    }

    public InterviewCategoryAdapter(List<InterviewCategory> categories, OnCategoryClickListener clickListener) {
        this.categories = categories;
        this.clickListener = clickListener;
    }
    
    public void setCategories(List<InterviewCategory> categories) {
        // Don't update with empty list if we already have data
        if (categories.isEmpty() && !this.categories.isEmpty()) {
            return;
        }
        this.categories = categories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_interview_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InterviewCategory category = categories.get(position);
        holder.categoryName.setText(category.getName());
        holder.categoryDescription.setText(category.getDescription());
        
        // Set emoji based on category name
        String emoji = getCategoryEmoji(category.getName());
        holder.categoryEmoji.setText(emoji);
        
        // Set question count
        holder.questionCount.setText("25+ Questions");
        
        holder.cardView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onCategoryClick(category);
            }
        });
    }
    
    private String getCategoryEmoji(String categoryName) {
        String name = categoryName.toLowerCase();
        if (name.contains("java") || name.contains("programming")) return "💻";
        if (name.contains("android") || name.contains("mobile")) return "📱";
        if (name.contains("web") || name.contains("html") || name.contains("css")) return "🌐";
        if (name.contains("database") || name.contains("sql")) return "🗄";
        if (name.contains("data") || name.contains("science")) return "📊";
        if (name.contains("network") || name.contains("system")) return "🌐";
        if (name.contains("algorithm") || name.contains("dsa")) return "🧠";
        return "📝";
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView categoryName, categoryDescription, categoryEmoji, questionCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.category_card);
            categoryName = itemView.findViewById(R.id.category_name);
            categoryDescription = itemView.findViewById(R.id.category_description);
            categoryEmoji = itemView.findViewById(R.id.category_emoji);
            questionCount = itemView.findViewById(R.id.question_count);
        }
    }
}