package com.ayamgorengsuharti.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.ayamgorengsuharti.adapter.HomeAdapter;
import com.ayamgorengsuharti.databinding.FragmentHomeBinding;
import com.ayamgorengsuharti.model.Category;
import com.ayamgorengsuharti.model.MenuItemResponse;
import com.ayamgorengsuharti.network.ApiClient;
import com.ayamgorengsuharti.network.ApiService;
import com.ayamgorengsuharti.ui.product.ProductDetailFragment;
import com.google.android.material.tabs.TabLayout;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.ayamgorengsuharti.viewmodel.CartViewModel;

public class HomeFragment extends Fragment implements HomeAdapter.OnItemClickListener {

    private FragmentHomeBinding binding;
    private HomeAdapter homeAdapter;
    private CartViewModel cartViewModel;

    // List untuk menampung data yang ditampilkan di adapter
    private final List<Object> displayedList = new ArrayList<>();

    // List untuk menyimpan SEMUA item dari API (tidak akan diubah)
    private final List<MenuItemResponse> allMenuItems = new ArrayList<>();

    // List untuk menyimpan kategori yang unik
    private final List<Category> uniqueCategories = new ArrayList<>();

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
        setupTabListener(); // Siapkan listener untuk tab
        fetchMenuData();

        cartViewModel.getCartItems().observe(getViewLifecycleOwner(), cartItems -> {
            if (homeAdapter != null) {
                homeAdapter.updateCartItems(cartItems);
            }
        });
    }


    private void setupUI() {
        homeAdapter = new HomeAdapter(displayedList, this);

        // --- INI BAGIAN YANG DIUBAH ---
        // Menggunakan GridLayoutManager dengan 2 kolom
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        binding.recyclerViewHome.setLayoutManager(gridLayoutManager);
        // --- AKHIR PERUBAHAN ---

        binding.recyclerViewHome.setAdapter(homeAdapter);
    }

    private void fetchMenuData() {
        binding.progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<MenuItemResponse>> call = apiService.getMenu();

        call.enqueue(new Callback<List<MenuItemResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<MenuItemResponse>> call, @NonNull Response<List<MenuItemResponse>> response) {
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    allMenuItems.clear();
                    allMenuItems.addAll(response.body());

                    // Ekstrak kategori unik dan setup tabs
                    extractAndSetupTabs(allMenuItems);

                    // Tampilkan semua produk secara default
                    filterListByCategory(null);
                } else {
                    Toast.makeText(getContext(), "Gagal memuat data menu.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<MenuItemResponse>> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- LOGIKA BARU UNTUK TABLAYOUT ---

    private void extractAndSetupTabs(List<MenuItemResponse> menuItems) {
        // Gunakan HashSet untuk memastikan ID kategori unik
        HashSet<Integer> categoryIds = new HashSet<>();
        uniqueCategories.clear();

        for (MenuItemResponse item : menuItems) {
            if (item.getKategori() != null && !categoryIds.contains(item.getKategori().getId())) {
                categoryIds.add(item.getKategori().getId());
                uniqueCategories.add(item.getKategori());
            }
        }

        // Urutkan kategori berdasarkan nama (opsional tapi bagus)
        Collections.sort(uniqueCategories, Comparator.comparing(Category::getNamaKategori));

        // Hapus tab lama dan buat yang baru
        binding.tabLayoutCategories.removeAllTabs();
        binding.tabLayoutCategories.addTab(binding.tabLayoutCategories.newTab().setText("Semua"));

        for (Category category : uniqueCategories) {
            binding.tabLayoutCategories.addTab(binding.tabLayoutCategories.newTab().setText(category.getNamaKategori()));
        }
    }

    private void setupTabListener() {
        binding.tabLayoutCategories.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    // Tab "Semua" diklik
                    filterListByCategory(null);
                } else {
                    // Tab kategori lain diklik, kurangi 1 karena ada tab "Semua"
                    Category selectedCategory = uniqueCategories.get(position - 1);
                    filterListByCategory(selectedCategory);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void filterListByCategory(@Nullable Category category) {
        List<MenuItemResponse> filteredList = new ArrayList<>();
        if (category == null) {
            // Jika null, tampilkan semua item
            filteredList.addAll(allMenuItems);
        } else {
            // Jika ada kategori, filter berdasarkan ID
            for (MenuItemResponse item : allMenuItems) {
                if (item.getKategori() != null && item.getKategori().getId() == category.getId()) {
                    filteredList.add(item);
                }
            }
        }

        // Update list di adapter
        displayedList.clear();
        displayedList.addAll(filteredList);
        homeAdapter.notifyDataSetChanged();
    }

    // --- AKHIR LOGIKA BARU ---

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onItemDetailClick(MenuItemResponse item) {
        // Logika lama untuk pindah ke halaman detail
        if (item == null) return;
        ProductDetailFragment bottomSheet = ProductDetailFragment.newInstance(item);
        bottomSheet.show(getParentFragmentManager(), "ProductDetailFragmentTag");
    }

    @Override
    public void onAddItemClick(MenuItemResponse item) {
        // Logika baru saat tombol "Tambah" diklik
        // Untuk sekarang kita tampilkan Toast saja
        cartViewModel.addItem(item);

        // TODO: Nanti di sini kamu bisa tambahkan logika untuk memasukkan item ke API keranjang
    }
    // Tambahkan dua method baru ini untuk -/+
    @Override
    public void onIncreaseItemClick(MenuItemResponse item) {
        cartViewModel.addItem(item);
    }

    @Override
    public void onDecreaseItemClick(MenuItemResponse item) {
        cartViewModel.removeItem(item);
    }


}