package com.example.android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText username, password;
    private Button loginButton;
    private TextView forgotPassword;
    private static final String TAG = "LoginActivity";

    private static final String LOGIN_URL ="https://8c36-120-29-110-79.ngrok-free.app/library_system/api/student/login.php";

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
        final String inputUser = username.getText().toString().trim();
        final String inputPass = password.getText().toString().trim();

        if (inputUser.isEmpty() || inputPass.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", inputUser);
            jsonBody.put("password", inputPass);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to create login request", Toast.LENGTH_SHORT).show();
            loginButton.setEnabled(true);
            loginButton.setText("Login");
            return;
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.POST,
                LOGIN_URL,
                jsonBody,
                response -> {
                    loginButton.setEnabled(true);
                    loginButton.setText("Login");
                    Log.d(TAG, "Server response: " + response);

                    try {
                        String status = response.getString("status");
                        if (status.equalsIgnoreCase("success")) {
                            String studentId = response.getString("student_id");
                            SharedPrefManager.getInstance(this).saveStudentId(studentId);

                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            String message = response.optString("message", "Invalid username or password.");
                            Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Invalid response from server", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "JSON parsing error", e);
                    }
                },
                error -> {
                    loginButton.setEnabled(true);
                    loginButton.setText("Login");

                    if (error instanceof ParseError) {
                        Log.e(TAG, "JSON parse error: " + error.getMessage());
                    } else {
                        Log.e(TAG, "Network error: " + error.toString());
                    }

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
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("ngrok-skip-browser-warning", "true");
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Volley.newRequestQueue(this).add(jsonRequest);
    }
}
