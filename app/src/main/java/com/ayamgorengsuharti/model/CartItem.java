package com.ayamgorengsuharti.model;

import android.os.Parcel;
import android.os.Parcelable;

// Model ini mirip MenuItemResponse tapi ada quantity
public class CartItem implements Parcelable {
    private final MenuItemResponse product;
    private int quantity;

    public CartItem(MenuItemResponse product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public MenuItemResponse getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    // --- Kode Parcelable ---
    protected CartItem(Parcel in) {
        product = in.readParcelable(MenuItemResponse.class.getClassLoader());
        quantity = in.readInt();
    }

    public static final Creator<CartItem> CREATOR = new Creator<CartItem>() {
        @Override
        public CartItem createFromParcel(Parcel in) {
            return new CartItem(in);
        }
        @Override
        public CartItem[] newArray(int size) {
            return new CartItem[size];
        }
    };

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(product, flags);
        dest.writeInt(quantity);
    }
}