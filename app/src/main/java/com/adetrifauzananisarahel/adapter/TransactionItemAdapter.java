package com.adetrifauzananisarahel.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.adetrifauzananisarahel.R;
import com.adetrifauzananisarahel.model.CartItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TransactionItemAdapter extends RecyclerView.Adapter<TransactionItemAdapter.ViewHolder> {
    private final List<CartItem> items;

    public TransactionItemAdapter(List<CartItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView qty, name, price;

        ViewHolder(@NonNull View v) {
            super(v);
            qty = v.findViewById(R.id.text_item_quantity);
            name = v.findViewById(R.id.text_item_name);
            price = v.findViewById(R.id.text_item_price);
        }

        void bind(CartItem item) {
            qty.setText(item.getQuantity() + "x");
            name.setText(item.getProductName());

            // Format harga agar lebih rapi
            try {
                Locale localeID = new Locale("in", "ID");
                NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(localeID);
                currencyFormatter.setMaximumFractionDigits(0);
                double priceValue = Double.parseDouble(item.getProductPrice().replaceAll("[^\\d]", ""));
                price.setText(currencyFormatter.format(priceValue));
            } catch (Exception e) {
                price.setText("Rp " + item.getProductPrice());
            }
        }
    }
}