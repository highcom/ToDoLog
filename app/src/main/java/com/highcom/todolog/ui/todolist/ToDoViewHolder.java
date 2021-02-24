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
    private ToDoAndLog mToDoAndLog;
    private ImageButton mCheckButton;
    private TextView mTodoContents;
    private TextView mTodoLogDate;
    private TextView mTodoLogOperation;
    private ImageButton mRearrangeButton;

    public interface ToDoViewHolderListener {
        void onToDoViewHolderClicked(ToDoAndLog toDoAndLog);
    }

    public ToDoViewHolder(@NonNull View itemView, ToDoViewHolderListener toDoViewHolderListener) {
        super(itemView);
        mCheckButton = (ImageButton) itemView.findViewById(R.id.check_button);
        mTodoContents = (TextView) itemView.findViewById(R.id.todo_contents);
        mTodoLogDate = (TextView) itemView.findViewById(R.id.todo_log_date);
        mTodoLogOperation = (TextView) itemView.findViewById(R.id.todo_log_operation);
        mRearrangeButton = (ImageButton) itemView.findViewById(R.id.rearrange_button);

        mCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toDoViewHolderListener.onToDoViewHolderClicked(mToDoAndLog);
            }
        });
    }

    public void bind(ToDoAndLog toDoAndLog) {
        mToDoAndLog = toDoAndLog;
        if (mToDoAndLog.toDo.getState() == STATUS_TODO) mCheckButton.setImageResource(R.drawable.ic_todo_uncheck);
        else mCheckButton.setImageResource(R.drawable.ic_todo_check);
        mTodoContents.setText(toDoAndLog.toDo.getContents());
        final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        mTodoLogDate.setText(dateFormat.format(toDoAndLog.log.getDate()));
        mTodoLogOperation.setText(toDoAndLog.log.getOperation());
    }

    static ToDoViewHolder create(ViewGroup parent, ToDoViewHolderListener toDoViewHolderListener) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_todolist, parent, false);
        return new ToDoViewHolder(view, toDoViewHolderListener);
    }

    public int getTodoId() {
        return mToDoAndLog.toDo.getTodoId();
    }
}
