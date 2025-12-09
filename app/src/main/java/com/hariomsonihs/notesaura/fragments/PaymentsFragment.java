package com.hariomsonihs.notesaura.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.adapters.PaymentAdapter;
import com.hariomsonihs.notesaura.models.Payment;
import com.hariomsonihs.notesaura.utils.FirebaseDBHelper;

public class PaymentsFragment extends Fragment {
    private RecyclerView paymentsRecyclerView;
    private PaymentAdapter paymentAdapter;
    private List<Payment> payments;
    private FirebaseDBHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payments, container, false);

        initializeViews(view);
        setupRecyclerView();
        loadPayments();

        return view;
    }

    private void initializeViews(View view) {
        paymentsRecyclerView = view.findViewById(R.id.payments_recycler_view);
        dbHelper = FirebaseDBHelper.getInstance();
    }

    private void setupRecyclerView() {
        paymentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        payments = new ArrayList<>();
        paymentAdapter = new PaymentAdapter(payments);
        paymentsRecyclerView.setAdapter(paymentAdapter);
    }

    private void loadPayments() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            dbHelper.getUserPayments(currentUser.getUid())
                    .orderBy("paymentDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        payments.clear();
                        queryDocumentSnapshots.forEach(document -> {
                            Payment payment = document.toObject(Payment.class);
                            payments.add(payment);
                        });
                        paymentAdapter.notifyDataSetChanged();
                    });
        }
    }
}