package com.highcom.todolog.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.highcom.todolog.R;
import com.highcom.todolog.datamodel.ToDoAndLog;
import com.highcom.todolog.datamodel.ToDoLogRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ToDoWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private int mAppWidgetId;
    private List<ToDoAndLog> mTodoAndLogList;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExtractor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public ToDoWidgetRemoteViewsFactory(Context applicationContext, Intent intent) {
        mContext = applicationContext;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        final long identityToken = Binder.clearCallingIdentity();
        long selectGroupId = ToDoAppWidgetConfigure.loadSelectWidgetGroupIdPref(mContext, mAppWidgetId);

        List<Future<?>> futureList = new ArrayList<>();
        // ワーカースレッドで実行する。
        Future<?> future = databaseWriteExtractor.submit(() -> {
            mTodoAndLogList = ToDoLogRepository.getInstance(mContext).getTodoListByTaskGroupSync(selectGroupId);
        });
        futureList .add(future);
        // ワーカースレっその処理完了を待つ
        for (Future<?> f : futureList) {
            try {
                f.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        futureList.clear();
        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return mTodoAndLogList.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        if (i == AdapterView.INVALID_POSITION) {
            return  null;
        }

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.todo_widget_list_item);
        rv.setTextViewText(R.id.widgetItemTaskNameLabel, mTodoAndLogList.get(i).toDo.getContents());

        Intent fillInIntent = new Intent();
        fillInIntent.putExtra("TASK_TEXT", mTodoAndLogList.get(i).toDo.getContents());
        rv.setOnClickFillInIntent(R.id.widgetItemContainer, fillInIntent);

        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
