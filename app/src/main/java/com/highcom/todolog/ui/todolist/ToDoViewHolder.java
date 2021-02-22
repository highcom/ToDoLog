package com.highcom.todolog.ui.todolist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.highcom.todolog.R;
import com.highcom.todolog.datamodel.ToDoAndLog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static com.highcom.todolog.datamodel.ToDoViewModel.STATUS_TODO;

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

    public void bind(ToDoAndLog toDoAndLog) {
        final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        mTodoId = toDoAndLog.toDo.getTodoId();
        if (toDoAndLog.toDo.getState() == STATUS_TODO) mCheckButton.setImageResource(R.drawable.ic_todo_uncheck);
        else mCheckButton.setImageResource(R.drawable.ic_todo_check);
        mTodoContents.setText(toDoAndLog.toDo.getContents());
        mTodoLogDate.setText(dateFormat.format(toDoAndLog.log.getDate()));
        mTodoLogOperation.setText(toDoAndLog.log.getOperation());
    }

    static ToDoViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_todolist, parent, false);
        return new ToDoViewHolder(view);
    }

    public int getTodoId() {
        return mTodoId;
    }
}
