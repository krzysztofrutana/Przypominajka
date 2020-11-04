package com.example.przypominajka.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.content.Intent;
import android.util.Log;;

import com.example.przypominajka.databases.PrzypominajkaDatabaseHelper;
import com.example.przypominajka.models.Event;
import com.example.przypominajka.utils.ReminderBroadcast;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;

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

                                Intent notificationIntent = new Intent(getApplicationContext(), ReminderBroadcast.class);
                                notificationIntent.putExtra("NOTIFY_TEXT", tempEvent.getEventName());
                                notificationIntent.putExtra("ID", przypominajkaDatabaseHelper.getEventId(tempEvent.getEventName()));
                                notificationIntent.putExtra("TIME", nextDayDate.toString());

                                PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(),
                                        przypominajkaDatabaseHelper.getEventId(tempEvent.getEventName()),
                                        notificationIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT);

                                LocalTime eventTime = tempEvent.getEventTime();

                                DateTime tempEventTime = new DateTime(nextDayDate.getYear(), nextDayDate.getMonthOfYear(),
                                        nextDayDate.getDayOfMonth(), eventTime.getHourOfDay(), eventTime.getMinuteOfHour());

                                alarmManager.set(AlarmManager.RTC_WAKEUP, tempEventTime.getMillis(), alarmIntent);

                                przypominajkaDatabaseHelper.updateNotificationCreatedColumn(tempEvent.getEventName(), true, nextDayDate);

                                Log.d("OnStartJob", "Stworzono powiadomienie dla " + tempEvent.getEventName() + " o godzinie " + tempEventTime.toString());
                                boolean insertNotify = przypominajkaDatabaseHelper.insertNotification(tempEvent.getEventName(),
                                        new LocalTime(tempEventTime.getHourOfDay(), tempEventTime.getMinuteOfHour()),
                                        tempEventTime.toLocalDate(), false);

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
                if (!przypominajkaDatabaseHelper.checkNotificationCreatedButNotNotify(eventsWithDefaultTime.toString(), nextDayDate, new LocalTime(przypominajkaDatabaseHelper.getDefaultTime()))) {
                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                    Intent notificationIntent = new Intent(getApplicationContext(), ReminderBroadcast.class);
                    notificationIntent.putExtra("NOTIFY_TEXT", eventsWithDefaultTime.toString());
                    notificationIntent.putExtra("ID", -nextDayDate.getDayOfYear());

                    PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(),
                            -nextDayDate.getDayOfYear(),
                            notificationIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    Log.d("OnStartJob", String.valueOf(nextDayDate.getDayOfYear()));

                    LocalTime eventTime = new LocalTime(przypominajkaDatabaseHelper.getDefaultTime());

                    DateTime tempEventTime = new DateTime(nextDayDate.getYear(), nextDayDate.getMonthOfYear(),
                            nextDayDate.getDayOfMonth(), eventTime.getHourOfDay(), eventTime.getMinuteOfHour());

                    alarmManager.set(AlarmManager.RTC_WAKEUP, tempEventTime.getMillis(), alarmIntent);

                    boolean insertNotify = przypominajkaDatabaseHelper.insertNotification(eventsWithDefaultTime.toString(),
                            new LocalTime(tempEventTime.getHourOfDay(), tempEventTime.getMinuteOfHour()),
                            tempEventTime.toLocalDate(), false);
                    if (insertNotify) {
                        Log.d("OnStartJob", "Dodano powiadomienie dla " + eventsWithDefaultTime + " " + tempEventTime.toLocalDate().toString()
                                + " " + new LocalTime(tempEventTime.getHourOfDay(), tempEventTime.getMinuteOfHour()).toString());
                    } else {
                        Log.d("OnStartJob", "Nie udało się dodać informacji o powiadomieniu");
                    }
                    Log.d("OnStartJob", "Stworzono powiadomienie dla " + eventsWithDefaultTime + " o godzinie " + tempEventTime.toString());
                }
            } catch (Exception e) {
                Log.d("OnStartJob", "Problem z stworzeniem powiadomienia dla wydarzen domyślnych " + e.getMessage());
            }
        }
    }
}