package com.example.android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class DashboardActivity extends AppCompatActivity {

    Button btnDue, btnHistory, btnScanQR, btnAttendance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Set up custom toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("STI");
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        // Initialize buttons
        btnDue = findViewById(R.id.btnDue);
        btnHistory = findViewById(R.id.btnHistory);
        btnScanQR = findViewById(R.id.btnScanQR);
        btnAttendance = findViewById(R.id.btnAttendance);

        // Apply modern touch animation
        applyTouchEffect(btnDue);
        applyTouchEffect(btnHistory);
        applyTouchEffect(btnScanQR);
        applyTouchEffect(btnAttendance);

        // Set up navigation
        btnDue.setOnClickListener(view -> {
            startActivity(new Intent(DashboardActivity.this, DuesActivity.class));
        });

        btnHistory.setOnClickListener(view -> {
            startActivity(new Intent(DashboardActivity.this, HistoryActivity.class));
        });

        btnScanQR.setOnClickListener(view -> {
            startActivity(new Intent(DashboardActivity.this, BorrowQR.class));
        });

        btnAttendance.setOnClickListener(view -> {
            startActivity(new Intent(DashboardActivity.this, AttendanceRecordActivity.class));
        });
    }

    // Modern touch effect on buttons
    @SuppressLint("ClickableViewAccessibility")
    private void applyTouchEffect(View button) {
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setScaleX(0.96f);
                    v.setScaleY(0.96f);
                    v.setElevation(2f);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.setScaleX(1f);
                    v.setScaleY(1f);
                    v.setElevation(8f);
                    break;
            }
            return false;
        });
    }

    // Inflate logout menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // Handle logout action
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        } else if (id == R.id.action_logout) {
            // Perform logout - go to login screen and clear activity stack
            Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
