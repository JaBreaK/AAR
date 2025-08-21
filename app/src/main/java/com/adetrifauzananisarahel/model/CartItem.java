package com.adetrifauzananisarahel.model;

public class CartItem {
    private long cartId;
    private long productId;
    private String productName;
    private String productPrice;
    private String imagePath;
    private int quantity;

    public CartItem(long cartId, long productId, String productName, String productPrice, String imagePath, int quantity) {
        this.cartId = cartId;
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.imagePath = imagePath;
        this.quantity = quantity;
    }

    // Getter methods
    public long getCartId() { return cartId; }
    public long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getProductPrice() { return productPrice; }
    public String getImagePath() { return imagePath; }
    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}