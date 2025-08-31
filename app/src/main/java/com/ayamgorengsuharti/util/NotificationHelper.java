package com.ayamgorengsuharti.util;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.ayamgorengsuharti.R;

public class NotificationHelper {

    private static final String CHANNEL_ID = "order_status_channel";
    private static final String CHANNEL_NAME = "Status Pesanan";
    private static final String CHANNEL_DESC = "Notifikasi untuk perubahan status pesanan";

    public static void createNotificationChannel(Context context) {
        // Channel hanya perlu dibuat sekali untuk Android Oreo (API 26) ke atas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public static void showNotification(Context context, String title, String content) {

        // --- TAMBAHKAN PENGECEKAN IZIN DI SINI ---
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Jika izin tidak ada, jangan lakukan apa-apa.
            // Permintaan izin akan di-handle oleh Fragment/Activity.
            return;
        }
        // --- AKHIR PENGECEKAN ---

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.keranjang) // Ganti dengan ikon notifikasimu
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());
    }
}