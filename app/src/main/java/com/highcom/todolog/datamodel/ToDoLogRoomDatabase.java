package com.highcom.todolog.datamodel;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.sql.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {ToDo.class, Log.class}, version = 1, exportSchema = false)
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
                ToDoDao todoDao = INSTANCE.toDoDao();
                todoDao.deleteAll();

                ToDo todo = new ToDo(1, 1, "TASK1", "作業内容1", "2021/2/1 20:00 update");
                todoDao.insert(todo);

                LogDao logDao = INSTANCE.logDao();
                logDao.deleteAll();

                Log log = new Log(1, 1, new Date(System.currentTimeMillis()).toString(), "regist");
                logDao.insert(log);
            });
        }
    };
}
