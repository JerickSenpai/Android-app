package com.example.android;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QRGeneratorActivity extends AppCompatActivity {

    private static final String TAG = "QRGeneratorActivity";

    private ImageView ivQRCode;
    private ProgressBar progressBar;
    private View placeholderLayout;

    private ExecutorService executor;
    private Bitmap currentQRBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_qr_generator);
            executor = Executors.newSingleThreadExecutor();

            initializeViews();

            // Check if views were initialized successfully
            if (ivQRCode == null || progressBar == null) {
                Log.e(TAG, "Critical views not found in layout");
                showError("Layout error: Required views missing");
                finish();
                return;
            }

            // Use SharedPrefManager to get student ID
            String studentIdStr = getLoggedInStudentId();
            Log.d(TAG, "Retrieved student ID: " + studentIdStr);

            if (studentIdStr != null && !studentIdStr.isEmpty()) {
                try {
                    int studentId = Integer.parseInt(studentIdStr);
                    generateQRCode(studentId);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Invalid student ID format: " + studentIdStr);
                    showError("Invalid student ID format");
                    finish();
                }
            } else {
                showError("Student ID not found. Please login again.");
                finish();
            }

        } catch (Exception e) {
            Log.e(TAG, "Critical error in onCreate", e);
            Toast.makeText(this, "Failed to initialize QR Generator: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeViews() {
        try {
            // Initialize toolbar
            Toolbar toolbar = findViewById(R.id.toolbarQRGenerator);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("Generate QR Code");
                }
            } else {
                Log.w(TAG, "Toolbar not found in layout");
            }

            // Initialize views with null checks
            ivQRCode = findViewById(R.id.ivQRCode);
            progressBar = findViewById(R.id.progressBar);
            placeholderLayout = findViewById(R.id.placeholderLayout);

            // Log which views were found
            Log.d(TAG, "ivQRCode found: " + (ivQRCode != null));
            Log.d(TAG, "progressBar found: " + (progressBar != null));
            Log.d(TAG, "placeholderLayout found: " + (placeholderLayout != null));

            // Set initial visibility
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }

            if (placeholderLayout != null) {
                placeholderLayout.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            throw e; // Re-throw to be caught in onCreate
        }
    }

    private String getLoggedInStudentId() {
        try {
            // Use SharedPrefManager for consistent access
            SharedPrefManager prefManager = SharedPrefManager.getInstance(this);
            String studentId = prefManager.getStudentId();

            Log.d(TAG, "Retrieved student ID from SharedPrefManager: " + studentId);
            return studentId;

        } catch (Exception e) {
            Log.e(TAG, "Error retrieving student ID from SharedPrefManager", e);
            return null;
        }
    }

    private void generateQRCode(int studentId) {
        if (studentId <= 0) {
            showError("Invalid student ID: " + studentId);
            return;
        }

        Log.d(TAG, "Generating QR code for student ID: " + studentId);
        showLoading(true);

        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }

        executor.execute(() -> {
            try {
                Log.d(TAG, "Starting QR code generation...");
                Bitmap qrBitmap = QRCodeGenerator.generateStudentQRCode(studentId, 400);

                runOnUiThread(() -> {
                    try {
                        if (qrBitmap != null && !qrBitmap.isRecycled()) {
                            Log.d(TAG, "QR code generated successfully");
                            displayQRCode(qrBitmap);
                            currentQRBitmap = qrBitmap;
                        } else {
                            Log.e(TAG, "QR code generation returned null or recycled bitmap");
                            showError("Failed to generate QR code");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in UI thread while displaying QR code", e);
                        showError("Error displaying QR code: " + e.getMessage());
                    } finally {
                        showLoading(false);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error generating QR code for student ID: " + studentId, e);
                runOnUiThread(() -> {
                    showError("Failed to generate QR code: " + e.getMessage());
                    showLoading(false);
                });
            }
        });
    }

    private void showLoading(boolean show) {
        try {
            if (progressBar != null) {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                Log.d(TAG, "Loading indicator " + (show ? "shown" : "hidden"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing/hiding loading indicator", e);
        }
    }

    private void displayQRCode(Bitmap bitmap) {
        try {
            if (ivQRCode != null && bitmap != null && !bitmap.isRecycled()) {
                ivQRCode.setImageBitmap(bitmap);
                ivQRCode.setVisibility(View.VISIBLE);

                if (placeholderLayout != null) {
                    placeholderLayout.setVisibility(View.GONE);
                }

                Log.d(TAG, "QR code displayed successfully");
            } else {
                Log.e(TAG, "Cannot display QR code - invalid bitmap or ImageView");
                showError("Cannot display QR code");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying QR code", e);
            showError("Error displaying QR code: " + e.getMessage());
        }
    }

    private void showError(String message) {
        try {
            if (this != null && !isFinishing()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
            Log.e(TAG, message);
        } catch (Exception e) {
            Log.e(TAG, "Error showing error message: " + message, e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            if (executor != null && !executor.isShutdown()) {
                executor.shutdown();
            }

            if (currentQRBitmap != null && !currentQRBitmap.isRecycled()) {
                currentQRBitmap.recycle();
                currentQRBitmap = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy", e);
        }
    }
}