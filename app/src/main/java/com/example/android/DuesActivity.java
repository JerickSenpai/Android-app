package com.example.android;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class DuesActivity extends AppCompatActivity {

    private ListView duesListView;
    private DuesAdapter adapter;
    private ArrayList<Due> duesList;

    // TODO: Replace this with your actual ngrok URL or your server IP/domain
    private static final String BASE_URL = "http://YOUR_NGROK_URL_HERE";

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
            finish(); // Go back when back button in toolbar is pressed
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadDuesData() {
        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String studentId = sharedPref.getString("student_id", "");

        if (studentId.isEmpty()) {
            Toast.makeText(this, "Student ID not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/api/get_dues.php?student_id=" + studentId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                this::parseDuesData,
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

    private void parseDuesData(JSONArray response) {
        try {
            duesList.clear();
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);

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

        } catch (Exception e) {
            Toast.makeText(this, "Error parsing dues: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
