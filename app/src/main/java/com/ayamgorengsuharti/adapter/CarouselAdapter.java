package com.ayamgorengsuharti.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ayamgorengsuharti.R;
import com.ayamgorengsuharti.model.Promo;
// import com.bumptech.glide.Glide; // Glide sudah tidak perlu
import java.util.ArrayList;
import java.util.List;

public class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.ViewHolder> {

    private List<Promo> promos = new ArrayList<>();

    public void submitList(List<Promo> newPromos) {
        this.promos = newPromos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carousel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(promos.get(position));
    }

    @Override
    public int getItemCount() {
        return promos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_carousel_image);
        }

        public void bind(Promo promo) {
            // Ganti Glide dengan setImageResource
            ivImage.setImageResource(promo.getImageResId());
        }
    }
}