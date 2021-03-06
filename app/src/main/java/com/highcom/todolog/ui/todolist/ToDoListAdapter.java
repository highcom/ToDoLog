package com.highcom.todolog.ui.todolist;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.highcom.todolog.datamodel.ToDoAndLog;

public class ToDoListAdapter extends ListAdapter<ToDoAndLog, ToDoViewHolder> implements ToDoViewHolder.ToDoViewHolderListener {
    public static final int TYPE_ITEM = 1;
    public static final int TYPE_FOOTER = 2;

    private ToDoListAdapterListener mToDoListAdapterListener;
    public interface ToDoListAdapterListener {
        void onToDoCheckButtonClicked(View view, ToDoAndLog toDoAndLog, String contents);
        void onToDoContentsClicked(View view);
        void onToDoContentsOutOfFocused(View view, ToDoAndLog toDoAndLog, String contents);
    }

    public ToDoListAdapter(@NonNull DiffUtil.ItemCallback<ToDoAndLog> diffCallback, ToDoListAdapterListener toDoListAdapterListener) {
        super(diffCallback);
        mToDoListAdapterListener = toDoListAdapterListener;
    }

    @NonNull
    @Override
    public ToDoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            return ToDoViewHolder.create(parent, this);
        } else if (viewType == TYPE_FOOTER) {
            return ToDoViewHolder.create(parent);
        } else {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ToDoViewHolder holder, int position) {
        // フッターセルの場合にはバインドしない
        if (position >= super.getItemCount()) return;

        ToDoAndLog current = getItem(position);
        holder.bind(current);
    }

    @Override
    public int getItemCount() {
        if (super.getItemCount() > 0) {
            return super.getItemCount() + 1;
        } else {
            return 1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= super.getItemCount()) {
            return TYPE_FOOTER;
        }
        return TYPE_ITEM;
    }

    @Override
    public void onToDoCheckButtonClicked(View view, ToDoAndLog toDoAndLog, String contents) {
        mToDoListAdapterListener.onToDoCheckButtonClicked(view, toDoAndLog, contents);
    }

    @Override
    public void onToDoContentsClicked(View view) {
        mToDoListAdapterListener.onToDoContentsClicked(view);
    }

    @Override
    public void onToDoContentsOutOfFocused(View view, ToDoAndLog toDoAndLog, String contents) {
        mToDoListAdapterListener.onToDoContentsOutOfFocused(view, toDoAndLog, contents);
    }

    public static class ToDoDiff extends DiffUtil.ItemCallback<ToDoAndLog> {

        @Override
        public boolean areItemsTheSame(@NonNull ToDoAndLog oldItem, @NonNull ToDoAndLog newItem) {
            return oldItem.toDo.getTodoId() == newItem.toDo.getTodoId() && oldItem.toDo.getTodoOrder() == newItem.toDo.getTodoOrder();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ToDoAndLog oldItem, @NonNull ToDoAndLog newItem) {
            return oldItem.toDo.equals(newItem.toDo);
        }
    }
}
