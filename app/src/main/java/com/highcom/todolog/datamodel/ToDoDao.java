package com.highcom.todolog.datamodel;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ToDoDao {
    @Transaction
    @Query("SELECT * FROM todo_table WHERE group_id = :groupId ORDER BY state ASC, todo_order ASC")
    LiveData<List<ToDoAndLog>> getToDoListByTaskGroup(long groupId);

    @Query("SELECT todo_id FROM todo_table WHERE group_id = :groupId")
    List<Long> getToDoIdListByTaskGroup(long groupId);

    @Query("SELECT * FROM todo_table WHERE todo_id = :id")
    LiveData<ToDo> getToDo(long id);

    @Query("SELECT * FROM todo_table WHERE todo_id = :id")
    ToDo getToDoSync(long id);

    @Query("SELECT todo_order FROM todo_table WHERE group_id = :groupId AND state = :state ORDER BY todo_order DESC LIMIT 1")
    int getToDoOrderByGroupIdAndState(long groupId, int state);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(ToDo todo);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(List<ToDo> todos);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(ToDo todo);

    @Query("DELETE FROM todo_table WHERE todo_id = :id")
    void deleteByTodoId(long id);

    @Query("DELETE FROM todo_table WHERE group_id = :id")
    void deleteByGroupId(long id);

    @Query("DELETE FROM todo_table")
    void deleteAll();
}
