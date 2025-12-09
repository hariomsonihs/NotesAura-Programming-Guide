package com.hariomsonihs.notesaura.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.adapters.AdminPaymentAdapter;
import com.hariomsonihs.notesaura.models.Payment;
import com.hariomsonihs.notesaura.utils.AdminHelper;

public class AdminPaymentsActivity extends AppCompatActivity {
    private RecyclerView paymentsRecyclerView;
    private AdminPaymentAdapter paymentAdapter;
    private List<Payment> payments;
    private AdminHelper adminHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_payments);
        
        adminHelper = AdminHelper.getInstance();
        
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        loadPayments();
    }
    
    private void initializeViews() {
        paymentsRecyclerView = findViewById(R.id.payments_recycler_view);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Payment History");
        }
    }
    
    private void setupRecyclerView() {
        paymentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        payments = new ArrayList<>();
        paymentAdapter = new AdminPaymentAdapter(payments);
        paymentsRecyclerView.setAdapter(paymentAdapter);
    }
    
    private void loadPayments() {
        adminHelper.getAllPayments(paymentList -> {
            payments.clear();
            payments.addAll(paymentList);
            paymentAdapter.notifyDataSetChanged();
        });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}