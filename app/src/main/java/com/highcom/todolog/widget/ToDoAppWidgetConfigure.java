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
import com.highcom.todolog.datamodel.GroupCount;
import com.highcom.todolog.datamodel.GroupViewModel;
import com.highcom.todolog.datamodel.StringsResource;
import com.highcom.todolog.datamodel.ToDo;
import com.highcom.todolog.ui.drawerlist.DrawerListAdapter;
import com.highcom.todolog.ui.drawerlist.DrawerListItem;
import com.highcom.todolog.ui.todolist.ToDoListFragment;

import java.util.ArrayList;
import java.util.List;

import static com.highcom.todolog.SettingActivity.PREF_FILE_NAME;
import static com.highcom.todolog.SettingActivity.PREF_PARAM_THEME_COLOR;
import static com.highcom.todolog.SettingActivity.PREF_PARAM_TODO_COUNT;
import static com.highcom.todolog.ui.todolist.ToDoListFragment.SELECT_GROUP;

public class ToDoAppWidgetConfigure extends AppCompatActivity {

    public static final String SELECT_WIDGET_GROUP_ID = "selectWidgetGroupId";
    public static final String SELECT_WIDGET_GROUP_NAME = "selectWidgetGroupName";
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    GroupViewModel mGroupViewModel;
    private boolean mTodoCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setThemeColor();
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
        mGroupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);
        mGroupViewModel.getGroupList().observe(this, groupList -> {
            ArrayList<DrawerListItem> drawerListItems = new ArrayList<>();
            for (Group group : groupList) {
                drawerListItems.add(new DrawerListItem(group.getGroupId(), group.getGroupName()));
            }
            // 各グループのToDoの数を設定する
            mGroupViewModel.getCountByGroupId(ToDo.STATUS_TODO).observe(this, groupCounts -> {
                for (DrawerListItem item : drawerListItems) {
                    // 一度0に設定してからToDo残数があるグループだけ更新
                    item.setGroupCount(0);
                    if (mTodoCount) {
                        for (GroupCount count : groupCounts) {
                            if (item.getGroupId() == count.mGroupId)
                                item.setGroupCount(count.mGroupCount);
                        }
                    }
                }
                DrawerListAdapter adapter = new DrawerListAdapter(this, R.layout.row_drawerlist, drawerListItems);
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
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences data = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        mTodoCount = data.getBoolean(PREF_PARAM_TODO_COUNT, true);
        // 更新通知を行うために、同じ値でupdateする
        List<Group> groupList = mGroupViewModel.getGroupList().getValue();
        if (groupList != null) {
            mGroupViewModel.update(groupList);
        }
    }

    static void saveSelectWidgetGroupPref(Context context, int appWidgetId, long selectGroupId, String selectGroupName) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putLong(SELECT_WIDGET_GROUP_ID + appWidgetId, selectGroupId).apply();
        prefs.edit().putString(SELECT_WIDGET_GROUP_NAME + appWidgetId, selectGroupName).apply();
    }

    static long loadSelectWidgetGroupIdPref(Context context, int appWidgetId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long groupId = prefs.getLong(SELECT_WIDGET_GROUP_ID + appWidgetId, -1);
        return groupId;
    }

    static String loadSelectWidgetGroupNamePref(Context context, int appWidgetId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String groupName = prefs.getString(SELECT_WIDGET_GROUP_NAME + appWidgetId, "");
        return groupName;
    }

    static void deleteSelectWidgetGroupPref(Context context, int appWidgetId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().remove(SELECT_WIDGET_GROUP_ID + appWidgetId).apply();
        prefs.edit().remove(SELECT_WIDGET_GROUP_NAME + appWidgetId).apply();
    }

    private void setThemeColor() {
        SharedPreferences data = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        int color = data.getInt(PREF_PARAM_THEME_COLOR, getResources().getColor(R.color.french_gray));
        if (color == getResources().getColor(R.color.topaz)) {
            setTheme(R.style.Theme_ToDoLog_topaz);
        } else if (color == getResources().getColor(R.color.water_green)) {
            setTheme(R.style.Theme_ToDoLog_water_green);
        } else if (color == getResources().getColor(R.color.day_dream)) {
            setTheme(R.style.Theme_ToDoLog_day_dream);
        } else if (color == getResources().getColor(R.color.old_rose)) {
            setTheme(R.style.Theme_ToDoLog_old_rose);
        } else if (color == getResources().getColor(R.color.mauve)) {
            setTheme(R.style.Theme_ToDoLog_mauve);
        } else {
            setTheme(R.style.Theme_ToDoLog_french_gray);
        }
    }
}