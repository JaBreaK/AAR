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

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    // [MODIFIKASI] Ubah dari 'final' agar bisa diubah isinya oleh method filter
    private List<Product> productList;
    private final Context context;
    private final OnProductClickListener clickListener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(Context context, List<Product> productList, OnProductClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.clickListener = listener;
    }

    // [BARU] Method untuk menerima data hasil filter dan me-refresh RecyclerView
    public void filterList(List<Product> filteredList) {
        // Ganti isi list yang sekarang dengan list hasil filter
        this.productList = filteredList;
        // Beri tahu adapter bahwa datanya telah berubah
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product);
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

        public void bind(final Product product) {
            textViewName.setText(product.getName());
            textViewPrice.setText("Rp " + product.getPrice());

            if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
                Glide.with(context)
                        .load(new File(product.getImagePath()))
                        .placeholder(R.drawable.pp)
                        .error(R.drawable.pp)
                        .into(imageViewProduct);
            } else {
                imageViewProduct.setImageResource(R.drawable.pp);
            }

            itemView.setOnClickListener(v -> clickListener.onProductClick(product));
        }
    }
}