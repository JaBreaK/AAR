package com.ayamgorengsuharti.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ayamgorengsuharti.R;
import com.ayamgorengsuharti.model.CartItem;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartSheetAdapter extends RecyclerView.Adapter<CartSheetAdapter.CartViewHolder> {

    private List<CartItem> cartItems = new ArrayList<>();
    private final CartListener listener;

    public interface CartListener {
        void onIncrease(CartItem item);
        void onDecrease(CartItem item);
        void onRemove(CartItem item);
    }

    public CartSheetAdapter(CartListener listener) {
        this.listener = listener;
    }

    public void submitList(List<CartItem> newCartItems) {
        this.cartItems = newCartItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        holder.bind(cartItems.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvPrice, tvQuantity;
        MaterialButton btnIncrease, btnDecrease;
        ImageButton btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_cart_item_image);
            tvName = itemView.findViewById(R.id.tv_cart_item_name);
            tvPrice = itemView.findViewById(R.id.tv_cart_item_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity_cart);
            btnIncrease = itemView.findViewById(R.id.btn_increase_quantity_cart);
            btnDecrease = itemView.findViewById(R.id.btn_decrease_quantity_cart);
            btnRemove = itemView.findViewById(R.id.btn_remove_item);
        }

        public void bind(final CartItem item, final CartListener listener) {
            tvName.setText(item.getProduct().getNamaProduk());
            tvQuantity.setText(String.valueOf(item.getQuantity()));

            Locale localeID = new Locale("in", "ID");
            NumberFormat format = NumberFormat.getCurrencyInstance(localeID);
            format.setMaximumFractionDigits(0);
            tvPrice.setText(format.format(item.getProduct().getHarga()));

            Glide.with(itemView.getContext()).load(item.getProduct().getGambarUrl()).into(ivImage);

            btnIncrease.setOnClickListener(v -> listener.onIncrease(item));
            btnDecrease.setOnClickListener(v -> listener.onDecrease(item));
            btnRemove.setOnClickListener(v -> listener.onRemove(item));
        }
    }
}