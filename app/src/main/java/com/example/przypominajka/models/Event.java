package com.example.przypominajka.models;

import android.util.Log;

import org.joda.time.LocalDate;

public class Event {

    private String eventName;
    private String eventDiscription;
    private int eventColor;

    private boolean monthInterval;
    private int monthNumberOfRepeats;

    private boolean shortTimeInterval;
    private int shortTimeType; // type (0 - none, 1 - day,2 - week,3 - month)
    private boolean shortTimeRepeatAllTime;
    private int shortTimeNumberOfRepeats;

    private boolean oneTimeEvent;
    private long oneTimeEventDate;

    private int timeIntervalOfRepeat;
    private long startDate;


    public Event(String eventName, String eventDiscription, int eventColor, boolean monthInterval, int monthNumberOfRepeats,
                 boolean shortTimeInterval, int shortTimeType, boolean shortTimeRepeatAllTime, int shortTimeNumberOfRepeats,
                 boolean oneTimeEvent, LocalDate oneTimeEventDate, int timeIntervalOfRepeat, LocalDate startDate) {
        this.eventName = eventName;
        // this is needed because SQLite not support space in name
        this.eventName = this.eventName.replaceAll(" ", "_");
        this.eventDiscription = eventDiscription;
        this.eventColor = eventColor;
        this.monthInterval = monthInterval;
        this.monthNumberOfRepeats = monthNumberOfRepeats;
        this.shortTimeInterval = shortTimeInterval;
        this.shortTimeType = shortTimeType;
        this.shortTimeRepeatAllTime = shortTimeRepeatAllTime;
        this.shortTimeNumberOfRepeats = shortTimeNumberOfRepeats;
        this.oneTimeEvent = oneTimeEvent;
        // convert LocalData to millisecond
        if (oneTimeEventDate != null) {
            this.oneTimeEventDate = oneTimeEventDate.toDateTimeAtStartOfDay().getMillis();
        } else {
            this.oneTimeEventDate = 0;
        }


        this.timeIntervalOfRepeat = timeIntervalOfRepeat;
        // convert LocalData to millisecond
        this.startDate = startDate.toDateTimeAtStartOfDay().getMillis();

    }

    public String getEventName() {
        return eventName;
    }

    public String getEventDiscription() {
        return eventDiscription;
    }

    public int getEventColor() {
        return eventColor;
    }

    // maybe will be needed in future :)
    public String getType() {
        if (monthInterval) {
            return "Month Interval";
        } else if (shortTimeInterval) {
            return "Day Interval";
        } else if (oneTimeEvent) {
            return "One time event";
        } else {
            return "Interval isn't choose";
        }
    }

    public boolean getItsMonthInterval() {
        return monthInterval;
    }

    public int getMonthNumberOfRepeats() {
        return monthNumberOfRepeats;
    }

    public boolean getItsShortTimeInterval() {
        return shortTimeInterval;
    }

    public int getShortTimeType() {
        return shortTimeType;
    }

    public boolean getItsShortTimeRepeatsAllTime() {
        return shortTimeRepeatAllTime;
    }

    public int getShortTimeNumberOfRepeats() {
        return shortTimeNumberOfRepeats;
    }

    public boolean getItsOneTimeEvent() {
        return oneTimeEvent;
    }

    public LocalDate getOneTimeEventDate() {
        return new LocalDate(oneTimeEventDate);
    }

    public long getOneTimeEventDateInMillis() {
        return oneTimeEventDate;
    }

    public int getTimeInterval() {
        return timeIntervalOfRepeat;
    }

    // convert date from millis to LocalData
    public LocalDate getStartDate() {
        return new LocalDate(startDate);
    }

    public long getStartDateInMillis() {
        return startDate;
    }


}


