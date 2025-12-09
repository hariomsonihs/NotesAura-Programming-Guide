package com.hariomsonihs.notesaura.utils;

import android.content.Context;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hariomsonihs.notesaura.R;

public class ImageLoader {
    
    public static void loadImage(Context context, String imageUrl, ImageView imageView, int placeholderRes) {
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Glide.with(context)
                .load(imageUrl)
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(imageView);
        } else {
            imageView.setImageResource(placeholderRes);
        }
    }
    
    public static void loadEbookImage(Context context, String imageUrl, ImageView imageView) {
        loadImage(context, imageUrl, imageView, R.drawable.ic_book_placeholder);
    }
    
    public static void loadCourseImage(Context context, String imageUrl, ImageView imageView, int defaultIcon) {
        loadImage(context, imageUrl, imageView, defaultIcon);
    }
}