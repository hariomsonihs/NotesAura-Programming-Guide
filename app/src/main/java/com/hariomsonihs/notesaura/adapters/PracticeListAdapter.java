package com.hariomsonihs.notesaura.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.models.PracticeList;

public class PracticeListAdapter extends RecyclerView.Adapter<PracticeListAdapter.ViewHolder> {
    public interface OnPracticeListClickListener { void onClick(PracticeList list); }
    private List<PracticeList> practiceLists;
    private OnPracticeListClickListener listener;
    public PracticeListAdapter(List<PracticeList> practiceLists, OnPracticeListClickListener listener) {
        this.practiceLists = practiceLists;
        this.listener = listener;
    }
    public void setPracticeLists(List<PracticeList> lists) {
        this.practiceLists = lists;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_practice_list, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PracticeList list = practiceLists.get(position);
        holder.name.setText(list.getName());
        holder.desc.setText(list.getDescription());
        holder.listNumber.setText(String.valueOf(position + 1));
        
        // Get actual exercise count
        com.hariomsonihs.notesaura.utils.PracticeDataManager dataManager = 
            com.hariomsonihs.notesaura.utils.PracticeDataManager.getInstance();
        int exerciseCount = dataManager.getExercisesByCategory(list.getId()).size();
        holder.exerciseCountBadge.setText(exerciseCount + " Ex");
        
        holder.itemView.setOnClickListener(v -> listener.onClick(list));
    }
    @Override
    public int getItemCount() { return practiceLists.size(); }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, desc, listNumber, exerciseCountBadge;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.practice_list_name);
            desc = itemView.findViewById(R.id.practice_list_desc);
            listNumber = itemView.findViewById(R.id.list_number);
            exerciseCountBadge = itemView.findViewById(R.id.exercise_count_badge);
        }
    }
}
