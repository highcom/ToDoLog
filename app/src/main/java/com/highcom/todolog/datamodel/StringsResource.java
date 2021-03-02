package com.highcom.todolog.datamodel;

import android.content.Context;

import com.highcom.todolog.R;
import com.highcom.todolog.ToDoDetailActivity;

import java.util.HashMap;
import java.util.Map;

public class StringsResource {
    public class StatusItem {
        public int mId;
        public String mName;

        public StatusItem(int id, String name) {
            mId = id;
            mName = name;
        }
    }
    public Map<Integer, StatusItem> mStatusItems;

    public Map<Integer, String> mLogOperationItems;

    private static StringsResource mInstance;
    private StringsResource (Context context) {
        mStatusItems = new HashMap<>();
        mStatusItems.put(0, new StatusItem(ToDo.STATUS_TODO, context.getString(R.string.detail_status_todo)));
        mStatusItems.put(1, new StatusItem(ToDo.STATUS_DONE, context.getString(R.string.detail_status_done)));

        mLogOperationItems = new HashMap<>();
        mLogOperationItems.put(Log.LOG_CREATE_NEW, context.getString(R.string.log_create_new));
        mLogOperationItems.put(Log.LOG_CHANGE_STATUS_TODO, context.getString(R.string.log_change_status_todo));
        mLogOperationItems.put(Log.LOG_CHANGE_STATUS_DONE, context.getString(R.string.log_change_status_done));
        mLogOperationItems.put(Log.LOG_CHANGE_GROUP, context.getString(R.string.log_change_group));
        mLogOperationItems.put(Log.LOG_CHANGE_CONTENTS, context.getString(R.string.log_change_contents));
    }

    public static void create(Context context) {
        mInstance = new StringsResource(context);
    }

    public static StringsResource get() {
        return mInstance;
    }
}
