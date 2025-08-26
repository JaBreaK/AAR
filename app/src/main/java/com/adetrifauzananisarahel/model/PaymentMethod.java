package com.adetrifauzananisarahel.model;

import com.google.gson.annotations.SerializedName;

public class PaymentMethod {
    @SerializedName("id")
    private int id;
    @SerializedName("nama_metode")
    private String namaMetode;
    @SerializedName("is_active")
    private boolean isActive;

    // Getter
    public int getId() { return id; }
    public String getNamaMetode() { return namaMetode; }
    public boolean isActive() { return isActive; }
}