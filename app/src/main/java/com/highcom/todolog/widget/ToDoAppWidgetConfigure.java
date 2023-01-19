package com.highcom.todolog.widget;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
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

/**
 * ウィジェットに表示するグループ選択画面クラス
 * ウィジェットをホーム画面に配置操作後にグループ一覧画面を表示する。
 */
public class ToDoAppWidgetConfigure extends AppCompatActivity {

    // ウィジェット用に選択したグループID
    public static final String SELECT_WIDGET_GROUP_ID = "selectWidgetGroupId";
    // ウィジェットように選択したグループ名
    public static final String SELECT_WIDGET_GROUP_NAME = "selectWidgetGroupName";
    // ホームに配置したウィジェットの識別ID
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    // グループ一覧表示用ViewModel
    GroupViewModel mGroupViewModel;
    // ToDo残数表示設定
    private boolean mTodoCount;

    /**
     * ウィジェット表示用グループ選択画面初期処理。
     * グループ一覧を取得して一覧表示する。
     * 選択したグループIDのウィジェットに渡すイベントリスナーを定義する。
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setThemeColor();
        setContentView(R.layout.activity_to_do_app_widget_configure);
        setTitle(getString(R.string.group_select));

        // ウィジェットIDを取得する
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

        // ListViewにグループ一覧情報を設定する
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

                // 選択したグループIDとグループ名を渡してウィジェットを生成する
                listView.setOnItemClickListener((adapterView, view, i, l) -> {
                    long selectGroupId = groupList.get(i).getGroupId();
                    String selectGroupName = groupList.get(i).getGroupName();
                    saveSelectWidgetGroupPref(getApplicationContext(), mAppWidgetId, selectGroupId, selectGroupName);
                    int layoutId = R.layout.todo_appwidget;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        // SDKのバージョンがR以降である場合にダークモード設定が導入されたため、それを判定する
                        if (getApplicationContext().getTheme().getResources().getConfiguration().isNightModeActive()) {
                            // ダークモードの場合はダークモード用の色設定にする
                            layoutId = R.layout.todo_appwidget_dark;
                        }
                    }
                    RemoteViews views = new RemoteViews(getPackageName(), layoutId);
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

    /**
     * 設定画面で変更された残ToDo数の表示設定を取得し、ListViewの表示を更新する。
     */
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

    /**
     * ウィジェットID事の選択したグループ保存処理
     * ウィジェットIDをキーにして、選択したグループIDとグループ名をSharedPreferenceに保存する。
     *
     * @param context グループ選択画面のコンテキスト
     * @param appWidgetId 操作対象のウィジェットID
     * @param selectGroupId 選択グループID
     * @param selectGroupName 選択グループ名
     */
    static void saveSelectWidgetGroupPref(Context context, int appWidgetId, long selectGroupId, String selectGroupName) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putLong(SELECT_WIDGET_GROUP_ID + appWidgetId, selectGroupId).apply();
        prefs.edit().putString(SELECT_WIDGET_GROUP_NAME + appWidgetId, selectGroupName).apply();
    }

    /**
     * ウィジェットIDに対応する選択グループID取得処理
     * SharedPreferenceからウィジェットIDに対応するグループIDを取得する。
     * ウィジェットが配置された際にToDoWidgetRemoteViewsFactoryのウィジェットのデータ変更イベントから呼び出される。
     *
     * @param context ウィジェット画面のコンテキスト
     * @param appWidgetId ウィジェットID
     * @return グループID
     */
    static long loadSelectWidgetGroupIdPref(Context context, int appWidgetId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long groupId = prefs.getLong(SELECT_WIDGET_GROUP_ID + appWidgetId, -1);
        return groupId;
    }

    /**
     * ウィジェットIDに対応するグループ名取得処理
     * SharedPreferenceからウィジェットIDに対応するグループ名を取得する。
     * ウィジェットが配置された際にToDoAppWidgetProviderのウィジェットの更新イベントから呼び出される。
     *
     * @param context ウィジェット画面のコンテキスト
     * @param appWidgetId ウィジェットID
     * @return グループ名
     */
    static String loadSelectWidgetGroupNamePref(Context context, int appWidgetId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String groupName = prefs.getString(SELECT_WIDGET_GROUP_NAME + appWidgetId, "");
        return groupName;
    }

    /**
     * ウィジェットIDに対応するグループ情報削除処理
     * SharedPreferenceからウィジェットIDに対応するグループ情報を削除する。
     * ToDoWidgetRemoteViewsFactoryのウィジェット破棄イベントから呼び出される。
     *
     * @param context ウィジェット画面のコンテキスト
     * @param appWidgetId ウィジェットID
     */
    static void deleteSelectWidgetGroupPref(Context context, int appWidgetId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().remove(SELECT_WIDGET_GROUP_ID + appWidgetId).apply();
        prefs.edit().remove(SELECT_WIDGET_GROUP_NAME + appWidgetId).apply();
    }

    /**
     * 設定されたカラーテーマに合わせた色変更処理
     * アクションバーをユーザーが設定したカラーテーマに変更する。
     */
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