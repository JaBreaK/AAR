package com.adetrifauzananisarahel.ui.admin;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.adetrifauzananisarahel.adapter.ManageCarouselAdapter;
import com.adetrifauzananisarahel.network.DatabaseContract;
import com.adetrifauzananisarahel.network.DatabaseHelper;
import com.adetrifauzananisarahel.databinding.FragmentAdminManageCarouselBinding;
import com.adetrifauzananisarahel.model.CarouselItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminManageCarouselFragment extends Fragment implements ManageCarouselAdapter.OnCarouselItemInteractionListener {

    private FragmentAdminManageCarouselBinding binding;
    private DatabaseHelper dbHelper;
    private ManageCarouselAdapter adapter;
    private final List<CarouselItem> carouselItems = new ArrayList<>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        saveNewCarouselItem(imageUri);
                    }
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminManageCarouselBinding.inflate(inflater, container, false);
        dbHelper = new DatabaseHelper(getContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Kelola Carousel");

        setupRecyclerView();
        loadCarouselItemsFromDb();

        binding.fabAddCarousel.setOnClickListener(v -> openGallery());
    }

    private void setupRecyclerView() {
        adapter = new ManageCarouselAdapter(carouselItems, this);
        binding.recyclerViewCarouselItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewCarouselItems.setAdapter(adapter);
    }

    private void loadCarouselItemsFromDb() {
        executor.execute(() -> {
            List<CarouselItem> items = new ArrayList<>();
            Cursor cursor = dbHelper.getAllCarouselItems();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.CarouselEntry._ID));
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.CarouselEntry.COLUMN_IMAGE_PATH));
                    items.add(new CarouselItem(id, path));
                } while (cursor.moveToNext());
                cursor.close();
            }

            mainThreadHandler.post(() -> {
                carouselItems.clear();
                carouselItems.addAll(items);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void saveNewCarouselItem(Uri imageUri) {
        Toast.makeText(getContext(), "Menyimpan gambar...", Toast.LENGTH_SHORT).show();
        executor.execute(() -> {
            String imagePath = saveImageToInternalStorage(imageUri);
            if (imagePath != null) {
                dbHelper.addCarouselItem(imagePath);
                mainThreadHandler.post(this::loadCarouselItemsFromDb); // Refresh list
            }
        });
    }

    @Override
    public void onDeleteClick(CarouselItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Hapus Gambar")
                .setMessage("Apakah Anda yakin ingin menghapus gambar ini dari carousel?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    executor.execute(() -> {
                        dbHelper.deleteCarouselItem(item.getId());
                        mainThreadHandler.post(this::loadCarouselItemsFromDb); // Refresh list
                    });
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private String saveImageToInternalStorage(Uri uri) {
        ContextWrapper cw = new ContextWrapper(getContext().getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        String fileName = "CAROUSEL_" + UUID.randomUUID().toString() + ".jpg";
        File mypath = new File(directory, fileName);

        try (InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(mypath)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
            mainThreadHandler.post(() -> Toast.makeText(getContext(), "Gagal menyimpan gambar", Toast.LENGTH_SHORT).show());
            return null;
        }
        return mypath.getAbsolutePath();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}