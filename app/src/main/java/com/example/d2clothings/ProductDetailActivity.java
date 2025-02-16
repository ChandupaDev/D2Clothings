package com.example.d2clothings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class ProductDetailActivity extends AppCompatActivity {
    private ImageView ivProduct;
    private TextView tvTitle, tvPrice, tvDescription, tvQuantity;
    private Button btnIncrease, btnDecrease;
    private int quantity = 1; // Default quantity starts from 1

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Find Views
        ivProduct = findViewById(R.id.ivProduct);
        tvTitle = findViewById(R.id.tvTitle);
        tvPrice = findViewById(R.id.tvPrice);
        tvDescription = findViewById(R.id.tvDescription);
        tvQuantity = findViewById(R.id.tvQuantity);
        btnIncrease = findViewById(R.id.btnIncrease);
        btnDecrease = findViewById(R.id.btnDecrease);

        // Get Data from Intent
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String price = intent.getStringExtra("price");
        String imageUrl = intent.getStringExtra("imageUrl");
        String description = intent.getStringExtra("description");

        // Debugging: Print received data
        Log.d("ProductDetailActivity", "Received title: " + title);
        Log.d("ProductDetailActivity", "Received price: " + price);
        Log.d("ProductDetailActivity", "Received description: " + description);

        // Set Data
        tvTitle.setText(title != null ? title : "No Title Found");
        tvPrice.setText(price != null ? price : "Rs. 0");
        tvDescription.setText(description != null ? description : "No description available");
        tvQuantity.setText(String.valueOf(quantity));

        // Load Image
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.profile_placeholder)
                .into(ivProduct);

        // Increase Quantity
        btnIncrease.setOnClickListener(v -> {
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
        });

        // Decrease Quantity (Prevent going below 1)
        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });

        // Back Button Click Event
        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

// Share Button Click Event
        findViewById(R.id.ivShare).setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this product: " + tvTitle.getText());
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

// Favorite Button Click Event
        findViewById(R.id.btnFavorite).setOnClickListener(v -> {
            Toast.makeText(this, "Added to Favorites!", Toast.LENGTH_SHORT).show();
        });

    }
}
