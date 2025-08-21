package com.adetrifauzananisarahel; // Letakkan di package utama

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.adetrifauzananisarahel.ui.auth.AuthActivity;

public class    LauncherActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        Intent intent;
        if (isLoggedIn) {
            // Jika sudah login, langsung ke MainActivity
            intent = new Intent(this, MainActivity.class);
        } else {
            // Jika belum, ke AuthActivity (halaman login/daftar)
            intent = new Intent(this, AuthActivity.class);
        }
        startActivity(intent);
        finish(); // Tutup LauncherActivity agar tidak bisa kembali ke sini
    }
}