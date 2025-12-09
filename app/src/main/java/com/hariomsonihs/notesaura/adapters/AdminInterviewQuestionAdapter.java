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
import com.hariomsonihs.notesaura.models.InterviewQuestion;

public class AdminInterviewQuestionAdapter extends RecyclerView.Adapter<AdminInterviewQuestionAdapter.ViewHolder> {
    private List<InterviewQuestion> questions;
    private OnQuestionActionListener editListener;
    private OnQuestionActionListener deleteListener;

    public interface OnQuestionActionListener {
        void onQuestionAction(InterviewQuestion question);
    }

    public AdminInterviewQuestionAdapter(List<InterviewQuestion> questions, 
                                       OnQuestionActionListener editListener,
                                       OnQuestionActionListener deleteListener) {
        this.questions = questions;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_interview_question, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InterviewQuestion question = questions.get(position);
        holder.questionTitle.setText(question.getTitle());
        holder.questionDescription.setText(question.getDescription());
        holder.questionLink.setText(question.getWebLink());
        
        holder.editBtn.setOnClickListener(v -> editListener.onQuestionAction(question));
        holder.deleteBtn.setOnClickListener(v -> deleteListener.onQuestionAction(question));
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView questionTitle, questionDescription, questionLink;
        ImageView editBtn, deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            questionTitle = itemView.findViewById(R.id.question_title);
            questionDescription = itemView.findViewById(R.id.question_description);
            questionLink = itemView.findViewById(R.id.question_link);
            editBtn = itemView.findViewById(R.id.edit_btn);
            deleteBtn = itemView.findViewById(R.id.delete_btn);
        }
    }
}