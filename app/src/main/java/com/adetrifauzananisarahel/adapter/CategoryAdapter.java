package com.adetrifauzananisarahel.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // IMPORT INI
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.adetrifauzananisarahel.R;
import com.adetrifauzananisarahel.model.Category;
import com.bumptech.glide.Glide; // IMPORT INI
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private final List<Category> categories;
    public CategoryAdapter(List<Category> categories) { this.categories = categories; }

    @NonNull @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Layoutnya sudah kita ubah di langkah sebelumnya
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        holder.bind(categories.get(position));
    }

    @Override public int getItemCount() { return categories.size(); }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView categoryName;
        private final ImageView categoryImage; // TAMBAHKAN INI

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.text_view_category_name);
            categoryImage = itemView.findViewById(R.id.image_view_category); // TAMBAHKAN INI
        }
        void bind(Category category) {
            categoryName.setText(category.getName());
            // Gunakan Glide untuk memuat gambar
            Glide.with(itemView.getContext())
                    .load(category.getImageResource())
                    .into(categoryImage);
        }
    }
}