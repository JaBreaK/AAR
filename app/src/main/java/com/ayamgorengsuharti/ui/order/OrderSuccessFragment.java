package com.ayamgorengsuharti.ui.order;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.ayamgorengsuharti.R;
import com.ayamgorengsuharti.databinding.FragmentOrderSuccessBinding;
import com.ayamgorengsuharti.model.OrderResponse;
import java.text.NumberFormat;
import java.util.Locale;

public class OrderSuccessFragment extends Fragment {

    private FragmentOrderSuccessBinding binding;
    private OrderResponse orderData;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderData = getArguments().getParcelable("ORDER_DATA");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrderSuccessBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (orderData != null) {
            populateUI();
        }

        binding.btnBackToHome.setText("Lihat Detail Pesanan");
        binding.btnBackToHome.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putInt("ORDER_ID", orderData.getId());
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_orderSuccessFragment_to_orderDetailFragment, bundle);
        });
    }

    private void populateUI() {
        binding.tvOrderId.setText("ID: " + orderData.getId());
        binding.tvOrderStatus.setText(orderData.getStatusPesanan().replace("_", " "));

        Locale localeID = new Locale("in", "ID");
        NumberFormat format = NumberFormat.getCurrencyInstance(localeID);
        format.setMaximumFractionDigits(0);
        binding.tvOrderTotal.setText(format.format(orderData.getTotalHarga()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}