package com.adetrifauzananisarahel.network;

import com.adetrifauzananisarahel.model.ApiResponse;
import com.adetrifauzananisarahel.model.AuthResponse;
import com.adetrifauzananisarahel.model.FoodItem; // <-- TAMBAHKAN IMPORT INI
import com.adetrifauzananisarahel.model.MenuCategory;

import java.util.List; // <-- TAMBAHKAN IMPORT INI

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query; // <-- TAMBAHKAN IMPORT INI

public interface ApiService {

    @FormUrlEncoded
    @POST("register.php")
    Call<AuthResponse> register(
            @Field("full_name") String full_name,
            @Field("email") String email,
            @Field("phone") String phone,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("login.php")
    Call<AuthResponse> login(
            @Field("email") String email,
            @Field("password") String password
    );

    // =========================================================================
    // PERBAIKAN: Mengubah return type agar sesuai dengan data API yang berupa
    // daftar (List) kategori, bukan cuma satu kategori.
    // =========================================================================
    @GET("getHomeData.php")
    Call<ApiResponse<List<MenuCategory>>> getHomeData();

    // =========================================================================
    // PENAMBAHAN: Fungsi baru untuk mengambil detail satu produk berdasarkan ID-nya.
    // Ini akan dipanggil oleh ProductDetailFragment.
    // Menggunakan @Query untuk membuat URL seperti: .../getProductDetail.php?id=menu-001
    // =========================================================================
    @GET("getProductDetail.php")
    Call<ApiResponse<FoodItem>> getProductDetail(@Query("id") String productId);

}