package com.highcom.todolog;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.highcom.todolog.datamodel.Log;
import com.highcom.todolog.datamodel.LogViewModel;
import com.highcom.todolog.datamodel.StringsResource;
import com.highcom.todolog.datamodel.ToDo;
import com.highcom.todolog.ui.DividerItemDecoration;
import com.highcom.todolog.ui.loglist.LogListAdapter;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class ToDoDetailActivity extends AppCompatActivity implements TextWatcher {

    private ToDo mBackupToDo;
    private ToDo mEditToDo;
    private EditText mDetailContents;
    private List<Group> mGroupList;

    private LogViewModel mLogViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tododetail);

        setTitle(getString(R.string.detail_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLogViewModel = new ViewModelProvider(this).get(LogViewModel.class);

        Intent intent = getIntent();
        long todoId = intent.getLongExtra("TODO_ID", -1);

        mDetailContents = findViewById(R.id.detail_contents_edit);
        mDetailContents.addTextChangedListener(this);

        // 状態選択スピナーの設定をする
        Spinner statusSpinner = findViewById(R.id.detail_status_spinner);
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (StringsResource.StatusItem item : StringsResource.get().mStatusItems.values()) {
            statusAdapter.add(item.mName);
        }
        statusSpinner.setAdapter(statusAdapter);
        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mEditToDo.setState(StringsResource.get().mStatusItems.get(i).mId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // グループ選択スピナーの設定をする
        Spinner taskGroupSpinner = findViewById(R.id.detail_taskgroup_spinner);
        ArrayAdapter<String> taskGroupAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        taskGroupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskGroupSpinner.setAdapter(taskGroupAdapter);
        taskGroupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mEditToDo.setGroupId(mGroupList.get(i).getGroupId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.detail_log_view);
        LogListAdapter adapter = new LogListAdapter(new LogListAdapter.LogDiff());
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(itemDecoration);

        // ToDoのデータを読み込んで、各エリアにデータをセットする
        mLogViewModel.getToDo(todoId).observe(this, toDo -> {
            if (toDo == null) return;
            mBackupToDo = toDo;
            mEditToDo = toDo.clone();
            mDetailContents.setText(toDo.getContents());
            statusSpinner.setSelection(toDo.getState() - 1);
            // グループリストの作成とToDoデータから現在選択されているデータを設定
            mLogViewModel.getGroupList().observe(this, groupList -> {
                mGroupList = groupList;
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
                break;
            case R.id.detail_done:
                int diffResult = diffCheckEditToDo(mBackupToDo, mEditToDo);
                switch (diffResult) {
                    case Log.LOG_NOCHANGE:
                        break;
                    default:
                        Log log = new Log(0, mEditToDo.getTodoId(), new Date(System.currentTimeMillis()), diffResult);
                        mLogViewModel.updateToDoAndLog(mEditToDo, log);
                        break;
                }
                break;
        }
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        mEditToDo.setContents(editable.toString());
    }

    private int diffCheckEditToDo(ToDo backupToDo, ToDo editTodo) {
        if (backupToDo.getGroupId() != editTodo.getGroupId()) {
            return Log.LOG_CHANGE_GROUP;
        } else if (backupToDo.getState() != editTodo.getState()) {
            if (editTodo.getState() == ToDo.STATUS_TODO) return Log.LOG_CHANGE_STATUS_TODO;
            else return Log.LOG_CHANGE_STATUS_DONE;
        } else if (!backupToDo.getContents().equals(editTodo.getContents())) {
            return Log.LOG_CHANGE_CONTENTS;
        }
        return Log.LOG_NOCHANGE;
    }
}
