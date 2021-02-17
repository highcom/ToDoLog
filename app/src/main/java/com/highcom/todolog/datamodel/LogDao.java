package com.highcom.todolog.datamodel;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LogDao {
    @Query("SELECT * FROM log_table WHERE todo_id = :id")
    LiveData<List<Log>> getLogByTodoId(int id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Log log);

    @Query("DELETE FROM log_table WHERE todo_id = :id")
    void deleteLogByTodoId(int id);

    @Query("DELETE FROM log_table")
    void deleteAll();
}
