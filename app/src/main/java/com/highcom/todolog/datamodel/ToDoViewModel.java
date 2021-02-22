package com.highcom.todolog.datamodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class ToDoViewModel extends AndroidViewModel {
    public static final int STATUS_TODO = 1;
    public static final int STATUS_DONE = 2;

    private ToDoLogRepository mRepository;

    public ToDoViewModel(@NonNull Application application) {
        super(application);
        mRepository = ToDoLogRepository.getInstance(application);
    }

    public LiveData<List<ToDoAndLog>> getToDoListByTaskGroup(int groupId) {
        return mRepository.getTodoListByTaskGroup(groupId);
    }

    public void deleteToDoByTodoId(int todoId) {
        mRepository.deleteToDoByTodoId(todoId);
        mRepository.deleteLogByTodoId(todoId);
    }

    public void update(ToDo todo) {
        mRepository.update(todo);
    }

    public void insert(ToDo todo) {
        mRepository.insert(todo);
    }
}
