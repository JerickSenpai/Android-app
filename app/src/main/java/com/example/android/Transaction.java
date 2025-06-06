package com.example.android;

public class Transaction {
    private String bookTitle;
    private String borrowDate;
    private String returnDate;
    private String status;
    private int transactionId;

    // Constructor
    public Transaction(String bookTitle, String borrowDate, String returnDate) {
        this.bookTitle = bookTitle;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
    }

    // Constructor with status
    public Transaction(String bookTitle, String borrowDate, String returnDate, String status) {
        this.bookTitle = bookTitle;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    // Constructor with all fields
    public Transaction(int transactionId, String bookTitle, String borrowDate, String returnDate, String status) {
        this.transactionId = transactionId;
        this.bookTitle = bookTitle;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    // Getters
    public String getBookTitle() {
        return bookTitle;
    }

    public String getBorrowDate() {
        return borrowDate;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public String getStatus() {
        return status;
    }

    public int getTransactionId() {
        return transactionId;
    }

    // Setters
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public void setBorrowDate(String borrowDate) {
        this.borrowDate = borrowDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }
}