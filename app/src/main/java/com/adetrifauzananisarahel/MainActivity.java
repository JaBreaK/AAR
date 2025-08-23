package com.adetrifauzananisarahel;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.adetrifauzananisarahel.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IConfigurationProvider provider = Configuration.getInstance();
        provider.load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        // Set User Agent untuk menghindari diblokir oleh server OSM
        provider.setUserAgentValue(getPackageName());
        // =======================================================

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());




        BottomNavigationView navView = binding.navView;

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_cart, R.id.navigation_transaksi, R.id.navigation_profile)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        NavigationUI.setupWithNavController(binding.navView, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.navigation_home ||
                    destination.getId() == R.id.navigation_cart ||
                    destination.getId() == R.id.navigation_transaksi ||
                    destination.getId() == R.id.navigation_profile) {
                binding.navView.setVisibility(View.VISIBLE);
            } else {
                binding.navView.setVisibility(View.GONE);
            }
        });

        // --- KODE UNTUK WARNA IKON NAVIGASI BAWAH ---
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
    }


}