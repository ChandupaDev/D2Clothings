package com.example.d2clothings.util;

import android.util.Log;
import com.example.d2clothings.Product;
import com.example.d2clothings.interfaces.FirestoreCallback;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class FirestoreHelper {
    private final FirebaseFirestore db;

    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
    }

    public void fetchProducts(FirestoreCallback callback) {
        db.collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> productList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Log.d("FirestoreHelper", "Fetched Document: " + doc.getData());

                        try {
                            // Fetch data safely
                            String title = doc.getString("name") != null ? doc.getString("name") : "No Title";
                            String description = doc.getString("description") != null ? doc.getString("description") : "No Description";
                            String imageUrl = doc.getString("imageUrl") != null ? doc.getString("imageUrl") : "";

                            // Fetch price safely
                            String price = doc.getString("price") != null ? doc.getString("price") : "";


                            String qty = doc.getString("qty") != null ? doc.getString("qty") : "";

                             {
                                Log.e("FirestoreHelper", "Field 'qty' not found in Firestore document: " + doc.getId());
                            }




                            // Debugging Logs
                            Log.d("FirestoreHelper", "Title: " + title);
                            Log.d("FirestoreHelper", "Price: " + price);
                            Log.d("FirestoreHelper", "Quantity: " + qty);

                            // **Pass qty to Product**
                            Product product = new Product(
                                    doc.getString("id"),
                                    title,
                                    description,
                                    price,
                                    qty, // ✅ Now qty is defined
                                    imageUrl
                            );
                            productList.add(product);
                        } catch (Exception e) {
                            Log.e("FirestoreHelper", "Error parsing product", e);
                        }
                    }
                    Log.d("FirestoreHelper", "Total Products Fetched: " + productList.size());
                    callback.onSuccess(productList);
                })
                .addOnFailureListener(e -> Log.e("FirestoreHelper", "Failed to fetch products", e));
    }

}