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

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> productList;
    private List<Product> filteredList; // For search functionality

    public ProductAdapter(List<Product> productList) {
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
        Log.d("ProductAdapter", "Binding Product: " + product.getName() + " | Qty: " + product.getQty());

        // Set product details
        holder.tvTitle.setText(product.getName());
        holder.tvPrice.setText("Rs. " + product.getPrice());

        // Avoid crashes if tvQuantity is null
        if (holder.tvQuantity != null) {
            holder.tvQuantity.setText("Qty: " + product.getQty());
        } else {
            Log.e("ProductAdapter", "tvQuantity is null! Check item_product.xml");
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
            intent.putExtra("price", "Rs. " + product.getPrice());
            intent.putExtra("imageUrl", product.getImageUrl());
            intent.putExtra("description", product.getDescription());
            intent.putExtra("quantity", String.valueOf(product.getQty()));

            Log.d("ProductAdapter", "Sending qty: " + product.getQty());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
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
}
