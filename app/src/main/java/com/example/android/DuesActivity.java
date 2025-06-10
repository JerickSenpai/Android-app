package com.example.android;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DuesActivity extends AppCompatActivity {

    private ListView duesListView;
    private DuesAdapter adapter;
    private ArrayList<Due> duesList;

    private static final String BASE_URL = "https://8c9f-120-29-110-79.ngrok-free.app/library_system";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dues);

        Toolbar toolbar = findViewById(R.id.toolbarDues);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Outstanding Dues");
        }

        duesListView = findViewById(R.id.duesListView);
        duesList = new ArrayList<>();
        adapter = new DuesAdapter(this, duesList);
        duesListView.setAdapter(adapter);

        loadDuesData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadDuesData() {
        SharedPrefManager prefManager = SharedPrefManager.getInstance(this);
        String studentId = prefManager.getStudentId();

        if (studentId == null || studentId.isEmpty()) {
            Toast.makeText(this, "Student ID not found. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/api/student/get_dues.php?student_id=" + studentId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
            response -> parseDuesData(response),
            error -> {
                String message = "Failed to load dues.";
                if (error.networkResponse != null) {
                    message += " Error code: " + error.networkResponse.statusCode;
                }
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void parseDuesData(JSONObject response) {
        try {
            duesList.clear();
            if (response.has("status") && response.getString("status").equals("success")) {
                JSONArray duesArray = response.getJSONArray("dues");
                for (int i = 0; i < duesArray.length(); i++) {
                    JSONObject obj = duesArray.getJSONObject(i);
                    String bookTitle = obj.optString("book_title", "N/A");
                    String borrowDate = obj.optString("borrow_date", "N/A");
                    String dueDate = obj.optString("due_date", "N/A");
                    double fineAmount = obj.optDouble("fine_amount", 0.0);
                    int daysOverdue = obj.optInt("days_overdue", 0);
                    duesList.add(new Due(bookTitle, borrowDate, dueDate, fineAmount, daysOverdue));
                }
                adapter.notifyDataSetChanged();
                if (duesList.isEmpty()) {
                    Toast.makeText(this, "No outstanding dues found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No outstanding dues found.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error parsing dues: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}