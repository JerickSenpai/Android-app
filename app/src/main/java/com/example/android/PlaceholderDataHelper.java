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