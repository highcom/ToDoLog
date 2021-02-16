package com.highcom.todolog.datamodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class ToDoViewModel extends AndroidViewModel {
    private ToDoLogRepository mRepository;

    private LiveData<List<ToDo>> mToDoList;

    public ToDoViewModel(@NonNull Application application) {
        super(application);
        mRepository = ToDoLogRepository.getInstance(application);
        mToDoList = mRepository.getToDoList();
    }

    public LiveData<List<ToDo>> getToDoList() {
        return mToDoList;
    }

    public LiveData<List<ToDo>> getToDoListByTaskGroup(String group) {
        return mRepository.getTodoListByTaskGroup(group);
    }

    public LiveData<List<String>> getDistinctToDoTaskGroup() {
        return mRepository.getDistinctToDoTaskGroup();
    }

    public LiveData<Log> getLogByTodoIdLatest(int todoId) {
        return mRepository.getLogByTodoIdLatest(todoId);
    }

    public void deleteToDoByTodoId(int todoId) {
        mRepository.deleteToDoByTodoId(todoId);
        mRepository.deleteLogByTodoId(todoId);
    }

    void insert(ToDo todo) {
        mRepository.insert(todo);
    }
}
