package com.example.przypominajka.databases.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.przypominajka.databases.PrzypominajkaDatabaseHelper;
import com.example.przypominajka.databases.entities.EventModel;
import com.example.przypominajka.databases.repositories.EventsRepository;

import java.util.List;

// ViewModel Layer for event model and event repository
public class EventsViewModel extends AndroidViewModel {

    private EventsRepository eventsRepository;
    private LiveData<List<EventModel>> allEvents;


    public EventsViewModel(@NonNull Application application) {
        super(application);
        eventsRepository = new EventsRepository(application);
        allEvents = eventsRepository.getAllEvents();
    }

    public LiveData<List<EventModel>> getAllEvents() {
        return eventsRepository.getAllEvents();
    }

    public List<EventModel> getAllEventsList() {
        return eventsRepository.getAllEventsList();
    }

    public EventModel findByEventName(String eventName) {
        return eventsRepository.findByEventName(eventName);
    }

    //it is necessary to call two methods, one to add an event by ROOM database, the other to create a day table for the event
    public long insertEvent(EventModel event) {
        long resultSQL = eventsRepository.insertEvent(event);
        boolean resultCreateTable = PrzypominajkaDatabaseHelper.insertEvent(event);
        if (resultSQL == -1 || !resultCreateTable) {
            return -1;
        } else {
            return resultSQL;
        }
    }

    // after delete event from events table, also must be deleted event table with days of event
    public int deleteEvent(EventModel event) {
        int resultOfDeleteEvent = eventsRepository.deleteEvent(event);
        PrzypominajkaDatabaseHelper.deleteEvent(event.getEventName());
        return resultOfDeleteEvent;
    }

    public int getEventID(String eventName) {
        return eventsRepository.getEventID(eventName);
    }

    public int updateEvent(EventModel event) {
        return eventsRepository.updateEvent(event);
    }

}
