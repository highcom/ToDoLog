package com.highcom.todolog.datamodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class ToDoViewModel extends AndroidViewModel {
    private ToDoLogRepository mRepository;

    public ToDoViewModel(@NonNull Application application) {
        super(application);
        mRepository = ToDoLogRepository.getInstance(application);
    }

    public LiveData<List<ToDoAndLog>> getToDoListByTaskGroup(long groupId) {
        return mRepository.getTodoListByTaskGroup(groupId);
    }

    public void deleteToDoByTodoId(long todoId) {
        mRepository.deleteToDoByTodoId(todoId);
        mRepository.deleteLogByTodoId(todoId);
    }

    public void updateToDoAndLog(ToDo toDo, Log log) {
        mRepository.updateToDoAndLog(toDo, log);
    }

    public void updateToDoAndLog(List<ToDo> toDoList, long targetToDoId, Log log) {
        mRepository.updateToDoAndLog(toDoList, targetToDoId, log);
    }

    public void update(ToDo todo) {
        mRepository.update(todo);
    }

    public void update(List<ToDo> todoList) {
        mRepository.updateToDoList(todoList);
    }

    public void insertToDoAndLog(ToDo todo, Log log) {
        mRepository.insertToDoAndLog(todo, log);
    }

    public void insert(ToDo todo) {
        mRepository.insert(todo);
    }

    public void insert(Log log) {
        mRepository.insert(log);
    }
}
