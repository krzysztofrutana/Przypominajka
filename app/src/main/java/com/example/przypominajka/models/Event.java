package com.example.przypominajka.models;

import android.util.Log;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

public class Event {

    private String eventName;
    private String eventDiscription;
    private int eventColor;

    private boolean monthInterval;
    private int monthNumberOfRepeats;

    private boolean customTimeInterval;
    private int customTimeType; // type (0 - none, 1 - day,2 - week,3 - month)
    private boolean customTimeRepeatAllTime;
    private int customTimeNumberOfRepeats;

    private boolean oneTimeEvent;
    private long oneTimeEventDate;

    private boolean eventTimeDefault;
    private long eventTime;

    private int timeIntervalOfRepeat;
    private long startDate;


    public Event(String eventName, String eventDiscription, int eventColor, boolean monthInterval, int monthNumberOfRepeats,
                 boolean customTimeInterval, int customTimeType, boolean customTimeRepeatAllTime, int customTimeNumberOfRepeats,
                 boolean oneTimeEvent, LocalDate oneTimeEventDate, boolean eventTimeDefault, long eventTime,
                 int timeIntervalOfRepeat, LocalDate startDate) {
        this.eventName = eventName;
        // this is needed because SQLite not support space in name
        this.eventName = this.eventName.replaceAll(" ", "_");
        this.eventDiscription = eventDiscription;
        this.eventColor = eventColor;
        this.monthInterval = monthInterval;
        this.monthNumberOfRepeats = monthNumberOfRepeats;
        this.customTimeInterval = customTimeInterval;
        this.customTimeType = customTimeType;
        this.customTimeRepeatAllTime = customTimeRepeatAllTime;
        this.customTimeNumberOfRepeats = customTimeNumberOfRepeats;
        this.oneTimeEvent = oneTimeEvent;
        // convert LocalData to millisecond
        if (oneTimeEventDate != null) {
            this.oneTimeEventDate = oneTimeEventDate.toDateTimeAtStartOfDay().getMillis();
        } else {
            this.oneTimeEventDate = 0;
        }

        this.eventTimeDefault = eventTimeDefault;
        this.eventTime = eventTime;

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
        } else if (customTimeInterval) {
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


    public boolean getItCustomTimeInterval() {
        return customTimeInterval;
    }

    public int getCustomTimeType() {
        return customTimeType;
    }

    public boolean getItsCustomTimeRepeatsAllTime() {
        return customTimeRepeatAllTime;
    }

    public int getCustomTimeNumberOfRepeats() {
        return customTimeNumberOfRepeats;
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

    public boolean getEventTimeDefault() {
        return eventTimeDefault;
    }

    public LocalTime getEventTime() {
        return new LocalTime(eventTime, DateTimeZone.UTC);
    }

    public long getEventTimeInMillis() {
        return eventTime;
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


