package com.ayamgorengsuharti.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.ayamgorengsuharti.R;
import com.ayamgorengsuharti.model.CartItem;
import com.ayamgorengsuharti.model.MenuItemResponse;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HomeAdapter extends ListAdapter<MenuItemResponse, HomeAdapter.ProductViewHolder> {

    private final OnItemClickListener listener;
    private Map<Integer, CartItem> cartItems = new HashMap<>();

    public interface OnItemClickListener {
        void onItemDetailClick(MenuItemResponse item);
        void onAddItemClick(MenuItemResponse item);
        void onIncreaseItemClick(MenuItemResponse item);
        void onDecreaseItemClick(MenuItemResponse item);
    }

    private static final DiffUtil.ItemCallback<MenuItemResponse> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<MenuItemResponse>() {
                @Override
                public boolean areItemsTheSame(@NonNull MenuItemResponse oldItem, @NonNull MenuItemResponse newItem) {
                    return oldItem.getId() == newItem.getId();
                }
                @Override
                public boolean areContentsTheSame(@NonNull MenuItemResponse oldItem, @NonNull MenuItemResponse newItem) {
                    return oldItem.getNamaProduk().equals(newItem.getNamaProduk());
                }
            };

    public HomeAdapter(OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    public void updateCartItems(Map<Integer, CartItem> newCartItems) {
        this.cartItems = newCartItems;
        // Gunakan cara ini untuk "memaksa" update pada item yang terlihat
        notifyItemRangeChanged(0, getItemCount());
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        MenuItemResponse currentItem = getItem(position);
        if (currentItem != null) {
            holder.bind(currentItem, listener, cartItems);
        }
    }

    // ==========================================================
    // == INI BAGIAN YANG DIPERBAIKI ==
    // ==========================================================
    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName;
        TextView tvProductPrice;
        MaterialButton btnAddToCart;
        ConstraintLayout quantitySelectorGroup;
        MaterialButton btnIncreaseQuantity;
        MaterialButton btnDecreaseQuantity;
        TextView tvQuantity;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            // Inisialisasi semua view dari layout item_product.xml yang baru
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
            quantitySelectorGroup = itemView.findViewById(R.id.quantity_selector_group);
            btnIncreaseQuantity = itemView.findViewById(R.id.btn_increase_quantity);
            btnDecreaseQuantity = itemView.findViewById(R.id.btn_decrease_quantity);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
        }

        public void bind(final MenuItemResponse menuItem, final OnItemClickListener listener, Map<Integer, CartItem> cartItems) {
            // Set data ke view
            tvProductName.setText(menuItem.getNamaProduk());

            Locale localeID = new Locale("in", "ID");
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(localeID);
            currencyFormatter.setMaximumFractionDigits(0);
            tvProductPrice.setText(currencyFormatter.format(menuItem.getHarga()));

            Glide.with(itemView.getContext())
                    .load(menuItem.getGambarUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(ivProductImage);

            // Logika untuk menampilkan tombol "Tambah" atau quantity selector
            CartItem itemInCart = cartItems.get(menuItem.getId());
            if (itemInCart != null) {
                btnAddToCart.setVisibility(View.GONE);
                quantitySelectorGroup.setVisibility(View.VISIBLE);
                tvQuantity.setText(String.valueOf(itemInCart.getQuantity()));
            } else {
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