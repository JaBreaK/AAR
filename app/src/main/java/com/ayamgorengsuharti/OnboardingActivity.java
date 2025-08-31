package com.ayamgorengsuharti;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.ayamgorengsuharti.adapter.OnboardingAdapter;
import com.ayamgorengsuharti.databinding.ActivityOnboardingBinding;
import com.ayamgorengsuharti.model.OnboardingItem;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ActivityOnboardingBinding binding;
    private OnboardingAdapter adapter;
    private List<OnboardingItem> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupOnboardingItems();
        setupViewPager();
        setupClickListeners();
    }
    private void setupClickListeners() {
        binding.btnNext.setOnClickListener(v -> {
            if (binding.viewPagerOnboarding.getCurrentItem() < items.size() - 1) {
                binding.viewPagerOnboarding.setCurrentItem(binding.viewPagerOnboarding.getCurrentItem() + 1);
            } else {
                finishOnboarding();
            }
        });

        binding.btnPrevious.setOnClickListener(v -> {
            if (binding.viewPagerOnboarding.getCurrentItem() > 0) {
                binding.viewPagerOnboarding.setCurrentItem(binding.viewPagerOnboarding.getCurrentItem() - 1);
            }
        });

        binding.tvSkip.setOnClickListener(v -> finishOnboarding());
    }

    private void setupOnboardingItems() {
        // GANTI DENGAN GAMBAR DAN TEKSMU SENDIRI
        // Buat file drawable untuk gambar-gambar ini di res/drawable
        items.add(new OnboardingItem(R.drawable.ic_onboarding_1, "Pesan Makanan Favoritmu", "Temukan berbagai menu lezat dan pesan dengan beberapa kali klik."));
        items.add(new OnboardingItem(R.drawable.ic_onboarding_2, "Pembayaran Mudah", "Pilih metode pembayaran yang paling nyaman untukmu, dari QRIS hingga transfer bank."));
        items.add(new OnboardingItem(R.drawable.ic_onboarding_3, "Lacak Pesananmu", "Pantau status pesananmu secara real-time, dari dapur hingga siap diambil."));
    }

    private void setupViewPager() {
        adapter = new OnboardingAdapter(items);
        binding.viewPagerOnboarding.setAdapter(adapter);

        // --- HAPUS KODE DI BAWAH INI ---
        // Hapus baris yang ini: new TabLayoutMediator(...)
        // atau baris yang ini: binding.dotsIndicator.setViewPager(...)

        // Sisa kode di bawah ini tidak berubah
        binding.viewPagerOnboarding.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateNavigationButtons(position);
            }
        });

        updateNavigationButtons(0);
    }
    // --- METHOD BARU UNTUK MENGATUR TOMBOL NAVIGASI ---
    private void updateNavigationButtons(int position) {
        if (position == 0) {
            // Halaman pertama
            binding.btnPrevious.setVisibility(View.INVISIBLE);
            binding.tvSkip.setVisibility(View.VISIBLE);
        } else {
            // Halaman tengah
            binding.btnPrevious.setVisibility(View.VISIBLE);
            binding.tvSkip.setVisibility(View.VISIBLE);
        }

        if (position == items.size() - 1) {
            // Halaman terakhir
            binding.btnNext.setText("Mulai Sekarang");
            binding.btnNext.setIcon(null); // Hapus ikon panah
        } else {
            // Halaman sebelum terakhir
            binding.btnNext.setText("Lanjut");
            binding.btnNext.setIconResource(R.drawable.baseline_arrow_forward_24);
        }
    }

    private void finishOnboarding() {
        // ... (isi method ini tidak berubah)
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("isFirstTime", false).apply();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
