package com.example.d2clothings;

import static android.app.PendingIntent.getActivity;
import static java.security.AccessController.getContext;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.d2clothings.db.SQLiteHelper;

import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.Item;

public class CartActivity extends AppCompatActivity {
    private static final int PAYHERE_REQUEST = 11001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        // Find the root layout (ConstraintLayout in your XML)
        View rootView = findViewById(R.id.root_layout);

        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        findViewById(R.id.ivBack1).setOnClickListener(v -> finish());

        // Setup checkout button
        setupCheckoutButton();
    }

    private void setupCheckoutButton() {
        // Find the checkout button
        View checkoutButton = findViewById(R.id.btnCheckout);
        if (checkoutButton != null) {
            checkoutButton.setOnClickListener(v -> {
                // Get current cart total
                TextView totalView = findViewById(R.id.TotalCheckout);
                String totalText = totalView.getText().toString().replace("Rs. ", "");

                try {
                    double totalAmount = Double.parseDouble(totalText);
                    if (totalAmount > 0) {
                        initiatePaymentGateway(totalAmount);
                    } else {
                        Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid total amount", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void initiatePaymentGateway(double amount) {
        // Create a unique order ID based on timestamp
        String orderId = "ORD" + System.currentTimeMillis();

        InitRequest req = new InitRequest();
        req.setMerchantId("1227564");       // Merchant ID
        req.setCurrency("LKR");             // Currency code LKR/USD/GBP/EUR/AUD
        req.setAmount(amount);              // Final Amount to be charged
        req.setOrderId(orderId);            // Unique Reference ID
        req.setItemsDescription("D2 Clothing Order");  // Item description title

        // Set customer information (You may want to get this from user input or stored profile)
        req.getCustomer().setFirstName("Chandupa");
        req.getCustomer().setLastName("");
        req.getCustomer().setEmail("Chandupajayalath20@gmail.com");
        req.getCustomer().setPhone("");
        req.getCustomer().getAddress().setAddress("20,lk,Sri Lanka");
        req.getCustomer().getAddress().setCity("Anuradhapura");
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        // Add items from cart to payment request
        addCartItemsToRequest(req);

        Intent intent = new Intent(this, PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
        startActivityForResult(intent, PAYHERE_REQUEST);
    }

    private void addCartItemsToRequest(InitRequest req) {
        SQLiteHelper sqLiteHelper = new SQLiteHelper(
                CartActivity.this,
                "cart.db",
                null,
                1);

        SQLiteDatabase sqLiteDatabase = sqLiteHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(
                "cart_item",
                null,
                null,
                null,
                null,
                null,
                "id DESC"
        );

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String qty = cursor.getString(cursor.getColumnIndexOrThrow("qty"));
                String price = cursor.getString(cursor.getColumnIndexOrThrow("price"));

                int quantity = Integer.parseInt(qty);
                double itemPrice = Double.parseDouble(price);

                req.getItems().add(new Item(null, title, quantity, itemPrice));
            }
            cursor.close();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PAYHERE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                // Payment successful
                String status = data.getStringExtra(PHConstants.INTENT_EXTRA_RESULT);
                if (status != null && !status.equals("FAILED") && !status.equals("CANCELLED")) {
                    // Payment successful
                    clearCart();
                    Toast.makeText(this, "Payment successful!", Toast.LENGTH_LONG).show();
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Payment canceled
                Toast.makeText(this, "Payment canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void clearCart() {
        SQLiteHelper sqLiteHelper = new SQLiteHelper(
                CartActivity.this,
                "cart.db",
                null,
                1);

        SQLiteDatabase sqLiteDatabase = sqLiteHelper.getWritableDatabase();
        sqLiteDatabase.delete("cart_item", null, null);

        // Refresh the cart UI
        recreate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RecyclerView recyclerView1 = findViewById(R.id.recyclerCart);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView1.setLayoutManager(linearLayoutManager);

        TextView checkoutTotalView = findViewById(R.id.TotalCheckout);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(0, ItemTouchHelper.LEFT);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Toast.makeText(CartActivity.this, "Swiped", Toast.LENGTH_SHORT).show();

                CartAdapter.CartViewHolder holder= (CartAdapter.CartViewHolder) viewHolder;

                SQLiteHelper sqLiteHelper = new SQLiteHelper(
                        viewHolder.itemView.getContext(),
                        "cart.db",
                        null,
                        1);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SQLiteDatabase sqLiteDatabase = sqLiteHelper.getWritableDatabase();
                        int row = sqLiteDatabase.delete("cart_item", "id=?", new String[]{holder.id});

                        // Query the updated data
                        SQLiteDatabase readableDb = sqLiteHelper.getReadableDatabase();
                        Cursor newCursor = readableDb.query(
                                "cart_item",
                                null,
                                null,
                                null,
                                null,
                                null,
                                "id DESC"
                        );

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                CartAdapter adapter= (CartAdapter) recyclerView1.getAdapter();
                                adapter.updateCursor(newCursor,checkoutTotalView);
                            }
                        });
                    }
                }).start();
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView1);

        SQLiteHelper sqLiteHelper = new SQLiteHelper(
                CartActivity.this,
                "cart.db",
                null,
                1);

        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase sqLiteDatabase = sqLiteHelper.getReadableDatabase();
                Cursor cursor = sqLiteDatabase.query(
                        "cart_item",
                        null,
                        null,
                        null,
                        null,
                        null,
                        "id DESC"
                );

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CartAdapter cartListAdapter = new CartAdapter(cursor,checkoutTotalView);
                        recyclerView1.setAdapter(cartListAdapter);
                    }
                });
            }
        }).start();
    }
}


class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    int subtotalAmount;
    private static final double DELIVERY_FEE = 150.0;
    private static final double TAX_FEE = 50.0;

    private TextView checkoutTotalView;
    private TextView subtotalTextView;
    private TextView deliveryTextView;
    private TextView taxTextView;
    Cursor cursor;

    public CartAdapter(Cursor cursor, TextView checkoutTotalView) {
        this.cursor = cursor;
        this.checkoutTotalView = checkoutTotalView;
        this.subtotalAmount = 0; // Reset subtotal

        // Find all the required TextViews
        View rootView = checkoutTotalView.getRootView();
        this.subtotalTextView = rootView.findViewById(R.id.SubTot);
        this.deliveryTextView = rootView.findViewById(R.id.DeliveryFee);
        this.taxTextView = rootView.findViewById(R.id.Tax);

        // Calculate initial subtotal by looping through all items
        calculateSubtotal();

        // Update all summary fields
        updateSummaryFields();
    }

    private void calculateSubtotal() {
        subtotalAmount = 0;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                String qty = cursor.getString(cursor.getColumnIndexOrThrow("qty"));
                String price = cursor.getString(cursor.getColumnIndexOrThrow("price"));
                int itemTotal = Integer.parseInt(qty) * Integer.parseInt(price);
                subtotalAmount += itemTotal;
            }
        }
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        // Existing code remains the same
        TextView ctitle;
        TextView cdetails;
        TextView cCheckoutTotal;
        TextView ctotal;
        ImageView cItemImage;
        String id;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ctitle = itemView.findViewById(R.id.tvProductName);
            cdetails = itemView.findViewById(R.id.tvProductPriceQty);
            ctotal = itemView.findViewById(R.id.CartQty);
            cItemImage = itemView.findViewById(R.id.ivProduct);
            cCheckoutTotal = itemView.findViewById(R.id.TotalCheckout);
        }
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Existing code remains the same
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_cart, parent, false);
        CartViewHolder cartViewHolder = new CartViewHolder(view);
        return cartViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        cursor.moveToPosition(position);

        String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
        String qty = cursor.getString(cursor.getColumnIndexOrThrow("qty"));
        String price = cursor.getString(cursor.getColumnIndexOrThrow("price"));
        String url = cursor.getString(cursor.getColumnIndexOrThrow("url"));

        holder.ctitle.setText(title);
        holder.id = cursor.getString(cursor.getColumnIndexOrThrow("id"));

        int total = Integer.parseInt(qty) * Integer.parseInt(price);

        Glide.with(holder.itemView.getContext())
                .load(url)
                .apply(new RequestOptions().timeout(60000))
                .into(holder.cItemImage);

        holder.cdetails.setText("Rs. " + price + " x " + qty);
        holder.ctotal.setText("Rs. " + String.valueOf(total));
    }

    private void updateSummaryFields() {
        // Check if cart is empty
        boolean isCartEmpty = (cursor == null || cursor.getCount() == 0);

        // Update subtotal
        if (subtotalTextView != null) {
            subtotalTextView.setText(String.format("Rs. %.2f", (double)subtotalAmount));
        }

        // Update delivery fee - set to 0 if cart is empty
        if (deliveryTextView != null) {
            double deliveryAmount = isCartEmpty ? 0 : DELIVERY_FEE;
            deliveryTextView.setText(String.format("Rs. %.2f", deliveryAmount));
        }

        // Update tax - set to 0 if cart is empty
        if (taxTextView != null) {
            double taxAmount = isCartEmpty ? 0 : TAX_FEE;
            taxTextView.setText(String.format("Rs. %.2f", taxAmount));
        }

        // Calculate the total
        double totalAmount = subtotalAmount;
        if (!isCartEmpty) {
            totalAmount += DELIVERY_FEE + TAX_FEE;
        }

        // Update the total checkout amount
        if (checkoutTotalView != null) {
            checkoutTotalView.setText(String.format("Rs. %.2f", totalAmount));
        }
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    public void updateCursor(Cursor newCursor, TextView checkoutTotalView) {
        if (cursor != null) {
            cursor.close();
        }
        cursor = newCursor;
        this.checkoutTotalView = checkoutTotalView;

        // Recalculate subtotal with the new cursor
        calculateSubtotal();

        // Update all summary fields with the new data
        updateSummaryFields();

        notifyDataSetChanged();
    }
}