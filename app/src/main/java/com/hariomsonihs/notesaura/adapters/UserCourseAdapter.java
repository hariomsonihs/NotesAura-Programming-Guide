package com.hariomsonihs.notesaura.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.models.UserCourse;

public class UserCourseAdapter extends RecyclerView.Adapter<UserCourseAdapter.CourseViewHolder> {
    private List<UserCourse> courses;
    private OnCourseClickListener listener;

    public interface OnCourseClickListener {
        void onCourseClick(UserCourse course);
    }

    public UserCourseAdapter(List<UserCourse> courses, OnCourseClickListener listener) {
        this.courses = courses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        UserCourse course = courses.get(position);
        holder.bind(course, listener);
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        private TextView courseName, category, progress, amount, enrollDate;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            courseName = itemView.findViewById(R.id.course_name);
            category = itemView.findViewById(R.id.course_category);
            progress = itemView.findViewById(R.id.course_progress);
            amount = itemView.findViewById(R.id.amount_paid);
            enrollDate = itemView.findViewById(R.id.enroll_date);
        }

        public void bind(UserCourse course, OnCourseClickListener listener) {
            courseName.setText(course.getCourseName());
            category.setText(course.getCategory());
            progress.setText((course.getProgressPercentage() != null ? course.getProgressPercentage() : 0) + "%");
            amount.setText("₹" + (course.getAmountPaid() != null ? course.getAmountPaid() : 0));
            
            if (course.getEnrollmentDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                enrollDate.setText(sdf.format(course.getEnrollmentDate()));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCourseClick(course);
                }
            });
        }
    }
}