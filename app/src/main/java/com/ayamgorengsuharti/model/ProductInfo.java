package com.ayamgorengsuharti.model;
import com.google.gson.annotations.SerializedName;
public class ProductInfo {
    @SerializedName("nama_produk") private String namaProduk;
    @SerializedName("gambar_url") private String gambarUrl;
    // Getter
    // --- TAMBAHKAN FIELD INI ---
    @SerializedName("harga")
    private int harga;

    public String getNamaProduk() {
        return namaProduk;
    }

    public String getGambarUrl() {
        return gambarUrl;
    }

    // --- TAMBAHKAN GETTER INI ---
    public int getHarga() {
        return harga;
    }
}