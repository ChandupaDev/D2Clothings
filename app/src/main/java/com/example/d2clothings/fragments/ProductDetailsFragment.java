package com.example.d2clothings.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.d2clothings.R;

public class ProductDetailsFragment extends Fragment {
    private ImageView productImage;
    private TextView productName, productPrice, productDescription;
    private Button btnClose;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_details, container, false);

        productImage = view.findViewById(R.id.productImage);
        productName = view.findViewById(R.id.productName);
        productPrice = view.findViewById(R.id.productPrice);
        productDescription = view.findViewById(R.id.productDescription);
        btnClose = view.findViewById(R.id.btnClose);

        if (getArguments() != null) {
            productName.setText(getArguments().getString("title"));
            productPrice.setText(getArguments().getString("price"));
            productDescription.setText(getArguments().getString("description"));

            // Load image using Glide
            Glide.with(requireContext())
                    .load(getArguments().getString("imageUrl"))
                    .placeholder(R.drawable.profile_placeholder)
                    .into(productImage);
        }

        btnClose.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        return view;
    }
}
