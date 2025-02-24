package com.example.d2clothings.fragments;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.d2clothings.R;
import com.example.d2clothings.databinding.FragmentProductManagementBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Objects;

public class ProductManagementFragment extends Fragment {
    private FragmentProductManagementBinding binding;

    private FirebaseFirestore firestore;

    EditText pTitle, pDescription, pPrice, pQty, pUrl ,pId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentProductManagementBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Button addProduct=binding.btnAddProduct;
        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pTitle = binding.etProductName2;
                pDescription = binding.etProductDescription;
                pPrice = binding.etProductPrice;
                pQty =binding.etProductQty;
                pUrl = binding.etProductImageUrl;

                String title = pTitle.getText().toString();
                String description = pDescription.getText().toString();
                String price = pPrice.getText().toString();
                String qty = pQty.getText().toString();
                String url = pUrl.getText().toString();

                if (title.isEmpty()) {
                    Toast.makeText(getContext(), "Product Title Can Not Be Empty", Toast.LENGTH_SHORT).show();
                } else if (description.isEmpty()) {
                    Toast.makeText(getContext(), "Product Description Can Not Be Empty", Toast.LENGTH_SHORT).show();

                } else if (price.isEmpty()) {
                    Toast.makeText(getContext(), "Product Price Can Not Be Empty", Toast.LENGTH_SHORT).show();

                } else if (qty.isEmpty()) {
                    Toast.makeText(getContext(), "Product Quantity Can Not Be Empty", Toast.LENGTH_SHORT).show();

                } else if (url.isEmpty()) {
                    Toast.makeText(getContext(), "Product URL Can Not Be Empty", Toast.LENGTH_SHORT).show();

                } else {

                    firestore = FirebaseFirestore.getInstance();

                    HashMap <String , Object> ProductMap = new HashMap<>();

                    ProductMap.put("name",title );
                    ProductMap.put("description",description );
                    ProductMap.put("price",price );
                    ProductMap.put("qty",qty );
                    ProductMap.put("imageUrl",url );


                    saveProduct(ProductMap);


                }
            }
        });

        Button updateProduct = binding.btnUpdateProduct;
        updateProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pId = binding.etProductId;
                pTitle = binding.etProductName2;
                pDescription = binding.etProductDescription;
                pPrice = binding.etProductPrice;
                pQty = binding.etProductQty;
                pUrl = binding.etProductImageUrl;

                String title = pTitle.getText().toString();
                String uid = pId.getText().toString();
                String description = pDescription.getText().toString();
                String price = pPrice.getText().toString();
                String qty = pQty.getText().toString();
                String url = pUrl.getText().toString();

                if (uid.isEmpty()) {
                    Toast.makeText(getContext(), "Product Id Can Not Be Empty", Toast.LENGTH_SHORT).show();
                } else if (title.isEmpty()) {
                    Toast.makeText(getContext(), "Product Title Can Not Be Empty", Toast.LENGTH_SHORT).show();
                } else if (description.isEmpty()) {
                    Toast.makeText(getContext(), "Product Description Can Not Be Empty", Toast.LENGTH_SHORT).show();

                } else if (price.isEmpty()) {
                    Toast.makeText(getContext(), "Product Price Can Not Be Empty", Toast.LENGTH_SHORT).show();

                } else if (qty.isEmpty()) {
                    Toast.makeText(getContext(), "Product Quantity Can Not Be Empty", Toast.LENGTH_SHORT).show();

                } else if (url.isEmpty()) {
                    Toast.makeText(getContext(), "Product URL Can Not Be Empty", Toast.LENGTH_SHORT).show();

                } else {

                    firestore = FirebaseFirestore.getInstance();

                    HashMap <String , Object> UProductMap = new HashMap<>();
                    UProductMap.put("id",uid);
                    UProductMap.put("name",title);
                    UProductMap.put("description",description);
                    UProductMap.put("price",price);
                    UProductMap.put("qty",qty);
                    UProductMap.put("imageUrl",url);



                    updateProduct(UProductMap,uid);

                }
            }
        });

        // Inflate the layout for this fragment
        return root;
    }
    private void saveProduct(HashMap product) {
        loadProductId(new ProductIdCallback() {
            @Override
            public void onProductIdReceived(int newPid) {
                product.put("id", (String.valueOf(newPid))); // Set new PID before saving

                firestore.collection("products").add(product)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Product Added Successfully", Toast.LENGTH_SHORT).show();
                                pTitle.setText("");
                                pDescription.setText("");
                                pPrice.setText("");
                                pQty.setText("");
                                pUrl.setText("");
                            } else {
                                Toast.makeText(getContext(), "Product Adding Failed", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Product Adding Failed", Toast.LENGTH_SHORT).show()
                        );
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Failed to get new Product ID", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadProductId(ProductIdCallback callback) {
        firestore.collection("products")
                .orderBy("id", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot latestDoc = queryDocumentSnapshots.getDocuments().get(0);
                        String pid = latestDoc.getString("id");

                        int newPid = Integer.parseInt(pid) + 1;
                        callback.onProductIdReceived(newPid); // Callback function
                    } else {
                        callback.onProductIdReceived(1); // Start from 1 if no products exist
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Product ID Fetch Failed", Toast.LENGTH_SHORT).show();
                    callback.onFailure(e);
                });


    }

    private void updateProduct(HashMap product , String Upid) {

        firestore.collection("products").whereEqualTo("id",Upid).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                if(!queryDocumentSnapshots.isEmpty()){
                    DocumentSnapshot documentSnapshot=queryDocumentSnapshots.getDocuments().get(0);
                    if(documentSnapshot.exists()){

                        firestore.collection("products").document(documentSnapshot.getId()).update(product).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(getContext(), "Product Updated Successfully", Toast.LENGTH_SHORT).show();
                                pId.setText("");
                                pTitle.setText("");
                                pDescription.setText("");
                                pPrice.setText("");
                                pQty.setText("");
                                pUrl.setText("");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "Product Updating Failed", Toast.LENGTH_SHORT).show();

                            }
                        });

                    }else{
                        Toast.makeText(getContext(), "Product Not Available", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getContext(), "Product Not Available", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }



    public interface ProductIdCallback {
        void onProductIdReceived(int newPid);
        void onFailure(Exception e);
    }
}