package com.hariomsonihs.notesaura.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.hariomsonihs.notesaura.R;

public class EmptyStateHelper {
    
    public static View createEmptyStateView(Context context, String message, String subtitle) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View emptyView = inflater.inflate(R.layout.layout_empty_state, null);
        
        TextView messageText = emptyView.findViewById(R.id.empty_message);
        TextView subtitleText = emptyView.findViewById(R.id.empty_subtitle);
        ImageView iconView = emptyView.findViewById(R.id.empty_icon);
        
        messageText.setText(message);
        subtitleText.setText(subtitle);
        
        // Start animation
        startEmptyStateAnimation(iconView, messageText, subtitleText);
        
        return emptyView;
    }
    
    public static void showEmptyState(ViewGroup container, String message, String subtitle) {
        container.removeAllViews();
        View emptyView = createEmptyStateView(container.getContext(), message, subtitle);
        container.addView(emptyView);
    }
    
    private static void startEmptyStateAnimation(ImageView icon, TextView message, TextView subtitle) {
        // Initial state
        icon.setAlpha(0f);
        message.setAlpha(0f);
        subtitle.setAlpha(0f);
        icon.setScaleX(0.5f);
        icon.setScaleY(0.5f);
        
        // Create animations
        ObjectAnimator iconAlpha = ObjectAnimator.ofFloat(icon, "alpha", 0f, 1f);
        ObjectAnimator iconScaleX = ObjectAnimator.ofFloat(icon, "scaleX", 0.5f, 1f);
        ObjectAnimator iconScaleY = ObjectAnimator.ofFloat(icon, "scaleY", 0.5f, 1f);
        
        ObjectAnimator messageAlpha = ObjectAnimator.ofFloat(message, "alpha", 0f, 1f);
        ObjectAnimator subtitleAlpha = ObjectAnimator.ofFloat(subtitle, "alpha", 0f, 1f);
        
        // Set durations
        iconAlpha.setDuration(600);
        iconScaleX.setDuration(600);
        iconScaleY.setDuration(600);
        messageAlpha.setDuration(400);
        subtitleAlpha.setDuration(400);
        
        // Set delays
        messageAlpha.setStartDelay(300);
        subtitleAlpha.setStartDelay(500);
        
        // Create animator set
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(iconAlpha, iconScaleX, iconScaleY, messageAlpha, subtitleAlpha);
        animatorSet.start();
        
        // Add bounce animation to icon
        ObjectAnimator bounce = ObjectAnimator.ofFloat(icon, "translationY", 0f, -20f, 0f);
        bounce.setDuration(1000);
        bounce.setRepeatCount(ObjectAnimator.INFINITE);
        bounce.setRepeatMode(ObjectAnimator.REVERSE);
        bounce.setStartDelay(1000);
        bounce.start();
    }
}