package com.highcom.todolog.ui.todolist;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.highcom.todolog.datamodel.ToDo;
import com.highcom.todolog.datamodel.ToDoAndLog;

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
        String logInfo = current.log.getDate().toString() + " " + current.log.getOperation();
        holder.bind(current.toDo.getTodoId(), current.toDo.getContents(), logInfo);
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
