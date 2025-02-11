package com.example.d2clothings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private TextView emailText;
    private EditText fullName, phone, address, city;
    private ImageView profileImage;
    private Button editProfileBtn;
    private SharedPreferences sharedPreferences;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

        emailText = findViewById(R.id.emailText);
        fullName = findViewById(R.id.fullName);
        phone = findViewById(R.id.phone);
        address = findViewById(R.id.address);
        city = findViewById(R.id.city);
        profileImage = findViewById(R.id.profileImage);
        editProfileBtn = findViewById(R.id.editProfileBtn);

        // Retrieve the logged-in user email from SharedPreferences
        userEmail = sharedPreferences.getString("userEmail", null);

        if (userEmail != null) {
            loadUserProfile();
        } else {
            Toast.makeText(this, "No logged-in user found! Redirecting to Sign In...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ProfileActivity.this, SigninActivity.class));
            finish();
        }

        editProfileBtn.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            intent.putExtra("email", userEmail);
            startActivity(intent);
        });
    }

    private void loadUserProfile() {
        db.collection("users").document(userEmail)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        emailText.setText(document.getString("email"));
                        fullName.setText(document.getString("fullName"));
                        phone.setText(document.getString("phone"));
                        address.setText(document.getString("address"));
                        city.setText(document.getString("city"));

                        // Load Profile Image if exists
                        String profileUrl = document.getString("profileImage");
                        if (profileUrl != null && !profileUrl.isEmpty()) {
                            Glide.with(this).load(profileUrl).into(profileImage);
                        }
                    } else {
                        Toast.makeText(this, "User profile not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error loading user profile", e));
    }
}
