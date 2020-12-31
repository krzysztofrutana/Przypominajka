package com.example.przypominajka.databases.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

//Entity for Room Database to create table contains all notifications
//This class have all necessary method for Notification class
@Entity(tableName = "NOTIFICATIONS")
public class NotificationModel {

    private static final String TABLE_NOTIFICATIONS = "NOTIFICATIONS";
    private static final String NOTIFICATION_EVENT_NAME = "NOTIFICATION_EVENT_NAME";
    private static final String NOTIFICATION_ID = "NOTIFICATION_ID";
    private static final String NOTIFICATION_DATE = "NOTIFICATION_DATE";
    private static final String NOTIFICATION_TIME = "NOTIFICATION_TIME";
    private static final String NOTIFICATION_COMPLETED = "NOTIFICATION_COMPLETED";

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = NOTIFICATION_EVENT_NAME)
    public String notificationEventName;

    @ColumnInfo(name = NOTIFICATION_ID)
    public int notificationEventID;

    @ColumnInfo(name = NOTIFICATION_DATE)
    public long notificationDate;

    @ColumnInfo(name = NOTIFICATION_TIME)
    public long notificationTime;

    @ColumnInfo(name = NOTIFICATION_COMPLETED)
    public boolean itsNotificationComplete;

    public NotificationModel() {
    }

    @Ignore
    public NotificationModel(String notificationName, int notificationID, LocalTime notificationTime, LocalDate notificationDate, boolean notificationCompleted) {
        this.notificationEventName = notificationName;
        this.notificationEventID = notificationID;
        this.notificationTime = notificationTime.getMillisOfDay();
        this.notificationDate = notificationDate.toDateTimeAtStartOfDay().getMillis();
        this.itsNotificationComplete = notificationCompleted;
    }

    public String getNotificationName() {
        return notificationEventName;
    }

    public int getNotificationID() {
        return notificationEventID;
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
        return itsNotificationComplete;
    }


}
