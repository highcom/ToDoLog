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

    public LiveData<List<Log>> getLogListByTodoId(long todoId) {
        return mRepository.getLogListByTodoId(todoId);
    }

    public LiveData<ToDo> getToDo(long todoId) {
        return mRepository.getToDo(todoId);
    }

    public LiveData<List<Group>> getGroupList() {
        return mGroupList;
    }

    public LiveData<List<LogCount>> getCountByLogOperation(long group_id) {
        return mRepository.getCountByLogOperation(group_id);
    }

    public void updateToDoAndLog(ToDo todo, Log log) {
        mRepository.updateToDoAndLog(todo, log);
    }

    public void update(ToDo toDo) {
        mRepository.update(toDo);
    }

    public void insert(Log log) {
        mRepository.insert(log);
    }
}
