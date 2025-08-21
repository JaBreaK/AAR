package com.adetrifauzananisarahel.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.adetrifauzananisarahel.adapter.CarouselAdapter;
import com.adetrifauzananisarahel.adapter.HomeAdapter;
import com.adetrifauzananisarahel.databinding.FragmentHomeBinding;
import com.adetrifauzananisarahel.model.ApiResponse;
import com.adetrifauzananisarahel.model.CarouselItem;
import com.adetrifauzananisarahel.model.MenuCategory;
import com.adetrifauzananisarahel.network.ApiClient;
import com.adetrifauzananisarahel.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    // Adapter & List untuk struktur baru
    private HomeAdapter homeAdapter;
    private final List<Object> homeList = new ArrayList<>(); // List ini berisi MenuCategory & MenuItem
    private final List<MenuCategory> originalCategoryList = new ArrayList<>(); // Untuk menyimpan data asli untuk fitur search

    // Adapter & List untuk Carousel (bisa dipertahankan atau diganti API juga)
    private CarouselAdapter carouselAdapter;
    private final List<CarouselItem> carouselItems = new ArrayList<>();
    private final Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;

    // TODO: Integrasikan fitur search, voice, dan theme toggle dengan data dari API

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // TODO: Inisialisasi launcher untuk search jika masih dipakai
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUI();
        fetchHomeData();
        // Jika carousel masih dipakai, panggil method untuk fetch data carousel di sini
    }

    private void setupUI() {
        // Setup Carousel Adapter (jika masih dipakai)
        carouselAdapter = new CarouselAdapter(carouselItems);
        binding.viewPagerCarousel.setAdapter(carouselAdapter);

        // Setup Adapter Utama untuk Home
        homeAdapter = new HomeAdapter(homeList);
        binding.recyclerViewHome.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewHome.setNestedScrollingEnabled(false); // Penting jika di dalam NestedScrollView
        binding.recyclerViewHome.setAdapter(homeAdapter);
    }

    private void fetchHomeData() {
        binding.progressBar.setVisibility(View.VISIBLE); // Tampilkan loading

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ApiResponse<MenuCategory>> call = apiService.getHomeData();

        call.enqueue(new Callback<ApiResponse<MenuCategory>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<MenuCategory>> call, @NonNull Response<ApiResponse<MenuCategory>> response) {
                binding.progressBar.setVisibility(View.GONE); // Sembunyikan loading

                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    List<MenuCategory> fetchedData = response.body().getData();

                    // Simpan data asli untuk keperluan filter/search nanti
                    originalCategoryList.clear();
                    originalCategoryList.addAll(fetchedData);

                    // "Flatten" atau ratakan data untuk ditampilkan di satu RecyclerView
                    updateHomeList(fetchedData);

                } else {
                    Toast.makeText(getContext(), "Gagal memuat data.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<MenuCategory>> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateHomeList(List<MenuCategory> categories) {
        homeList.clear();
        for (MenuCategory category : categories) {
            // Jangan tampilkan kategori jika tidak ada item di dalamnya
            if (category.getItems() != null && !category.getItems().isEmpty()) {
                homeList.add(category); // Tambah objek kategori sebagai header
                homeList.addAll(category.getItems()); // Tambah semua item di bawahnya
            }
        }
        homeAdapter.notifyDataSetChanged();
    }

    // TODO: Buat ulang method filter untuk search agar sesuai dengan struktur homeList
    // private void filter(String query) { ... }

    // ... (kode untuk onCreateOptionsMenu, onOptionsItemSelected, voice search bisa ditambahkan kembali di sini) ...

    // ... (kode untuk Carousel auto-slide bisa ditambahkan kembali di sini) ...

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}