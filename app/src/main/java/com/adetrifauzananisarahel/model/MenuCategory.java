package com.adetrifauzananisarahel.model;

import android.view.MenuItem;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MenuCategory {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    // Ini bagian pentingnya: setiap kategori punya daftar item
    @SerializedName("items")
    private List<FoodItem> items;

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<FoodItem> getItems() { return items; }
}