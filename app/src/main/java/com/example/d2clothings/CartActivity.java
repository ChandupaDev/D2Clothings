package com.example.d2clothings;

import static android.app.PendingIntent.getActivity;
import static java.security.AccessController.getContext;

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

public class CartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        // Find the root layout (ConstraintLayout in your XML)
        View rootView = findViewById(R.id.root_layout);  // Change 'root_layout' to the correct ID

        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
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

    int checkoutTotal;

    private TextView checkoutTotalView;
    Cursor cursor;

    public CartAdapter(Cursor cursor, TextView checkoutTotalView) {
        this.cursor = cursor;
        this.checkoutTotalView = checkoutTotalView;
        this.checkoutTotal = 0; // Reset total
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {

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
        checkoutTotal += total;

        Glide.with(holder.itemView.getContext())
                .load(url)
                .apply(new RequestOptions().timeout(60000))
                .into(holder.cItemImage);

        holder.cdetails.setText("Rs. " + price + " x " + qty);
        holder.ctotal.setText("Rs. " + String.valueOf(total));

        // Update the checkout total TextView
        if (checkoutTotalView != null) {
            checkoutTotalView.setText("Total: Rs. " + checkoutTotal);
        }

    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    public void removeItem(int position) {
        notifyItemRemoved(position);
    }

    public void updateCursor(Cursor newCursor, TextView checkoutTotalView) {
        if (cursor != null) {
            cursor.close();
        }
        cursor = newCursor;
        this.checkoutTotalView = checkoutTotalView;
        this.checkoutTotal = 0; // Reset total
        notifyDataSetChanged();
    }
}