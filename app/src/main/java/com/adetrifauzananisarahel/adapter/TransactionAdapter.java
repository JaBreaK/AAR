package com.adetrifauzananisarahel.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.adetrifauzananisarahel.R;
import com.adetrifauzananisarahel.model.Transaction;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private final List<Transaction> transactionList;
    private final OnTransactionClickListener listener;

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
    }

    public TransactionAdapter(List<Transaction> transactionList, OnTransactionClickListener listener) {
        this.transactionList = transactionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.bind(transaction, listener);
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView textDate, textTrxId, textTotal;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            textDate = itemView.findViewById(R.id.text_view_trx_date);
            textTrxId = itemView.findViewById(R.id.text_view_trx_id);
            textTotal = itemView.findViewById(R.id.text_view_trx_total);
        }

        void bind(final Transaction transaction, final OnTransactionClickListener listener) {
            // Format Tanggal
            try {
                SimpleDateFormat sdfDb = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date = sdfDb.parse(transaction.getTimestamp());
                SimpleDateFormat sdfDisplay = new SimpleDateFormat("dd MMMM yyyy", new Locale("in", "ID"));
                textDate.setText(sdfDisplay.format(date));
            } catch (Exception e) {
                textDate.setText(transaction.getTimestamp());
            }

            // Format Invoice ID
            textTrxId.setText("INV/" + transaction.getTimestamp().replaceAll("[^\\d]", "") + "/" + transaction.getId());

            // Format Mata Uang
            Locale localeID = new Locale("in", "ID");
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(localeID);
            currencyFormatter.setMaximumFractionDigits(0);
            textTotal.setText(currencyFormatter.format(transaction.getTotalPrice()));

            itemView.setOnClickListener(v -> listener.onTransactionClick(transaction));
        }
    }
}