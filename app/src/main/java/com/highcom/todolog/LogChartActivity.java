package com.highcom.todolog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.highcom.todolog.datamodel.DoneCount;
import com.highcom.todolog.datamodel.Log;
import com.highcom.todolog.datamodel.LogCount;
import com.highcom.todolog.datamodel.LogViewModel;
import com.highcom.todolog.ui.AdMobLoader;
import com.highcom.todolog.ui.chartitem.BarChartItem;
import com.highcom.todolog.ui.chartitem.ChartItem;
import com.highcom.todolog.ui.chartitem.LineChartItem;
import com.highcom.todolog.ui.chartitem.PieChartItem;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static com.highcom.todolog.SettingActivity.PREF_FILE_NAME;
import static com.highcom.todolog.SettingActivity.PREF_PARAM_THEME_COLOR;

/**
 * 操作ログをグラフで表示するクラス
 * LineChart、BarChartで完了ToDo数をグラフで表示する
 * PieChartで操作ログ種別毎の割合を表示する
 */
public class LogChartActivity extends AppCompatActivity {
    private long mGroupId;
    private String mGroupName;
    // ログデータ用のViewModel
    private LogViewModel mLogViewModel;
    // チャート表示用アダプタ
    ChartDataAdapter mCda;

    AdMobLoader mAdMobLoader;

    /**
     * 操作ログをグラフで表示する画面の初期設定
     * 選択されているグループの操作ログ情報を取得して各種Chartに設定する。
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setThemeColor();
        setContentView(R.layout.activity_log_chart);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // AdMobのロード
        mAdMobLoader = new AdMobLoader(this, findViewById(R.id.ad_view_frame_logchart), getString(R.string.admob_unit_id_3));
        mAdMobLoader.load();

        ListView lv = findViewById(R.id.logChartListView);

        Intent intent = getIntent();
        mGroupId = intent.getLongExtra("GROUP_ID", -1);
        mGroupName = intent.getStringExtra("GROUP_NAME");
        if (mGroupName != null) setTitle(mGroupName);

        ArrayList<ChartItem> list = new ArrayList<>();

        // 選択グループがある場合には対応するデータを取得する
        if (mGroupId != -1) {
            mLogViewModel = new ViewModelProvider(this).get(LogViewModel.class);

            mLogViewModel.getDoneCountByLogDate(mGroupId).observe(this, doneCounts -> {
                List<Date> dateRange = createDateRange(doneCounts);
                HashMap<Date, Integer> dateHashMap = summarizeDateCount(doneCounts);
                list.add(new LineChartItem(dateRange, generateDataLine(dateRange, dateHashMap), getApplicationContext()));
                list.add(new BarChartItem(dateRange, generateDataBar(dateRange, dateHashMap), getApplicationContext()));
                lv.setAdapter(mCda);

                // 最後にPieChartを表示する
                mLogViewModel.getCountByLogOperation(mGroupId).observe(this, logCounts -> {
                    list.add(new PieChartItem(generateDataPie(logCounts), getApplicationContext()));
                    lv.setAdapter(mCda);
                });
            });
        }

        mCda = new ChartDataAdapter(getApplicationContext(), list);
        lv.setAdapter(mCda);
    }

    /**
     * メニューのドロップダウンリストのinflateを行う。
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * メニューのドロップダウンリストの表示設定。
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.setGroupVisible(R.id.change_all_group, false);
        return true;
    }

    /**
     * メニュー選択時の処理
     * 現在表示しているグループのToDo全てに対して、ToDoのステータスを更新する。
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
            case R.id.action_setting:
                Intent settingIntent = new Intent(this, SettingActivity.class);
                startActivity(settingIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * admobを終了させる。
     */
    @Override
    protected void onDestroy() {
        mAdMobLoader.getAdView().destroy();
        super.onDestroy();
    }

    /** adapter that supports 3 different item types */
    private class ChartDataAdapter extends ArrayAdapter<ChartItem> {

        ChartDataAdapter(Context context, List<ChartItem> objects) {
            super(context, 0, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            //noinspection ConstantConditions
            return getItem(position).getView(position, convertView, getContext());
        }

        @Override
        public int getItemViewType(int position) {
            // return the views type
            ChartItem ci = getItem(position);
            return ci != null ? ci.getItemType() : 0;
        }

        @Override
        public int getViewTypeCount() {
            return 3; // we have 3 different item-types
        }
    }

    /**
     * 与えられた日時の時分秒を切り捨てて返却する
     *
     * @param date 日時
     * @return 時分秒を切り捨てた日時
     */
    private Date adjustDate(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return new Date(c.getTime().getTime());
    }

    /**
     * 与えられた完了日時データから、日付の始まりから終わりまでの日付のリストを作成する
     *
     * @param doneCounts 完了日時データ
     * @return 日付のリスト
     */
    private List<Date> createDateRange(List<DoneCount> doneCounts) {
        if (doneCounts.size() == 0) return null;

        List<Date> dateRange = new ArrayList<>();
        Date firstDate = adjustDate(doneCounts.get(0).mDate);
        Date lastDate = adjustDate(doneCounts.get(doneCounts.size() - 1).mDate);
        Calendar c = Calendar.getInstance();
        c.setTime(firstDate);
        // 開始日の1日前からをグラフにする(完了数を0スタートで表示させるため)
        for (c.add(Calendar.DATE, -1); c.getTime().getTime() <= lastDate.getTime(); c.add(Calendar.DATE, 1)) {
            Date date = new Date(c.getTime().getTime());
            dateRange.add(date);
        }

        return dateRange;
    }

    /**
     * 与えられた完了日時データから、日付単位で数をカウントする
     *
     * @param doneCounts 完了日時データ
     * @return 日付単位で完了数をサマリしたデータ
     */
    private HashMap<Date, Integer> summarizeDateCount(List<DoneCount> doneCounts) {
        HashMap<Date, Integer> dateHashMap = new HashMap<>();

        for (DoneCount doneCount : doneCounts) {
            Date date = adjustDate(doneCount.mDate);
            Integer count = dateHashMap.get(date);
            if (count != null) {
                count += doneCount.mDoneCount;
                dateHashMap.put(date, count);
            } else {
                dateHashMap.put(date, doneCount.mDoneCount);
            }
        }

        return dateHashMap;
    }

    /**
     * generates a random ChartData object with just one DataSet
     *
     * @return Line data
     */
    private LineData generateDataLine(List<Date> dateRange, HashMap<Date, Integer> dateHashMap) {

        ArrayList<Entry> values1 = new ArrayList<>();

        if (dateHashMap.size() > 0) {
            int cnt = 0;
            int total = 0;
            for (Date date : dateRange) {
                Integer count = dateHashMap.get(date);
                if (count != null) {
                    total += count;
                }
                values1.add(new Entry(cnt, total));
                cnt++;
            }
        }

        LineDataSet d1 = new LineDataSet(values1, getString(R.string.chart_line_guide));
        d1.setLineWidth(2.5f);
        d1.setCircleRadius(4.5f);
        d1.setHighLightColor(Color.rgb(244, 117, 117));
        d1.setDrawValues(false);

        ArrayList<ILineDataSet> sets = new ArrayList<>();
        sets.add(d1);

        return new LineData(sets);
    }

    /**
     * generates a random ChartData object with just one DataSet
     *
     * @return Bar data
     */
    private BarData generateDataBar(List<Date> dateRange, HashMap<Date, Integer> dateHashMap) {
        ArrayList<BarEntry> entries = new ArrayList<>();

        if (dateHashMap.size() > 0) {
            int cnt = 0;
            for (Date date : dateRange) {
                Integer count = dateHashMap.get(date);
                if (count != null) {
                    entries.add(new BarEntry(cnt, count));
                } else {
                    entries.add(new BarEntry(cnt, 0));
                }
                cnt++;
            }
        }

        BarDataSet d = new BarDataSet(entries, getString(R.string.chart_bar_guide));
        d.setColor(rgb(Log.LOG_CHANGE_STATUS_DONE_COLOR));
        d.setHighLightAlpha(255);
        // Barグラフ上の数値を整数にする
        d.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if ((int) value == 0) {
                    return "";
                } else {
                    return "" + (int) value;
                }
            }
        });

        BarData cd = new BarData(d);
        cd.setBarWidth(0.9f);
        return cd;
    }

    /**
     * generates a random ChartData object with just one DataSet
     *
     * @return Pie data
     */
    private PieData generateDataPie(List<LogCount> logCounts) {

        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        for (LogCount logCount : logCounts) {
            colors.add(getOperationColor(logCount.mOperation));
            entries.add(new PieEntry(logCount.mLogCount, getOperationName(logCount.mOperation)));
        }

        PieDataSet d = new PieDataSet(entries, getString(R.string.chart_pie_guide));

        // space between slices
        d.setSliceSpace(2f);
        d.setColors(colors);

        return new PieData(d);
    }

    /**
     * 操作ログ種別名称取得
     *
     * @param operation 操作ログ種別
     * @return 操作ログ種別名称
     */
    private String getOperationName(int operation) {
        String operationName;
        switch (operation) {
            case Log.LOG_NOCHANGE:
                operationName = "";
                break;
            case Log.LOG_CREATE_NEW:
            default:
                operationName = getString(R.string.log_create_new);
                break;
            case Log.LOG_CHANGE_STATUS_TODO:
                operationName = getString(R.string.log_change_status_todo);
                break;
            case Log.LOG_CHANGE_STATUS_DONE:
                operationName = getString(R.string.log_change_status_done);
                break;
            case Log.LOG_CHANGE_GROUP:
                operationName = getString(R.string.log_change_group);
                break;
            case Log.LOG_CHANGE_CONTENTS:
                operationName = getString(R.string.log_change_contents);
                break;
        }
        return operationName;

    }

    /**
     * 操作ログ種別に対応する色を取得する
     *
     * @param operation 操作ログ種別
     * @return 操作ログ種別に対応する色
     */
    private int getOperationColor(int operation) {
        int rgbColor;
        switch (operation) {
            case Log.LOG_NOCHANGE:
                rgbColor = rgb(Log.LOG_NOCHANGE_COLOR);
                break;
            case Log.LOG_CREATE_NEW:
            default:
                rgbColor = rgb(Log.LOG_CREATE_NEW_COLOR);
                break;
            case Log.LOG_CHANGE_STATUS_TODO:
                rgbColor = rgb(Log.LOG_CHANGE_STATUS_TODO_COLOR);
                break;
            case Log.LOG_CHANGE_STATUS_DONE:
                rgbColor = rgb(Log.LOG_CHANGE_STATUS_DONE_COLOR);
                break;
            case Log.LOG_CHANGE_GROUP:
                rgbColor = rgb(Log.LOG_CHANGE_GROUP_COLOR);
                break;
            case Log.LOG_CHANGE_CONTENTS:
                rgbColor = rgb(Log.LOG_CHANGE_CONTENTS_COLOR);
                break;
        }
        return rgbColor;
    }

    /**
     * 文字列で表現された色をRGB値に変換する
     *
     * @param hex 色の文字列
     * @return RGB値
     */
    private int rgb(String hex) {
        int color = (int) Long.parseLong(hex.replace("#", ""), 16);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color >> 0) & 0xFF;
        return Color.rgb(r, g, b);
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