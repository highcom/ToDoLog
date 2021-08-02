package com.highcom.todolog.datamodel;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.highcom.todolog.R;

import java.sql.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Group.class, ToDo.class, Log.class}, version = 1, exportSchema = false)
@TypeConverters({DataConverters.class})
abstract class ToDoLogRoomDatabase extends RoomDatabase {
    abstract GroupDao groupDao();
    abstract ToDoDao toDoDao();
    abstract LogDao logDao();

    private static volatile ToDoLogRoomDatabase INSTANCE;
    private static Context mContext;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExtractor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static ToDoLogRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ToDoLogRoomDatabase.class) {
                if (INSTANCE == null) {
                    mContext = context;
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), ToDoLogRoomDatabase.class, "todolog_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            databaseWriteExtractor.execute(() -> {
                GroupDao groupDao = INSTANCE.groupDao();
                groupDao.deleteAll();
                groupDao.insert(new Group(1, 1, mContext.getString(R.string.group_data_1)));
                groupDao.insert(new Group(2, 2, mContext.getString(R.string.group_data_2)));
                groupDao.insert(new Group(3, 3, mContext.getString(R.string.group_data_3)));

                long currentTime = System.currentTimeMillis();
                LogDao logDao = INSTANCE.logDao();
                logDao.deleteAll();
                logDao.insert(new Log(1, 1, new Date(currentTime), Log.LOG_CREATE_NEW));
                logDao.insert(new Log(2, 2, new Date(currentTime), Log.LOG_CREATE_NEW));
                logDao.insert(new Log(3, 3, new Date(currentTime), Log.LOG_CREATE_NEW));
                logDao.insert(new Log(4, 4, new Date(currentTime), Log.LOG_CREATE_NEW));
                logDao.insert(new Log(5, 5, new Date(currentTime), Log.LOG_CREATE_NEW));
                logDao.insert(new Log(6, 6, new Date(currentTime), Log.LOG_CREATE_NEW));
                logDao.insert(new Log(7, 7, new Date(currentTime - (1000 * 60 * 60 * 24)), Log.LOG_CREATE_NEW));
                logDao.insert(new Log(8, 8, new Date(currentTime - (1000 * 60 * 60 * 24 * 2)), Log.LOG_CREATE_NEW));
                logDao.insert(new Log(9, 9, new Date(currentTime), Log.LOG_CREATE_NEW));
                logDao.insert(new Log(10, 10, new Date(currentTime), Log.LOG_CREATE_NEW));
                logDao.insert(new Log(11, 11, new Date(currentTime), Log.LOG_CREATE_NEW));
                logDao.insert(new Log(12, 7, new Date(currentTime), Log.LOG_CHANGE_STATUS_DONE));
                logDao.insert(new Log(13, 8, new Date(currentTime - (1000 * 60 * 60 * 24 * 3 / 2)), Log.LOG_CHANGE_STATUS_DONE));
                logDao.insert(new Log(14, 12, new Date(currentTime), Log.LOG_CREATE_NEW));
                logDao.insert(new Log(15, 13, new Date(currentTime - (1000 * 60 * 60 * 24 * 2)), Log.LOG_CREATE_NEW));
                logDao.insert(new Log(16, 14, new Date(currentTime), Log.LOG_CREATE_NEW));
                logDao.insert(new Log(17, 15, new Date(currentTime), Log.LOG_CREATE_NEW));
                logDao.insert(new Log(18, 13, new Date(currentTime - (1000 * 60 * 60 * 24)), Log.LOG_CHANGE_CONTENTS));
                logDao.insert(new Log(19, 13, new Date(currentTime), Log.LOG_CHANGE_STATUS_DONE));
                logDao.insert(new Log(20, 8, new Date(currentTime - (1000 * 60 * 60 * 24)), Log.LOG_CHANGE_STATUS_TODO));
                logDao.insert(new Log(21, 8, new Date(currentTime), Log.LOG_CHANGE_STATUS_DONE));
                logDao.insert(new Log(22, 16, new Date(currentTime), Log.LOG_CREATE_NEW));
                logDao.insert(new Log(23, 17, new Date(currentTime), Log.LOG_CREATE_NEW));

                ToDoDao todoDao = INSTANCE.toDoDao();
                todoDao.deleteAll();
                todoDao.insert(new ToDo(1, 1, 1, 1, mContext.getString(R.string.todo_data_1), 1));
                todoDao.insert(new ToDo(2, 2, 1, 1, mContext.getString(R.string.todo_data_2), 2));
                todoDao.insert(new ToDo(3, 3, 1, 1, mContext.getString(R.string.todo_data_3), 3));
                todoDao.insert(new ToDo(4, 4, 1, 1, mContext.getString(R.string.todo_data_4), 4));
                todoDao.insert(new ToDo(5, 5, 1, 1, mContext.getString(R.string.todo_data_5), 5));
                todoDao.insert(new ToDo(6, 6, 1, 1, mContext.getString(R.string.todo_data_6), 6));

                todoDao.insert(new ToDo(7, 1, 2, 2, mContext.getString(R.string.todo_data_7), 12));
                todoDao.insert(new ToDo(8, 2, 2, 2, mContext.getString(R.string.todo_data_8), 21));
                todoDao.insert(new ToDo(9, 3, 1, 2, mContext.getString(R.string.todo_data_9), 9));
                todoDao.insert(new ToDo(10, 4, 1, 2, mContext.getString(R.string.todo_data_10), 10));
                todoDao.insert(new ToDo(11, 5, 1, 2, mContext.getString(R.string.todo_data_11), 11));

                todoDao.insert(new ToDo(12, 1, 1, 3, mContext.getString(R.string.todo_data_12), 14));
                todoDao.insert(new ToDo(13, 1, 2, 3, mContext.getString(R.string.todo_data_13), 19));
                todoDao.insert(new ToDo(14, 3, 1, 3, mContext.getString(R.string.todo_data_14), 16));
                todoDao.insert(new ToDo(15, 4, 1, 3, mContext.getString(R.string.todo_data_15), 17));

                todoDao.insert(new ToDo(16, 7, 1, 1, mContext.getString(R.string.todo_data_16), 22));
                todoDao.insert(new ToDo(17, 8, 1, 1, mContext.getString(R.string.todo_data_17), 23));
            });
        }
    };
}
