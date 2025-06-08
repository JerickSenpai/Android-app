package com.example.android;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class PlaceholderDataHelper {

    private static final Random random = new Random();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // Sample book titles for placeholder data
    private static final String[] BOOK_TITLES = {
            "Introduction to Programming",
            "Data Structures and Algorithms",
            "Computer Networks",
            "Database Management Systems",
            "Software Engineering Principles",
            "Web Development Fundamentals",
            "Mobile App Development",
            "Artificial Intelligence Basics",
            "Cybersecurity Essentials",
            "Operating Systems Concepts"
    };

    // Transaction statuses for history
    private static final String[] TRANSACTION_STATUSES = {"approved", "returned", "pending", "overdue"};

    /**
     * Generate placeholder attendance records
     */
    public static List<AttendanceModel> generatePlaceholderAttendance(int count) {
        List<AttendanceModel> attendanceList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < count; i++) {
            calendar.add(Calendar.DAY_OF_YEAR, -random.nextInt(30));
            String date = dateFormat.format(calendar.getTime());

            int timeInHour = 8 + random.nextInt(4);
            int timeInMinute = random.nextInt(60);
            String timeIn = String.format(Locale.getDefault(), "%02d:%02d", timeInHour, timeInMinute);

            String timeOut = null;
            if (random.nextFloat() < 0.8f) {
                int timeOutHour = timeInHour + 2 + random.nextInt(6);
                int timeOutMinute = random.nextInt(60);
                timeOut = String.format(Locale.getDefault(), "%02d:%02d", timeOutHour, timeOutMinute);
            }

            String studentId = "STU255172339";
            String fullName = "Jerick Francisco";
            String program = "Information Technology";
            String type = timeOut != null ? "exit" : "entry";
            String id = String.valueOf(i + 1);

            attendanceList.add(new AttendanceModel(
                    id, studentId, fullName, program, type, date, timeIn, timeOut
            ));

            calendar = Calendar.getInstance();
        }

        return attendanceList;
    }

    /**
     * Generate placeholder transaction history
     */
    public static List<Transaction> generatePlaceholderTransactions(int count) {
        List<Transaction> transactions = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < count; i++) {
            // Generate borrow date (past dates)
            calendar.add(Calendar.DAY_OF_YEAR, -random.nextInt(90));
            String borrowDate = dateFormat.format(calendar.getTime());

            // Generate return date (some books returned, some not)
            String returnDate;
            String status = TRANSACTION_STATUSES[random.nextInt(TRANSACTION_STATUSES.length)];

            if (status.equals("returned")) {
                calendar.add(Calendar.DAY_OF_YEAR, random.nextInt(14) + 1); // 1-14 days after borrow
                returnDate = dateFormat.format(calendar.getTime());
            } else if (status.equals("approved")) {
                returnDate = "Currently borrowed";
            } else {
                returnDate = "Not returned";
            }

            String bookTitle = BOOK_TITLES[random.nextInt(BOOK_TITLES.length)];

            transactions.add(new Transaction(bookTitle, borrowDate, returnDate, status));

            // Reset calendar for next iteration
            calendar = Calendar.getInstance();
        }

        return transactions;
    }

    /**
     * Generate placeholder dues data
     */
    public static List<Due> generatePlaceholderDues(int count) {
        List<Due> dues = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < count; i++) {
            // Generate borrow date (past dates)
            calendar.add(Calendar.DAY_OF_YEAR, -random.nextInt(45) - 15); // 15-60 days ago
            String borrowDate = dateFormat.format(calendar.getTime());

            // Generate due date (overdue dates)
            calendar.add(Calendar.DAY_OF_YEAR, 14); // 14 days borrowing period
            String dueDate = dateFormat.format(calendar.getTime());

            // Calculate days overdue (current date - due date)
            Calendar today = Calendar.getInstance();
            long diffInMillies = today.getTimeInMillis() - calendar.getTimeInMillis();
            int daysOverdue = (int) (diffInMillies / (1000 * 60 * 60 * 24));

            // Calculate fine amount (e.g., 5 pesos per day)
            double fineAmount = daysOverdue * 5.0;

            String bookTitle = BOOK_TITLES[random.nextInt(BOOK_TITLES.length)];

            dues.add(new Due(bookTitle, borrowDate, dueDate, fineAmount, daysOverdue));

            // Reset calendar for next iteration
            calendar = Calendar.getInstance();
        }

        return dues;
    }
}