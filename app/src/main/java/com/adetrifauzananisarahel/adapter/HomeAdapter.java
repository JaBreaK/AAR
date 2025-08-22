package com.adetrifauzananisarahel.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.adetrifauzananisarahel.R;
import com.adetrifauzananisarahel.model.MenuCategory;
import com.adetrifauzananisarahel.model.FoodItem;
import com.bumptech.glide.Glide;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Object> homeList;

    // =========================================================================
    // LANGKAH 1: Buat interface untuk menangani klik
    // =========================================================================
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(FoodItem item);
    }
    // =========================================================================

    private static final int VIEW_TYPE_CATEGORY = 1;
    private static final int VIEW_TYPE_ITEM = 2;

    // =========================================================================
    // LANGKAH 2: Modifikasi constructor untuk menerima listener
    // =========================================================================
    public HomeAdapter(List<Object> homeList, OnItemClickListener listener) {
        this.homeList = homeList;
        this.listener = listener;
    }
    // =========================================================================

    @Override
    public int getItemViewType(int position) {
        if (homeList.get(position) instanceof MenuCategory) {
            return VIEW_TYPE_CATEGORY;
        } else if (homeList.get(position) instanceof FoodItem) {
            return VIEW_TYPE_ITEM;
        }
        return -1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_CATEGORY) {
            View view = inflater.inflate(R.layout.item_category_header, parent, false);
            return new CategoryViewHolder(view);
        } else { // VIEW_TYPE_ITEM
            View view = inflater.inflate(R.layout.item_menu, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_CATEGORY) {
            MenuCategory category = (MenuCategory) homeList.get(position);
            ((CategoryViewHolder) holder).bind(category);
        } else {
            FoodItem item = (FoodItem) homeList.get(position);
            // =========================================================================
            // LANGKAH 3: Panggil listener saat item di-bind ke ViewHolder
            // =========================================================================
            ((ItemViewHolder) holder).bind(item, listener);
            // =========================================================================
        }
    }

    @Override
    public int getItemCount() {
        return homeList.size();
    }

    // ViewHolder untuk Judul Kategori (Tidak ada perubahan)
    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryTitle;
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryTitle = itemView.findViewById(R.id.tv_category_title);
        }
        void bind(MenuCategory category) {
            tvCategoryTitle.setText(category.getName());
        }
    }

    // ViewHolder untuk Item Menu
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMenuImage;
        TextView tvMenuName, tvMenuDescription, tvMenuPrice;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMenuImage = itemView.findViewById(R.id.iv_menu_image);
            tvMenuName = itemView.findViewById(R.id.tv_menu_name);
            tvMenuDescription = itemView.findViewById(R.id.tv_menu_description);
            tvMenuPrice = itemView.findViewById(R.id.tv_menu_price);
        }

        // Modifikasi method bind untuk menerima listener
        void bind(FoodItem item, OnItemClickListener listener) {
            tvMenuName.setText(item.getName());
            tvMenuDescription.setText(item.getDescription());

            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
            tvMenuPrice.setText(formatter.format(item.getPrice()));

            Glide.with(itemView.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.buah)
                    .into(ivMenuImage);

            // Tambahkan OnClickListener di sini
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}