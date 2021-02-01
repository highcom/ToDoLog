package com.highcom.todolog.ui.todolist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.highcom.todolog.R;
import com.highcom.todolog.datamodel.ToDoViewModel;

public class ToDoListFragment extends Fragment {

    private LifecycleOwner mOwner;
    private ToDoViewModel mToDoViewModel;

    public ToDoListFragment(LifecycleOwner owner) {
        this.mOwner = owner;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_todolist, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.todo_list_view);
        ToDoListAdapter adapter = new ToDoListAdapter(new ToDoListAdapter.ToDoDiff());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mToDoViewModel = new ViewModelProvider(this).get(ToDoViewModel.class);
        mToDoViewModel.getToDoList().observe(this.mOwner, todolist -> {
            adapter.submitList(todolist);
        });
    }
}
