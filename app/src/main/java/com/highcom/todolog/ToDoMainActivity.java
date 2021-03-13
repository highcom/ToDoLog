package com.highcom.todolog;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.highcom.todolog.datamodel.Group;
import com.highcom.todolog.datamodel.GroupViewModel;
import com.highcom.todolog.datamodel.StringsResource;
import com.highcom.todolog.ui.grouplist.GroupListFragment;
import com.highcom.todolog.ui.todolist.ToDoListFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import static com.highcom.todolog.ui.todolist.ToDoListFragment.SELECT_GROUP;

public class ToDoMainActivity extends AppCompatActivity {

    private enum SELECT_FRAGMENT {
        FRAGMENT_TODOLIST,
        FRAGMENT_GROUPLIST,
    }
    private SELECT_FRAGMENT selectFragment;
    private FloatingActionButton fab;
    private ToDoListFragment mToDoListFragment;
    private GroupListFragment mGroupListFragment;
    private GroupViewModel mGroupViewModel;
    private SharedPreferences mPreferences;
    private boolean hasBeenFirstGroupHandled;
    private long mSelectGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            switch (selectFragment) {
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
            selectFragment = SELECT_FRAGMENT.FRAGMENT_GROUPLIST;
            getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, mGroupListFragment).commit();
            drawer.closeDrawers();
        });

        mToDoListFragment = new ToDoListFragment();
        selectFragment = SELECT_FRAGMENT.FRAGMENT_TODOLIST;

        ListView listView = findViewById(R.id.task_list_view_inside_nav);
        mGroupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);

        if (savedInstanceState == null) {
            mSelectGroup = mPreferences.getLong(SELECT_GROUP, 0);
            if (mSelectGroup != 0) {
                // 前回選択していたグループを設定する
                Bundle args = new Bundle();
                args.putLong(SELECT_GROUP, mSelectGroup);
                mToDoListFragment.setArguments(args);
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, mToDoListFragment).commit();
            } else {
                // 初期表示のFragmentを設定する
                mGroupViewModel.getFirstGroup().observe(this, firstGroup -> {
                    if (firstGroup == null) return;

                    // この変化通知を受けるのは１度だけ
                    if (hasBeenFirstGroupHandled) return;
                    hasBeenFirstGroupHandled = true;

                    setTitle(firstGroup.getGroupName());
                    mSelectGroup = firstGroup.getGroupId();
                    Bundle args = new Bundle();
                    args.putLong(SELECT_GROUP, mSelectGroup);
                    mToDoListFragment.setArguments(args);
                    getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, mToDoListFragment).commit();
                });
            }
        }
        // Drawerに表示するListを設定する
        mGroupViewModel.getGroupList().observe(this, groupList -> {
            List<String> groupNames = new ArrayList<>();
            for (Group group : groupList) {
                groupNames.add(group.getGroupName());
                // グループ編集以外を選択している場合には、タイトルも更新する
                if (selectFragment == SELECT_FRAGMENT.FRAGMENT_TODOLIST && group.getGroupId() == mSelectGroup) {
                    setTitle(group.getGroupName());
                }
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, groupNames);
            listView.setAdapter(arrayAdapter);
            listView.setOnItemClickListener((adapterView, view, i, l) -> {
                setTitle(groupList.get(i).getGroupName());
                long selectGroup = groupList.get(i).getGroupId();
                mPreferences.edit().putLong(SELECT_GROUP, selectGroup).apply();
                mToDoListFragment = new ToDoListFragment();
                selectFragment = SELECT_FRAGMENT.FRAGMENT_TODOLIST;
                Bundle args = new Bundle();
                args.putLong(SELECT_GROUP, selectGroup);
                mToDoListFragment.setArguments(args);
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, mToDoListFragment).commit();
                drawer.closeDrawers();
            });

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void hideFloatingButton() {
        fab.hide();
    }

    public void showFloatingButton() {
        fab.show();
    }
}