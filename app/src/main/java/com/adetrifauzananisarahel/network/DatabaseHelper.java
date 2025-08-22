package com.adetrifauzananisarahel.network;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.adetrifauzananisarahel.network.DatabaseContract.UserEntry;
import com.adetrifauzananisarahel.network.DatabaseContract.ProductEntry;
import com.adetrifauzananisarahel.network.DatabaseContract.CarouselEntry;
import com.adetrifauzananisarahel.network.DatabaseContract.CartEntry;
import com.adetrifauzananisarahel.network.DatabaseContract.TransactionEntry;
import com.adetrifauzananisarahel.model.Transaction;

import java.io.File;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "AplikasiJualan_v4.db";
    // PERBAIKAN 1: NAIKKAN DATABASE_VERSION UNTUK Memicu onUpgrade()
    private static final int DATABASE_VERSION = 11; // Jika sebelumnya 10, naikkan jadi 11

    private final Context context;

    // Definisi SQL untuk membuat tabel
    private static final String SQL_CREATE_USER_TABLE =
            "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
                    UserEntry._ID + " INTEGER PRIMARY KEY," +
                    UserEntry.COLUMN_USERNAME + " TEXT UNIQUE," +
                    UserEntry.COLUMN_PASSWORD + " TEXT," +
                    UserEntry.COLUMN_ROLE + " TEXT," +
                    UserEntry.COLUMN_PROFILE_PICTURE_PATH + " TEXT" +
                    ")";

    // PERBAIKAN 2: Tambahkan kolom COLUMN_SERVER_ID untuk menyimpan ID dari API (misal: "menu-001")
    private static final String SQL_CREATE_PRODUCT_TABLE =
            "CREATE TABLE " + ProductEntry.TABLE_NAME + " (" +
                    ProductEntry._ID + " INTEGER PRIMARY KEY," +
                    ProductEntry.COLUMN_SERVER_ID + " TEXT UNIQUE," + // <-- KOLOM BARU
                    ProductEntry.COLUMN_NAME + " TEXT," +
                    ProductEntry.COLUMN_PRICE + " TEXT," +
                    ProductEntry.COLUMN_IMAGE_PATH + " TEXT," +
                    ProductEntry.COLUMN_DESCRIPTION + " TEXT," +
                    ProductEntry.COLUMN_ADDRESS + " TEXT" +
                    ")";

    private static final String SQL_CREATE_CAROUSEL_TABLE =
            "CREATE TABLE " + CarouselEntry.TABLE_NAME + " (" +
                    CarouselEntry._ID + " INTEGER PRIMARY KEY," +
                    CarouselEntry.COLUMN_IMAGE_PATH + " TEXT" +
                    ")";

    // PERBAIKAN 3: Ubah tipe data COLUMN_PRODUCT_ID dari INTEGER menjadi TEXT
    private static final String SQL_CREATE_CART_TABLE =
            "CREATE TABLE " + CartEntry.TABLE_NAME + " (" +
                    CartEntry._ID + " INTEGER PRIMARY KEY," +
                    CartEntry.COLUMN_USER_USERNAME + " TEXT," +
                    CartEntry.COLUMN_PRODUCT_ID + " TEXT," + // <-- DIUBAH MENJADI TEXT
                    CartEntry.COLUMN_PRODUCT_NAME + " TEXT," +
                    CartEntry.COLUMN_PRODUCT_PRICE + " TEXT," +
                    CartEntry.COLUMN_PRODUCT_IMAGE_PATH + " TEXT," +
                    CartEntry.COLUMN_QUANTITY + " INTEGER" +
                    ")";

    private static final String SQL_CREATE_TRANSACTIONS_TABLE =
            "CREATE TABLE " + TransactionEntry.TABLE_NAME + " (" +
                    TransactionEntry._ID + " INTEGER PRIMARY KEY," +
                    TransactionEntry.COLUMN_USER_USERNAME + " TEXT," +
                    TransactionEntry.COLUMN_ITEMS_JSON + " TEXT," +
                    TransactionEntry.COLUMN_TOTAL_PRICE + " REAL," +
                    TransactionEntry.COLUMN_ADDRESS + " TEXT," +
                    TransactionEntry.COLUMN_ORIGIN_ADDRESS + " TEXT," +
                    TransactionEntry.COLUMN_COURIER + " TEXT," +
                    TransactionEntry.COLUMN_PAYMENT_METHOD + " TEXT," +
                    TransactionEntry.COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USER_TABLE);
        db.execSQL(SQL_CREATE_PRODUCT_TABLE);
        db.execSQL(SQL_CREATE_CAROUSEL_TABLE);
        db.execSQL(SQL_CREATE_CART_TABLE);
        db.execSQL(SQL_CREATE_TRANSACTIONS_TABLE);
        addAdmin(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        clearInternalImageDir();
        db.execSQL("DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProductEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CarouselEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CartEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TransactionEntry.TABLE_NAME);
        onCreate(db);
    }

    private void clearInternalImageDir() {
        try {
            File dir = context.getDir("imageDir", Context.MODE_PRIVATE);
            if (dir != null && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        file.delete();
                    }
                }
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error saat clearInternalImageDir: ", e);
        }
    }

    private void addAdmin(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(UserEntry.COLUMN_USERNAME, "admin");
        values.put(UserEntry.COLUMN_PASSWORD, "admin123");
        values.put(UserEntry.COLUMN_ROLE, "admin");
        db.insert(UserEntry.TABLE_NAME, null, values);
    }

    // =====================
    // Method CRUD User
    // =====================

    public boolean registerUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserEntry.COLUMN_USERNAME, username);
        values.put(UserEntry.COLUMN_PASSWORD, password);
        values.put(UserEntry.COLUMN_ROLE, "user");
        long newRowId = db.insert(UserEntry.TABLE_NAME, null, values);
        return newRowId != -1;
    }

    public String login(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = { UserEntry.COLUMN_ROLE };
        String selection = UserEntry.COLUMN_USERNAME + " = ? AND " + UserEntry.COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = { username, password };
        Cursor cursor = db.query(UserEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String role = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_ROLE));
            cursor.close();
            return role;
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

    public Cursor getUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(UserEntry.TABLE_NAME, null,
                UserEntry.COLUMN_USERNAME + " = ?", new String[]{username},
                null, null, null);
    }

    public boolean updateUser(String oldUsername, String newUsername, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserEntry.COLUMN_USERNAME, newUsername);
        values.put(UserEntry.COLUMN_PASSWORD, newPassword);
        int rowsAffected = db.update(UserEntry.TABLE_NAME, values,
                UserEntry.COLUMN_USERNAME + " = ?", new String[]{oldUsername});
        return rowsAffected > 0;
    }

    public boolean updateUserProfilePicture(String username, String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserEntry.COLUMN_PROFILE_PICTURE_PATH, path);
        int rowsAffected = db.update(UserEntry.TABLE_NAME, values,
                UserEntry.COLUMN_USERNAME + " = ?", new String[]{username});
        return rowsAffected > 0;
    }

    // =====================
    // Method CRUD Product
    // =====================

    public boolean addProduct(String serverId, String name, String price, String imagePath, String description, String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_SERVER_ID, serverId); // Simpan juga server_id
        values.put(ProductEntry.COLUMN_NAME, name);
        values.put(ProductEntry.COLUMN_PRICE, price);
        values.put(ProductEntry.COLUMN_IMAGE_PATH, imagePath);
        values.put(ProductEntry.COLUMN_DESCRIPTION, description);
        values.put(ProductEntry.COLUMN_ADDRESS, address);
        long result = db.insert(ProductEntry.TABLE_NAME, null, values);
        return result != -1;
    }

    public Cursor getAllProducts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + ProductEntry.TABLE_NAME, null);
    }

    // PERBAIKAN 4: Ubah method ini untuk menerima String dan mencari di kolom server_id
    public Cursor getProductById(String serverId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(ProductEntry.TABLE_NAME, null,
                ProductEntry.COLUMN_SERVER_ID + " = ?", new String[]{serverId},
                null, null, null);
    }

    public boolean updateProduct(long id, String name, String price, String imagePath, String description, String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_NAME, name);
        values.put(ProductEntry.COLUMN_PRICE, price);
        values.put(ProductEntry.COLUMN_IMAGE_PATH, imagePath);
        values.put(ProductEntry.COLUMN_DESCRIPTION, description);
        values.put(ProductEntry.COLUMN_ADDRESS, address);
        int result = db.update(ProductEntry.TABLE_NAME, values,
                ProductEntry._ID + " = ?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    public boolean deleteProduct(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(ProductEntry.TABLE_NAME,
                ProductEntry._ID + " = ?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    // =====================
    // Method CRUD Carousel
    // =====================

    public boolean addCarouselItem(String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CarouselEntry.COLUMN_IMAGE_PATH, imagePath);
        long result = db.insert(CarouselEntry.TABLE_NAME, null, values);
        return result != -1;
    }

    public Cursor getAllCarouselItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + CarouselEntry.TABLE_NAME, null);
    }

    public boolean deleteCarouselItem(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(CarouselEntry.TABLE_NAME,
                CarouselEntry._ID + " = ?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    // ==============================
    // Method Cart & Transaction
    // ==============================

    // PERBAIKAN 5: Ubah method ini untuk menerima productId sebagai String
    public void addToCart(String username, String productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] projection = { CartEntry._ID, CartEntry.COLUMN_QUANTITY };
        String selection = CartEntry.COLUMN_USER_USERNAME + " = ? AND " + CartEntry.COLUMN_PRODUCT_ID + " = ?";
        String[] selectionArgs = { username, productId };
        Cursor cursor = db.query(CartEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int currentQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(CartEntry.COLUMN_QUANTITY));
            long cartId = cursor.getLong(cursor.getColumnIndexOrThrow(CartEntry._ID));
            ContentValues updateValues = new ContentValues();
            updateValues.put(CartEntry.COLUMN_QUANTITY, currentQuantity + 1);
            db.update(CartEntry.TABLE_NAME, updateValues, CartEntry._ID + " = ?", new String[]{String.valueOf(cartId)});
        } else {
            Cursor productCursor = getProductById(productId);
            if (productCursor != null && productCursor.moveToFirst()) {
                String name = productCursor.getString(productCursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME));
                String price = productCursor.getString(productCursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRICE));
                String imagePath = productCursor.getString(productCursor.getColumnIndexOrThrow(ProductEntry.COLUMN_IMAGE_PATH));
                ContentValues insertValues = new ContentValues();
                insertValues.put(CartEntry.COLUMN_USER_USERNAME, username);
                insertValues.put(CartEntry.COLUMN_PRODUCT_ID, productId);
                insertValues.put(CartEntry.COLUMN_PRODUCT_NAME, name);
                insertValues.put(CartEntry.COLUMN_PRODUCT_PRICE, price);
                insertValues.put(CartEntry.COLUMN_PRODUCT_IMAGE_PATH, imagePath);
                insertValues.put(CartEntry.COLUMN_QUANTITY, 1);
                db.insert(CartEntry.TABLE_NAME, null, insertValues);
                productCursor.close();
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    public Cursor getCartItems(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(CartEntry.TABLE_NAME, null,
                CartEntry.COLUMN_USER_USERNAME + " = ?", new String[]{username},
                null, null, null);
    }

    public boolean updateCartItemQuantity(long cartId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CartEntry.COLUMN_QUANTITY, quantity);
        int rows = db.update(CartEntry.TABLE_NAME, values,
                CartEntry._ID + " = ?", new String[]{String.valueOf(cartId)});
        return rows > 0;
    }

    public boolean removeCartItem(long cartId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(CartEntry.TABLE_NAME,
                CartEntry._ID + " = ?", new String[]{String.valueOf(cartId)});
        return rows > 0;
    }

    public long addTransaction(String username, String itemsJson, double totalPrice,
                               String address, String originAddress, String courier, String paymentMethod) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TransactionEntry.COLUMN_USER_USERNAME, username);
        values.put(TransactionEntry.COLUMN_ITEMS_JSON, itemsJson);
        values.put(TransactionEntry.COLUMN_TOTAL_PRICE, totalPrice);
        values.put(TransactionEntry.COLUMN_ADDRESS, address);
        values.put(TransactionEntry.COLUMN_ORIGIN_ADDRESS, originAddress);
        values.put(TransactionEntry.COLUMN_COURIER, courier);
        values.put(TransactionEntry.COLUMN_PAYMENT_METHOD, paymentMethod);
        return db.insert(TransactionEntry.TABLE_NAME, null, values);
    }

    public void clearCart(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(CartEntry.TABLE_NAME,
                CartEntry.COLUMN_USER_USERNAME + " = ?", new String[]{username});
    }

    public Cursor getTransactions(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TransactionEntry.TABLE_NAME, null,
                TransactionEntry.COLUMN_USER_USERNAME + " = ?", new String[]{username},
                null, null, TransactionEntry.COLUMN_TIMESTAMP + " DESC");
    }

    public Transaction getTransactionById(long transactionId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TransactionEntry.TABLE_NAME, null,
                TransactionEntry._ID + " = ?", new String[]{String.valueOf(transactionId)},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(TransactionEntry._ID));
            String itemsJson = cursor.getString(cursor.getColumnIndexOrThrow(TransactionEntry.COLUMN_ITEMS_JSON));
            double totalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(TransactionEntry.COLUMN_TOTAL_PRICE));
            String address = cursor.getString(cursor.getColumnIndexOrThrow(TransactionEntry.COLUMN_ADDRESS));
            String originAddress = cursor.getString(cursor.getColumnIndexOrThrow(TransactionEntry.COLUMN_ORIGIN_ADDRESS));
            String courier = cursor.getString(cursor.getColumnIndexOrThrow(TransactionEntry.COLUMN_COURIER));
            String paymentMethod = cursor.getString(cursor.getColumnIndexOrThrow(TransactionEntry.COLUMN_PAYMENT_METHOD));
            String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(TransactionEntry.COLUMN_TIMESTAMP));
            cursor.close();
            return new Transaction(id, itemsJson, totalPrice, address, originAddress, courier, paymentMethod, timestamp);
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }
}