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
    private static final String BASE_URL = "https://8c36-120-29-110-79.ngrok-free.app/library_system/api/student/get_attendance.php";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public interface AttendanceCallback {
        void onSuccess(List<AttendanceModel> attendanceList);
        void onError(String error);
    }

    public interface AttendanceActionCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public static void fetchAttendanceRecords(String studentId, AttendanceCallback callback) {
        executor.execute(() -> {
            String urlString = BASE_URL;
            if (studentId != null && !studentId.isEmpty()) {
                urlString += "?student_id=" + studentId;
            }

            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

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
                JSONObject jsonResponse = new JSONObject(result);
                String status = jsonResponse.optString("status", "error");

                if (status.equals("success")) {
                    JSONArray logsArray = jsonResponse.getJSONArray("attendance_logs");
                    List<AttendanceModel> logs = new ArrayList<>();

                    for (int i = 0; i < logsArray.length(); i++) {
                        JSONObject item = logsArray.getJSONObject(i);

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
                    callback.onError(jsonResponse.optString("message", "Unknown error"));
                }
            } catch (JSONException e) {
                Log.e("ParseError", "JSON parsing error", e);
                callback.onError("Response parsing error");
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