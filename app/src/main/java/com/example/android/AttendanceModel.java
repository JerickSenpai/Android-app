package com.example.android;

public class AttendanceModel {
    private String id;
    private String studentId;
    private String fullName;
    private String program;
    private String type;
    private String date;
    private String timeIn;
    private String timeOut;

    // Constructor
    public AttendanceModel(String id, String studentId, String fullName, String program,
                           String type, String date, String timeIn, String timeOut) {
        this.id = id;
        this.studentId = studentId;
        this.fullName = fullName;
        this.program = program;
        this.type = type;
        this.date = date;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
    }

    // Alternative constructor for backward compatibility
    public AttendanceModel(String timeIn, String timeOut, String date) {
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.date = date;
    }

    // Getters
    public String getId() { return id; }
    public String getStudentId() { return studentId; }
    public String getFullName() { return fullName; }
    public String getProgram() { return program; }
    public String getType() { return type; }
    public String getDate() { return date; }
    public String getTimeIn() { return timeIn; }
    public String getTimeOut() { return timeOut; }

    // Setters
    public void setId(int id) { this.id = String.valueOf(id); }
    public void setStudentId(int studentId) { this.studentId = String.valueOf(studentId); }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setProgram(String program) { this.program = program; }
    public void setType(String type) { this.type = type; }
    public void setDate(String date) { this.date = date; }
    public void setTimeIn(String timeIn) { this.timeIn = timeIn; }
    public void setTimeOut(String timeOut) { this.timeOut = timeOut; }

    // Helper method to format time display
    public String getFormattedTimeIn() {
        return timeIn != null ? "Time In: " + formatTime(timeIn) : "Time In: --";
    }

    public String getFormattedTimeOut() {
        return timeOut != null ? "Time Out: " + formatTime(timeOut) : "Time Out: --";
    }

    // Helper method to format time from 24h to 12h format
    private String formatTime(String time24h) {
        try {
            String[] parts = time24h.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            String period = hour >= 12 ? "PM" : "AM";
            if (hour == 0) hour = 12;
            else if (hour > 12) hour -= 12;

            return String.format("%02d:%02d %s", hour, minute, period);
        } catch (Exception e) {
            return time24h; // Return original if formatting fails
        }
    }
}