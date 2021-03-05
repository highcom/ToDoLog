package com.highcom.todolog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

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
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import static com.highcom.todolog.ui.todolist.ToDoListFragment.SELECT_GROUP;

public class ToDoMainActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private ToDoListFragment toDoListFragment;
    private GroupViewModel mGroupViewModel;
    private SharedPreferences mPreferences;
    private int mSelectGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        StringsResource.create(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> toDoListFragment.addNewToDoAndLog());
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
            GroupListFragment groupListFragment = new GroupListFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, groupListFragment).commit();
            drawer.closeDrawers();
        });

        toDoListFragment = new ToDoListFragment();

        ListView listView = findViewById(R.id.task_list_view_inside_nav);
        mGroupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);

        if (savedInstanceState == null) {
            mSelectGroup = mPreferences.getInt(SELECT_GROUP, 0);
            if (mSelectGroup != 0) {
                // 前回選択していたグループを設定する
                Bundle args = new Bundle();
                args.putInt(SELECT_GROUP, mSelectGroup);
                toDoListFragment.setArguments(args);
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, toDoListFragment).commit();
            } else {
                // 初期表示のFragmentを設定する
                mGroupViewModel.getFirstGroup().observe(this, firstGroup -> {
                    if (firstGroup == null) return;
                    mSelectGroup = firstGroup.getGroupId();
                    Bundle args = new Bundle();
                    args.putInt(SELECT_GROUP, mSelectGroup);
                    toDoListFragment.setArguments(args);
                    getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, toDoListFragment).commit();
                });
            }
        }
        // Drawerに表示するListを設定する
        mGroupViewModel.getGroupList().observe(this, groupList -> {
            List<String> groupNames = new ArrayList<>();
            for (Group group : groupList) {
                groupNames.add(group.getGroupName());
                if (group.getGroupId() == mSelectGroup) {
                    setTitle(group.getGroupName());
                }
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, groupNames);
            listView.setAdapter(arrayAdapter);
            listView.setOnItemClickListener((adapterView, view, i, l) -> {
                setTitle(groupList.get(i).getGroupName());
                int selectGroup = groupList.get(i).getGroupId();
                mPreferences.edit().putInt(SELECT_GROUP, selectGroup).apply();
                toDoListFragment = new ToDoListFragment();
                Bundle args = new Bundle();
                args.putInt(SELECT_GROUP, selectGroup);
                toDoListFragment.setArguments(args);
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, toDoListFragment).commit();
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