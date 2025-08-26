package com.adetrifauzananisarahel.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class OrderResponse implements Parcelable {
    @SerializedName("id")
    private int id;
    @SerializedName("nama_pelanggan")
    private String namaPelanggan;
    @SerializedName("nomor_wa")
    private String nomorWa;
    @SerializedName("total_harga")
    private double totalHarga;
    @SerializedName("status_pembayaran")
    private String statusPembayaran;
    @SerializedName("status_pesanan")
    private String statusPesanan;

    // --- Kode Parcelable (bisa digenerate otomatis) ---
    protected OrderResponse(Parcel in) {
        id = in.readInt();
        namaPelanggan = in.readString();
        nomorWa = in.readString();
        totalHarga = in.readDouble();
        statusPembayaran = in.readString();
        statusPesanan = in.readString();
    }

    public static final Creator<OrderResponse> CREATOR = new Creator<OrderResponse>() {
        @Override
        public OrderResponse createFromParcel(Parcel in) { return new OrderResponse(in); }
        @Override
        public OrderResponse[] newArray(int size) { return new OrderResponse[size]; }
    };
    @Override
    public int describeContents() { return 0; }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(namaPelanggan);
        dest.writeString(nomorWa);
        dest.writeDouble(totalHarga);
        dest.writeString(statusPembayaran);
        dest.writeString(statusPesanan);
    }
    // --- Getter ---
    public int getId() { return id; }
    public String getNamaPelanggan() { return namaPelanggan; }
    public double getTotalHarga() { return totalHarga; }
    public String getStatusPesanan() { return statusPesanan; }
}