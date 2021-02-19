package com.highcom.todolog.datamodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class LogViewModel extends AndroidViewModel {
    private ToDoLogRepository mRepository;
    private LiveData<List<Group>> mGroupList;

    public LogViewModel(@NonNull Application application) {
        super(application);
        mRepository = ToDoLogRepository.getInstance(application);
        mGroupList = mRepository.getGroupList();
    }

    public LiveData<List<Log>> getLogListByTodoId(int todoId) {
        return mRepository.getLogListByTodoId(todoId);
    }

    public LiveData<ToDo> getToDo(int todoId) {
        return mRepository.getToDo(todoId);
    }

    public LiveData<List<Group>> getGroupList() {
        return mGroupList;
    }

    public void update(ToDo toDo) {
        mRepository.update(toDo);
    }

    public void insert(Log log) {
        mRepository.insert(log);
    }
}
