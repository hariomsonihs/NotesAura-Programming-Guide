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

public class AdminQuizSubcategoryAdapter extends RecyclerView.Adapter<AdminQuizSubcategoryAdapter.ViewHolder> {
    private List<QuizSubcategory> subcategories;
    private OnSubcategoryEditListener editListener;
    private OnSubcategoryDeleteListener deleteListener;

    public interface OnSubcategoryEditListener {
        void onEdit(QuizSubcategory subcategory);
    }

    public interface OnSubcategoryDeleteListener {
        void onDelete(QuizSubcategory subcategory);
    }

    public AdminQuizSubcategoryAdapter(List<QuizSubcategory> subcategories, OnSubcategoryEditListener editListener,
                                     OnSubcategoryDeleteListener deleteListener) {
        this.subcategories = subcategories;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_quiz_subcategory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuizSubcategory subcategory = subcategories.get(position);
        holder.nameText.setText(subcategory.getName());
        holder.urlText.setText(subcategory.getWebUrl());
        
        holder.editBtn.setOnClickListener(v -> editListener.onEdit(subcategory));
        holder.deleteBtn.setOnClickListener(v -> deleteListener.onDelete(subcategory));
    }

    @Override
    public int getItemCount() {
        return subcategories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, urlText, editBtn, deleteBtn;

        ViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.subcategory_name);
            urlText = itemView.findViewById(R.id.subcategory_url);
            editBtn = itemView.findViewById(R.id.edit_btn);
            deleteBtn = itemView.findViewById(R.id.delete_btn);
        }
    }
}