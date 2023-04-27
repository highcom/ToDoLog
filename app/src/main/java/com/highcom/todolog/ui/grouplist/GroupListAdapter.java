package com.highcom.todolog.ui.grouplist;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.highcom.todolog.datamodel.Group;

/**
 * グループ一覧表示用のアダプタ
 */
public class GroupListAdapter extends ListAdapter<GroupListItem, GroupViewHolder> implements GroupViewHolder.GroupViewHolderListener {
    // グループ項目定数値
    public static final int TYPE_ITEM = 1;
    // フッター定数値
    public static final int TYPE_FOOTER = 2;

    // アダプタからの通知用リスナー
    private GroupListAdapterListener mGroupListAdapterListener;

    /**
     * グループ一覧表示用のアダプタからの通知用リスナーインタフェース
     */
    public interface GroupListAdapterListener {
        void onGroupNameClicked(View view);
        void onGroupNameOutOfFocused(View view, Group group, String editGroupName);
    }

    /**
     * コンストラクタ
     * @param diffCallback 差分比較用コールバック
     * @param groupListAdapterListener アダプタからの通知用リスナー
     */
    public GroupListAdapter(@NonNull DiffUtil.ItemCallback<GroupListItem> diffCallback, GroupListAdapterListener groupListAdapterListener) {
        super(diffCallback);
        mGroupListAdapterListener = groupListAdapterListener;
    }

    /**
     * グループ項目ビューホルダー生成処理
     * @param parent 対象のビューグループ
     * @param viewType 項目表示タイプ
     * @return ビューホルダー
     */
    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            return GroupViewHolder.create(parent, this);
        } else if (viewType == TYPE_FOOTER) {
            return GroupViewHolder.create(parent);
        } else {
            return null;
        }
    }

    /**
     * グループ項目ビューホルダーのバインディング処理
     * @param holder ビューホルダー
     * @param position 項目位置
     */
    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        // フッターセルの場合にはバインドしない
        if (position >= super.getItemCount()) return;

        GroupListItem current = getItem(position);
        holder.bind(current);
    }

    /**
     * グループ一覧表示数取得処理
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
     * グループの内容にフォーカスされた時に入力状態にする処理
     * @param view フォーカス対象のビュー
     */
    @Override
    public void onGroupNameClicked(View view) {
        mGroupListAdapterListener.onGroupNameClicked(view);
    }

    /**
     * グループ入力中にフォーカスが外れた時の入力状態解除処理
     * @param view 対象のビュー
     * @param group 対象のToDoデータ
     * @param editGroupName 記入内容文字列
     */
    @Override
    public void onGroupNameOutOfFocused(View view, Group group, String editGroupName) {
        mGroupListAdapterListener.onGroupNameOutOfFocused(view, group, editGroupName);
    }

    /**
     * グループ項目比較用クラス
     */
    public static class GroupDiff extends DiffUtil.ItemCallback<GroupListItem> {

        @Override
        public boolean areItemsTheSame(@NonNull GroupListItem oldItem, @NonNull GroupListItem newItem) {
            return oldItem.getGroup().getGroupId() == newItem.getGroup().getGroupId() && oldItem.getGroup().getGroupOrder() == newItem.getGroup().getGroupOrder();
        }

        @Override
        public boolean areContentsTheSame(@NonNull GroupListItem oldItem, @NonNull GroupListItem newItem) {
            return oldItem.getGroup().equals(newItem.getGroup()) && oldItem.getCount() == newItem.getCount();
        }
    }
}
