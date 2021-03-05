package com.highcom.todolog.ui.grouplist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.highcom.todolog.R;

public class GroupViewHolder extends RecyclerView.ViewHolder {
    private EditText mGroupName;

    public GroupViewHolder(@NonNull View itemView) {
        super(itemView);
        mGroupName = (EditText)itemView.findViewById(R.id.group_name);
    }

    public void bind(String groupName) {
        mGroupName.setText(groupName);
    }

    static GroupViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_group, parent, false);
        return new GroupViewHolder(view);
    }
}
