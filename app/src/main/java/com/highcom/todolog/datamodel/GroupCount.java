package com.highcom.todolog.datamodel;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

public class GroupCount {
    @ColumnInfo(name = "group_id")
    public int mGroupId;
    @ColumnInfo(name = "count")
    public int mGroupCount;
}
