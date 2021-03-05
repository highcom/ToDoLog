package com.highcom.todolog.ui.grouplist;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.highcom.todolog.R;
import com.highcom.todolog.datamodel.GroupViewModel;
import com.highcom.todolog.ui.todolist.ToDoListAdapter;

public class GroupListFragment extends Fragment {

    private GroupViewModel mGroupViewModel;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_grouplist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.group_list_view);
        GroupListAdapter adapter = new GroupListAdapter(new GroupListAdapter.GroupDiff());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mGroupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);
        mGroupViewModel.getGroupList().observe(getViewLifecycleOwner(), groupList -> {
            adapter.submitList(groupList);
        });
    }
}