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
import com.highcom.todolog.ui.todolist.ToDoListFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LifecycleOwner;

public class ToDoMainActivity extends AppCompatActivity {

    private LifecycleOwner lifecycleOwner = this;
    private ToDoListFragment toDoListFragment;
    private static final String lists[] = { "TASK1", "TASK2", "TASK3"};

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

        // 初期表示のFragmentを設定する
        toDoListFragment = new ToDoListFragment(lifecycleOwner);
        toDoListFragment.setSelectGroup("TASK1");
        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, toDoListFragment).commit();

        ListView listView = findViewById(R.id.task_list_view_inside_nav);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lists);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            toDoListFragment = new ToDoListFragment(lifecycleOwner);
            switch (i) {
                case 0:
                    toDoListFragment.setSelectGroup("TASK1");
                    break;
                case 1:
                    toDoListFragment.setSelectGroup("TASK2");
                    break;
                case 2:
                    toDoListFragment.setSelectGroup("TASK3");
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, toDoListFragment).commit();
            drawer.closeDrawers();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}