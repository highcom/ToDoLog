package com.highcom.todolog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MenuItem;
import android.view.Menu;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.highcom.todolog.datamodel.Group;
import com.highcom.todolog.datamodel.GroupViewModel;
import com.highcom.todolog.datamodel.StringsResource;
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

import jp.co.recruit_mp.android.rmp_appirater.RmpAppirater;

import static com.highcom.todolog.ui.todolist.ToDoListFragment.SELECT_GROUP;

public class ToDoMainActivity extends AppCompatActivity {

    private enum SELECT_FRAGMENT {
        FRAGMENT_TODOLIST,
        FRAGMENT_GROUPLIST,
    }
    private SELECT_FRAGMENT mSelectFragment;
    private FloatingActionButton fab;
    private ToDoListFragment mToDoListFragment;
    private GroupListFragment mGroupListFragment;
    private GroupViewModel mGroupViewModel;
    private SharedPreferences mPreferences;
    private boolean hasBeenFirstGroupHandled;
    private long mSelectGroup;

    private FirebaseAnalytics mFirebaseAnalytics;
    private FrameLayout adContainerView;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_ToDoLog_NoActionBar);
        setContentView(R.layout.activity_main);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) { }
        });
        MobileAds.setRequestConfiguration(
                new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("874848BA4D9A6B9B0A256F7862A47A31")).build());
        adContainerView = findViewById(R.id.ad_view_frame);
        adContainerView.post(() -> loadBanner());

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

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        StringsResource.create(this);

        hasBeenFirstGroupHandled = false;

        TextView headerTitle = findViewById(R.id.nav_header_title);
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
            headerTitle.setText(getString(R.string.nav_header_title) + " Ver " + info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
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
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle =
                new ActionBarDrawerToggle(
                        this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return false;
            }
        });

        Button groupEditButton = findViewById(R.id.group_edit_button);
        groupEditButton.setOnClickListener(view -> {
            setTitle(R.string.group_edit_title);
            mGroupListFragment = new GroupListFragment();
            mSelectFragment = SELECT_FRAGMENT.FRAGMENT_GROUPLIST;
            getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, mGroupListFragment).commit();
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
            ArrayList<DrawerListItem> drawerListItem = new ArrayList<>();
            for (Group group : groupList) {
                drawerListItem.add(new DrawerListItem(group.getGroupName()));
                // グループ編集以外を選択している場合には、タイトルも更新する
                if (mSelectFragment == SELECT_FRAGMENT.FRAGMENT_TODOLIST && group.getGroupId() == mSelectGroup) {
                    setTitle(group.getGroupName());
                }
            }
            DrawerListAdapter adapter = new DrawerListAdapter(this, R.layout.row_drawerlist, drawerListItem);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener((adapterView, view, i, l) -> {
                setTitle(groupList.get(i).getGroupName());
                long selectGroup = groupList.get(i).getGroupId();
                mPreferences.edit().putLong(SELECT_GROUP, selectGroup).apply();
                mToDoListFragment = new ToDoListFragment();
                mSelectFragment = SELECT_FRAGMENT.FRAGMENT_TODOLIST;
                Bundle args = new Bundle();
                args.putLong(SELECT_GROUP, selectGroup);
                mToDoListFragment.setArguments(args);
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, mToDoListFragment).commit();
                drawer.closeDrawers();
            });
        });
    }

    private void loadBanner() {
        // Create an ad request.
        mAdView = new AdView(this);
        mAdView.setAdUnitId(getString(R.string.admob_unit_id));
        adContainerView.removeAllViews();
        adContainerView.addView(mAdView);

        AdSize adSize = getAdSize();
        mAdView.setAdSize(adSize);

        AdRequest adRequest = new AdRequest.Builder().addTestDevice(getString(R.string.admob_test_device)).build();

        // Start loading the ad in the background.
        mAdView.loadAd(adRequest);
    }

    private AdSize getAdSize() {
        // Determine the screen width (less decorations) to use for the ad width.
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = outMetrics.density;

        float adWidthPixels = adContainerView.getWidth();

        // If the ad hasn't been laid out, default to the full screen width.
        if (adWidthPixels == 0) {
            adWidthPixels = outMetrics.widthPixels;
        }

        int adWidth = (int) (adWidthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void hideFloatingButton() {
        mAdView.setVisibility(AdView.GONE);
        fab.hide();
    }

    public void showFloatingButton() {
        mAdView.setVisibility(AdView.VISIBLE);
        fab.show();
    }

    @Override
    protected void onDestroy() {
        mAdView.destroy();
        super.onDestroy();
    }
}