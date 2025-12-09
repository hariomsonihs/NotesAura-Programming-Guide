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
import com.hariomsonihs.notesaura.models.InterviewQuestion;

public class InterviewQuestionAdapter extends RecyclerView.Adapter<InterviewQuestionAdapter.ViewHolder> {
    private List<InterviewQuestion> questions;
    private OnQuestionClickListener clickListener;

    public interface OnQuestionClickListener {
        void onQuestionClick(InterviewQuestion question);
    }

    public InterviewQuestionAdapter(List<InterviewQuestion> questions, OnQuestionClickListener clickListener) {
        this.questions = questions;
        this.clickListener = clickListener;
    }
    
    public void setQuestions(List<InterviewQuestion> questions) {
        this.questions = questions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_interview_question, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InterviewQuestion question = questions.get(position);
        holder.questionTitle.setText(question.getTitle());
        
        // Set question number
        holder.questionNumber.setText(String.valueOf(position + 1));
        
        // Set difficulty badge
        String difficulty = getDifficulty(position);
        holder.difficultyBadge.setText(difficulty);
        setDifficultyBackground(holder.difficultyBadge, difficulty);
        
        holder.cardView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onQuestionClick(question);
            }
        });
    }
    
    private String getDifficulty(int position) {
        // Simple logic to assign difficulty based on position
        if (position % 3 == 0) return "Easy";
        if (position % 3 == 1) return "Medium";
        return "Hard";
    }
    
    private void setDifficultyBackground(TextView badge, String difficulty) {
        switch (difficulty) {
            case "Easy":
                badge.setBackgroundResource(R.drawable.bg_difficulty_easy);
                break;
            case "Medium":
                badge.setBackgroundResource(R.drawable.bg_difficulty_medium);
                break;
            case "Hard":
                badge.setBackgroundResource(R.drawable.bg_difficulty_hard);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView questionTitle, questionNumber, difficultyBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.question_card);
            questionTitle = itemView.findViewById(R.id.question_title);
            questionNumber = itemView.findViewById(R.id.question_number);
            difficultyBadge = itemView.findViewById(R.id.difficulty_badge);
        }
    }
}