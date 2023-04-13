package com.highcom.todolog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.highcom.todolog.datamodel.ToDoLogRepository;
import com.highcom.todolog.ui.themelist.ThemeColorUtil;
import com.highcom.todolog.util.InputExternalFile;
import com.highcom.todolog.util.OutputExternalFile;
import com.highcom.todolog.util.SelectInputOutputFileDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 設定画面クラス
 * 各設定のイベントリスナーの登録
 * 変更した設定をSharedPreferenceに登録する
 */
public class SettingActivity extends AppCompatActivity implements ThemeColorUtil.ThemeColorListener, SelectInputOutputFileDialog.InputOutputFileDialogListener, InputExternalFile.InputExternalFileListener {

    // アクティビティの結果コード定義
    private static final int DATA_BACKUP = 1001;
    private static final int DATA_RESTORE = 1002;

    // SharedPreferenceの保存ファイル名
    public static final String PREF_FILE_NAME ="com.highcom.ToDoLog.UserData";
    // 起動時のグループ選択表示設定名称
    public static final String PREF_PARAM_START_GROUP ="StartGroup";
    // ToDoの残数表示設定名称
    public static final String PREF_PARAM_TODO_COUNT ="ToDoCount";
    // 新規ToDo追加位置設定名称
    public static final String PREF_PARAM_NEW_TODO_ORDER ="NewToDoOrder";
    // カラーテーマの色設定名称
    public static final String PREF_PARAM_THEME_COLOR ="ThemeColor";
    // ユーザー設定データ
    private SharedPreferences sharedPreferences;

    /**
     * 各設定項目のイベントリスナーを登録する
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        setThemeColor();
        setContentView(R.layout.activity_setting);

        setTitle(getString(R.string.setting_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 起動時のグループ選択表示のユーザー設定値の取得とイベントリスナーの設定
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch startGroupSwitch = (Switch)findViewById(R.id.start_group_switch);
        boolean startGroup = sharedPreferences.getBoolean(PREF_PARAM_START_GROUP, false);
        startGroupSwitch.setChecked(startGroup);
        startGroupSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(PREF_PARAM_START_GROUP, isChecked);
                editor.apply();
            }
        });

        // ToDoの残数表示状態のユーザー設定値の取得とイベントリスナーの設定
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch todoCountSwitch = (Switch)findViewById(R.id.todo_count_switch);
        boolean todoCount = sharedPreferences.getBoolean(PREF_PARAM_TODO_COUNT, true);
        todoCountSwitch.setChecked(todoCount);
        todoCountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(PREF_PARAM_TODO_COUNT, isChecked);
                editor.apply();
            }
        });

        // 新規ToDo追加位置の設定
        TextView newToDoOrderView = findViewById(R.id.new_todo_order_text);
        newToDoOrderView.setOnClickListener(v -> newToDoOrderDialog());
        int order = sharedPreferences.getInt(PREF_PARAM_NEW_TODO_ORDER, ToDoLogRepository.ORDER_ASC);
        TextView newToDoOrderSelect = findViewById(R.id.new_todo_order_select);
        if (order == ToDoLogRepository.ORDER_DESC) {
            newToDoOrderSelect.setText(getString(R.string.order_top));
        } else {
            newToDoOrderSelect.setText(getString(R.string.order_bottom));
        }

        // カラーテーマ設定
        TextView themeColorTextView = findViewById(R.id.theme_color_text);
        themeColorTextView.setOnClickListener(v -> colorSelectDialog());

        // バックアップと復元
        TextView backupRestoreTextView = findViewById(R.id.backup_restore_text);
        backupRestoreTextView.setOnClickListener(v -> backupRestoreDialog());

        // ライセンス一覧の設定
        TextView licenseTextView = findViewById(R.id.license_text);
        licenseTextView.setOnClickListener(view -> {
            OssLicensesMenuActivity.setActivityTitle(getString(R.string.setting_license));
            startActivity(new Intent(getApplicationContext(), OssLicensesMenuActivity.class));
        });

        // プライバシーポリシーの設定
        TextView privacyPolicyTextView = findViewById(R.id.privacy_policy_text);
        privacyPolicyTextView.setOnClickListener(view -> {
            Uri uri = Uri.parse(getString(R.string.privacy_policy_url));
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            startActivity(intent);
        });

        // このアプリの評価をする設定
        TextView starTextView = findViewById(R.id.star_text);
        starTextView.setOnClickListener(view -> {
            Uri uri = Uri.parse(getString(R.string.star_url));
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            startActivity(intent);
        });
    }

    /**
     * 戻るボタン押下時に設定画面を終了する処理。
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 新規ToDo追加位置ダイアログ
     */
    private void newToDoOrderDialog() {
        final String[] items = {getString(R.string.order_bottom_detail), getString(R.string.order_top_detail)};
        int defaultItem = sharedPreferences.getInt(PREF_PARAM_NEW_TODO_ORDER, ToDoLogRepository.ORDER_ASC);
        final List<Integer> checkedItems = new ArrayList<>();
        checkedItems.add(defaultItem);
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.setting_new_todo_order))
                .setSingleChoiceItems(items, defaultItem, (dialog, which) -> {
                    checkedItems.clear();
                    checkedItems.add(which);
                })
                .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                    if (!checkedItems.isEmpty()) {
                        TextView newToDoOrderSelect = findViewById(R.id.new_todo_order_select);
                        if (checkedItems.get(0) == ToDoLogRepository.ORDER_DESC) {
                            newToDoOrderSelect.setText(getString(R.string.order_top));
                        } else {
                            newToDoOrderSelect.setText(getString(R.string.order_bottom));
                        }
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(PREF_PARAM_NEW_TODO_ORDER, checkedItems.get(0));
                        editor.apply();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    /**
     * テーマカラー選択ダイアログ表示
     */
    private void colorSelectDialog() {
        ThemeColorUtil themeColorUtil = new ThemeColorUtil(getApplicationContext(), this);
        themeColorUtil.createThemeColorDialog(this);
    }

    /**
     * 設定されたカラーテーマに合わせた色変更処理
     * アクションバーをユーザーが設定したカラーテーマに変更する。
     */
    private void setThemeColor() {
        int color = sharedPreferences.getInt(PREF_PARAM_THEME_COLOR, getResources().getColor(R.color.french_gray));
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

    /**
     * 選択したカラーテーマを保存してアプリを再起動する。
     * @param color
     */
    @Override
    public void onSelectColorClicked(int color) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PREF_PARAM_THEME_COLOR, color);
        editor.commit();
        Intent intent = new Intent(this, ToDoMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // 起動しているActivityをすべて削除し、新しいタスクでMainActivityを起動する
        startActivity(intent);
    }

    private void backupRestoreDialog() {
        SelectInputOutputFileDialog selectInputOutputFileDialog = new SelectInputOutputFileDialog(this, this);
        selectInputOutputFileDialog.createOpenFileDialog().show();
    }

    @Override
    public void onSelectOperationClicked(String path) {
        if (path.equals(getString(R.string.data_backup))) {
            confirmOutputSelectFile();
        } else if (path.equals(getString(R.string.data_restore))) {
            confirmInputSelectFile();
        }
    }

    private void confirmOutputSelectFile() {
        String fileName = "ToDoLogDB_" + getNowDateString() + ".zip";

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/zip");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);

        startActivityForResult(intent, DATA_BACKUP);
    }

    private void confirmInputSelectFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        startActivityForResult(intent, DATA_RESTORE);
    }

    private String getNowDateString(){
        Date date = new Date();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(date);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData );

        if (resultCode == Activity.RESULT_OK && resultData.getData() != null) {
            Uri uri = resultData.getData();
            switch (requestCode) {
                case DATA_BACKUP:
                    OutputExternalFile outputExternalFile = new OutputExternalFile(this);
                    outputExternalFile.outputSelectFolder(uri);
                    break;
                case DATA_RESTORE:
                    InputExternalFile inputExternalFile = new InputExternalFile(this, this);
                    inputExternalFile.inputSelectFolder(uri);
                    break;
            }
        }
    }

    @Override
    public void importComplete() {
        Intent intent = new Intent(this, ToDoMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // 起動しているActivityをすべて削除し、新しいタスクでMainActivityを起動する
        startActivity(intent);
    }
}