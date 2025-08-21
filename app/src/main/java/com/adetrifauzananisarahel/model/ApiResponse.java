package com.adetrifauzananisarahel.model; // Pastikan nama package ini sesuai dengan project kamu

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Ini adalah kelas generik (umum) untuk membungkus semua respons dari API.
 * Tipe <T> artinya 'Tipe data apa saja'. Jadi kelas ini bisa dipakai untuk
 * membungkus List<MenuCategory>, List<User>, atau data lainnya dari server.
 */
public class ApiResponse<T> {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<T> data; // 'data' bisa berisi list dengan tipe apa saja

    // --- Getters ---
    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<T> getData() {
        return data;
    }
}