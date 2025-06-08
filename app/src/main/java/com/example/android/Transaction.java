package com.example.android;

public class Transaction {
    private String bookTitle;
    private String borrowDate;
    private String returnDate;
    private String status;

    public Transaction(String bookTitle, String borrowDate, String returnDate, String status) {
        this.bookTitle = bookTitle;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.status = status;
    }

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
}