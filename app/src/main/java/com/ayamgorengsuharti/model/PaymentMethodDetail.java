package com.ayamgorengsuharti.model;
import com.google.gson.annotations.SerializedName;
public class PaymentMethodDetail {
    @SerializedName("nama_metode") private String namaMetode;
    @SerializedName("nomor_rekening") private String nomorRekening;
    @SerializedName("nama_rekening") private String namaRekening;
    @SerializedName("gambar_qris_url") private String gambarQrisUrl;
    // Getter

    public String getNamaMetode() {
        return namaMetode;
    }

    public String getNomorRekening() {
        return nomorRekening;
    }

    public String getNamaRekening() {
        return namaRekening;
    }

    public String getGambarQrisUrl() {
        return gambarQrisUrl;
    }
}