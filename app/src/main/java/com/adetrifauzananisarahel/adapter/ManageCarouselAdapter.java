package com.adetrifauzananisarahel.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.adetrifauzananisarahel.R;
import com.adetrifauzananisarahel.model.CarouselItem;
import com.bumptech.glide.Glide;

import java.util.List;

public class ManageCarouselAdapter extends RecyclerView.Adapter<ManageCarouselAdapter.CarouselViewHolder> {

    private final List<CarouselItem> carouselItems;
    private OnCarouselItemInteractionListener listener;

    public interface OnCarouselItemInteractionListener {
        void onDeleteClick(CarouselItem item);
    }

    public ManageCarouselAdapter(List<CarouselItem> carouselItems, OnCarouselItemInteractionListener listener) {
        this.carouselItems = carouselItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CarouselViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_carousel, parent, false);
        return new CarouselViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarouselViewHolder holder, int position) {
        CarouselItem item = carouselItems.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return carouselItems.size();
    }

    static class CarouselViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewCarousel;
        ImageButton buttonDelete;

        public CarouselViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewCarousel = itemView.findViewById(R.id.image_view_carousel);
            buttonDelete = itemView.findViewById(R.id.button_delete_carousel);
        }

        public void bind(final CarouselItem item, final OnCarouselItemInteractionListener listener) {
            Glide.with(itemView.getContext())
                    .load(item.getImagePath())
                    .centerCrop()
                    .into(imageViewCarousel);

            buttonDelete.setOnClickListener(v -> listener.onDeleteClick(item));
        }
    }
}