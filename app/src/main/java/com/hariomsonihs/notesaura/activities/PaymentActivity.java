package com.hariomsonihs.notesaura.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import org.json.JSONObject;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.models.Payment;
import com.hariomsonihs.notesaura.utils.Constants;
import com.hariomsonihs.notesaura.utils.FirebaseDBHelper;

public class PaymentActivity extends AppCompatActivity implements PaymentResultListener {
    private TextView courseTitle, courseFee, totalAmount;
    private Button payButton;
    private String courseId, courseName;
    private double amount;
    private FirebaseDBHelper dbHelper;
    private Payment currentPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        initializeViews();
        setupToolbar();
        loadCourseData();
        setupClickListeners();
    }

    private void initializeViews() {
    courseTitle = findViewById(R.id.course_title);
    courseFee = findViewById(R.id.course_fee);
    totalAmount = findViewById(R.id.total_amount);
        payButton = findViewById(R.id.pay_button);
        dbHelper = FirebaseDBHelper.getInstance();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Payment");
        }
    }

    private void loadCourseData() {
        courseId = getIntent().getStringExtra(Constants.KEY_COURSE_ID);
        courseName = getIntent().getStringExtra("course_name");
        amount = getIntent().getDoubleExtra("course_price", 299.0);

    courseTitle.setText(courseName != null ? courseName : "Course");
    courseFee.setText("₹" + String.format("%.0f", amount));
    totalAmount.setText("₹" + String.format("%.0f", amount));
    }

    private void setupClickListeners() {
    payButton.setOnClickListener(v -> initiateRazorpayPayment());
    }
    
    private void initiateRazorpayPayment() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login to continue", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create payment record
        currentPayment = new Payment(courseName, amount, new Date(), "PENDING", user.getUid());
        String transactionId = "RZP" + System.currentTimeMillis();
        currentPayment.setTransactionId(transactionId);
        savePaymentToFirebase();

        // Fetch real user phone and name from Firestore
        FirebaseDBHelper.getInstance().getUserDocument(user.getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                String phone = documentSnapshot.getString("phone");
                String name = documentSnapshot.getString("name");
                try {
                    Checkout checkout = new Checkout();
                    checkout.setImage(R.drawable.ic_logo);
                    checkout.setKeyID("rzp_live_RDxnU79iQNwjZM");
                    Checkout.preload(getApplicationContext());
                    JSONObject options = new JSONObject();
                    options.put("name", "NotesAura");
                    options.put("description", "Course: " + courseName);
                    options.put("currency", "INR");
                    options.put("amount", (int)(amount * 100));
                    JSONObject theme = new JSONObject();
                    theme.put("color", "#4776E6");
                    options.put("theme", theme);
                    JSONObject prefill = new JSONObject();
                    prefill.put("email", user.getEmail() != null ? user.getEmail() : "test@example.com");
                    prefill.put("contact", phone != null ? phone : "9999999999");
                    prefill.put("name", name != null ? name : "User");
                    options.put("prefill", prefill);
                    JSONObject method = new JSONObject();
                    method.put("netbanking", true);
                    method.put("card", true);
                    method.put("upi", true);
                    method.put("wallet", true);
                    options.put("method", method);
                    JSONObject retry = new JSONObject();
                    retry.put("enabled", true);
                    retry.put("max_count", 3);
                    options.put("retry", retry);
                    JSONObject notes = new JSONObject();
                    notes.put("course_id", courseId);
                    notes.put("user_id", user.getUid());
                    notes.put("platform", "android");
                    options.put("notes", notes);
                    checkout.open(this, options);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Razorpay initialization failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    new android.app.AlertDialog.Builder(this)
                            .setTitle("Payment Error")
                            .setMessage("Razorpay not working. Use test payment instead?")
                            .setPositiveButton("Test Payment", (dialog, which) -> {
                                currentPayment.setStatus("SUCCESS");
                                updatePaymentStatus("SUCCESS");
                                enrollUserInCourse();
                                Toast.makeText(this, "Test payment successful! Course enrolled.", Toast.LENGTH_LONG).show();
                                finish();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to fetch user phone. Using default.", Toast.LENGTH_SHORT).show();
                try {
                    Checkout checkout = new Checkout();
                    checkout.setImage(R.drawable.ic_logo);
                    checkout.setKeyID("rzp_live_RDxnU79iQNwjZM");
                    Checkout.preload(getApplicationContext());
                    JSONObject options = new JSONObject();
                    options.put("name", "NotesAura");
                    options.put("description", "Course: " + courseName);
                    options.put("currency", "INR");
                    options.put("amount", (int)(amount * 100));
                    JSONObject theme = new JSONObject();
                    theme.put("color", "#4776E6");
                    options.put("theme", theme);
                    JSONObject prefill = new JSONObject();
                    prefill.put("email", user.getEmail() != null ? user.getEmail() : "test@example.com");
                    prefill.put("contact", "9999999999");
                    prefill.put("name", user.getDisplayName() != null ? user.getDisplayName() : "User");
                    options.put("prefill", prefill);
                    JSONObject method = new JSONObject();
                    method.put("netbanking", true);
                    method.put("card", true);
                    method.put("upi", true);
                    method.put("wallet", true);
                    options.put("method", method);
                    JSONObject retry = new JSONObject();
                    retry.put("enabled", true);
                    retry.put("max_count", 3);
                    options.put("retry", retry);
                    JSONObject notes = new JSONObject();
                    notes.put("course_id", courseId);
                    notes.put("user_id", user.getUid());
                    notes.put("platform", "android");
                    options.put("notes", notes);
                    checkout.open(this, options);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(this, "Razorpay initialization failed: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }
    
    @Override
    public void onPaymentSuccess(String paymentId) {
        // Razorpay payment successful
        if (currentPayment != null) {
            currentPayment.setTransactionId(paymentId);
            currentPayment.setStatus("SUCCESS");
            // Update both transactionId and status in Firebase
            dbHelper.getPaymentDocument(currentPayment.getUserId(), paymentId)
                    .set(currentPayment)
                    .addOnSuccessListener(aVoid -> {
                        enrollUserInCourse();
                        Toast.makeText(this, "Payment successful! Course enrolled.\nTxn ID: " + paymentId, Toast.LENGTH_LONG).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Payment success but failed to update record", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }
    }
    
    @Override
    public void onPaymentError(int code, String response) {
        // Razorpay payment failed
        String status = "FAILED";
        String message = "Payment failed";
        
        // Check if user cancelled or actual failure
        if (response != null && response.contains("Cancelled")) {
            status = "CANCELLED";
            message = "Payment cancelled by user";
        }
        
        updatePaymentStatus(status);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void initiatePayment() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login to continue", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create payment record
        currentPayment = new Payment(courseName, amount, new Date(), "PENDING", user.getUid());
        String transactionId = "TXN" + System.currentTimeMillis();
        currentPayment.setTransactionId(transactionId);

        // Save payment to Firebase
        savePaymentToFirebase();

        // Launch UPI payment
        launchUPIPayment(transactionId);
    }

    private void savePaymentToFirebase() {
        dbHelper.getPaymentDocument(currentPayment.getUserId(), currentPayment.getTransactionId())
                .set(currentPayment)
                .addOnSuccessListener(aVoid -> {
                    // Payment record saved
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to create payment record", Toast.LENGTH_SHORT).show();
                });
    }

    private void launchUPIPayment(String transactionId) {
        String upiId = "7667110195@upi";
        String name = "NotesAura";
        String note = "Payment for " + courseName + " - " + transactionId;
        
        // Try different UPI URL formats for better compatibility
        String upiUrl = "upi://pay?pa=" + upiId + 
                       "&pn=" + Uri.encode(name) + 
                       "&am=" + amount + 
                       "&cu=INR" + 
                       "&tn=" + Uri.encode(note) +
                       "&tr=" + transactionId;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(upiUrl));
        
        // Add flags for better handling
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        Intent chooser = Intent.createChooser(intent, "Complete Payment");
        
        if (chooser.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(chooser, 1001);
        } else {
            // Fallback: Show manual payment instructions
            showManualPaymentDialog(upiId, transactionId);
        }
    }
    
    private void showManualPaymentDialog(String upiId, String transactionId) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Manual Payment")
                .setMessage("No UPI app found. Please pay manually:\n\n" +
                           "UPI ID: " + upiId + "\n" +
                           "Amount: ₹" + amount + "\n" +
                           "Reference: " + transactionId + "\n\n" +
                           "After payment, contact support with transaction details.")
                .setPositiveButton("I Paid", (dialog, which) -> {
                    updatePaymentStatus("PENDING");
                    Toast.makeText(this, "Payment marked as pending. We'll verify and activate your course.", Toast.LENGTH_LONG).show();
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    updatePaymentStatus("CANCELLED");
                    finish();
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1001) {
            String status = "FAILED";
            String message = "Payment failed";
            
            if (data != null) {
                String response = data.getStringExtra("response");
                if (response != null) {
                    if (response.toLowerCase().contains("success") || response.toLowerCase().contains("submitted")) {
                        status = "SUCCESS";
                        message = "Payment successful! Course enrolled.";
                    } else if (response.toLowerCase().contains("fail")) {
                        status = "FAILED";
                        message = "Payment failed";
                    } else {
                        status = "CANCELLED";
                        message = "Payment cancelled by user";
                    }
                } else {
                    // No response data - treat as cancelled
                    status = "CANCELLED";
                    message = "Payment cancelled";
                }
            } else {
                // No data returned - treat as cancelled
                status = "CANCELLED";
                message = "Payment cancelled";
            }
            
            updatePaymentStatus(status);
            
            if ("SUCCESS".equals(status)) {
                enrollUserInCourse();
            }
            
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            
            // Always finish activity after payment attempt
            finish();
        }
    }

    private void updatePaymentStatus(String status) {
    if (currentPayment != null) {
        currentPayment.setStatus(status);
        dbHelper.getPaymentDocument(currentPayment.getUserId(), currentPayment.getTransactionId())
            .update("status", status,
                "transactionId", currentPayment.getTransactionId());
    }
    }

    private void enrollUserInCourse() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && currentPayment != null) {
            Map<String, Object> enrollmentData = new HashMap<>();
            enrollmentData.put("courseId", courseId);
            enrollmentData.put("courseName", courseName);
            enrollmentData.put("category", getCategoryFromCourseId(courseId));
            enrollmentData.put("enrollmentDate", new Date());
            enrollmentData.put("lastAccessed", new Date());
            enrollmentData.put("progressPercentage", 0);
            enrollmentData.put("paymentId", currentPayment.getTransactionId());
            enrollmentData.put("amountPaid", amount);
            enrollmentData.put("paymentStatus", currentPayment.getStatus());

            dbHelper.getEnrollmentDocument(user.getUid(), courseId)
                    .set(enrollmentData)
                    .addOnSuccessListener(aVoid -> {
                        // Course enrolled successfully
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to enroll in course", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private String getCategoryFromCourseId(String courseId) {
        if (courseId.contains("java") || courseId.contains("python") || courseId.contains("c_") || 
            courseId.contains("cpp") || courseId.contains("programming")) {
            return "Programming";
        } else if (courseId.contains("html") || courseId.contains("css") || courseId.contains("javascript") || 
                   courseId.contains("web") || courseId.contains("react")) {
            return "Web Development";
        } else if (courseId.contains("android") || courseId.contains("flutter") || courseId.contains("app")) {
            return "App Development";
        } else if (courseId.contains("machine") || courseId.contains("data") || courseId.contains("ml")) {
            return "Data Science";
        } else if (courseId.contains("cheat")) {
            return "Cheat Sheets";
        }
        return "Programming";
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}