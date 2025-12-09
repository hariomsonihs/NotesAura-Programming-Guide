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

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.ViewHolder> {
    private List<Payment> payments;

    public PaymentAdapter(List<Payment> payments) {
        this.payments = payments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Payment payment = payments.get(position);
        holder.bind(payment);
    }

    @Override
    public int getItemCount() {
        return payments.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView courseName, transactionId, amount, paymentDate, status;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            courseName = itemView.findViewById(R.id.course_name);
            transactionId = itemView.findViewById(R.id.transaction_id);
            amount = itemView.findViewById(R.id.amount);
            paymentDate = itemView.findViewById(R.id.payment_date);
            status = itemView.findViewById(R.id.status);
        }

        void bind(Payment payment) {
            courseName.setText(payment.getCourseTitle());
            transactionId.setText("TXN ID: " + payment.getTransactionId());
            amount.setText("₹" + String.format("%.0f", payment.getAmount()));
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            paymentDate.setText(sdf.format(payment.getPaymentDate()));
            
            status.setText(payment.getStatus());
            
            // Set status color
            int statusColor;
            switch (payment.getStatus()) {
                case "SUCCESS":
                    statusColor = itemView.getContext().getColor(R.color.accent_green);
                    break;
                case "FAILED":
                    statusColor = itemView.getContext().getColor(R.color.accent_red);
                    break;
                default:
                    statusColor = itemView.getContext().getColor(R.color.accent_orange);
                    break;
            }
            status.setTextColor(statusColor);
        }
    }
}