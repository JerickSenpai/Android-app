package com.example.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {

    private List<AttendanceModel> attendanceList;
    private Context context;

    public AttendanceAdapter(List<AttendanceModel> attendanceList) {
        this.attendanceList = attendanceList;
    }

    public AttendanceAdapter(List<AttendanceModel> attendanceList, Context context) {
        this.attendanceList = attendanceList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_attendance_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AttendanceModel attendance = attendanceList.get(position);

        // Set student information
        holder.tvName.setText(attendance.getFullName() != null ? attendance.getFullName() : "Unknown Student");
        holder.tvSection.setText(attendance.getProgram() != null ? attendance.getProgram() : "No Program");
        holder.tvAddress.setText("Library Room"); // You can make this dynamic based on your needs

        // Format and set date
        holder.tvDate.setText(formatDate(attendance.getDate()));

        // Set time in/out with formatting
        String timeInText = attendance.getTimeIn() != null ? formatTime(attendance.getTimeIn()) : "--:--";
        String timeOutText = attendance.getTimeOut() != null ? formatTime(attendance.getTimeOut()) : "--:--";

        holder.tvTimeIn.setText(timeInText);
        holder.tvTimeOut.setText(timeOutText);
    }

    @Override
    public int getItemCount() {
        return attendanceList != null ? attendanceList.size() : 0;
    }

    // Helper method to format date from YYYY-MM-DD to readable format
    private String formatDate(String dateStr) {
        if (dateStr == null) return "Unknown Date";

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr; // Return original if parsing fails
        }
    }

    // Helper method to format time from 24h to 12h format
    private String formatTime(String time24h) {
        if (time24h == null) return "--:--";

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

    // Method to update the list
    public void updateAttendanceList(List<AttendanceModel> newList) {
        this.attendanceList = newList;
        notifyDataSetChanged();
    }

    // Method to add single attendance record
    public void addAttendanceRecord(AttendanceModel attendance) {
        if (attendanceList != null) {
            attendanceList.add(0, attendance); // Add to beginning
            notifyItemInserted(0);
        }
    }

    // Method to remove attendance record
    public void removeAttendanceRecord(int position) {
        if (attendanceList != null && position >= 0 && position < attendanceList.size()) {
            attendanceList.remove(position);
            notifyItemRemoved(position);
        }
    }

    // Get attendance record at position
    public AttendanceModel getAttendanceAt(int position) {
        if (attendanceList != null && position >= 0 && position < attendanceList.size()) {
            return attendanceList.get(position);
        }
        return null;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSection, tvAddress, tvTimeIn, tvTimeOut, tvDate;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvSection = itemView.findViewById(R.id.tvSection);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvTimeIn = itemView.findViewById(R.id.tvTimeIn);
            tvTimeOut = itemView.findViewById(R.id.tvTimeOut);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}