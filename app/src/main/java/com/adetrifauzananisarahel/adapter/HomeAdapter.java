package com.adetrifauzananisarahel.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.adetrifauzananisarahel.R;
import com.adetrifauzananisarahel.model.CartItem;
import com.adetrifauzananisarahel.model.MenuItemResponse;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ProductViewHolder> {

    private final List<Object> list;
    private final OnItemClickListener listener;
    private static Map<Integer, CartItem> cartItems = new HashMap<>();

    // Interface untuk menangani klik
    public interface OnItemClickListener {
        void onItemDetailClick(MenuItemResponse item);
        void onAddItemClick(MenuItemResponse item);
        void onIncreaseItemClick(MenuItemResponse item); // Baru
        void onDecreaseItemClick(MenuItemResponse item); // Baru
    }
    // 2. Buat method untuk update data keranjang dari Fragment
    public void updateCartItems(Map<Integer, CartItem> newCartItems) {
        this.cartItems = newCartItems;
        notifyDataSetChanged(); // Perbarui seluruh tampilan
    }

    public HomeAdapter(List<Object> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Menggunakan layout item_product.xml yang baru dibuat
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Object item = list.get(position);

        // Cek apakah objek adalah instance dari MenuItemResponse
        if (list.get(position) instanceof MenuItemResponse) {
            holder.bind((MenuItemResponse) list.get(position), listener, cartItems); // Kirim cartItems
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ViewHolder khusus untuk menampilkan produk
    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName;
        TextView tvProductDescription;
        TextView tvProductPrice;
        MaterialButton btnAddToCart;

        ConstraintLayout quantitySelectorGroup;
        MaterialButton btnIncreaseQuantity;
        MaterialButton btnDecreaseQuantity;
        TextView tvQuantity;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            // Inisialisasi view dari layout item_product.xml
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);

            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
            quantitySelectorGroup = itemView.findViewById(R.id.quantity_selector_group);
            btnIncreaseQuantity = itemView.findViewById(R.id.btn_increase_quantity);
            btnDecreaseQuantity = itemView.findViewById(R.id.btn_decrease_quantity);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
        }

        // Method untuk binding data ke view
        public void bind(final MenuItemResponse menuItem, final OnItemClickListener listener, Map<Integer, CartItem> cartItems) {
            tvProductName.setText(menuItem.getNamaProduk());
            // ... (kode format harga & Glide tetap sama)

            Locale localeID = new Locale("in", "ID");
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(localeID);
            currencyFormatter.setMaximumFractionDigits(0);

            tvProductPrice.setText(currencyFormatter.format(menuItem.getHarga()));

            Glide.with(itemView.getContext())
                    .load(menuItem.getGambarUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(ivProductImage);

            // Listener untuk seluruh kartu (pindah ke detail)
            itemView.setOnClickListener(v -> listener.onItemDetailClick(menuItem));

            // Listener KHUSUS untuk tombol "Tambah"

            CartItem itemInCart = HomeAdapter.cartItems.get(menuItem.getId());

            if (itemInCart != null) {
                // Jika item ADA di keranjang
                btnAddToCart.setVisibility(View.GONE);
                quantitySelectorGroup.setVisibility(View.VISIBLE);
                tvQuantity.setText(String.valueOf(itemInCart.getQuantity()));
            } else {
                // Jika item TIDAK ADA di keranjang
                btnAddToCart.setVisibility(View.VISIBLE);
                quantitySelectorGroup.setVisibility(View.GONE);
            }

            // Setup listener
            itemView.setOnClickListener(v -> listener.onItemDetailClick(menuItem));
            btnAddToCart.setOnClickListener(v -> listener.onAddItemClick(menuItem));
            btnIncreaseQuantity.setOnClickListener(v -> listener.onIncreaseItemClick(menuItem));
            btnDecreaseQuantity.setOnClickListener(v -> listener.onDecreaseItemClick(menuItem));
        }
    }
}