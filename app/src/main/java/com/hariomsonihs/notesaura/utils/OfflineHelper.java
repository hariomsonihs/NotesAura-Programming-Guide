package com.hariomsonihs.notesaura.utils;

import android.content.Context;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

public class OfflineHelper {
    
    public static void showNoInternetMessage(Context context) {
        Toast.makeText(context, "Turn on your internet to load new content", Toast.LENGTH_SHORT).show();
    }
    
    public static void showNoInternetDialog(Context context, Runnable retryAction) {
        new AlertDialog.Builder(context)
                .setTitle("Turn On Your Internet")
                .setMessage("Please turn on your internet connection to load content. Once loaded, it will be available offline for future use.")
                .setCancelable(false)
                .setPositiveButton("Retry", (dialog, which) -> {
                    dialog.dismiss();
                    if (retryAction != null) {
                        retryAction.run();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
    
    public static boolean checkNetworkAndShowMessage(Context context) {
        if (!NetworkUtil.isNetworkAvailable(context)) {
            showNoInternetMessage(context);
            return false;
        }
        return true;
    }
    
    public static void showOfflineAvailableMessage(Context context) {
        Toast.makeText(context, "Content loaded! Now available offline", Toast.LENGTH_SHORT).show();
    }
    
    public static void showCachedContentMessage(Context context) {
        Toast.makeText(context, "Showing cached content - Turn on internet for updates", Toast.LENGTH_SHORT).show();
    }
}