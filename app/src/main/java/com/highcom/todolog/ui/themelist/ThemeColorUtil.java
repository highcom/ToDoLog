package com.highcom.todolog.ui.themelist;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.widget.ListView;

import androidx.annotation.ColorInt;

import com.highcom.todolog.R;

import java.util.ArrayList;
import java.util.List;

public class ThemeColorUtil {
    private static final int COLOR_NUM = 6;
    private int colors[];
    ThemeColorListener backgroundColorListener;

    public interface ThemeColorListener {
        void onSelectColorClicked(int color);
    }

    public ThemeColorUtil(Context context, ThemeColorListener listener) {
        colors = new int[COLOR_NUM];
        colors[0] = context.getResources().getColor(R.color.topaz);
        colors[1] = context.getResources().getColor(R.color.water_green);
        colors[2] = context.getResources().getColor(R.color.day_dream);
        colors[3] = context.getResources().getColor(R.color.old_rose);
        colors[4] = context.getResources().getColor(R.color.mauve);
        colors[5] = context.getResources().getColor(R.color.french_gray);

        backgroundColorListener = listener;
    }

    public boolean isColorExists(@ColorInt int color) {
        for (int i = 0; i < COLOR_NUM; i++) {
            if (colors[i] == color) return true;
        }
        return false;
    }

    @SuppressLint("ResourceType")
    public AlertDialog createThemeColorDialog(Activity activity) {
        final AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setView(activity.getLayoutInflater().inflate(R.layout.alert_theme_color, null))
                .create();
        alertDialog.show();
        ListView listView = alertDialog.findViewById(R.id.themeColorListView);

        List<ThemeColorItem> colorItems = new ArrayList<>();
        colorItems.add(new ThemeColorItem(activity.getString(R.string.color_topaz), colors[0]));
        colorItems.add(new ThemeColorItem(activity.getString(R.string.color_water_green), colors[1]));
        colorItems.add(new ThemeColorItem(activity.getString(R.string.color_day_dream), colors[2]));
        colorItems.add(new ThemeColorItem(activity.getString(R.string.color_old_rose), colors[3]));
        colorItems.add(new ThemeColorItem(activity.getString(R.string.color_mauve), colors[4]));
        colorItems.add(new ThemeColorItem(activity.getString(R.string.color_french_gray), colors[5]));
        ThemeColorAdapter adapter = new ThemeColorAdapter(activity, R.layout.row_theme_color, colorItems);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            backgroundColorListener.onSelectColorClicked(colors[i]);
            alertDialog.dismiss();
        });

        return alertDialog;
    }
}