package com.highcom.todolog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.highcom.todolog.ui.themelist.ThemeColorUtil;

/**
 * 設定画面クラス
 * 各設定のイベントリスナーの登録
 * 変更した設定をSharedPreferenceに登録する
 */
public class SettingActivity extends AppCompatActivity implements ThemeColorUtil.ThemeColorListener {

    // SharedPreferenceの保存ファイル名
    public static final String PREF_FILE_NAME ="com.highcom.ToDoLog.UserData";
    // ToDoの残数表示設定名称
    public static final String PREF_PARAM_TODO_COUNT ="ToDoCount";
    // カラーテーマの色設定名称
    public static final String PREF_PARAM_THEME_COLOR ="ThemeColor";

    /**
     * 各設定項目のイベントリスナーを登録する
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setThemeColor();
        setContentView(R.layout.activity_setting);

        setTitle(getString(R.string.setting_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // ToDoの残数表示状態のユーザー設定値の取得とイベントリスナーの設定
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch todoCountSwitch = (Switch)findViewById(R.id.todo_count_switch);
        SharedPreferences data = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        boolean todoCount = data.getBoolean(PREF_PARAM_TODO_COUNT, true);
        todoCountSwitch.setChecked(todoCount);
        todoCountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = data.edit();
                editor.putBoolean(PREF_PARAM_TODO_COUNT, isChecked);
                editor.apply();
            }
        });

        // カラーテーマ設定
        TextView themeColorTextView = findViewById(R.id.theme_color_text);
        themeColorTextView.setOnClickListener(v -> colorSelectDialog());

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

    /**
     * 選択したカラーテーマを保存してアプリを再起動する。
     * @param color
     */
    @Override
    public void onSelectColorClicked(int color) {
        SharedPreferences data = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = data.edit();
        editor.putInt(PREF_PARAM_THEME_COLOR, color);
        editor.commit();
        Intent intent = new Intent(this, ToDoMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // 起動しているActivityをすべて削除し、新しいタスクでMainActivityを起動する
        startActivity(intent);
    }
}