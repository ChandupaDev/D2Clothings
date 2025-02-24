package com.example.d2clothings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.d2clothings.databinding.FragmentTrackodersBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class TrackOdersFragment extends Fragment {

    private FragmentTrackodersBinding binding;

    private FirebaseFirestore firestore;
    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private List<Order> orders;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentTrackodersBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        firestore = FirebaseFirestore.getInstance();

        // Set up RecyclerView for orders
        recyclerView = root.findViewById(R.id.orderRecyclerVIew);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        orders = new ArrayList<>();
        adapter = new OrderAdapter(firestore, getContext(), orders);
        recyclerView.setAdapter(adapter);

        // Get email from SharedPreferences
        SharedPreferences sharedPref = getActivity().getSharedPreferences("MyAppPrefs", getActivity().MODE_PRIVATE);
        String email = sharedPref.getString("userEmail", null);


        // Query user by email
        firestore.collection("users").whereEqualTo("email", email).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> documentSnapshotList = queryDocumentSnapshots.getDocuments();
                    if (!documentSnapshotList.isEmpty()) {
                        DocumentSnapshot firstDocument = documentSnapshotList.get(0);
                        String uid = firstDocument.getString("uid");
                        if (uid != null) {
                            loadOrders(uid);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("FirestoreError", "Query failed", e));


        return root;
    }

    private void loadOrders(String uid) {
        firestore.collection("orders").whereEqualTo("uid", uid).orderBy("order_id").get()
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

class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private final List<Order> orders;
    private FirebaseFirestore firestore;
    private Context context; // Add context variable


    static class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView orderIdText,orderTotal;
        private RelativeLayout expandableLayout;
        private ImageView arrowImage;
        private RecyclerView orderItemRecyclerView;

        private Button callButton, messageButton, trackButton;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdText = itemView.findViewById(R.id.orderIdText);
            expandableLayout = itemView.findViewById(R.id.orderExpandableLayout);
            arrowImage = itemView.findViewById(R.id.orderArrowImage);
            orderItemRecyclerView = itemView.findViewById(R.id.singleOrderItemRecyclerView);
            callButton = itemView.findViewById(R.id.callButton);
            messageButton = itemView.findViewById(R.id.messageButton);
            trackButton = itemView.findViewById(R.id.trackButton);
            orderTotal = itemView.findViewById(R.id.orderTotal);
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
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.orderIdText.setText("#" + order.getOrder_id());
        boolean isExpandable = order.isExpandable();

        holder.expandableLayout.setVisibility(isExpandable ? View.VISIBLE : View.GONE);
        holder.arrowImage.setImageResource(isExpandable ? R.drawable.arrow_up : R.drawable.arrow_down);

        // Create a list to hold order items for this specific order
        List<Order_Item> orderItems = new ArrayList<>();
        OrderItemAdapter orderItemAdapter = new OrderItemAdapter(orderItems);

        // Set up RecyclerView
        holder.orderItemRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.orderItemRecyclerView.setAdapter(orderItemAdapter);

        // If expanded, load the items
        if (isExpandable) {
            loadSingleOrderItems(order.getOrder_id(), orderItems, orderItemAdapter, total -> {
                Log.d("OrderTotal", "Total Order Price: " + total);
                holder.orderTotal.setText("Rs. "+String.valueOf(total));
            });
        }

        holder.itemView.setOnClickListener(view -> {
            boolean newExpandableState = !order.isExpandable();
            order.setExpandable(newExpandableState);

            // Load items when expanding
            if (newExpandableState) {
                loadSingleOrderItems(order.getOrder_id(), orderItems, orderItemAdapter, total -> {
                    Log.d("OrderTotal", "Total Order Price: " + total);
                    holder.orderTotal.setText("Rs. "+String.valueOf(total));
                });
            }

            notifyItemChanged(holder.getAdapterPosition());
        });


        // Set listener for the call button
        holder.callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = "tel:94765917189"; // Replace with the phone number
                Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(phoneNumber));
                context.startActivity(callIntent);
            }
        });

        // Set listener for the message button
        holder.messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = "smsto:94765917189"; // Replace with the phone number
                Intent messageIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse(phoneNumber));
                messageIntent.putExtra("sms_body", "Hello, this is a test message."); // Optional message body
                context.startActivity(messageIntent);
            }
        });

        // Set listener for the track button
        holder.trackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MapActivity.class);
                intent.putExtra("order_id", order.getOrder_id());
                context.startActivity(intent);
            }
        });
    }

    private void loadSingleOrderItems(String order_id, List<Order_Item> orderItems, OrderItemAdapter adapter, OrderTotalCallback callback) {
        if (context == null) return;

        Log.d("dd",order_id);

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

                        Log.d("dd",pid);

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

class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {
    private final List<Order_Item> orderItems;

    static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        TextView singleOrderTitle, singleOrderQtyPrice, singleOrderTotal;
        ImageView singleOrderImg;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            singleOrderTitle = itemView.findViewById(R.id.singleOrderTitle);
            singleOrderQtyPrice = itemView.findViewById(R.id.signleOrderQtyPrice);
            singleOrderTotal = itemView.findViewById(R.id.singleOrderTotal);
            singleOrderImg = itemView.findViewById(R.id.singleOrderImg);
        }
    }

    public OrderItemAdapter(List<Order_Item> orderItems) {
        this.orderItems = orderItems;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_order_item, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        Order_Item order_item = orderItems.get(position);
        holder.singleOrderTitle.setText(order_item.getP_name());
        holder.singleOrderQtyPrice.setText(order_item.getP_qty_price());
        holder.singleOrderTotal.setText("Rs." + order_item.getP_tot());

        Log.i("Hello", String.valueOf(order_item.getP_name()));

        Glide.with(holder.itemView.getContext())
                .load(order_item.getUrl())
                .apply(new RequestOptions().timeout(60000))
                .into(holder.singleOrderImg);
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }
}