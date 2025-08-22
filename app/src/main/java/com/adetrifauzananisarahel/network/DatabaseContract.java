package com.adetrifauzananisarahel.network;

import android.provider.BaseColumns;

public final class DatabaseContract {
    private DatabaseContract() {}

    public static class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_ROLE = "role";
        public static final String COLUMN_PROFILE_PICTURE_PATH = "profile_picture_path";
    }

    public static class ProductEntry implements BaseColumns {
        public static final String TABLE_NAME = "products";
        // =======================================================================================
        // PERBAIKAN UTAMA: Tambahkan konstanta untuk kolom server_id
        // Ini akan digunakan di DatabaseHelper untuk menyimpan ID asli dari API (misal: "menu-001")
        // =======================================================================================
        public static final String COLUMN_SERVER_ID = "server_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_IMAGE_PATH = "image_path";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_ADDRESS = "address";
    }

    public static class CarouselEntry implements BaseColumns {
        public static final String TABLE_NAME = "carousel_items";
        public static final String COLUMN_IMAGE_PATH = "image_path";
    }

    public static final class CartEntry implements BaseColumns {
        public static final String TABLE_NAME = "cart";
        public static final String COLUMN_USER_USERNAME = "username";
        public static final String COLUMN_PRODUCT_ID = "product_id"; // Ini akan menyimpan server_id (String)
        public static final String COLUMN_PRODUCT_NAME = "product_name";
        public static final String COLUMN_PRODUCT_PRICE = "product_price";
        public static final String COLUMN_PRODUCT_IMAGE_PATH = "product_image";
        public static final String COLUMN_QUANTITY = "quantity";
    }

    public static final class TransactionEntry implements BaseColumns {
        public static final String TABLE_NAME = "transactions";
        public static final String COLUMN_USER_USERNAME = "username";
        public static final String COLUMN_ITEMS_JSON = "items_json";
        public static final String COLUMN_TOTAL_PRICE = "total_price";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_ORIGIN_ADDRESS = "origin_address";
        public static final String COLUMN_COURIER = "courier";
        public static final String COLUMN_PAYMENT_METHOD = "payment_method";
        public static final String COLUMN_TIMESTAMP = "timestamp";
    }
}