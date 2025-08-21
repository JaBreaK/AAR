package com.adetrifauzananisarahel.model; // Pastikan package ini sesuai

import com.google.gson.annotations.SerializedName;

public class FoodItem {

    @SerializedName("id")
    private String id;

    // Kita tidak butuh category_id di sini karena data sudah terkelompok
    // tapi jika butuh untuk keperluan lain, bisa ditambahkan.

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("price")
    private double price;

    @SerializedName("image_url")
    private String imageUrl; // Tambahkan ini jika belum ada

    @SerializedName("stock")
    private int stock;

    @SerializedName("is_available")
    private int isAvailable;

    @SerializedName("is_popular")
    private int isPopular;

    // Buat Getter untuk semua field di atas (Alt + Insert -> Getter)
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public int getStock() { return stock; }
    public int getIsAvailable() { return isAvailable; }
    public int getIsPopular() { return isPopular; }
}