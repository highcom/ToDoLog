package com.highcom.todolog;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.highcom.todolog.datamodel.Group;
import com.highcom.todolog.datamodel.GroupViewModel;
import com.highcom.todolog.datamodel.LogViewModel;
import com.highcom.todolog.ui.loglist.LogListAdapter;

import java.util.ArrayList;
import java.util.List;

public class ToDoDetailActivity extends AppCompatActivity {

    private EditText detailContents;
    private final String[] statusItems = {"ToDo", "Done"};

    private LogViewModel mLogViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tododetail);

        setTitle(getString(R.string.detail_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLogViewModel = new ViewModelProvider(this).get(LogViewModel.class);

        Intent intent = getIntent();
        int todoId = intent.getIntExtra("TODO_ID", -1);

        detailContents = findViewById(R.id.detail_contents_edit);

        Spinner statusSpinner = findViewById(R.id.detail_status_spinner);
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusAdapter.addAll(statusItems);
        statusSpinner.setAdapter(statusAdapter);
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
        taskGroupSpinner.setAdapter(taskGroupAdapter);
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

        // ToDoのデータを読み込んで、各エリアにデータをセットする
        mLogViewModel.getToDo(todoId).observe(this, toDo -> {
            detailContents.setText(toDo.getContents());
            statusSpinner.setSelection(toDo.getState() - 1);
            mLogViewModel.getGroupList().observe(this, groupList -> {
                List<String> groupNames = new ArrayList<>();
                for (Group group : groupList) groupNames.add(group.getGroupName());
                taskGroupAdapter.addAll(groupNames);
                for (int num = 0; num < groupList.size(); num++) {
                    if (groupList.get(num).getGroupId() == toDo.getGroupId()) {
                        taskGroupSpinner.setSelection(num);
                        break;
                    }
                }
            });
        });

        // 選択されたToDoに対応するLogを出すようにする
        mLogViewModel.getLogListByTodoId(todoId).observe(this, logList -> {
            adapter.submitList(logList);
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
