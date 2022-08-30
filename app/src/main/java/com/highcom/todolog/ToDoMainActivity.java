package com.highcom.todolog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.highcom.todolog.datamodel.Group;
import com.highcom.todolog.datamodel.GroupCount;
import com.highcom.todolog.datamodel.GroupViewModel;
import com.highcom.todolog.datamodel.StringsResource;
import com.highcom.todolog.datamodel.ToDo;
import com.highcom.todolog.ui.AdMobLoader;
import com.highcom.todolog.ui.drawerlist.DrawerListAdapter;
import com.highcom.todolog.ui.drawerlist.DrawerListItem;
import com.highcom.todolog.ui.grouplist.GroupListFragment;
import com.highcom.todolog.ui.todolist.ToDoListFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.lifecycle.ViewModelProvider;

import java.util.Arrays;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import jp.co.recruit_mp.android.rmp_appirater.RmpAppirater;

import static com.highcom.todolog.SettingActivity.PREF_FILE_NAME;
import static com.highcom.todolog.SettingActivity.PREF_PARAM_THEME_COLOR;
import static com.highcom.todolog.SettingActivity.PREF_PARAM_TODO_COUNT;
import static com.highcom.todolog.ui.todolist.ToDoListFragment.SELECT_GROUP;

/**
 * ToDoリスト一覧表示クラス
 * DrawerLayout、ActionBar、FloatingButtonの制御を行う
 * 前回表示していたタスクグループでFragmentを生成する
 */
public class ToDoMainActivity extends AppCompatActivity {

    // Fragmentの種別
    private enum SELECT_FRAGMENT {
        FRAGMENT_TODOLIST,
        FRAGMENT_GROUPLIST,
    }
    private SELECT_FRAGMENT mSelectFragment;
    // ToDoの追加フローティングボタン
    private FloatingActionButton fab;
    // ToDo一覧表示用のフラグメント
    private ToDoListFragment mToDoListFragment;
    // タスクグループ一覧表示用のフラグメント
    private GroupListFragment mGroupListFragment;
    // タスクグループ用のViewModel
    private GroupViewModel mGroupViewModel;
    // ユーザー設定値
    private SharedPreferences mPreferences;
    // 最初のグループが設定されたか
    private boolean hasBeenFirstGroupHandled;
    // 前回選択していたGroup
    private long mSelectGroup;
    private String mSelectGroupName;
    // メニューアイコン表示設定
    private boolean mMenuVisible;
    // 残ToDo数の表示設定
    private boolean mTodoCount;

    private FirebaseAnalytics mFirebaseAnalytics;
    private AdMobLoader mAdMobLoader;

    /**
     * ToDoリスト一覧画面の初期設定
     * 最初にFirebase、Admobのロード、アプリ評価ダイアログの初期設定を行う。
     * 前回選択されていたグループを取得してFragmentを生成する。
     * DrawerLayoutにグループの一覧情報を設定する。
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setThemeNoActionBarColor();
        setContentView(R.layout.activity_main);

        mMenuVisible = true;

        // Firebaseの初期設定
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) { }
        });
        MobileAds.setRequestConfiguration(
                new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList(getString(R.string.admob_test_device))).build());
        // AdMobをロードする
        mAdMobLoader = new AdMobLoader(this, findViewById(R.id.ad_view_frame), getString(R.string.admob_unit_id));
        mAdMobLoader.load();

        // 一定基準を満たしたらアプリ評価ダイアログを表示
        RmpAppirater.appLaunched(this,
            (appLaunchCount, appThisVersionCodeLaunchCount, firstLaunchDate, appVersionCode, previousAppVersionCode, rateClickDate, reminderClickDate, doNotShowAgain) -> {
                // 現在のアプリのバージョンで10回以上起動したか
                if (appThisVersionCodeLaunchCount < 10) {
                    return false;
                }
                // ダイアログで「いいえ」を選択していないか
                if (doNotShowAgain) {
                    return false;
                }
                // ユーザーがまだ評価していないか
                if (rateClickDate != null) {
                    return false;
                }
                // ユーザーがまだ「あとで」を選択していないか
                if (reminderClickDate != null) {
                    // 「あとで」を選択してから5日以上経過しているか
                    long prevtime = reminderClickDate.getTime();
                    long nowtime = new Date().getTime();
                    long diffDays = (nowtime - prevtime) / (1000 * 60 * 60 * 24);
                    if (diffDays < 5) {
                        return false;
                    }
                }

                return true;
            }
        );

        // ユーザー設定値取得
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        StringsResource.create(this);

        hasBeenFirstGroupHandled = false;

        // DrawerLayoutのヘッダにアプリのバージョンを設定する
        TextView headerTitle = findViewById(R.id.nav_header_title);
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
            headerTitle.setText(getString(R.string.nav_header_title) + " Ver " + info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // ツールバーに設定しているボタンの各種イベントリスナー設定
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new FloatingButtonEditClickListener());
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ToDoActionBarDrawerToggle toDoActionBarDrawerToggle = new ToDoActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.addDrawerListener(toDoActionBarDrawerToggle);
        toDoActionBarDrawerToggle.syncState();

        // DrawlerLayoutの内容を設定
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> false);

        // グループリスト編集ボタンのイベントリスナー設定
        Button groupEditButton = findViewById(R.id.group_edit_button);
        groupEditButton.setOnClickListener(view -> {
            mGroupListFragment = new GroupListFragment();
            mSelectFragment = SELECT_FRAGMENT.FRAGMENT_GROUPLIST;
            mMenuVisible = false;
            invalidateOptionsMenu();
            drawer.closeDrawers();
        });

        ListView listView = findViewById(R.id.task_list_view_inside_nav);
        mGroupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);

        // 初期表示のFragmentを設定する
        mGroupViewModel.getGroupList().observe(this, groupList -> {
            // グループが1つも存在しない場合にはタスクリスト編集に移る
            if (groupList.size() == 0) {
                setTitle(R.string.group_edit_title);
                mGroupListFragment = new GroupListFragment();
                mSelectFragment = SELECT_FRAGMENT.FRAGMENT_GROUPLIST;
                mMenuVisible = false;
                invalidateOptionsMenu();
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, mGroupListFragment).commit();
                return;
            }

            // この変化通知を受けるのは１度だけ
            if (hasBeenFirstGroupHandled) return;
            hasBeenFirstGroupHandled = true;

            // 初めて起動時はgroupListが空で来た後、サンプルデータが作成されるのでToDoList選択状態にしたい
            // グループが１つで名前が空の場合は初めて起動時ではなく、グループを全削除した後の新規追加時の登録なので何もしない
            if (groupList.size() == 1 && groupList.get(0).getGroupName().equals("")) return;

            mToDoListFragment = new ToDoListFragment();
            mSelectFragment = SELECT_FRAGMENT.FRAGMENT_TODOLIST;
            mMenuVisible = true;
            invalidateOptionsMenu();

            // 前回選択されていたグループに応じた初期画面データ設定
            mSelectGroup = mPreferences.getLong(SELECT_GROUP, 0);
            boolean isGroupExist = false;
            for (Group group : groupList) if (group.getGroupId() == mSelectGroup) isGroupExist = true;
            if (isGroupExist) {
                // 前回選択していたグループを設定する
                Bundle args = new Bundle();
                args.putLong(SELECT_GROUP, mSelectGroup);
                mToDoListFragment.setArguments(args);
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, mToDoListFragment).commit();
            } else {
                // 前回選択していたグループが存在していない場合は最初のグループを選択する
                setTitle(groupList.get(0).getGroupName());
                mSelectGroup = groupList.get(0).getGroupId();
                Bundle args = new Bundle();
                args.putLong(SELECT_GROUP, mSelectGroup);
                mToDoListFragment.setArguments(args);
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, mToDoListFragment).commit();
            }
        });
        // Drawerに表示するListを設定する
        mGroupViewModel.getGroupList().observe(this, groupList -> {
            ArrayList<DrawerListItem> drawerListItems = new ArrayList<>();
            for (Group group : groupList) {
                drawerListItems.add(new DrawerListItem(group.getGroupId(), group.getGroupName()));
                // グループ編集以外を選択している場合には、タイトルも更新する
                if (mSelectFragment == SELECT_FRAGMENT.FRAGMENT_TODOLIST && group.getGroupId() == mSelectGroup) {
                    mSelectGroupName = group.getGroupName();
                    setTitle(mSelectGroupName);
                }
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
                // ドロワーリストを設定する
                DrawerListAdapter adapter = new DrawerListAdapter(this, R.layout.row_drawerlist, drawerListItems);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener((adapterView, view, i, l) -> {
                    mSelectGroup = groupList.get(i).getGroupId();
                    mSelectGroupName = groupList.get(i).getGroupName();
                    mPreferences.edit().putLong(SELECT_GROUP, mSelectGroup).apply();
                    mToDoListFragment = new ToDoListFragment();
                    mSelectFragment = SELECT_FRAGMENT.FRAGMENT_TODOLIST;
                    mMenuVisible = true;
                    invalidateOptionsMenu();
                    Bundle args = new Bundle();
                    args.putLong(SELECT_GROUP, mSelectGroup);
                    mToDoListFragment.setArguments(args);
                    drawer.closeDrawers();
                });
            });
        });
    }

    /**
     * 設定画面で変更された残ToDo数の表示設定を取得し、DrawerLayoutの表示を更新する。
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
     * メニューのドロップダウンリストのinflateを行う。
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * メニューのドロップダウンリストの表示設定。
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.setGroupVisible(R.id.change_all_group, mMenuVisible);
        return true;
    }

    /**
     * メニュー選択時の処理
     * 現在表示しているグループのToDo全てに対して、ToDoのステータスを更新する。
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                Intent settingIntent = new Intent(this, SettingActivity.class);
                startActivity(settingIntent);
                break;
            case R.id.action_change_all_done:
                mGroupViewModel.updateAllToDoByGroupToState(mSelectGroup, ToDo.STATUS_DONE);
                break;
            case R.id.action_change_all_todo:
                mGroupViewModel.updateAllToDoByGroupToState(mSelectGroup, ToDo.STATUS_TODO);
                break;
            case R.id.action_show_log_chart:
                Intent logChartIntent = new Intent(this, LogChartActivity.class);
                logChartIntent.putExtra("GROUP_ID", mSelectGroup);
                logChartIntent.putExtra("GROUP_NAME", mSelectGroupName);
                startActivity(logChartIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * フローティングボタンをToDo記入完了ボタンに設定する。
     */
    public void changeDoneFloatingButton() {
        mAdMobLoader.getAdView().setVisibility(AdView.GONE);
        fab.setImageResource(R.drawable.ic_baseline_check_24);
        fab.setOnClickListener(new FloatingButtonDoneClickListener());
    }

    /**
     * フローティングボタンをToDo記入開始ボタンに設定する。
     */
    public void changeEditFloatingButton() {
        mAdMobLoader.getAdView().setVisibility(AdView.VISIBLE);
        fab.setImageResource(R.drawable.ic_new_edit);
        fab.setOnClickListener(new FloatingButtonEditClickListener());
    }

    /**
     * admobを終了させる。
     */
    @Override
    protected void onDestroy() {
        mAdMobLoader.getAdView().destroy();
        super.onDestroy();
    }

    /**
     * 設定されたカラーテーマに合わせた色変更処理
     * アクションバーをユーザーが設定したカラーテーマに変更する。
     */
    private void setThemeNoActionBarColor() {
        SharedPreferences data = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        int color = data.getInt(PREF_PARAM_THEME_COLOR, getResources().getColor(R.color.french_gray));
        if (color == getResources().getColor(R.color.topaz)) {
            setTheme(R.style.Theme_ToDoLog_topaz_NoActionBar_topaz);
        } else if (color == getResources().getColor(R.color.water_green)) {
            setTheme(R.style.Theme_ToDoLog_water_green_NoActionBar_water_green);
        } else if (color == getResources().getColor(R.color.day_dream)) {
            setTheme(R.style.Theme_ToDoLog_day_dream_NoActionBar_day_dream);
        } else if (color == getResources().getColor(R.color.old_rose)) {
            setTheme(R.style.Theme_ToDoLog_old_rose_NoActionBar_old_rose);
        } else if (color == getResources().getColor(R.color.mauve)) {
            setTheme(R.style.Theme_ToDoLog_mauve_NoActionBar_mauve);
        } else {
            setTheme(R.style.Theme_ToDoLog_french_gray_NoActionBar_french_gray);
        }
    }

    /**
     * ドロワーの開閉時の処理を定義するクラス
     */
    private class ToDoActionBarDrawerToggle extends ActionBarDrawerToggle {

        public ToDoActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
            super(activity, drawerLayout, toolbar, openDrawerContentDescRes, closeDrawerContentDescRes);
        }

        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            super.onDrawerSlide(drawerView, slideOffset);
        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {
            super.onDrawerOpened(drawerView);
        }

        /**
         * ドロワーを選択して閉じた後にFragmentの更新を行う
         *
         * @param drawerView
         */
        @Override
        public void onDrawerClosed(@NonNull View drawerView) {
            switch (mSelectFragment) {
                case FRAGMENT_TODOLIST:
                    if (mToDoListFragment != null) {
                        setTitle(mSelectGroupName);
                        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, mToDoListFragment).commit();
                    }
                    break;
                case FRAGMENT_GROUPLIST:
                    if (mGroupListFragment != null) {
                        setTitle(R.string.group_edit_title);
                        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, mGroupListFragment).commit();
                    }
                    break;
                default:
                    break;
            }
            super.onDrawerClosed(drawerView);
        }

        @Override
        public void onDrawerStateChanged(int newState) {
            // 何もしない
        }
    }

    /**
     * フローティングボタンのToDo入力開始イベントリスナー
     */
    public class FloatingButtonEditClickListener implements View.OnClickListener {

        /**
         * 入力された内容をデータベースに登録する。
         * ToDoが入力されていた場合はToDoを追加する。
         * グループ編集が入力されていた場合は、グループを追加する。
         *
         * @param v
         */
        @Override
        public void onClick(View v) {
            switch (mSelectFragment) {
                case FRAGMENT_TODOLIST:
                    if (mToDoListFragment != null) mToDoListFragment.addNewToDoAndLog();
                    break;
                case FRAGMENT_GROUPLIST:
                    if (mGroupListFragment != null) mGroupListFragment.addNewGroup();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * フローティングボタンのToDo入力完了イベントリスナー
     */
    public class FloatingButtonDoneClickListener implements View.OnClickListener {

        /**
         * 入力完了時にEditTextからフォーカスを外す
         * @param v
         */
        @Override
        public void onClick(View v) {
            // フォーカスをfabにすることで内容からフォーカスを外して入力を完了させる
            fab.setFocusable(true);
            fab.setFocusableInTouchMode(true);
            fab.requestFocus();
        }
    }
}