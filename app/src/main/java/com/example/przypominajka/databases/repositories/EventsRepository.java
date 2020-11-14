package com.example.przypominajka.databases.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.przypominajka.databases.PrzypominajkaDatabase;
import com.example.przypominajka.databases.entities.EventModel;
import com.example.przypominajka.databases.interfaces.EventsDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

//Repository layer for EventModel class using Room Database
//INSERT, UPDATE and DELETE method return long/int value to check results of query
//all methods are run on a separate thread
//getAllEvents return LiveData, so automatically use separated thread
public class EventsRepository {

    private EventsDAO eventsDAO;

    private LiveData<List<EventModel>> allEvent;


    public EventsRepository(Application application) {
        PrzypominajkaDatabase db = PrzypominajkaDatabase.getDatabase(application);
        eventsDAO = db.eventsDAO();
        allEvent = eventsDAO.getAll();

    }

    public LiveData<List<EventModel>> getAllEvents() {
        return allEvent;
    }

    public List<EventModel> getAllEventsList() {
        List<EventModel> allEvents = new ArrayList<>();
        try {
            allEvents = PrzypominajkaDatabase.databaseWriteExecutor.submit(() -> eventsDAO.getAllList()).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return allEvents;
    }

    public EventModel findByEventName(String eventName) {
        EventModel event = new EventModel();
        try {
            event = PrzypominajkaDatabase.databaseWriteExecutor.submit(() -> eventsDAO.findByEventName(eventName)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return event;
    }

    public long insertEvent(final EventModel event) {
        long results = -1;
        try {
            results = PrzypominajkaDatabase.databaseWriteExecutor.submit(() -> eventsDAO.insertEvent(event)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return results;
    }

    public int deleteEvent(final EventModel event) {
        int results = -1;
        try {
            results = PrzypominajkaDatabase.databaseWriteExecutor.submit(() -> eventsDAO.delete(event)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return results;
    }

    public int getEventID(String eventName) {
        int eventId = -1;
        try {
            eventId = Executors.newSingleThreadExecutor().submit(() ->
                    eventsDAO.getEventID(eventName)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return eventId;
    }
}
