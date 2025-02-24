package com.example.d2clothings.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.d2clothings.Order;
import com.example.d2clothings.fragments.OrderAdapter;
import com.example.d2clothings.Order_Item;
import com.example.d2clothings.R;
import com.example.d2clothings.databinding.FragmentOrderManagementBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OrderManagementFragment extends Fragment {

    private FragmentOrderManagementBinding binding;

    private FirebaseFirestore firestore;
    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private List<Order> orders;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_management, container, false);

        binding = FragmentOrderManagementBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firestore = FirebaseFirestore.getInstance();

        // Set up RecyclerView for orders
        recyclerView = binding.AdminOrderManagementRV;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        orders = new ArrayList<>();
        adapter = new OrderAdapter(firestore, getContext(), orders);
        recyclerView.setAdapter(adapter);


        loadOrders();


        return root;
    }

    private void loadOrders() {
        firestore.collection("orders").orderBy("order_id").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orders.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = document.toObject(Order.class);
                        if (order != null) orders.add(order);
                    }
                    requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                })
                .addOnFailureListener(e -> Log.e("FirestoreError", "Query failed", e));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}

class OrderAdapter extends RecyclerView.Adapter<com.example.d2clothings.fragments.OrderAdapter.OrderViewHolder> {
    private final List<Order> orders;
    private FirebaseFirestore firestore;
    private Context context; // Add context variable


    static class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView adminOrderIdText, adminOrderTotal;
        private EditText adminULocation;
        private ConstraintLayout expandableLayout;
        private ImageView arrowImage;
        private RecyclerView adminOrderItemRecyclerView;

        private Button btnAdminUpdateLocation;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);

            adminOrderIdText = itemView.findViewById(R.id.adminOrderIdText);
            expandableLayout = itemView.findViewById(R.id.adminOrderExpandableLayout);
            arrowImage = itemView.findViewById(R.id.adminOrderArrowImage);
            adminOrderItemRecyclerView = itemView.findViewById(R.id.adminSingleOrderItemRecyclerView);
            btnAdminUpdateLocation = itemView.findViewById(R.id.btnAdminUpdateLocation);
            adminOrderTotal = itemView.findViewById(R.id.adminOrderTotal);
            adminULocation = itemView.findViewById(R.id.adminULocation);
        }
    }

    public interface OnOrderClickListener {
        void onOrderClicked(String orderId);
    }

    public OrderAdapter(FirebaseFirestore firestore, Context context, List<Order> orders) {
        this.orders = orders;
        this.context = context;
        this.firestore = firestore;
    }

    @NonNull
    @Override
    public OrderAdapter.OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ✅ Use fragment_order_management.xml for the OrderAdapter
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull com.example.d2clothings.fragments.OrderAdapter.OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.adminOrderIdText.setText("#" + order.getOrder_id());
        boolean isExpandable = order.isExpandable();

        holder.expandableLayout.setVisibility(isExpandable ? View.VISIBLE : View.GONE);
        holder.arrowImage.setImageResource(isExpandable ? R.drawable.arrow_up : R.drawable.arrow_down);

        // Create a list to hold order items for this specific order
        List<Order_Item> orderItems = new ArrayList<>();
        com.example.d2clothings.fragments.OrderItemAdapter orderItemAdapter = new com.example.d2clothings.fragments.OrderItemAdapter(orderItems);

        // Set up RecyclerView
        holder.adminOrderItemRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.adminOrderItemRecyclerView.setAdapter(orderItemAdapter);

        // If expanded, load the items
        if (isExpandable) {
            loadSingleOrderItems(order.getOrder_id(), orderItems, orderItemAdapter, total -> {
                Log.d("OrderTotal", "Total Order Price: " + total);
                holder.adminOrderTotal.setText("Rs. "+String.valueOf(total));
            });
        }

        holder.itemView.setOnClickListener(view -> {
            boolean newExpandableState = !order.isExpandable();
            order.setExpandable(newExpandableState);

            // Load items when expanding
            if (newExpandableState) {
                loadSingleOrderItems(order.getOrder_id(), orderItems, orderItemAdapter, total -> {
                    Log.d("OrderTotal", "Total Order Price: " + total);
                    holder.adminOrderTotal.setText("Rs. "+String.valueOf(total));
                });
            }

            notifyItemChanged(holder.getAdapterPosition());
        });


        // Set listener for the track button
        holder.btnAdminUpdateLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String adminUpdatedLocation=holder.adminULocation.getText().toString();

                if(adminUpdatedLocation.isEmpty()){
                    Toast.makeText(context, "New Location Can Not Be Empty", Toast.LENGTH_SHORT).show();
                }else{
                    updateLocation(order.getOrder_id(),adminUpdatedLocation,holder.adminULocation);
                }

            }
        });
    }

    private void updateLocation(String adminOrderIdText, String adminULocation,EditText locationTag) {

        firestore.collection("orders").whereEqualTo("order_id",adminOrderIdText).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                if(!queryDocumentSnapshots.isEmpty()){
                    DocumentSnapshot documentSnapshot=queryDocumentSnapshots.getDocuments().get(0);
                    if(documentSnapshot.exists()){
                        // Create a map with updated data
                        HashMap<String, Object> updatedLocationData = new HashMap<>();
                        updatedLocationData.put("location", adminULocation);

                        firestore.collection("orders").document(documentSnapshot.getId()).update(updatedLocationData).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(context, "Order Location Updated Successfully", Toast.LENGTH_SHORT).show();
                                locationTag.setText("");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Order Location Updating Failed", Toast.LENGTH_SHORT).show();

                            }
                        });

                    }else{
                        Toast.makeText(context, "Order Not Available", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(context, "Order Searching Failed", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void loadSingleOrderItems(String order_id, List<Order_Item> orderItems, com.example.d2clothings.fragments.OrderItemAdapter adapter, com.example.d2clothings.fragments.OrderAdapter.OrderTotalCallback callback) {
        if (context == null) return;

        // Query Firestore for order items
        firestore.collection("oder_item")
                .whereEqualTo("order_id", order_id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderItems.clear();

                    // Keep track of pending queries
                    final int[] pendingQueries = {queryDocumentSnapshots.size()};

                    int[] orderTotal = {0};  // Effectively final wrapper
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String qty = document.getString("qty");
                        String pid = document.getString("pid");

                        if (qty != null && pid != null) {
                            firestore.collection("products")
                                    .whereEqualTo("id", pid)
                                    .get()
                                    .addOnSuccessListener(productSnapshots -> {
                                        if (!productSnapshots.isEmpty()) {
                                            DocumentSnapshot productDoc = productSnapshots.getDocuments().get(0);
                                            String pname = productDoc.getString("name");
                                            String price = productDoc.getString("price");
                                            String url = productDoc.getString("imageUrl");

                                            if (pname != null && price != null) {
                                                int totalPrice = Integer.parseInt(qty) * Integer.parseInt(price);
                                                orderTotal[0] += totalPrice;

                                                callback.onTotalCalculated(orderTotal[0]);

                                                Order_Item orderItem = new Order_Item();
                                                orderItem.setP_name(pname);
                                                orderItem.setP_qty_price(price + " x " + qty);
                                                orderItem.setP_tot(String.valueOf(totalPrice));
                                                orderItem.setUrl(url);

                                                orderItems.add(orderItem);
                                            }
                                        }

                                        // Decrement pending queries
                                        pendingQueries[0]--;

                                        // If all queries are complete, update the adapter
                                        if (pendingQueries[0] == 0) {
                                            ((Activity) context).runOnUiThread(() -> {
                                                adapter.notifyDataSetChanged();
                                            });
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("FirestoreError", "Product Query failed", e);
                                        pendingQueries[0]--;
                                    });
                        }
// Return final order total
                    }

                })
                .addOnFailureListener(e -> Log.e("FirestoreError", "Order Items Query failed", e));
    }

    public interface OrderTotalCallback {
        void onTotalCalculated(int total);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

}



class OrderItemAdapter extends RecyclerView.Adapter<com.example.d2clothings.fragments.OrderItemAdapter.OrderItemViewHolder> {
    private final List<Order_Item> orderItems;

    static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        TextView adminSingleOrderTitle, adminSingleOrderQtyPrice, adminSingleOrderTotal;
        ImageView adminSingleOrderImg;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            adminSingleOrderTitle = itemView.findViewById(R.id.adminSingleOrderTitle);
            adminSingleOrderQtyPrice = itemView.findViewById(R.id.adminSignleOrderQtyPrice);
            adminSingleOrderTotal = itemView.findViewById(R.id.adminSingleOrderTotal);
            adminSingleOrderImg = itemView.findViewById(R.id.adminSingleOrderImg);
        }
    }

    public OrderItemAdapter(List<Order_Item> orderItems) {
        this.orderItems = orderItems;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_single_order_item, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull com.example.d2clothings.fragments.OrderItemAdapter.OrderItemViewHolder holder, int position) {
        Order_Item order_item = orderItems.get(position);
        holder.adminSingleOrderTitle.setText(order_item.getP_name());
        holder.adminSingleOrderQtyPrice.setText(order_item.getP_qty_price());
        holder.adminSingleOrderTotal.setText("Rs." + order_item.getP_tot());

        Log.i("Hello", String.valueOf(order_item.getP_name()));

        Glide.with(holder.itemView.getContext())
                .load(order_item.getUrl())
                .apply(new RequestOptions().timeout(60000))
                .into(holder.adminSingleOrderImg);

        Log.d("image",String.valueOf(order_item.getUrl()));
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }
}