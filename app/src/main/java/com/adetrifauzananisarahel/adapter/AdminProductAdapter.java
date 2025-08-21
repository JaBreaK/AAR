package com.adetrifauzananisarahel.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.adetrifauzananisarahel.R;
import com.adetrifauzananisarahel.model.Product;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ProductViewHolder> {

    private final List<Product> productList;
    private final OnItemClickListener listener;
    private final Context context;

    // Interface untuk menangani klik
    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    public AdminProductAdapter(Context context, List<Product> productList, OnItemClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Kita bisa pakai layout item_product yang sudah ada
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product, listener);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewProduct;
        TextView textViewName, textViewPrice;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.image_view_product);
            textViewName = itemView.findViewById(R.id.text_view_product_name);
            textViewPrice = itemView.findViewById(R.id.text_view_product_price);
        }

        public void bind(final Product product, final OnItemClickListener listener) {
            textViewName.setText(product.getName());
            textViewPrice.setText(product.getPrice());

            // Muat gambar dari file path menggunakan Glide
            if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
                Glide.with(context)
                        .load(new File(product.getImagePath())) // Load dari File
                        .placeholder(R.drawable.pp) // Gambar default saat loading
                        .into(imageViewProduct);
            }

            // Set listener klik pada seluruh item view
            itemView.setOnClickListener(v -> listener.onItemClick(product));
        }
    }
}