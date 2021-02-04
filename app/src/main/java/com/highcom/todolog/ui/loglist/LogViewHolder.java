package com.highcom.todolog.ui.loglist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.highcom.todolog.R;

import java.sql.Date;

public class LogViewHolder extends RecyclerView.ViewHolder {
    private TextView mDateLog;
    private TextView mOperationLog;

    public LogViewHolder(@NonNull View itemView) {
        super(itemView);
        mDateLog = (TextView) itemView.findViewById(R.id.todolog_date);
        mOperationLog = (TextView) itemView.findViewById(R.id.todolog_operation);
    }

    public void bind(String date, String log) {
        mDateLog.setText(date);
        mOperationLog.setText(log);
    }

    static LogViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_todolog, parent, false);
        return new LogViewHolder(view);
    }
}
