package com.example.d2clothings;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.d2clothings.fragments.AdminLoginFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class SigninActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, globalEmail;
    private Button btnSignIn, btnSignUp, btnAdminLogin, btnForgotPassword;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;
    private RequestQueue requestQueue;


    private static final String SERVLET_URL = " https://4b7f-2407-c00-6007-a6bf-7ded-3e2a-b940-ab77.ngrok-free.app/D2ClothingsFP2/ForgetPassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);  // Initialize btnAdminLogin

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Signing In...");

        // Auto Redirect if already logged in
        if (isUserLoggedIn()) {
            startActivity(new Intent(SigninActivity.this, HomeActivity.class));
            finish();
        }

        btnSignIn.setOnClickListener(v -> loginUser());

        // Redirect to SignupActivity when "Sign Up Here" button is clicked
        btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(SigninActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        // Open AdminLoginFragment when Admin Login button is clicked
        btnAdminLogin = findViewById(R.id.btnAdminLogin);

        btnAdminLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdminLoginFragment adminLoginFragment = new AdminLoginFragment();
                adminLoginFragment.show(getSupportFragmentManager(), "adminLoginDialog");
            }
        });

        requestQueue = Volley.newRequestQueue(this);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        btnForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                globalEmail = findViewById(R.id.etEmail);

                String globalemail = globalEmail.getText().toString();



                if (globalemail.isEmpty()) {
                    Toast.makeText(SigninActivity.this, "Email Can Not Be Empty", Toast.LENGTH_SHORT).show();
                } else {
                    fetchUniqueValue(globalemail);
                }
            }
        });



        ImageView bgGif = findViewById(R.id.bgGif);
        Glide.with(this).asGif().load(R.drawable.clothing_bg).into(bgGif);
    }


    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        db.collection("users").document(email).get().addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String storedPassword = document.getString("password");

                    if (storedPassword != null && storedPassword.equals(password)) {
                        saveUserEmail(email); // Save email locally
                        Toast.makeText(SigninActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SigninActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(SigninActivity.this, "Incorrect Password!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SigninActivity.this, "User not found!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SigninActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Save email in SharedPreferences
    private void saveUserEmail(String email) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userEmail", email);
        editor.apply();
    }

    // Check if user email is saved (already logged in)
    private boolean isUserLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.contains("userEmail");
    }

    private void fetchUniqueValue(String email) {
        // Create POST request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, SERVLET_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        updateNewPassword(response, email);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("errorrrr", "Error sending email: " + error.getMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                return params;
            }
        };

        requestQueue.add(stringRequest);

    }

    private void updateNewPassword(String response, String email) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            HashMap<String, Object> updatedUserData = new HashMap<>();
                            updatedUserData.put("password", response);

                            db.collection("users").document(documentSnapshot.getId()).update(updatedUserData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(SigninActivity.this, "Password Updated", Toast.LENGTH_SHORT).show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(SigninActivity.this, "Password Updating Failed", Toast.LENGTH_SHORT).show();

                                }
                            });
                        }else{
                            Toast.makeText(SigninActivity.this, "User Not Found", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SigninActivity.this, "User Finding Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
