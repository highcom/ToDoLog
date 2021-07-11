package com.highcom.todolog.widget;

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

/**
 * コレクションのウィジェットを生成する場合の本体となるクラス
 * ListViewを利用する場合のAdapterクラスと同じ位置付けのもの。
 */
public class ToDoWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private int mAppWidgetId;
    private List<ToDoAndLog> mTodoAndLogList;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExtractor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    /**
     * コンストラクタ
     * ToDoAppWidgetProviderからUriで渡されたデータであるウィジェットIDを取得する。
     *
     * @param applicationContext
     * @param intent
     */
    public ToDoWidgetRemoteViewsFactory(Context applicationContext, Intent intent) {
        mContext = applicationContext;
        if (intent.getData() != null) {
            mAppWidgetId = Integer.parseInt(intent.getData().getSchemeSpecificPart());
        }
    }

    @Override
    public void onCreate() {
    }

    /**
     * 変更データ読み出し処理
     * このメソッドは変更がある度に呼び出される。
     */
    @Override
    public void onDataSetChanged() {
        final long identityToken = Binder.clearCallingIdentity();
        long selectGroupId = ToDoAppWidgetConfigure.loadSelectWidgetGroupIdPref(mContext, mAppWidgetId);

        List<Future<?>> futureList = new ArrayList<>();
        // ワーカースレッドで実行する。
        Future<?> future = databaseWriteExtractor.submit(() -> {
            mTodoAndLogList = ToDoLogRepository.getInstance(mContext).getTodoListOnlyToDoByTaskGroupSync(selectGroupId);
        });
        futureList .add(future);
        // ワーカースレッドその処理完了を待つ
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

    /**
     * 対応するウィジェットIDのSharedPreferenceのデータを削除するための処理
     * ウィジェットが削除される際に呼び出される。
     */
    @Override
    public void onDestroy() {
        ToDoAppWidgetConfigure.deleteSelectWidgetGroupPref(mContext, mAppWidgetId);
    }

    /**
     * コレクションであるListViewに表示する数を返却する
     *
     * @return データ数
     */
    @Override
    public int getCount() {
        return mTodoAndLogList.size();
    }

    /**
     * 引数で渡されている要素の順番を利用してデータを設定する処理
     * 各セル単位で呼び出される。
     *
     * @param i セルの行番号
     * @return セルに表示するView
     */
    @Override
    public RemoteViews getViewAt(int i) {
        if (i == AdapterView.INVALID_POSITION) {
            return  null;
        }

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.todo_widget_list_item);
        rv.setTextViewText(R.id.widget_todo_contents, mTodoAndLogList.get(i).toDo.getContents());

        Intent fillInIntent = new Intent();
        fillInIntent.putExtra("TASK_TEXT", mTodoAndLogList.get(i).toDo.getContents());
        rv.setOnClickFillInIntent(R.id.widget_item_container, fillInIntent);

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
