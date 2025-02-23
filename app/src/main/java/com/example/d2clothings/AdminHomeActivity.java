package com.example.d2clothings;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import com.example.d2clothings.fragments.OrderManagementFragment;
import com.example.d2clothings.fragments.ProductManagementFragment;
import com.example.d2clothings.fragments.ProductDeleteFragment;

public class AdminHomeActivity extends AppCompatActivity {

    private Button btnManageProducts;
    private Button btnManageOrders;
    private Button btnDeleteProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Admin Dashboard");
        }

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        btnManageProducts = findViewById(R.id.btnManageProducts);
        btnManageOrders = findViewById(R.id.btnManageOrders);
        btnDeleteProducts = findViewById(R.id.btnDeleteProducts);
    }

    private void setupClickListeners() {
        btnManageProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProductManagement();
            }
        });

        btnManageOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOrderManagement();
            }
        });

        btnDeleteProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProductDelete();
            }
        });
    }

    private void openProductManagement() {
        findViewById(R.id.dashboard_content).setVisibility(View.GONE);

        ProductManagementFragment fragment = new ProductManagementFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openOrderManagement() {
        findViewById(R.id.dashboard_content).setVisibility(View.GONE);

        OrderManagementFragment fragment = new OrderManagementFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openProductDelete() {
        findViewById(R.id.dashboard_content).setVisibility(View.GONE);

        ProductDeleteFragment fragment = new ProductDeleteFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            findViewById(R.id.dashboard_content).setVisibility(View.VISIBLE);
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("AdminSession", 0);
        prefs.edit().clear().apply();

        Intent intent = new Intent(this, SigninActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}