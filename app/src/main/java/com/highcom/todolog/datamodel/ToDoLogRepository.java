package com.highcom.todolog.datamodel;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;

import java.sql.Date;
import java.util.List;

import static com.highcom.todolog.SettingActivity.PREF_FILE_NAME;
import static com.highcom.todolog.SettingActivity.PREF_PARAM_NEW_TODO_ORDER;

public class ToDoLogRepository {
    public static final int ORDER_ASC = 0;
    public static final int ORDER_DESC = 1;

    private static ToDoLogRepository instance;

    private GroupDao mGroupDao;
    private ToDoDao mTodoDao;
    private LogDao mLogDao;
    private LiveData<List<Group>> mGroupList;

    public static ToDoLogRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (ToDoLogRepository.class) {
                if (instance == null) {
                    instance = new ToDoLogRepository(context);
                }
            }
        }
        return instance;
    }

    private ToDoLogRepository(Context context) {
        ToDoLogRoomDatabase db = ToDoLogRoomDatabase.getDatabase(context);
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

    public List<ToDoAndLog> getTodoListOnlyToDoByTaskGroupSync(long groupId) {
        return mTodoDao.getToDoListOnlyToDoByTaskGroupSync(groupId);
    }

    LiveData<List<Log>> getLogListByTodoId(long todoId) {
        return mLogDao.getLogByTodoId(todoId);
    }

    LiveData<ToDo> getToDo(long todoId) {
        return mTodoDao.getToDo(todoId);
    }

    LiveData<List<GroupCount>> getCountByGroupId(int state) {
        return mTodoDao.getCountByGroupId(state);
    }

    LiveData<List<DoneCount>> getDoneCountByLogDate(long group_id) {
        return mLogDao.getDoneCountByLogDate(group_id);
    }

    LiveData<List<LogCount>> getCountByLogOperation(long group_id) {
        return mLogDao.getCountByLogOperation(group_id);
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

    void updateAllToDoByGroupToState(long groupId, int setState) {
        ToDoLogRoomDatabase.databaseWriteExtractor.execute(() -> {
            int getState;
            int logOperation;
            if (setState == ToDo.STATUS_TODO) {
                getState = ToDo.STATUS_DONE;
                logOperation = Log.LOG_CHANGE_STATUS_TODO;
            } else {
                getState = ToDo.STATUS_TODO;
                logOperation = Log.LOG_CHANGE_STATUS_DONE;
            }

            List<ToDo> toDoList;
            toDoList = mTodoDao.getToDoListByTaskGroupAndStateSync(groupId, getState);
            for (ToDo toDo : toDoList) {
                toDo.setState(setState);
                Log log = new Log(0, toDo.getTodoId(), new Date(System.currentTimeMillis()), logOperation);
                long logRowId = mLogDao.insert(log);
                toDo.setTodoOrder(mTodoDao.getToDoOrderByGroupIdAndState(toDo.getGroupId(), toDo.getState()) + 1);
                toDo.setLatestLogId(logRowId);
                mTodoDao.update(toDo);
            }
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
