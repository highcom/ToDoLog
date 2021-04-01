package com.highcom.todolog.widget;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RemoteViews;

import com.highcom.todolog.R;
import com.highcom.todolog.ToDoMainActivity;
import com.highcom.todolog.datamodel.Group;
import com.highcom.todolog.datamodel.GroupViewModel;
import com.highcom.todolog.datamodel.StringsResource;
import com.highcom.todolog.ui.drawerlist.DrawerListAdapter;
import com.highcom.todolog.ui.drawerlist.DrawerListItem;
import com.highcom.todolog.ui.todolist.ToDoListFragment;

import java.util.ArrayList;

import static com.highcom.todolog.ui.todolist.ToDoListFragment.SELECT_GROUP;

public class ToDoAppWidgetConfigure extends AppCompatActivity {

    public static final String SELECT_WIDGET_GROUP_ID = "selectWidgetGroupId";
    public static final String SELECT_WIDGET_GROUP_NAME = "selectWidgetGroupName";
    // TODO:対策できたらstaticけす
    private static int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_app_widget_configure);
        setTitle(getString(R.string.group_select));

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        ListView listView = findViewById(R.id.app_widget_list_view);
        GroupViewModel groupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);
        groupViewModel.getGroupList().observe(this, groupList -> {
            ArrayList<DrawerListItem> drawerListItem = new ArrayList<>();
            for (Group group : groupList) {
                drawerListItem.add(new DrawerListItem(group.getGroupName()));
            }
            DrawerListAdapter adapter = new DrawerListAdapter(this, R.layout.row_drawerlist, drawerListItem);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener((adapterView, view, i, l) -> {
                long selectGroupId = groupList.get(i).getGroupId();
                String selectGroupName = groupList.get(i).getGroupName();
                saveSelectWidgetGroupPref(getApplicationContext(), mAppWidgetId, selectGroupId, selectGroupName);
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.todo_appwidget);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
                appWidgetManager.updateAppWidget(mAppWidgetId, views);
                ToDoAppWidgetProvider.updateAppWidget(this.getApplicationContext(), appWidgetManager, mAppWidgetId);

                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            });
        });
    }

    static void saveSelectWidgetGroupPref(Context context, int appWidgetId, long selectGroupId, String selectGroupName) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putLong(SELECT_WIDGET_GROUP_ID + appWidgetId, selectGroupId).apply();
        prefs.edit().putString(SELECT_WIDGET_GROUP_NAME + appWidgetId, selectGroupName).apply();
    }

    static long loadSelectWidgetGroupIdPref(Context context, int appWidgetId) {
        // TODO:どう直すか
        if (appWidgetId == 0) appWidgetId = mAppWidgetId;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long groupId = prefs.getLong(SELECT_WIDGET_GROUP_ID + appWidgetId, -1);
        return groupId;
    }

    static String loadSelectWidgetGroupNamePref(Context context, int appWidgetId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String groupName = prefs.getString(SELECT_WIDGET_GROUP_NAME + appWidgetId, "");
        return groupName;
    }
}