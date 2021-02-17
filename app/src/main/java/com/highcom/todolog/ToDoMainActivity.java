package com.highcom.todolog;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.highcom.todolog.datamodel.Group;
import com.highcom.todolog.datamodel.GroupViewModel;
import com.highcom.todolog.datamodel.ToDoViewModel;
import com.highcom.todolog.ui.todolist.ToDoListFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

public class ToDoMainActivity extends AppCompatActivity {

    private LifecycleOwner lifecycleOwner = this;
    private ToDoListFragment toDoListFragment;
    private GroupViewModel mGroupViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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

        toDoListFragment = new ToDoListFragment(lifecycleOwner);

        ListView listView = findViewById(R.id.task_list_view_inside_nav);
        mGroupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);

        // 初期表示のFragmentを設定する
        mGroupViewModel.getFirstGroup().observe(this, firstGroup -> {
            toDoListFragment.setSelectGroup(firstGroup.getGroupId());
            getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, toDoListFragment).commit();
        });
        // Drawerに表示するListを瀬亭する
        mGroupViewModel.getGroupList().observe(this, groupList -> {
            List<String> groupNames = new ArrayList<>();
            for (Group group : groupList) groupNames.add(group.getGroupName());
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, groupNames);
            listView.setAdapter(arrayAdapter);
            listView.setOnItemClickListener((adapterView, view, i, l) -> {
                toDoListFragment = new ToDoListFragment(lifecycleOwner);
                toDoListFragment.setSelectGroup(groupList.get(i).getGroupId());
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
}