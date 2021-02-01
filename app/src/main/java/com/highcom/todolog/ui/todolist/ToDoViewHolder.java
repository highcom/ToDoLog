package com.highcom.todolog.ui.todolist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.highcom.todolog.R;

public class ToDoViewHolder extends RecyclerView.ViewHolder {
    private ImageButton mCheckButton;
    private TextView mTodoContents;
    private TextView mTodoLog;
    private ImageButton mRearrangeButton;


    public ToDoViewHolder(@NonNull View itemView) {
        super(itemView);
        mCheckButton = (ImageButton) itemView.findViewById(R.id.check_button);
        mTodoContents = (TextView) itemView.findViewById(R.id.todo_contents);
        mTodoLog = (TextView) itemView.findViewById(R.id.todo_log);
        mRearrangeButton = (ImageButton) itemView.findViewById(R.id.rearrange_button);
    }

    public void bind(String contents, String log) {
        mTodoContents.setText(contents);
        mTodoLog.setText(log);
    }

    static ToDoViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_todolist, parent, false);
        return new ToDoViewHolder(view);
    }
}
