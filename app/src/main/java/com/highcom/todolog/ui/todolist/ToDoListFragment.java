package com.highcom.todolog.ui.todolist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.highcom.todolog.R;
import com.highcom.todolog.ToDoDetailActivity;
import com.highcom.todolog.ToDoMainActivity;
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

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
        mToDoViewModel.getToDoListByTaskGroup(mSelectGroupId).observe(getViewLifecycleOwner(), toDoAndLogList -> {
            // Todoの一覧が読み込まれたらバインドする
            adapter.submitList(toDoAndLogList);
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

    public void addNewToDoAndLog() {
        ToDo todo = new ToDo(0, STATUS_TODO, mSelectGroupId, "", 0);
        Log log = new Log(0, 0, new Date(System.currentTimeMillis()), "regist");
        mToDoViewModel.insertToDoAndLog(todo, log);
    }

    @Override
    public void onToDoCheckButtonClicked(ToDoAndLog toDoAndLog) {
        // 内容編集中にチェックボックスが押下された場合は、キーボードを閉じる
        InputMethodManager inputMethodManager = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        getView().requestFocus();

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

    @Override
    public void onToDoContentsClicked(View view) {
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
    public void onToDoContentsOutOfFocused(View view, ToDoAndLog toDoAndLog, String contents, boolean changed) {
        ((ToDoMainActivity)getContext()).showFloatingButton();
        view.setFocusable(false);
        view.setFocusableInTouchMode(false);
        view.requestFocus();
        // 内容が空白の場合には削除する
        if (contents.equals("")) {
            mToDoViewModel.deleteToDoByTodoId(toDoAndLog.toDo.getTodoId());
            return;
        }
        // 内容が変更されていない場合には更新をしない
        if (!changed) return;
        // ToDo:operationの内容は差分比較と優先度を決める
        mToDoViewModel.insert(new Log(0, toDoAndLog.toDo.getTodoId(), new Date(System.currentTimeMillis()), "modify"));
        // insetしたLogIdでToDoもupdateする
        mToDoViewModel.getLogIdByTodoIdLatest(toDoAndLog.toDo.getTodoId()).observe(getViewLifecycleOwner(), logId -> {
            ToDo toDo = new ToDo(toDoAndLog.toDo.getTodoId(), toDoAndLog.toDo.getState(), toDoAndLog.toDo.getGroupId(), toDoAndLog.toDo.getContents(), toDoAndLog.toDo.getLatestLogId());
            toDo.setLatestLogId(logId);
            toDo.setContents(contents);
            mToDoViewModel.update(toDo);
        });
    }
}
