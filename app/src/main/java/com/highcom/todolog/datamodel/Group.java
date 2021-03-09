package com.highcom.todolog.datamodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "group_table")
public class Group implements Cloneable {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "group_id")
    private long mGroupId;

    @NonNull
    @ColumnInfo(name = "group_order")
    private int mGroupOrder;

    @NonNull
    @ColumnInfo(name = "group_name")
    private String mGroupName;

    public Group(@NonNull long groupId,
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

    @NonNull
    @Override
    public Group clone() {
        try {
            return (Group) super.clone();
        } catch (CloneNotSupportedException e) {
            return new Group(this.mGroupId, this.mGroupOrder, this.mGroupName);
        }
    }

    public long getGroupId() {
        return mGroupId;
    }

    public int getGroupOrder() {
        return mGroupOrder;
    }

    public String getGroupName() {
        return mGroupName;
    }

    public void setGroupId(long groupId) {
        this.mGroupId = groupId;
    }

    public void setGroupOrder(int groupOrder) {
        this.mGroupOrder = groupOrder;
    }

    public void setGroupName(String groupName) {
        this.mGroupName = groupName;
    }
}
