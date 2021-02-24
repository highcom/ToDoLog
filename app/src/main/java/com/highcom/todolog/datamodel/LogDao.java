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

    @Query("SELECT log_id FROM log_table WHERE todo_id = :id ORDER BY log_id DESC LIMIT 1")
    LiveData<Integer> getLogIdByTodoIdLatest(int id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Log log);

    @Query("DELETE FROM log_table WHERE todo_id = :id")
    void deleteLogByTodoId(int id);

    @Query("DELETE FROM log_table")
    void deleteAll();
}
