package com.example.android;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.material.textfield.TextInputEditText;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BorrowQR extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private DecoratedBarcodeView barcodeScanner;
    private LinearLayout loadingOverlay;
    private TextView loadingText, bookTitleText, bookIdText, bookAvailabilityText, borrowDateText, expirationDateText;
    private TextInputEditText studentNameInput, studentIdInput, reasonInput;
    private ScrollView borrowSlipContainer;
    private LinearLayout scanInstructions;
    private Button submitButton, cancelButton;
    private String scannedBookId = "";
    private String autoExpirationDate = "";

    private final String API_URL = "http://192.168.100.145/library_system/api/borrow/borrow_book.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow);

        // Initialize views
        barcodeScanner = findViewById(R.id.barcode_scanner);
        loadingOverlay = findViewById(R.id.loading_overlay);
        loadingText = findViewById(R.id.loading_text);
        bookTitleText = findViewById(R.id.book_title);
        bookIdText = findViewById(R.id.book_id);
        bookAvailabilityText = findViewById(R.id.book_availability);
        borrowDateText = findViewById(R.id.borrow_date);
        expirationDateText = findViewById(R.id.expiration_date);
        studentNameInput = findViewById(R.id.student_name);
        studentIdInput = findViewById(R.id.student_id);
        reasonInput = findViewById(R.id.reason);
        borrowSlipContainer = findViewById(R.id.borrow_slip_container);
        scanInstructions = findViewById(R.id.scan_instructions);
        submitButton = findViewById(R.id.submit_button);
        cancelButton = findViewById(R.id.cancel_button);

        // Check for camera permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        } else {
            barcodeScanner.decodeContinuous(callback);
        }

        // Submit button
        submitButton.setOnClickListener(v -> {
            String studentId = studentIdInput.getText().toString().trim();
            String studentName = studentNameInput.getText().toString().trim();
            String reason = reasonInput.getText().toString().trim();

            if (studentId.isEmpty() || studentName.isEmpty()) {
                Toast.makeText(this, "Please enter all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            submitBorrowRequest(studentId, scannedBookId, reason, autoExpirationDate);
        });

        // Cancel button
        cancelButton.setOnClickListener(v -> {
            borrowSlipContainer.setVisibility(View.GONE);
            scanInstructions.setVisibility(View.VISIBLE);
            barcodeScanner.setVisibility(View.VISIBLE);
            barcodeScanner.resume();
        });

        // Back button
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
    }

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (!result.getText().equals(scannedBookId)) {
                scannedBookId = result.getText();
                barcodeScanner.pause();
                showBorrowForm(scannedBookId);
            }
        }
    };

    private void showBorrowForm(String bookId) {
        loadingOverlay.setVisibility(View.VISIBLE);
        loadingText.setText("Fetching book info...");

        // Simulate book info
        bookIdText.setText("Book ID: " + bookId);
        bookTitleText.setText("Book Title: [Scanned Book Title]");
        bookAvailabilityText.setText("Status: Available");

        // Set borrow and expiration dates
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        String today = sdf.format(calendar.getTime());
        borrowDateText.setText("Borrow Date: " + today);

        calendar.add(Calendar.DAY_OF_YEAR, 7);
        autoExpirationDate = sdf.format(calendar.getTime());
        expirationDateText.setText("Expiration Date: " + autoExpirationDate);

        // Show form
        loadingOverlay.setVisibility(View.GONE);
        borrowSlipContainer.setVisibility(View.VISIBLE);
        scanInstructions.setVisibility(View.GONE);
        barcodeScanner.setVisibility(View.GONE);
    }

    private void submitBorrowRequest(String studentId, String bookId, String reason, String expirationDate) {
        loadingOverlay.setVisibility(View.VISIBLE);
        loadingText.setText("Submitting borrow request...");

        JSONObject data = new JSONObject();
        try {
            data.put("student_id", studentId);
            data.put("book_id", bookId);
            data.put("reason", reason);
            data.put("expiration_date", expirationDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, API_URL, data,
                response -> {
                    loadingOverlay.setVisibility(View.GONE);
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(this, "Borrow request submitted!", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Error: " + response.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Invalid response from server", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(this, "Network error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
        );

        queue.add(request);
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeScanner.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeScanner.pause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            barcodeScanner.decodeContinuous(callback);
        } else {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
