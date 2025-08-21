package com.adetrifauzananisarahel.model;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private User data;

    // Buat Getter
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public User getData() { return data; }
}