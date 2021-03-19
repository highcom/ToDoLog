package com.highcom.todolog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
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

    private FrameLayout adContainerView;
    private AdView mAdView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tododetail);

        adContainerView = findViewById(R.id.ad_view_frame_tododetail);
        adContainerView.post(() -> loadBanner());

        setTitle(getString(R.string.detail_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLogViewModel = new ViewModelProvider(this).get(LogViewModel.class);

        Intent intent = getIntent();
        long todoId = intent.getLongExtra("TODO_ID", -1);

        mDetailContents = findViewById(R.id.detail_contents_edit);
        mDetailContents.addTextChangedListener(this);
        mDetailContents.setOnClickListener(view -> {
            view.post(() -> {
                view.setFocusable(true);
                view.setFocusableInTouchMode(true);
                view.requestFocus();
                InputMethodManager inputMethodManager = (InputMethodManager)getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.showSoftInput(view, 0);
                    // キーボードを表示する時は広告を非表示「にする
                    mAdView.setVisibility(AdView.GONE);
                }
            });
        });
        mDetailContents.setOnFocusChangeListener((view, b) -> {
            // フォーカスが外れるとキーボードが閉じるので広告を表示する
            if (!b) {
                mAdView.setVisibility(AdView.VISIBLE);
            }
        });

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

    private void loadBanner() {
        // Create an ad request.
        mAdView = new AdView(this);
        mAdView.setAdUnitId(getString(R.string.admob_unit_id_2));
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
    public boolean onTouchEvent(MotionEvent event) {
        // 内容編集中にフォーカスが外れた場合は、キーボードを閉じる
        mDetailContents.setFocusable(false);
        mDetailContents.setFocusableInTouchMode(false);
        mDetailContents.requestFocus();
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
        return super.onTouchEvent(event);
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

    @Override
    protected void onDestroy() {
        mAdView.destroy();
        super.onDestroy();
    }
}
