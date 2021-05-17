package com.highcom.todolog.ui.themelist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.highcom.todolog.R;

import java.util.List;

public class ThemeColorAdapter extends ArrayAdapter<ThemeColorItem> {

    private int mResource;
    private List<ThemeColorItem> mItems;
    private LayoutInflater mInflater;

    public ThemeColorAdapter(@NonNull Context context, int resource, @NonNull List<ThemeColorItem> objects) {
        super(context, resource, objects);

        mResource = resource;
        mItems = objects;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;

        if (convertView != null) {
            view = convertView;
        }
        else {
            view = mInflater.inflate(mResource, null);
        }

        // リストビューに表示する要素を取得
        ThemeColorItem item = mItems.get(position);

        // 背景色と名前を設定
        TextView title = view.findViewById(R.id.themeColorName);
        title.setText(item.getColorName());
        LinearLayout linearLayout = view.findViewById(R.id.themeColorRow);
        linearLayout.setBackgroundColor(item.getColorCode());

        return view;
    }
}