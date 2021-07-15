package com.highcom.todolog.datamodel;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LogDao {
    @Query("SELECT * FROM log_table WHERE todo_id = :id ORDER BY log_id DESC")
    LiveData<List<Log>> getLogByTodoId(long id);

    @Query("SELECT log_table.operation, COUNT(*) AS count FROM log_table INNER JOIN todo_table ON log_table.todo_id = todo_table.todo_id WHERE todo_table.group_id = :group_id GROUP BY log_table.operation")
    LiveData<List<LogCount>> getCountByLogOperation(long group_id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Log log);

    @Query("DELETE FROM log_table WHERE todo_id = :id")
    void deleteLogByTodoId(long id);

    @Query("DELETE FROM log_table")
    void deleteAll();
}
