package com.hariomsonihs.notesaura.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.hariomsonihs.notesaura.R;

public class AnnouncementActivity extends AppCompatActivity {
    
    public static void showAnnouncement(Context context, String title, String message, String type) {
        Intent intent = new Intent(context, AnnouncementActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.putExtra("type", type);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement);
        
        String title = getIntent().getStringExtra("title");
        String message = getIntent().getStringExtra("message");
        String type = getIntent().getStringExtra("type");
        
        TextView titleText = findViewById(R.id.announcement_title);
        TextView messageText = findViewById(R.id.announcement_message);
        ImageView iconView = findViewById(R.id.announcement_icon);
        Button okButton = findViewById(R.id.ok_button);
        
        titleText.setText(title != null ? title : "Announcement");
        messageText.setText(message != null ? message : "");
        
        // Set icon based on type
        switch (type != null ? type : "info") {
            case "warning":
                iconView.setImageResource(R.drawable.ic_notification);
                break;
            case "success":
                iconView.setImageResource(R.drawable.ic_check);
                break;
            case "error":
                iconView.setImageResource(R.drawable.ic_close);
                break;
            default:
                iconView.setImageResource(R.drawable.ic_notification);
                break;
        }
        
        okButton.setOnClickListener(v -> finish());
    }
}