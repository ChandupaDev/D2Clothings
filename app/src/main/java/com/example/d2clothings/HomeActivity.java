package com.example.d2clothings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
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

import com.example.d2clothings.interfaces.FirestoreCallback;
import com.example.d2clothings.util.FirestoreHelper;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView tvWelcome;
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
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
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        // Setup Toolbar & Drawer
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
        toggle.syncState();

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
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String fullName = document.getString("fullName");
                        tvWelcome.setText("Welcome, " + fullName + "!");
                        return;
                    }
                    Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
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
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("userEmail");
        editor.apply();

        Log.d("Logout", "User email after logout: " + sharedPreferences.getString("userEmail", "No email found"));

        Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
        redirectToSignIn();
    }

    private void redirectToSignIn() {
        Intent intent = new Intent(HomeActivity.this, SigninActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
