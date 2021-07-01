package com.highcom.todolog.ui.grouplist;

import com.highcom.todolog.datamodel.Group;
import com.highcom.todolog.datamodel.GroupCount;

public class GroupListItem {
    private Group mGroup;
    private int mCount;

    public GroupListItem(Group group) {
        mGroup = group;
        mCount = 0;
    }

    public Group getGroup() {
        return mGroup;
    }

    public void setCount(int count) {
        mCount = count;
    }

    public int getCount() {
        return mCount;
    }
}
