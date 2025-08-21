package com.adetrifauzananisarahel.ui.admin;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.adetrifauzananisarahel.R;
import com.adetrifauzananisarahel.adapter.AdminProductAdapter;
import com.adetrifauzananisarahel.network.DatabaseContract;
import com.adetrifauzananisarahel.network.DatabaseHelper;
import com.adetrifauzananisarahel.databinding.FragmentAdminManageProductsBinding;
import com.adetrifauzananisarahel.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminManageProductsFragment extends Fragment implements AdminProductAdapter.OnItemClickListener {

    private FragmentAdminManageProductsBinding binding;
    private DatabaseHelper dbHelper;
    private AdminProductAdapter adminProductAdapter;
    private final List<Product> productList = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminManageProductsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Kelola Produk");
        dbHelper = new DatabaseHelper(getContext());
        setupRecyclerView();

        binding.fabAddProduct.setOnClickListener(v -> {
            // Navigasi ke form tanpa mengirim ID (mode tambah)
            NavHostFragment.findNavController(AdminManageProductsFragment.this)
                    .navigate(R.id.action_adminManageProductsFragment_to_addEditProductFragment);
        });
    }

    private void setupRecyclerView() {
        adminProductAdapter = new AdminProductAdapter(getContext(), productList, this);
        binding.recyclerViewAdminProducts.setLayoutManager(new GridLayoutManager(getContext(), 2)); // Grid layout 2 kolom
        binding.recyclerViewAdminProducts.setAdapter(adminProductAdapter);
    }

    // Pastikan fragment ini punya executor dan handler, sama seperti fragment lain
// private final ExecutorService executor = Executors.newSingleThreadExecutor();
// private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private void loadProductsFromDb() {
        executor.execute(() -> {
            List<Product> loadedProducts = new ArrayList<>();
            Cursor cursor = dbHelper.getAllProducts();

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry._ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_NAME));
                    String price = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_PRICE));
                    String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_IMAGE_PATH));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_DESCRIPTION));

                    // =======================================================
                    // 1. AMBIL DATA ALAMAT DARI CURSOR
                    // =======================================================
                    String address = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_ADDRESS));

                    // =======================================================
                    // 2. GUNAKAN CONSTRUCTOR DENGAN 6 PARAMETER
                    // =======================================================
                    loadedProducts.add(new Product(id, name, price, imagePath, description, address));

                } while (cursor.moveToNext());
                cursor.close();
            }

            mainThreadHandler.post(() -> {
                productList.clear();
                productList.addAll(loadedProducts);
                adminProductAdapter.notifyDataSetChanged();
            });
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Muat ulang data setiap kali fragment ini ditampilkan, agar selalu update
        loadProductsFromDb();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Ini adalah implementasi dari interface OnItemClickListener di adapter
    @Override
    public void onItemClick(Product product) {
        // Saat item di-klik, navigasi ke form dengan mengirim ID produk (mode edit)
        Bundle bundle = new Bundle();
        bundle.putLong("PRODUCT_ID", product.getId());
        NavHostFragment.findNavController(AdminManageProductsFragment.this)
                .navigate(R.id.action_adminManageProductsFragment_to_addEditProductFragment, bundle);
    }
}