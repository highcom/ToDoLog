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

@Database(entities = {Group.class, ToDo.class, Log.class}, version = 1, exportSchema = false)
@TypeConverters({DataConverters.class})
abstract class ToDoLogRoomDatabase extends RoomDatabase {
    abstract GroupDao groupDao();
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
                GroupDao groupDao = INSTANCE.groupDao();
                groupDao.deleteAll();
                groupDao.insert(new Group(1, 2, "GROUP1"));
                groupDao.insert(new Group(2, 3, "GROUP2"));
                groupDao.insert(new Group(3, 4, "GROUP3"));
                groupDao.insert(new Group(4, 1, "GROUP4"));

                LogDao logDao = INSTANCE.logDao();
                logDao.deleteAll();
                logDao.insert(new Log(1, 1, new Date(System.currentTimeMillis()), Log.LOG_CREATE_NEW));
                logDao.insert(new Log(2, 1, new Date(System.currentTimeMillis()), Log.LOG_CHANGE_CONTENTS));
                logDao.insert(new Log(7, 1, new Date(System.currentTimeMillis()), Log.LOG_CHANGE_CONTENTS));
                logDao.insert(new Log(3, 2, new Date(System.currentTimeMillis()), Log.LOG_CREATE_NEW));
                logDao.insert(new Log(4, 3, new Date(System.currentTimeMillis()), Log.LOG_CREATE_NEW));
                logDao.insert(new Log(5, 4, new Date(System.currentTimeMillis()), Log.LOG_CREATE_NEW));
                logDao.insert(new Log(6, 5, new Date(System.currentTimeMillis()), Log.LOG_CREATE_NEW));
                logDao.insert(new Log(8, 6, new Date(System.currentTimeMillis()), Log.LOG_CREATE_NEW));
                logDao.insert(new Log(9, 7, new Date(System.currentTimeMillis()), Log.LOG_CREATE_NEW));

                ToDoDao todoDao = INSTANCE.toDoDao();
                todoDao.deleteAll();
                todoDao.insert(new ToDo(1, 1, 1, 1, "作業内容1", 7));
                todoDao.insert(new ToDo(2, 2, 1, 1, "作業内容2", 3));
                todoDao.insert(new ToDo(3, 1, 2, 1, "作業内容3", 4));
                todoDao.insert(new ToDo(4, 1, 1, 2, "作業内容4", 5));
                todoDao.insert(new ToDo(5, 1, 1, 3, "作業内容5", 6));
                todoDao.insert(new ToDo(6, 2, 2, 1, "作業内容6", 8));
                todoDao.insert(new ToDo(7, 1, 1, 4, "作業内容7", 9));

            });
        }
    };
}
