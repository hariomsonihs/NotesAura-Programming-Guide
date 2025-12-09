package com.hariomsonihs.notesaura.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.models.EnrolledCourse;

public class EnrolledCourseAdapter extends RecyclerView.Adapter<EnrolledCourseAdapter.ViewHolder> {
    private List<EnrolledCourse> enrolledCourses;
    private OnEnrolledCourseClickListener listener;

    public interface OnEnrolledCourseClickListener {
        void onEnrolledCourseClick(EnrolledCourse course);
    }

    public EnrolledCourseAdapter(List<EnrolledCourse> enrolledCourses, OnEnrolledCourseClickListener listener) {
        this.enrolledCourses = enrolledCourses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_enrolled_course, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EnrolledCourse course = enrolledCourses.get(position);
        holder.bind(course);
    }

    @Override
    public int getItemCount() {
        return enrolledCourses.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView courseTitle, category, progressText, lastAccessed;
        ProgressBar progressBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            courseTitle = itemView.findViewById(R.id.course_title);
            category = itemView.findViewById(R.id.course_category);
            progressText = itemView.findViewById(R.id.progress_text);
            progressBar = itemView.findViewById(R.id.progress_bar);
            lastAccessed = itemView.findViewById(R.id.last_accessed);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEnrolledCourseClick(enrolledCourses.get(getAdapterPosition()));
                }
            });
        }

        void bind(EnrolledCourse course) {
            courseTitle.setText(course.getCourseTitle());
            category.setText(course.getCategory().toUpperCase());
            progressText.setText(course.getProgressPercentage() + "%");
            progressBar.setProgress(course.getProgressPercentage());
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            lastAccessed.setText("Last accessed: " + sdf.format(course.getLastAccessed()));
        }
    }
}