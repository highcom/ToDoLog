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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.highcom.todolog.R;
import com.highcom.todolog.ToDoDetailActivity;
import com.highcom.todolog.datamodel.Log;
import com.highcom.todolog.datamodel.ToDo;
import com.highcom.todolog.datamodel.ToDoAndLog;
import com.highcom.todolog.datamodel.ToDoViewModel;
import com.highcom.todolog.ui.SimpleCallbackHelper;

import java.sql.Date;
import java.util.List;

import static com.highcom.todolog.datamodel.ToDoViewModel.STATUS_DONE;
import static com.highcom.todolog.datamodel.ToDoViewModel.STATUS_TODO;

public class ToDoListFragment extends Fragment implements SimpleCallbackHelper.SimpleCallbackListener, ToDoListAdapter.ToDoListAdapterListener {

    public static final String SELECT_GROUP = "selectGroup";
    private int mSelectGroupId;
    private ToDoViewModel mToDoViewModel;
    private SimpleCallbackHelper simpleCallbackHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mSelectGroupId = args.getInt(SELECT_GROUP);
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
        ToDoListAdapter adapter = new ToDoListAdapter(new ToDoListAdapter.ToDoDiff(), this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mToDoViewModel = new ViewModelProvider(this).get(ToDoViewModel.class);
        mToDoViewModel.getToDoListByTaskGroup(mSelectGroupId).observe(getViewLifecycleOwner(), toDoAndLog -> {
            // Todoの一覧が読み込まれたらバインドする
            adapter.submitList(toDoAndLog);
        });

        final float scale = getResources().getDisplayMetrics().density;
        // ドラックアンドドロップの操作を実装する
        simpleCallbackHelper = new SimpleCallbackHelper(getContext(), recyclerView, scale, this) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new SimpleCallbackHelper.UnderlayButton(
                        getString(R.string.swipe_button_delete),
                        0,
                        Color.parseColor("#FF3C30"),
                        (RecyclerView.ViewHolder) viewHolder,
                        (holder, pos) -> {
                            mToDoViewModel.deleteToDoByTodoId(((ToDoViewHolder)holder).getTodoId());
                        }
                ));
                underlayButtons.add(new SimpleCallbackHelper.UnderlayButton(
                        getString(R.string.swipe_button_detail),
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

    @Override
    public void onToDoListAdapterClicked(ToDoAndLog toDoAndLog) {
        // ToDo:operationの内容は差分比較と優先度を決める
        mToDoViewModel.insert(new Log(0, toDoAndLog.toDo.getTodoId(), new Date(System.currentTimeMillis()), "modify"));

        // insetしたLogIdでToDoもupdateする
        mToDoViewModel.getLogIdByTodoIdLatest(toDoAndLog.toDo.getTodoId()).observe(getViewLifecycleOwner(), logId -> {
            // ToDo状態と済状態をトグルする
            ToDo toDo = new ToDo(toDoAndLog.toDo.getTodoId(), toDoAndLog.toDo.getState(), toDoAndLog.toDo.getGroupId(), toDoAndLog.toDo.getContents(), toDoAndLog.toDo.getLatestLogId());
            if (toDo.getState() == STATUS_TODO) toDo.setState(STATUS_DONE);
            else toDo.setState(STATUS_TODO);
            toDo.setLatestLogId(logId);
            mToDoViewModel.update(toDo);
        });
    }
}
