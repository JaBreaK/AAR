package com.adetrifauzananisarahel.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.adetrifauzananisarahel.databinding.FragmentRegisterBinding;
import com.adetrifauzananisarahel.model.AuthResponse;
import com.adetrifauzananisarahel.network.ApiClient;
import com.adetrifauzananisarahel.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    // HAPUS: private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // HAPUS: dbHelper = new DatabaseHelper(requireContext());

        binding.buttonRegister.setOnClickListener(v -> handleRegister());
    }

    private void handleRegister() {
        // SESUAIKAN DENGAN NAMA DI DATABASE & XML KAMU
        String full_name = binding.editTextName.getText().toString().trim(); // Yang lama 'username'
        String email = binding.editTextEmail.getText().toString().trim();
        String phone = binding.editTextPhone.getText().toString().trim();
        String password = binding.editTextPasswordRegister.getText().toString().trim();

        if (full_name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Semua data tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tampilkan loading di sini jika perlu
        binding.buttonRegister.setEnabled(false); // Biar gak diklik berkali-kali

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<AuthResponse> call = apiService.register(full_name, email, phone, password);

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                binding.buttonRegister.setEnabled(true); // Aktifkan lagi tombolnya
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    Toast.makeText(requireContext(), authResponse.getMessage(), Toast.LENGTH_LONG).show();

                    if (authResponse.getStatus().equals("success")) {
                        // Kembali ke halaman login
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                } else {
                    Toast.makeText(requireContext(), "Registrasi gagal. Coba lagi.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                binding.buttonRegister.setEnabled(true);
                Toast.makeText(requireContext(), "Gagal terhubung ke server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}