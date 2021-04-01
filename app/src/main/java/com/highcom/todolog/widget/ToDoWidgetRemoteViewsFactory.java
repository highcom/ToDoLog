package com.highcom.todolog.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.highcom.todolog.R;
import com.highcom.todolog.datamodel.ToDoAndLog;
import com.highcom.todolog.datamodel.ToDoLogRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ToDoWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private List<ToDoAndLog> mTodoAndLogList;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExtractor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public ToDoWidgetRemoteViewsFactory(Context applicationContext, Intent intent) {
        mContext = applicationContext;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        final long identityToken = Binder.clearCallingIdentity();
        long selectGroupId = ToDoAppWidgetConfigure.loadSelectWidgetGroupIdPref(mContext, 0);
        //ワーカースレッドからDB読み込み結果を受け取る。
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.obj != null) {
                    // TODO:ハンドラが帰ってくるまで待たせられないか？
                    mTodoAndLogList = (List<ToDoAndLog>) msg.obj;
                }
            }
        };
        BackgroundTaskRead backgroundTaskRead = new BackgroundTaskRead(mContext, handler, selectGroupId, mTodoAndLogList);
        //ワーカースレッドで実行する。
        databaseWriteExtractor.submit(backgroundTaskRead);
        Binder.restoreCallingIdentity(identityToken);
    }

    private static class BackgroundTaskRead implements Runnable {
        private Context context;
        private final Handler handler;
        private long selectGroupId;
        private List<ToDoAndLog> data;

        BackgroundTaskRead(Context context, Handler handler, long selectGroupId, List<ToDoAndLog> data) {
            this.context = context;
            this.handler = handler;
            this.selectGroupId = selectGroupId;
            this.data = data;
        }

        @WorkerThread
        @Override
        public void run() {
            //Daoクラスで用意したDB読み込みメソッドを実行する。
            data = ToDoLogRepository.getInstance(context).getTodoListByTaskGroupSync(selectGroupId);
        }
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        // TODO:データの読み込みタイミングが出来ていない
//        return mTodoAndLogList.size();
        return 3;
    }

    @Override
    public RemoteViews getViewAt(int i) {
        if (i == AdapterView.INVALID_POSITION) {
            return  null;
        }

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.todo_widget_list_item);
        // TODO:データの読み込みタイミングが出来ていない
//        rv.setTextViewText(R.id.widgetItemTaskNameLabel, mTodoAndLogList.get(i).toDo.getContents());
        rv.setTextViewText(R.id.widgetItemTaskNameLabel, "TEST");

        Intent fillInIntent = new Intent();
        // TODO:データの読み込みタイミングが出来ていない
//        fillInIntent.putExtra("TASK_TEXT", mTodoAndLogList.get(i).toDo.getContents());
        fillInIntent.putExtra("TASK_TEXT", "TEST");
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
