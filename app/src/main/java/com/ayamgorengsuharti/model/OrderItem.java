package com.ayamgorengsuharti.model;
import com.google.gson.annotations.SerializedName;
public class OrderItem {
    @SerializedName("jumlah") private int jumlah;
    @SerializedName("subtotal") private double subtotal;
    @SerializedName("produk") private ProductInfo produk;
    // Getter

    public int getJumlah() {
        return jumlah;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public ProductInfo getProduk() {
        return produk;
    }
}