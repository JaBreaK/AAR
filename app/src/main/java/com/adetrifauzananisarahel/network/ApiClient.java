package com.adetrifauzananisarahel.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // Ganti dengan alamat IP kamu. Pakai 10.0.2.2 jika emulator di laptop yg sama dgn server
    private static final String BASE_URL = "https://ayamgorengsuharti.com/api/";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}