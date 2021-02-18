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
    private int mTodoId;
    private ImageButton mCheckButton;
    private TextView mTodoContents;
    private TextView mTodoLogDate;
    private TextView mTodoLogOperation;
    private ImageButton mRearrangeButton;


    public ToDoViewHolder(@NonNull View itemView) {
        super(itemView);
        mCheckButton = (ImageButton) itemView.findViewById(R.id.check_button);
        mTodoContents = (TextView) itemView.findViewById(R.id.todo_contents);
        mTodoLogDate = (TextView) itemView.findViewById(R.id.todo_log_date);
        mTodoLogOperation = (TextView) itemView.findViewById(R.id.todo_log_operation);
        mRearrangeButton = (ImageButton) itemView.findViewById(R.id.rearrange_button);
    }

    public void bind(int todoId, String contents, String logDate, String logOperation) {
        mTodoId = todoId;
        mTodoContents.setText(contents);
        mTodoLogDate.setText(logDate);
        mTodoLogOperation.setText(logOperation);
    }

    static ToDoViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_todolist, parent, false);
        return new ToDoViewHolder(view);
    }

    public int getTodoId() {
        return mTodoId;
    }
}
