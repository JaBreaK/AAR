package com.adetrifauzananisarahel.model;

// Model untuk satu item di dalam array cartItems pada payload
public class OrderItemPayload {
    private int id;
    private String nama_produk;
    private int harga;
    private int jumlah;

    public OrderItemPayload(int id, String nama_produk, int harga, int jumlah) {
        this.id = id;
        this.nama_produk = nama_produk;
        this.harga = harga;
        this.jumlah = jumlah;
    }
}