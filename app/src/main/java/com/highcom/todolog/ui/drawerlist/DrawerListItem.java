package com.highcom.todolog.ui.drawerlist;

public class DrawerListItem {
    private long mGroupId;
    private String mGroupName;
    private int mCount;

    public DrawerListItem() {

    }

    public DrawerListItem(long groupId, String groupName) {
        mGroupId = groupId;
        mGroupName = groupName;
    }

    public void setGroupId(long groupId) {
        mGroupId = groupId;
    }

    public void setGroupName(String groupName) {
        mGroupName = groupName;
    }

    public void setGroupCount(int count) {
        mCount = count;
    }

    public long getGroupId() {
        return mGroupId;
    }

    public String getGroupName() {
        return mGroupName;
    }

    public int getGroupCount() {
        return mCount;
    }
}
