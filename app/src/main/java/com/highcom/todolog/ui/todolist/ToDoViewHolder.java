package com.highcom.todolog.ui.todolist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
    private EditText mTodoContents;
    private TextView mTodoLogDate;
    private TextView mTodoLogOperation;
    private ImageButton mRearrangeButton;

    private String mBackupToDoContents;

    public interface ToDoViewHolderListener {
        void onToDoCheckButtonClicked(ToDoAndLog toDoAndLog);
        void onToDoContentsClicked(View view);
        void onToDoContentsOutOfFocused(ToDoAndLog toDoAndLog, String contents, boolean changed);
    }

    public ToDoViewHolder(@NonNull View itemView, ToDoViewHolderListener toDoViewHolderListener) {
        super(itemView);
        mCheckButton = (ImageButton) itemView.findViewById(R.id.check_button);
        mTodoContents = (EditText) itemView.findViewById(R.id.todo_contents);
        mTodoLogDate = (TextView) itemView.findViewById(R.id.todo_log_date);
        mTodoLogOperation = (TextView) itemView.findViewById(R.id.todo_log_operation);
        mRearrangeButton = (ImageButton) itemView.findViewById(R.id.rearrange_button);

        mCheckButton.setOnClickListener(view -> toDoViewHolderListener.onToDoCheckButtonClicked(mToDoAndLog));
        mTodoContents.setOnClickListener(view -> toDoViewHolderListener.onToDoContentsClicked(view));
        mTodoContents.setOnFocusChangeListener((view, b) -> {
            if (b) {
                // フォーカスが当たった時に編集前状態の内容ほ保持しておく
                mBackupToDoContents = mTodoContents.getText().toString();
            } else {
                // フォーカスが外れた時に内容が変更されていたら更新する
                boolean changed = !mTodoContents.getText().toString().equals(mBackupToDoContents);
                toDoViewHolderListener.onToDoContentsOutOfFocused(mToDoAndLog, mTodoContents.getText().toString(), changed);
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
