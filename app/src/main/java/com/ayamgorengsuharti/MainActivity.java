package com.ayamgorengsuharti;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.ayamgorengsuharti.databinding.ActivityMainBinding;
import com.ayamgorengsuharti.model.CartItem;
import com.ayamgorengsuharti.viewmodel.CartViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private CartViewModel cartViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Inisialisasi ViewModel di awal
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        // 2. Setup Navigasi
        setupNavigation();

        // 3. Setup Listener (Hanya Sekali)
        binding.cartCapsuleBar.setOnClickListener(v ->
                navController.navigate(R.id.action_global_cartSheetFragment)
        );

        // 4. Panggil Observer (Hanya Sekali)
        observeCart();
    }

    private void setupNavigation() {
        BottomNavigationView navView = binding.navView;
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_transaksi)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Listener untuk pindah halaman
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            boolean isMainDestination = destination.getId() == R.id.navigation_home ||
                    destination.getId() == R.id.navigation_transaksi;

            // Atur visibilitas BottomNavigationView
            binding.navView.setVisibility(isMainDestination ? View.VISIBLE : View.GONE);

            // Panggil ulang observeCart untuk re-evaluasi visibilitas kapsul
            // Ini akan memastikan status kapsul benar saat kembali ke halaman utama
            if(isMainDestination) {
                updateCartCapsuleVisibility();
            } else {
                // Sembunyikan kapsul jika bukan di halaman utama
                binding.cartCapsuleBar.setVisibility(View.GONE);
                cartViewModel.setCartCapsuleVisible(false);
            }
        });
    }

    private void observeCart() {
        cartViewModel.getCartItems().observe(this, cartItems -> {
            // Update teks dan harga di kapsul
            if (cartItems != null && !cartItems.isEmpty()) {
                int totalItems = 0;
                double totalPrice = 0;
                for (CartItem item : cartItems.values()) {
                    totalItems += item.getQuantity();
                    totalPrice += item.getProduct().getHarga() * item.getQuantity();
                }
                binding.btnCheckout.setText("Check Out (" + totalItems + ")");
                Locale localeID = new Locale("in", "ID");
                NumberFormat format = NumberFormat.getCurrencyInstance(localeID);
                format.setMaximumFractionDigits(0);
                binding.tvCartTotalPrice.setText(format.format(totalPrice));
            }

            // Update visibilitas kapsul
            updateCartCapsuleVisibility();
        });
    }

    // METHOD BARU: Untuk sentralisasi logika menampilkan/menyembunyikan kapsul
    private void updateCartCapsuleVisibility() {
        if (navController.getCurrentDestination() == null) return;

        boolean isMainDestination = navController.getCurrentDestination().getId() == R.id.navigation_home ||
                navController.getCurrentDestination().getId() == R.id.navigation_transaksi;

        Map<Integer, CartItem> cart = cartViewModel.getCartItems().getValue();
        boolean isCartEmpty = cart == null || cart.isEmpty();

        // Kapsul hanya muncul jika: di halaman utama DAN keranjang tidak kosong
        if (isMainDestination && !isCartEmpty) {
            binding.cartCapsuleBar.setVisibility(View.VISIBLE);
            cartViewModel.setCartCapsuleVisible(true);
        } else {
            binding.cartCapsuleBar.setVisibility(View.GONE);
            cartViewModel.setCartCapsuleVisible(false);
        }
    }
}