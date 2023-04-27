package com.highcom.todolog.ui.grouplist;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Filter;
import android.widget.Filterable;

import com.highcom.todolog.LogChartActivity;
import com.highcom.todolog.R;
import com.highcom.todolog.ToDoDetailActivity;
import com.highcom.todolog.ToDoMainActivity;
import com.highcom.todolog.datamodel.Group;
import com.highcom.todolog.datamodel.GroupCount;
import com.highcom.todolog.datamodel.GroupViewModel;
import com.highcom.todolog.datamodel.ToDo;
import com.highcom.todolog.datamodel.ToDoAndLog;
import com.highcom.todolog.ui.SimpleCallbackHelper;
import com.highcom.todolog.ui.todolist.ToDoViewHolder;
import com.highcom.todolog.widget.ToDoAppWidgetProvider;

import java.util.ArrayList;
import java.util.List;

import static com.highcom.todolog.SettingActivity.PREF_FILE_NAME;
import static com.highcom.todolog.SettingActivity.PREF_PARAM_TODO_COUNT;

/**
 * グループリストを一覧で表示する部分のFragment
 */
public class GroupListFragment extends Fragment implements SimpleCallbackHelper.SimpleCallbackListener, GroupListAdapter.GroupListAdapterListener, Filterable {

    // グループ一覧用のリサイクラービュー
    private RecyclerView mRecyclerView;
    // 初期表示位置で設定するかどうか
    private boolean isInitPositionSet;
    // グループ一覧の最新の順番
    private int mLatestGroupOrder;
    // フィルタ前のグループ一覧
    private List<Group> mOrigGroupList;
    // グループ事の残ToDo数
    private List<GroupCount> mGroupCounts;
    // 並べ替え用のグループ一覧
    private List<Group> mRearrangeGroupList;
    // グループ一覧表示用アダプタ
    private GroupListAdapter mGroupListAdapter;
    // グループ一覧用ViewModel
    private GroupViewModel mGroupViewModel;
    // グループ項目スワイプ時のボタン定義用イベント
    private SimpleCallbackHelper mSimpleCallbackHelper;
    // 残ToDo数を表示するかどうか
    private boolean mTodoCount;
    // 検索文字列
    private String searchViewWord;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        isInitPositionSet = false;
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_grouplist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.group_list_view);
        mGroupListAdapter = new GroupListAdapter(new GroupListAdapter.GroupDiff(), this);
        mRecyclerView.setAdapter(mGroupListAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mGroupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);
        mGroupViewModel.getGroupList().observe(getViewLifecycleOwner(), groupList -> {
            mOrigGroupList = groupList;
            mGroupViewModel.getCountByGroupId(ToDo.STATUS_TODO).observe(getViewLifecycleOwner(), groupCounts -> {
                mGroupCounts = groupCounts;
                // 新規作成時の最新の順番を設定
                mLatestGroupOrder = groupList.size();
                // 並べ替え用のリストを作成する
                mRearrangeGroupList = new ArrayList<>();
                for (Group group : groupList) mRearrangeGroupList.add(group.clone());
                // 検索文字列でフィルタ
                setSearchWordFilter(searchViewWord);
            });
        });

        final float scale = getResources().getDisplayMetrics().density;
        // スワイプしたチキのボタンの定義
        mSimpleCallbackHelper = new SimpleCallbackHelper(getContext(), mRecyclerView, scale, this) {
            @SuppressLint("ResourceType")
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                if (viewHolder.itemView.getId() == R.id.row_footer) return;

                underlayButtons.add(new SimpleCallbackHelper.UnderlayButton(
                        getString(R.string.swipe_button_delete),
                        BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete),
                        Color.parseColor(getString(R.color.red)),
                        (RecyclerView.ViewHolder) viewHolder,
                        (holder, pos) -> {
                            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                                    .setTitle(((GroupViewHolder)holder).getGroup().getGroupName() + getString(R.string.group_dialog_title))
                                    .setMessage(getString(R.string.group_dialog_detail))
                                    .setPositiveButton(getString(R.string.group_dialog_ok), (dialog1, which) -> mGroupViewModel.deleteGroupByGroupId(((GroupViewHolder)holder).getGroupId()))
                                    .setNegativeButton(getString(R.string.group_dialog_cancel), null)
                                    .show();
                        }
                ));
                underlayButtons.add(new SimpleCallbackHelper.UnderlayButton(
                        getString(R.string.swipe_button_chart),
                        BitmapFactory.decodeResource(getResources(), R.drawable.ic_chart),
                        Color.parseColor(getString(R.color.lightgray)),
                        (RecyclerView.ViewHolder) viewHolder,
                        (holder, pos) -> {
                            Intent intent = new Intent(getContext(), LogChartActivity.class);
                            intent.putExtra("GROUP_ID", ((GroupViewHolder)holder).getGroup().getGroupId());
                            intent.putExtra("GROUP_NAME", ((GroupViewHolder)holder).getGroup().getGroupName());
                            startActivity(intent);
                        }
                ));
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences data = getContext().getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        mTodoCount = data.getBoolean(PREF_PARAM_TODO_COUNT, true);
        // 更新通知を行うために、同じ値でupdateする
        List<Group> groupList = mGroupViewModel.getGroupList().getValue();
        if (groupList != null) {
            mGroupViewModel.update(groupList);
        }
    }

    /**
     * グループ一覧の並べ替え処理
     * @param viewHolder 並べ替え元ビューホルダー
     * @param target 並べ替え操作対象のビューホルダー
     * @return 並べ替え成功かどうか
     */
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
        for (Group group : mRearrangeGroupList) {
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

    /**
     * グループ一覧を操作後の更新処理
     * @param recyclerView
     * @param viewHolder 対象のビューホルダー
     */
    @Override
    public void clearSimpleCallbackView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        mGroupViewModel.update(mRearrangeGroupList);
    }

    /**
     * 新規グループ追加処理
     */
    public void addNewGroup() {
        Group group = new Group(0, mLatestGroupOrder + 1, "");
        mGroupViewModel.insert(group);
    }

    /**
     * グループの内容にフォーカスされた時に入力状態にする処理
     * @param view フォーカス対象のビュー
     */
    @Override
    public void onGroupNameClicked(View view) {
        view.post(() -> {
            ((ToDoMainActivity)getContext()).changeDoneFloatingButton();
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.requestFocus();
            InputMethodManager inputMethodManager = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.showSoftInput(view, 0);
            }
        });
    }

    /**
     * グループ入力中にフォーカスが外れた時の入力状態解除処理
     * @param view 対象のビュー
     * @param group 対象のToDoデータ
     * @param editGroupName 記入内容文字列
     */
    @Override
    public void onGroupNameOutOfFocused(View view, Group group, String editGroupName) {
        // 内容編集中にフォーカスが外れた場合は、キーボードを閉じる
        InputMethodManager inputMethodManager = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        ((ToDoMainActivity)getContext()).changeEditFloatingButton();
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

    /**
     * 検索文字設定処理
     *
     * @param wordFilter 検索文字列
     */
    public void setSearchWordFilter(String wordFilter) {
        searchViewWord = wordFilter;
        if (TextUtils.isEmpty(searchViewWord)) {
            getFilter().filter(null);
        } else {
            getFilter().filter(searchViewWord.toLowerCase());
        }
    }

    /**
     * 検索文字列での一覧のフィルタ処理
     * @return フィルタ
     */
    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults filterResults = new FilterResults();
                final ArrayList<Group> results = new ArrayList<>();
                if (constraint != null) {
                    if (mOrigGroupList != null && mOrigGroupList.size() > 0) {
                        for (final Group group : mOrigGroupList) {
                            if (group.getGroupName().toLowerCase().contains(constraint.toString()))
                                results.add(group);
                        }
                    }
                    filterResults.values = results;
                } else {
                    filterResults.values = mOrigGroupList;
                }
                return filterResults;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {
                List<GroupListItem> groupListItems = new ArrayList<>();
                for (Group group : (ArrayList<Group>)results.values) {
                    GroupListItem groupListItem = new GroupListItem(group);
                    if (mTodoCount) {
                        for (GroupCount groupCount : mGroupCounts) {
                            if (group.getGroupId() == groupCount.mGroupId)
                                groupListItem.setCount(groupCount.mGroupCount);
                        }
                    }
                    groupListItems.add(groupListItem);
                }
                // フィルタ結果でグループの一覧をバインドする
                mGroupListAdapter.submitList(groupListItems);
                // 初期表示の時は先頭位置にする
                if (!isInitPositionSet) {
                    mRecyclerView.scrollToPosition(0);
                    isInitPositionSet = true;
                }
                // 新規作成時は対象のセルにフォーカスされるようにスクロールする
                for (int position = 0; position < ((ArrayList<Group>)results.values).size(); position++) {
                    if (mOrigGroupList.get(position).getGroupName().equals("")) {
                        mRecyclerView.smoothScrollToPosition(position);
                        break;
                    }
                }
            }
        };
    }
}