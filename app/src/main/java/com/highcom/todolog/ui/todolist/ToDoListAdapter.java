package com.highcom.todolog.ui.todolist;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.highcom.todolog.datamodel.ToDoAndLog;

public class ToDoListAdapter extends ListAdapter<ToDoAndLog, ToDoViewHolder> implements ToDoViewHolder.ToDoViewHolderListener {

    private ToDoListAdapterListener mToDoListAdapterListener;
    public interface ToDoListAdapterListener {
        void onToDoCheckButtonClicked(ToDoAndLog toDoAndLog);
        void onToDoContentsClicked(View view);
    }

    public ToDoListAdapter(@NonNull DiffUtil.ItemCallback<ToDoAndLog> diffCallback, ToDoListAdapterListener toDoListAdapterListener) {
        super(diffCallback);
        mToDoListAdapterListener = toDoListAdapterListener;
    }

    @NonNull
    @Override
    public ToDoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ToDoViewHolder.create(parent, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ToDoViewHolder holder, int position) {
        ToDoAndLog current = getItem(position);
        holder.bind(current);
    }

    @Override
    public void onToDoCheckButtonClicked(ToDoAndLog toDoAndLog) {
        mToDoListAdapterListener.onToDoCheckButtonClicked(toDoAndLog);
    }

    @Override
    public void onToDoContentsClicked(View view) {
        mToDoListAdapterListener.onToDoContentsClicked(view);
    }

    public static class ToDoDiff extends DiffUtil.ItemCallback<ToDoAndLog> {

        @Override
        public boolean areItemsTheSame(@NonNull ToDoAndLog oldItem, @NonNull ToDoAndLog newItem) {
            return oldItem.toDo.getTodoId() == newItem.toDo.getTodoId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ToDoAndLog oldItem, @NonNull ToDoAndLog newItem) {
            return oldItem.toDo.equals(newItem.toDo);
        }
    }
}
