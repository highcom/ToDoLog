package com.highcom.todolog.datamodel;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class ToDoLogRepository {
    private ToDoDao mTodoDao;
    private LiveData<List<ToDo>> mToDoList;

    ToDoLogRepository(Application application) {
        ToDoRoomDatabase db = ToDoRoomDatabase.getDatabase(application);
        mTodoDao = db.toDoDao();
        mToDoList = mTodoDao.getToDoList();
    }

    LiveData<List<ToDo>> getToDoList() {
        return mToDoList;
    }

    void insert(ToDo todo) {
        ToDoRoomDatabase.databaseWriteExtractor.execute(() -> {
            mTodoDao.insert(todo);
        });
    }
}
