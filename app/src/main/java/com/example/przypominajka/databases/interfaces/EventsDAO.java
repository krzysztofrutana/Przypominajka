package com.example.przypominajka.databases.interfaces;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.przypominajka.databases.entities.EventModel;

import java.util.List;

//DAO for EventModel class for Room Database
//INSERT, UPDATE and DELETE method return long/int value to check results of query
//getAll method in two version, returns normal List or LiveData list
@Dao
public interface EventsDAO {

    @Insert
    long insertEvent(EventModel event);

    @Delete
    int delete(EventModel event);

    @Query("SELECT * FROM EVENTS WHERE EVENT_NAME LIKE :eventName")
    EventModel findByEventName(String eventName);

    @Query("SELECT * FROM EVENTS")
    LiveData<List<EventModel>> getAll();

    @Query("SELECT * FROM EVENTS")
    List<EventModel> getAllList();

    @Query("SELECT id FROM EVENTS WHERE EVENT_NAME LIKE :eventName")
    int getEventID(String eventName);


}
