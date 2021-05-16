package com.highcom.todolog.ui.loglist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.highcom.todolog.R;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class LogViewHolder extends RecyclerView.ViewHolder {
    private TextView mDateLog;
    private ImageButton mLogIcon;
    private TextView mOperationLog;

    public LogViewHolder(@NonNull View itemView) {
        super(itemView);
        mDateLog = (TextView) itemView.findViewById(R.id.todolog_date);
        mLogIcon = (ImageButton) itemView.findViewById(R.id.log_icon);
        mOperationLog = (TextView) itemView.findViewById(R.id.todolog_operation);
    }

    public void bind(Date date, int color, String log) {
        final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        mDateLog.setText(dateFormat.format(date));
        mLogIcon.setColorFilter(color);
        mOperationLog.setText(log);
    }

    static LogViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_todolog, parent, false);
        return new LogViewHolder(view);
    }
}
