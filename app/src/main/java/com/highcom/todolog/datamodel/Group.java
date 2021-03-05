package com.highcom.todolog.datamodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "group_table")
public class Group {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "group_id")
    private int mGroupId;

    @NonNull
    @ColumnInfo(name = "groupOrder")
    private int mGroupOrder;

    @NonNull
    @ColumnInfo(name = "groupName")
    private String mGroupName;

    public Group(@NonNull int groupId,
                 @NonNull int groupOrder,
                 @NonNull String groupName) {
        this.mGroupId = groupId;
        this.mGroupOrder = groupOrder;
        this.mGroupName = groupName;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        Group group = (Group)obj;
        if (this.getGroupId() == group.getGroupId()
            && this.getGroupOrder() == group.getGroupOrder()
            && this.getGroupName().equals(group.getGroupName())) {
            return true;
        }
        return false;
    }

    public int getGroupId() {
        return mGroupId;
    }

    public int getGroupOrder() {
        return mGroupOrder;
    }

    public String getGroupName() {
        return mGroupName;
    }

    public void setGroupId(int groupId) {
        this.mGroupId = groupId;
    }

    public void setmGroupOrder(int groupOrder) {
        this.mGroupOrder = groupOrder;
    }

    public void setGroupName(String groupName) {
        this.mGroupName = groupName;
    }
}
