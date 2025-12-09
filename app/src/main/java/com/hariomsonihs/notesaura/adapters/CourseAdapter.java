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
import com.bumptech.glide.Glide;
import android.text.TextUtils;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {
    private List<Course> courses;
    private OnCourseClickListener listener;
    private OnItemLongClickListener longClickListener;
    private boolean showBookmarkIcon = true;

    public interface OnItemLongClickListener {
        void onItemLongClick(int position, Course course);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public CourseAdapter(List<Course> courses, OnCourseClickListener listener) {
        this.courses = courses;
        this.listener = listener;
    }
    
    public CourseAdapter(List<Course> courses, OnCourseClickListener listener, boolean showBookmarkIcon) {
        this.courses = courses;
        this.listener = listener;
        this.showBookmarkIcon = showBookmarkIcon;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.bind(course, listener, showBookmarkIcon);
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

    class CourseViewHolder extends RecyclerView.ViewHolder {
        private ImageView courseIcon, bookmarkIcon;
        private TextView courseTitle, courseDescription, courseDifficulty, courseDuration, coursePrice;
        private View categoryBackground;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            courseIcon = itemView.findViewById(R.id.course_icon);
            bookmarkIcon = itemView.findViewById(R.id.bookmark_icon);
            courseTitle = itemView.findViewById(R.id.course_title);
            courseDescription = itemView.findViewById(R.id.course_description);
            courseDifficulty = itemView.findViewById(R.id.course_difficulty);
            courseDuration = itemView.findViewById(R.id.course_duration);
            coursePrice = itemView.findViewById(R.id.course_price);
            categoryBackground = itemView.findViewById(R.id.category_background);
        }

        public void bind(Course course, OnCourseClickListener listener, boolean showBookmarkIcon) {
            courseTitle.setText(course.getTitle());
            courseDescription.setText(course.getDescription());
            courseDifficulty.setText(course.getDifficulty() != null ? course.getDifficulty() : "Beginner");

            // Dynamic exercise count
            int exerciseCount = 0;
            if (course.getId() != null) {
                exerciseCount = CourseDataManager.getInstance().getExercises(course.getId()).size();
            }
            courseDuration.setText(exerciseCount + " exercises");

            coursePrice.setText(course.isFree() ? "Free" : "" + String.format("%.0f", course.getPrice()));

            // Set course icon/image and background
            setCourseImage(course);
            setCategoryBackground(course.getCategory());
            
            // Set bookmark state and visibility
            if (showBookmarkIcon) {
                bookmarkIcon.setVisibility(View.VISIBLE);
                boolean isBookmarked = CourseDataManager.getInstance().isBookmarked(course.getId());
                bookmarkIcon.setImageResource(isBookmarked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_border);
                
                // Bookmark click listener
                bookmarkIcon.setOnClickListener(v -> {
                    CourseDataManager.getInstance().toggleBookmark(course.getId());
                    boolean newState = CourseDataManager.getInstance().isBookmarked(course.getId());
                    bookmarkIcon.setImageResource(newState ? R.drawable.ic_heart_filled : R.drawable.ic_heart_border);
                });
            } else {
                bookmarkIcon.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCourseClick(course);
                }
            });
        }
        
    // getExerciseCount removed, now dynamic

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
        
        private void setCourseImage(Course course) {
            String imageUrl = course.getImageUrl();
            
            if (!TextUtils.isEmpty(imageUrl)) {
                // Load full background image from URL
                courseIcon.setImageTintList(null);
                Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .placeholder(getDefaultIcon(course.getCategory()))
                    .error(getDefaultIcon(course.getCategory()))
                    .centerCrop()
                    .into(courseIcon);
                // Hide gradient overlay when image is loaded
                categoryBackground.setAlpha(0.3f);
            } else {
                // Use gradient background only
                courseIcon.setImageResource(getDefaultIcon(course.getCategory()));
                categoryBackground.setAlpha(1.0f);
            }
        }
        
        private int getDefaultIcon(String category) {
            if (category == null) category = "";
            
            String lowerCategory = category.toLowerCase();
            
            if (lowerCategory.contains("java")) {
                return R.drawable.ic_code;
            } else if (lowerCategory.contains("python")) {
                return R.drawable.ic_code;
            } else if (lowerCategory.contains("web") || lowerCategory.contains("html") || lowerCategory.contains("css") || lowerCategory.contains("javascript")) {
                return R.drawable.ic_web;
            } else if (lowerCategory.contains("android") || lowerCategory.contains("app")) {
                return R.drawable.ic_phone;
            } else if (lowerCategory.contains("data") || lowerCategory.contains("machine") || lowerCategory.contains("ai")) {
                return R.drawable.ic_analytics;
            } else if (lowerCategory.contains("c++") || lowerCategory.contains("cpp") || lowerCategory.contains("c")) {
                return R.drawable.ic_note;
            } else {
                return R.drawable.ic_code;
            }
        }
    }
}