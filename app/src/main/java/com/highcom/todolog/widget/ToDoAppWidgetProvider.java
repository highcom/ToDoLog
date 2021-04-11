package com.highcom.todolog.widget;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.highcom.todolog.R;
import com.highcom.todolog.ToDoMainActivity;

public class ToDoAppWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        String selectGroupName = ToDoAppWidgetConfigure.loadSelectWidgetGroupNamePref(context, appWidgetId);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.todo_appwidget);

        Intent titleIntent = new Intent(context, ToDoMainActivity.class);
        titleIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        views.setTextViewText(R.id.todo_widget_title_view, selectGroupName);
        PendingIntent titlePendingIntent = PendingIntent.getActivity(context, 0, titleIntent, 0);
        // タイトルを押下した時のアクションを定義する
        views.setOnClickPendingIntent(R.id.todo_widget_title_view, titlePendingIntent);

        Intent listIntent = new Intent(context, ToDoWidgetRemoteViewsService.class);
        listIntent.setData(Uri.fromParts("content", Integer.toString(appWidgetId), null));
        views.setRemoteAdapter(R.id.todo_widget_list_view, listIntent);
        // リストを選択した時のアクションを定義する
        Intent clickIntentTemplate = new Intent(context, ToDoMainActivity.class);
        clickIntentTemplate.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(clickIntentTemplate)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.todo_widget_list_view, clickPendingIntentTemplate);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static void sendRefreshBroadcast(Context context) {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.setComponent(new ComponentName(context, ToDoAppWidgetProvider.class));
        context.sendBroadcast(intent);
    }

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
