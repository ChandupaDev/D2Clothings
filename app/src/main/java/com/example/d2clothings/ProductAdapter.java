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
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.d2clothings.fragments.ProductDetailsFragment;

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

        Log.d("ProductAdapter", "Binding Product: " + product.getName() + " | Qty: " + product.getQty());  // Debugging

        // Set product title and price
        holder.tvTitle.setText(product.getName());
        holder.tvPrice.setText("Rs. " + product.getPrice());

        // Load product image with Glide
        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .placeholder(R.drawable.profile_placeholder)
                .into(holder.ivProduct);

        // Handle item click to open ProductDetailsActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProductDetailActivity.class);
            intent.putExtra("id", product.getId());
            intent.putExtra("title", product.getName());
            intent.putExtra("price", "Rs. " + product.getPrice());
            intent.putExtra("imageUrl", product.getImageUrl());
            intent.putExtra("description", product.getDescription());
            intent.putExtra("quantity", String.valueOf(product.getQty()));

            Log.d("ProductAdapter", "Sending qty: " + product.getQty());  // Debugging
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
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            ivProduct = itemView.findViewById(R.id.ivProduct);
        }
    }
}
