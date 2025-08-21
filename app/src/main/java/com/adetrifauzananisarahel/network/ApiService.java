package com.adetrifauzananisarahel.network;

import com.adetrifauzananisarahel.model.ApiResponse;
import com.adetrifauzananisarahel.model.AuthResponse;
import com.adetrifauzananisarahel.model.MenuCategory;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

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

    @GET("getHomeData.php")
    Call<ApiResponse<MenuCategory>> getHomeData();
}