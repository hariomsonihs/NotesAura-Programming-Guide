package com.hariomsonihs.notesaura.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.adapters.InterviewCategoryAdapter;
import com.hariomsonihs.notesaura.models.InterviewCategory;
import com.hariomsonihs.notesaura.utils.InterviewDataManager;

public class InterviewFragment extends Fragment implements InterviewDataManager.DataUpdateListener {
    private RecyclerView categoriesRecyclerView;
    private InterviewCategoryAdapter categoryAdapter;
    private List<InterviewCategory> categories;
    private InterviewDataManager dataManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_interview, container, false);
        
        initViews(view);
        setupRecyclerView();
        dataManager = InterviewDataManager.getInstance();
        dataManager.addDataUpdateListener(this);
        loadData();
        
        return view;
    }

    private void initViews(View view) {
        categoriesRecyclerView = view.findViewById(R.id.interview_categories_recycler_view);
    }

    private void setupRecyclerView() {
        categoriesRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        categories = new ArrayList<>();
        categoryAdapter = new InterviewCategoryAdapter(categories, category -> {
            InterviewQuestionsFragment fragment = InterviewQuestionsFragment.newInstance(category.getId(), category.getName());
            getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
        });
        categoriesRecyclerView.setAdapter(categoryAdapter);
    }

    private void loadData() {
        categories.clear();
        categories.addAll(InterviewDataManager.getInstance().getAllCategories());
        categoryAdapter.notifyDataSetChanged();
    }

    @Override
    public void onInterviewCategoriesUpdated() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(this::loadData);
        }
    }

    @Override
    public void onInterviewQuestionsUpdated() {
        // Not needed here
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dataManager != null) {
            dataManager.removeDataUpdateListener(this);
        }
    }
}