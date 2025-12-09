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

public class CourseGridAdapter extends RecyclerView.Adapter<CourseGridAdapter.CourseViewHolder> {
    private List<Course> courses;
    private OnCourseClickListener listener;

    public CourseGridAdapter(List<Course> courses, OnCourseClickListener listener) {
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
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        private ImageView courseIcon, bookmarkIcon, rateCourseIcon;
        private TextView courseTitle, courseDescription, courseDifficulty, courseDuration, coursePrice;
        private View categoryBackground;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            courseIcon = itemView.findViewById(R.id.course_icon);
            bookmarkIcon = itemView.findViewById(R.id.bookmark_icon);
            rateCourseIcon = itemView.findViewById(R.id.rate_course_icon);
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

            setCourseImage(course);
            setCategoryBackground(course.getCategory());
            
            // Set bookmark state
            if (bookmarkIcon != null) {
                boolean isBookmarked = CourseDataManager.getInstance().isBookmarked(course.getId());
                bookmarkIcon.setImageResource(isBookmarked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_border);
                
                // Bookmark click listener
                bookmarkIcon.setOnClickListener(v -> {
                    CourseDataManager.getInstance().toggleBookmark(course.getId());
                    boolean newState = CourseDataManager.getInstance().isBookmarked(course.getId());
                    bookmarkIcon.setImageResource(newState ? R.drawable.ic_heart_filled : R.drawable.ic_heart_border);
                });
            }
            
            // Rate course click listener
            if (rateCourseIcon != null) {
                rateCourseIcon.setOnClickListener(v -> showRatingDialog(course));
            }

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
        
        private void showRatingDialog(Course course) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(itemView.getContext());
            android.view.View dialogView = android.view.LayoutInflater.from(itemView.getContext()).inflate(R.layout.dialog_rate_course, null);
            
            ImageView[] stars = {
                dialogView.findViewById(R.id.star1),
                dialogView.findViewById(R.id.star2),
                dialogView.findViewById(R.id.star3),
                dialogView.findViewById(R.id.star4),
                dialogView.findViewById(R.id.star5)
            };
            
            android.widget.EditText commentEditText = dialogView.findViewById(R.id.comment_edit_text);
            final int[] selectedRating = {0};
            
            // Star click listeners
            for (int i = 0; i < stars.length; i++) {
                final int rating = i + 1;
                stars[i].setOnClickListener(v -> {
                    selectedRating[0] = rating;
                    updateStars(stars, rating);
                });
            }
            
            builder.setView(dialogView)
                    .setTitle("Rate " + course.getTitle())
                    .setPositiveButton("Submit", (dialog, which) -> {
                        if (selectedRating[0] > 0) {
                            String comment = commentEditText.getText().toString().trim();
                            submitRating(course.getId(), selectedRating[0], comment);
                            android.widget.Toast.makeText(itemView.getContext(), "Rating submitted!", android.widget.Toast.LENGTH_SHORT).show();
                        } else {
                            android.widget.Toast.makeText(itemView.getContext(), "Please select a rating", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
        
        private void updateStars(ImageView[] stars, int rating) {
            for (int i = 0; i < stars.length; i++) {
                if (i < rating) {
                    stars[i].setImageResource(R.drawable.ic_star_filled);
                } else {
                    stars[i].setImageResource(R.drawable.ic_star_border);
                }
            }
        }
        
        private void submitRating(String courseId, int rating, String comment) {
            com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                java.util.Map<String, Object> ratingData = new java.util.HashMap<>();
                ratingData.put("courseId", courseId);
                ratingData.put("userId", currentUser.getUid());
                ratingData.put("userName", currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Anonymous");
                ratingData.put("rating", rating);
                ratingData.put("comment", comment);
                ratingData.put("timestamp", System.currentTimeMillis());
                
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("course_ratings")
                    .document(currentUser.getUid() + "_" + courseId)
                    .set(ratingData);
            }
        }
    }
}