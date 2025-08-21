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
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.adetrifauzananisarahel.R;
import com.adetrifauzananisarahel.network.DatabaseContract;
import com.adetrifauzananisarahel.network.DatabaseHelper;
import com.adetrifauzananisarahel.databinding.FragmentAddEditProductBinding;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddEditProductFragment extends Fragment {

    private FragmentAddEditProductBinding binding;
    private DatabaseHelper dbHelper;
    private long productId = -1;
    private String currentImagePath = null;
    private String selectedProductAddress = null; // Variabel untuk menyimpan alamat dari peta

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        binding.imageViewProductPreview.setImageURI(imageUri);
                        currentImagePath = saveImageToInternalStorage(imageUri);
                    }
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddEditProductBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DatabaseHelper(getContext());

        if (getArguments() != null) {
            productId = getArguments().getLong("PRODUCT_ID", -1);
        }

        if (productId != -1) {
            getActivity().setTitle("Edit Produk");
            loadProductData();
            binding.buttonDelete.setVisibility(View.VISIBLE);
        } else {
            getActivity().setTitle("Tambah Produk Baru");
            binding.buttonDelete.setVisibility(View.GONE);
        }

        setupListeners();
        setupResultListenerFromMap(); // Panggil listener untuk hasil dari peta
    }

    private void setupListeners() {
        binding.buttonSelectImage.setOnClickListener(v -> openGallery());
        binding.buttonSave.setOnClickListener(v -> saveProduct());
        binding.buttonDelete.setOnClickListener(v -> deleteProduct());

        // Listener untuk tombol pilih lokasi
        binding.buttonSelectLocation.setOnClickListener(v -> {
            // Ganti R.id.action... dengan ID action navigasi yang benar dari nav_graph.xml
            // Ini adalah contoh, pastikan ID-nya benar
            NavHostFragment.findNavController(AddEditProductFragment.this)
                    .navigate(R.id.action_addEditProductFragment_to_mapPickerFragment);
        });
    }

    private void setupResultListenerFromMap() {
        getParentFragmentManager().setFragmentResultListener("address_request", this, (requestKey, bundle) -> {
            String address = bundle.getString("selectedAddress");
            if (address != null) {
                this.selectedProductAddress = address;
                binding.textViewSelectedAddress.setText(address);
                binding.textViewSelectedAddress.setError(null);
            }
        });
    }



    private void loadProductData() {
        executor.execute(() -> {
            final Cursor cursor = dbHelper.getProductById(productId);
            mainThreadHandler.post(() -> {
                if (cursor != null && cursor.moveToFirst()) {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_NAME));
                    String price = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_PRICE));
                    String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_IMAGE_PATH));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_DESCRIPTION));
                    String address = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_ADDRESS));
                    cursor.close();

                    binding.editTextProductName.setText(name);
                    binding.editTextProductPrice.setText(price);
                    binding.editTextProductDescription.setText(description);

                    // Tampilkan alamat di TextView dan simpan ke variabel
                    binding.textViewSelectedAddress.setText(address);
                    this.selectedProductAddress = address;

                    currentImagePath = imagePath;
                    Glide.with(requireContext())
                            .load(imagePath)
                            .placeholder(R.drawable.pp)
                            .into(binding.imageViewProductPreview);
                } else {
                    Toast.makeText(getContext(), "Gagal memuat data produk.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void saveProduct() {
        String name = binding.editTextProductName.getText().toString().trim();
        String price = binding.editTextProductPrice.getText().toString().trim();
        String description = binding.editTextProductDescription.getText().toString().trim();

        // Validasi menggunakan variabel 'selectedProductAddress'
        if (name.isEmpty() || price.isEmpty() || description.isEmpty() || currentImagePath == null || selectedProductAddress == null) {
            Toast.makeText(getContext(), "Semua field, gambar, dan lokasi harus diisi!", Toast.LENGTH_SHORT).show();
            if (selectedProductAddress == null) {
                binding.textViewSelectedAddress.setError("Lokasi wajib dipilih");
            }
            return;
        }

        // Ambil alamat dari variabel
        String address = this.selectedProductAddress;

        boolean success;
        if (productId == -1) {
            success = dbHelper.addProduct(name, price, currentImagePath, description, address);
        } else {
            success = dbHelper.updateProduct(productId, name, price, currentImagePath, description, address);
        }

        if (success) {
            Toast.makeText(getContext(), "Produk berhasil disimpan!", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigateUp();
        } else {
            Toast.makeText(getContext(), "Gagal menyimpan produk.", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteProduct() {
        if (currentImagePath != null) {
            new File(currentImagePath).delete();
        }
        if (dbHelper.deleteProduct(productId)) {
            Toast.makeText(getContext(), "Produk berhasil dihapus.", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigateUp();
        } else {
            Toast.makeText(getContext(), "Gagal menghapus produk.", Toast.LENGTH_SHORT).show();
        }
    }

    private String saveImageToInternalStorage(Uri uri) {
        ContextWrapper cw = new ContextWrapper(getContext().getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        String fileName = "IMG_" + UUID.randomUUID().toString() + ".jpg";
        File mypath = new File(directory, fileName);

        try (InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(mypath)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
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