package com.highcom.todolog.datamodel;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class ToDoLogRepository {
    private static ToDoLogRepository instance;

    private ToDoDao mTodoDao;
    private LogDao mLogDao;
    private LiveData<List<ToDo>> mToDoList;
    private LiveData<List<Log>> mLogList;

    public static ToDoLogRepository getInstance(Application application) {
        if (instance == null) {
            synchronized (ToDoLogRepository.class) {
                if (instance == null) {
                    instance = new ToDoLogRepository(application);
                }
            }
        }
        return instance;
    }

    private ToDoLogRepository(Application application) {
        ToDoLogRoomDatabase db = ToDoLogRoomDatabase.getDatabase(application);
        mTodoDao = db.toDoDao();
        mToDoList = mTodoDao.getToDoList();
        mLogDao = db.logDao();
        mLogList = mLogDao.getLogList();
    }

    LiveData<List<ToDo>> getToDoList() {
        return mToDoList;
    }

    LiveData<List<ToDo>> getTodoListByTaskGroup(String group) {
        return mTodoDao.getToDoListByTaskGroup(group);
    }

    LiveData<List<Log>> getLogList() {
        return mLogList;
    }

    LiveData<List<Log>> getLogListByTodoId(int todoId) {
        return mLogDao.getLogByTodoId(todoId);
    }

    LiveData<Log> getLogByTodoIdLatest(int todoId) {
        return mLogDao.getLogByTodoIdLatest(todoId);
    }

    void insert(ToDo todo) {
        ToDoLogRoomDatabase.databaseWriteExtractor.execute(() -> {
            mTodoDao.insert(todo);
        });
    }

    void insert(Log log) {
        ToDoLogRoomDatabase.databaseWriteExtractor.execute(() -> {
            mLogDao.insert(log);
        });
    }
}
