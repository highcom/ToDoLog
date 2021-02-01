package com.highcom.todolog.datamodel;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {ToDo.class}, version = 1, exportSchema = false)
abstract class ToDoRoomDatabase extends RoomDatabase {
    abstract ToDoDao toDoDao();

    private static volatile ToDoRoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExtractor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static ToDoRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ToDoRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), ToDoRoomDatabase.class, "todo_database")
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
                ToDoDao dao = INSTANCE.toDoDao();
                dao.deleteAll();

                ToDo todo = new ToDo(1, 1, "TASK1", "作業内容1", "2021/2/1 20:00 update");
                dao.insert(todo);
            });
        }
    };
}
