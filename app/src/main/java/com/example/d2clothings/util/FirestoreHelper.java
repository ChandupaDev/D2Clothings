package com.example.d2clothings.util;

import android.util.Log;
import com.example.d2clothings.Product;
import com.example.d2clothings.interfaces.FirestoreCallback;
import com.example.d2clothings.interfaces.FirestoreOperationCallback;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class FirestoreHelper {
    private static final String TAG = "FirestoreHelper";
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
                            String id = doc.getId(); // Use document ID if 'id' field is missing
                            String title = doc.getString("name") != null ? doc.getString("name") : "No Title";
                            String description = doc.getString("description") != null ? doc.getString("description") : "No Description";
                            String imageUrl = doc.getString("imageUrl") != null ? doc.getString("imageUrl") : "";
                            String price = doc.getString("price") != null ? doc.getString("price") : "0";

                            // Check if qty field exists and handle its absence
                            String qty = "0";
                            if (doc.contains("qty")) {
                                qty = doc.getString("qty") != null ? doc.getString("qty") : "0";
                            } else {
                                Log.w("FirestoreHelper", "Field 'qty' not found in Firestore document: " + doc.getId());
                                // Use default value of "0"
                            }

                            // Debugging Logs
                            Log.d("FirestoreHelper", "Title: " + title);
                            Log.d("FirestoreHelper", "Price: " + price);
                            Log.d("FirestoreHelper", "Quantity: " + qty);

                            // Create product with safe values
                            Product product = new Product(
                                    id,
                                    title,
                                    description,
                                    price,
                                    qty,
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
                .addOnFailureListener(e -> {
                    Log.e("FirestoreHelper", "Failed to fetch products", e);
                    callback.onFailure(e);
                });
    }

    public void deleteProduct(String productId, FirestoreOperationCallback<Void> callback) {
        if (productId == null || productId.isEmpty()) {
            callback.onFailure("Product ID cannot be empty");
            return;
        }

        Log.d(TAG, "Attempting to delete product with ID: " + productId);

        db.collection("products")
                .document(productId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Product successfully deleted: " + productId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting product: " + e.getMessage());
                    callback.onFailure("Failed to delete: " + e.getMessage());
                });
    }

    public void checkProductExists(String productId, FirestoreOperationCallback<Boolean> callback) {
        Log.d(TAG, "Checking if product exists with ID: " + productId);

        db.collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean exists = documentSnapshot.exists();
                    Log.d(TAG, "Product exists check result: " + exists + " for ID: " + productId);
                    callback.onSuccess(exists);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking product existence: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    // Helper method to query product by ID
    public void getProductById(String productId, FirestoreOperationCallback<Product> callback) {
        Log.d(TAG, "Querying product by ID: " + productId);

        db.collection("products")
                .whereEqualTo("id", productId)  // Try matching against the 'id' field
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        Log.d(TAG, "Found product with matching ID field: " + document.getData());
                        callback.onSuccess(document.toObject(Product.class));
                    } else {
                        Log.d(TAG, "No product found with matching ID field: " + productId);
                        callback.onFailure("Product not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error querying product: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }
}