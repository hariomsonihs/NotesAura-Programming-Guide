package com.hariomsonihs.notesaura.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.models.PracticeList;
import com.hariomsonihs.notesaura.models.PracticeCategory;
import com.hariomsonihs.notesaura.utils.PracticeDataManager;

public class AdminPracticeListManageAdapter extends RecyclerView.Adapter<AdminPracticeListManageAdapter.ViewHolder> {
    private List<PracticeList> practiceLists;
    private OnEditListener editListener;
    private OnDeleteListener deleteListener;
    private OnOpenExercisesListener openExercisesListener;
    private PracticeDataManager dataManager;

    public interface OnEditListener { void onEdit(PracticeList list); }
    public interface OnDeleteListener { void onDelete(PracticeList list); }
    public interface OnOpenExercisesListener { void onOpen(PracticeList list); }

    public AdminPracticeListManageAdapter(List<PracticeList> practiceLists, OnEditListener editListener, OnDeleteListener deleteListener, OnOpenExercisesListener openExercisesListener) {
        this.practiceLists = practiceLists;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
        this.openExercisesListener = openExercisesListener;
        this.dataManager = PracticeDataManager.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_practice_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PracticeList list = practiceLists.get(position);
        holder.id.setText("ID: " + list.getId());
        holder.name.setText(list.getName());
        holder.desc.setText(list.getDescription());
        
        // Get category name
        String categoryName = "Unknown";
        List<PracticeCategory> categories = dataManager.getAllCategories();
        for (PracticeCategory cat : categories) {
            if (cat.getId().equals(list.getCategoryId())) {
                categoryName = cat.getName();
                break;
            }
        }
        holder.category.setText("Category: " + categoryName);
        
        holder.manageExercises.setOnClickListener(v -> openExercisesListener.onOpen(list));
        holder.edit.setOnClickListener(v -> editListener.onEdit(list));
        holder.delete.setOnClickListener(v -> deleteListener.onDelete(list));
    }

    @Override
    public int getItemCount() { return practiceLists.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView id, name, desc, category, edit, delete;
        Button manageExercises;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.practice_list_id);
            name = itemView.findViewById(R.id.practice_list_name);
            desc = itemView.findViewById(R.id.practice_list_desc);
            category = itemView.findViewById(R.id.practice_list_category);
            edit = itemView.findViewById(R.id.edit_btn);
            delete = itemView.findViewById(R.id.delete_btn);
            manageExercises = itemView.findViewById(R.id.manage_exercises_btn);
        }
    }
}
