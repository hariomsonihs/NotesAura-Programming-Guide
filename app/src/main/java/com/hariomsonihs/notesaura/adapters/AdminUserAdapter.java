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
import com.hariomsonihs.notesaura.models.User;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {
    private List<User> users;
    private OnUserClickListener listener;
    private OnUserLongClickListener longClickListener;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }
    
    public interface OnUserLongClickListener {
        void onUserLongClick(User user);
    }

    public AdminUserAdapter(List<User> users, OnUserClickListener listener, OnUserLongClickListener longClickListener) {
        this.users = users;
        this.listener = listener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user, listener, longClickListener);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView userName, userEmail, userStatus, joinDate;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userEmail = itemView.findViewById(R.id.user_email);
            userStatus = itemView.findViewById(R.id.user_status);
            joinDate = itemView.findViewById(R.id.join_date);
        }

        public void bind(User user, OnUserClickListener listener, OnUserLongClickListener longClickListener) {
            userName.setText(user.getName());
            userEmail.setText(user.getEmail());
            userStatus.setText(user.isAdmin() ? "Admin" : "User");
            
            if (user.getJoiningDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                joinDate.setText("Joined: " + sdf.format(user.getJoiningDate()));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUserClick(user);
                }
            });
            
            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onUserLongClick(user);
                    return true;
                }
                return false;
            });
        }
    }
}