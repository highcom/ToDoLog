package com.highcom.todolog.datamodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class LogViewModel extends AndroidViewModel {
    private ToDoLogRepository mRepository;

    private LiveData<List<Log>> mLogList;

    public LogViewModel(@NonNull Application application) {
        super(application);
        mRepository = ToDoLogRepository.getInstance(application);
        mLogList = mRepository.getLogList();
    }

    public LiveData<List<Log>> getLogList() {
        return mLogList;
    }

    void insert(Log log) {
        mRepository.insert(log);
    }
}
