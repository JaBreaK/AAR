package com.adetrifauzananisarahel.model;

public class CarouselItem {
    private long id;
    private String imagePath;

    // Constructor untuk memuat dari DB
    public CarouselItem(long id, String imagePath) {
        this.id = id;
        this.imagePath = imagePath;
    }

    // Constructor untuk menambah item baru (ID belum ada)
    public CarouselItem(String imagePath) {
        this.imagePath = imagePath;
    }

    public long getId() { return id; }
    public String getImagePath() { return imagePath; }
}