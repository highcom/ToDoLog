package com.highcom.todolog.datamodel;

import androidx.room.ColumnInfo;

public class LogCount {
    @ColumnInfo(name = "operation")
    public int mOperation;
    @ColumnInfo(name = "count")
    public int mLogCount;
}
