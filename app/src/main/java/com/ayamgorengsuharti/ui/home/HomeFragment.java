package com.ayamgorengsuharti.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.ayamgorengsuharti.R;
import com.ayamgorengsuharti.adapter.CarouselAdapter;
import com.ayamgorengsuharti.adapter.CategoryAdapter;
import com.ayamgorengsuharti.adapter.HomeAdapter;
import com.ayamgorengsuharti.adapter.HomeHeaderAdapter;
import com.ayamgorengsuharti.databinding.FragmentHomeBinding;
import com.ayamgorengsuharti.model.Category;
import com.ayamgorengsuharti.model.MenuItemResponse;
import com.ayamgorengsuharti.model.Promo;
import com.ayamgorengsuharti.network.ApiClient;
import com.ayamgorengsuharti.network.ApiService;
import com.ayamgorengsuharti.ui.product.ProductDetailFragment;
import com.ayamgorengsuharti.viewmodel.CartViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.google.gson.Gson;

public class HomeFragment extends Fragment implements HomeAdapter.OnItemClickListener, CategoryAdapter.OnCategoryClickListener {

    private FragmentHomeBinding binding;
    private HomeAdapter homeAdapter;
    private HomeHeaderAdapter headerAdapter;
    private ConcatAdapter concatAdapter;
    private CartViewModel cartViewModel;

    private final List<MenuItemResponse> allMenuItems = new ArrayList<>();
    private Category selectedCategory = new Category(0, "Semua");

    // Variabel untuk Carousel
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;
    private List<Promo> promoList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);

        setupUI();
        fetchMenuData();

        // --- PASTIKAN BLOK KODE INI ADA DAN LENGKAP ---
        cartViewModel.getCartItems().observe(getViewLifecycleOwner(), cartItems -> {
            if (homeAdapter != null) {
                // Panggil methodnya di sini
                homeAdapter.updateCartItems(cartItems);
            }
        });
        // --- BARU: Observer untuk mengatur padding ---
        cartViewModel.getIsCartCapsuleVisible().observe(getViewLifecycleOwner(), isVisible -> {
            if (binding == null) return; // Safety check

            // Dapatkan padding yang sudah ada agar tidak hilang
            int paddingTop = binding.recyclerViewHome.getPaddingTop();
            int paddingLeft = binding.recyclerViewHome.getPaddingLeft();
            int paddingRight = binding.recyclerViewHome.getPaddingRight();

            if (isVisible) {
                // Kapsul muncul, tambah padding bawah sekitar 88dp
                binding.recyclerViewHome.setPadding(paddingLeft, paddingTop, paddingRight, dpToPx(88));
            } else {
                // Kapsul hilang, kembalikan padding bawah ke nilai default (misal: 16dp)
                binding.recyclerViewHome.setPadding(paddingLeft, paddingTop, paddingRight, dpToPx(0));
            }
        });
    }
    // --- BARU: Helper method untuk konversi dp ke pixel ---
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    private void setupUI() {
        // 1. Inisialisasi setiap adapter
        // HomeHeaderAdapter sekarang akan meng-handle setup listener-nya sendiri
        headerAdapter = new HomeHeaderAdapter(this, this::setupSearchListener, this::setupCarousel);
        homeAdapter = new HomeAdapter(this);

        // 2. Gabungkan keduanya dengan ConcatAdapter
        concatAdapter = new ConcatAdapter(headerAdapter, homeAdapter);

        // 3. Atur GridLayoutManager agar header punya lebar penuh
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                // Jika item adalah header (posisi 0), beri lebar 2 span (penuh)
                // Jika bukan (menu item), beri lebar 1 span
                return position == 0 ? 2 : 1;
            }
        });

        // 4. Set layout manager dan adapter penggabung ke RecyclerView utama
        binding.recyclerViewHome.setLayoutManager(layoutManager);
        binding.recyclerViewHome.setAdapter(concatAdapter);
    }

    // Method ini akan dipanggil dari dalam Header Adapter
    private void setupSearchListener(TextInputEditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMenuItems(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Method ini juga akan dipanggil dari dalam Header Adapter
    private void setupCarousel(ViewPager2 viewPager) {
        promoList.clear();
        promoList.add(new Promo(1, "Promo 1", R.drawable.ayam3));
        promoList.add(new Promo(1, "Promo 2", R.drawable.ayam4));


        CarouselAdapter carouselAdapter = new CarouselAdapter();
        viewPager.setAdapter(carouselAdapter);
        carouselAdapter.submitList(promoList);

        viewPager.setClipToPadding(false);
        viewPager.setClipChildren(false);
        viewPager.setOffscreenPageLimit(3);
        viewPager.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(40));
        transformer.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            page.setScaleY(0.85f + r * 0.15f);
        });
        viewPager.setPageTransformer(transformer);

        sliderRunnable = () -> {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem == promoList.size() - 1) {
                viewPager.setCurrentItem(0);
            } else {
                viewPager.setCurrentItem(currentItem + 1);
            }
        };
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });
    }

    private void fetchMenuData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = ApiClient.getClient(requireContext()).create(ApiService.class);
        apiService.getMenu().enqueue(new Callback<List<MenuItemResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<MenuItemResponse>> call, @NonNull Response<List<MenuItemResponse>> response) {
                // --- TAMBAHKAN PENGECEKAN INI ---
                if (binding == null) {
                    return; // Jika view sudah dihancurkan, jangan lakukan apa-apa
                }
                // --- AKHIR PENGECEKAN ---

                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    allMenuItems.clear();
                    allMenuItems.addAll(response.body());

                    List<Category> uniqueCategories = extractUniqueCategories(allMenuItems);
                    headerAdapter.submitCategories(uniqueCategories);
                    homeAdapter.submitList(allMenuItems);
                } else {
                    Toast.makeText(getContext(), "Gagal memuat data menu.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<MenuItemResponse>> call, @NonNull Throwable t) {
                // --- TAMBAHKAN PENGECEKAN INI JUGA ---
                if (binding == null) {
                    return;
                }
                // --- AKHIR PENGECEKAN ---

                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<Category> extractUniqueCategories(List<MenuItemResponse> menuItems) {
        HashSet<Integer> categoryIds = new HashSet<>();
        List<Category> uniqueCategories = new ArrayList<>();
        for (MenuItemResponse item : menuItems) {
            if (item.getKategori() != null && !categoryIds.contains(item.getKategori().getId())) {
                categoryIds.add(item.getKategori().getId());
                uniqueCategories.add(item.getKategori());
            }
        }
        Collections.sort(uniqueCategories, Comparator.comparing(Category::getNamaKategori));
        return uniqueCategories;
    }

    private void filterMenuItems(String query) {
        List<MenuItemResponse> categoryFilteredList = new ArrayList<>();
        if (selectedCategory.getId() == 0) {
            categoryFilteredList.addAll(allMenuItems);
        } else {
            for (MenuItemResponse item : allMenuItems) {
                if (item.getKategori() != null && item.getKategori().getId() == selectedCategory.getId()) {
                    categoryFilteredList.add(item);
                }
            }
        }

        List<MenuItemResponse> searchFilteredList = new ArrayList<>();
        if (query.isEmpty()) {
            searchFilteredList.addAll(categoryFilteredList);
        } else {
            for (MenuItemResponse item : categoryFilteredList) {
                if (item.getNamaProduk().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))) {
                    searchFilteredList.add(item);
                }
            }
        }
        homeAdapter.submitList(searchFilteredList);
    }

    @Override
    public void onCategoryClick(Category category) {
        this.selectedCategory = category;
        headerAdapter.updateSelectedCategory(category.getId());
        String currentQuery = headerAdapter.getCurrentSearchQuery(); // Ambil query terakhir dari header
        filterMenuItems(currentQuery);
    }

    @Override
    public void onItemDetailClick(MenuItemResponse item) {
        // --- GUNAKAN CARA BARU INI ---

        // 1. Buat instance Gson
        Gson gson = new Gson();
        // 2. Ubah objek 'item' menjadi teks JSON
        String productJson = gson.toJson(item);

        // 3. Siapkan Bundle dan masukkan teks JSON
        Bundle bundle = new Bundle();
        bundle.putString("product_item", productJson); // "product_item" harus sama dengan nama argument di XML

        // 4. Navigasi seperti biasa
        NavHostFragment.findNavController(this).navigate(R.id.action_home_to_product_detail, bundle);
    }

    @Override
    public void onAddItemClick(MenuItemResponse item) {

        cartViewModel.addItem(item);
    }
    @Override
    public void onIncreaseItemClick(MenuItemResponse item) {
        cartViewModel.addItem(item);
    }
    @Override
    public void onDecreaseItemClick(MenuItemResponse item) {
        cartViewModel.removeItem(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sliderRunnable != null) {
            sliderHandler.postDelayed(sliderRunnable, 3000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}