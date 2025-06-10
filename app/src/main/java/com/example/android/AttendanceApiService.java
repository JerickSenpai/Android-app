package com.example.android;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AttendanceApiService {
    private static final String TAG = "AttendanceApiService";
    private static final String BASE_URL = "https://619e-120-29-110-79.ngrok-free.app/library_system/api/student/get_attendance.php";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public interface AttendanceCallback {
        void onSuccess(List<AttendanceModel> attendanceList);
        void onError(String error);
    }

    public interface AttendanceActionCallback {
        void onSuccess(String message);
        void onError(String errorMessage);
    }

    public static void fetchAttendanceRecords(String student_id, AttendanceCallback callback) {
        executor.execute(() -> {
            String urlString = BASE_URL;
            if (student_id != null && !student_id.isEmpty()) {
                urlString += "?student_id=" + student_id;
            }

            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                
                // Add necessary headers for ngrok
                connection.setRequestProperty("ngrok-skip-browser-warning", "true");
                connection.setRequestProperty("Accept", "application/json");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    parseAttendanceResponse(response.toString(), callback);
                } else {
                    postError(callback, "Server error: " + responseCode);
                }
            } catch (IOException e) {
                postError(callback, "Network error: " + e.getMessage());
            }
        });
    }


    public static void recordTimeIn(String studentId, AttendanceActionCallback callback) {
        recordAttendance(studentId, "entry", callback);
    }

    public static void recordTimeOut(String studentId, AttendanceActionCallback callback) {
        recordAttendance(studentId, "exit", callback);
    }

    private static void recordAttendance(String studentId, String type, AttendanceActionCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(BASE_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                JSONObject jsonPayload = new JSONObject();
                jsonPayload.put("student_id", studentId);
                jsonPayload.put("type", type);

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                writer.write(jsonPayload.toString());
                writer.flush();
                writer.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                parseAttendanceActionResponse(response.toString(), callback);
            } catch (IOException | JSONException e) {
                postError(callback, "Network error: " + e.getMessage());
            }
        });
    }


    private static void parseAttendanceResponse(String result, AttendanceCallback callback) {
        handler.post(() -> {
            try {
                Log.d(TAG, "Raw response: " + result);
                JSONObject jsonResponse = new JSONObject(result);
                
                boolean isSuccess = jsonResponse.optBoolean("success", false) || 
                                  jsonResponse.optString("status", "").equals("success");
                
                if (isSuccess) {
                    JSONArray logsArray;
                    if (jsonResponse.has("attendance_logs")) {
                        logsArray = jsonResponse.getJSONArray("attendance_logs");
                    } else if (jsonResponse.has("data")) {
                        logsArray = jsonResponse.getJSONArray("data");
                    } else {
                        callback.onError("No attendance data found in response");
                        return;
                    }

                    List<AttendanceModel> logs = new ArrayList<>();

                    for (int i = 0; i < logsArray.length(); i++) {
                        JSONObject item = logsArray.getJSONObject(i);
                        Log.d(TAG, "Processing item: " + item.toString());

                        String id = item.optString("id", "");
                        String studentId = item.optString("student_id", "");
                        String fullName = item.optString("full_name", "");
                        String program = item.optString("program", "");
                        String type = item.optString("type", "");
                        String date = item.optString("date", "");
                        String timeIn = item.optString("time_in", "");
                        String timeOut = item.optString("time_out", "");

                        AttendanceModel model = new AttendanceModel(
                                id, studentId, fullName, program, type, date, timeIn, timeOut
                        );

                        logs.add(model);
                    }

                    callback.onSuccess(logs);
                } else {
                    String errorMessage = jsonResponse.optString("message", 
                                     jsonResponse.optString("error", "Unknown error"));
                    Log.e(TAG, "API Error: " + errorMessage);
                    callback.onError(errorMessage);
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSON parsing error: " + e.getMessage());
                Log.e(TAG, "Response that caused error: " + result);
                callback.onError("Response parsing error: " + e.getMessage());
            }
        });
    }

    private static void parseAttendanceActionResponse(String result, AttendanceActionCallback callback) {
        handler.post(() -> {
            try {
                JSONObject jsonResponse = new JSONObject(result);
                String status = jsonResponse.optString("status", "error");

                if (status.equals("success")) {
                    callback.onSuccess(jsonResponse.getString("message"));
                } else {
                    callback.onError(jsonResponse.optString("message", "Unknown error"));
                }
            } catch (JSONException e) {
                Log.e("ParseError", "JSON parsing error", e);
                callback.onError("Response parsing error");
            }
        });
    }

    private static void postError(AttendanceCallback callback, String errorMessage) {
        handler.post(() -> {
            Log.e(TAG, "Error: " + errorMessage);
            callback.onError(errorMessage);
        });
    }

    private static void postError(AttendanceActionCallback callback, String errorMessage) {
        handler.post(() -> {
            Log.e(TAG, "Error: " + errorMessage);
            callback.onError(errorMessage);
        });
    }
}