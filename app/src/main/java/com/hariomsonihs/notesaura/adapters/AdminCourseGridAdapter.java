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
import com.hariomsonihs.notesaura.models.Course;
import com.hariomsonihs.notesaura.interfaces.OnCourseClickListener;
import com.hariomsonihs.notesaura.utils.CourseDataManager;

public class AdminCourseGridAdapter extends RecyclerView.Adapter<AdminCourseGridAdapter.CourseViewHolder> {
    private List<Course> courses;
    private OnCourseClickListener listener;
    private OnItemLongClickListener longClickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(int position, Course course);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public AdminCourseGridAdapter(List<Course> courses, OnCourseClickListener listener) {
        this.courses = courses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course_grid, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.bind(course, listener);
        if (longClickListener != null) {
            holder.itemView.setOnLongClickListener(v -> {
                longClickListener.onItemLongClick(position, course);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        private ImageView courseIcon;
        private TextView courseTitle, courseDescription, courseDifficulty, courseDuration, coursePrice;
        private View categoryBackground;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            courseIcon = itemView.findViewById(R.id.course_icon);
            courseTitle = itemView.findViewById(R.id.course_title);
            courseDescription = itemView.findViewById(R.id.course_description);
            courseDifficulty = itemView.findViewById(R.id.course_difficulty);
            courseDuration = itemView.findViewById(R.id.course_duration);
            coursePrice = itemView.findViewById(R.id.course_price);
            categoryBackground = itemView.findViewById(R.id.category_background);
        }

        public void bind(Course course, OnCourseClickListener listener) {
            courseTitle.setText(course.getTitle());
            courseDescription.setText(course.getDescription());
            courseDifficulty.setText(course.getDifficulty() != null ? course.getDifficulty() : "Beginner");

            int exerciseCount = 0;
            if (course.getId() != null) {
                exerciseCount = CourseDataManager.getInstance().getExercises(course.getId()).size();
            }
            courseDuration.setText(exerciseCount + " exercises");

            coursePrice.setText(course.isFree() ? "Free" : "₹" + String.format("%.0f", course.getPrice()));

            setCourseIcon(course.getCategory());
            setCategoryBackground(course.getCategory());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCourseClick(course);
                }
            });
        }

        private void setCategoryBackground(String category) {
            if (category == null) category = "";
            
            int backgroundRes;
            String lowerCategory = category.toLowerCase();
            
            if (lowerCategory.contains("java")) {
                backgroundRes = R.drawable.bg_gradient_java;
            } else if (lowerCategory.contains("python")) {
                backgroundRes = R.drawable.bg_gradient_python;
            } else if (lowerCategory.contains("web") || lowerCategory.contains("html") || lowerCategory.contains("css") || lowerCategory.contains("javascript")) {
                backgroundRes = R.drawable.bg_gradient_web;
            } else if (lowerCategory.contains("android") || lowerCategory.contains("app")) {
                backgroundRes = R.drawable.bg_gradient_android;
            } else if (lowerCategory.contains("data") || lowerCategory.contains("machine") || lowerCategory.contains("ai")) {
                backgroundRes = R.drawable.bg_gradient_data;
            } else if (lowerCategory.contains("c++") || lowerCategory.contains("cpp") || lowerCategory.contains("c")) {
                backgroundRes = R.drawable.bg_gradient_cpp;
            } else {
                backgroundRes = R.drawable.bg_gradient_secondary;
            }
            
            categoryBackground.setBackgroundResource(backgroundRes);
        }
        
        private void setCourseIcon(String category) {
            if (category == null) category = "";
            
            int iconRes;
            String lowerCategory = category.toLowerCase();
            
            if (lowerCategory.contains("java")) {
                iconRes = R.drawable.ic_code;
            } else if (lowerCategory.contains("python")) {
                iconRes = R.drawable.ic_code;
            } else if (lowerCategory.contains("web") || lowerCategory.contains("html") || lowerCategory.contains("css") || lowerCategory.contains("javascript")) {
                iconRes = R.drawable.ic_web;
            } else if (lowerCategory.contains("android") || lowerCategory.contains("app")) {
                iconRes = R.drawable.ic_phone;
            } else if (lowerCategory.contains("data") || lowerCategory.contains("machine") || lowerCategory.contains("ai")) {
                iconRes = R.drawable.ic_analytics;
            } else if (lowerCategory.contains("c++") || lowerCategory.contains("cpp") || lowerCategory.contains("c")) {
                iconRes = R.drawable.ic_note;
            } else {
                iconRes = R.drawable.ic_code;
            }
            
            courseIcon.setImageResource(iconRes);
        }
    }
}
