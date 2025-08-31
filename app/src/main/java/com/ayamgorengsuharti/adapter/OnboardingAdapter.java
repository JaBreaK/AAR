package com.ayamgorengsuharti.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ayamgorengsuharti.R; // <-- PASTIKAN IMPORT INI YANG DIPAKAI
import com.ayamgorengsuharti.model.OnboardingItem;
import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.ViewHolder> {

    private final List<OnboardingItem> onboardingItems;

    public OnboardingAdapter(List<OnboardingItem> onboardingItems) {
        this.onboardingItems = onboardingItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_onboarding_slide, parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(onboardingItems.get(position));
    }

    @Override
    public int getItemCount() {
        return onboardingItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivImage;
        private TextView tvTitle;
        private TextView tvDescription;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_onboarding_image);
            tvTitle = itemView.findViewById(R.id.tv_onboarding_title);
            tvDescription = itemView.findViewById(R.id.tv_onboarding_description);
        }

        void bind(OnboardingItem item) {
            ivImage.setImageResource(item.getImage());
            tvTitle.setText(item.getTitle());
            tvDescription.setText(item.getDescription());
        }
    }
}