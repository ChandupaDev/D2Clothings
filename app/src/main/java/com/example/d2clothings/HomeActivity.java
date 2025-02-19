package com.example.d2clothings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.d2clothings.interfaces.FirestoreCallback;
import com.example.d2clothings.util.FirestoreHelper;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView tvWelcome;
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private EditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();

        // Initialize Views
        tvWelcome = findViewById(R.id.tvWelcome);
        searchBar = findViewById(R.id.search_bar);
        recyclerView = findViewById(R.id.latestProductsRecyclerView);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);


        ImageButton btnDrawer = findViewById(R.id.btnDrawer);
        ImageButton btnNotification = findViewById(R.id.btnNotification);

        btnDrawer.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        btnNotification.setOnClickListener(v ->
                Toast.makeText(HomeActivity.this, "Notifications clicked!", Toast.LENGTH_SHORT).show()
        );


        setupNavigationDrawer();

        // Setup Search Functionality
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (productAdapter != null) {
                    productAdapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Setup Banner ViewPager2
        ViewPager2 bannerViewPager = findViewById(R.id.bannerViewPager);
        List<Integer> images = Arrays.asList(R.drawable.banner, R.drawable.banner, R.drawable.banner);
        BannerAdapter adapter = new BannerAdapter(this, images);
        bannerViewPager.setAdapter(adapter);

        // Setup Toolbar & Navigation Drawer
        setupNavigationDrawer();

        // Setup RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Load User Data
        String userEmail = getUserEmail();
        if (userEmail == null) {
            redirectToSignIn();
            return;
        }
        loadUserData(userEmail);

        // Load Products from Firestore
        fetchProducts();
    }

    private void setupNavigationDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState(); // Ensure proper drawer toggle behavior

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            try {
                if (id == R.id.nav_profile) {
                    startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                } else if (id == R.id.nav_cart) {
                    startActivity(new Intent(HomeActivity.this, CartActivity.class));
                } else if (id == R.id.nav_logout) {
                    logout();
                }
            } catch (Exception e) {
                Log.e("NavigationDrawer", "Error: " + e.getMessage(), e);
                Toast.makeText(HomeActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private String getUserEmail() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("userEmail", null);
    }

    private void loadUserData(String userEmail) {
        db.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String fullName = document.getString("fullName");
                            if (fullName != null && !fullName.isEmpty()) {
                                tvWelcome.setText("Welcome, " + fullName + "!");
                            } else {
                                tvWelcome.setText("Welcome!");
                            }
                            break; // Stop after the first match
                        }
                    } else {
                        tvWelcome.setText("Welcome, Guest!");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load user data: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }


    private void fetchProducts() {
        new FirestoreHelper().fetchProducts(new FirestoreCallback() {
            @Override
            public void onSuccess(List<Product> productList) {
                runOnUiThread(() -> {
                    productAdapter = new ProductAdapter(productList);
                    recyclerView.setAdapter(productAdapter);
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("HomeActivity", "Error fetching products", e);
                Toast.makeText(HomeActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchProducts(); // Auto-refresh products when activity resumes
    }

    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().remove("userEmail").apply();
        Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
        redirectToSignIn();
    }

    private void redirectToSignIn() {
        startActivity(new Intent(HomeActivity.this, SigninActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}
