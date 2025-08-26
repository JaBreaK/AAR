package com.adetrifauzananisarahel.ui.product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider; // Import baru

import com.adetrifauzananisarahel.databinding.FragmentProductDetailBinding;
import com.adetrifauzananisarahel.model.CartItem; // Import baru
import com.adetrifauzananisarahel.model.MenuItemResponse;
import com.adetrifauzananisarahel.viewmodel.CartViewModel; // Import baru
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map; // Import baru

public class ProductDetailFragment extends BottomSheetDialogFragment {

    private FragmentProductDetailBinding binding;
    private MenuItemResponse menuItem;

    // --- BARU: Tambahkan ViewModel untuk keranjang ---
    private CartViewModel cartViewModel;

    public static ProductDetailFragment newInstance(MenuItemResponse menuItem) {
        ProductDetailFragment fragment = new ProductDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable("PRODUCT_ITEM", menuItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            menuItem = getArguments().getParcelable("PRODUCT_ITEM");
        }
        // --- BARU: Inisialisasi ViewModel ---
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProductDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (menuItem != null) {
            populateUI();
            setupListeners();
            // --- BARU: Amati perubahan di keranjang ---
            observeCart();
        }
    }

    private void observeCart() {
        cartViewModel.getCartItems().observe(getViewLifecycleOwner(), this::updateButtonState);
    }

    // --- BARU: Method untuk update tampilan tombol ---
    private void updateButtonState(Map<Integer, CartItem> cartItems) {
        if (menuItem == null) return;

        CartItem itemInCart = cartItems.get(menuItem.getId());

        if (itemInCart != null) {
            // Jika item ADA di keranjang, tampilkan quantity selector
            binding.btnAddToCartDetail.setVisibility(View.GONE);
            binding.quantitySelectorGroupDetail.setVisibility(View.VISIBLE);
            binding.tvQuantityDetail.setText(String.valueOf(itemInCart.getQuantity()));
        } else {
            // Jika item TIDAK ADA, tampilkan tombol "Tambah"
            binding.btnAddToCartDetail.setVisibility(View.VISIBLE);
            binding.quantitySelectorGroupDetail.setVisibility(View.GONE);
        }
    }

    private void populateUI() {
        // ... (kode ini tidak berubah)
        binding.tvProductDetailName.setText(menuItem.getNamaProduk());
        binding.tvProductDetailDescription.setText(menuItem.getDeskripsi());

        Locale localeID = new Locale("in", "ID");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(localeID);
        currencyFormatter.setMaximumFractionDigits(0);
        binding.tvProductDetailPrice.setText(currencyFormatter.format(menuItem.getHarga()));

        Glide.with(this)
                .load(menuItem.getGambarUrl())
                .into(binding.ivProductDetailImage);
    }

    private void setupListeners() {
        // --- DIUBAH: Semua listener sekarang memanggil ViewModel ---

        // Listener untuk tombol "Tambah ke Keranjang"
        binding.btnAddToCartDetail.setOnClickListener(v -> {
            cartViewModel.addItem(menuItem);
            Toast.makeText(getContext(), menuItem.getNamaProduk() + " ditambah", Toast.LENGTH_SHORT).show();
        });

        // Listener untuk tombol (+)
        binding.btnIncreaseQuantityDetail.setOnClickListener(v -> {
            cartViewModel.addItem(menuItem);
        });

        // Listener untuk tombol (-)
        binding.btnDecreaseQuantityDetail.setOnClickListener(v -> {
            cartViewModel.removeItem(menuItem);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}