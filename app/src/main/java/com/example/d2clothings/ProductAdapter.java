package com.example.d2clothings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> productList;

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        // Debugging Logs
        Log.d("ProductAdapter", "Binding Product: " + product.getName() + " | Qty: " + product.getQty());

        // Set product details
        holder.tvTitle.setText(product.getName());
        holder.tvPrice.setText("Rs. " + product.getPrice());

        // Fix the quantity setting to avoid crashes
        if (holder.tvQuantity != null) {
            holder.tvQuantity.setText("Qty: " + product.getQty());
        } else {
            Log.e("ProductAdapter", "tvQuantity is null! Check item_product.xml");
        }

        // Load product image
        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .placeholder(R.drawable.profile_placeholder)
                .into(holder.ivProduct);

        // Handle click event
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
        Log.d("ProductAdapter", "Adapter Item Count: " + productList.size());
        return productList.size();
    }

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
}
