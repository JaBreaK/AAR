package com.adetrifauzananisarahel;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class LauncherActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hapus semua logika pengecekan login (SharedPreferences)
        // Langsung arahkan ke MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        // Tutup LauncherActivity agar tidak bisa kembali ke sini dengan tombol back
        finish();
    }
}