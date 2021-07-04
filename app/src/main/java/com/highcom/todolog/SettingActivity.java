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

public class SettingActivity extends AppCompatActivity implements ThemeColorUtil.ThemeColorListener {

    public static final String PREF_FILE_NAME ="com.highcom.ToDoLog.UserData";
    public static final String PREF_PARAM_TODO_COUNT ="ToDoCount";
    public static final String PREF_PARAM_THEME_COLOR ="ThemeColor";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setThemeColor();
        setContentView(R.layout.activity_setting);

        setTitle(getString(R.string.setting_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        TextView themeColorTextView = findViewById(R.id.theme_color_text);
        themeColorTextView.setOnClickListener(v -> colorSelectDialog());

        TextView licenseTextView = findViewById(R.id.license_text);
        licenseTextView.setOnClickListener(view -> {
            OssLicensesMenuActivity.setActivityTitle(getString(R.string.setting_license));
            startActivity(new Intent(getApplicationContext(), OssLicensesMenuActivity.class));
        });

        TextView privacyPolicyTextView = findViewById(R.id.privacy_policy_text);
        privacyPolicyTextView.setOnClickListener(view -> {
            Uri uri = Uri.parse(getString(R.string.privacy_policy_url));
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            startActivity(intent);
        });

        TextView starTextView = findViewById(R.id.star_text);
        starTextView.setOnClickListener(view -> {
            Uri uri = Uri.parse(getString(R.string.star_url));
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            startActivity(intent);
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void colorSelectDialog() {
        ThemeColorUtil themeColorUtil = new ThemeColorUtil(getApplicationContext(), this);
        themeColorUtil.createThemeColorDialog(this);
    }

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