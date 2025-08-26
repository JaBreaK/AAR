package com.adetrifauzananisarahel.network;

import com.adetrifauzananisarahel.model.MenuItemResponse; // Import model baru
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import com.adetrifauzananisarahel.model.OrderPayload;
import com.adetrifauzananisarahel.model.OrderResponse;
import com.adetrifauzananisarahel.model.PaymentMethod;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    // Endpoint baru untuk mendapatkan semua menu
    @GET("menu")
    Call<List<MenuItemResponse>> getMenu();

    // BARU: Endpoint untuk mengambil metode pembayaran
    @GET("metode-pembayaran")
    Call<List<PaymentMethod>> getPaymentMethods();

    // BARU: Endpoint untuk mengirim pesanan
    @POST("orders")
    Call<OrderResponse> createOrder(@Body OrderPayload payload);
}