package com.highcom.todolog.datamodel;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class ToDoLogRepository {
    private static ToDoLogRepository instance;

    private GroupDao mGroupDao;
    private ToDoDao mTodoDao;
    private LogDao mLogDao;
    private LiveData<List<Group>> mGroupList;
    private LiveData<Group> mFirstGroup;

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
        mGroupDao = db.groupDao();
        mGroupList = mGroupDao.getGroupList();
        mFirstGroup = mGroupDao.getFirstGroup();
        mTodoDao = db.toDoDao();
        mLogDao = db.logDao();
    }

    LiveData<List<Group>> getGroupList() {
        return mGroupList;
    }

    LiveData<Group> getFirstGroup() {
        return mFirstGroup;
    }

    LiveData<List<ToDoAndLog>> getTodoListByTaskGroup(int groupId) {
        return mTodoDao.getToDoListByTaskGroup(groupId);
    }

    LiveData<List<Log>> getLogListByTodoId(int todoId) {
        return mLogDao.getLogByTodoId(todoId);
    }

    LiveData<Integer> getLogIdByTodoIdLatest(int todoId) {
        return mLogDao.getLogIdByTodoIdLatest(todoId);
    }

    LiveData<ToDo> getToDo(int todoId) {
        return mTodoDao.getToDo(todoId);
    }

    void update(ToDo todo) {
        ToDoLogRoomDatabase.databaseWriteExtractor.execute(() -> {
            mTodoDao.update(todo);
        });
    }

    void insert(Group group) {
        ToDoLogRoomDatabase.databaseWriteExtractor.execute(() -> {
            mGroupDao.insert(group);
        });
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

    void deleteToDoByTodoId(int todoId) {
        ToDoLogRoomDatabase.databaseWriteExtractor.execute(() -> {
            mTodoDao.deleteByTodoId(todoId);
        });
    }

    void deleteLogByTodoId(int todoId) {
        ToDoLogRoomDatabase.databaseWriteExtractor.execute(() -> {
            mLogDao.deleteLogByTodoId(todoId);
        });
    }
}
