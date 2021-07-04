package com.highcom.todolog.ui.grouplist;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.highcom.todolog.datamodel.Group;

public class GroupListAdapter extends ListAdapter<GroupListItem, GroupViewHolder> implements GroupViewHolder.GroupViewHolderListener {
    public static final int TYPE_ITEM = 1;
    public static final int TYPE_FOOTER = 2;

    private GroupListAdapterListener mGroupListAdapterListener;

    public interface GroupListAdapterListener {
        void onGroupNameClicked(View view);
        void onGroupNameOutOfFocused(View view, Group group, String editGroupName);
    }

    public GroupListAdapter(@NonNull DiffUtil.ItemCallback<GroupListItem> diffCallback, GroupListAdapterListener groupListAdapterListener) {
        super(diffCallback);
        mGroupListAdapterListener = groupListAdapterListener;
    }

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

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        // フッターセルの場合にはバインドしない
        if (position >= super.getItemCount()) return;

        GroupListItem current = getItem(position);
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
    public void onGroupNameClicked(View view) {
        mGroupListAdapterListener.onGroupNameClicked(view);
    }

    @Override
    public void onGroupNameOutOfFocused(View view, Group group, String editGroupName) {
        mGroupListAdapterListener.onGroupNameOutOfFocused(view, group, editGroupName);
    }

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
