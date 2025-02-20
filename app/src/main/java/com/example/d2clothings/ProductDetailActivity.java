package com.example.d2clothings;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.d2clothings.db.SQLiteHelper;

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
        tvPrice.setText( "Rs. " + price);
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


        int maxQuantity = Integer.parseInt(getIntent().getStringExtra("quantity"));
        Button plusButton = findViewById(R.id.btnIncrease);
        Button minusButton = findViewById(R.id.btnDecrease);
        TextView tvQuantity = findViewById(R.id.tvQuantity);

        tvQuantity.setText(String.valueOf(quantity));

        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (quantity < maxQuantity) {
                    quantity++;
                    tvQuantity.setText(String.valueOf(quantity));
                }
            }
        });

        minusButton.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });

        Button AddToCart =  findViewById(R.id.btnAddToCart);
        AddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteHelper SQLiteCartHelper = new SQLiteHelper(
                        ProductDetailActivity.this,
                        "cart.db",
                        null,
                        1);

                SQLiteDatabase db = SQLiteCartHelper.getReadableDatabase(); // Get readable database


                String query = "SELECT * FROM cart_item WHERE id = ?";
                Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(getIntent().getStringExtra("id"))});

                if (cursor.moveToFirst()) { // If data exists
                    String qty = cursor.getString(cursor.getColumnIndexOrThrow("qty"));
                    TextView tvQuantity = findViewById(R.id.tvQuantity);
                    Log.i("stockCount",String.valueOf(Integer.parseInt(qty) ));
                    Log.i("stockCount",String.valueOf(Integer.parseInt(tvQuantity.getText().toString())));
                    Log.i("stockCount",String.valueOf(Integer.parseInt(qty) + Integer.parseInt(tvQuantity.getText().toString())));
                    if ((Integer.parseInt(qty) + Integer.parseInt(tvQuantity.getText().toString())) < maxQuantity) {
                        //Update Qty
                        SQLiteDatabase dbUpdate = SQLiteCartHelper.getWritableDatabase();

                        // Try to update the record first
                        ContentValues values = new ContentValues();
                        values.put("qty", String.valueOf(Integer.parseInt(qty) + Integer.parseInt(tvQuantity.getText().toString())));

                        int rowsAffected = db.update("cart_item", values, "id=?", new String[]{String.valueOf(getIntent().getStringExtra("id"))});

                        // If no rows were updated, insert a new record
                        if (rowsAffected != 0) {
                            Toast.makeText(ProductDetailActivity.this, "Quantity Updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ProductDetailActivity.this, "Quantity Updating Failed", Toast.LENGTH_SHORT).show();
                        }
                        dbUpdate.close();
                        db.close();

                    } else {
                        //Cant Add Qty Exceeded
                        Toast.makeText(ProductDetailActivity.this, "No Such Stock Select Less Quantity", Toast.LENGTH_SHORT).show();
                    }

                    db.close();
                } else {
                    //new product added to the cart
                    SQLiteDatabase dbInsert = SQLiteCartHelper.getWritableDatabase();

                    // Try to update the record first
                    ContentValues values = new ContentValues();

                    TextView tvQuantity = findViewById(R.id.tvQuantity);

                    Log.i("stockCount",tvQuantity.getText().toString());

                    values.put("id", getIntent().getStringExtra("id"));
                    values.put("title", getIntent().getStringExtra("title"));
                    values.put("price", getIntent().getStringExtra("price"));
                    values.put("qty", tvQuantity.getText().toString());
                    values.put("url", getIntent().getStringExtra("imageUrl"));

                    Log.i("stockdata",getIntent().getStringExtra("id"));
                    Log.i("stockdata",getIntent().getStringExtra("title"));
                    Log.i("stockdata", tvQuantity.getText().toString());
                    Log.i("stockdata",getIntent().getStringExtra("imageUrl"));
                    Log.i("stockdata",getIntent().getStringExtra("price"));


                    long count = dbInsert.insert("cart_item", null,values);

                    if (count == -1) {
                        Toast.makeText(ProductDetailActivity.this, "Failed to insert data", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProductDetailActivity.this, "Data inserted successfully", Toast.LENGTH_SHORT).show();
                    }

                    dbInsert.close(); // Close the database
                }
            }
        });

// Favorite Button Click Event
        findViewById(R.id.btnFavorite).setOnClickListener(v -> {
            Toast.makeText(this, "Added to Favorites!", Toast.LENGTH_SHORT).show();
        });

    }
}
