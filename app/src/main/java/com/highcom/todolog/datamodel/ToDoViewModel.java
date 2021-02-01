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
        mRepository = new ToDoLogRepository(application);
        mToDoList = mRepository.getToDoList();
    }

    public LiveData<List<ToDo>> getToDoList() {
        return mToDoList;
    }

    void insert(ToDo todo) {
        mRepository.insert(todo);
    }
}
