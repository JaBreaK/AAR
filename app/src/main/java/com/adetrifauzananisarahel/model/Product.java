package com.adetrifauzananisarahel.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

public class Product implements Parcelable {
    private long id;
    private String name;
    private String price;
    private String imagePath;
    private String description;
    private String address; // <-- TAMBAHKAN INI

    public Product(long id, String name, String price, String imagePath, String description, String address) { // <-- UPDATE CONSTRUCTOR
        this.id = id;
        this.name = name;
        this.price = price;
        this.imagePath = imagePath;
        this.description = description;
        this.address = address; // <-- TAMBAHKAN INI
    }

    // --- PARCELABLE METHODS ---
    protected Product(Parcel in) {
        id = in.readLong();
        name = in.readString();
        price = in.readString();
        imagePath = in.readString();
        description = in.readString();
        address = in.readString(); // <-- TAMBAHKAN INI
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(price);
        dest.writeString(imagePath);
        dest.writeString(description);
        dest.writeString(address); // <-- TAMBAHKAN INI
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    // --- GETTERS ---
    public long getId() { return id; }
    public String getName() { return name; }
    public String getPrice() { return price; }
    public String getImagePath() { return imagePath; }
    public String getDescription() { return description; }
    public String getAddress() { return address; } // <-- TAMBAHKAN INI
}