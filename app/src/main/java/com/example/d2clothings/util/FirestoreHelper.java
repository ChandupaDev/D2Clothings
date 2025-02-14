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
                        Log.d("FirestoreHelper", "Fetched Document: " + doc.getData()); // Debugging Log
                        try {
                            Object priceObj = doc.get("price");
                            Long price = null;
                            if (priceObj instanceof Long) {
                                price = (Long) priceObj;
                            } else if (priceObj instanceof Double) {
                                price = ((Double) priceObj).longValue();
                            }

                            Product product = new Product(
                                    doc.getString("id"),
                                    doc.getString("title"),
                                    doc.getString("description"),
                                    price,
                                    doc.getString("qty"),
                                    doc.getString("imageUrl")
                            );
                            productList.add(product);
                        } catch (Exception e) {
                            Log.e("FirestoreHelper", "Error parsing product: " + e.getMessage());
                        }
                    }
                    Log.d("FirestoreHelper", "Total Products Fetched: " + productList.size());
                    callback.onSuccess(productList);
                })
                .addOnFailureListener(e -> Log.e("FirestoreHelper", "Failed to fetch products", e));
    }

}
