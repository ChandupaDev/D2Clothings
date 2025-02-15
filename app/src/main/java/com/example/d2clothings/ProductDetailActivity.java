package com.example.d2clothings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.d2clothings.R;

public class ProductDetailActivity extends AppCompatActivity {
    private ImageView ivProduct;
    private TextView tvTitle, tvPrice, tvDescription, tvQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail); // Ensure correct layout file

        // Find Views
        ivProduct = findViewById(R.id.ivProduct);
        tvTitle = findViewById(R.id.tvTitle);
        tvPrice = findViewById(R.id.tvPrice);
        tvDescription = findViewById(R.id.tvDescription);
        tvQuantity = findViewById(R.id.tvQuantity);

        // Get Data from Intent
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String price = intent.getStringExtra("price");
        String imageUrl = intent.getStringExtra("imageUrl");
        String description = intent.getStringExtra("description");
        String quantity = intent.getStringExtra("quantity");

        // Debugging: Print received data
        Log.d("ProductDetailsActivity", "Received title: " + title);
        Log.d("ProductDetailsActivity", "Received price: " + price);
        Log.d("ProductDetailsActivity", "Received description: " + description);
        Log.d("ProductDetailsActivity", "Received quantity: " + quantity);

        // Set Data
        if (title != null) {
            tvTitle.setText(title);
        } else {
            tvTitle.setText("No Title Found"); // Fallback text
        }

        tvPrice.setText(price != null ? price : "Rs. 0");
        tvDescription.setText(description != null ? description : "No description available");
        tvQuantity.setText("Quantity: " + (quantity != null ? quantity : "N/A"));

        // Load Image
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.profile_placeholder) // Ensure this image exists
                .into(ivProduct);
    }
}
