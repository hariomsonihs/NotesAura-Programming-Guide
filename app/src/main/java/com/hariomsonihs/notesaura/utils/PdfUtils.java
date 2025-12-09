package com.hariomsonihs.notesaura.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PdfUtils {
    
    private static final String PREF_PDF_VIEWER_TYPE = "pdf_viewer_type";
    private static final String VIEWER_NATIVE = "native";
    private static final String VIEWER_WEB = "web";
    
    public static void openPdfViewer(Context context, String pdfUrl, String pdfName) {
        Intent intent = new Intent(context, com.hariomsonihs.notesaura.activities.PdfViewerActivity.class);
        intent.putExtra("pdf_url", pdfUrl);
        intent.putExtra("pdf_name", pdfName != null ? pdfName : "PDF Document");
        context.startActivity(intent);
    }
    
    public static void openOptimizedPdfViewer(Context context, String pdfUrl, String pdfName) {
        Intent intent = new Intent(context, com.hariomsonihs.notesaura.activities.PdfViewerActivity.class);
        intent.putExtra("pdf_url", pdfUrl);
        intent.putExtra("pdf_name", pdfName != null ? pdfName : "PDF Document");
        context.startActivity(intent);
    }
    
    public static void openWebPdfViewer(Context context, String pdfUrl, String pdfName) {
        Intent intent = new Intent(context, com.hariomsonihs.notesaura.activities.PdfViewerActivity.class);
        intent.putExtra("pdf_url", pdfUrl);
        intent.putExtra("pdf_name", pdfName != null ? pdfName : "PDF Document");
        context.startActivity(intent);
    }
}