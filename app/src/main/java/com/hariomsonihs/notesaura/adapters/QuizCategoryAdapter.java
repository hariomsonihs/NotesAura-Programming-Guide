package com.hariomsonihs.notesaura.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.models.QuizCategory;

public class QuizCategoryAdapter extends RecyclerView.Adapter<QuizCategoryAdapter.ViewHolder> {
    private List<QuizCategory> categories;
    private OnCategoryClickListener clickListener;

    public interface OnCategoryClickListener {
        void onCategoryClick(QuizCategory category);
    }

    public QuizCategoryAdapter(List<QuizCategory> categories, OnCategoryClickListener clickListener) {
        this.categories = categories;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuizCategory category = categories.get(position);
        holder.name.setText(category.getName());
        holder.itemView.setOnClickListener(v -> clickListener.onCategoryClick(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.category_name);
        }
    }
}