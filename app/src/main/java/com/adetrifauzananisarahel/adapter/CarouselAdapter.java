package com.adetrifauzananisarahel.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.adetrifauzananisarahel.R;
import com.adetrifauzananisarahel.model.CarouselItem;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

public class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder> {

    // Nama variabelnya adalah 'items'
    private final List<CarouselItem> items;

    public CarouselAdapter(List<CarouselItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public CarouselViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carousel, parent, false);
        return new CarouselViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarouselViewHolder holder, int position) {
        // Gunakan variabel 'items', bukan 'carouselItems'
        CarouselItem item = items.get(position);

        // Logika Glide sudah benar, kita pertahankan
        Glide.with(holder.itemView.getContext())
                .load(new File(item.getImagePath()))
                .placeholder(R.drawable.pp) // Ganti dengan placeholder yang sesuai
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolder yang sudah dirapikan
    static class CarouselViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView; // Dibuat public atau package-private agar bisa diakses dari onBindViewHolder

        CarouselViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view_carousel);
        }
        // Method 'bind' yang salah dan tidak terpakai sudah dihapus
    }
}