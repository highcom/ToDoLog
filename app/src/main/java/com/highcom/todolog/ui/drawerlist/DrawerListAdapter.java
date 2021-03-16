package com.highcom.todolog.ui.drawerlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.highcom.todolog.R;

import java.util.List;

public class DrawerListAdapter extends ArrayAdapter<DrawerListItem> {

    private int mResource;
    private List<DrawerListItem> mItems;
    private LayoutInflater mInflater;

    public DrawerListAdapter(@NonNull Context context, int resource, @NonNull List<DrawerListItem> objects) {
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
        DrawerListItem item = mItems.get(position);

        // グループ名を設定
        TextView title = view.findViewById(R.id.drawer_title);
        title.setText(item.getGroupName());

        return view;
    }
}
