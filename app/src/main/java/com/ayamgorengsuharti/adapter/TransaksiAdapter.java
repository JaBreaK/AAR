package com.ayamgorengsuharti.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ayamgorengsuharti.R;
import com.ayamgorengsuharti.model.Order;
import com.ayamgorengsuharti.model.OrderItem;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransaksiAdapter extends RecyclerView.Adapter<TransaksiAdapter.ViewHolder> {

    private List<Order> orders = new ArrayList<>();
    private final OnOrderClickListener listener;

    // Interface untuk menangani klik pada item
    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public TransaksiAdapter(OnOrderClickListener listener) {
        this.listener = listener;
    }

    // Method untuk mengupdate daftar pesanan
    public void submitList(List<Order> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaksi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(orders.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvOrderStatus, tvOrderItemsPreview, tvOrderTotal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvOrderDate = itemView.findViewById(R.id.tv_order_date);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvOrderItemsPreview = itemView.findViewById(R.id.tv_order_items_preview);
            tvOrderTotal = itemView.findViewById(R.id.tv_order_total);
        }

        public void bind(final Order order, final OnOrderClickListener listener) {
            // Set data dasar
            tvOrderId.setText("Pesanan #" + order.getId());
            tvOrderDate.setText(formatDateTime(order.getWaktuOrder()));
            tvOrderTotal.setText(formatRupiah(order.getTotalHarga()));

            // Set Teks dan Warna Status Pembayaran
            String statusText = order.getStatusPembayaran().replace("_", " ");
            tvOrderStatus.setText(statusText);

            Context context = itemView.getContext();
            GradientDrawable statusBackground = (GradientDrawable) tvOrderStatus.getBackground();

            switch (order.getStatusPembayaran()) {
                case "LUNAS":
                    statusBackground.setColor(ContextCompat.getColor(context, R.color.green)); // Buat R.color.green
                    break;
                case "DIBATALKAN":
                    statusBackground.setColor(ContextCompat.getColor(context, R.color.red)); // Buat R.color.red
                    break;
                case "BELUM_BAYAR":
                default:
                    statusBackground.setColor(ContextCompat.getColor(context, R.color.yellow)); // Buat R.color.yellow
                    break;
            }

            // Buat ringkasan item
            StringBuilder itemsPreview = new StringBuilder();
            List<OrderItem> orderItems = order.getOrderItems();
            if (orderItems != null && !orderItems.isEmpty()) {
                for (int i = 0; i < orderItems.size(); i++) {
                    OrderItem item = orderItems.get(i);
                    itemsPreview.append(item.getJumlah())
                            .append("x ")
                            .append(item.getProduk().getNamaProduk());
                    if (i < orderItems.size() - 1) {
                        itemsPreview.append(", ");
                    }
                }
            }
            tvOrderItemsPreview.setText(itemsPreview.toString());

            // Set listener klik untuk pindah ke detail
            itemView.setOnClickListener(v -> listener.onOrderClick(order));
        }

        // Helper method untuk format angka ke Rupiah
        private String formatRupiah(double number) {
            Locale localeID = new Locale("in", "ID");
            NumberFormat format = NumberFormat.getCurrencyInstance(localeID);
            format.setMaximumFractionDigits(0);
            return format.format(number);
        }

        // Helper method untuk format tanggal
        private String formatDateTime(String isoDate) {
            if (isoDate == null) return "-";
            try {
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                Date date = isoFormat.parse(isoDate);
                // Format ke "27 Agu 2025, 12:04"
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("id", "ID"));
                return outputFormat.format(date);
            } catch (Exception e) {
                return isoDate;
            }
        }
    }
}