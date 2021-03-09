package com.highcom.todolog.datamodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "todo_table")
public class ToDo implements Cloneable {
    public static final int STATUS_TODO = 1;
    public static final int STATUS_DONE = 2;

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "todo_id")
    private long mTodoId;

    @NonNull
    @ColumnInfo(name = "todo_order")
    private int mTodoOrder;

    @NonNull
    @ColumnInfo(name = "state")
    private int mState;

    @NonNull
    @ColumnInfo(name = "group_id")
    private long mGroupId;

    @ColumnInfo(name = "contents")
    private String mContents;

    @ColumnInfo(name = "latest_log_id")
    private long mLatestLogId;

    public ToDo(@NonNull long todoId,
                @NonNull int todoOrder,
                @NonNull int state,
                @NonNull long groupId,
                String contents,
                long latestLogId) {
        this.mTodoId = todoId;
        this.mTodoOrder = todoOrder;
        this.mState = state;
        this.mGroupId = groupId;
        this.mContents = contents;
        this.mLatestLogId = latestLogId;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        ToDo toDo = (ToDo)obj;
        if (this.getTodoId() == toDo.getTodoId()
                && this.getTodoOrder() == toDo.getTodoOrder()
                && this.getState() == toDo.getState()
                && this.getGroupId() == toDo.getGroupId()
                && this.getContents().equals(toDo.getContents())
                && this.getLatestLogId() == toDo.getLatestLogId()) {
            return true;
        }
        return false;
    }

    @NonNull
    @Override
    public ToDo clone() {
        try {
            return (ToDo) super.clone();
        } catch (CloneNotSupportedException e) {
            return new ToDo(this.mTodoId, this.mTodoOrder, this.mState, this.mGroupId, this.mContents, this.mLatestLogId);
        }
    }

    @NonNull
    public long getTodoId() {
        return this.mTodoId;
    }

    @NonNull
    public int getTodoOrder() {
        return this.mTodoOrder;
    }

    public int getState() {
        return this.mState;
    }

    public long getGroupId() {
        return this.mGroupId;
    }

    public String getContents() {
        return this.mContents;
    }

    public long getLatestLogId() {
        return this.mLatestLogId;
    }

    @NonNull
    public void setTodoId(long todoId) {
        this.mTodoId = todoId;
    }

    @NonNull
    public void setTodoOrder(int todoOrder) {
        this.mTodoOrder = todoOrder;
    }

    public void setState(int state) {
        this.mState = state;
    }

    public void setGroupId(long groupId) {
        this.mGroupId = groupId;
    }

    public void setContents(String contents) {
        this.mContents = contents;
    }

    public void setLatestLogId(long latestLogId) {
        this.mLatestLogId = latestLogId;
    }
}
