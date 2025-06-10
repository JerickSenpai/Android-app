package com.example.android;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static final String SHARED_PREF_NAME = "my_shared_pref";
    private static final String KEY_STUDENT_ID = "student_id"; // This is the string like STU2024-0001

    private static SharedPrefManager instance;
    private final SharedPreferences prefs;

    // Private constructor to ensure singleton pattern
    private SharedPrefManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    // Singleton instance access
    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }

    // Save student_id (e.g., "STU2024-0001")
    public void saveStudentId(String studentId) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_STUDENT_ID, studentId);
        editor.apply();
    }

    // Retrieve saved student_id
    public String getStudentId() {
        return prefs.getString(KEY_STUDENT_ID, null);
    }

    // Clear all saved preferences (e.g., on logout)
    public void clear() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
}