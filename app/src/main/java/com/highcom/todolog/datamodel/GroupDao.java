package com.highcom.todolog.datamodel;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface GroupDao {
    @Query("SELECT * FROM group_table ORDER BY groupOrder ASC")
    LiveData<List<Group>> getGroupList();

    @Query("SELECT * FROM group_table ORDER BY groupOrder ASC LIMIT 1")
    LiveData<Group> getFirstGroup();

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(Group group);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(List<Group> groups);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Group group);

    @Query("DELETE FROM group_table WHERE group_id = :id")
    void deleteByGroupId(long id);

    @Query("DELETE FROM group_table")
    void deleteAll();
}
