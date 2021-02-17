package com.highcom.todolog.datamodel;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "todo_table")
public class ToDo {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "todo_id")
    private int mTodoId;

    @NonNull
    @ColumnInfo(name = "state")
    private int mState;

    @NonNull
    @ColumnInfo(name = "task_group")
    private String mTaskGroup;

    @ColumnInfo(name = "contents")
    private String mContents;

    @ColumnInfo(name = "latest_log_id")
    private int mLatestLogId;

    public ToDo(@NonNull int todoId,
                @NonNull int state,
                @NonNull String taskGroup,
                String contents,
                int latestLogId) {
        this.mTodoId = todoId;
        this.mState = state;
        this.mTaskGroup = taskGroup;
        this.mContents = contents;
        this.mLatestLogId = latestLogId;
    }

    @NonNull
    public int getTodoId() {
        return this.mTodoId;
    }

    public int getState() {
        return this.mState;
    }

    public String getTaskGroup() {
        return this.mTaskGroup;
    }

    public String getContents() {
        return this.mContents;
    }

    public int getLatestLogId() {
        return this.mLatestLogId;
    }

    @NonNull
    public void setTodoId(int todoId) {
        this.mTodoId = todoId;
    }

    public void setState(int state) {
        this.mState = state;
    }

    public void setTaskGroup(String taskGroup) {
        this.mTaskGroup = taskGroup;
    }

    public void setContents(String contents) {
        this.mContents = contents;
    }

    public void setLatestLogId(int latestLogId) {
        this.mLatestLogId = latestLogId;
    }
}
