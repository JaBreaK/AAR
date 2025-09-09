package com.ayamgorengsuharti.ui.transaksi;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ayamgorengsuharti.R;
import com.ayamgorengsuharti.adapter.TransaksiAdapter;
import com.ayamgorengsuharti.databinding.FragmentTransaksiBinding;
import com.ayamgorengsuharti.model.Order;
import com.ayamgorengsuharti.network.ApiClient;
import com.ayamgorengsuharti.network.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransaksiFragment extends Fragment implements TransaksiAdapter.OnOrderClickListener {

    private FragmentTransaksiBinding binding;
    private TransaksiAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTransaksiBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();

        // Ambil nomor WA yang tersimpan di SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);
        String nomorWa = prefs.getString("nomorWa", null);

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            if (nomorWa != null && !nomorWa.isEmpty()) {
                fetchOrders(nomorWa); // Panggil ulang API saat di-refresh
            } else {
                binding.swipeRefreshLayout.setRefreshing(false); // Hentikan animasi refresh
            }
        });

        if (nomorWa != null && !nomorWa.isEmpty()) {
            // Jika nomor WA ada, ambil data riwayat pesanan
            fetchOrders(nomorWa);
        } else {
            // Jika tidak ada (user baru), tampilkan pesan
            binding.progressBar.setVisibility(View.GONE);
            binding.tvEmptyState.setVisibility(View.VISIBLE);
        }
    }

    private void setupRecyclerView() {
        adapter = new TransaksiAdapter(this);
        binding.recyclerViewTransaksi.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewTransaksi.setAdapter(adapter);
    }

    private void fetchOrders(String nomorWa) {
        if (!binding.swipeRefreshLayout.isRefreshing()) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        binding.tvEmptyState.setVisibility(View.GONE);

        ApiClient.getClient(requireContext()).create(ApiService.class).getOrdersByWa(nomorWa).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(@NonNull Call<List<Order>> call, @NonNull Response<List<Order>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isEmpty()) {
                        // Jika API mengembalikan list kosong
                        binding.tvEmptyState.setVisibility(View.VISIBLE);
                    } else {
                        // Jika ada data, tampilkan di adapter
                        adapter.submitList(response.body());
                    }
                } else {
                    binding.tvEmptyState.setText("Gagal memuat riwayat pesanan.");
                    binding.tvEmptyState.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Order>> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.tvEmptyState.setText("Terjadi kesalahan jaringan.");
                binding.tvEmptyState.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Method ini dipanggil dari TransaksiAdapter saat salah satu item pesanan di-klik.
     * @param order Objek pesanan yang di-klik.
     */
    @Override
    public void onOrderClick(Order order) {
        // Pindah ke halaman detail pesanan sambil mengirim ID pesanan
        Bundle bundle = new Bundle();
        bundle.putInt("ORDER_ID", order.getId());
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_navigation_transaksi_to_orderDetailFragment, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}