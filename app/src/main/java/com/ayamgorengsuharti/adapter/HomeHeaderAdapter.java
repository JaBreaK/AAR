package com.ayamgorengsuharti.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.ayamgorengsuharti.databinding.LayoutHomeHeaderBinding;
import com.ayamgorengsuharti.model.Category;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HomeHeaderAdapter extends RecyclerView.Adapter<HomeHeaderAdapter.ViewHolder> {

    private final CategoryAdapter.OnCategoryClickListener categoryListener;

    // --- BARU: Interface callback untuk setup ---
    private final Consumer<TextInputEditText> searchListenerSetup;
    private final Consumer<ViewPager2> carouselListenerSetup;

    private CategoryAdapter categoryAdapter;
    private ViewHolder holder; // Simpan referensi ke ViewHolder
    private List<Category> pendingCategories = new ArrayList<>();
    private int pendingSelectedCategoryId = 0;



    // --- BARU: Konstruktor yang benar ---
    public HomeHeaderAdapter(
            CategoryAdapter.OnCategoryClickListener categoryListener,
            Consumer<TextInputEditText> searchListenerSetup,
            Consumer<ViewPager2> carouselListenerSetup) {
        this.categoryListener = categoryListener;
        this.searchListenerSetup = searchListenerSetup;
        this.carouselListenerSetup = carouselListenerSetup;
    }

    // --- DIUBAH: Method ini sekarang menyimpan data ke penampung ---
    public void submitCategories(List<Category> categories) {
        this.pendingCategories = categories;
        // Jika view sudah ada, langsung update. Jika belum, data akan menunggu.
        if (categoryAdapter != null) {
            categoryAdapter.submitList(pendingCategories);
        }
    }

    // --- DIUBAH: Method ini juga menyimpan data ke penampung ---
    public void updateSelectedCategory(int categoryId) {
        this.pendingSelectedCategoryId = categoryId;
        if (categoryAdapter != null) {
            categoryAdapter.setSelectedCategoryId(pendingSelectedCategoryId);
        }
    }

    // --- BARU: Method untuk mendapatkan query pencarian ---
    public String getCurrentSearchQuery() {
        if (holder != null && holder.binding.etSearch != null) {
            return holder.binding.etSearch.getText().toString();
        }
        return "";
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutHomeHeaderBinding binding = LayoutHomeHeaderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding, categoryListener, searchListenerSetup, carouselListenerSetup);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        this.categoryAdapter = holder.getCategoryAdapter();

        // --- BARU: Cek apakah ada data yang "menunggu" untuk ditampilkan ---
        if (!pendingCategories.isEmpty()) {
            categoryAdapter.submitList(pendingCategories);
            categoryAdapter.setSelectedCategoryId(pendingSelectedCategoryId);
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final LayoutHomeHeaderBinding binding;
        private final CategoryAdapter categoryAdapter;

        public ViewHolder(
                LayoutHomeHeaderBinding binding,
                CategoryAdapter.OnCategoryClickListener categoryListener,
                Consumer<TextInputEditText> searchListenerSetup,
                Consumer<ViewPager2> carouselListenerSetup) {
            super(binding.getRoot());
            this.binding = binding;

            // Setup RecyclerView Kategori
            this.categoryAdapter = new CategoryAdapter(categoryListener);
            binding.recyclerViewCategories.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            binding.recyclerViewCategories.setAdapter(this.categoryAdapter);

            // Panggil callback untuk setup search dan carousel di Fragment
            searchListenerSetup.accept(binding.etSearch);
            carouselListenerSetup.accept(binding.viewPagerCarousel);
        }

        public CategoryAdapter getCategoryAdapter() {
            return categoryAdapter;
        }
    }
}