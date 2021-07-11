package com.highcom.todolog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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

import com.google.android.gms.ads.AdView;
import com.highcom.todolog.datamodel.Group;
import com.highcom.todolog.datamodel.Log;
import com.highcom.todolog.datamodel.LogViewModel;
import com.highcom.todolog.datamodel.StringsResource;
import com.highcom.todolog.datamodel.ToDo;
import com.highcom.todolog.ui.AdMobLoader;
import com.highcom.todolog.ui.DividerItemDecoration;
import com.highcom.todolog.ui.loglist.LogListAdapter;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import static com.highcom.todolog.SettingActivity.PREF_FILE_NAME;
import static com.highcom.todolog.SettingActivity.PREF_PARAM_THEME_COLOR;

/**
 * ToDo詳細画面表示クラス
 * ToDo一覧画面で選択されたToDoの詳細内容について表示する
 */
public class ToDoDetailActivity extends AppCompatActivity implements TextWatcher {

    // 変更内容比較のための変更前のToDoの内容
    private ToDo mBackupToDo;
    // 変更中のToDoの内容
    private ToDo mEditToDo;
    // ToDoの内容
    private EditText mDetailContents;
    // タスクグループ一覧
    private List<Group> mGroupList;
    // ログデータ用のViewModel
    private LogViewModel mLogViewModel;

    AdMobLoader mAdMobLoader;

    /**
     * ToDo詳細画面の初期設定
     * IntentでToDo詳細を表示するIDをもらう。
     * ToDoの内容とToDoに紐づくLog一覧を取得してRecyclerViewに表示する。
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setThemeColor();
        setContentView(R.layout.activity_tododetail);

        // AdMobのロード
        mAdMobLoader = new AdMobLoader(this, findViewById(R.id.ad_view_frame_tododetail), getString(R.string.admob_unit_id_2));
        mAdMobLoader.load();

        // タイトルの設定
        setTitle(getString(R.string.detail_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLogViewModel = new ViewModelProvider(this).get(LogViewModel.class);

        // ToDo一覧画面で選択されてToDoのIDを取得する
        Intent intent = getIntent();
        long todoId = intent.getLongExtra("TODO_ID", -1);

        // ToDoの内容を選択された場合のイベントリスナー設定
        mDetailContents = findViewById(R.id.detail_contents_edit);
        mDetailContents.addTextChangedListener(this);
        mDetailContents.setOnClickListener(view -> {
            // キーボードを確実に表示させるために、スレッドで処理を実行する
            view.post(() -> {
                view.setFocusable(true);
                view.setFocusableInTouchMode(true);
                view.requestFocus();
                InputMethodManager inputMethodManager = (InputMethodManager)getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.showSoftInput(view, 0);
                    // キーボードを表示する時は広告を非表示「にする
                    mAdMobLoader.getAdView().setVisibility(AdView.GONE);
                }
            });
        });
        mDetailContents.setOnFocusChangeListener((view, b) -> {
            // フォーカスが外れるとキーボードが閉じるので広告を表示する
            if (!b) {
                mAdMobLoader.getAdView().setVisibility(AdView.VISIBLE);
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

        // Log一覧を表示するためのRecyclerViewの初期設定
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

    /**
     * ToDo内容入力状態でのタッチイベント処理。
     * ToDo内容入力エリア以外の部分をタッチされた場合に、入力を終了する
     *
     * @param event
     * @return
     */
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

    /**
     * 入力完了のメニュー作成処理
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_detail_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * アイテム選択時の処理
     * 戻るメニューが押された場合には、データ更新せずに終了する。
     * 完了メニューが選択された場合は、変更があればデータを更新する。
     *
     * @param item
     * @return
     */
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

    /**
     * ToDo内容変更処理
     * 変更された内容で変更中のToDO内容を更新する
     *
     * @param editable
     */
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
    private void setThemeColor() {
        SharedPreferences data = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        int color = data.getInt(PREF_PARAM_THEME_COLOR, getResources().getColor(R.color.french_gray));
        if (color == getResources().getColor(R.color.topaz)) {
            setTheme(R.style.Theme_ToDoLog_topaz);
        } else if (color == getResources().getColor(R.color.water_green)) {
            setTheme(R.style.Theme_ToDoLog_water_green);
        } else if (color == getResources().getColor(R.color.day_dream)) {
            setTheme(R.style.Theme_ToDoLog_day_dream);
        } else if (color == getResources().getColor(R.color.old_rose)) {
            setTheme(R.style.Theme_ToDoLog_old_rose);
        } else if (color == getResources().getColor(R.color.mauve)) {
            setTheme(R.style.Theme_ToDoLog_mauve);
        } else {
            setTheme(R.style.Theme_ToDoLog_french_gray);
        }
    }
}
