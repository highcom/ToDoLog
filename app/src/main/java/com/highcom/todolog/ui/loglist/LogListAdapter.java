package com.highcom.todolog.ui.loglist;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.highcom.todolog.datamodel.Log;

public class LogListAdapter extends ListAdapter<Log, LogViewHolder> {
    public LogListAdapter(@NonNull DiffUtil.ItemCallback<Log> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return LogViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        Log current = getItem(position);
        holder.bind(current.getDate(), current.getOperation());
    }

    public static class LogDiff extends DiffUtil.ItemCallback<Log> {

        @Override
        public boolean areItemsTheSame(@NonNull Log oldItem, @NonNull Log newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Log oldItem, @NonNull Log newItem) {
            // 後で実装する
            return false;
        }
    }
}
