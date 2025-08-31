package com.ayamgorengsuharti.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Order {
    @SerializedName("id") private int id;
    @SerializedName("waktu_order") private String waktuOrder;
    @SerializedName("nama_pelanggan") private String namaPelanggan;
    @SerializedName("total_harga") private double totalHarga;
    @SerializedName("status_pembayaran") private String statusPembayaran;
    @SerializedName("status_pesanan") private String statusPesanan;
    @SerializedName("catatan_pelanggan") private String catatanPelanggan;
    @SerializedName("orderitems") private List<OrderItem> orderItems;
    @SerializedName("pembayaran") private List<PaymentInfo> pembayaran;

    // Buat Getter untuk semua field di atas

    public int getId() {
        return id;
    }

    public String getWaktuOrder() {
        return waktuOrder;
    }

    public String getNamaPelanggan() {
        return namaPelanggan;
    }

    public double getTotalHarga() {
        return totalHarga;
    }

    public String getStatusPembayaran() {
        return statusPembayaran;
    }

    public String getStatusPesanan() {
        return statusPesanan;
    }

    public String getCatatanPelanggan() {
        return catatanPelanggan;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public List<PaymentInfo> getPembayaran() {
        return pembayaran;
    }
}