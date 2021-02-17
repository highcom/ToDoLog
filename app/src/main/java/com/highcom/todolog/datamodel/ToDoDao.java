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

    @Query("SELECT * FROM todo_table ORDER BY todo_id ASC")
    LiveData<List<ToDo>> getToDoList();

    @Transaction
    @Query("SELECT * FROM todo_table WHERE task_group = :group")
    LiveData<List<ToDoAndLog>> getToDoListByTaskGroup(String group);

    @Query("SELECT DISTINCT task_group FROM todo_table")
    LiveData<List<String>> getDistinctToDoTaskGroup();

    @Query("SELECT * FROM todo_table WHERE todo_id = :id")
    LiveData<ToDo> getToDo(int id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(ToDo todo);

    @Query("DELETE FROM todo_table WHERE todo_id = :id")
    void deleteByTodoId(int id);

    @Query("DELETE FROM todo_table")
    void deleteAll();
}
