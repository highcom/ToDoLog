package com.highcom.todolog.ui.todolist;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.highcom.todolog.R;
import com.highcom.todolog.ToDoDetailActivity;
import com.highcom.todolog.datamodel.ToDo;
import com.highcom.todolog.datamodel.ToDoViewModel;
import com.highcom.todolog.ui.DividerItemDecoration;
import com.highcom.todolog.ui.SimpleCallbackHelper;

import java.util.List;

public class ToDoListFragment extends Fragment implements SimpleCallbackHelper.SimpleCallbackListener{

    private String mSelectGroup;
    private LifecycleOwner mOwner;
    private ToDoViewModel mToDoViewModel;
    private SimpleCallbackHelper simpleCallbackHelper;

    public ToDoListFragment(LifecycleOwner owner) {
        this.mOwner = owner;
    }

    public void setSelectGroup(String group) {
        mSelectGroup = group;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_todolist, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.todo_list_view);
        ToDoListAdapter adapter = new ToDoListAdapter(new ToDoListAdapter.ToDoDiff());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // セル間に区切り線を実装する
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);

        mToDoViewModel = new ViewModelProvider(this).get(ToDoViewModel.class);
        mToDoViewModel.getToDoListByTaskGroup(mSelectGroup).observe(this.mOwner, todolist -> {
            // Todoの一覧が読み込まれたらバインドする
            adapter.submitList(todolist);
            // 各ToDoに対して、Logデータの最新情報で更新して、変更通知をする
            for ( ToDo todo : todolist) {
                mToDoViewModel.getLogByTodoIdLatest(todo.getTodoId()).observe(this.mOwner, log -> {
                    todo.setLog(log.getDate().toString() + " " + log.getOperation());
                    adapter.notifyDataSetChanged();
                });
            }
        });

        final float scale = getResources().getDisplayMetrics().density;
        // ドラックアンドドロップの操作を実装する
        simpleCallbackHelper = new SimpleCallbackHelper(getContext(), recyclerView, scale, this) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new SimpleCallbackHelper.UnderlayButton(
                        "削除",
                        0,
                        Color.parseColor("#FF3C30"),
                        (RecyclerView.ViewHolder) viewHolder,
                        (holder, pos) -> {
                            // 削除処理を実装する
                        }
                ));
                underlayButtons.add(new SimpleCallbackHelper.UnderlayButton(
                        "詳細",
                        0,
                        Color.parseColor("#C7C7CB"),
                        (RecyclerView.ViewHolder) viewHolder,
                        (holder, pos) -> {
                            Intent intent = new Intent(getContext(), ToDoDetailActivity.class);
                            intent.putExtra("TODO_ID", ((ToDoViewHolder)holder).getTodoId());
                            startActivity(intent);
                        }
                ));
            }
        };
    }

    @Override
    public boolean onSimpleCallbackMove(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void clearSimpleCallbackView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {

    }
}
