package com.highcom.todolog.datamodel;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface ToDoDao {
    @Transaction
    @Query("SELECT * FROM todo_table WHERE group_id = :groupId")
    LiveData<List<ToDoAndLog>> getToDoListByTaskGroup(int groupId);

    @Query("SELECT * FROM todo_table WHERE todo_id = :id")
    LiveData<ToDo> getToDo(int id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(ToDo todo);

    @Query("DELETE FROM todo_table WHERE todo_id = :id")
    void deleteByTodoId(int id);

    @Query("DELETE FROM todo_table")
    void deleteAll();
}
