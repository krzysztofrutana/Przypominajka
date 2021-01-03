package com.example.przypominajka.models;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

// class to save information about necessary information to create new eventModel object
public class EventModelProperties {

    private boolean itsMonthInterval;
    private int monthNumberOfRepeats;

    private boolean itsCustomTimeInterval;
    private int customTimeNumberOfRepeats;

    private boolean itsOneTimeEvent;
    private LocalDate oneTimeDate;

    private boolean itsEventDefaultTime;

    private int timeInterval;
    private LocalDate startDateLocalData;

    private LocalTime eventTIme;

    public EventModelProperties() {

    }

    public void setPropertiesForMonthIntervalEvent(int monthNumberOfRepeats, boolean itsEventDefaultTime, int timeInterval, LocalDate startDate, LocalTime eventTIme) {
        this.itsMonthInterval = true;
        this.monthNumberOfRepeats = monthNumberOfRepeats;
        this.itsEventDefaultTime = itsEventDefaultTime;
        this.timeInterval = timeInterval;
        this.startDateLocalData = startDate;
        this.eventTIme = eventTIme;

        this.itsCustomTimeInterval = false;
        this.customTimeNumberOfRepeats = 0;
        this.itsOneTimeEvent = false;
        this.oneTimeDate = null;
    }

    public void setPropertiesForCustomTimeIntervalEvent(int customTimeNumberOfRepeats, boolean itsEventDefaultTime, int timeInterval, LocalDate startDate, LocalTime eventTIme) {
        this.itsCustomTimeInterval = true;
        this.customTimeNumberOfRepeats = customTimeNumberOfRepeats;
        this.itsEventDefaultTime = itsEventDefaultTime;
        this.timeInterval = timeInterval;
        this.startDateLocalData = startDate;
        this.eventTIme = eventTIme;

        this.itsMonthInterval = false;
        this.monthNumberOfRepeats = 0;

        this.itsOneTimeEvent = false;
        this.oneTimeDate = null;
    }

    public void setPropertiesForOneTimeEvent(LocalDate oneTimeEventDate, boolean itsEventDefaultTime, LocalTime eventTIme) {

        this.itsOneTimeEvent = true;
        this.oneTimeDate = oneTimeEventDate;
        this.itsEventDefaultTime = itsEventDefaultTime;
        this.timeInterval = 0;
        this.startDateLocalData = LocalDate.now();
        this.eventTIme = eventTIme;

        this.itsMonthInterval = false;
        this.monthNumberOfRepeats = 0;

        this.itsCustomTimeInterval = false;
        this.customTimeNumberOfRepeats = 0;
    }

    public boolean isItsMonthInterval() {
        return itsMonthInterval;
    }

    public boolean isItsCustomTimeInterval() {
        return itsCustomTimeInterval;
    }

    public boolean isItsOneTimeEvent() {
        return itsOneTimeEvent;
    }

    public boolean isItsEventDefaultTime() {
        return itsEventDefaultTime;
    }

    public int getMonthNumberOfRepeats() {
        return monthNumberOfRepeats;
    }

    public int getCustomTimeNumberOfRepeats() {
        return customTimeNumberOfRepeats;
    }

    public int getTimeInterval() {
        return timeInterval;
    }

    public LocalDate getStartDateLocalData() {
        return startDateLocalData;
    }

    public long getStartDateInMillis() {
        return startDateLocalData.toDateTimeAtStartOfDay().getMillis();
    }

    public LocalDate getOneTimeDate() {
        return oneTimeDate;
    }

    public long getOneTimeDateInMillis() {
        return oneTimeDate.toDateTimeAtStartOfDay().getMillis();
    }

    public LocalTime getEventTIme() {
        return eventTIme;
    }

    public long getEventTimeInMillis() {
        return eventTIme.getMillisOfDay();
    }

}
