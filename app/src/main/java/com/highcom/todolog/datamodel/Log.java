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
    private int mLogId;

    @NonNull
    @ColumnInfo(name = "todo_id")
    private int mTodoId;

    @ColumnInfo(name = "date")
    private Date mDate;

    @ColumnInfo(name = "operation")
    private String mOperation;

    public Log(@NonNull int logId,
               @NonNull int todoId,
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

    public int getLogId() {
        return mLogId;
    }

    public int getTodoId() {
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

    public void setTodoId(int todoid) {
        this.mLogId = todoid;
    }

    public void setDate(Date date) {
        this.mDate = date;
    }

    public void setOperation(String operation) {
        this.mOperation = operation;
    }
}
