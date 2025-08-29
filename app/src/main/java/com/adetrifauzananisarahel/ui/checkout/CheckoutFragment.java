package com.adetrifauzananisarahel.ui.checkout;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.adetrifauzananisarahel.R;
import com.adetrifauzananisarahel.databinding.FragmentCheckoutBinding;
import com.adetrifauzananisarahel.model.CartItem;
import com.adetrifauzananisarahel.model.OrderItemPayload;
import com.adetrifauzananisarahel.model.OrderPayload;
import com.adetrifauzananisarahel.model.OrderResponse;
import com.adetrifauzananisarahel.model.PaymentMethod;
import com.adetrifauzananisarahel.network.ApiClient;
import com.adetrifauzananisarahel.network.ApiService;
import com.adetrifauzananisarahel.viewmodel.CartViewModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutFragment extends Fragment {

    private FragmentCheckoutBinding binding;
    private CartViewModel cartViewModel;
    private double totalPrice = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCheckoutBinding.inflate(inflater, container, false);
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadUserData();
        displayOrderSummary();
        fetchPaymentMethods();

        binding.btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void displayOrderSummary() {
        Map<Integer, CartItem> cartItems = cartViewModel.getCartItems().getValue();
        if (cartItems == null) return;

        // Reset totalPrice setiap kali dipanggil
        totalPrice = 0;
        for (CartItem item : cartItems.values()) {
            totalPrice += item.getProduct().getHarga() * item.getQuantity();
        }

        Locale localeID = new Locale("in", "ID");
        NumberFormat format = NumberFormat.getCurrencyInstance(localeID);
        format.setMaximumFractionDigits(0);
        binding.tvTotalPriceCheckout.setText("Total: " + format.format(totalPrice));
    }

    private void fetchPaymentMethods() {
        binding.progressBarPayment.setVisibility(View.VISIBLE);
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getPaymentMethods().enqueue(new Callback<List<PaymentMethod>>() {
            @Override
            public void onResponse(Call<List<PaymentMethod>> call, Response<List<PaymentMethod>> response) {
                binding.progressBarPayment.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    populatePaymentMethods(response.body());
                } else {
                    Toast.makeText(getContext(), "Gagal memuat metode pembayaran", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PaymentMethod>> call, Throwable t) {
                binding.progressBarPayment.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populatePaymentMethods(List<PaymentMethod> methods) {
        binding.radioGroupPayment.removeAllViews();
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        };
        int[] colors = new int[]{
                ContextCompat.getColor(requireContext(), R.color.black),
                ContextCompat.getColor(requireContext(), R.color.abu) // Pastikan R.color.abu ada di colors.xml
        };
        ColorStateList colorStateList = new ColorStateList(states, colors);
        for (PaymentMethod method : methods) {
            if (method.isActive() && !method.getNamaMetode().toLowerCase(Locale.ROOT).contains("cash")) {
                // Jika kedua syarat terpenuhi, baru buat dan tampilkan RadioButton
                RadioButton radioButton = new RadioButton(getContext());
                radioButton.setText(method.getNamaMetode());
                radioButton.setId(method.getId());
                radioButton.setTextColor(colorStateList);
                radioButton.setButtonTintList(colorStateList);
                binding.radioGroupPayment.addView(radioButton);
            }
        }
    }

    private void placeOrder() {
        String customerName = binding.etCustomerName.getText().toString().trim();
        String customerWa = binding.etCustomerWhatsapp.getText().toString().trim();
        String customerNotes = binding.etCustomerNotes.getText().toString().trim();
        int selectedPaymentId = binding.radioGroupPayment.getCheckedRadioButtonId();

        if (customerName.isEmpty() || customerWa.isEmpty() || selectedPaymentId == -1) {
            Toast.makeText(getContext(), "Harap isi nama, nomor WA, dan pilih metode pembayaran", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBarOrder.setVisibility(View.VISIBLE);

        List<OrderItemPayload> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartViewModel.getCartItems().getValue().values()) {
            orderItems.add(new OrderItemPayload(
                    cartItem.getProduct().getId(),
                    cartItem.getProduct().getNamaProduk(),
                    cartItem.getProduct().getHarga(),
                    cartItem.getQuantity()
            ));
        }

        OrderPayload payload = new OrderPayload(
                orderItems,
                customerName,
                "ONLINE",
                customerWa,
                totalPrice,
                selectedPaymentId,
                customerNotes
        );

        // HANYA ADA SATU PANGGILAN API DI SINI
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.createOrder(payload).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                binding.progressBarOrder.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "Order Berhasil!", Toast.LENGTH_LONG).show();
                    cartViewModel.clearCart();

                    SharedPreferences prefs = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("nomorWa", customerWa);
                    editor.putString("customerName", customerName); // <-- TAMBAHKAN INI
                    editor.apply();

                    Bundle bundle = new Bundle();
                    bundle.putParcelable("ORDER_DATA", response.body());
                    NavHostFragment.findNavController(CheckoutFragment.this)
                            .navigate(R.id.action_checkoutFragment_to_orderSuccessFragment, bundle);
                } else {
                    Toast.makeText(getContext(), "Gagal membuat pesanan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                binding.progressBarOrder.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    // --- BARU: Method untuk membaca SharedPreferences dan mengisi form ---
    private void loadUserData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);
        String savedName = prefs.getString("customerName", "");
        String savedWa = prefs.getString("nomorWa", "");

        binding.etCustomerName.setText(savedName);
        binding.etCustomerWhatsapp.setText(savedWa);
    }

    // METHOD openUrlInCustomTab SUDAH DIHAPUS

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}