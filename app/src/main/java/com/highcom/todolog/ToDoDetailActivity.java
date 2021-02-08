package com.highcom.todolog;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.highcom.todolog.datamodel.LogViewModel;
import com.highcom.todolog.datamodel.ToDoViewModel;
import com.highcom.todolog.ui.loglist.LogListAdapter;

public class ToDoDetailActivity extends AppCompatActivity {

    private final String[] statusItems = {"ToDo", "Done"};
    private final String[] groupItems = {"TASK1", "TASK2", "TASK3"};

    private LogViewModel mLogViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tododetail);

        setTitle("Detail");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Spinner statusSpinner = findViewById(R.id.detail_status_spinner);
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusAdapter.addAll(statusItems);
        statusSpinner.setAdapter(statusAdapter);
        if (2 > statusSpinner.getCount()) {
            statusSpinner.setSelection(0);
        } else {
            statusSpinner.setSelection(1); // 初期選択位置の設定
        }
        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Spinner taskGroupSpinner = findViewById(R.id.detail_taskgroup_spinner);
        ArrayAdapter<String> taskGroupAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        taskGroupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskGroupAdapter.addAll(groupItems);
        taskGroupSpinner.setAdapter(taskGroupAdapter);
        if (3 > taskGroupSpinner.getCount()) {
            taskGroupSpinner.setSelection(0);
        } else {
            taskGroupSpinner.setSelection(2); // 初期選択位置の設定
        }
        taskGroupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.detail_log_view);
        LogListAdapter adapter = new LogListAdapter(new LogListAdapter.LogDiff());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mLogViewModel = new ViewModelProvider(this).get(LogViewModel.class);
        mLogViewModel.getLogList().observe(this, loglist -> {
            adapter.submitList(loglist);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_detail_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.detail_done:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
