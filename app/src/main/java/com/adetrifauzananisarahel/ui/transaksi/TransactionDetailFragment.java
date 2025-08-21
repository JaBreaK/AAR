package com.adetrifauzananisarahel.ui.transaksi;

import android.database.Cursor; // PASTIKAN IMPORT INI ADA
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.adetrifauzananisarahel.adapter.TransactionItemAdapter;
import com.adetrifauzananisarahel.network.DatabaseContract; // PASTIKAN IMPORT INI ADA
import com.adetrifauzananisarahel.network.DatabaseHelper;   // PASTIKAN IMPORT INI ADA
import com.adetrifauzananisarahel.databinding.FragmentTransactionDetailBinding;
import com.adetrifauzananisarahel.model.CartItem; // Ganti Product menjadi CartItem
import com.adetrifauzananisarahel.model.Transaction;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionDetailFragment extends Fragment {

    private FragmentTransactionDetailBinding binding;
    private MapView mapView;
    private Transaction transaction;
    private DatabaseHelper dbHelper; // Tambahkan DatabaseHelper sebagai member

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            transaction = getArguments().getParcelable("TRANSACTION_DATA");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTransactionDetailBinding.inflate(inflater, container, false);
        mapView = binding.mapViewRoute;
        dbHelper = new DatabaseHelper(getContext()); // Inisialisasi dbHelper
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (transaction != null) {
            setupDetails();
            setupMap();
        } else {
            Toast.makeText(getContext(), "Gagal memuat detail transaksi.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupDetails() {
        binding.textDetailTrxId.setText("INV/" + transaction.getTimestamp().replaceAll("[^\\d]", "") + "/" + transaction.getId());
        binding.textDetailTrxDate.setText(transaction.getTimestamp());
        Locale localeID = new Locale("in", "ID");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(localeID);
        currencyFormatter.setMaximumFractionDigits(0);
        binding.textDetailTotal.setText(currencyFormatter.format(transaction.getTotalPrice()));
        binding.textDetailAddress.setText(transaction.getAddress());

        // Sekarang kita bisa pastikan adapter menerima List<CartItem>
        TransactionItemAdapter adapter = new TransactionItemAdapter(transaction.getItems());
        binding.recyclerViewTrxItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewTrxItems.setAdapter(adapter);
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(12.0);
        drawRoute();
    }

    private void drawRoute() {
        Toast.makeText(getContext(), "Memuat rute pengiriman...", Toast.LENGTH_SHORT).show();

        executor.execute(() -> {
            // 1. Dapatkan alamat tujuan
            final GeoPoint endPoint = getGeoPointFromAddress(transaction.getAddress());
            if (endPoint == null) {
                mainThreadHandler.post(() -> Toast.makeText(getContext(), "Gagal mendapatkan koordinat tujuan.", Toast.LENGTH_LONG).show());
                return;
            }

            // 2. Kumpulkan alamat asal unik dengan MENCARI DI DATABASE
            final HashSet<String> uniqueOriginAddresses = new HashSet<>();
            if (transaction.getItems() != null) {
                for (CartItem item : transaction.getItems()) {
                    long productId = item.getProductId();
                    // Lakukan query ke DB untuk mendapatkan alamat produk berdasarkan ID
                    Cursor cursor = dbHelper.getProductById(productId);
                    if (cursor != null && cursor.moveToFirst()) {
                        String address = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_ADDRESS));
                        uniqueOriginAddresses.add(address);
                        cursor.close(); // PENTING: Selalu tutup cursor
                    }
                }
            }

            // 3. Konversi alamat unik menjadi GeoPoint unik
            final ArrayList<GeoPoint> startPoints = new ArrayList<>();
            for (String address : uniqueOriginAddresses) {
                GeoPoint point = getGeoPointFromAddress(address);
                if (point != null) {
                    startPoints.add(point);
                }
            }

            if (startPoints.isEmpty()) {
                mainThreadHandler.post(() -> Toast.makeText(getContext(), "Gagal mendapatkan koordinat asal pengiriman.", Toast.LENGTH_LONG).show());
                return;
            }

            // 4. Siapkan rute dan overlay
            final RoadManager roadManager = new OSRMRoadManager(getContext(), requireActivity().getPackageName());
            final ArrayList<Polyline> roadOverlays = new ArrayList<>();
            final ArrayList<Marker> markers = new ArrayList<>();

            for (GeoPoint startPoint : startPoints) {
                ArrayList<GeoPoint> waypoints = new ArrayList<>();
                waypoints.add(startPoint);
                waypoints.add(endPoint);
                Road road = roadManager.getRoad(waypoints);
                if (road.mStatus == Road.STATUS_OK) {
                    roadOverlays.add(RoadManager.buildRoadOverlay(road));
                }
                addMarker(startPoint, "Asal Pengiriman", startPoint.toString(), markers);
            }
            addMarker(endPoint, "Tujuan Pengiriman", transaction.getAddress(), markers);

            // 5. Siapkan BoundingBox
            final ArrayList<GeoPoint> allPointsForBoundingBox = new ArrayList<>(startPoints);
            allPointsForBoundingBox.add(endPoint);
            final BoundingBox boundingBox = BoundingBox.fromGeoPoints(allPointsForBoundingBox);

            // 6. Update UI di Main Thread
            mainThreadHandler.post(() -> {
                mapView.getOverlays().clear();
                if (!roadOverlays.isEmpty()) {
                    mapView.getOverlays().addAll(roadOverlays);
                    mapView.getOverlays().addAll(markers);
                    mapView.post(() -> mapView.zoomToBoundingBox(boundingBox, true, 150));
                } else {
                    Toast.makeText(getContext(), "Gagal membuat rute.", Toast.LENGTH_SHORT).show();
                    mapView.getOverlays().addAll(markers);
                    mapView.post(() -> mapView.zoomToBoundingBox(boundingBox, true, 150));
                }
                mapView.invalidate();
            });
        });
    }

    private GeoPoint getGeoPointFromAddress(String addressString) {
        if (addressString == null || addressString.isEmpty()) return null;
        if (addressString.startsWith("Lat:") && addressString.contains(", Lon:")) {
            try {
                String[] parts = addressString.split(",");
                String latString = parts[0].substring(parts[0].indexOf(":") + 1).trim();
                String lonString = parts[1].substring(parts[1].indexOf(":") + 1).trim();
                return new GeoPoint(Double.parseDouble(latString), Double.parseDouble(lonString));
            } catch (Exception e) {
                Log.e("TransactionDetail", "Gagal parsing string koordinat: " + addressString, e);
            }
        }
        Log.d("TransactionDetail", "Mencoba Geocoder untuk alamat: " + addressString);
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(addressString, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return new GeoPoint(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
            }
        } catch (IOException e) {
            Log.e("TransactionDetail", "Geocoder IOException untuk alamat: " + addressString, e);
        }
        return null;
    }

    private void addMarker(GeoPoint point, String title, String description, List<Marker> markerList) {
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(title);
        marker.setSnippet(description);
        markerList.add(marker);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mapView != null) mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mapView != null) mapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executor.shutdownNow();
        if (mapView != null) {
            mapView.onDetach();
        }
        binding = null;
    }
}