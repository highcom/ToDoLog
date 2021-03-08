package com.highcom.todolog.datamodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class GroupViewModel extends AndroidViewModel {
    private ToDoLogRepository mRepository;
    private LiveData<List<Group>> mGroupList;
    private LiveData<Group> mFirstGroup;

    public GroupViewModel(@NonNull Application application) {
        super(application);
        mRepository = ToDoLogRepository.getInstance(application);
        mGroupList = mRepository.getGroupList();
        mFirstGroup = mRepository.getFirstGroup();
    }

    public LiveData<List<Group>> getGroupList() {
        return mGroupList;
    }

    public LiveData<Group> getFirstGroup() {
        return mFirstGroup;
    }

    public void update(Group group) {
        mRepository.update(group);
    }

    public void update(List<Group> groupList) {
        mRepository.update(groupList);
    }

    public void insert(Group group) {
        mRepository.insert(group);
    }

    public void deleteGroupByGroupId(long groupId) {
        mRepository.deleteGroupByGroupId(groupId);
    }
}
