package com.highcom.todolog.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class ToDoWidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ToDoWidgetRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}
