package com.example.android;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class DuesActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<String> duesList;
    ArrayAdapter<String> adapter;
    String URL = "http://192.168.x.x/myapi/dues.php"; // Replace with your IP/path
    private boolean useTestData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dues);

        Toolbar toolbar = findViewById(R.id.toolbarDues);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = findViewById(R.id.duesListView);
        duesList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, duesList);
        listView.setAdapter(adapter);

        // Check if we should use test data
        if (getIntent().getBooleanExtra("use_test_data", false)) {
            useTestData = true;
            getSupportActionBar().setTitle("Dues (Test Data)");
            loadPlaceholderDues();
        } else {
            fetchDuesFromServer();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add menu item to toggle test data
        menu.add(0, 1, 0, useTestData ? "Use Real Data" : "Use Test Data");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == 1) {
            // Toggle between test and real data
            useTestData = !useTestData;
            invalidateOptionsMenu(); // Refresh menu

            if (useTestData) {
                getSupportActionBar().setTitle("Dues (Test Data)");
                loadPlaceholderDues();
            } else {
                getSupportActionBar().setTitle("Dues");
                fetchDuesFromServer();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchDuesFromServer() {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray duesArray = response.getJSONArray("dues");
                            duesList.clear();
                            for (int i = 0; i < duesArray.length(); i++) {
                                duesList.add(duesArray.getString(i));
                            }
                            adapter.notifyDataSetChanged();

                            if (duesList.isEmpty()) {
                                Toast.makeText(this, "No dues found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Failed to load dues. Loading test data instead.", Toast.LENGTH_SHORT).show();
                            loadPlaceholderDues();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Parsing error. Loading test data instead.", Toast.LENGTH_SHORT).show();
                        loadPlaceholderDues();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Network error. Loading test data instead.", Toast.LENGTH_SHORT).show();
                    loadPlaceholderDues();
                });

        queue.add(request);
    }

    /**
     * Load placeholder dues data for testing
     */
    private void loadPlaceholderDues() {
        duesList.clear();
        duesList.addAll(PlaceholderDataGenerator.generateSampleDues(5));
        adapter.notifyDataSetChanged();

        Toast.makeText(this, "Loaded " + duesList.size() + " sample dues", Toast.LENGTH_SHORT).show();
    }
}