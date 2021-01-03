package com.example.przypominajka.models;

import com.example.przypominajka.databases.entities.EventModel;

import org.joda.time.LocalDate;

import java.util.List;

// class to save information about days of one not done event
public class NotDoneEventModel {

    private List<LocalDate> listOdNotDoneDays;
    private List<String> textForDays;
    private EventModel eventModel;

    public NotDoneEventModel(List<LocalDate> listOdNotDoneDays, List<String> textForDays, EventModel eventModel) {
        this.listOdNotDoneDays = listOdNotDoneDays;
        this.textForDays = textForDays;
        this.eventModel = eventModel;
    }

    public List<LocalDate> getListOdNotDoneDays() {
        return listOdNotDoneDays;
    }

    public List<String> getTextForDays() {
        return textForDays;
    }

    public EventModel getEventModel() {
        return eventModel;
    }
}
