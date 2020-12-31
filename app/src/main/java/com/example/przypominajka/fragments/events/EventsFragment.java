package com.example.przypominajka.fragments.events;

import android.content.Context;
import android.content.Intent;
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

import com.example.przypominajka.activities.AddNewEventActivity;

import com.example.przypominajka.R;
import com.example.przypominajka.adapters.EventListAdapter;
import com.example.przypominajka.databases.viewModels.EventsViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class EventsFragment extends Fragment {

    Context context;

    FloatingActionButton fab;

    View v;

    EventListAdapter adapter;
    RecyclerView recyclerViewListOfEvents;

    EventsViewModel taskListViewModel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        taskListViewModel = new ViewModelProvider(this).get(EventsViewModel.class);
        View v = inflater.inflate(R.layout.fragment_events, container, false);
        fab = v.findViewById(R.id.floating_action_button);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(context, AddNewEventActivity.class);
            startActivity(intent);
            adapter.notifyDataSetChanged();
        });

        return v;
    }

    @Override
    public void onAttach(Context cont) {
        super.onAttach(context);
        context = cont;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        v = view;

        recyclerViewListOfEvents = view.findViewById(R.id.allEventList);
        RecyclerView.LayoutManager recycelLayoutManager = new
                LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerViewListOfEvents.setLayoutManager(recycelLayoutManager);
        adapter = new EventListAdapter(context, false);
        recyclerViewListOfEvents.setAdapter(adapter);
        showAllEventList();
    }

    @Override
    public void onResume() {

        showAllEventList();
        super.onResume();
    }


    public void showAllEventList() {

        taskListViewModel.getAllEvents()
                .observe(getViewLifecycleOwner(), eventModels -> adapter.setList(eventModels, false));

    }
}
