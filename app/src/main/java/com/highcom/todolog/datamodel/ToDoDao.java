package com.highcom.todolog.datamodel;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ToDoDao {

    @Query("SELECT * FROM todo_table ORDER BY todo_id ASC")
    LiveData<List<ToDo>> getToDoList();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(ToDo todo);

    @Query("DELETE FROM todo_table")
    void deleteAll();
}
