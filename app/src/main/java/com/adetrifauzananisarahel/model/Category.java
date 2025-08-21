package com.adetrifauzananisarahel.model;

public class Category {
    private final String name;
    private final int imageResource; // TAMBAHKAN INI

    public Category(String name, int imageResource) { // UBAH CONSTRUCTOR
        this.name = name;
        this.imageResource = imageResource; // TAMBAHKAN INI
    }

    public String getName() {
        return name;
    }

    public int getImageResource() { // TAMBAHKAN INI
        return imageResource;
    }
}