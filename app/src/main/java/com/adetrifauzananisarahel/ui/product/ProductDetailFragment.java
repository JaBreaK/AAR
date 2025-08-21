package com.adetrifauzananisarahel.ui.product;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.adetrifauzananisarahel.R;
import com.adetrifauzananisarahel.network.DatabaseContract;
import com.adetrifauzananisarahel.network.DatabaseHelper;
import com.adetrifauzananisarahel.databinding.FragmentProductDetailBinding;
import com.bumptech.glide.Glide;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductDetailFragment extends Fragment {

    private FragmentProductDetailBinding binding;
    private DatabaseHelper dbHelper;
    private long productId;

    // Variabel untuk TextToSpeech Engine
    private TextToSpeech tts;
    private boolean isTtsReady = false;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProductDetailBinding.inflate(inflater, container, false);
        dbHelper = new DatabaseHelper(getContext());

        // Inisialisasi TextToSpeech
        initializeTts();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            productId = getArguments().getLong("PRODUCT_ID", -1);
            if (productId != -1) {
                loadProductDetails();
            }
        }

        binding.buttonAddToCart.setOnClickListener(v -> addToCart());

        // Set listener untuk tombol speaker
        binding.buttonSpeak.setOnClickListener(v -> speakProductDetails());
    }

    private void initializeTts() {
        tts = new TextToSpeech(getContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                // Set bahasa ke Bahasa Indonesia
                int result = tts.setLanguage(new Locale("id", "ID"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Bahasa Indonesia tidak didukung di perangkat ini.");
                    isTtsReady = false;
                } else {
                    isTtsReady = true;
                }
            } else {
                Log.e("TTS", "Inisialisasi TextToSpeech gagal. Status: " + status);
                isTtsReady = false;
            }
        });
    }

    private void speakProductDetails() {
        if (!isTtsReady) {
            Toast.makeText(getContext(), "Fitur suara belum siap, coba lagi.", Toast.LENGTH_SHORT).show();
            // Coba inisialisasi ulang jika gagal sebelumnya
            initializeTts();
            return;
        }

        // Pastikan view tidak null
        if (binding == null) return;

        // Gabungkan nama dan deskripsi untuk dibacakan
        String productName = binding.textViewProductName.getText().toString();
        String productDescription = binding.textViewProductDescription.getText().toString();
        String textToSpeak = "Nama produk: " + productName + ". " + "Deskripsi: " + productDescription;

        if (!textToSpeak.isEmpty()) {
            // Menggunakan QUEUE_FLUSH agar ucapan sebelumnya berhenti dan diganti yang baru
            tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            Toast.makeText(getContext(), "Tidak ada detail untuk dibacakan.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProductDetails() {
        executor.execute(() -> {
            final Cursor cursor = dbHelper.getProductById(productId);
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_NAME));
                String price = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_PRICE));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_IMAGE_PATH));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_DESCRIPTION));
                cursor.close();

                // Update UI di main thread
                mainThreadHandler.post(() -> {
                    // Pastikan fragment masih terpasang dan binding tidak null
                    if (isAdded() && binding != null) {
                        if (getActivity() != null) {
                            getActivity().setTitle(name);
                        }
                        binding.textViewProductName.setText(name);
                        binding.textViewProductPrice.setText("Rp " + price);
                        binding.textViewProductDescription.setText(description);
                        Glide.with(requireContext()).load(imagePath).placeholder(R.drawable.pp).into(binding.imageViewProduct);
                    }
                });
            }
        });
    }

    private void addToCart() {
        if (getActivity() == null) return;

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);

        if (username == null) {
            Toast.makeText(getContext(), "Anda harus login untuk menambah ke keranjang", Toast.LENGTH_SHORT).show();
            return;
        }

        // SESUDAH
        executor.execute(() -> {
            dbHelper.addToCart(username, productId); // Langsung panggil saja
            mainThreadHandler.post(() -> {
                // Langsung tampilkan pesan berhasil (mengasumsikan selalu sukses)
                Toast.makeText(getContext(), "Produk ditambahkan ke keranjang!", Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public void onDestroyView() {
        // Hentikan dan matikan TTS engine untuk mencegah memory leak
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroyView();
        binding = null; // Wajib untuk mencegah memory leak
    }
}
