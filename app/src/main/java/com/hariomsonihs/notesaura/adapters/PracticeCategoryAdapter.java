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
import com.hariomsonihs.notesaura.models.PracticeCategory;

public class PracticeCategoryAdapter extends RecyclerView.Adapter<PracticeCategoryAdapter.ViewHolder> {
    public interface OnCategoryClickListener {
        void onCategoryClick(PracticeCategory category);
    }
    private List<PracticeCategory> categories;
    private OnCategoryClickListener listener;
    public PracticeCategoryAdapter(List<PracticeCategory> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }
    public void setCategories(List<PracticeCategory> categories) {
        this.categories = categories;
        notifyDataSetChanged();
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
        holder.bind(category, listener);
    }
    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name, emoji, description, exerciseCount;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.practice_category_name);
            emoji = itemView.findViewById(R.id.category_emoji);
            description = itemView.findViewById(R.id.category_description);
            exerciseCount = itemView.findViewById(R.id.exercise_count);
        }
        public void bind(PracticeCategory category, OnCategoryClickListener listener) {
            name.setText(category.getName());
            description.setText("Practice coding exercises");
            
            // Get actual exercise count from data manager
            com.hariomsonihs.notesaura.utils.PracticeDataManager dataManager = 
                com.hariomsonihs.notesaura.utils.PracticeDataManager.getInstance();
            int totalExercises = dataManager.getTotalExercisesByCategory(category.getId());
            exerciseCount.setText(totalExercises + " Exercises");
            
            // Set emoji based on category name
            String categoryEmoji = getCategoryEmoji(category.getName());
            emoji.setText(categoryEmoji);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onCategoryClick(category);
            });
        }
        
        private String getCategoryEmoji(String categoryName) {
            String name = categoryName.toLowerCase();
            if (name.contains("java") || name.contains("programming")) return "💻";
            if (name.contains("android") || name.contains("mobile")) return "📱";
            if (name.contains("web") || name.contains("html") || name.contains("css")) return "🌐";
            if (name.contains("database") || name.contains("sql")) return "🗄️";
            if (name.contains("data") || name.contains("science")) return "📊";
            if (name.contains("algorithm") || name.contains("dsa")) return "🧠";
            return "📝";
        }
    }
}
