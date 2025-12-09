package com.hariomsonihs.notesaura.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.adapters.InterviewQuestionAdapter;
import com.hariomsonihs.notesaura.models.InterviewQuestion;
import com.hariomsonihs.notesaura.utils.InterviewDataManager;

public class InterviewQuestionsFragment extends Fragment implements InterviewDataManager.DataUpdateListener {
    private static final String ARG_CATEGORY_ID = "category_id";
    private static final String ARG_CATEGORY_NAME = "category_name";
    
    private RecyclerView questionsRecyclerView;
    private TextView categoryTitle;
    private InterviewQuestionAdapter questionAdapter;
    private List<InterviewQuestion> questions;
    private InterviewDataManager dataManager;
    private String categoryId;

    public static InterviewQuestionsFragment newInstance(String categoryId, String categoryName) {
        InterviewQuestionsFragment fragment = new InterviewQuestionsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_ID, categoryId);
        args.putString(ARG_CATEGORY_NAME, categoryName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_interview_questions, container, false);
        
        if (getArguments() != null) {
            categoryId = getArguments().getString(ARG_CATEGORY_ID);
            String categoryName = getArguments().getString(ARG_CATEGORY_NAME);
        }
        
        initViews(view);
        setupRecyclerView();
        loadData();
        
        dataManager = InterviewDataManager.getInstance();
        dataManager.addDataUpdateListener(this);
        
        return view;
    }

    private void initViews(View view) {
        questionsRecyclerView = view.findViewById(R.id.questions_recycler_view);
        categoryTitle = view.findViewById(R.id.category_title);
        
        if (getArguments() != null) {
            categoryTitle.setText(getArguments().getString(ARG_CATEGORY_NAME) + " Interview Questions");
        }
    }

    private void setupRecyclerView() {
        questionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        questions = new ArrayList<>();
        questionAdapter = new InterviewQuestionAdapter(questions, question -> {
            String url = question.getWebLink();
            
            if (isPdfLink(url)) {
                // For now, open PDF in external browser (cleanest solution)
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            } else {
                // Open web page in WebView
                Intent webIntent = new Intent(getContext(), com.hariomsonihs.notesaura.activities.WebViewActivity.class);
                webIntent.putExtra("web_url", url);
                webIntent.putExtra("web_title", question.getTitle());
                startActivity(webIntent);
            }
        });
        questionsRecyclerView.setAdapter(questionAdapter);
    }

    private void loadData() {
        questions.clear();
        questions.addAll(InterviewDataManager.getInstance().getQuestionsByCategory(categoryId));
        questionAdapter.notifyDataSetChanged();
    }

    @Override
    public void onInterviewCategoriesUpdated() {
        // Not needed here
    }

    @Override
    public void onInterviewQuestionsUpdated() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(this::loadData);
        }
    }

    private boolean isPdfLink(String url) {
        return url.toLowerCase().contains(".pdf") || 
               url.contains("drive.google.com") ||
               url.contains("docs.google.com") ||
               url.toLowerCase().contains("pdf");
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dataManager != null) {
            dataManager.removeDataUpdateListener(this);
        }
    }
}