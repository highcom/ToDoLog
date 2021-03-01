package com.highcom.todolog.datamodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Date;

@Entity(tableName = "log_table")
public class Log {
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
    private String mOperation;

    public Log(@NonNull long logId,
               @NonNull long todoId,
               Date date,
               String operation) {
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
                && this.getOperation().equals(log.getOperation())) {
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

    public String getOperation() {
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

    public void setOperation(String operation) {
        this.mOperation = operation;
    }
}
