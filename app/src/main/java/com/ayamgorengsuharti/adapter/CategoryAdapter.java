package com.ayamgorengsuharti.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.ayamgorengsuharti.R;
import com.ayamgorengsuharti.model.Category;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<Category> categories = new ArrayList<>();
    private final OnCategoryClickListener listener;
    private int selectedCategoryId = 0;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Category> newCategories) {
        categories.clear();
        categories.add(new Category(0, "Semua"));
        categories.addAll(newCategories);
        notifyDataSetChanged();
    }

    public void setSelectedCategoryId(int categoryId) {
        this.selectedCategoryId = categoryId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category currentCategory = categories.get(position);
        holder.bind(currentCategory, listener, currentCategory.getId() == selectedCategoryId);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView ivIcon;
        TextView tvName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            ivIcon = itemView.findViewById(R.id.iv_category_icon);
            tvName = itemView.findViewById(R.id.tv_category_name);
        }

        public void bind(final Category category, final OnCategoryClickListener listener, boolean isSelected) {
            tvName.setText(category.getNamaKategori());
            Context context = itemView.getContext();

            // --- LOGIKA IKON BARU MENGGUNAKAN GAMBAR PNG/JPG ---
            // Pastikan nama file di sini SAMA PERSIS dengan nama file gambarmu
            switch (category.getNamaKategori().toLowerCase()) {
                case "makanan":
                    ivIcon.setImageResource(R.drawable.makanan);
                    break;
                case "minuman":
                    ivIcon.setImageResource(R.drawable.minuman);
                    break;
                case "camilan":
                    ivIcon.setImageResource(R.drawable.cemilan);
                    break;
                case "paket hemat":
                    ivIcon.setImageResource(R.drawable.pakethemat);
                    break;
                case "semua":
                default:
                    ivIcon.setImageResource(R.drawable.ic_onboarding_1);
                    break;
            }

            if (isSelected) {
                cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.orange_primary));
                tvName.setTextColor(ContextCompat.getColor(context, R.color.black));
                // Opsional: Jika gambarmu satu warna, setColorFilter akan mengubah warnanya jadi putih
                // Jika gambarmu berwarna, hapus baris ini agar warnanya tidak aneh

            } else {
                cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.light_orange_bg));
                tvName.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
                // Menghapus filter warna agar kembali ke warna asli gambar
                ivIcon.clearColorFilter();
            }

            itemView.setOnClickListener(v -> listener.onCategoryClick(category));
        }
    }
}