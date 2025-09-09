package com.ayamgorengsuharti.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://ayamgorengsuharti.vercel.app/api/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {

            // --- BAGIAN CACHE (TETAP SAMA) ---
            long cacheSize = 1000 * 1024 * 1024; // 10 MB
            File cacheDir = new File(context.getCacheDir(), "http-cache");
            Cache cache = new Cache(cacheDir, cacheSize);

            // --- INTERCEPTOR BARU UNTUK MEMAKSA CACHE ---

            // Interceptor ini berjalan saat ada koneksi internet
            Interceptor networkInterceptor = chain -> {
                Response response = chain.proceed(chain.request());

                // Ganti header dari server. Simpan cache selama 1 jam.
                CacheControl cacheControl = new CacheControl.Builder()
                        .maxAge(1, TimeUnit.HOURS)
                        .build();

                return response.newBuilder()
                        .header("Cache-Control", cacheControl.toString())
                        .build();
            };

            // Interceptor ini berjalan saat TIDAK ada koneksi internet
            Interceptor offlineInterceptor = chain -> {
                Request request = chain.request();

                // Jika tidak ada internet, paksa ambil dari cache
                if (!isNetworkAvailable(context)) {
                    CacheControl cacheControl = new CacheControl.Builder()
                            .maxStale(7, TimeUnit.DAYS) // Boleh pakai cache basi sampai 7 hari
                            .build();

                    request = request.newBuilder()
                            .cacheControl(cacheControl)
                            .build();
                }

                return chain.proceed(request);
            };

            // --- BUAT OkHttpClient DENGAN INTERCEPTOR BARU ---
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .cache(cache)
                    .addInterceptor(offlineInterceptor) // Tambah offline interceptor
                    .addNetworkInterceptor(networkInterceptor) // Tambah network interceptor
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient) // Terapkan OkHttpClient yang baru
                    .build();
        }
        return retrofit;
    }

    // Helper method untuk cek koneksi internet
    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}