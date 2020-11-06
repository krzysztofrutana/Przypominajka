package com.example.przypominajka.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.content.Intent;
import android.util.Log;;

import androidx.annotation.LongDef;

import com.example.przypominajka.databases.PrzypominajkaDatabaseHelper;
import com.example.przypominajka.models.Event;
import com.example.przypominajka.models.Notification;
import com.example.przypominajka.utils.ReminderBroadcast;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

// class to set alarm for notification, run in custom time interval or when turn on application
// method of setting alarm is this same as in AddNewActivity class
public class SetAlarmService extends android.app.job.JobService {

    PrzypominajkaDatabaseHelper przypominajkaDatabaseHelper = new PrzypominajkaDatabaseHelper(this);
    JobParameters mParams;
    LocalDate nextDayDate;

    @Override
    public boolean onStartJob(JobParameters params) {
        mParams = params;

        new Thread(new Runnable() {
            @Override
            public void run() {
                setNotify();
            }
        }).start();
        if (mParams != null) {
            jobFinished(mParams, false);

        }
        return true;
    }

    // this isn't very important and quick task, so if job stop nothing happen
    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private void setNotify() {
        List<Event> allEvent;
        List<Event> forNextDayDefaultTime;
        try {
            Log.d("OnStartJob", "Rozpoczęte");
            nextDayDate = LocalDate.now();
            nextDayDate = nextDayDate.plusDays(1);
            allEvent = przypominajkaDatabaseHelper.getEventForCurrentDay(nextDayDate);
            forNextDayDefaultTime = new ArrayList<>();
        } catch (Exception e) {
            Log.d("setNotify", "Problem z pobraniem wydarzen " + e.getMessage());
            return;
        }
        if (allEvent.size() > 0) {
            try {
                for (Event tempEvent : allEvent) {
                    // check if its not one time event
                    if (tempEvent.getEventTimeDefault()) {
                        if (!tempEvent.getItsOneTimeEvent()) {
                            forNextDayDefaultTime.add(tempEvent);
                        }
                    } else if (!tempEvent.getEventTimeDefault()) {
                        // set alarm for event which dont have default time for alarm
                        if (!tempEvent.getItsOneTimeEvent()) {
                            boolean isCreated = przypominajkaDatabaseHelper.checkIfNotificationIsCreated(tempEvent.getEventName(), nextDayDate);
                            Log.d("OnStartJob boolean", String.valueOf(isCreated));
                            if (!isCreated) {
                                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                                int year = nextDayDate.getYear();
                                String yearShortString = Integer.toString(year).substring(2);
                                int yearShort = Integer.parseInt(yearShortString);

                                int notifyPendingIntentID = Integer.parseInt(String.valueOf(przypominajkaDatabaseHelper.getEventId(tempEvent.getEventName()) +
                                        String.valueOf(nextDayDate.getDayOfYear()) + String.valueOf(yearShort)));

                                Intent notificationIntent = new Intent(getApplicationContext(), ReminderBroadcast.class);
                                notificationIntent.putExtra("NOTIFY_TEXT", tempEvent.getEventName());
                                notificationIntent.putExtra("ID", notifyPendingIntentID);

                                PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(),
                                        notifyPendingIntentID,
                                        notificationIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT);

                                Log.d("setAlarmService", "ID " + notifyPendingIntentID);
                                LocalTime eventTime = tempEvent.getEventTime();


                                DateTime tempEventTime = new DateTime(nextDayDate.getYear(), nextDayDate.getMonthOfYear(),
                                        nextDayDate.getDayOfMonth(), eventTime.getHourOfDay(), eventTime.getMinuteOfHour())
                                        .withZoneRetainFields(DateTimeZone.forID(TimeZone.getDefault().getID()));

                                alarmManager.setExact(AlarmManager.RTC_WAKEUP, tempEventTime.getMillis(), alarmIntent);

                                przypominajkaDatabaseHelper.updateNotificationCreatedColumn(tempEvent.getEventName(), true, nextDayDate);

                                Log.d("OnStartJob", "Stworzono powiadomienie dla " + tempEvent.getEventName() + " o godzinie " + tempEventTime.toString());
                                Notification newNotification = new Notification(tempEvent.getEventName(), notifyPendingIntentID,
                                        new LocalTime(tempEventTime.getHourOfDay(), tempEventTime.getMinuteOfHour()), tempEventTime.toLocalDate(), false);
                                boolean insertNotify = przypominajkaDatabaseHelper.insertNotification(newNotification);

                                if (insertNotify) {
                                    Log.d("OnStartJob", "Dodano powiadomienie dla " + tempEvent.getEventName() + " " + tempEventTime.toLocalDate().toString()
                                            + " " + new LocalTime(tempEventTime.getHourOfDay(), tempEventTime.getMinuteOfHour()).toString());
                                } else {
                                    Log.d("OnStartJob", "Nie udało się dodać informacji o powiadomieniu");
                                }
                            }
                        }
                    }

                }
            } catch (Exception e) {
                Log.d("OnStartJob", "Problem z stworzeniem powiadomienia dla wydarzen z czasem innym niż domyślny " + e.getMessage());
                return;
            }
            try {
                // build alarm for all events for current day they have set default time
                StringBuilder eventsWithDefaultTime = new StringBuilder();
                if (forNextDayDefaultTime.size() > 0) {
                    for (Event eventWithDefaultTime : forNextDayDefaultTime) {
                        if (eventsWithDefaultTime.toString().equals("")) {
                            if (!przypominajkaDatabaseHelper.checkIfNotificationIsCreated(eventWithDefaultTime.getEventName(), nextDayDate)) {
                                przypominajkaDatabaseHelper.updateNotificationCreatedColumn(eventWithDefaultTime.getEventName(), true, nextDayDate);
                                eventsWithDefaultTime = new StringBuilder(eventWithDefaultTime.getEventName());
                            } else {
                                eventsWithDefaultTime = new StringBuilder(eventWithDefaultTime.getEventName());
                            }
                        } else {
                            if (!przypominajkaDatabaseHelper.checkIfNotificationIsCreated(eventWithDefaultTime.getEventName(), nextDayDate)) {
                                przypominajkaDatabaseHelper.updateNotificationCreatedColumn(eventWithDefaultTime.getEventName(), true, nextDayDate);
                                eventsWithDefaultTime.append(", ").append(eventWithDefaultTime.getEventName());
                            } else {
                                eventsWithDefaultTime.append(", ").append(eventWithDefaultTime.getEventName());
                            }
                        }
                    }
                }
                if (!eventsWithDefaultTime.toString().equals("")) {
                    Notification tempNotification = new Notification(eventsWithDefaultTime.toString(), -nextDayDate.getDayOfYear(),
                            new LocalTime(przypominajkaDatabaseHelper.getDefaultTime(), DateTimeZone.forID("UTC")), nextDayDate, false);
                    if (!przypominajkaDatabaseHelper.checkNotificationCreatedButNotNotify(tempNotification)) {
                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);


                        Intent notificationIntent = new Intent(getApplicationContext(), ReminderBroadcast.class);
                        notificationIntent.putExtra("NOTIFY_TEXT", eventsWithDefaultTime.toString());
                        notificationIntent.putExtra("ID", -nextDayDate.getDayOfYear());

                        PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(),
                                -nextDayDate.getDayOfYear(),
                                notificationIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);
                        Log.d("OnStartJob", String.valueOf(nextDayDate.getDayOfYear()));

                        LocalTime eventTime = new LocalTime(przypominajkaDatabaseHelper.getDefaultTime(), DateTimeZone.forID("UCT"));
                        Log.d("setAlarmService", eventTime.toString());

                        DateTime tempEventTime = new DateTime(nextDayDate.getYear(), nextDayDate.getMonthOfYear(),
                                nextDayDate.getDayOfMonth(), eventTime.getHourOfDay(), eventTime.getMinuteOfHour())
                                .withZoneRetainFields(DateTimeZone.forID(TimeZone.getDefault().getID()));

                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, tempEventTime.getMillis(), alarmIntent);

                        boolean insertNotify = przypominajkaDatabaseHelper.insertNotification(tempNotification);
                        if (insertNotify) {
                            Log.d("OnStartJob", "Dodano powiadomienie dla " + eventsWithDefaultTime + " " + tempEventTime.toLocalDate().toString()
                                    + " " + new LocalTime(tempEventTime.getHourOfDay(), tempEventTime.getMinuteOfHour()).toString());
                        } else {
                            Log.d("OnStartJob", "Nie udało się dodać informacji o powiadomieniu");
                        }
                        Log.d("OnStartJob", "Stworzono powiadomienie dla " + eventsWithDefaultTime + " o godzinie " + tempEventTime.toString());
                    }
                }
            } catch (Exception e) {
                Log.d("OnStartJob", "Problem z stworzeniem powiadomienia dla wydarzen domyślnych " + e.getMessage());
            }
        }
    }
}