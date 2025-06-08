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

    public String getId() { return id; }
    public String getStudentId() { return studentId; }
    public String getFullName() { return fullName; }
    public String getProgram() { return program; }
    public String getType() { return type; }
    public String getDate() { return date; }
    public String getTimeIn() { return timeIn; }
    public String getTimeOut() { return timeOut; }

    public String getFormattedTimeIn() {
        return timeIn != null ? "Time In: " + formatTime(timeIn) : "Time In: --";
    }

    public String getFormattedTimeOut() {
        return timeOut != null ? "Time Out: " + formatTime(timeOut) : "Time Out: --";
    }

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
            return time24h;
        }
    }
}
