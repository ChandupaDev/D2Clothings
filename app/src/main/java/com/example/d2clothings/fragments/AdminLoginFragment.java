package com.example.d2clothings.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.d2clothings.R;
import com.example.d2clothings.AdminHomeActivity; // Add this import
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminLoginFragment extends DialogFragment {

    private TextInputEditText etAdminEmail, etAdminPassword;
    private MaterialButton btnAdminSignIn;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        etAdminEmail = view.findViewById(R.id.etAdminEmail);
        etAdminPassword = view.findViewById(R.id.etAdminPassword);
        btnAdminSignIn = view.findViewById(R.id.btnAdminSignIn);

        btnAdminSignIn.setOnClickListener(v -> validateAndLogin());
    }

    private void validateAndLogin() {
        String email = etAdminEmail.getText().toString().trim();
        String password = etAdminPassword.getText().toString().trim();

        // Input validation
        if (TextUtils.isEmpty(email)) {
            etAdminEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etAdminPassword.setError("Password is required");
            return;
        }

        // Show loading state
        btnAdminSignIn.setEnabled(false);
        btnAdminSignIn.setText("Signing In...");

        // Query Firestore for any admin with matching credentials
        db.collection("admin")
                .whereEqualTo("email", email)
                .whereEqualTo("password", password)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // Get the first matching admin document
                            DocumentSnapshot adminDoc = task.getResult().getDocuments().get(0);

                            // Login successful
                            Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();

                            // Save admin session info
                            saveAdminSession(adminDoc);

                            // Redirect to AdminHomeActivity
                            redirectToAdminHome();

                            dismiss();  // Close dialog
                        } else {
                            // No matching admin found
                            etAdminPassword.setError("Invalid credentials");
                            Toast.makeText(getContext(), "Invalid email or password", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Error occurred
                        Toast.makeText(getContext(), "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }

                    // Reset button state
                    btnAdminSignIn.setEnabled(true);
                    btnAdminSignIn.setText("Sign In");
                });
    }

    private void saveAdminSession(DocumentSnapshot adminData) {
        // Save admin details to SharedPreferences
        String fname = adminData.getString("fname");
        String lname = adminData.getString("lname");
        String email = adminData.getString("email");
        String docId = adminData.getId();

        if (getActivity() != null) {
            getActivity().getSharedPreferences("AdminSession", 0)
                    .edit()
                    .putBoolean("isLoggedIn", true)
                    .putString("adminEmail", email)
                    .putString("adminName", fname + " " + lname)
                    .putString("adminId", docId)
                    .apply();
        }
    }

    private void redirectToAdminHome() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), AdminHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
            startActivity(intent);
            getActivity().finish(); // Close current activity
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}