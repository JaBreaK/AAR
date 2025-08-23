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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.adetrifauzananisarahel.R;
import com.adetrifauzananisarahel.adapter.CarouselAdapter;
import com.adetrifauzananisarahel.adapter.HomeAdapter;
import com.adetrifauzananisarahel.databinding.FragmentHomeBinding;
import com.adetrifauzananisarahel.model.ApiResponse;
import com.adetrifauzananisarahel.model.CarouselItem;
import com.adetrifauzananisarahel.model.FoodItem;
import com.adetrifauzananisarahel.model.MenuCategory;
import com.adetrifauzananisarahel.network.ApiClient;
import com.adetrifauzananisarahel.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements HomeAdapter.OnItemClickListener {

    private FragmentHomeBinding binding;
    private HomeAdapter homeAdapter;
    private final List<Object> homeList = new ArrayList<>();
    private final List<MenuCategory> originalCategoryList = new ArrayList<>();
    private CarouselAdapter carouselAdapter;
    private final List<CarouselItem> carouselItems = new ArrayList<>();
    private final Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
    }

    private void setupUI() {
        carouselAdapter = new CarouselAdapter(carouselItems);
        binding.viewPagerCarousel.setAdapter(carouselAdapter);

        homeAdapter = new HomeAdapter(homeList, this);
        binding.recyclerViewHome.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewHome.setNestedScrollingEnabled(false);
        binding.recyclerViewHome.setAdapter(homeAdapter);
    }

    private void fetchHomeData() {
        binding.progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        // =================================================================================
        // PERBAIKAN 1: Tambahkan List<> agar cocok dengan yang ada di ApiService
        // =================================================================================
        Call<ApiResponse<List<MenuCategory>>> call = apiService.getHomeData();

        call.enqueue(new Callback<ApiResponse<List<MenuCategory>>>() { // <-- PERBAIKAN 2: Sesuaikan tipe data di Callback
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<MenuCategory>>> call, @NonNull Response<ApiResponse<List<MenuCategory>>> response) { // <-- PERBAIKAN 3: Sesuaikan tipe data di parameter onResponse
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    List<MenuCategory> fetchedData = response.body().getData();
                    originalCategoryList.clear();
                    originalCategoryList.addAll(fetchedData);
                    updateHomeList(fetchedData);
                } else {
                    Toast.makeText(getContext(), "Gagal memuat data.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<MenuCategory>>> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateHomeList(List<MenuCategory> categories) {
        homeList.clear();
        for (MenuCategory category : categories) {
            if (category.getItems() != null && !category.getItems().isEmpty()) {
                homeList.add(category);
                homeList.addAll(category.getItems());
            }
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onItemClick(FoodItem item) {
        if (item == null || item.getId() == null) {
            Toast.makeText(getContext(), "Error: Data produk tidak valid.", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("foodId", item.getId());

        if (getView() != null) {
            // Pastikan ID ini SAMA PERSIS dengan ID action di mobile_navigation.xml
            Navigation.findNavController(getView()).navigate(R.id.action_home_to_product_detail, bundle);
        }
    }
}