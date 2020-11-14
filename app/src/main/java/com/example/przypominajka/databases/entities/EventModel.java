package com.example.przypominajka.databases.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

//Entity for Room Database to create table contains all events
//This class have all necessary method for Event class

@Entity(tableName = "EVENTS")
public class EventModel {

    private static final String TABLE_EVENTS = "EVENTS"; // main table name

    private static final String EVENT_NAME = "EVENT_NAME"; // strings
    private static final String EVENT_DISCRIPTION = "EVENT_DISCRIPTION"; // strings

    private static final String EVENT_COLOR = "EVENT_COLOR";//int

    private static final String MONTH_INTERVAL = "MONTH_INTERVAL"; // boolean
    private static final String MONTH_NUMBER_OF_REPEATS = "MONTH_NUMBER_OF_REPEATS"; // int

    private static final String CUSTOM_TIME_INTERVAL = "CUSTOM_TIME_INTERVAL"; // boolean
    private static final String CUSTOM_TIME_TYPE = "CUSTOM_TIME_TYPE"; // type (0 - none, 1 - day,2 - week,3 - month)
    private static final String CUSTOM_TIME_REPEATS_ALL_TIME = "CUSTOM_TIME_REPEATS_ALL_TIME";// boolean
    private static final String CUSTOM_TIME_NUMBER_OF_REPEATS = "CUSTOM_TIME_NUMBER_OF_REPEATS"; // int
    private static final String ONE_TIME = "ONE_TIME"; // boolean
    private static final String ONE_TIME_DATE = "ONE_TIME_DATE"; // date
    private static final String TIME_INTERVAL = "TIME_INTERVAL"; // int
    private static final String START_DATE = "START_DATE"; // date
    private static final String EVENT_DEFAULT_TIME = "EVENT_DEFAULT_TIME";
    private static final String EVENT_TIME = "EVENT_TIME";

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = EVENT_NAME)
    public String eventName;

    @ColumnInfo(name = EVENT_DISCRIPTION)
    public String eventDiscription;

    @ColumnInfo(name = EVENT_COLOR)
    public int eventColor;

    @ColumnInfo(name = MONTH_INTERVAL)
    public boolean itsMonthInterval;

    @ColumnInfo(name = MONTH_NUMBER_OF_REPEATS)
    public int monthNumberOfRepeats;

    @ColumnInfo(name = CUSTOM_TIME_INTERVAL)
    public boolean itsCustomTimeInterval;

    @ColumnInfo(name = CUSTOM_TIME_TYPE)
    public int customTimeType;

    @ColumnInfo(name = CUSTOM_TIME_REPEATS_ALL_TIME)
    public boolean customTimeRepeatsAllTime;

    @ColumnInfo(name = CUSTOM_TIME_NUMBER_OF_REPEATS)
    public int customTimeNumberOfRepeats;

    @ColumnInfo(name = ONE_TIME)
    public boolean itsOneTime;

    @ColumnInfo(name = ONE_TIME_DATE)
    public long oneTimeDate;

    @ColumnInfo(name = TIME_INTERVAL)
    public int timeInterval;

    @ColumnInfo(name = START_DATE)
    public long startDate;

    @ColumnInfo(name = EVENT_DEFAULT_TIME)
    public boolean itsEventDefaultTime;

    @ColumnInfo(name = EVENT_TIME)
    public long eventTime;

    public EventModel() {
    }


    @Ignore
    public EventModel(String eventName, String eventDiscription, int eventColor, boolean itsMonthInterval, int monthNumberOfRepeats,
                      boolean itsCustomTimeInterval, int customTimeType, boolean customTimeRepeatsAllTime, int customTimeNumberOfRepeats,
                      boolean itsOneTime, LocalDate oneTimeDate, int timeInterval, LocalDate startDate, boolean itsEventDefaultTime, long eventTime) {

        this.eventName = eventName;
        // this is needed because SQLite not support space in name
        this.eventName = this.eventName.replaceAll(" ", "_");
        this.eventDiscription = eventDiscription;
        this.eventColor = eventColor;
        this.itsMonthInterval = itsMonthInterval;
        this.monthNumberOfRepeats = monthNumberOfRepeats;
        this.itsCustomTimeInterval = itsCustomTimeInterval;
        this.customTimeType = customTimeType;
        this.customTimeRepeatsAllTime = customTimeRepeatsAllTime;
        this.customTimeNumberOfRepeats = customTimeNumberOfRepeats;
        this.itsOneTime = itsOneTime;
        // convert LocalData to millisecond
        if (oneTimeDate != null) {
            this.oneTimeDate = oneTimeDate.toDateTimeAtStartOfDay().getMillis();
        } else {
            this.oneTimeDate = 0;
        }
        this.timeInterval = timeInterval;
        // convert LocalData to millisecond
        this.startDate = startDate.toDateTimeAtStartOfDay().getMillis();

        this.itsEventDefaultTime = itsEventDefaultTime;
        this.eventTime = eventTime;
    }

    @Ignore
    public int getId() {
        return id;
    }

    @Ignore
    public String getEventName() {
        return eventName;
    }

    @Ignore
    public String getEventDiscription() {
        return eventDiscription;
    }

    @Ignore
    public int getEventColor() {
        return eventColor;
    }

    @Ignore
    public boolean getItsMonthInterval() {
        return itsMonthInterval;
    }

    @Ignore
    public int getMonthNumberOfRepeats() {
        return monthNumberOfRepeats;
    }

    @Ignore
    public boolean getItCustomTimeInterval() {
        return itsCustomTimeInterval;
    }

    @Ignore
    public int getCustomTimeType() {
        return customTimeType;
    }

    @Ignore
    public boolean getItsCustomTimeRepeatsAllTime() {
        return customTimeRepeatsAllTime;
    }

    @Ignore
    public int getCustomTimeNumberOfRepeats() {
        return customTimeNumberOfRepeats;
    }

    @Ignore
    public boolean getItsOneTimeEvent() {
        return itsOneTime;
    }

    @Ignore
    public LocalDate getOneTimeEventDate() {
        return new LocalDate(oneTimeDate);
    }

    @Ignore
    public long getOneTimeEventDateInMillis() {
        return oneTimeDate;
    }

    @Ignore
    public boolean getEventTimeDefault() {
        return itsEventDefaultTime;
    }

    @Ignore
    public LocalTime getEventTime() {
        return new LocalTime(eventTime, DateTimeZone.UTC);
    }

    @Ignore
    public long getEventTimeInMillis() {
        return eventTime;
    }

    @Ignore
    public int getTimeInterval() {
        return timeInterval;
    }

    // convert date from millis to LocalData
    @Ignore
    public LocalDate getStartDate() {
        return new LocalDate(startDate);
    }

    @Ignore
    public long getStartDateInMillis() {
        return startDate;
    }

}
