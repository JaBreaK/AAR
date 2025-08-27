package com.adetrifauzananisarahel.ui.order;

import static org.osmdroid.tileprovider.cachemanager.CacheManager.getFileName;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;



import com.adetrifauzananisarahel.R;
import com.adetrifauzananisarahel.adapter.OrderDetailAdapter;
import com.adetrifauzananisarahel.databinding.FragmentOrderDetailBinding;
import com.adetrifauzananisarahel.model.Order;
import com.adetrifauzananisarahel.model.PaymentInfo;
import com.adetrifauzananisarahel.network.ApiClient;
import com.adetrifauzananisarahel.network.ApiService;
import com.bumptech.glide.Glide;

import java.io.InputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.adetrifauzananisarahel.util.NotificationHelper;

public class OrderDetailFragment extends Fragment {

    private FragmentOrderDetailBinding binding;
    private int orderId;
    private OrderDetailAdapter adapter;
    private String nomorWa;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private Uri selectedImageUri;
    // --- BARU: Variabel untuk Polling ---
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pollingRunnable;
    private final long POLLING_INTERVAL = 20000; // 30 detik
    private String currentOrderStatus; // Untuk menyimpan status terakhir
    private boolean isFirstLoad = true; // Untuk menandai pemuatan pertama
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Izin diberikan. Notifikasi sekarang bisa muncul.
                    Toast.makeText(getContext(), "Izin notifikasi diberikan.", Toast.LENGTH_SHORT).show();
                } else {
                    // Izin ditolak. Beri tahu user kenapa izin ini penting.
                    Toast.makeText(getContext(), "Tanpa izin, Anda tidak akan mendapat update status pesanan.", Toast.LENGTH_LONG).show();
                }
            });


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationHelper.createNotificationChannel(requireContext());

        imagePickerLauncher = registerForActivityResult(
                // PASTIKAN MENGGUNAKAN "GetContent()" BUKAN "StartActivityForResult()"
                new ActivityResultContracts.GetContent(),
                uri -> {
                    // Callback ini akan berjalan setelah user memilih gambar
                    if (uri != null) {
                        selectedImageUri = uri; // Simpan Uri gambar yang dipilih
                        String fileName = getFileName(selectedImageUri);
                        binding.tvSelectedFileName.setText(fileName);
                    }
                }
        );

        if (getArguments() != null) {
            orderId = getArguments().getInt("ORDER_ID");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrderDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        askNotificationPermission();

        binding.tvBackToMenu.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack(R.id.navigation_home, false)
        );

        SharedPreferences prefs = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);
        nomorWa = prefs.getString("nomorWa", null);

        if (orderId > 0 && nomorWa != null) {
            fetchOrderDetail(orderId, nomorWa);
        } else {
            Toast.makeText(getContext(), "Data pesanan tidak valid", Toast.LENGTH_SHORT).show();
        }
        // --- BARU: Tambahkan listener untuk SwipeRefreshLayout ---
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            if (orderId > 0 && nomorWa != null) {
                fetchOrderDetail(orderId, nomorWa); // Panggil ulang API saat di-refresh
            } else {
                binding.swipeRefreshLayout.setRefreshing(false); // Hentikan animasi jika tidak ada data
            }
        });

        if (orderId > 0 && nomorWa != null) {
            fetchOrderDetail(orderId, nomorWa);
        } else {
            Toast.makeText(getContext(), "Data pesanan tidak valid", Toast.LENGTH_SHORT).show();
        }
    }
    // --- BARU: Lifecycle methods untuk memulai dan menghentikan polling ---
    @Override
    public void onResume() {
        super.onResume();
        if (orderId > 0 && nomorWa != null) {
            isFirstLoad = true; // Setel ulang saat kembali ke halaman ini
            startPolling();
        } else {
            Toast.makeText(getContext(), "Data pesanan tidak valid", Toast.LENGTH_SHORT).show();
        }
    }

    private void askNotificationPermission() {
        // Izin ini hanya diperlukan untuk Android 13 (API 33) ke atas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                // Jika izin belum ada, minta izin ke user
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
            // Jika izin sudah ada, tidak perlu melakukan apa-apa
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopPolling();
    }
    // --- AKHIR BARU ---
    private void startPolling() {
        pollingRunnable = () -> {
            fetchOrderDetail(orderId, nomorWa);
            // Jadwalkan pengecekan berikutnya setelah interval
            handler.postDelayed(pollingRunnable, POLLING_INTERVAL);
        };
        // Mulai pengecekan pertama kali segera
        handler.post(pollingRunnable);
    }

    private void stopPolling() {
        if (pollingRunnable != null) {
            handler.removeCallbacks(pollingRunnable);
        }
    }

    private void setupRecyclerView() {
        adapter = new OrderDetailAdapter();
        binding.recyclerViewOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewOrderItems.setAdapter(adapter);
    }

    private void fetchOrderDetail(int id, String wa) {
        if (isFirstLoad) {
            binding.progressBarDetail.setVisibility(View.VISIBLE);
            binding.contentLayout.setVisibility(View.GONE);
        }
        if (!binding.swipeRefreshLayout.isRefreshing()) {
            binding.progressBarDetail.setVisibility(View.VISIBLE);
        }
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getOrderDetail(id, wa).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                binding.progressBarDetail.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    binding.contentLayout.setVisibility(View.VISIBLE);
                    Order orderData = response.body();
                    String newStatus = orderData.getStatusPesanan();

                    if (currentOrderStatus != null && !currentOrderStatus.equals(newStatus)) {
                        String notificationText = "Pesanan #" + orderId + " sekarang: " + newStatus.replace("_", " ");
                        NotificationHelper.showNotification(requireContext(), "Status Pesanan Berubah!", notificationText);
                    }
                    // Update status terakhir
                    currentOrderStatus = newStatus;if (currentOrderStatus != null && !currentOrderStatus.equals(newStatus)) {
                        String notificationText = "Pesanan #" + orderId + " sekarang: " + newStatus.replace("_", " ");
                        NotificationHelper.showNotification(requireContext(), "Status Pesanan Berubah!", notificationText);
                    }
                    // Update status terakhir
                    currentOrderStatus = newStatus;
                    populateUI(orderData);
                    populateUI(response.body());
                } else {
                    Toast.makeText(getContext(), "Gagal memuat detail pesanan", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                binding.progressBarDetail.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- METHOD INI DIROMBAK TOTAL ---
    private void populateUI(Order data) {
        // Header
        binding.tvOrderDetailTitle.setText("Detail Pesanan #" + data.getId());

        // Status
        String statusPesanan = data.getStatusPesanan();
        String statusPembayaran = data.getStatusPembayaran();

        // Header (tidak berubah)
        binding.tvOrderDetailTitle.setText("Detail Pesanan #" + data.getId());

        // --- LOGIKA BARU UNTUK STATUS BOX ---
        String statusTextToShow;
        String statusSubTextToShow;
        int statusColorRes;
        int statusTextColorRes = R.color.black; // Default warna teks hitam
        Context context = requireContext();

        // Tentukan Judul, Sub-Teks, dan Warna berdasarkan kombinasi status
        if ("DIBATALKAN".equals(statusPesanan)) {
            statusTextToShow = "Pesanan Dibatalkan";
            statusSubTextToShow = "Pesanan ini telah dibatalkan.";
            statusColorRes = R.color.red;
            statusTextColorRes = R.color.white;
        } else if ("SELESAI".equals(statusPesanan)) {
            statusTextToShow = "Pesanan Selesai";
            statusSubTextToShow = "Terima kasih telah memesan dari kami.";
            statusColorRes = R.color.gelap; // Warna baru sesuai screenshot
            statusTextColorRes = R.color.white;
        } else if ("SIAP_DIAMBIL".equals(statusPesanan)) {
            statusTextToShow = "Pesanan Siap Diambil!";
            statusSubTextToShow = "Hore! Pesananmu sudah selesai dimasak dan siap untuk diambil.";
            statusColorRes = R.color.green;
            statusTextColorRes = R.color.white;
        } else if ("SEDANG_DIMASAK".equals(statusPesanan)) {
            statusTextToShow = "Pesanan Sedang Dimasak!";
            statusSubTextToShow = "Tim dapur kami sedang menyiapkan pesananmu. Mohon ditunggu!";
            statusColorRes = R.color.hijau_muda;
            statusTextColorRes = R.color.white;
        } else if ("SEDANG_DIPROSES".equals(statusPesanan) ) {
            statusTextToShow = "Sedang Disiapkan";
            statusSubTextToShow = "Tim dapur kami sedang menyiapkan pesananmu. Mohon ditunggu!";
            statusColorRes = R.color.blue;
            statusTextColorRes = R.color.white;
        } else if ("PESANAN_DITERIMA".equals(statusPesanan)) {
            switch (statusPembayaran) {
                case "LUNAS":
                    statusTextToShow = "Pembayaran Lunas";
                    statusSubTextToShow = "Pesananmu telah kami terima dan akan segera masuk antrean dapur.";
                    statusColorRes = R.color.green;
                    statusTextColorRes = R.color.white;
                    break;
                case "MENUNGGU_KONFIRMASI":
                    statusTextToShow = "Pembayaran Sedang Diverifikasi";
                    statusSubTextToShow = "Bukti pembayaranmu sudah kami terima dan akan segera diperiksa.";
                    statusColorRes = R.color.blue;
                    statusTextColorRes = R.color.white;
                    break;
                case "BELUM_BAYAR":
                default:
                    statusTextToShow = "Menunggu Pembayaran";
                    statusSubTextToShow = "Silakan lakukan pembayaran dan upload bukti di bawah ini.";
                    statusColorRes = R.color.yellow;
                    statusTextColorRes = R.color.white;
                    break;
            }
        } else {
            // Fallback untuk status tak terduga
            statusTextToShow = statusPesanan.replace("_", " ");
            statusSubTextToShow = "Status pesanan sedang diproses.";
            statusColorRes = R.color.yellow;
            statusTextColorRes = R.color.white;
        }

        // Terapkan semua perubahan ke UI
        binding.tvOrderStatusDetail.setText(statusTextToShow);
        binding.tvOrderStatusSubtitle.setText(statusSubTextToShow);
        binding.cardStatus.setCardBackgroundColor(ContextCompat.getColor(context, statusColorRes));
        binding.tvOrderStatusDetail.setTextColor(ContextCompat.getColor(context, statusTextColorRes));
        binding.tvOrderStatusSubtitle.setTextColor(ContextCompat.getColor(context, statusTextColorRes));
        // --- AKHIR LOGIKA STATUS BOX ---

        binding.btnConfirmPayment.setOnClickListener(v -> uploadProof());



        // Ringkasan
        binding.tvCustomerNameDetail.setText("Nama: " + data.getNamaPelanggan());
        binding.tvOrderTimeDetail.setText("Waktu: " + formatDateTime(data.getWaktuOrder()));
        if (data.getCatatanPelanggan() != null && !data.getCatatanPelanggan().isEmpty()) {
            binding.tvCustomerNotesDetail.setText(data.getCatatanPelanggan());
        } else {
            binding.tvCustomerNotesDetail.setText("-");
        }

        adapter.submitList(data.getOrderItems());

        // Total
        Locale localeID = new Locale("in", "ID");
        NumberFormat format = NumberFormat.getCurrencyInstance(localeID);
        format.setMaximumFractionDigits(0);
        binding.tvTotalPriceDetail.setText(format.format(data.getTotalHarga()));

        // Logika untuk menampilkan metode pembayaran
        if (data.getPembayaran() != null && !data.getPembayaran().isEmpty()) {
            PaymentInfo payment = data.getPembayaran().get(0);
            String qrisUrl = payment.getMetodePembayaran().getGambarQrisUrl();

            if (qrisUrl != null && !qrisUrl.isEmpty()) {
                binding.ivQris.setVisibility(View.VISIBLE);
                binding.layoutBankTransfer.setVisibility(View.GONE);
                Glide.with(this).load(qrisUrl).into(binding.ivQris);
            } else {
                binding.ivQris.setVisibility(View.GONE);
                binding.layoutBankTransfer.setVisibility(View.VISIBLE);
                binding.tvPaymentMethodName.setText(payment.getMetodePembayaran().getNamaMetode());
                binding.tvAccountNumber.setText("No. Rek: " + payment.getMetodePembayaran().getNomorRekening());
                binding.tvAccountName.setText("a.n: " + payment.getMetodePembayaran().getNamaRekening());
            }
        }


        // Cek status pembayaran untuk menampilkan/menyembunyikan instruksi
        if ("BELUM_BAYAR".equals(statusPembayaran)) {
            // Jika status BELUM_BAYAR, tampilkan instruksi
            binding.layoutPaymentInstructions.setVisibility(View.VISIBLE);

            // Logika untuk menampilkan QRIS atau Transfer Bank (tidak berubah)
            if (data.getPembayaran() != null && !data.getPembayaran().isEmpty()) {
                PaymentInfo payment = data.getPembayaran().get(0);
                String qrisUrl = payment.getMetodePembayaran().getGambarQrisUrl();

                if (qrisUrl != null && !qrisUrl.isEmpty()) {
                    binding.ivQris.setVisibility(View.VISIBLE);
                    binding.layoutBankTransfer.setVisibility(View.GONE);
                    Glide.with(this).load(qrisUrl).into(binding.ivQris);
                } else {
                    binding.ivQris.setVisibility(View.GONE);
                    binding.layoutBankTransfer.setVisibility(View.VISIBLE);
                    binding.tvPaymentMethodName.setText(payment.getMetodePembayaran().getNamaMetode());
                    binding.tvAccountNumber.setText("No. Rek: " + payment.getMetodePembayaran().getNomorRekening());
                    binding.tvAccountName.setText("a.n: " + payment.getMetodePembayaran().getNamaRekening());
                }
            }
        } else {
            // Jika statusnya BUKAN BELUM_BAYAR (misal: LUNAS, MENUNGGU_KONFIRMASI, dll)
            // SEMBUNYIKAN seluruh bagian instruksi pembayaran
            binding.layoutPaymentInstructions.setVisibility(View.GONE);
        }

        // Listener untuk tombol upload (UI-nya sudah siap)
        binding.btnChooseFile.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        binding.btnConfirmPayment.setOnClickListener(v -> uploadProof());


    }
    // Method untuk membuka galeri
    private void openImagePicker() {
        // Cukup panggil launch dengan tipe filenya saja
        imagePickerLauncher.launch("image/*"); // <-- INI YANG BENAR
    }
    // Method utama untuk proses upload
    private void uploadProof() {
        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "Silakan pilih file bukti pembayaran", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Tampilkan loading
            binding.progressBarDetail.setVisibility(View.VISIBLE);
            binding.contentLayout.setAlpha(0.5f);

            // Ubah Uri menjadi format yang bisa diupload Retrofit (MultipartBody.Part)
            InputStream inputStream = requireContext().getContentResolver().openInputStream(selectedImageUri);
            byte[] fileBytes = new byte[inputStream.available()];
            inputStream.read(fileBytes);
            inputStream.close();

            RequestBody requestFile = RequestBody.create(MediaType.parse(requireContext().getContentResolver().getType(selectedImageUri)), fileBytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("bukti", getFileName(selectedImageUri), requestFile);

            // Panggil API
            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            apiService.uploadPaymentProof(orderId, body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    binding.progressBarDetail.setVisibility(View.GONE);
                    binding.contentLayout.setAlpha(1.0f);
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Upload bukti berhasil!", Toast.LENGTH_LONG).show();
                        // Refresh halaman untuk melihat status baru
                        fetchOrderDetail(orderId, nomorWa);
                    } else {
                        Toast.makeText(getContext(), "Upload gagal, coba lagi.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    binding.progressBarDetail.setVisibility(View.GONE);
                    binding.contentLayout.setAlpha(1.0f);
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            binding.progressBarDetail.setVisibility(View.GONE);
            binding.contentLayout.setAlpha(1.0f);
        }
    }

    // Helper method untuk mendapatkan nama file dari Uri
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    // --- METHOD BARU UNTUK FORMAT TANGGAL ---
    private String formatDateTime(String isoDate) {
        if (isoDate == null) return "-";
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            Date date = isoFormat.parse(isoDate);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("id", "ID"));
            return outputFormat.format(date);
        } catch (Exception e) {
            return isoDate;
        }
    }
}