package com.adetrifauzananisarahel.model;

import java.util.List;

// Model untuk seluruh body JSON yang dikirim saat order
public class OrderPayload {
    private List<OrderItemPayload> cartItems;
    private String nama_pelanggan;
    private String nomor_wa;
    private double total_harga;
    private int metode_pembayaran_id;
    private String catatan_pelanggan;

    public OrderPayload(List<OrderItemPayload> cartItems, String nama_pelanggan, String nomor_wa, double total_harga, int metode_pembayaran_id, String catatan_pelanggan) {
        this.cartItems = cartItems;
        this.nama_pelanggan = nama_pelanggan;
        this.nomor_wa = nomor_wa;
        this.total_harga = total_harga;
        this.metode_pembayaran_id = metode_pembayaran_id;
        this.catatan_pelanggan = catatan_pelanggan;
    }
}