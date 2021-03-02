package com.highcom.todolog.datamodel;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface GroupDao {
    @Query("SELECT * FROM group_table ORDER BY groupOrder ASC")
    LiveData<List<Group>> getGroupList();

    @Query("SELECT * FROM group_table ORDER BY groupOrder ASC LIMIT 1")
    LiveData<Group> getFirstGroup();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Group group);

    @Query("DELETE FROM group_table")
    void deleteAll();
}