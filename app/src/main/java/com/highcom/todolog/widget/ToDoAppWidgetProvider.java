package com.highcom.todolog.widget;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import com.highcom.todolog.R;
import com.highcom.todolog.ToDoMainActivity;

/**
 * ウィジェットに対するブロードキャストを処理するクラス
 * RemoteViewsServiceを呼び出してコレクションのウィジェットを生成する。
 */
public class ToDoAppWidgetProvider extends AppWidgetProvider {

    /**
     * ウィジェット配置時の画面更新処理
     * 配置されたウィジェットの数分のIDを引数で受け取り、ウィジェットIDに合わせた更新処理を行うように指示する。
     *
     * @param context ウィジェット画面のコンテキスト
     * @param appWidgetManager ウィジェット管理クラス
     * @param appWidgetIds 配置されているウィジェットIDのリスト
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    /**
     * ウィジェットに設定するデータの更新処理
     * onUpdateからと設定アクティビティで選択した際に明示的に呼び出される。
     * 設定アクティビティからの呼び出しは、選択したデータをSharedPreferenceに保存してから改めてウィジェットを更新するため。
     * onUpdateの呼び出しは、再起動時に保存されたSharedPreferenceからデータを読み出して呼び出される。
     *
     * @param context ウィジェット画面のコンテキスト
     * @param appWidgetManager ウィジェット管理クラス
     * @param appWidgetId 更新対象のウィジェットID
     */
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        String selectGroupName = ToDoAppWidgetConfigure.loadSelectWidgetGroupNamePref(context, appWidgetId);
        long selectGroupId = ToDoAppWidgetConfigure.loadSelectWidgetGroupIdPref(context, appWidgetId);

        int layoutId = R.layout.todo_appwidget;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // SDKのバージョンがR以降である場合にダークモード設定が導入されたため、それを判定する
            if (context.getTheme().getResources().getConfiguration().isNightModeActive()) {
                // ダークモードの場合はダークモード用の色設定にする
                layoutId = R.layout.todo_appwidget_dark;
            }
        }
        RemoteViews views = new RemoteViews(context.getPackageName(), layoutId);

        Intent titleIntent = new Intent(context, ToDoMainActivity.class);
        titleIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        views.setTextViewText(R.id.todo_widget_title_view, selectGroupName);
        titleIntent.putExtra(ToDoAppWidgetConfigure.SELECT_WIDGET_GROUP_ID, selectGroupId);
        titleIntent.setData(Uri.parse(titleIntent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent titlePendingIntent = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(titleIntent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        // タイトルを押下した時のアクションを定義する
        views.setOnClickPendingIntent(R.id.todo_widget_title_view, titlePendingIntent);

        Intent listIntent = new Intent(context, ToDoWidgetRemoteViewsService.class);
        listIntent.setData(Uri.fromParts("content", Integer.toString(appWidgetId), null));
        views.setRemoteAdapter(R.id.todo_widget_list_view, listIntent);
        // リストを選択した時のアクションを定義する
        Intent clickIntentTemplate = new Intent(context, ToDoMainActivity.class);
        clickIntentTemplate.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        clickIntentTemplate.putExtra(ToDoAppWidgetConfigure.SELECT_WIDGET_GROUP_ID, selectGroupId);
        clickIntentTemplate.setData(Uri.parse(clickIntentTemplate.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(clickIntentTemplate)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.todo_widget_list_view, clickPendingIntentTemplate);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * 外側のアプリからウィジェットに対してイベントを発行する処理
     *
     * @param context ウィジェット画面のコンテキスト
     */
    public static void sendRefreshBroadcast(Context context) {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.setComponent(new ComponentName(context, ToDoAppWidgetProvider.class));
        context.sendBroadcast(intent);
    }

    /**
     * ウィジェット自身がイベントを受信する事ができるようにする処理
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(final Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            // refresh all your widgets
            AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            ComponentName cn = new ComponentName(context, ToDoAppWidgetProvider.class);
            mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.todo_widget_list_view);
        }
        super.onReceive(context, intent);
    }
}
