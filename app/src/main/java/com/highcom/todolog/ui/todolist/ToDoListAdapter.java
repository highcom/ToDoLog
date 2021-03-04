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
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;

    private Animation mAnimation;

    private ToDoListAdapterListener mToDoListAdapterListener;
    public interface ToDoListAdapterListener {
        void onToDoCheckButtonClicked(ToDoAndLog toDoAndLog);
        void onToDoContentsClicked(View view);
        void onToDoContentsOutOfFocused(View view, ToDoAndLog toDoAndLog, String contents);
    }

    public ToDoListAdapter(@NonNull DiffUtil.ItemCallback<ToDoAndLog> diffCallback, ToDoListAdapterListener toDoListAdapterListener, Animation animation) {
        super(diffCallback);
        mToDoListAdapterListener = toDoListAdapterListener;
        mAnimation = animation;
    }

    @NonNull
    @Override
    public ToDoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            return ToDoViewHolder.create(parent, this, mAnimation);
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
            return 0;
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
    public void onToDoCheckButtonClicked(ToDoAndLog toDoAndLog) {
        mToDoListAdapterListener.onToDoCheckButtonClicked(toDoAndLog);
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
            return oldItem.toDo.getTodoId() == newItem.toDo.getTodoId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ToDoAndLog oldItem, @NonNull ToDoAndLog newItem) {
            return oldItem.toDo.equals(newItem.toDo);
        }
    }
}
