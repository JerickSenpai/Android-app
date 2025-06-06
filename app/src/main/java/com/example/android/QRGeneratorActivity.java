package com.example.android;

import android.content.Intent;
import android.content.SharedPreferences;
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
        setContentView(R.layout.activity_qr_generator);

        executor = Executors.newSingleThreadExecutor();

        initializeViews();

        // Automatically fetch student ID from stored session
        int studentId = getLoggedInStudentId();
        if (studentId > 0) {
            generateQRCode(studentId);
        } else {
            showError("Student ID not found. Please log in again.");
        }
    }

    private void initializeViews() {
        try {
            Toolbar toolbar = findViewById(R.id.toolbarQRGenerator);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("Generate QR Code");
                }
            }

            ivQRCode = findViewById(R.id.ivQRCode);
            progressBar = findViewById(R.id.progressBar);
            placeholderLayout = findViewById(R.id.placeholderLayout);

            if (ivQRCode == null || progressBar == null) {
                Log.e(TAG, "Required views not found in layout");
                Toast.makeText(this, "Layout error: Missing views", Toast.LENGTH_LONG).show();
                finish();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            Toast.makeText(this, "Error setting up the screen", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private int getLoggedInStudentId() {
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        return prefs.getInt("student_id", -1); // Replace with your real session key
    }

    private void generateQRCode(int studentId) {
        if (studentId <= 0) {
            Toast.makeText(this, "Invalid student ID", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        executor.execute(() -> {
            try {
                Bitmap qrBitmap = QRCodeGenerator.generateStudentQRCode(studentId, 400);
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
    }

    private void displayQRCode(Bitmap bitmap) {
        if (ivQRCode != null && bitmap != null) {
            ivQRCode.setImageBitmap(bitmap);
            ivQRCode.setVisibility(View.VISIBLE);

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

        if (currentQRBitmap != null && !currentQRBitmap.isRecycled()) {
            currentQRBitmap.recycle();
            currentQRBitmap = null;
        }
    }
}
