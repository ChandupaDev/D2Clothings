package com.example.d2clothings.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.d2clothings.R;
import com.example.d2clothings.databinding.FragmentProductDeleteBinding;
import com.example.d2clothings.util.FirestoreHelper;
import com.example.d2clothings.interfaces.FirestoreOperationCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProductDeleteFragment extends Fragment {
    private FragmentProductDeleteBinding binding;

    private FirebaseFirestore firestore;
    EditText dId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProductDeleteBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firestore=FirebaseFirestore.getInstance();

        Button deleteProduct = binding.btnDeleteProduct;
        deleteProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dId = binding.etProductId;

                String did = dId.getText().toString();

                if (did.isEmpty()) {
                    Toast.makeText(getContext(), "Product Id Can Not Be Empty", Toast.LENGTH_SHORT).show();
                } else {
                    deleteProduct(did);
                }
            }
        });



        return root;
}

    private void deleteProduct(String did) {
        Log.i("did", did);
        firestore.collection("products")
                .whereEqualTo("id", did) // Find document by pid
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            document.getReference().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getContext(), "Product Deleted Successfully", Toast.LENGTH_SHORT).show();
                                    dId.setText("");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), "Product Deleting Failed", Toast.LENGTH_SHORT).show();

                                }
                            });
                        }
                    } else {
                        Toast.makeText(getContext(), "Product Not Found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error Finding Product", Toast.LENGTH_SHORT).show()
                );
    }


}

