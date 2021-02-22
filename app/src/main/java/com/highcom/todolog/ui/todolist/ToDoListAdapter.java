package com.highcom.todolog.ui.todolist;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.highcom.todolog.datamodel.ToDo;
import com.highcom.todolog.datamodel.ToDoAndLog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ToDoListAdapter extends ListAdapter<ToDoAndLog, ToDoViewHolder> {
    public ToDoListAdapter(@NonNull DiffUtil.ItemCallback<ToDoAndLog> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public ToDoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ToDoViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ToDoViewHolder holder, int position) {
        ToDoAndLog current = getItem(position);
        holder.bind(current);
    }

    public static class ToDoDiff extends DiffUtil.ItemCallback<ToDoAndLog> {

        @Override
        public boolean areItemsTheSame(@NonNull ToDoAndLog oldItem, @NonNull ToDoAndLog newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ToDoAndLog oldItem, @NonNull ToDoAndLog newItem) {
            return oldItem.log.getDate().equals(newItem.log.getDate());
        }
    }
}
