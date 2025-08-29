package com.adetrifauzananisarahel;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.adetrifauzananisarahel.databinding.ActivityMainBinding;
import com.adetrifauzananisarahel.model.CartItem;
import com.adetrifauzananisarahel.viewmodel.CartViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.adetrifauzananisarahel.ui.cart.CartSheetFragment;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    // --- BARU: Tambahkan ViewModel untuk keranjang ---
    private CartViewModel cartViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // --- BARU: Inisialisasi CartViewModel ---
        // ViewModelProvider akan memastikan kita dapat ViewModel yang sama di seluruh activity
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        // --- Kode Navigasi Bawah (TETAP ADA) ---
        BottomNavigationView navView = binding.navView;

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,  R.id.navigation_transaksi)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        NavigationUI.setupWithNavController(binding.navView, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Cek apakah halaman saat ini adalah salah satu dari tab utama
            boolean isMainDestination = destination.getId() == R.id.navigation_home ||
                    destination.getId() == R.id.navigation_transaksi;


            if (isMainDestination) {
                // Jika di halaman utama, TAMPILKAN menu bawah
                binding.navView.setVisibility(View.VISIBLE);

                // --- BAGIAN BARU ---
                // Cek juga kondisi keranjang untuk menampilkan/menyembunyikan kapsul
                Map<Integer, CartItem> currentCart = cartViewModel.getCartItems().getValue();
                if (currentCart != null && !currentCart.isEmpty()) {
                    binding.cartCapsuleBar.setVisibility(View.VISIBLE);
                } else {
                    binding.cartCapsuleBar.setVisibility(View.GONE);
                }
                // --- AKHIR BAGIAN BARU ---

            } else {
                // Jika BUKAN di halaman utama (misal: Checkout, Detail Produk, dll)
                // SEMBUNYIKAN menu bawah DAN kapsul keranjang
                binding.navView.setVisibility(View.GONE);
                binding.cartCapsuleBar.setVisibility(View.GONE); // <-- TAMBAHKAN INI
            }
        });

        // --- Kode Warna Ikon Navigasi (TETAP ADA) ---
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        };

        int[] colors = new int[]{
                getResources().getColor(R.color.bottom_nav_item_selected, getTheme()),
                getResources().getColor(R.color.bottom_nav_item_unselected, getTheme())
        };

        android.content.res.ColorStateList myColorStateList = new android.content.res.ColorStateList(states, colors);

        binding.navView.setItemIconTintList(myColorStateList);
        binding.navView.setItemTextColor(myColorStateList);
        // --- AKHIR DARI KODE WARNA ---

        // --- BARU: Panggil method untuk mengamati perubahan keranjang ---
        observeCart();

        binding.cartCapsuleBar.setOnClickListener(v -> {
            CartSheetFragment cartSheet = new CartSheetFragment();
            cartSheet.show(getSupportFragmentManager(), cartSheet.getTag());
        });

        observeCart();

        // --- BARU: Tambahkan listener untuk seluruh kapsul ---
        binding.cartCapsuleBar.setOnClickListener(v -> showCartSheet());

        // --- BARU: Tambahkan juga listener untuk tombol checkout di dalam kapsul ---
        binding.btnCheckout.setOnClickListener(v -> showCartSheet());
    }

    private void showCartSheet() {
        CartSheetFragment cartSheet = new CartSheetFragment();
        cartSheet.show(getSupportFragmentManager(), cartSheet.getTag());
    }

    /**
     * --- METHOD BARU ---
     * Method ini "mendengarkan" setiap perubahan data di dalam CartViewModel.
     * Jika ada item ditambah/dikurangi, kode di dalam observer ini akan otomatis berjalan.
     */
    private void observeCart() {
        cartViewModel.getCartItems().observe(this, cartItems -> {
            if (cartItems == null || cartItems.isEmpty()) {
                // Jika keranjang kosong, sembunyikan kapsul
                binding.cartCapsuleBar.setVisibility(View.GONE);
            } else {
                // Jika ada isinya, tampilkan kapsul dan hitung total
                binding.cartCapsuleBar.setVisibility(View.VISIBLE);

                int totalItems = 0;
                double totalPrice = 0;
                for (CartItem item : cartItems.values()) {
                    totalItems += item.getQuantity();
                    totalPrice += item.getProduct().getHarga() * item.getQuantity();
                }

                // Update teks pada tombol checkout
                binding.btnCheckout.setText("Check Out (" + totalItems + ")");

                // Format harga ke Rupiah dan update teks total harga
                Locale localeID = new Locale("in", "ID");
                NumberFormat format = NumberFormat.getCurrencyInstance(localeID);
                format.setMaximumFractionDigits(0);
                binding.tvCartTotalPrice.setText(format.format(totalPrice));
            }
        });
    }

}