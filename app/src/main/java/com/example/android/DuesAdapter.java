package com.example.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class DuesAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<Due> duesList;

    public DuesAdapter(Context context, ArrayList<Due> duesList) {
        this.context = context;
        this.duesList = duesList;
    }

    @Override
    public int getCount() {
        return duesList.size();
    }

    @Override
    public Object getItem(int position) {
        return duesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        TextView title, borrowDate, dueDate, fineAmount, daysOverdue;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_due, parent, false);
            holder = new ViewHolder();
            holder.title = convertView.findViewById(R.id.bookTitle);
            holder.borrowDate = convertView.findViewById(R.id.borrowDate);
            holder.dueDate = convertView.findViewById(R.id.dueDate);
            holder.fineAmount = convertView.findViewById(R.id.fineAmount);
            holder.daysOverdue = convertView.findViewById(R.id.daysOverdue);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Due due = duesList.get(position);
        holder.title.setText(due.getBookTitle() != null ? due.getBookTitle() : "N/A");
        holder.borrowDate.setText("Borrowed: " + (due.getBorrowDate() != null ? due.getBorrowDate() : "N/A"));
        holder.dueDate.setText("Due: " + (due.getDueDate() != null ? due.getDueDate() : "N/A"));
        holder.fineAmount.setText(String.format(Locale.getDefault(), "Fine: ₱%.2f", due.getFineAmount()));
        holder.daysOverdue.setText("Overdue: " + due.getDaysOverdue() + " day(s)");

        return convertView;
    }
}
