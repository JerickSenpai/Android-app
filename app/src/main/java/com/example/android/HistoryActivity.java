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

public class HistoryActivity extends AppCompatActivity {

    private ListView historyListView;
    private HistoryAdapter adapter;
    private ArrayList<Transaction> transactionList;

    private static final String BASE_URL = "https://09ae-120-29-110-79.ngrok-free.app/library_system";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Toolbar toolbar = findViewById(R.id.toolbarHistory);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Transaction History");
        }

        historyListView = findViewById(R.id.historyListView);
        transactionList = new ArrayList<>();
        adapter = new HistoryAdapter(this, transactionList);
        historyListView.setAdapter(adapter);

        loadTransactionHistory();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadTransactionHistory() {
        SharedPrefManager prefManager = SharedPrefManager.getInstance(this);
        String studentId = prefManager.getStudentId();

        if (studentId == null || studentId.isEmpty()) {
            Toast.makeText(this, "Student ID not found. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/api/student/get_borrow_history.php?student_id=" + studentId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> parseHistoryData(response),
                error -> {
                    String errorMessage = "Failed to load history.";
                    if (error.networkResponse != null) {
                        errorMessage += " Error code: " + error.networkResponse.statusCode;
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void parseHistoryData(JSONObject response) {
        try {
            transactionList.clear();
            if (response.has("status") && response.getString("status").equals("success")) {
                JSONArray history = response.getJSONArray("borrow_history");
                for (int i = 0; i < history.length(); i++) {
                    JSONObject obj = history.getJSONObject(i);
                    String title = obj.optString("book_title", "Unknown Book");
                    String borrowDate = obj.optString("borrow_date", "N/A");
                    String returnDate = obj.optString("return_date", "Not returned");
                    String status = obj.optString("status", "Unknown");

                    if (returnDate.equals("null") || returnDate.isEmpty()) {
                        returnDate = status.equals("approved") ? "Currently borrowed" : "Not returned";
                    }

                    transactionList.add(new Transaction(title, borrowDate, returnDate, status));
                }
                adapter.notifyDataSetChanged();

                if (transactionList.isEmpty()) {
                    Toast.makeText(this, "No transaction history found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No transaction history found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error parsing history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
