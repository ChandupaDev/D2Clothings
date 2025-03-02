package com.example.d2clothings;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ShAdapter extends RecyclerView.Adapter<ShAdapter.ProductViewHolder> {

    private List<Product> productList;
    private List<Product> filteredList; // For search functionality

    public ShAdapter(List<Product> productList) {
        this.productList = productList;
        this.filteredList = new ArrayList<>(productList); // Create a copy for filtering
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = filteredList.get(position);

        // Debugging Logs
        Log.d("ShAdapter", "Binding Product: " + product.getName() + " | Qty: " + product.getQty());

        // Set product details
        holder.tvTitle.setText(product.getName());
        holder.tvPrice.setText("Rs. " + product.getPrice());

        // Avoid crashes if tvQuantity is null
        if (holder.tvQuantity != null) {
            holder.tvQuantity.setText("Qty: " + product.getQty());
        } else {
            Log.e("ShAdapter", "tvQuantity is null! Check item_product.xml");
        }

        // Load product image efficiently
        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .placeholder(R.drawable.profile_placeholder)
                .into(holder.ivProduct);

        // Handle click event to open Product Detail
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProductDetailActivity.class);
            intent.putExtra("id", product.getId());
            intent.putExtra("title", product.getName());
            intent.putExtra("price", String.valueOf(product.getPrice()));
            intent.putExtra("imageUrl", product.getImageUrl());
            intent.putExtra("description", product.getDescription());
            intent.putExtra("quantity", String.valueOf(product.getQty()));

            Log.d("ShAdapter", "Sending qty: " + product.getQty());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    // Method to update data
    public void updateData(List<Product> newProducts) {
        this.productList = new ArrayList<>(newProducts);
        this.filteredList.clear();
        this.filteredList.addAll(newProducts);
        notifyDataSetChanged();
    }

    // ViewHolder class
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPrice, tvQuantity;
        ImageView ivProduct;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity); // Ensure this exists in XML
            ivProduct = itemView.findViewById(R.id.ivProduct);
        }
    }

    // Filtering method for search functionality
    public void filter(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(productList);
        } else {
            for (Product product : productList) {
                if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(product);
                }
            }
        }
        notifyDataSetChanged(); // Refresh the adapter
    }

    // Add a method to filter by price range as well
    public void filter(String query, String priceRange) {
        filteredList.clear();

        for (Product product : productList) {
            boolean matchesQuery = query.isEmpty() ||
                    product.getName().toLowerCase().contains(query.toLowerCase()) ||
                    product.getDescription().toLowerCase().contains(query.toLowerCase());

            boolean matchesPriceRange = true; // Default to true if "All" is selected

            if (!priceRange.equals("All")) {
                try {
                    double price = Double.parseDouble(product.getPrice());

                    switch (priceRange) {
                        case "Under Rs.500":
                            matchesPriceRange = price < 500;
                            break;
                        case "Rs.500-Rs.1000":
                            matchesPriceRange = price >= 500 && price <= 1000;
                            break;
                        case "Rs.1000-Rs.2000":
                            matchesPriceRange = price > 1000 && price <= 2000;
                            break;
                        case "Above Rs.2000":
                            matchesPriceRange = price > 2000;
                            break;
                    }
                } catch (NumberFormatException e) {
                    Log.e("ShAdapter", "Error parsing price: " + product.getPrice(), e);
                    matchesPriceRange = true; // Include if price can't be parsed
                }
            }

            if (matchesQuery && matchesPriceRange) {
                filteredList.add(product);
            }
        }

        notifyDataSetChanged();
    }
}