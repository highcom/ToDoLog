package com.highcom.todolog.ui.todolist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.highcom.todolog.R;
import com.highcom.todolog.datamodel.StringsResource;
import com.highcom.todolog.datamodel.ToDo;
import com.highcom.todolog.datamodel.ToDoAndLog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ToDoViewHolder extends RecyclerView.ViewHolder {
    private ToDoAndLog mToDoAndLog;
    private ImageButton mCheckButton;
    private EditText mTodoContents;
    private TextView mTodoLogDate;
    private TextView mTodoLogOperation;
    private ImageButton mRearrangeButton;

    public interface ToDoViewHolderListener {
        void onToDoCheckButtonClicked(View view, ToDoAndLog toDoAndLog, String contents);
        void onToDoContentsClicked(View view);
        void onToDoContentsOutOfFocused(View view, ToDoAndLog toDoAndLog, String contents);
    }

    public ToDoViewHolder(@NonNull View itemView, ToDoViewHolderListener toDoViewHolderListener) {
        super(itemView);
        mCheckButton = (ImageButton) itemView.findViewById(R.id.check_button);
        mTodoContents = (EditText) itemView.findViewById(R.id.todo_contents);
        mTodoLogDate = (TextView) itemView.findViewById(R.id.todo_log_date);
        mTodoLogOperation = (TextView) itemView.findViewById(R.id.todo_log_operation);
        mRearrangeButton = (ImageButton) itemView.findViewById(R.id.rearrange_button);

        mCheckButton.setOnClickListener(view -> {
            // 内容を編集中であればやめる
            mTodoContents.setFocusable(false);
            mTodoContents.setFocusableInTouchMode(false);
            mTodoContents.requestFocus();
            toDoViewHolderListener.onToDoCheckButtonClicked(view, mToDoAndLog, mTodoContents.getText().toString());
        });
        mTodoContents.setOnClickListener(view -> toDoViewHolderListener.onToDoContentsClicked(view));
        mTodoContents.setOnFocusChangeListener((view, b) -> {
            if (b) {
                toDoViewHolderListener.onToDoContentsClicked(view);
            } else {
                // フォーカスが外れた時に内容が変更されていたら更新する
                toDoViewHolderListener.onToDoContentsOutOfFocused(view, mToDoAndLog, mTodoContents.getText().toString());
            }
        });
        mRearrangeButton.setOnClickListener(view -> {
            // 内容を編集中であればやめる
            mTodoContents.setFocusable(false);
            mTodoContents.setFocusableInTouchMode(false);
            mTodoContents.requestFocus();
        });
    }

    public ToDoViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void bind(ToDoAndLog toDoAndLog) {
        mToDoAndLog = toDoAndLog;
        if (toDoAndLog.toDo == null || toDoAndLog.log == null) return;
        // チェックボタンの状態を設定する
        if (mToDoAndLog.toDo.getState() == ToDo.STATUS_TODO) mCheckButton.setImageResource(R.drawable.ic_todo_uncheck);
        else mCheckButton.setImageResource(R.drawable.ic_todo_check);
        // 内容を設定する
        mTodoContents.setText(toDoAndLog.toDo.getContents());
        // ステータスによて表示色を変える
        if (mToDoAndLog.toDo.getState() == ToDo.STATUS_TODO) mTodoContents.setTextColor(mTodoContents.getResources().getColor(android.R.color.black));
        else if (mToDoAndLog.toDo.getState() == ToDo.STATUS_DONE) mTodoContents.setTextColor(mTodoContents.getResources().getColor(android.R.color.darker_gray));

        // ログ内容を設定する
        final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        mTodoLogDate.setText(dateFormat.format(toDoAndLog.log.getDate()));
        mTodoLogOperation.setText(StringsResource.get().mLogOperationItems.get(toDoAndLog.log.getOperation()));

        // 内容が空の場合、新規に作成されたものなので編集状態にする
        if (toDoAndLog.toDo.getContents().equals("")) {
            mTodoContents.performClick();
        }
    }

    static ToDoViewHolder create(ViewGroup parent, ToDoViewHolderListener toDoViewHolderListener) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_todolist, parent, false);
        return new ToDoViewHolder(view, toDoViewHolderListener);
    }

    static ToDoViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_footer, parent, false);
        return new ToDoViewHolder(view);
    }

    public long getTodoId() {
        return mToDoAndLog.toDo.getTodoId();
    }

    public ToDo getToDo() {
        return mToDoAndLog.toDo;
    }
}
