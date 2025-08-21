package com.adetrifauzananisarahel.ui.cart;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.adetrifauzananisarahel.R;
import com.adetrifauzananisarahel.adapter.CartAdapter;
import com.adetrifauzananisarahel.network.DatabaseContract;
import com.adetrifauzananisarahel.network.DatabaseHelper;
import com.adetrifauzananisarahel.databinding.FragmentCartBinding;
import com.adetrifauzananisarahel.model.CartItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CartFragment extends Fragment implements CartAdapter.CartInteractionListener {

    private FragmentCartBinding binding;
    private DatabaseHelper dbHelper;
    private CartAdapter cartAdapter;
    private final List<CartItem> cartItems = new ArrayList<>();
    private String currentUsername;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        dbHelper = new DatabaseHelper(getContext());

        SharedPreferences prefs = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        currentUsername = prefs.getString("username", null);

        setupRecyclerView();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Muat ulang data setiap kali fragment ditampilkan
        loadCartData();
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(cartItems, this);
        binding.recyclerViewCart.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewCart.setAdapter(cartAdapter);
    }

    private void loadCartData() {
        if (currentUsername == null) {
            updateUIForEmptyCart();
            return;
        }

        executor.execute(() -> {
            final List<CartItem> loadedItems = new ArrayList<>();
            final Cursor cursor = dbHelper.getCartItems(currentUsername);
            double totalPrice = 0;

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long cartId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.CartEntry._ID));
                    long productId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.CartEntry.COLUMN_PRODUCT_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.CartEntry.COLUMN_PRODUCT_NAME));
                    String price = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.CartEntry.COLUMN_PRODUCT_PRICE));
                    String image = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.CartEntry.COLUMN_PRODUCT_IMAGE_PATH));
                    int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.CartEntry.COLUMN_QUANTITY));

                    loadedItems.add(new CartItem(cartId, productId, name, price, image, quantity));
                    totalPrice += Double.parseDouble(price.replaceAll("[^\\d]", "")) * quantity;

                } while (cursor.moveToNext());
                cursor.close();
            }

            double finalTotalPrice = totalPrice;
            mainThreadHandler.post(() -> {
                cartItems.clear();
                cartItems.addAll(loadedItems);
                cartAdapter.notifyDataSetChanged();

                if (cartItems.isEmpty()) {
                    updateUIForEmptyCart();
                } else {
                    updateUIForNonEmptyCart(finalTotalPrice);
                }
            });
        });
    }

    private void updateUIForEmptyCart() {
        binding.recyclerViewCart.setVisibility(View.GONE);
        binding.cardSummary.setVisibility(View.GONE);
        binding.textViewEmptyCart.setVisibility(View.VISIBLE);
    }

    private void updateUIForNonEmptyCart(double totalPrice) {
        binding.recyclerViewCart.setVisibility(View.VISIBLE);
        binding.cardSummary.setVisibility(View.VISIBLE);
        binding.textViewEmptyCart.setVisibility(View.GONE);
        binding.textViewTotalPrice.setText("Rp " + String.format("%,.0f", totalPrice));
    }

    @Override
    public void onIncreaseQuantity(CartItem item) {
        executor.execute(() -> {
            dbHelper.updateCartItemQuantity(item.getCartId(), item.getQuantity() + 1);
            mainThreadHandler.post(this::loadCartData);
        });
    }

    @Override
    public void onDecreaseQuantity(CartItem item) {
        if (item.getQuantity() > 1) {
            executor.execute(() -> {
                dbHelper.updateCartItemQuantity(item.getCartId(), item.getQuantity() - 1);
                mainThreadHandler.post(this::loadCartData);
            });
        }
    }

    @Override
    public void onRemoveItem(CartItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Hapus Item")
                .setMessage("Yakin ingin menghapus " + item.getProductName() + " dari keranjang?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    executor.execute(() -> {
                        dbHelper.removeCartItem(item.getCartId());
                        mainThreadHandler.post(this::loadCartData);
                    });
                })
                .setNegativeButton("Batal", null)
                .show();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // ... kode setupRecyclerView ...

        binding.buttonCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(getContext(), "Keranjang Anda kosong!", Toast.LENGTH_SHORT).show();
            } else {
                NavHostFragment.findNavController(this).navigate(R.id.action_cart_to_checkout);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}