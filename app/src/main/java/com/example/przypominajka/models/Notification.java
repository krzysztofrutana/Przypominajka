package com.example.przypominajka.models;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

public class Notification {

    private String notificationName;
    private int notificationID;
    private long notificationTime;
    private long notificationDate;
    private boolean notificationCompleted;

    public Notification(String notificationName, int notificationID, LocalTime notificationTime, LocalDate notificationDate, boolean notificationCompleted) {
        this.notificationName = notificationName;
        this.notificationID = notificationID;
        this.notificationTime = notificationTime.getMillisOfDay();
        this.notificationDate = notificationDate.toDateTimeAtStartOfDay().getMillis();
        this.notificationCompleted = notificationCompleted;
    }

    public String getNotificationName() {
        return notificationName;
    }

    public int getNotificationID() {
        return notificationID;
    }

    public long getNotificationTimeInMillis() {
        return notificationTime;
    }

    public LocalTime getNotificationTime() {
        return new LocalTime(notificationTime, DateTimeZone.forID("UTC"));
    }

    public long getNotificationDateInMillis() {
        return notificationDate;
    }

    public LocalDate getNotificationDate() {
        return new LocalDate(notificationDate);
    }

    public boolean itsCompleted() {
        return notificationCompleted;
    }
}
