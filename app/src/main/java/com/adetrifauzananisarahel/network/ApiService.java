package com.adetrifauzananisarahel.network;

import com.adetrifauzananisarahel.model.MenuItemResponse; // Import model baru
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

import com.adetrifauzananisarahel.model.Order;
import com.adetrifauzananisarahel.model.OrderPayload;
import com.adetrifauzananisarahel.model.OrderResponse;
import com.adetrifauzananisarahel.model.PaymentMethod;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

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

    @GET("orders/{id}")
    Call<Order> getOrderDetail(
            @Path("id") int orderId,
            @Query("nomorWa") String nomorWa
    );
    // BARU: Endpoint untuk mengambil riwayat pesanan
    @GET("orders/by-wa/{nomorWa}")
    Call<List<Order>> getOrdersByWa(@Path("nomorWa") String nomorWa);

    @Multipart
    @POST("konfirmasi-pembayaran/{id}")
    Call<ResponseBody> uploadPaymentProof(
            @Path("id") int orderId,
            @Part MultipartBody.Part bukti
    );
}