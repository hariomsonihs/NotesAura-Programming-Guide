package com.hariomsonihs.notesaura.fragments;

import android.content.Intent;
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
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.activities.PracticeListsActivity;
import com.hariomsonihs.notesaura.adapters.PracticeCategoryAdapter;
import com.hariomsonihs.notesaura.models.PracticeCategory;
import com.hariomsonihs.notesaura.utils.PracticeDataManager;

public class PracticeFragment extends Fragment implements PracticeDataManager.DataUpdateListener {
    private RecyclerView categoriesRecyclerView;
    private PracticeCategoryAdapter adapter;
    private PracticeDataManager dataManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_practice, container, false);
            
            categoriesRecyclerView = view.findViewById(R.id.practice_categories_recycler_view);
            categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            
            dataManager = PracticeDataManager.getInstance();
            // Force refresh data from Firebase
            loadPracticeCategories();
            
            adapter = new PracticeCategoryAdapter(dataManager.getAllCategories(), category -> {
                if (category != null && getContext() != null) {
                    Intent intent = new Intent(getContext(), PracticeListsActivity.class);
                    intent.putExtra("category_id", category.getId());
                    intent.putExtra("category_name", category.getName());
                    startActivity(intent);
                }
            });
            
            categoriesRecyclerView.setAdapter(adapter);
            dataManager.addDataUpdateListener(this);
            
            return view;
        } catch (Exception e) {
            // Return a simple error view if something goes wrong
            TextView errorView = new TextView(getContext());
            errorView.setText("Error loading practice section");
            errorView.setGravity(android.view.Gravity.CENTER);
            return errorView;
        }
    }

    @Override
    public void onPracticeCategoriesUpdated() {
        if (getActivity() != null && !getActivity().isFinishing()) {
            getActivity().runOnUiThread(() -> {
                try {
                    if (adapter != null && dataManager != null) {
                        adapter.setCategories(dataManager.getAllCategories());
                    }
                } catch (Exception e) {
                    // Handle update errors silently
                }
            });
        }
    }

    @Override
    public void onPracticeListsUpdated() {}

    @Override
    public void onPracticeExercisesUpdated() {}

    private void loadPracticeCategories() {
        if (dataManager != null) {
            List<PracticeCategory> categories = dataManager.getAllCategories();
            if (adapter != null) {
                adapter.setCategories(categories);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dataManager != null) {
            dataManager.removeDataUpdateListener(this);
        }
    }
}