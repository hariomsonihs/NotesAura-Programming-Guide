package com.hariomsonihs.notesaura.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.models.Course;
import com.hariomsonihs.notesaura.utils.CourseDataManager;

public class AdminFeaturedCoursesAdapter extends RecyclerView.Adapter<AdminFeaturedCoursesAdapter.ViewHolder> {
    private List<Course> courses;
    private OnCourseActionListener listener;

    public interface OnCourseActionListener {
        void onToggleFeatured(Course course, boolean isFeatured);
    }

    public AdminFeaturedCoursesAdapter(List<Course> courses, OnCourseActionListener listener) {
        this.courses = courses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_featured_course, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.courseTitle.setText(course.getTitle());
        holder.courseCategory.setText(course.getCategory());
        
        // Clear listener first to prevent unwanted triggers
        holder.featuredSwitch.setOnCheckedChangeListener(null);
        
        boolean isFeatured = CourseDataManager.getInstance().isFeatured(course.getId());
        holder.featuredSwitch.setChecked(isFeatured);
        
        // Set listener after setting the checked state
        holder.featuredSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onToggleFeatured(course, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView courseTitle, courseCategory;
        Switch featuredSwitch;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            courseTitle = itemView.findViewById(R.id.course_title);
            courseCategory = itemView.findViewById(R.id.course_category);
            featuredSwitch = itemView.findViewById(R.id.featured_switch);
        }
    }
}