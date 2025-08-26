package com.adetrifauzananisarahel.ui.checkout;

import android.content.Intent;
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

import androidx.browser.customtabs.CustomTabsIntent;
import android.net.Uri;

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

        displayOrderSummary();
        fetchPaymentMethods();

        binding.btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void displayOrderSummary() {
        Map<Integer, CartItem> cartItems = cartViewModel.getCartItems().getValue();
        if (cartItems == null) return;

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
        // --- BAGIAN BARU: Siapkan ColorStateList ---
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        };
        int[] colors = new int[]{
                ContextCompat.getColor(requireContext(), R.color.black),
                ContextCompat.getColor(requireContext(), R.color.abu)
        };
        ColorStateList colorStateList = new ColorStateList(states, colors);
        // --- AKHIR BAGIAN BARU ---
        for (PaymentMethod method : methods) {
            if (method.isActive()) {
                RadioButton radioButton = new RadioButton(getContext());
                radioButton.setText(method.getNamaMetode());
                radioButton.setId(method.getId()); // Penting: ID RadioButton = ID dari API
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

        // Siapkan payload
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
                customerWa,
                totalPrice,
                selectedPaymentId,
                customerNotes
        );

        // Panggil API
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.createOrder(payload).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                binding.progressBarOrder.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "Order Berhasil!", Toast.LENGTH_LONG).show();
                    cartViewModel.clearCart(); // Kosongkan keranjang

                    // TODO: Navigasi ke halaman sukses order dengan membawa data 'response.body()'
                    // Contoh:
                    // Bundle bundle = new Bundle();
                    // bundle.putParcelable("ORDER_DATA", response.body());
                    // NavHostFragment.findNavController(CheckoutFragment.this).navigate(R.id.action_checkout_to_success, bundle);

                    // Untuk sementara, kembali ke home
                    NavHostFragment.findNavController(CheckoutFragment.this).popBackStack(R.id.navigation_home, false);

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

        apiService.createOrder(payload).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                binding.progressBarOrder.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "Order Berhasil!", Toast.LENGTH_LONG).show();
                    cartViewModel.clearCart(); // Kosongkan keranjang

                    // --- BAGIAN BARU DIMULAI DI SINI ---

                    // Ambil ID dari response
                    int orderId = response.body().getId();
                    // Buat URL lengkapnya
                    String url = "https://ayamgorengsuharti.vercel.app/pesanan/" + orderId;

                    // Panggil method untuk membuka URL
                    openUrlInCustomTab(url);

                    // --- AKHIR BAGIAN BARU ---

                    // Kembali ke halaman utama
                    NavHostFragment.findNavController(CheckoutFragment.this).popBackStack(R.id.navigation_home, false);

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
    // --- METHOD BARU UNTUK MEMBUKA CUSTOM TABS ---
    private void openUrlInCustomTab(String url) {
        if (getContext() == null) return;

        try {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            // Optional: Atur warna toolbar agar sesuai dengan tema aplikasi
            builder.setToolbarColor(ContextCompat.getColor(getContext(), R.color.orange_primary));
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(getContext(), Uri.parse(url));
        } catch (Exception e) {
            // Fallback: Jika Chrome Custom Tabs gagal, buka browser biasa
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}