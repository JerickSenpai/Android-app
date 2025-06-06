package com.example.android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QRGeneratorActivity extends AppCompatActivity {

    private static final String TAG = "QRGeneratorActivity";

    private EditText etStudentId;
    private Button btnGenerateQR;
    private ImageView ivQRCode;
    private ProgressBar progressBar;
    private View placeholderLayout;

    private ExecutorService executor;
    private Bitmap currentQRBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_generator);

        executor = Executors.newSingleThreadExecutor();

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        try {
            // Toolbar setup
            Toolbar toolbar = findViewById(R.id.toolbarQRGenerator);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("Generate QR Code");
                }
            }

            // Initialize views with null checks
            etStudentId = findViewById(R.id.etStudentId);
            btnGenerateQR = findViewById(R.id.btnGenerateQR);
            ivQRCode = findViewById(R.id.ivQRCode);
            progressBar = findViewById(R.id.progressBar);
            placeholderLayout = findViewById(R.id.placeholderLayout);

            // Verify all required views are found
            if (etStudentId == null || btnGenerateQR == null || ivQRCode == null || progressBar == null) {
                Log.e(TAG, "One or more required views not found in layout");
                Toast.makeText(this, "Layout error: Missing required views", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            // Check if student ID was passed from intent
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra("student_id")) {
                String studentIdStr = intent.getStringExtra("student_id");
                if (!TextUtils.isEmpty(studentIdStr)) {
                    etStudentId.setText(studentIdStr);
                    try {
                        int studentId = Integer.parseInt(studentIdStr);
                        if (studentId > 0) {
                            generateQRCode(studentId);
                        }
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "Invalid student ID format from intent: " + studentIdStr);
                        Toast.makeText(this, "Invalid student ID format", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            Toast.makeText(this, "Error setting up the screen", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupListeners() {
        if (btnGenerateQR != null) {
            btnGenerateQR.setOnClickListener(v -> {
                String studentIdStr = etStudentId.getText().toString().trim();
                if (!TextUtils.isEmpty(studentIdStr)) {
                    try {
                        int studentId = Integer.parseInt(studentIdStr);
                        if (studentId <= 0) {
                            Toast.makeText(this, "Student ID must be a positive number", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        generateQRCode(studentId);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Please enter a valid numeric ID", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Student ID cannot be empty", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void generateQRCode(int studentId) {
        if (studentId <= 0) {
            Toast.makeText(this, "Invalid student ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        showLoading(true);

        // Use background thread for QR generation
        executor.execute(() -> {
            try {
                // Use your custom QR generator instead of JourneyApps
                Bitmap qrBitmap = QRCodeGenerator.generateStudentQRCode(studentId, 400);

                // Update UI on main thread
                runOnUiThread(() -> {
                    if (qrBitmap != null) {
                        displayQRCode(qrBitmap);
                        currentQRBitmap = qrBitmap;
                    } else {
                        showError("Failed to generate QR code");
                    }
                    showLoading(false);
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
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (btnGenerateQR != null) {
            btnGenerateQR.setEnabled(!show);
        }
    }

    private void displayQRCode(Bitmap bitmap) {
        if (ivQRCode != null && bitmap != null) {
            ivQRCode.setImageBitmap(bitmap);
            ivQRCode.setVisibility(View.VISIBLE);

            // Hide placeholder if it exists
            if (placeholderLayout != null) {
                placeholderLayout.setVisibility(View.GONE);
            }
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.e(TAG, message);
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
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }

        // Clean up bitmap to prevent memory leaks
        if (currentQRBitmap != null && !currentQRBitmap.isRecycled()) {
            currentQRBitmap.recycle();
            currentQRBitmap = null;
        }
    }
}