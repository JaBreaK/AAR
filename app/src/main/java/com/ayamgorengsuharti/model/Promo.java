package com.ayamgorengsuharti.model;

public class Promo {
    private int id;
    private String title;
    private int imageResId; // Diubah dari String imageUrl

    public Promo(int id, String title, int imageResId) {
        this.id = id;
        this.title = title;
        this.imageResId = imageResId;
    }

    // Getter diubah
    public int getImageResId() {
        return imageResId;
    }
}