package com.adetrifauzananisarahel.ui.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import com.adetrifauzananisarahel.databinding.FragmentProfileBinding;
import com.adetrifauzananisarahel.ui.auth.AuthActivity;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;


public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private String currentUsername;
    private String currentProfilePicPath;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());




    // Launcher modern untuk memilih gambar dari galeri
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        saveAndSetImage(imageUri);
                    }
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        dbHelper = new DatabaseHelper(requireContext());
        sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", "");
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadUserProfile();
        setupClickListeners();
    }

    private void loadUserProfile() {
        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(requireContext(), "Username tidak ditemukan, silakan login ulang.", Toast.LENGTH_LONG).show();
            // Mungkin panggil method logout di sini
            return;
        }

        executor.execute(() -> {
            // ---- INI BERJALAN DI BACKGROUND ----
            // Mengambil data dari database itu berat, lakukan di sini
            final Cursor cursor = dbHelper.getUser(currentUsername);

            mainThreadHandler.post(() -> {
                // ---- KEMBALI KE UI THREAD ----
                // Update UI harus di sini
                if (cursor != null && cursor.moveToFirst()) {
                    // Ambil data dari cursor
                    String password = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.UserEntry.COLUMN_PASSWORD));
                    String role = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.UserEntry.COLUMN_ROLE));
                    currentProfilePicPath = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.UserEntry.COLUMN_PROFILE_PICTURE_PATH));
                    cursor.close(); // PENTING: Tutup cursor setelah selesai dipakai

                    // Update semua UI
                    binding.editTextUsername.setText(currentUsername);
                    binding.editTextPassword.setText(password);

                    if(role != null) {
                        binding.textViewRole.setText(role.substring(0, 1).toUpperCase() + role.substring(1));
                        if ("admin".equalsIgnoreCase(role)) {
                            binding.buttonAdminPanel.setVisibility(View.VISIBLE);
                            binding.buttonManageCarousel.setVisibility(View.VISIBLE); // TAMBAHKAN INI
                        } else {
                            binding.buttonAdminPanel.setVisibility(View.GONE);
                            binding.buttonManageCarousel.setVisibility(View.GONE); // TAMBAHKAN INI
                        }
                    } else {
                        binding.textViewRole.setText("Role tidak diketahui");
                        binding.buttonAdminPanel.setVisibility(View.GONE);
                    }

                    // Glide tetap di sini
                    Glide.with(ProfileFragment.this)
                            .load(currentProfilePicPath)
                            .placeholder(R.drawable.pp)
                            .error(R.drawable.pp)
                            .circleCrop()
                            .into(binding.imageViewProfile);

                } else {
                    // Handle jika user tidak ditemukan di DB
                    Toast.makeText(requireContext(), "Gagal memuat data user.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void setupClickListeners() {
        binding.textChangePhoto.setOnClickListener(v -> openGallery());
        binding.imageViewProfile.setOnClickListener(v -> openGallery());
        binding.buttonSave.setOnClickListener(v -> saveProfileChanges());
        binding.buttonLogout.setOnClickListener(v -> logoutUser());

        // TAMBAHKAN INI
        binding.buttonAdminPanel.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_navigation_profile_to_adminManageProductsFragment);
            // Ganti R.id.action_... dengan action navigasi yang benar di nav_graph kamu
            // Contoh: NavHostFragment.findNavController(this).navigate(R.id.action_profileFragment_to_adminManageProductFragment);
            Toast.makeText(getContext(), "Menuju Panel Admin...", Toast.LENGTH_SHORT).show();
        });
        binding.buttonManageCarousel.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_profile_to_manage_carousel);
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void saveAndSetImage(Uri imageUri) {
        try {
            // Salin gambar dari galeri ke penyimpanan internal aplikasi agar tidak hilang
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            File internalFile = new File(requireContext().getFilesDir(), "profile_" + currentUsername + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(internalFile);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.close();
            inputStream.close();

            // Simpan path file baru ke database
            String newPath = internalFile.getAbsolutePath();
            dbHelper.updateUserProfilePicture(currentUsername, newPath);

            // Tampilkan gambar baru menggunakan Glide
            Glide.with(this).load(newPath).circleCrop().into(binding.imageViewProfile);
            Toast.makeText(requireContext(), "Foto profil diperbarui", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Gagal memuat gambar", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfileChanges() {
        String newUsername = binding.editTextUsername.getText().toString().trim();
        String newPassword = binding.editTextPassword.getText().toString().trim();

        if (newUsername.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Username dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isSuccess = dbHelper.updateUser(currentUsername, newUsername, newPassword);
        if (isSuccess) {
            // PENTING: Update juga SharedPreferences!
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("username", newUsername);
            editor.apply();
            currentUsername = newUsername; // Update username saat ini di fragment

            Toast.makeText(requireContext(), "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Gagal memperbarui profil", Toast.LENGTH_SHORT).show();
        }
    }

    private void logoutUser() {
        // 1. Hapus data login dari SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Hapus semua data (isLoggedIn, username, role)
        editor.apply();

        // 2. Tampilkan pesan (opsional)
        Toast.makeText(requireContext(), "Anda telah logout", Toast.LENGTH_SHORT).show();

        // 3. Buat Intent untuk kembali ke halaman login (AuthActivity)
        Intent intent = new Intent(requireActivity(), AuthActivity.class);

        // 4. Set Flags PENTING untuk menghapus semua halaman sebelumnya (back stack)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // 5. Jalankan intent dan tutup Activity saat ini
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}