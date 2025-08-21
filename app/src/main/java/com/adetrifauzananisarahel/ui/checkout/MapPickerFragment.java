package com.adetrifauzananisarahel.ui.checkout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.adetrifauzananisarahel.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.Locale;

/**
 * MapPickerFragment (Versi Modifikasi):
 * - Menampilkan peta OsmDroid fullscreen.
 * - Static ImageView (id: image_view_marker) di tengah sebagai penanda lokasi.
 * - Tombol Konfirmasi Lokasi (id: button_confirm_location) mengambil koordinat tengah peta.
 * - TIDAK LAGI menggunakan Geocoder untuk mencari alamat.
 * - Hasil dikirim via FragmentResult API dengan key "address_request":
 * bundle.putString("selectedAddress", ...); // Berisi string koordinat
 * bundle.putDouble("selectedLat", ...);
 * bundle.putDouble("selectedLon", ...);
 */
public class MapPickerFragment extends Fragment {
    private static final String TAG = "MapPickerFragment";

    private MapView mapView;
    private ImageView imageViewMarker;
    private Button buttonConfirmLocation;

    private FusedLocationProviderClient fusedLocationClient;

    // Launcher untuk runtime permission ACCESS_FINE_LOCATION
    private ActivityResultLauncher<String> locationPermissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_picker, container, false);

        // Inisialisasi view
        mapView = view.findViewById(R.id.map_view);
        imageViewMarker = view.findViewById(R.id.image_view_marker);
        buttonConfirmLocation = view.findViewById(R.id.button_confirm_location);

        // Inisialisasi lokasi client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Inisialisasi permission launcher
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Log.d(TAG, "Lokasi permission granted");
                        initMapAfterPermission();
                    } else {
                        Log.w(TAG, "Lokasi permission denied");
                        Toast.makeText(requireContext(),
                                "Izin lokasi ditolak. Peta tetap ditampilkan, fungsi Lokasi Saya tidak tersedia.",
                                Toast.LENGTH_LONG).show();
                        initMapWithoutPermission();
                    }
                }
        );

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Cek runtime permission ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            initMapAfterPermission();
        } else {
            // Request permission
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        // Tombol Konfirmasi Lokasi
        buttonConfirmLocation.setOnClickListener(v -> confirmLocation());
    }

    /** Inisialisasi peta jika permission lokasi diberikan: set tile source, multi-touch, center ke last location jika ada. */
    private void initMapAfterPermission() {
        setupMapBasics();
        // Center ke lokasi user terakhir jika tersedia
        getLastLocationAndCenter();
    }

    /** Inisialisasi peta jika permission lokasi tidak diberikan: set tile source dan center default. */
    private void initMapWithoutPermission() {
        setupMapBasics();
        setDefaultCenter();
    }

    /** Setup MapView dasar: tile source Osm, multi-touch, zoom level awal. */
    private void setupMapBasics() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        IMapController mapController = mapView.getController();
        mapController.setZoom(15.0);
        mapView.invalidate();
    }

    /** Dapatkan last location user dan center map; jika gagal atau null, gunakan default center. */
    @SuppressLint("MissingPermission")
    private void getLastLocationAndCenter() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "getLastLocationAndCenter dipanggil tanpa permission");
            setDefaultCenter();
            return;
        }
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            centerMap(location);
                        } else {
                            Log.w(TAG, "Last location null");
                            setDefaultCenter();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Gagal mendapatkan last location", e);
                        setDefaultCenter();
                    });
        } catch (SecurityException se) {
            Log.e(TAG, "SecurityException saat getLastLocation", se);
            setDefaultCenter();
        }
    }

    /** Pusatkan map ke lokasi user dari Location object. */
    private void centerMap(Location location) {
        GeoPoint pt = new GeoPoint(location.getLatitude(), location.getLongitude());
        IMapController mapController = mapView.getController();
        mapController.setZoom(18.0);
        mapController.setCenter(pt);
    }

    /** Center map ke lokasi default (misal Jakarta). */
    private void setDefaultCenter() {
        GeoPoint defaultPt = new GeoPoint(-6.2088, 106.8456);
        IMapController mapController = mapView.getController();
        mapController.setZoom(12.0);
        mapController.setCenter(defaultPt);
    }

    /**
     * Saat tombol Konfirmasi Lokasi ditekan:
     * - Ambil koordinat center peta (di bawah image_view_marker).
     * - Langsung kirim hasil berupa koordinat.
     */
    private void confirmLocation() {
        // Ambil koordinat dari pusat peta
        GeoPoint centerPoint = (GeoPoint) mapView.getMapCenter();
        double lat = centerPoint.getLatitude();
        double lon = centerPoint.getLongitude();

        // Buat string format koordinat sebagai pengganti alamat
        // Menggunakan Locale.US agar format desimal menggunakan titik (.)
        String coordinateString = String.format(Locale.US, "Lat: %.6f, Lon: %.6f", lat, lon);
        Toast.makeText(getContext(), "Lokasi dipilih: " + coordinateString, Toast.LENGTH_SHORT).show();

        // Siapkan bundle untuk dikirim kembali ke fragment sebelumnya
        Bundle result = new Bundle();

        // Kita tetap isi "selectedAddress" dengan string koordinat agar konsisten,
        // atau bisa juga diisi string kosong "" jika tidak ingin digunakan sama sekali.
        result.putString("selectedAddress", coordinateString);
        result.putDouble("selectedLat", lat);
        result.putDouble("selectedLon", lon);

        // Kirim hasil dan kembali (pop back stack)
        getParentFragmentManager().setFragmentResult("address_request", result);
        NavHostFragment.findNavController(MapPickerFragment.this).popBackStack();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume(); // Wajib untuk OsmDroid
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause(); // Wajib untuk OsmDroid
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Tidak ada lagi executor yang perlu di-shutdown
    }
}