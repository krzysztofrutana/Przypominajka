package com.example.przypominajka.fragments.events;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.przypominajka.activities.AddNewEventActivity;
import com.example.przypominajka.models.Event;
import com.example.przypominajka.activities.EventDetailsActivity;
import com.example.przypominajka.databases.PrzypominajkaDatabaseHelper;
import com.example.przypominajka.R;
import com.example.przypominajka.adapters.EventsListColorAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class EventsFragment extends Fragment {

    PrzypominajkaDatabaseHelper przypominajkaDatabaseHelper;
    Context context;

    FloatingActionButton fab;

    View v;

    ListView eventList;
    ArrayList<Event> eventArray = new ArrayList<Event>();
    EventsListColorAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View v = inflater.inflate(R.layout.fragment_events, container, false);
        fab = (FloatingActionButton) v.findViewById(R.id.floating_action_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AddNewEventActivity.class);
                startActivity(intent);
                adapter.notifyDataSetChanged();
            }
        });

        return v;
    }

    @Override
    public void onAttach(Context cont) {
        super.onAttach(context);
        context = cont;
        przypominajkaDatabaseHelper = new PrzypominajkaDatabaseHelper(context);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        v = view;

        eventList = view.findViewById(R.id.allEventList);

        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showAllEventList();
            }
        });
    }

    @Override
    public void onResume() {

        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showAllEventList();
            }
        });
        super.onResume();
    }


    public void showAllEventList() {

        List<Event> events = przypominajkaDatabaseHelper.getAllEvent();
        eventArray.clear();
        if (events.size() > 0) {

            eventArray.addAll(events);
            adapter = new EventsListColorAdapter(context, R.layout.row_list, eventArray);
            eventList.setAdapter(adapter);
            eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent eventDetail = new Intent(context.getApplicationContext(), EventDetailsActivity.class);

                    EventsListColorAdapter customAdapter = (EventsListColorAdapter) parent.getAdapter();
                    Event event = customAdapter.getItem(position);

                    assert event != null;
                    eventDetail.putExtra("EVENT_NAME", event.getEventName());
                    startActivity(eventDetail);
                }
            });
        } else {
            adapter = new EventsListColorAdapter(context, R.layout.row_list, eventArray);
            eventList.setAdapter(adapter);
        }
    }
}
