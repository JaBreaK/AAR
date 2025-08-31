package com.ayamgorengsuharti.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class MenuItemResponse implements Parcelable {

    @SerializedName("id")
    private int id;

    @SerializedName("nama_produk")
    private String namaProduk;

    @SerializedName("deskripsi")
    private String deskripsi;

    @SerializedName("harga")
    private int harga;

    @SerializedName("gambar_url")
    private String gambarUrl;

    @SerializedName("kategori")
    private Category kategori;

    // --- Konstruktor Parcelable (untuk membaca data) ---
    protected MenuItemResponse(Parcel in) {
        id = in.readInt();
        namaProduk = in.readString();
        deskripsi = in.readString();
        harga = in.readInt();
        gambarUrl = in.readString();
        // Membaca objek Category yang juga Parcelable
        kategori = in.readParcelable(Category.class.getClassLoader());
    }

    // --- CREATOR (wajib ada untuk Parcelable) ---
    public static final Creator<MenuItemResponse> CREATOR = new Creator<MenuItemResponse>() {
        @Override
        public MenuItemResponse createFromParcel(Parcel in) {
            return new MenuItemResponse(in);
        }

        @Override
        public MenuItemResponse[] newArray(int size) {
            return new MenuItemResponse[size];
        }
    };

    // --- Getter Methods (untuk mengakses data) ---
    public int getId() {
        return id;
    }

    public String getNamaProduk() {
        return namaProduk;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public int getHarga() {
        return harga;
    }

    public String getGambarUrl() {
        return gambarUrl;
    }

    public Category getKategori() {
        return kategori;
    }


    // --- Metode Parcelable (untuk menulis data) ---
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(namaProduk);
        dest.writeString(deskripsi);
        dest.writeInt(harga);
        dest.writeString(gambarUrl);
        // Menulis objek Category
        dest.writeParcelable(kategori, flags);
    }
}