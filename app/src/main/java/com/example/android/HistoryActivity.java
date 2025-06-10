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

    private static final String BASE_URL = "https://619e-120-29-110-79.ngrok-free.app/library_system";

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
                response -> {
                    try {
                        parseHistoryData(response);
                    } catch (Exception e) {
                        Toast.makeText(this, "Error parsing history: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    String errorMessage = "Failed to load history. ";
                    if (error.networkResponse != null) {
                        errorMessage += "Error code: " + error.networkResponse.statusCode + ". ";
                    }
                    if (error instanceof com.android.volley.TimeoutError) {
                        errorMessage += "Request timed out. Please try again.";
                    } else if (error instanceof com.android.volley.NoConnectionError) {
                        errorMessage += "No internet connection.";
                    } else if (error instanceof com.android.volley.AuthFailureError) {
                        errorMessage += "Authentication failed.";
                    } else if (error instanceof com.android.volley.ServerError) {
                        errorMessage += "Server error. Please try again later.";
                    } else if (error instanceof com.android.volley.NetworkError) {
                        errorMessage += "Network error. Please check your connection.";
                    } else if (error instanceof com.android.volley.ParseError) {
                        errorMessage += "Response parsing error.";
                    } else {
                        errorMessage += error.getMessage();
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                }
        );

        request.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                15000,
                com.android.volley.DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        try {
            com.android.volley.RequestQueue queue = com.android.volley.toolbox.Volley.newRequestQueue(this);
            queue.add(request);
        } catch (Exception e) {
            Toast.makeText(this, "Unexpected error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void parseHistoryData(JSONObject response) {
        try {
            transactionList.clear();
            if (response == null) {
                Toast.makeText(this, "No response from server.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (response.has("status") && response.getString("status").equals("success")) {
                if (!response.has("borrow_history")) {
                    Toast.makeText(this, "No transaction history found.", Toast.LENGTH_SHORT).show();
                    return;
                }
                JSONArray history = response.getJSONArray("borrow_history");
                for (int i = 0; i < history.length(); i++) {
                    JSONObject obj = history.getJSONObject(i);
                    String title = obj.optString("title", "Unknown Book");
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
                    Toast.makeText(this, "No transaction history found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                String message = response.has("message") ? response.optString("message") : "No transaction history found.";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error parsing history: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
