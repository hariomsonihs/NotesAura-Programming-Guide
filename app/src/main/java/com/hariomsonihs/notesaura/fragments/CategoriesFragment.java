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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.hariomsonihs.notesaura.utils.CourseDataManager;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.adapters.CategoryAdapter;
import com.hariomsonihs.notesaura.models.Category;

public class CategoriesFragment extends Fragment implements CourseDataManager.DataUpdateListener {
    private RecyclerView categoriesRecyclerView;
    private CategoryAdapter categoryAdapter;
    private List<Category> categories = new ArrayList<>();
    private CourseDataManager dataManager;
    private SwipeRefreshLayout swipeRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);
        dataManager = CourseDataManager.getInstance();
        initializeViews(view);
        setupRecyclerView();
        loadCategories();
        dataManager.addDataUpdateListener(this);
        return view;
    }

    private void initializeViews(View view) {
        categoriesRecyclerView = view.findViewById(R.id.categories_recycler_view);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        
        swipeRefresh.setOnRefreshListener(this::refreshCategories);
        swipeRefresh.setColorSchemeResources(
            R.color.primary_start,
            R.color.secondary_start,
            R.color.accent_green
        );
    }

    private void setupRecyclerView() {
        categoriesRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        categories = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(categories, category -> {
            CategoryCoursesFragment fragment = CategoryCoursesFragment.newInstance(category.getId(), category.getName());
            getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
        });
        categoriesRecyclerView.setAdapter(categoryAdapter);
    }

    private void loadCategories() {
        categories.clear();
        Map<String, String> categoryMap = dataManager.getOrderedCategoriesMap();
        for (Map.Entry<String, String> entry : categoryMap.entrySet()) {
            String id = entry.getKey();
            String name = entry.getValue();
            Category category = new Category(id, name, "", "#4776E6");
            // Load image URL from Firebase data if available
            category.setIconUrl(getCategoryImageUrl(id));
            category.setCourseCount(dataManager.getCoursesByCategory(id).size());
            categories.add(category);
        }
        categoryAdapter.notifyDataSetChanged();
        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(false);
        }
    }
    
    private void refreshCategories() {
        loadCategories();
    }

    @Override
    public void onCategoriesUpdated() {
        loadCategories();
    }

    @Override
    public void onCoursesUpdated() {
        loadCategories(); // To update course counts per category
    }

    private String getCategoryImageUrl(String categoryId) {
        return dataManager.getCategoryImageUrl(categoryId);
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dataManager != null) {
            dataManager.removeDataUpdateListener(this);
        }
    }
}