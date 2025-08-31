package com.ayamgorengsuharti.model;
import com.google.gson.annotations.SerializedName;
public class PaymentInfo {
    @SerializedName("metodepembayaran") private PaymentMethodDetail metodePembayaran;
    // Getter

    public PaymentMethodDetail getMetodePembayaran() {
        return metodePembayaran;
    }
}