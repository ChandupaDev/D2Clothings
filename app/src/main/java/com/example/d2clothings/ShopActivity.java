package com.example.d2clothings;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.d2clothings.interfaces.FirestoreCallback;
import com.example.d2clothings.util.FirestoreHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class ShopActivity extends AppCompatActivity {
    private static final String TAG = "ShopActivity";

    // UI Components
    private RecyclerView recyclerProducts;
    private EditText etSearch;
    private ImageView ivClearSearch;
    private ChipGroup chipGroupCategories;
    private LinearLayout emptyView;
    private ProgressBar progressBar;
    private TextView tvAllProducts;

    // Data
    private FirestoreHelper firestoreHelper;
    private ShAdapter ShAdapter;
    private List<Product> productList = new ArrayList<>();
    private List<Product> originalProductList = new ArrayList<>(); // Keep original list for filtering

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        // Initialize UI components
        initViews();

        // Initialize Firestore
        firestoreHelper = new FirestoreHelper();

        // Setup RecyclerView
        setupRecyclerView();

        // Load products immediately when activity starts
        loadProducts();

        // Setup listeners
        setupListeners();
    }

    private void initViews() {
        recyclerProducts = findViewById(R.id.recyclerProducts);
        etSearch = findViewById(R.id.etSearch);
        ivClearSearch = findViewById(R.id.ivClearSearch);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        emptyView = findViewById(R.id.emptyView);
        progressBar = findViewById(R.id.progressBar);
        tvAllProducts = findViewById(R.id.tvAllProducts);

        // Use chip group for price ranges
        updateChipsForPriceRanges();
    }

    private void updateChipsForPriceRanges() {
        // Clear existing chips
        chipGroupCategories.removeAllViews();

        // Add "All" chip
        Chip allChip = new Chip(this);
        allChip.setId(View.generateViewId());
        allChip.setText("All");
        allChip.setCheckable(true);
        allChip.setChecked(true);
        chipGroupCategories.addView(allChip);

        // Add price range chips
        String[] priceRanges = {"Under Rs.500", "Rs.500-Rs.1000", "Rs.1000-Rs.2000", "Above Rs.2000"};
        for (String range : priceRanges) {
            Chip chip = new Chip(this);
            chip.setId(View.generateViewId());
            chip.setText(range);
            chip.setCheckable(true);
            chipGroupCategories.addView(chip);
        }
    }

    private void setupRecyclerView() {
        recyclerProducts.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns grid
        ShAdapter = new ShAdapter(new ArrayList<>()); // Initialize with an empty list
        recyclerProducts.setAdapter(ShAdapter);
    }

    private void loadProducts() {
        showLoading(true);

        firestoreHelper.fetchProducts(new FirestoreCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                runOnUiThread(() -> {
                    showLoading(false);
                    if (products != null && !products.isEmpty()) {
                        // Store the original list for filtering
                        originalProductList = new ArrayList<>(products);
                        productList = new ArrayList<>(products);
                        updateProductList(productList);
                    } else {
                        showEmptyView(true);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showEmptyView(true);
                    Log.e(TAG, "Failed to load products", e);
                    Toast.makeText(ShopActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateProductList(List<Product> products) {
        if (ShAdapter == null) {
            ShAdapter = new ShAdapter(products);
            recyclerProducts.setAdapter(ShAdapter);
        } else {
            ShAdapter.updateData(products);
        }

        showEmptyView(products.isEmpty());

        // Update the header with count
        updateProductHeaderText(getSelectedPriceRange());
    }

    private void setupListeners() {
        // Search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Show clear button if there's text
                ivClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);

                // Filter products
                filterProducts(s.toString(), getSelectedPriceRange());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Clear search button
        ivClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            ivClearSearch.setVisibility(View.GONE);
        });

        // Price range chip selection
        chipGroupCategories.setOnCheckedChangeListener((group, checkedId) -> {
            String searchText = etSearch.getText().toString();
            String priceRange = "All";

            if (checkedId != View.NO_ID) {
                Chip selectedChip = findViewById(checkedId);
                if (selectedChip != null) {
                    priceRange = selectedChip.getText().toString();
                }
            }

            filterProducts(searchText, priceRange);
        });
    }

    private String getSelectedPriceRange() {
        int checkedId = chipGroupCategories.getCheckedChipId();
        if (checkedId != View.NO_ID) {
            Chip selectedChip = findViewById(checkedId);
            if (selectedChip != null) {
                return selectedChip.getText().toString();
            }
        }
        return "All";
    }

    private void filterProducts(String searchText, String priceRange) {
        // Use the adapter's filter method directly
        ShAdapter.filter(searchText, priceRange);

        // Update the header count using the filtered count
        int filteredCount = ShAdapter.getItemCount();
        if (priceRange.equals("All")) {
            tvAllProducts.setText("All Products (" + filteredCount + ")");
        } else {
            tvAllProducts.setText(priceRange + " (" + filteredCount + ")");
        }

        // Show empty view if no results
        showEmptyView(filteredCount == 0);
    }

    private boolean matchesPriceRange(Product product, String priceRange) {
        if (priceRange.equals("All")) {
            return true;
        }

        try {
            double price = Double.parseDouble(product.getPrice());

            switch (priceRange) {
                case "Under Rs.500":
                    return price < 500;
                case "Rs.500-Rs.1000":
                    return price >= 500 && price <= 1000;
                case "Rs.1000-Rs.2000":
                    return price > 1000 && price <= 2000;
                case "Above Rs.2000":
                    return price > 2000;
                default:
                    return true;
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing price: " + product.getPrice(), e);
            return true; // Include if price can't be parsed
        }
    }

    private void updateProductHeaderText(String priceRange) {
        int count = ShAdapter != null ? ShAdapter.getItemCount() : 0;
        if (priceRange.equals("All")) {
            tvAllProducts.setText("All Products (" + count + ")");
        } else {
            tvAllProducts.setText(priceRange + " (" + count + ")");
        }
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerProducts.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void showEmptyView(boolean isEmpty) {
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerProducts.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}