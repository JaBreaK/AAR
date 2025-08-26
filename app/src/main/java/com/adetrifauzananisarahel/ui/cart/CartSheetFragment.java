package com.adetrifauzananisarahel.ui.cart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.adetrifauzananisarahel.R;
import com.adetrifauzananisarahel.adapter.CartSheetAdapter;
import com.adetrifauzananisarahel.databinding.FragmentCartSheetBinding;
import com.adetrifauzananisarahel.model.CartItem;
import com.adetrifauzananisarahel.viewmodel.CartViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CartSheetFragment extends BottomSheetDialogFragment implements CartSheetAdapter.CartListener {

    private FragmentCartSheetBinding binding;
    private CartViewModel cartViewModel;
    private CartSheetAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCartSheetBinding.inflate(inflater, container, false);
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        observeCart();
        binding.btnGoToCheckout.setOnClickListener(v -> {
            // Panggil ID GLOBAL ACTION yang baru
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_global_checkoutFragment);
            dismiss();
        });
    }

    private void setupRecyclerView() {
        adapter = new CartSheetAdapter(this);
        binding.recyclerViewCart.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewCart.setAdapter(adapter);
    }

    private void observeCart() {
        cartViewModel.getCartItems().observe(getViewLifecycleOwner(), cartItems -> {
            if (cartItems == null || cartItems.isEmpty()) {
                dismiss(); // Jika keranjang jadi kosong, tutup bottom sheet
                return;
            }

            adapter.submitList(new ArrayList<>(cartItems.values()));

            // Hitung dan update total harga
            double totalPrice = 0;
            for (CartItem item : cartItems.values()) {
                totalPrice += item.getProduct().getHarga() * item.getQuantity();
            }
            Locale localeID = new Locale("in", "ID");
            NumberFormat format = NumberFormat.getCurrencyInstance(localeID);
            format.setMaximumFractionDigits(0);
            binding.tvTotalPriceCart.setText(format.format(totalPrice));
        });
    }

    // --- Implementasi Listener dari Adapter ---
    @Override
    public void onIncrease(CartItem item) {
        cartViewModel.addItem(item.getProduct());
    }

    @Override
    public void onDecrease(CartItem item) {
        cartViewModel.removeItem(item.getProduct());
    }

    @Override
    public void onRemove(CartItem item) {
        // Hapus semua kuantitas item ini
        for (int i = 0; i < item.getQuantity(); i++) {
            cartViewModel.removeItem(item.getProduct());
        }
    }
}