package com.highcom.todolog.ui.grouplist;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.highcom.todolog.datamodel.Group;

public class GroupListAdapter extends ListAdapter<Group, GroupViewHolder> implements GroupViewHolder.GroupViewHolderListener {
    public static final int TYPE_ITEM = 1;
    public static final int TYPE_FOOTER = 2;

    private GroupListAdapterListener mGroupListAdapterListener;

    public interface GroupListAdapterListener {
        void onGroupNameClicked(View view);
        void onGroupNameOutOfFocused(View view, Group group, String editGroupName);
    }

    public GroupListAdapter(@NonNull DiffUtil.ItemCallback<Group> diffCallback, GroupListAdapterListener groupListAdapterListener) {
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

        Group current = getItem(position);
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

    public static class GroupDiff extends DiffUtil.ItemCallback<Group> {

        @Override
        public boolean areItemsTheSame(@NonNull Group oldItem, @NonNull Group newItem) {
            return oldItem.getGroupId() == newItem.getGroupId() && oldItem.getGroupOrder() == newItem.getGroupOrder();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Group oldItem, @NonNull Group newItem) {
            return oldItem.equals(newItem);
        }
    }
}
