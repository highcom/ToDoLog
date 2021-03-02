package com.highcom.todolog.datamodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Date;

@Entity(tableName = "log_table")
public class Log {
    public static final int LOG_CREATE_NEW = 1;
    public static final int LOG_CHANGE_STATUS_TODO = 2;
    public static final int LOG_CHANGE_STATUS_DONE = 3;
    public static final int LOG_CHANGE_GROUP = 4;
    public static final int LOG_CHANGE_CONTENTS = 5;

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "log_id")
    private long mLogId;

    @NonNull
    @ColumnInfo(name = "todo_id")
    private long mTodoId;

    @ColumnInfo(name = "date")
    private Date mDate;

    @ColumnInfo(name = "operation")
    private int mOperation;

    public Log(@NonNull long logId,
               @NonNull long todoId,
               Date date,
               int operation) {
        this.mLogId = logId;
        this.mTodoId = todoId;
        this.mDate = date;
        this.mOperation = operation;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        Log log = (Log)obj;
        if (this.getLogId() == log.getLogId()
                && this.getTodoId() == log.getTodoId()
                && this.getDate().equals(log.getDate())
                && this.getOperation() == log.getOperation()) {
            return true;
        }
        return false;
    }

    public long getLogId() {
        return mLogId;
    }

    public long getTodoId() {
        return mTodoId;
    }

    public Date getDate() {
        return mDate;
    }

    public int getOperation() {
        return mOperation;
    }

    public void setLogId(int logid) {
        this.mLogId = logid;
    }

    public void setTodoId(long todoid) {
        this.mTodoId = todoid;
    }

    public void setDate(Date date) {
        this.mDate = date;
    }

    public void setOperation(int operation) {
        this.mOperation = operation;
    }
}
