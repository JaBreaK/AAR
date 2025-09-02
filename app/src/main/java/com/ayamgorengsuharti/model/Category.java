package com.ayamgorengsuharti.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class Category implements Parcelable {

    @SerializedName("id")
    private int id;

    @SerializedName("nama_kategori")
    private String namaKategori;

    // --- TAMBAHKAN KONSTRUKTOR INI ---
    public Category(int id, String namaKategori) {
        this.id = id;
        this.namaKategori = namaKategori;
    }
    // --- AKHIR TAMBAHAN ---

    protected Category(Parcel in) {
        id = in.readInt();
        namaKategori = in.readString();
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    // --- Getter ---
    public int getId() {
        return id;
    }

    public String getNamaKategori() {
        return namaKategori;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(namaKategori);
    }
}