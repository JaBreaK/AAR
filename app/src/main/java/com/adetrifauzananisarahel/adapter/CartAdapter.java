package com.adetrifauzananisarahel.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.adetrifauzananisarahel.R;
import com.adetrifauzananisarahel.model.CartItem;
import com.bumptech.glide.Glide;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<CartItem> cartItems;
    private final CartInteractionListener listener;

    public interface CartInteractionListener {
        void onIncreaseQuantity(CartItem item);
        void onDecreaseQuantity(CartItem item);
        void onRemoveItem(CartItem item);
    }

    public CartAdapter(List<CartItem> cartItems, CartInteractionListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textName, textPrice, textQuantity;
        ImageButton btnIncrease, btnDecrease, btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view_cart_product);
            textName = itemView.findViewById(R.id.text_view_cart_product_name);
            textPrice = itemView.findViewById(R.id.text_view_cart_product_price);
            textQuantity = itemView.findViewById(R.id.text_view_quantity);
            btnIncrease = itemView.findViewById(R.id.button_increase_quantity);
            btnDecrease = itemView.findViewById(R.id.button_decrease_quantity);
            btnRemove = itemView.findViewById(R.id.button_remove_item);
        }

        public void bind(final CartItem item, final CartInteractionListener listener) {
            textName.setText(item.getProductName());
            textPrice.setText("Rp " + item.getProductPrice());
            textQuantity.setText(String.valueOf(item.getQuantity()));

            Glide.with(itemView.getContext()).load(item.getImagePath()).into(imageView);

            btnIncrease.setOnClickListener(v -> listener.onIncreaseQuantity(item));
            btnDecrease.setOnClickListener(v -> listener.onDecreaseQuantity(item));
            btnRemove.setOnClickListener(v -> listener.onRemoveItem(item));

            // Tombol minus tidak bisa ditekan jika jumlahnya 1
            btnDecrease.setEnabled(item.getQuantity() > 1);
        }
    }
}