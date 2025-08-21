package com.adetrifauzananisarahel.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.adetrifauzananisarahel.MainActivity;
import com.adetrifauzananisarahel.R;
import com.adetrifauzananisarahel.databinding.FragmentLoginBinding;
import com.adetrifauzananisarahel.model.AuthResponse;
import com.adetrifauzananisarahel.model.User;
import com.adetrifauzananisarahel.network.ApiClient;
import com.adetrifauzananisarahel.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    // HAPUS: private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.buttonLogin.setOnClickListener(v -> handleLogin());
        binding.textViewRegisterPrompt.setOnClickListener(v -> {
            NavHostFragment.findNavController(LoginFragment.this)
                    .navigate(R.id.action_loginFragment_to_registerFragment);
        });
    }

    private void handleLogin() {
        // GANTI dari username ke email
        String email = binding.editTextEmailLogin.getText().toString().trim(); // Pastikan ID di XML sesuai
        String password = binding.editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.buttonLogin.setEnabled(false);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<AuthResponse> call = apiService.login(email, password);

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                binding.buttonLogin.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();

                    if (authResponse.getStatus().equals("success")) {
                        // Ambil data user dari response
                        User loggedInUser = authResponse.getData();

                        // Simpan data user ke SharedPreferences
                        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.putString("userId", loggedInUser.getId());
                        editor.putString("userName", loggedInUser.getName());
                        editor.putString("userEmail", loggedInUser.getEmail());
                        editor.putString("userRole", loggedInUser.getRole());
                        editor.apply();

                        Toast.makeText(requireContext(), "Login berhasil sebagai: " + loggedInUser.getRole(), Toast.LENGTH_LONG).show();

                        // Arahkan ke MainActivity
                        Intent intent = new Intent(requireActivity(), MainActivity.class);
                        startActivity(intent);
                        requireActivity().finish();

                    } else {
                        // Jika status "error", tampilkan pesan dari server
                        Toast.makeText(requireContext(), authResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Login gagal. Periksa kembali email dan password.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                binding.buttonLogin.setEnabled(true);
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