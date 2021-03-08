package com.highcom.todolog.ui.todolist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

public class ToDoListFragment extends Fragment implements SimpleCallbackHelper.SimpleCallbackListener, ToDoListAdapter.ToDoListAdapterListener {

    public static final String SELECT_GROUP = "selectGroup";
    private long mSelectGroupId;
    private ToDoViewModel mToDoViewModel;
    private SimpleCallbackHelper mSimpleCallbackHelper;

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
        mSelectGroupId = args.getLong(SELECT_GROUP);
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
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.scale_animation);
        ToDoListAdapter adapter = new ToDoListAdapter(new ToDoListAdapter.ToDoDiff(), this, animation);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mToDoViewModel = new ViewModelProvider(this).get(ToDoViewModel.class);
        mToDoViewModel.getToDoListByTaskGroup(mSelectGroupId).observe(getViewLifecycleOwner(), toDoAndLogList -> {
            // Todoの一覧が読み込まれたらバインドする
            adapter.submitList(toDoAndLogList);
            // 新規作成時は対象のセルにフォーカスされるようにスクロールする
            for (int position = 0; position < toDoAndLogList.size(); position++) {
                if (toDoAndLogList.get(position).toDo.getContents().equals("")) {
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
                            mToDoViewModel.deleteToDoByTodoId(((ToDoViewHolder)holder).getTodoId());
                        }
                ));
                underlayButtons.add(new SimpleCallbackHelper.UnderlayButton(
                        getString(R.string.swipe_button_detail),
                        BitmapFactory.decodeResource(getResources(), R.drawable.ic_detail),
                        Color.parseColor(getString(R.color.lightgray)),
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
        ToDo todo = new ToDo(0, ToDo.STATUS_TODO, mSelectGroupId, "", 0);
        Log log = new Log(0, 0, new Date(System.currentTimeMillis()), Log.LOG_CREATE_NEW);
        mToDoViewModel.insertToDoAndLog(todo, log);
    }

    @Override
    public void onToDoCheckButtonClicked(ToDoAndLog toDoAndLog) {
        // 内容編集中にチェックボックスが押下された場合は、キーボードを閉じる
        InputMethodManager inputMethodManager = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        getView().requestFocus();

        ToDo toDo = new ToDo(toDoAndLog.toDo.getTodoId(), toDoAndLog.toDo.getState(), toDoAndLog.toDo.getGroupId(), toDoAndLog.toDo.getContents(), toDoAndLog.toDo.getLatestLogId());
        int logOperation;
        if (toDo.getState() == ToDo.STATUS_TODO) {
            toDo.setState(ToDo.STATUS_DONE);
            logOperation = Log.LOG_CHANGE_STATUS_DONE;
        } else {
            toDo.setState(ToDo.STATUS_TODO);
            logOperation = Log.LOG_CHANGE_STATUS_TODO;
        }
        Log log = new Log(0, toDoAndLog.toDo.getTodoId(), new Date(System.currentTimeMillis()), logOperation);
        mToDoViewModel.updateToDoAndLog(toDo, log);
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
    public void onToDoContentsOutOfFocused(View view, ToDoAndLog toDoAndLog, String contents) {
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
        if (toDoAndLog.toDo.getContents().equals(contents)) return;

        ToDo toDo = new ToDo(toDoAndLog.toDo.getTodoId(), toDoAndLog.toDo.getState(), toDoAndLog.toDo.getGroupId(), toDoAndLog.toDo.getContents(), toDoAndLog.toDo.getLatestLogId());
        toDo.setContents(contents);

        // 変更前の内容が空の場合、新規作成なので内容変更のログは追加しない。
        if (toDoAndLog.toDo.getContents().equals("")) {
            mToDoViewModel.update(toDo);
        } else {
            Log log = new Log(0, toDoAndLog.toDo.getTodoId(), new Date(System.currentTimeMillis()), Log.LOG_CHANGE_CONTENTS);
            mToDoViewModel.updateToDoAndLog(toDo, log);
        }
    }
}
