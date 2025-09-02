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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.ayamgorengsuharti.R;
import com.ayamgorengsuharti.adapter.CarouselAdapter;
import com.ayamgorengsuharti.adapter.CategoryAdapter;
import com.ayamgorengsuharti.adapter.HomeAdapter;
import com.ayamgorengsuharti.databinding.FragmentHomeBinding;
import com.ayamgorengsuharti.model.Category;
import com.ayamgorengsuharti.model.MenuItemResponse;
import com.ayamgorengsuharti.model.Promo;
import com.ayamgorengsuharti.network.ApiClient;
import com.ayamgorengsuharti.network.ApiService;
import com.ayamgorengsuharti.ui.product.ProductDetailFragment;
import com.ayamgorengsuharti.viewmodel.CartViewModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements HomeAdapter.OnItemClickListener, CategoryAdapter.OnCategoryClickListener {

    private FragmentHomeBinding binding;
    private HomeAdapter homeAdapter;
    private CategoryAdapter categoryAdapter; // Adapter baru
    private CarouselAdapter carouselAdapter;
    private CartViewModel cartViewModel;

    private final List<MenuItemResponse> allMenuItems = new ArrayList<>();
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;
    private List<Promo> promoList = new ArrayList<>();
    private Category selectedCategory = new Category(0, "Semua");

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
        setupCarousel();
        setupSearchListener();
        fetchMenuData();

        cartViewModel.getCartItems().observe(getViewLifecycleOwner(), cartItems -> {
            if (homeAdapter != null) {
                homeAdapter.updateCartItems(cartItems);
            }
        });
        // --- BARU: Observer untuk mengatur padding ---
        cartViewModel.getIsCartCapsuleVisible().observe(getViewLifecycleOwner(), isVisible -> {
            // Dapatkan padding horizontal yang sudah ada agar tidak hilang
            int paddingHorizontal = binding.recyclerViewHome.getPaddingLeft();

            if (isVisible) {
                // Kapsul muncul, tambah padding bawah sekitar 80dp
                binding.recyclerViewHome.setPadding(paddingHorizontal, 0, paddingHorizontal, dpToPx(88));
            } else {
                // Kapsul hilang, kembalikan padding bawah ke 0 (atau nilai default)
                binding.recyclerViewHome.setPadding(paddingHorizontal, 0, paddingHorizontal, dpToPx(16));
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
        // Setup adapter menu
        homeAdapter = new HomeAdapter(this);
        binding.recyclerViewHome.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.recyclerViewHome.setAdapter(homeAdapter);

        // Setup adapter kategori
        categoryAdapter = new CategoryAdapter(this);
        binding.recyclerViewCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewCategories.setAdapter(categoryAdapter);
    }
    // --- METHOD BARU UNTUK SEMUA LOGIKA CAROUSEL ---
    // --- BARU: Method untuk listener pencarian ---
    private void setupSearchListener() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMenuItems(); // Panggil filter setiap kali teks berubah
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void setupCarousel() {
        // 1. Siapkan data gambar lokal
        promoList.clear();
        // Pastikan nama drawable SAMA PERSIS dengan nama file gambarmu
        promoList.add(new Promo(1, "Promo 1", R.drawable.ayam));
        promoList.add(new Promo(2, "Promo 2", R.drawable.ayam2));


        // 2. Setup Adapter dan ViewPager2
        carouselAdapter = new CarouselAdapter();
        binding.viewPagerCarousel.setAdapter(carouselAdapter);
        carouselAdapter.submitList(promoList);

        // 3. Setup visual (efek zoom & margin)
        binding.viewPagerCarousel.setClipToPadding(false);
        binding.viewPagerCarousel.setClipChildren(false);
        binding.viewPagerCarousel.setOffscreenPageLimit(3);
        binding.viewPagerCarousel.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(40));
        transformer.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            page.setScaleY(0.85f + r * 0.15f);
        });
        binding.viewPagerCarousel.setPageTransformer(transformer);

        // 4. Setup auto-scroll
        sliderRunnable = () -> {
            // TAMBAHKAN PENGECEKAN INI
            if (binding == null) {
                return; // Jika binding null (halaman sudah dihancurkan), hentikan eksekusi
            }

            int currentItem = binding.viewPagerCarousel.getCurrentItem();
            if (currentItem == promoList.size() - 1) {
                binding.viewPagerCarousel.setCurrentItem(0);
            } else {
                binding.viewPagerCarousel.setCurrentItem(currentItem + 1);
            }
        };
        binding.viewPagerCarousel.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000); // Ganti slide setiap 3 detik
            }
        });
    }

    private void fetchMenuData() {
        // Tampilkan loading (jika ada)

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getMenu().enqueue(new Callback<List<MenuItemResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<MenuItemResponse>> call, @NonNull Response<List<MenuItemResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allMenuItems.clear();
                    allMenuItems.addAll(response.body());

                    // Ekstrak kategori unik
                    List<Category> uniqueCategories = extractUniqueCategories(allMenuItems);

                    // Kirim data ke masing-masing adapter
                    categoryAdapter.submitList(uniqueCategories);
                    homeAdapter.submitList(allMenuItems); // Tampilkan semua menu di awal
                } else {
                    Toast.makeText(getContext(), "Gagal memuat data.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<MenuItemResponse>> call, @NonNull Throwable t) {
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

    // --- BARU: Method filter utama yang menggabungkan kategori dan pencarian ---
    private void filterMenuItems() {
        // 1. Ambil kata kunci pencarian dari EditText
        String query = binding.etSearch.getText().toString().toLowerCase(Locale.ROOT);

        // 2. Siapkan list awal berdasarkan kategori yang dipilih
        List<MenuItemResponse> categoryFilteredList = new ArrayList<>();
        if (selectedCategory.getId() == 0) { // Jika "Semua"
            categoryFilteredList.addAll(allMenuItems);
        } else {
            for (MenuItemResponse item : allMenuItems) {
                if (item.getKategori() != null && item.getKategori().getId() == selectedCategory.getId()) {
                    categoryFilteredList.add(item);
                }
            }
        }

        // 3. Filter lebih lanjut list tersebut dengan kata kunci pencarian
        List<MenuItemResponse> searchFilteredList = new ArrayList<>();
        if (query.isEmpty()) {
            searchFilteredList.addAll(categoryFilteredList);
        } else {
            for (MenuItemResponse item : categoryFilteredList) {
                if (item.getNamaProduk().toLowerCase(Locale.ROOT).contains(query)) {
                    searchFilteredList.add(item);
                }
            }
        }

        // 4. Update adapter dengan hasil akhir
        homeAdapter.submitList(searchFilteredList);
    }

    // --- Implementasi Listener ---

    @Override
    public void onCategoryClick(Category category) {
        // --- BARU: Update tampilan kategori yang aktif ---
        categoryAdapter.setSelectedCategoryId(category.getId());

        // Logika filter (tidak berubah)
        if (category.getId() == 0) {
            homeAdapter.submitList(allMenuItems);
        } else {
            List<MenuItemResponse> filteredList = new ArrayList<>();
            for (MenuItemResponse item : allMenuItems) {
                if (item.getKategori() != null && item.getKategori().getId() == category.getId()) {
                    filteredList.add(item);
                }
            }
            homeAdapter.submitList(filteredList);
        }
    }

    @Override
    public void onItemDetailClick(MenuItemResponse item) {
        ProductDetailFragment.newInstance(item).show(getParentFragmentManager(), "ProductDetailFragmentTag");
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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}