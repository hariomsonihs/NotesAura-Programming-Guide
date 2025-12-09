package com.hariomsonihs.notesaura.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.models.AppNotification;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private List<AppNotification> notifications;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(AppNotification notification);
        void onNotificationDelete(AppNotification notification, int position);
    }

    public NotificationAdapter(List<AppNotification> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppNotification notification = notifications.get(position);
        
        holder.titleText.setText(notification.getTitle());
        holder.messageText.setText(notification.getMessage());
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        holder.timeText.setText(sdf.format(notification.getTimestampAsDate()));
        
        // Set read/unread state
        holder.itemView.setAlpha(notification.isRead() ? 0.7f : 1.0f);
        holder.unreadIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);
        
        // Load image
        if (notification.getImageUrl() != null && !notification.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(notification.getImageUrl())
                .placeholder(R.drawable.ic_notification)
                .error(R.drawable.ic_notification)
                .into(holder.iconImage);
        } else {
            holder.iconImage.setImageResource(getTypeIcon(notification.getType()));
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification);
            }
        });
        
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationDelete(notification, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    private int getTypeIcon(String type) {
        switch (type) {
            case "course": return R.drawable.ic_code;
            case "ebook": return R.drawable.ic_book_placeholder;
            case "practice": return R.drawable.ic_note;
            case "interview": return R.drawable.ic_analytics;
            default: return R.drawable.ic_notification;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImage, deleteButton;
        View unreadIndicator;
        TextView titleText, messageText, timeText;

        ViewHolder(View itemView) {
            super(itemView);
            iconImage = itemView.findViewById(R.id.notification_icon);
            unreadIndicator = itemView.findViewById(R.id.unread_indicator);
            titleText = itemView.findViewById(R.id.notification_title);
            messageText = itemView.findViewById(R.id.notification_message);
            timeText = itemView.findViewById(R.id.notification_time);
            deleteButton = itemView.findViewById(R.id.delete_notification);
        }
    }
}