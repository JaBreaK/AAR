package com.adetrifauzananisarahel.network;

import com.adetrifauzananisarahel.model.AuthResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {

    @FormUrlEncoded
    @POST("register.php")
    Call<AuthResponse> register(
            @Field("name") String name,
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
}