package com.highcom.todolog.ui.grouplist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.highcom.todolog.R;
import com.highcom.todolog.datamodel.Group;

import java.nio.file.attribute.GroupPrincipal;

public class GroupViewHolder extends RecyclerView.ViewHolder {
    private Group mGroup;
    private EditText mGroupName;

    private GroupViewHolderListener mGroupViewHolderListener;
    public interface GroupViewHolderListener {
        void onGroupNameClicked(View view);
        void onGroupNameOutOfFocused(View view, Group group, String editGroupName);
    }

    public GroupViewHolder(@NonNull View itemView, GroupViewHolderListener groupViewHolderListener) {
        super(itemView);
        mGroupViewHolderListener = groupViewHolderListener;
        mGroupName = (EditText)itemView.findViewById(R.id.group_name);
        mGroupName.setOnClickListener(view -> mGroupViewHolderListener.onGroupNameClicked(view));
        mGroupName.setOnFocusChangeListener((view, b) -> {
            if (b) {
                mGroupViewHolderListener.onGroupNameClicked(view);
            } else {
                // グループ名を空欄で登録することは出来ないので元の名前にする
                if (mGroupName.getText().toString().equals("")) {
                    mGroupName.setText(mGroup.getGroupName());
                }
                // フォーカスが外れた時に内容が変更されていたら更新する
                mGroupViewHolderListener.onGroupNameOutOfFocused(view, mGroup, mGroupName.getText().toString());
            }
        });
    }

    public GroupViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void bind(Group group) {
        mGroup = group;
        mGroupName.setText(group.getGroupName());

        // 内容が空の場合、新規に作成されたものなので編集状態にする
        if (group.getGroupName().equals("")) {
            mGroupName.performClick();
        }
    }

    static GroupViewHolder create(ViewGroup parent, GroupViewHolderListener groupViewHolderListener) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_group, parent, false);
        return new GroupViewHolder(view, groupViewHolderListener);
    }

    static GroupViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_footer, parent, false);
        return new GroupViewHolder(view);
    }

    public long getGroupId() {
        return mGroup.getGroupId();
    }

    public Group getGroup() {
        return mGroup;
    }
}
