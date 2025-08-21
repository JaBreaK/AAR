package com.adetrifauzananisarahel.model;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("id")
    private String id;

    @SerializedName("full_name")
    private String full_name;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    @SerializedName("role")
    private String role;

    // Buat Getter untuk semua field di atas
    // (Bisa generate otomatis di Android Studio: Alt + Insert -> Getter)

    public String getId() { return id; }
    public String getName() { return full_name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
}