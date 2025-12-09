package com.hariomsonihs.notesaura.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.models.Payment;

public class AdminPaymentAdapter extends RecyclerView.Adapter<AdminPaymentAdapter.PaymentViewHolder> {
    private List<Payment> payments;

    public AdminPaymentAdapter(List<Payment> payments) {
        this.payments = payments;
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_payment, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Payment payment = payments.get(position);
        holder.bind(payment);
    }

    @Override
    public int getItemCount() {
        return payments.size();
    }

    static class PaymentViewHolder extends RecyclerView.ViewHolder {
        private TextView courseTitle, amount, paymentDate, paymentStatus;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            courseTitle = itemView.findViewById(R.id.course_title);
            amount = itemView.findViewById(R.id.amount);
            paymentDate = itemView.findViewById(R.id.payment_date);
            paymentStatus = itemView.findViewById(R.id.payment_status);
        }

        public void bind(Payment payment) {
            courseTitle.setText(payment.getCourseTitle());
            amount.setText("₹" + payment.getAmount());
            paymentStatus.setText(payment.getStatus());
            
            if (payment.getPaymentDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                paymentDate.setText(sdf.format(payment.getPaymentDate()));
            }
        }
    }
}