package com.adetrifauzananisarahel.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class Transaction implements Parcelable {
    private long id;
    private String itemsJson;
    private double totalPrice;
    private String address; // Alamat tujuan (user)
    private String originAddress; // Alamat asal (produk) <-- TAMBAHKAN INI
    private String courier;
    private String paymentMethod;
    private String timestamp;

    public Transaction(long id, String itemsJson, double totalPrice, String address, String originAddress, String courier, String paymentMethod, String timestamp) { // <-- UPDATE CONSTRUCTOR
        this.id = id;
        this.itemsJson = itemsJson;
        this.totalPrice = totalPrice;
        this.address = address;
        this.originAddress = originAddress; // <-- TAMBAHKAN INI
        this.courier = courier;
        this.paymentMethod = paymentMethod;
        this.timestamp = timestamp;
    }

    // --- PARCELABLE METHODS ---
    protected Transaction(Parcel in) {
        id = in.readLong();
        itemsJson = in.readString();
        totalPrice = in.readDouble();
        address = in.readString();
        originAddress = in.readString(); // <-- TAMBAHKAN INI
        courier = in.readString();
        paymentMethod = in.readString();
        timestamp = in.readString();
    }

    public static final Creator<Transaction> CREATOR = new Creator<Transaction>() {
        @Override
        public Transaction createFromParcel(Parcel in) { return new Transaction(in); }
        @Override
        public Transaction[] newArray(int size) { return new Transaction[size]; }
    };

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(itemsJson);
        dest.writeDouble(totalPrice);
        dest.writeString(address);
        dest.writeString(originAddress); // <-- TAMBAHKAN INI
        dest.writeString(courier);
        dest.writeString(paymentMethod);
        dest.writeString(timestamp);
    }

    // --- GETTERS ---
    public long getId() { return id; }
    public double getTotalPrice() { return totalPrice; }
    public String getAddress() { return address; }
    public String getOriginAddress() { return originAddress; } // <-- TAMBAHKAN INI
    public String getCourier() { return courier; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getTimestamp() { return timestamp; }

    public List<CartItem> getItems() {
        if (itemsJson == null || itemsJson.isEmpty()) return Collections.emptyList();
        Gson gson = new Gson();
        Type type = new TypeToken<List<CartItem>>() {}.getType();
        return gson.fromJson(itemsJson, type);
    }
}