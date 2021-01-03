package com.example.przypominajka.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.content.Intent;
import android.util.Log;
;

import com.example.przypominajka.databases.PrzypominajkaDatabaseHelper;
import com.example.przypominajka.databases.entities.EventModel;
import com.example.przypominajka.databases.entities.NotificationModel;
import com.example.przypominajka.utils.MyPrzypominajkaApp;
import com.example.przypominajka.broadcasts.ReminderBroadcast;
import com.example.przypominajka.databases.viewModels.EventsViewModel;
import com.example.przypominajka.databases.viewModels.NotificationViewModel;
import com.example.przypominajka.databases.viewModels.SettingsViewModel;

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

    JobParameters mParams;
    LocalDate nextDayDate;

    private final NotificationViewModel notificationViewModel = new NotificationViewModel(MyPrzypominajkaApp.get());
    private final SettingsViewModel settingsViewModel = new SettingsViewModel(MyPrzypominajkaApp.get());

    private static final String TAG = "SetAlarmService";

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
        List<EventModel> allEvent;
        List<EventModel> forNextDayDefaultTime;
        try {
            Log.d(TAG, "setNotify: Rozpoczęte");
            nextDayDate = LocalDate.now();
            nextDayDate = nextDayDate.plusDays(1);

            allEvent = PrzypominajkaDatabaseHelper.getEventForCurrentDay(nextDayDate);
            forNextDayDefaultTime = new ArrayList<>();
        } catch (Exception e) {
            Log.d(TAG, "setNotify: Problem z pobraniem wydarzen " + e.getMessage());
            return;
        }
        try {
            if (allEvent != null && allEvent.size() > 0) {
                try {
                    for (EventModel tempEvent : allEvent) {
                        // check if its not one time event
                        if (tempEvent.getEventTimeDefault()) {
                            if (!tempEvent.getItsOneTimeEvent()) {
                                forNextDayDefaultTime.add(tempEvent);
                            }
                        } else if (!tempEvent.getEventTimeDefault()) {
                            // set alarm for event which dont have default time for alarm
                            if (!tempEvent.getItsOneTimeEvent()) {
                                boolean isCreated = PrzypominajkaDatabaseHelper.checkIfNotificationIsCreated(tempEvent.getEventName(), nextDayDate);
                                if (!isCreated) {

                                    int year = nextDayDate.getYear();
                                    String yearShortString = Integer.toString(year).substring(2);
                                    int yearShort = Integer.parseInt(yearShortString);

                                    int notifyPendingIntentID = Integer.parseInt(String.valueOf(tempEvent.getId()) +
                                            String.valueOf(nextDayDate.getDayOfYear()) + String.valueOf(yearShort));

                                    boolean notificationCreated = createNotification(tempEvent.getEventName(),
                                            notifyPendingIntentID,
                                            tempEvent.getEventTime(),
                                            1,
                                            false);

                                    if (!notificationCreated) {
                                        Log.d(TAG, "setNotify: Problem z stworzeniem powiadomienia dla " + tempEvent.getEventName());
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, "setNotify: Problem z stworzeniem powiadomienia dla wydarzen z czasem innym niż domyślny " + e.getMessage());
                    return;
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "setNotify: Problem z stworzeniem powiadomienia " + e.getMessage());
        }

        try {
            // build alarm for all events for current day they have set default time
            StringBuilder eventsWithDefaultTime = new StringBuilder();
            if (forNextDayDefaultTime.size() > 0) {
                for (EventModel eventWithDefaultTime : forNextDayDefaultTime) {
                    if (eventsWithDefaultTime.toString().equals("")) {
                        if (!PrzypominajkaDatabaseHelper.checkIfNotificationIsCreated(eventWithDefaultTime.getEventName(), nextDayDate)) {
                            PrzypominajkaDatabaseHelper.updateNotificationCreatedColumn(eventWithDefaultTime.getEventName(), true, nextDayDate);
                            eventsWithDefaultTime = new StringBuilder(eventWithDefaultTime.getEventName());
                        } else {
                            eventsWithDefaultTime = new StringBuilder(eventWithDefaultTime.getEventName());
                        }
                    } else {
                        if (!PrzypominajkaDatabaseHelper.checkIfNotificationIsCreated(eventWithDefaultTime.getEventName(), nextDayDate)) {
                            PrzypominajkaDatabaseHelper.updateNotificationCreatedColumn(eventWithDefaultTime.getEventName(), true, nextDayDate);
                            eventsWithDefaultTime.append(", ").append(eventWithDefaultTime.getEventName());
                        } else {
                            eventsWithDefaultTime.append(", ").append(eventWithDefaultTime.getEventName());
                        }
                    }
                }
            }
            if (!eventsWithDefaultTime.toString().equals("")) {
                NotificationModel tempNotification = new NotificationModel(eventsWithDefaultTime.toString(), -nextDayDate.getDayOfYear(),
                        new LocalTime(settingsViewModel.getDefaultTime(), DateTimeZone.forID("UTC")), nextDayDate, false);
                List<NotificationModel> notificationModelsList = notificationViewModel.checkNotificationCreatedButNotNotifyList(tempNotification.notificationEventName,
                        tempNotification.getNotificationDateInMillis(),
                        tempNotification.getNotificationTimeInMillis());
                if (notificationModelsList.size() == 0) {

                    LocalTime eventTime = new LocalTime(settingsViewModel.getDefaultTime(), DateTimeZone.forID("UCT"));

                    boolean notificationCreated = createNotification(eventsWithDefaultTime.toString(),
                            -nextDayDate.getDayOfYear(),
                            eventTime,
                            forNextDayDefaultTime.size(),
                            true);
                    if (!notificationCreated) {
                        Log.d(TAG, "setNotify: Problem z stworzeniem powiadomienia dla " + eventsWithDefaultTime.toString());
                    }

                }
            }
        } catch (Exception e) {
            Log.d(TAG, "setNotify: Problem z stworzeniem powiadomienia dla wydarzen domyślnych " + e.getMessage());
        }
    }

    private boolean createNotification(String notifyText, int notifyPendingIntentID, LocalTime eventTime, int numberOfEvents, boolean itsDefaultTimeNotification) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent notificationIntent = new Intent(getApplicationContext(), ReminderBroadcast.class);
        notificationIntent.putExtra("NOTIFY_TEXT", notifyText);
        notificationIntent.putExtra("ID", notifyPendingIntentID);
        notificationIntent.putExtra("MANY", numberOfEvents);

        PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(),
                notifyPendingIntentID,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        DateTime tempEventTime = new DateTime(nextDayDate.getYear(), nextDayDate.getMonthOfYear(),
                nextDayDate.getDayOfMonth(), eventTime.getHourOfDay(), eventTime.getMinuteOfHour())
                .withZoneRetainFields(DateTimeZone.forID(TimeZone.getDefault().getID()));

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, tempEventTime.getMillis(), alarmIntent);

        if (!itsDefaultTimeNotification) {
            PrzypominajkaDatabaseHelper.updateNotificationCreatedColumn(notifyText, true, nextDayDate);
        }

        Log.d(TAG, "setNotify: Stworzono powiadomienie dla " + notifyText + " o godzinie " + tempEventTime.toString());
        NotificationModel newNotification = new NotificationModel(notifyText, notifyPendingIntentID,
                new LocalTime(tempEventTime.getHourOfDay(), tempEventTime.getMinuteOfHour()), tempEventTime.toLocalDate(), false);
        long result = notificationViewModel.insertNotification(newNotification);
        if (result != -1) {
            Log.d(TAG, "setNotify: Dodano powiadomienie dla " + notifyText + " " + tempEventTime.toLocalDate().toString()
                    + " " + new LocalTime(tempEventTime.getHourOfDay(), tempEventTime.getMinuteOfHour()).toString());
            return true;
        } else {
            Log.d(TAG, "setNotify: Nie udało się dodać informacji o powiadomieniu");
            return false;
        }
    }
}
