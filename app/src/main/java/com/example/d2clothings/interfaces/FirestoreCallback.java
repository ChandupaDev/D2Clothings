package com.example.d2clothings.interfaces;

import com.example.d2clothings.Product;
import java.util.List;

public interface FirestoreCallback {
    void onSuccess(List<Product> productList);
}