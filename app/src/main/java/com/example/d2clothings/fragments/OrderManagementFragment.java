package com.example.d2clothings.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.d2clothings.R;

public class OrderManagementFragment extends Fragment {
    private RecyclerView orderList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_management, container, false);

        orderList = view.findViewById(R.id.rvOrders);
        orderList.setLayoutManager(new LinearLayoutManager(getContext()));

        loadOrders();

        return view;
    }

    private void loadOrders() {
        // TODO: Load orders from database and set adapter
    }

    private void updateOrderStatus(String orderId, String status) {
        // TODO: Update order status in database
    }
}