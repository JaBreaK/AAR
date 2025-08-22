package com.adetrifauzananisarahel.ui.product;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.adetrifauzananisarahel.R;
import com.adetrifauzananisarahel.databinding.FragmentProductDetailBinding;
import com.adetrifauzananisarahel.model.ApiResponse;
import com.adetrifauzananisarahel.model.FoodItem;
import com.adetrifauzananisarahel.network.ApiClient;
import com.adetrifauzananisarahel.network.ApiService;
import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailFragment extends Fragment {

    private FragmentProductDetailBinding binding;
    private String productId;
    private FoodItem currentProduct;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProductDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // =====================================================================
        // PENANGKAP ERROR DIMULAI DI SINI
        // =====================================================================
        try {
            // Semua kode asli kita sekarang ada di dalam blok 'try'
            // Ambil ID produk yang dikirim dari HomeFragment
            if (getArguments() != null) {
                this.productId = getArguments().getString("foodId");
            }

            // Jika ID valid, panggil API untuk mengambil detailnya
            if (this.productId != null && !this.productId.isEmpty()) {
                fetchProductDetailsFromApi();
            } else {
                Toast.makeText(getContext(), "ID produk tidak valid.", Toast.LENGTH_SHORT).show();
            }

            // Fungsi tombol Add to Cart
            binding.buttonAddToCart.setOnClickListener(v -> {
                if (currentProduct != null) {
                    // TODO: Nanti, logika add to cart juga harus memanggil API
                    Toast.makeText(getContext(), currentProduct.getName() + " ditambahkan ke keranjang!", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            // JIKA TERJADI CRASH, BUKANNYA FORCE CLOSE, KODE INI YANG AKAN DIJALANKAN
            // Menampilkan pesan error asli di layar
            String errorMessage = e.toString();
            Toast.makeText(getContext(), "PENYEBAB CRASH: " + errorMessage, Toast.LENGTH_LONG).show();

            // Mencatat error ke Logcat untuk jaga-jaga
            Log.e("MANUAL_CRASH_REPORT", "Error terdeteksi di ProductDetailFragment: ", e);
        }
        // =====================================================================
        // PENANGKAP ERROR BERAKHIR DI SINI
        // =====================================================================
    }

    private void fetchProductDetailsFromApi() {
        // ... (method ini tidak diubah)
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ApiResponse<FoodItem>> call = apiService.getProductDetail(this.productId);

        call.enqueue(new Callback<ApiResponse<FoodItem>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<FoodItem>> call, @NonNull Response<ApiResponse<FoodItem>> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    currentProduct = response.body().getData();
                    if (currentProduct != null) {
                        updateUi(currentProduct);
                    }
                } else {
                    Toast.makeText(getContext(), "Gagal memuat detail produk.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<FoodItem>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUi(FoodItem product) {
        // ... (method ini tidak diubah)
        if (getContext() == null || binding == null) return;
        binding.textViewProductName.setText(product.getName());
        binding.textViewProductDescription.setText(product.getDescription());
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        binding.textViewProductPrice.setText(formatter.format(product.getPrice()));
        Glide.with(getContext())
                .load(product.getImageUrl())
                .placeholder(R.drawable.pp)
                .into(binding.imageViewProduct);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}