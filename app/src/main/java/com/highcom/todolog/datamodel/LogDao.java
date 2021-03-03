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
    LiveData<List<Log>> getLogByTodoId(long id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Log log);

    @Query("DELETE FROM log_table WHERE todo_id = :id")
    void deleteLogByTodoId(long id);

    @Query("DELETE FROM log_table")
    void deleteAll();
}
