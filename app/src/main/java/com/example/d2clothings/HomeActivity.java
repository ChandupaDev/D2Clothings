package com.example.d2clothings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

import com.example.d2clothings.db.SQLiteHelper;
import com.example.d2clothings.util.FirestoreHelper;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView tvWelcome;
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private Button btnLogout, btnRefresh;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();

        // Initialize Views
        tvWelcome = findViewById(R.id.tvWelcome);
        recyclerView = findViewById(R.id.recyclerView);
        btnLogout = findViewById(R.id.btnLogout);
        btnRefresh = findViewById(R.id.btnRefresh); // New Refresh Button
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        // Setup Toolbar & Drawer
        setupNavigationDrawer();

        // Setup RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList);
        recyclerView.setAdapter(productAdapter);

        // Load User Data
        String userEmail = getUserEmail();
        if (userEmail == null) {
            redirectToSignIn();
            return;
        }

        loadUserData(userEmail);
        loadProducts();

        // Logout Button Click
        btnLogout.setOnClickListener(v -> logout());

        // Refresh Button Click
        btnRefresh.setOnClickListener(v -> loadProducts());
    }

    // 🔹 Set up the Navigation Drawer
    private void setupNavigationDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Handle Navigation Clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            try {
                if (id == R.id.nav_home) {
                    Toast.makeText(HomeActivity.this, "Already on Home", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_profile) {
                    Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                    startActivity(intent);
                } else if (id == R.id.nav_cart) {
                    Intent intent = new Intent(HomeActivity.this, CartActivity.class);
                    startActivity(intent);
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

    // 🔹 Get Stored Email from SharedPreferences
    private String getUserEmail() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("userEmail", null);
    }

    // 🔹 Load User Data from Firestore
    private void loadUserData(String userEmail) {
        db.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String fullName = document.getString("fullName");
                        tvWelcome.setText("Welcome, " + fullName + "!");
                        return; // Stop loop after first match
                    }
                    Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load user data: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    // 🔹 Load Products from SQLite & Firestore
    private void loadProducts() {
        if (productList == null) {
            productList = new ArrayList<>();
        }
        productList.clear();

        FirestoreHelper firestoreHelper = new FirestoreHelper();
        firestoreHelper.fetchProducts(fetchedProducts -> {
            productList.addAll(fetchedProducts);
            runOnUiThread(() -> {
                Log.d("HomeActivity", "Products loaded: " + productList.size());
                productAdapter.notifyDataSetChanged();
            });
        });
    }



    // 🔹 Refresh Products when Activity Resumes
    @Override
    protected void onResume() {
        super.onResume();
        loadProducts(); // Refresh products when returning to this activity
    }

    // 🔹 Logout User
    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("userEmail"); // Remove user data
        editor.apply();

        Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
        redirectToSignIn();
    }

    // 🔹 Redirect to Sign-in Screen
    private void redirectToSignIn() {
        Intent intent = new Intent(HomeActivity.this, SigninActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // 🔹 Close Drawer on Back Press
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
