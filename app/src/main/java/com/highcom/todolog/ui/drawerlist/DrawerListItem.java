package com.highcom.todolog.ui.drawerlist;

public class DrawerListItem {
    private String mGroupName;

    public DrawerListItem() {

    }

    public DrawerListItem(String groupName) {
        mGroupName = groupName;
    }

    public void setGroupName(String groupName) {
        mGroupName = groupName;
    }

    public String getGroupName() {
        return mGroupName;
    }
}
