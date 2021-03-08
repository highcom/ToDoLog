package com.highcom.todolog.ui.grouplist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.highcom.todolog.R;
import com.highcom.todolog.ToDoMainActivity;
import com.highcom.todolog.datamodel.Group;
import com.highcom.todolog.datamodel.GroupViewModel;
import com.highcom.todolog.ui.SimpleCallbackHelper;

import java.util.ArrayList;
import java.util.List;

public class GroupListFragment extends Fragment implements SimpleCallbackHelper.SimpleCallbackListener, GroupListAdapter.GroupListAdapterListener {

    private int latestGroupOrder;
    private List<Group> rearrangeGroupList;
    GroupListAdapter mGroupListAdapter;
    private GroupViewModel mGroupViewModel;
    private SimpleCallbackHelper mSimpleCallbackHelper;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_grouplist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.group_list_view);
        mGroupListAdapter = new GroupListAdapter(new GroupListAdapter.GroupDiff(), this);
        recyclerView.setAdapter(mGroupListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mGroupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);
        mGroupViewModel.getGroupList().observe(getViewLifecycleOwner(), groupList -> {
            latestGroupOrder = groupList.size();
            rearrangeGroupList = new ArrayList<>();
            for (Group group : groupList) {
                rearrangeGroupList.add(group.clone());
            }
            mGroupListAdapter.submitList(groupList);
            // 新規作成時は対象のセルにフォーカスされるようにスクロールする
            for (int position = 0; position < groupList.size(); position++) {
                if (groupList.get(position).getGroupName().equals("")) {
                    recyclerView.smoothScrollToPosition(position);
                    break;
                }
            }
        });

        final float scale = getResources().getDisplayMetrics().density;
        // スワイプしたチキのボタンの定義
        mSimpleCallbackHelper = new SimpleCallbackHelper(getContext(), recyclerView, scale, this) {
            @SuppressLint("ResourceType")
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new SimpleCallbackHelper.UnderlayButton(
                        getString(R.string.swipe_button_delete),
                        BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete),
                        Color.parseColor(getString(R.color.red)),
                        (RecyclerView.ViewHolder) viewHolder,
                        (holder, pos) -> {
                            mGroupViewModel.deleteGroupByGroupId(((GroupViewHolder)holder).getGroupId());
                        }
                ));
            }
        };
    }

    @Override
    public boolean onSimpleCallbackMove(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        // 並べ替え対象がITEMでなければ行わない
        if (viewHolder.getItemViewType() != GroupListAdapter.TYPE_ITEM || target.getItemViewType() != GroupListAdapter.TYPE_ITEM) return false;
        // Adapterの表示を更新する
        final int fromPos = viewHolder.getAdapterPosition();
        final int toPos = target.getAdapterPosition();
        mGroupListAdapter.notifyItemMoved(fromPos, toPos);
        // 並べ替えが終わるまでOrderを編集する
        final long fromId =  ((GroupViewHolder)viewHolder).getGroup().getGroupId();
        final long toId = ((GroupViewHolder)target).getGroup().getGroupId();
        Group fromGroup = null;
        Group toGroup = null;
        for (Group group : rearrangeGroupList) {
            if (group.getGroupId() == fromId) fromGroup = group;
            if (group.getGroupId() == toId) toGroup = group;
        }
        if (fromGroup != null && toGroup != null) {
            final int fromOrder = fromGroup.getGroupOrder();
            final int toOrder = toGroup.getGroupOrder();
            fromGroup.setGroupOrder(toOrder);
            toGroup.setGroupOrder(fromOrder);
        }
        return true;
    }

    @Override
    public void clearSimpleCallbackView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        mGroupViewModel.update(rearrangeGroupList);
    }

    public void addNewGroup() {
        Group group = new Group(0, latestGroupOrder + 1, "");
        mGroupViewModel.insert(group);
    }

    @Override
    public void onGroupNameClicked(View view) {
        view.post(() -> {
            ((ToDoMainActivity)getContext()).hideFloatingButton();
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.requestFocus();
            InputMethodManager inputMethodManager = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.showSoftInput(view, 0);
            }
        });
    }

    @Override
    public void onGroupNameOutOfFocused(View view, Group group, String editGroupName) {
        ((ToDoMainActivity)getContext()).showFloatingButton();
        view.setFocusable(false);
        view.setFocusableInTouchMode(false);
        view.requestFocus();
        // 内容が空白の場合には削除する
        if (editGroupName.equals("")) {
            mGroupViewModel.deleteGroupByGroupId(group.getGroupId());
            return;
        }
        // 内容が変更されていない場合には更新をしない
        if (group.getGroupName().equals(editGroupName)) return;

        Group editGroup = new Group(group.getGroupId(), group.getGroupOrder(), group.getGroupName());
        editGroup.setGroupName(editGroupName);
        mGroupViewModel.update(editGroup);
    }
}