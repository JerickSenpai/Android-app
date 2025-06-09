package com.example.android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class AttendanceRecordActivity extends AppCompatActivity {

    private static final String TAG = "AttendanceRecord";
    private static final int QR_SCAN_REQUEST_CODE = 101;

    private RecyclerView recyclerViewAttendance;
    private AttendanceAdapter attendanceAdapter;
    private List<AttendanceModel> attendanceList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private LinearLayout tvNoData;

    // QR Footer components
    private Button btnGenerateQR;
    private Button btnScanQR;

    private static final Random random = new Random();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_record);

        initializeViews();
        setupRecyclerView();
        setupQRFooter();
        loadAttendanceData(); // Default load from shared preferences
    }

    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbarAttendance);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Attendance Records");
        }

        recyclerViewAttendance = findViewById(R.id.recyclerViewAttendance);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        tvNoData = findViewById(R.id.tvNoData);

        btnGenerateQR = findViewById(R.id.btnGenerateQR);
        btnScanQR = findViewById(R.id.btnScanQR);

        swipeRefreshLayout.setOnRefreshListener(this::loadAttendanceData);
    }

    private void setupRecyclerView() {
        recyclerViewAttendance.setLayoutManager(new LinearLayoutManager(this));
        attendanceList = new ArrayList<>();
        attendanceAdapter = new AttendanceAdapter(attendanceList, this);
        recyclerViewAttendance.setAdapter(attendanceAdapter);
    }

    private void setupQRFooter() {
        btnGenerateQR.setOnClickListener(v -> {
            Intent intent = new Intent(AttendanceRecordActivity.this, QRGeneratorActivity.class);
            startActivity(intent);
        });

        btnScanQR.setOnClickListener(v -> {
            // TODO: Implement QR Scanner Activity
            // Intent intent = new Intent(AttendanceRecordActivity.this, QRScannerActivity.class);
            // startActivity(intent);
            Toast.makeText(AttendanceRecordActivity.this,
                    "QR Scanner feature coming soon!",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void loadAttendanceData() {
        SharedPrefManager prefManager = SharedPrefManager.getInstance(this);
        String studentIdStr = prefManager.getStudentId();

        if (studentIdStr == null || studentIdStr.isEmpty()) {
            showLoading(false);
            showNoDataMessage(true);
            Log.w(TAG, "Student not logged in yet. Skipping API call.");
            Toast.makeText(this, "Please log in first to view attendance.", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            int studentId = Integer.parseInt(studentIdStr);
            loadAttendanceData(studentIdStr);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid student ID format in SharedPreferences: " + studentIdStr);
            Toast.makeText(this, "Stored student ID is invalid. Please re-login.", Toast.LENGTH_LONG).show();
            showLoading(false);
            showNoDataMessage(true);
        }
    }

    private void loadAttendanceData(String studentId) {
        showLoading(true);

        AttendanceApiService.fetchAttendanceRecords(studentId, new AttendanceApiService.AttendanceCallback() {
            @Override
            public void onSuccess(List<AttendanceModel> attendanceListResult) {
                showLoading(false);
                attendanceList.clear();
                if (attendanceListResult != null && !attendanceListResult.isEmpty()) {
                    attendanceList.addAll(attendanceListResult);
                    attendanceAdapter.notifyDataSetChanged();
                    showNoDataMessage(false);
                } else {
                    showNoDataMessage(true);
                }
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                Log.e(TAG, "Error fetching attendance: " + errorMessage);
                showNoDataMessage(true);
            }
        });
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void showNoDataMessage(boolean show) {
        if (tvNoData != null) {
            tvNoData.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (recyclerViewAttendance != null) {
            recyclerViewAttendance.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Back arrow
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == QR_SCAN_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String scannedStudentId = data.getStringExtra("scanned_student_id");
            if (scannedStudentId != null && !scannedStudentId.isEmpty()) {
                loadAttendanceData(scannedStudentId); // Pass as String
            } else {
                Toast.makeText(this, "Invalid scanned ID format.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
