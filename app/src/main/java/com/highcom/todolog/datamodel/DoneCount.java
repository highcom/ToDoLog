package com.highcom.todolog.datamodel;

import androidx.room.ColumnInfo;

import java.sql.Date;

public class DoneCount {
    @ColumnInfo(name = "date")
    public Date mDate;
    @ColumnInfo(name = "count")
    public int mDoneCount;
}
