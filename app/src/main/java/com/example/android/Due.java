package com.example.android;

public class Due {
    private String bookTitle;
    private String borrowDate;
    private String dueDate;
    private double fineAmount;
    private int daysOverdue;

    public Due(String bookTitle, String borrowDate, String dueDate, double fineAmount, int daysOverdue) {
        this.bookTitle = bookTitle;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.fineAmount = fineAmount;
        this.daysOverdue = daysOverdue;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getBorrowDate() {
        return borrowDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public double getFineAmount() {
        return fineAmount;
    }

    public int getDaysOverdue() {
        return daysOverdue;
    }
}
