package com.adetrifauzananisarahel.ui.home;

// Import-import yang dibutuhkan
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.adetrifauzananisarahel.R;
import com.adetrifauzananisarahel.adapter.CarouselAdapter;
import com.adetrifauzananisarahel.adapter.CategoryAdapter;
import com.adetrifauzananisarahel.adapter.ProductAdapter;
import com.adetrifauzananisarahel.network.DatabaseContract;
import com.adetrifauzananisarahel.network.DatabaseHelper;
import com.adetrifauzananisarahel.databinding.FragmentHomeBinding;
import com.adetrifauzananisarahel.model.CarouselItem;
import com.adetrifauzananisarahel.model.Category;
import com.adetrifauzananisarahel.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HomeFragment extends Fragment implements ProductAdapter.OnProductClickListener {

    private FragmentHomeBinding binding;
    private DatabaseHelper dbHelper;

    private CarouselAdapter carouselAdapter;
    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;

    private final List<CarouselItem> carouselItems = new ArrayList<>();
    private final List<Product> productList = new ArrayList<>();
    private final List<Category> categoryList = new ArrayList<>();
    private final List<Product> originalProductList = new ArrayList<>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private final Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;

    // Variabel untuk fitur-fitur baru
    private SearchView searchView;
    private ActivityResultLauncher<Intent> speechRecognitionLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // Inisialisasi Launcher untuk meminta izin mikrofon
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                // Jika izin diberikan, langsung mulai voice search
                launchSpeechToText();
            } else {
                // Jika izin ditolak, beri tahu user
                Toast.makeText(getContext(), "Izin mikrofon ditolak", Toast.LENGTH_SHORT).show();
            }
        });

        // Inisialisasi Launcher untuk menerima hasil dari Speech-to-Text
        speechRecognitionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        ArrayList<String> speechResult = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        if (speechResult != null && !speechResult.isEmpty()) {
                            // Ambil teks pertama dan masukkan ke SearchView
                            String voiceText = speechResult.get(0);
                            if (searchView != null) {
                                // Buka SearchView jika masih tertutup
                                searchView.onActionViewExpanded();
                                searchView.setQuery(voiceText, true); // true untuk langsung submit
                            }
                        }
                    }
                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        dbHelper = new DatabaseHelper(getContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupAdapters();
        setupCategories();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.home_menu, menu);

        // Setup SearchView dan auto-scroll
        MenuItem searchItem = menu.findItem(R.id.action_search);
        this.searchView = (SearchView) searchItem.getActionView();
        this.searchView.setQueryHint("Cari produk...");

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                scrollToProducts();
                return true;
            }
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return true;
            }
        });

        this.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                filterProducts(newText);
                return true;
            }
        });

        // Setup ikon dark/light mode
        MenuItem themeToggleItem = menu.findItem(R.id.action_theme_toggle);
        int nightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightMode == Configuration.UI_MODE_NIGHT_YES) {
            themeToggleItem.setIcon(R.drawable.light);
        } else {
            themeToggleItem.setIcon(R.drawable.dark);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_theme_toggle) {
            int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            return true;
        } else if (id == R.id.action_voice_search) {
            startVoiceSearch();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startVoiceSearch() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            launchSpeechToText();
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO);
        }
    }

    private void launchSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Mulai bicara...");

        try {
            speechRecognitionLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "Maaf, perangkatmu tidak mendukung input suara", Toast.LENGTH_SHORT).show();
        }
    }

    private void scrollToProducts() {
        binding.titleProducts.post(() -> {
            if (binding != null) {
                ((NestedScrollView) binding.getRoot()).smoothScrollTo(0, binding.titleProducts.getTop() - 30);
            }
        });
    }

    private void filterProducts(String query) {
        List<Product> filteredList = new ArrayList<>();
        if (query == null || query.isEmpty()) {
            filteredList.addAll(originalProductList);
        } else {
            for (Product product : originalProductList) {
                if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(product);
                }
            }
        }
        productAdapter.filterList(filteredList);
    }

    private void setupAdapters() {
        carouselAdapter = new CarouselAdapter(carouselItems);
        binding.viewPagerCarousel.setAdapter(carouselAdapter);
        productAdapter = new ProductAdapter(getContext(), productList, this);
        binding.recyclerViewProducts.setNestedScrollingEnabled(false);
        binding.recyclerViewProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.recyclerViewProducts.setAdapter(productAdapter);
        categoryAdapter = new CategoryAdapter(categoryList);
        binding.recyclerViewCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewCategories.setAdapter(categoryAdapter);
    }

    @Override
    public void onProductClick(Product product) {
        Bundle bundle = new Bundle();
        bundle.putLong("PRODUCT_ID", product.getId());
        NavHostFragment.findNavController(this).navigate(R.id.action_home_to_product_detail, bundle);
    }

    private void loadAllDataFromDb() {
        executor.execute(() -> {
            List<CarouselItem> loadedCarouselItems = new ArrayList<>();
            Cursor carouselCursor = dbHelper.getAllCarouselItems();
            if (carouselCursor != null) {
                while (carouselCursor.moveToNext()) {
                    long id = carouselCursor.getLong(carouselCursor.getColumnIndexOrThrow(DatabaseContract.CarouselEntry._ID));
                    String imagePath = carouselCursor.getString(carouselCursor.getColumnIndexOrThrow(DatabaseContract.CarouselEntry.COLUMN_IMAGE_PATH));
                    loadedCarouselItems.add(new CarouselItem(id, imagePath));
                }
                carouselCursor.close();
            }
            List<Product> loadedProducts = new ArrayList<>();
            Cursor productCursor = dbHelper.getAllProducts();
            if (productCursor != null) {
                while (productCursor.moveToNext()) {
                    long id = productCursor.getLong(productCursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry._ID));
                    String name = productCursor.getString(productCursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_NAME));
                    String price = productCursor.getString(productCursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_PRICE));
                    String imagePath = productCursor.getString(productCursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_IMAGE_PATH));
                    String description = productCursor.getString(productCursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_DESCRIPTION));
                    String address = productCursor.getString(productCursor.getColumnIndexOrThrow(DatabaseContract.ProductEntry.COLUMN_ADDRESS));
                    loadedProducts.add(new Product(id, name, price, imagePath, description, address));
                }
                productCursor.close();
            }
            mainThreadHandler.post(() -> {
                carouselItems.clear();
                carouselItems.addAll(loadedCarouselItems);
                carouselAdapter.notifyDataSetChanged();
                if (!carouselItems.isEmpty()) {
                    setupAutoSlide();
                }
                productList.clear();
                originalProductList.clear();
                productList.addAll(loadedProducts);
                originalProductList.addAll(loadedProducts);
                productAdapter.notifyDataSetChanged();
            });
        });
    }

    private void setupCategories() {
        categoryList.clear();
        categoryList.add(new Category("Makanan", R.drawable.food));
        categoryList.add(new Category("Buah", R.drawable.buah));
        categoryList.add(new Category("Buku", R.drawable.pp));
        categoryList.add(new Category("Gaming", R.drawable.pp));
        categoryList.add(new Category("Kecantikan", R.drawable.pp));
        categoryList.add(new Category("Olahraga", R.drawable.pp));
        categoryList.add(new Category("Rumah", R.drawable.pp));
        categoryAdapter.notifyDataSetChanged();
    }

    private void setupAutoSlide() {
        sliderHandler.removeCallbacks(sliderRunnable);
        sliderRunnable = () -> {
            if (binding != null && carouselAdapter.getItemCount() > 0) {
                int currentItem = binding.viewPagerCarousel.getCurrentItem();
                int nextItem = (currentItem + 1) % carouselAdapter.getItemCount();
                binding.viewPagerCarousel.setCurrentItem(nextItem, true);
                sliderHandler.postDelayed(sliderRunnable, 5000);
            }
        };
        sliderHandler.postDelayed(sliderRunnable, 5000);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAllDataFromDb();
        if (sliderRunnable != null) {
            sliderHandler.postDelayed(sliderRunnable, 5000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
