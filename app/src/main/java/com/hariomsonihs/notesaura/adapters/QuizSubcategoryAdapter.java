package com.hariomsonihs.notesaura.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.models.QuizSubcategory;

public class QuizSubcategoryAdapter extends RecyclerView.Adapter<QuizSubcategoryAdapter.ViewHolder> {
    private List<QuizSubcategory> subcategories;
    private OnSubcategoryClickListener clickListener;

    public interface OnSubcategoryClickListener {
        void onSubcategoryClick(QuizSubcategory subcategory);
    }

    public QuizSubcategoryAdapter(List<QuizSubcategory> subcategories, OnSubcategoryClickListener clickListener) {
        this.subcategories = subcategories;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_subcategory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuizSubcategory subcategory = subcategories.get(position);
        holder.name.setText(subcategory.getName());
        holder.itemView.setOnClickListener(v -> clickListener.onSubcategoryClick(subcategory));
    }

    @Override
    public int getItemCount() {
        return subcategories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.subcategory_name);
        }
    }
}