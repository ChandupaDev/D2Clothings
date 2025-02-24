package com.example.d2clothings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class OdersActivity extends AppCompatActivity {

    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oders);

        // Initialize views
        drawerLayout = findViewById(R.id.mainOA);
        navigationView = findViewById(R.id.navigation_viewOA);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Hide default title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        ImageButton btnDrawer = findViewById(R.id.btnDrawerOA);
        ImageButton btnNotification = findViewById(R.id.btnNotification);

        // Setup click listeners
        btnDrawer.setOnClickListener(v -> {
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        btnNotification.setOnClickListener(v ->
                Toast.makeText(this, "Notifications clicked!", Toast.LENGTH_SHORT).show()
        );

        setupNavigationDrawer();

        // Load initial fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new TrackOdersFragment())
                    .commit();
        }
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
                if (id == R.id.nav_home){
                    startActivity(new Intent(OdersActivity.this, HomeActivity.class));
                } else if(id == R.id.nav_profile) {
                    startActivity(new Intent(OdersActivity.this, ProfileActivity.class));
                } else if (id == R.id.nav_cart) {
                    startActivity(new Intent(OdersActivity.this, CartActivity.class));
                }else if (id == R.id.nav_trackoders) {
                    startActivity(new Intent(this, OdersActivity.class));
                } else if (id == R.id.nav_logout) {
                    logout();
                }
            } catch (Exception e) {
                Log.e("NavigationDrawer", "Error: " + e.getMessage(), e);
                Toast.makeText(OdersActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().remove("userEmail").apply();
        Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
        redirectToSignIn();
    }

    private void redirectToSignIn() {
        Intent intent = new Intent(this, SigninActivity.class);
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

