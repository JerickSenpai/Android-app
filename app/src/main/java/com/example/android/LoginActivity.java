package com.example.android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.*;
import com.android.volley.toolbox.*;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText username, password;
    private Button loginButton;
    private TextView forgotPassword;
    private static final String TAG = "LoginActivity";

    // Updated to use ngrok URL
    private static final String LOGIN_URL = "https://abcd-1234.ngrok-free.app/api/login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        forgotPassword = findViewById(R.id.forgotPassword);

        loginButton.setOnClickListener(view -> validateLogin());

        forgotPassword.setOnClickListener(view ->
                Toast.makeText(LoginActivity.this, "Please contact the librarian", Toast.LENGTH_SHORT).show());
    }

    private void validateLogin() {
        String inputUser = username.getText().toString().trim();
        String inputPass = password.getText().toString().trim();

        if (inputUser.isEmpty() || inputPass.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");

        StringRequest request = new StringRequest(Request.Method.POST, LOGIN_URL,
                response -> {
                    // Reset button state
                    loginButton.setEnabled(true);
                    loginButton.setText("Login");

                    Log.d(TAG, "Server response: " + response);

                    // More robust response handling
                    if (response != null && response.trim().equalsIgnoreCase("success")) {
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Invalid username or password. Server response: " + response, Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    // Reset button state
                    loginButton.setEnabled(true);
                    loginButton.setText("Login");

                    Log.e(TAG, "Volley error: ", error);

                    String errorMessage = "Connection error: ";
                    if (error instanceof NetworkError) {
                        errorMessage += "Network problem. Check your internet connection.";
                    } else if (error instanceof ServerError) {
                        errorMessage += "Server error. Please try again later.";
                    } else if (error instanceof TimeoutError) {
                        errorMessage += "Request timeout. Please try again.";
                    } else if (error instanceof NoConnectionError) {
                        errorMessage += "No internet connection.";
                    } else {
                        errorMessage += error.getMessage();
                    }

                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", inputUser);
                params.put("password", inputPass);
                Log.d(TAG, "Sending params: username=" + inputUser);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                // Add ngrok-skip-browser-warning header to avoid ngrok browser warning
                headers.put("ngrok-skip-browser-warning", "true");
                return headers;
            }
        };

        // Set timeout values - increased for ngrok tunneling
        request.setRetryPolicy(new DefaultRetryPolicy(
                15000,  // 15 seconds timeout (increased for ngrok latency)
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Volley.newRequestQueue(this).add(request);
    }
}