package com.adetrifauzananisarahel.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.adetrifauzananisarahel.R;
import com.adetrifauzananisarahel.model.OrderItem;
import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.ViewHolder> {

    private List<OrderItem> items = new ArrayList<>();

    // Method untuk mengirimkan daftar item baru ke adapter
    public void submitList(List<OrderItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_detail, parent, false);
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

    // ViewHolder untuk menampung view dari satu baris item
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName;
        TextView tvQuantityAndPrice;
        TextView tvSubtotal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvQuantityAndPrice = itemView.findViewById(R.id.tv_quantity_and_price);
            tvSubtotal = itemView.findViewById(R.id.tv_subtotal);
        }

        public void bind(OrderItem item) {
            // Set nama produk
            tvProductName.setText(item.getProduk().getNamaProduk());

            // Set detail jumlah dan harga satuan
            String quantityText = item.getJumlah() + " x " + formatRupiah(item.getProduk().getHarga());
            tvQuantityAndPrice.setText(quantityText);

            // Set subtotal
            tvSubtotal.setText(formatRupiah(item.getSubtotal()));

            // Load gambar produk menggunakan Glide
            Glide.with(itemView.getContext())
                    .load(item.getProduk().getGambarUrl())
                    .placeholder(R.drawable.ic_launcher_background) // Ganti dengan placeholder-mu
                    .into(ivProductImage);
        }

        // Helper method untuk format angka ke Rupiah
        private String formatRupiah(double number) {
            Locale localeID = new Locale("in", "ID");
            NumberFormat format = NumberFormat.getCurrencyInstance(localeID);
            format.setMaximumFractionDigits(0);
            return format.format(number);
        }
    }
}