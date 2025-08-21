package com.adetrifauzananisarahel.ui.checkout;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.adetrifauzananisarahel.R;
import com.adetrifauzananisarahel.network.DatabaseContract;
import com.adetrifauzananisarahel.network.DatabaseHelper;
import com.adetrifauzananisarahel.databinding.FragmentCheckoutBinding;
import com.adetrifauzananisarahel.model.CartItem;
import com.adetrifauzananisarahel.model.Transaction;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CheckoutFragment extends Fragment {

    private static final String TAG = "CHECKOUT_DEBUG";

    // --- Variabel Binding & Helper ---
    private FragmentCheckoutBinding binding;
    private DatabaseHelper dbHelper;

    // --- Variabel Data & State ---
    private String currentUsername;
    private List<CartItem> cartItemsForTransaction = new ArrayList<>();
    private String selectedAddress = "Alamat belum dipilih";
    private double subtotal = 0;
    private double shippingCost = 0;
    private String selectedCourier = "";
    private String selectedPaymentMethod = "";

    // --- Threading ---
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    // --- Launcher untuk Izin Lokasi ---
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                Log.d(TAG, "Hasil permintaan izin diterima. Diberikan: " + isGranted);
                if (isGranted) {
                    navigateToMapPicker();
                } else {
                    Toast.makeText(getContext(), "Izin lokasi dibutuhkan untuk memilih alamat di peta.", Toast.LENGTH_LONG).show();
                }
            });

    // --- Lifecycle Methods ---

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Listener untuk menerima hasil alamat dari MapPickerFragment
        getParentFragmentManager().setFragmentResultListener("address_request", this, (requestKey, bundle) -> {
            selectedAddress = bundle.getString("selectedAddress", "Gagal mendapatkan alamat");
            Log.d(TAG, "Alamat diterima dari peta: " + selectedAddress);
            binding.labelAlamatDipilih.setText(selectedAddress);
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCheckoutBinding.inflate(inflater, container, false);
        dbHelper = new DatabaseHelper(getContext());
        SharedPreferences prefs = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        currentUsername = prefs.getString("username", null);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Checkout");
        setupListeners();
        loadCartSummary();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // --- Setup & UI Logic ---

    private void setupListeners() {
        // Listener untuk Tombol Pilih Lokasi
        binding.buttonPilihLokasi.setOnClickListener(v -> {
            Log.d(TAG, "Tombol 'Pilih di Peta' diklik.");
            openMapPicker();
        });

        // Listener untuk Tombol Bayar
        binding.buttonPlaceOrder.setOnClickListener(v -> placeOrder());

        // Listener untuk Pilihan Kurir
        binding.radioGroupKurir.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_jne) {
                shippingCost = 10000;
                selectedCourier = "JNE Reguler";
            } else if (checkedId == R.id.radio_sicepat) {
                shippingCost = 12000;
                selectedCourier = "SiCepat BEST";
            }
            updateSummary();
        });

        // Listener untuk Pilihan Pembayaran
        binding.radioGroupPembayaran.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = group.findViewById(checkedId);
            if (rb != null) {
                selectedPaymentMethod = rb.getText().toString();
            }
        });
    }

    private void updateSummary() {
        double tax = subtotal * 0.11; // PPN 11%
        double grandTotal = subtotal + shippingCost + tax;

        Locale localeID = new Locale("in", "ID");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(localeID);
        currencyFormatter.setMaximumFractionDigits(0);

        binding.textSubtotal.setText(currencyFormatter.format(subtotal));
        binding.textOngkir.setText(currencyFormatter.format(shippingCost));
        binding.textPajak.setText(currencyFormatter.format(tax));
        binding.textGrandTotal.setText(currencyFormatter.format(grandTotal));
    }


    // --- Data & Navigasi ---

    private void loadCartSummary() {
        if (currentUsername == null) return;
        executor.execute(() -> {
            cartItemsForTransaction.clear();
            double tempSubtotal = 0;
            Cursor cursor = dbHelper.getCartItems(currentUsername);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long cartId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.CartEntry._ID));
                    long productId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.CartEntry.COLUMN_PRODUCT_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.CartEntry.COLUMN_PRODUCT_NAME));
                    // --- INI BAGIAN YANG DIPERBAIKI ---
                    String price = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.CartEntry.COLUMN_PRODUCT_PRICE));
                    String image = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.CartEntry.COLUMN_PRODUCT_IMAGE_PATH));
                    int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.CartEntry.COLUMN_QUANTITY));

                    cartItemsForTransaction.add(new CartItem(cartId, productId, name, price, image, quantity));
                    tempSubtotal += Double.parseDouble(price.replaceAll("[^\\d]", "")) * quantity;
                } while (cursor.moveToNext());
                cursor.close();
            }

            subtotal = tempSubtotal;
            mainThreadHandler.post(this::updateSummary);
        });
    }

    private void openMapPicker() {
        Log.d(TAG, "Mengecek izin lokasi...");
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Izin sudah ada. Langsung navigasi ke peta.");
            navigateToMapPicker();
        } else {
            Log.d(TAG, "Izin belum ada. Meminta izin sekarang...");
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void navigateToMapPicker() {
        NavHostFragment.findNavController(this).navigate(R.id.action_checkout_to_map_picker);
    }

    private void placeOrder() {
        if (selectedAddress.isEmpty() || selectedAddress.equals("Alamat belum dipilih") || selectedCourier.isEmpty() || selectedPaymentMethod.isEmpty()) {
            Toast.makeText(getContext(), "Harap lengkapi alamat, kurir, dan metode pembayaran!", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Konfirmasi Pesanan")
                .setMessage("Apakah Anda yakin ingin melanjutkan pembayaran?")
                .setPositiveButton("Ya, Bayar", (dialog, which) -> executePlaceOrder())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void executePlaceOrder() {
        executor.execute(() -> {
            String originAddress = "Alamat asal tidak ditemukan";
            // Ambil alamat dari produk pertama di keranjang
            if (!cartItemsForTransaction.isEmpty()) {
                long firstProductId = cartItemsForTransaction.get(0).getProductId();
                Cursor productCursor = dbHelper.getProductById(firstProductId);
                if(productCursor != null && productCursor.moveToFirst()){
                    originAddress = productCursor.getString(productCursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_ADDRESS));
                    productCursor.close();
                }
            }

            Gson gson = new Gson();
            String itemsJson = gson.toJson(cartItemsForTransaction);
            double grandTotal = subtotal + shippingCost + (subtotal * 0.11);

            // Panggil addTransaction yang sudah diupdate
            long newTransactionId = dbHelper.addTransaction(currentUsername, itemsJson, grandTotal,
                    selectedAddress, originAddress, selectedCourier, selectedPaymentMethod);

            if (newTransactionId != -1) {
                dbHelper.clearCart(currentUsername);
                // Ambil data lengkap transaksi yang baru dibuat
                Transaction newTransaction = dbHelper.getTransactionById(newTransactionId);

                mainThreadHandler.post(() -> {
                    Toast.makeText(getContext(), "Transaksi berhasil!", Toast.LENGTH_LONG).show();

                    if (newTransaction != null) {
                        // Navigasi ke Halaman Detail dengan data transaksi baru
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("TRANSACTION_DATA", newTransaction);
                        NavHostFragment.findNavController(this).navigate(R.id.action_checkout_to_transaction_detail, bundle);
                    } else {
                        // Fallback jika gagal ambil detail, kembali ke home
                        NavHostFragment.findNavController(this).popBackStack(R.id.navigation_home, false);
                    }
                });
            } else {
                mainThreadHandler.post(() -> Toast.makeText(getContext(), "Gagal memproses transaksi.", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
