package com.adetrifauzananisarahel.model; // Pastikan nama package ini sesuai dengan project kamu

import com.google.gson.annotations.SerializedName;

/**
 * Ini adalah kelas generik (umum) untuk membungkus semua respons dari API.
 * Tipe <T> artinya 'Tipe data apa saja'. Jadi kelas ini bisa dipakai untuk
 * membungkus FoodItem, List<MenuCategory>, atau data lainnya dari server.
 */
public class ApiResponse<T> {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    // =======================================================================================
    // PERBAIKAN UTAMA: Hapus 'List<>' agar 'data' bisa menjadi tipe apa saja (T),
    // baik itu satu objek (FoodItem) maupun sebuah daftar (List<MenuCategory>).
    // =======================================================================================
    @SerializedName("data")
    private T data;

    // --- Getters ---
    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    // Getter ini sekarang akan mengembalikan tipe data yang benar sesuai kebutuhan
    public T getData() {
        return data;
    }
}