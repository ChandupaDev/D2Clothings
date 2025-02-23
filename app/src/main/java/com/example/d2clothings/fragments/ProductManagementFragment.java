package com.example.d2clothings.fragments;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.d2clothings.R;

public class ProductManagementFragment extends Fragment {
    private EditText productName, productPrice, productDescription;
    private Button btnAddProduct, btnUpdateProduct;
    private RecyclerView productList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_management, container, false);

        initializeViews(view);
        setupListeners();
        loadProducts();

        return view;
    }

    private void initializeViews(View view) {
        productName = view.findViewById(R.id.etProductId);
        productPrice = view.findViewById(R.id.etProductPrice);
        productDescription = view.findViewById(R.id.etProductDescription);
        btnAddProduct = view.findViewById(R.id.btnAddProduct);
        btnUpdateProduct = view.findViewById(R.id.btnUpdateProduct);
//        productList = view.findViewById(R.id.rvProducts);
    }

    private void setupListeners() {
        btnAddProduct.setOnClickListener(v -> addProduct());
        btnUpdateProduct.setOnClickListener(v -> updateProduct());
    }

    private void addProduct() {
        // Add your product adding logic here
        String name = productName.getText().toString();
        String price = productPrice.getText().toString();
        String description = productDescription.getText().toString();

        // Validate inputs
        if (name.isEmpty() || price.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Add to database
        Toast.makeText(getContext(), "Product added successfully", Toast.LENGTH_SHORT).show();
        clearFields();
    }

    private void updateProduct() {
        // Add your product update logic here
        Toast.makeText(getContext(), "Product updated successfully", Toast.LENGTH_SHORT).show();
    }

    private void loadProducts() {
        // TODO: Load products from database
    }

    private void clearFields() {
        productName.setText("");
        productPrice.setText("");
        productDescription.setText("");
    }
}