package com.example.przypominajka.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;;

import androidx.lifecycle.LiveData;

import com.example.przypominajka.activities.AddNewEventActivity;
import com.example.przypominajka.databases.PrzypominajkaDatabase;
import com.example.przypominajka.databases.PrzypominajkaDatabaseHelper;
import com.example.przypominajka.databases.entities.EventModel;
import com.example.przypominajka.databases.entities.NotificationModel;
import com.example.przypominajka.utils.MyPrzypominajkaApp;
import com.example.przypominajka.utils.ReminderBroadcast;
import com.example.przypominajka.viewModels.EventsViewModel;
import com.example.przypominajka.viewModels.NotificationViewModel;
import com.example.przypominajka.viewModels.SettingsViewModel;

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

    private EventsViewModel eventsViewModel = new EventsViewModel(MyPrzypominajkaApp.get());
    private NotificationViewModel notificationViewModel = new NotificationViewModel(MyPrzypominajkaApp.get());
    private SettingsViewModel settingsViewModel = new SettingsViewModel(MyPrzypominajkaApp.get());

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
            Log.d("OnStartJob", "Rozpoczęte");
            nextDayDate = LocalDate.now();
            nextDayDate = nextDayDate.plusDays(1);

            allEvent = PrzypominajkaDatabaseHelper.getEventForCurrentDay(nextDayDate);
            forNextDayDefaultTime = new ArrayList<>();
        } catch (Exception e) {
            Log.d("setNotify", "Problem z pobraniem wydarzen " + e.getMessage());
            return;
        }
        try {
            if (allEvent != null) {
                if (allEvent.size() > 0) {
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
                                    Log.d("OnStartJob boolean", String.valueOf(isCreated));
                                    if (!isCreated) {
                                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                                        int year = nextDayDate.getYear();
                                        String yearShortString = Integer.toString(year).substring(2);
                                        int yearShort = Integer.parseInt(yearShortString);

                                        int notifyPendingIntentID = Integer.parseInt(String.valueOf(tempEvent.getId()) +
                                                String.valueOf(nextDayDate.getDayOfYear()) + String.valueOf(yearShort));

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

                                        PrzypominajkaDatabaseHelper.updateNotificationCreatedColumn(tempEvent.getEventName(), true, nextDayDate);

                                        Log.d("OnStartJob", "Stworzono powiadomienie dla " + tempEvent.getEventName() + " o godzinie " + tempEventTime.toString());
                                        NotificationModel newNotification = new NotificationModel(tempEvent.getEventName(), notifyPendingIntentID,
                                                new LocalTime(tempEventTime.getHourOfDay(), tempEventTime.getMinuteOfHour()), tempEventTime.toLocalDate(), false);
                                        long result = notificationViewModel.insertNotification(newNotification);
                                        if (result != -1) {
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
                }
            }
        } catch (Exception e) {
            Log.d("OnStartJob", "Problem z stworzeniem powiadomienia " + e.getMessage());
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
                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                    Intent notificationIntent = new Intent(getApplicationContext(), ReminderBroadcast.class);
                    notificationIntent.putExtra("NOTIFY_TEXT", eventsWithDefaultTime.toString());
                    notificationIntent.putExtra("ID", -nextDayDate.getDayOfYear());
                    notificationIntent.putExtra("MANY", forNextDayDefaultTime.size());

                    PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(),
                            -nextDayDate.getDayOfYear(),
                            notificationIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    Log.d("OnStartJob", String.valueOf(nextDayDate.getDayOfYear()));

                    LocalTime eventTime = new LocalTime(settingsViewModel.getDefaultTime(), DateTimeZone.forID("UCT"));
                    Log.d("setAlarmService", eventTime.toString());

                    DateTime tempEventTime = new DateTime(nextDayDate.getYear(), nextDayDate.getMonthOfYear(),
                            nextDayDate.getDayOfMonth(), eventTime.getHourOfDay(), eventTime.getMinuteOfHour())
                            .withZoneRetainFields(DateTimeZone.forID(TimeZone.getDefault().getID()));

                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, tempEventTime.getMillis(), alarmIntent);
                    long result = notificationViewModel.insertNotification(tempNotification);
                    if (result != -1) {
                        Log.d("OnStartJob", "Dodano powiadomienie dla " + eventsWithDefaultTime + " " + tempEventTime.toLocalDate().toString()
                                + " " + new LocalTime(tempEventTime.getHourOfDay(), tempEventTime.getMinuteOfHour()).toString());
                    } else {
                        Log.d("OnStartJob", "Stworzono powiadomienie dla " + eventsWithDefaultTime + " o godzinie " + tempEventTime.toString());
                    }
                }
            }
        } catch (Exception e) {
            Log.d("OnStartJob", "Problem z stworzeniem powiadomienia dla wydarzen domyślnych " + e.getMessage());
        }
    }
}
