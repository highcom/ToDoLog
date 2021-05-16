package com.highcom.todolog.datamodel;

import android.graphics.Color;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Date;

@Entity(tableName = "log_table")
public class Log implements Cloneable {
    public static final int LOG_NOCHANGE = 0;
    public static final int LOG_CREATE_NEW = 1;
    public static final int LOG_CHANGE_STATUS_TODO = 2;
    public static final int LOG_CHANGE_STATUS_DONE = 3;
    public static final int LOG_CHANGE_GROUP = 4;
    public static final int LOG_CHANGE_CONTENTS = 5;
    public static final String LOG_NOCHANGE_COLOR = "#ffd3a8";
    public static final String LOG_CREATE_NEW_COLOR = "#ffa8d3";
    public static final String LOG_CHANGE_STATUS_TODO_COLOR = "#a8ffa8";
    public static final String LOG_CHANGE_STATUS_DONE_COLOR = "#a8ffff";
    public static final String LOG_CHANGE_GROUP_COLOR = "#a8a8ff";
    public static final String LOG_CHANGE_CONTENTS_COLOR = "#ffffa8";

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

    @NonNull
    @Override
    public Log clone() {
        try {
            return (Log) super.clone();
        } catch (CloneNotSupportedException e) {
            return new Log(this.mLogId, this.mTodoId, this.mDate, this.mOperation);
        }
    }

    public int getLogIconColor() {
        switch (mOperation) {
            case Log.LOG_NOCHANGE:
                return Color.parseColor(Log.LOG_NOCHANGE_COLOR);
            case Log.LOG_CREATE_NEW:
            default:
                return Color.parseColor(Log.LOG_CREATE_NEW_COLOR);
            case Log.LOG_CHANGE_STATUS_TODO:
                return Color.parseColor(Log.LOG_CHANGE_STATUS_TODO_COLOR);
            case Log.LOG_CHANGE_STATUS_DONE:
                return Color.parseColor(Log.LOG_CHANGE_STATUS_DONE_COLOR);
            case Log.LOG_CHANGE_GROUP:
                return Color.parseColor(Log.LOG_CHANGE_GROUP_COLOR);
            case Log.LOG_CHANGE_CONTENTS:
                return Color.parseColor(Log.LOG_CHANGE_CONTENTS_COLOR);
        }
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
