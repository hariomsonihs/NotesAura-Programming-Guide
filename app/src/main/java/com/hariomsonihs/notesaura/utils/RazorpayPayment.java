package com.hariomsonihs.notesaura.utils;

import android.app.Activity;
import org.json.JSONObject;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

public class RazorpayPayment implements PaymentResultListener {
    private Activity activity;
    private PaymentCallback callback;
    
    public interface PaymentCallback {
        void onPaymentSuccess(String paymentId);
        void onPaymentError(String error);
    }
    
    public RazorpayPayment(Activity activity, PaymentCallback callback) {
        this.activity = activity;
        this.callback = callback;
    }
    
    public void startPayment(String courseName, double amount, String userEmail, String userPhone) {
        try {
            Checkout checkout = new Checkout();
            checkout.setKeyID("rzp_test_1DP5mmOlF5G5ag"); // Free test key
            
            JSONObject options = new JSONObject();
            options.put("name", "NotesAura");
            options.put("description", "Payment for " + courseName);
            options.put("image", "https://your-logo-url.com/logo.png");
            options.put("order_id", "order_" + System.currentTimeMillis());
            options.put("theme.color", "#4776E6");
            options.put("currency", "INR");
            options.put("amount", (int)(amount * 100)); // Amount in paise
            
            JSONObject prefill = new JSONObject();
            prefill.put("email", userEmail);
            prefill.put("contact", userPhone != null ? userPhone : "9999999999");
            options.put("prefill", prefill);
            
            checkout.open(activity, options);
            
        } catch (Exception e) {
            callback.onPaymentError("Payment initialization failed: " + e.getMessage());
        }
    }
    
    @Override
    public void onPaymentSuccess(String paymentId) {
        callback.onPaymentSuccess(paymentId);
    }
    
    @Override
    public void onPaymentError(int code, String response) {
        callback.onPaymentError("Payment failed: " + response);
    }
}