package com.example.d2clothings.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

import com.example.d2clothings.R;
import com.example.d2clothings.util.FirestoreHelper;
import com.example.d2clothings.interfaces.FirestoreOperationCallback;

public class ProductDeleteFragment extends Fragment {
    private static final String TAG = "ProductDeleteFragment";
    private EditText etProductId;
    private Button btnDeleteProduct;
    private FirestoreHelper firestoreHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_delete, container, false);
        initializeViews(view);
        setupListeners();
        return view;
    }

    private void initializeViews(View view) {
        etProductId = view.findViewById(R.id.etProductId);
        btnDeleteProduct = view.findViewById(R.id.btnDeleteProduct);
        firestoreHelper = new FirestoreHelper();
    }

    private void setupListeners() {
        btnDeleteProduct.setOnClickListener(v -> deleteProduct());
    }

    private void deleteProduct() {
        String productId = etProductId.getText().toString().trim();
        Log.d(TAG, "Attempting to delete product with ID: " + productId);

        if (productId.isEmpty()) {
            etProductId.setError("Please enter product ID");
            return;
        }

        btnDeleteProduct.setEnabled(false);

        // First check if product exists
        firestoreHelper.checkProductExists(productId, new FirestoreOperationCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean exists) {
                if (exists) {
                    // Product exists, proceed with deletion
                    firestoreHelper.deleteProduct(productId, new FirestoreOperationCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            showToast("Product deleted successfully");
                            etProductId.setText("");
                            btnDeleteProduct.setEnabled(true);
                        }

                        @Override
                        public void onFailure(String error) {
                            showToast("Failed to delete product: " + error);
                            btnDeleteProduct.setEnabled(true);
                        }
                    });
                } else {
                    showToast("Product not found with ID: " + productId);
                    btnDeleteProduct.setEnabled(true);
                }
            }

            @Override
            public void onFailure(String error) {
                showToast("Error checking product: " + error);
                btnDeleteProduct.setEnabled(true);
            }
        });
    }

    private void showToast(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() ->
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show()
            );
        }
    }
}