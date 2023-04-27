package com.highcom.todolog.ui.todolist;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.highcom.todolog.datamodel.ToDoAndLog;

/**
 * ToDo一覧表示用のアダプタ
 */
public class ToDoListAdapter extends ListAdapter<ToDoAndLog, ToDoViewHolder> implements ToDoViewHolder.ToDoViewHolderListener {
    // Todo項目定数値
    public static final int TYPE_ITEM = 1;
    // フッター定数値
    public static final int TYPE_FOOTER = 2;

    // アダプタからの通知用リスナー
    private ToDoListAdapterListener mToDoListAdapterListener;

    /**
     * ToDo一覧表示用のアダプタからの通知用リスナーインタフェース
     */
    public interface ToDoListAdapterListener {
        void onToDoCheckButtonClicked(View view, ToDoAndLog toDoAndLog, String contents);
        void onToDoContentsClicked(View view);
        void onToDoContentsOutOfFocused(View view, ToDoAndLog toDoAndLog, String contents);
    }

    /**
     * コンストラクタ
     * @param diffCallback 差分比較用コールバック
     * @param toDoListAdapterListener アダプタからの通知用リスナー
     */
    public ToDoListAdapter(@NonNull DiffUtil.ItemCallback<ToDoAndLog> diffCallback, ToDoListAdapterListener toDoListAdapterListener) {
        super(diffCallback);
        mToDoListAdapterListener = toDoListAdapterListener;
    }

    /**
     * ToDo項目ビューホルダー生成処理
     * @param parent 対象のビューグループ
     * @param viewType 項目表示タイプ
     * @return ビューホルダー
     */
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

    /**
     * ToDo項目ビューホルダーのバインディング処理
     * @param holder ビューホルダー
     * @param position 項目位置
     */
    @Override
    public void onBindViewHolder(@NonNull ToDoViewHolder holder, int position) {
        // フッターセルの場合にはバインドしない
        if (position >= super.getItemCount()) return;

        ToDoAndLog current = getItem(position);
        holder.bind(current);
    }

    /**
     * ToDo一覧表示数取得処理
     * @return 表示数
     */
    @Override
    public int getItemCount() {
        if (super.getItemCount() > 0) {
            return super.getItemCount() + 1;
        } else {
            return 1;
        }
    }

    /**
     * 項目表示タイプ取得処理
     * @param position 対象の項目位置
     * @return 項目表示タイプ
     */
    @Override
    public int getItemViewType(int position) {
        if (position >= super.getItemCount()) {
            return TYPE_FOOTER;
        }
        return TYPE_ITEM;
    }

    /**
     * ToDoチェックボタンを押下された時のステータス変更処理
     * @param view 対象のビュー
     * @param toDoAndLog ToDoデータ
     * @param contents 記入内容文字列
     */
    @Override
    public void onToDoCheckButtonClicked(View view, ToDoAndLog toDoAndLog, String contents) {
        mToDoListAdapterListener.onToDoCheckButtonClicked(view, toDoAndLog, contents);
    }

    /**
     * ToDoの内容にフォーカスされた時に入力状態にする処理
     * @param view 対象のビュー
     */
    @Override
    public void onToDoContentsClicked(View view) {
        mToDoListAdapterListener.onToDoContentsClicked(view);
    }

    /**
     * ToDo入力中にフォーカスが外れた時の入力状態解除処理
     * @param view 対象のビュー
     * @param toDoAndLog 対象のToDoデータ
     * @param contents 記入内容文字列
     */
    @Override
    public void onToDoContentsOutOfFocused(View view, ToDoAndLog toDoAndLog, String contents) {
        mToDoListAdapterListener.onToDoContentsOutOfFocused(view, toDoAndLog, contents);
    }

    /**
     * ToDo項目比較用クラス
     */
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
