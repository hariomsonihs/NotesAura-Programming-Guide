package com.hariomsonihs.notesaura.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.adapters.InterviewQuestionAdapter;
import com.hariomsonihs.notesaura.utils.InterviewDataManager;
import com.hariomsonihs.notesaura.utils.EmptyStateHelper;
import android.widget.LinearLayout;
import android.view.View;
import java.util.List;
import com.hariomsonihs.notesaura.models.InterviewQuestion;

public class InterviewQuestionsActivity extends AppCompatActivity implements InterviewDataManager.DataUpdateListener {
    private RecyclerView questionsRecyclerView;
    private InterviewQuestionAdapter adapter;
    private InterviewDataManager dataManager;
    private String categoryId, categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interview_questions);
        
        categoryId = getIntent().getStringExtra("category_id");
        categoryName = getIntent().getStringExtra("category_name");
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(categoryName + " Questions");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        questionsRecyclerView = findViewById(R.id.interview_questions_recycler_view);
        questionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        dataManager = InterviewDataManager.getInstance();
        
        List<InterviewQuestion> questions = dataManager.getQuestionsByCategory(categoryId);
        
        if (questions.isEmpty()) {
            showEmptyState();
        } else {
            adapter = new InterviewQuestionAdapter(questions, question -> {
                if (question.getWebLink() != null && !question.getWebLink().isEmpty()) {
                    Intent intent = new Intent(this, InterviewContentActivity.class);
                    intent.putExtra("question_id", question.getId());
                    intent.putExtra("question_title", question.getTitle());
                    startActivity(intent);
                }
            });
            questionsRecyclerView.setAdapter(adapter);
        }
        
        dataManager.addDataUpdateListener(this);
    }

    @Override
    public void onInterviewQuestionsUpdated() {
        runOnUiThread(() -> {
            List<InterviewQuestion> questions = dataManager.getQuestionsByCategory(categoryId);
            
            if (questions.isEmpty()) {
                showEmptyState();
            } else {
                hideEmptyState();
                if (adapter != null) {
                    adapter.setQuestions(questions);
                } else {
                    adapter = new InterviewQuestionAdapter(questions, question -> {
                        if (question.getWebLink() != null && !question.getWebLink().isEmpty()) {
                            Intent intent = new Intent(this, InterviewContentActivity.class);
                            intent.putExtra("question_id", question.getId());
                            intent.putExtra("question_title", question.getTitle());
                            startActivity(intent);
                        }
                    });
                    questionsRecyclerView.setAdapter(adapter);
                }
            }
        });
    }
    
    private void showEmptyState() {
        questionsRecyclerView.setVisibility(View.GONE);
        LinearLayout container = findViewById(R.id.content_container);
        if (container != null) {
            EmptyStateHelper.showEmptyState(
                container,
                "We are working on it",
                "Questions for this category will be available soon"
            );
        }
    }
    
    private void hideEmptyState() {
        questionsRecyclerView.setVisibility(View.VISIBLE);
        LinearLayout container = findViewById(R.id.content_container);
        if (container != null && container.getChildCount() > 1) {
            container.removeViewAt(1);
        }
    }

    @Override
    public void onInterviewCategoriesUpdated() {}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dataManager != null) {
            dataManager.removeDataUpdateListener(this);
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}