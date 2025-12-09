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

public class AdminQuizCategoryAdapter extends RecyclerView.Adapter<AdminQuizCategoryAdapter.ViewHolder> {
    private List<QuizCategory> categories;
    private OnCategoryEditListener editListener;
    private OnCategoryDeleteListener deleteListener;
    private OnCategorySelectListener selectListener;

    public interface OnCategoryEditListener {
        void onEdit(QuizCategory category);
    }

    public interface OnCategoryDeleteListener {
        void onDelete(QuizCategory category);
    }

    public interface OnCategorySelectListener {
        void onSelect(QuizCategory category);
    }

    public AdminQuizCategoryAdapter(List<QuizCategory> categories, OnCategoryEditListener editListener,
                                  OnCategoryDeleteListener deleteListener, OnCategorySelectListener selectListener) {
        this.categories = categories;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
        this.selectListener = selectListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_quiz_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuizCategory category = categories.get(position);
        holder.nameText.setText(category.getName());
        
        holder.itemView.setOnClickListener(v -> selectListener.onSelect(category));
        holder.editBtn.setOnClickListener(v -> editListener.onEdit(category));
        holder.deleteBtn.setOnClickListener(v -> deleteListener.onDelete(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, editBtn, deleteBtn;

        ViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.category_name);
            editBtn = itemView.findViewById(R.id.edit_btn);
            deleteBtn = itemView.findViewById(R.id.delete_btn);
        }
    }
}