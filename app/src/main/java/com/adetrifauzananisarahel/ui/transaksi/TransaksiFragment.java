package com.adetrifauzananisarahel.ui.transaksi;

import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.adetrifauzananisarahel.R;
import com.adetrifauzananisarahel.adapter.TransactionAdapter;
import com.adetrifauzananisarahel.network.DatabaseContract;
import com.adetrifauzananisarahel.network.DatabaseHelper;
import com.adetrifauzananisarahel.databinding.FragmentTransaksiBinding;
import com.adetrifauzananisarahel.model.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransaksiFragment extends Fragment implements TransactionAdapter.OnTransactionClickListener {

    private FragmentTransaksiBinding binding;
    private DatabaseHelper dbHelper;
    private TransactionAdapter adapter;
    private final List<Transaction> transactionList = new ArrayList<>();
    private String currentUsername;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTransaksiBinding.inflate(inflater, container, false);
        dbHelper = new DatabaseHelper(getContext());
        SharedPreferences prefs = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        currentUsername = prefs.getString("username", null);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTransactionData();
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(transactionList, this);
        binding.recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewTransactions.setAdapter(adapter);
    }

    private void loadTransactionData() {
        if (currentUsername == null) {
            updateUIForEmptyState();
            return;
        }

        executor.execute(() -> {
            List<Transaction> loadedTransactions = new ArrayList<>();
            Cursor cursor = dbHelper.getTransactions(currentUsername);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.TransactionEntry._ID));
                    String itemsJson = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.TransactionEntry.COLUMN_ITEMS_JSON));
                    double totalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.TransactionEntry.COLUMN_TOTAL_PRICE));
                    String address = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.TransactionEntry.COLUMN_ADDRESS));
                    String courier = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.TransactionEntry.COLUMN_COURIER));
                    String payment = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.TransactionEntry.COLUMN_PAYMENT_METHOD));
                    String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.TransactionEntry.COLUMN_TIMESTAMP));

                    // =======================================================
                    // 1. AMBIL DATA ALAMAT ASAL DARI CURSOR
                    // =======================================================
                    String originAddress = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.TransactionEntry.COLUMN_ORIGIN_ADDRESS));

                    // =======================================================
                    // 2. GUNAKAN CONSTRUCTOR DENGAN 8 PARAMETER
                    // =======================================================
                    loadedTransactions.add(new Transaction(id, itemsJson, totalPrice, address, originAddress, courier, payment, timestamp));

                } while (cursor.moveToNext());
                cursor.close();
            }

            mainThreadHandler.post(() -> {
                transactionList.clear();
                transactionList.addAll(loadedTransactions);
                adapter.notifyDataSetChanged();

                if (transactionList.isEmpty()) {
                    updateUIForEmptyState();
                } else {
                    updateUIForNonEmptyState();
                }
            });
        });
    }

    private void updateUIForEmptyState() {
        binding.recyclerViewTransactions.setVisibility(View.GONE);
        binding.textViewEmptyTransaksi.setVisibility(View.VISIBLE);
    }

    private void updateUIForNonEmptyState() {
        binding.recyclerViewTransactions.setVisibility(View.VISIBLE);
        binding.textViewEmptyTransaksi.setVisibility(View.GONE);
    }

    @Override
    public void onTransactionClick(Transaction transaction) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("TRANSACTION_DATA", transaction);
        NavHostFragment.findNavController(this).navigate(R.id.action_transaksi_to_detail, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}