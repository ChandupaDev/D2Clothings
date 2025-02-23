package com.example.d2clothings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;



public class MapActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String order_id = getIntent().getStringExtra("order_id");

        firestore = FirebaseFirestore.getInstance();


        SupportMapFragment supportMapFragment = new SupportMapFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.mapFrameLayout, supportMapFragment);
        fragmentTransaction.commit();


        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                loadLocation(order_id, location -> {

                    // Split by comma
                    String[] parts = location.split(",");

                    // Convert to double
                    double latitude = Double.parseDouble(parts[0].trim());
                    double longitude = Double.parseDouble(parts[1].trim());

                    LatLng latLng = new LatLng(latitude, longitude);

                    googleMap.animateCamera(
                            CameraUpdateFactory.newCameraPosition(
                                    new CameraPosition.Builder().
                                            target(latLng).
                                            zoom(18).
                                            build()
                            )
                    );

                    googleMap.addMarker(
                            new MarkerOptions()
                                    .position(latLng)
                                    .title("Your Package")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.parcels))
                    );
                });
            }
        });
    }

    private void loadLocation(String orderId, OnLocationLoadedListener listener) {
        firestore.collection("orders")
                .whereEqualTo("order_id", orderId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    String location = "";
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot firstDocument = queryDocumentSnapshots.getDocuments().get(0);
                        location = firstDocument.getString("location");
                    }
                    listener.onLocationLoaded(location);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Query failed", e);
                    listener.onLocationLoaded("");
                });
    }

    // Interface for the callback
    interface OnLocationLoadedListener {
        void onLocationLoaded(String location);
    }
}