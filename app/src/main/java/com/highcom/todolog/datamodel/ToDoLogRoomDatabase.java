package com.highcom.todolog.datamodel;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.sql.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {ToDo.class, Log.class}, version = 1, exportSchema = false)
@TypeConverters({DataConverters.class})
abstract class ToDoLogRoomDatabase extends RoomDatabase {
    abstract ToDoDao toDoDao();
    abstract LogDao logDao();

    private static volatile ToDoLogRoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExtractor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static ToDoLogRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ToDoLogRoomDatabase.class) {
                if (INSTANCE == null) {
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
                LogDao logDao = INSTANCE.logDao();
                logDao.deleteAll();
                logDao.insert(new Log(1, 1, new Date(System.currentTimeMillis()), "regist"));
                logDao.insert(new Log(2, 1, new Date(System.currentTimeMillis()), "modify"));
                logDao.insert(new Log(3, 2, new Date(System.currentTimeMillis()), "regist"));
                logDao.insert(new Log(4, 3, new Date(System.currentTimeMillis()), "regist"));
                logDao.insert(new Log(5, 4, new Date(System.currentTimeMillis()), "regist"));
                logDao.insert(new Log(6, 5, new Date(System.currentTimeMillis()), "regist"));

                ToDoDao todoDao = INSTANCE.toDoDao();
                todoDao.deleteAll();
                todoDao.insert(new ToDo(1, 1, "TASK1", "作業内容1", ""));
                todoDao.insert(new ToDo(2, 1, "TASK1", "作業内容2", ""));
                todoDao.insert(new ToDo(3, 2, "TASK1", "作業内容3", ""));
                todoDao.insert(new ToDo(4, 1, "TASK2", "作業内容4", ""));
                todoDao.insert(new ToDo(5, 1, "TASK3", "作業内容5", ""));

            });
        }
    };
}
