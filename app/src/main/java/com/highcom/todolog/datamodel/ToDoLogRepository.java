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

    LiveData<List<ToDoAndLog>> getTodoListByTaskGroup(long groupId) {
        return mTodoDao.getToDoListByTaskGroup(groupId);
    }

    LiveData<List<Log>> getLogListByTodoId(long todoId) {
        return mLogDao.getLogByTodoId(todoId);
    }

    LiveData<ToDo> getToDo(long todoId) {
        return mTodoDao.getToDo(todoId);
    }

    void updateToDoAndLog(ToDo todo, Log log) {
        ToDoLogRoomDatabase.databaseWriteExtractor.execute(() -> {
            long logRowId = mLogDao.insert(log);
            todo.setLatestLogId(logRowId);
            mTodoDao.update(todo);
        });
    }

    void update(Group group) {
        ToDoLogRoomDatabase.databaseWriteExtractor.execute(() -> {
            mGroupDao.update(group);
        });
    }

    void update(ToDo todo) {
        ToDoLogRoomDatabase.databaseWriteExtractor.execute(() -> {
            mTodoDao.update(todo);
        });
    }

    void insertToDoAndLog(ToDo todo, Log log) {
        ToDoLogRoomDatabase.databaseWriteExtractor.execute(() -> {
            long toDoRowId = mTodoDao.insert(todo);
            log.setTodoId(toDoRowId);
            long logRowId = mLogDao.insert(log);
            ToDo insertedToDo = mTodoDao.getToDoSync(toDoRowId);
            insertedToDo.setLatestLogId(logRowId);
            mTodoDao.update(insertedToDo);
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

    void deleteGroupByGroupId(long groupId) {
        ToDoLogRoomDatabase.databaseWriteExtractor.execute(() -> {
            mGroupDao.deleteByGroupId(groupId);
        });
    }

    void deleteToDoByTodoId(long todoId) {
        ToDoLogRoomDatabase.databaseWriteExtractor.execute(() -> {
            mTodoDao.deleteByTodoId(todoId);
        });
    }

    void deleteLogByTodoId(long todoId) {
        ToDoLogRoomDatabase.databaseWriteExtractor.execute(() -> {
            mLogDao.deleteLogByTodoId(todoId);
        });
    }
}
