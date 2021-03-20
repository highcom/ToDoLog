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
        mTodoDao = db.toDoDao();
        mLogDao = db.logDao();
    }

    LiveData<List<Group>> getGroupList() {
        return mGroupDao.getGroupList();
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

    void updateToDoAndLog(ToDo toDo, Log log) {
        ToDoLogRoomDatabase.databaseWriteExtractor.execute(() -> {
            long logRowId = mLogDao.insert(log);
            // グループ又はステータスを変更した場合には、変更先の順番の末尾にする
            if (log.getOperation() == Log.LOG_CHANGE_GROUP || log.getOperation() == Log.LOG_CHANGE_STATUS_TODO || log.getOperation() == Log.LOG_CHANGE_STATUS_DONE) {
                toDo.setTodoOrder(mTodoDao.getToDoOrderByGroupIdAndState(toDo.getGroupId(), toDo.getState()) + 1);
            }
            toDo.setLatestLogId(logRowId);
            mTodoDao.update(toDo);
        });
    }

    void updateToDoAndLog(List<ToDo> toDoList, long targetToDoId, Log log) {
        ToDoLogRoomDatabase.databaseWriteExtractor.execute(() -> {
            long logRowId = mLogDao.insert(log);
            for (ToDo toDo : toDoList) {
                if (toDo.getTodoId() == targetToDoId) {
                    toDo.setLatestLogId(logRowId);
                    break;
                }
            }
            mTodoDao.update(toDoList);
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

    void updateGroupList(List<Group> groupList) {
        ToDoLogRoomDatabase.databaseWriteExtractor.execute(() -> {
            mGroupDao.update(groupList);
        });
    }

    void updateToDoList(List<ToDo> todoList) {
        ToDoLogRoomDatabase.databaseWriteExtractor.execute(() -> {
            mTodoDao.update(todoList);
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
            List<Long> deleteToDoIds = mTodoDao.getToDoIdListByTaskGroup(groupId);
            for (Long toDoId : deleteToDoIds) mLogDao.deleteLogByTodoId(toDoId.longValue());
            mGroupDao.deleteByGroupId(groupId);
            mTodoDao.deleteByGroupId(groupId);
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
