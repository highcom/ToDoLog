package com.highcom.todolog.datamodel;

import androidx.room.Embedded;
import androidx.room.Relation;

public class ToDoAndLog {
    @Embedded public ToDo toDo;
    @Relation( parentColumn = "latest_log_id", entityColumn = "log_id")
    public Log log;
}
