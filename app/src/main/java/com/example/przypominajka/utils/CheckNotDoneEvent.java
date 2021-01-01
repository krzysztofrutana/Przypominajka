package com.example.przypominajka.utils;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;

import com.example.przypominajka.R;
import com.example.przypominajka.broadcasts.AlarmDialogBroadcast;
import com.example.przypominajka.databases.PrzypominajkaDatabaseHelper;
import com.example.przypominajka.databases.entities.EventModel;
import com.example.przypominajka.databases.repositories.EventsRepository;
import com.example.przypominajka.models.NotDoneEventModel;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

//TODO fix remind me later options

public class CheckNotDoneEvent {

    private final EventsRepository eventsRepository = new EventsRepository(MyPrzypominajkaApp.get());

    public static final ExecutorService ckeckNotDoneEventExecutor =
            Executors.newFixedThreadPool(1);

    private final String alertDialogTitle = MyPrzypominajkaApp.get().getString(R.string.ADtitle);
    private final String alertDialogMessage = MyPrzypominajkaApp.get().getString(R.string.ADmessage);

    public void checkAllNotDoneEventInPast(Context context) {
        List<EventModel> allEvents = eventsRepository.getAllEventsList();
        List<NotDoneEventModel> eventsWithNotDoneDays = new ArrayList<>();
        for (EventModel event : allEvents) {
            if (event.itsMonthInterval || event.itsCustomTimeInterval) {
                NotDoneEventModel notDoneEventModel = checkOneNotDoneEventInPasts(event);
                if (notDoneEventModel != null) {
                    eventsWithNotDoneDays.add(notDoneEventModel);
                }
            }
        }
        if (eventsWithNotDoneDays.size() == 1) {
            showAlertDialogIfOnlyOneEvent(context, eventsWithNotDoneDays.get(0));
        } else if (eventsWithNotDoneDays.size() > 1) {
            StringBuilder textOfDaysForAllEvents = new StringBuilder();
            for (NotDoneEventModel notDoneEventModel : eventsWithNotDoneDays) {
                textOfDaysForAllEvents.append(notDoneEventModel.getEventModel().getEventName().replace('_', ' ')).append("\n");
                for (String tempDayText : notDoneEventModel.getTextForDays()) {
                    textOfDaysForAllEvents.append(tempDayText).append("\n");
                }
                textOfDaysForAllEvents.append("\n");
            }
            showAlertDialogForMoreEvents(context, textOfDaysForAllEvents.toString(), eventsWithNotDoneDays);
        }
    }

    private NotDoneEventModel checkOneNotDoneEventInPasts(EventModel event) {
        List<LocalDate> listOfNotDoneDay = new ArrayList<>();
        try {
            listOfNotDoneDay = ckeckNotDoneEventExecutor.submit(() -> PrzypominajkaDatabaseHelper.checkNotDoneEventFromCurrentDay(event.getEventName(), LocalDate.now())).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        List<String> findedDays = new ArrayList<>();
        if (listOfNotDoneDay != null && listOfNotDoneDay.size() > 0) {
            for (LocalDate date : listOfNotDoneDay) {
                findedDays.add("Dnia " + date.toString());
            }
            return new NotDoneEventModel(listOfNotDoneDay, findedDays, event);
        } else {
            return null;
        }
    }

    private void showAlertDialogIfOnlyOneEvent(Context context, NotDoneEventModel notDoneEventModel) {
        StringBuilder textForDays = new StringBuilder();
        for (String tempString : notDoneEventModel.getTextForDays()) {
            textForDays.append(tempString).append("\n");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(alertDialogTitle);
        builder.setMessage(alertDialogMessage + "\n\n" + textForDays);
        // add the buttons
        builder.setPositiveButton("Oznacz jako wykonane", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (LocalDate date : notDoneEventModel.getListOdNotDoneDays()) {
                    PrzypominajkaDatabaseHelper.updateEventMadeColumn(notDoneEventModel.getEventModel().getEventName(), true, date);
                }
            }
        });
        builder.setNeutralButton("Przypomnij mi później", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlarmManager alarmManager = (AlarmManager) MyPrzypominajkaApp.get().getApplicationContext().getSystemService(Context.ALARM_SERVICE);

                DateTime alarmTime = new DateTime(DateTime.now().getYear(), DateTime.now().getMonthOfYear(),
                        DateTime.now().getDayOfMonth(), DateTime.now().getHourOfDay(), DateTime.now().getMinuteOfHour())
                        .withZoneRetainFields(DateTimeZone.forID(TimeZone.getDefault().getID()));

                alarmTime = alarmTime.plusHours(2);

                int id = ThreadLocalRandom.current().nextInt(10000, 11000 + 1);
                Intent intent = new Intent(MyPrzypominajkaApp.get().getApplicationContext(), AlarmDialogBroadcast.class);
                intent.putExtra("ID", id);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(MyPrzypominajkaApp.get().getApplicationContext(),
                        id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                alarmManager.setExact(
                        AlarmManager.RTC, alarmTime.getMillis(), pendingIntent);
            }
        });
        builder.setNegativeButton("Ignoruj", (dialog, which) -> dialog.dismiss());
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showAlertDialogForMoreEvents(Context context, String textOfDaysForAllEvents, List<NotDoneEventModel> listOfNotDoneEvents) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(alertDialogTitle);
        builder.setMessage(alertDialogMessage + "\n\n" + textOfDaysForAllEvents);
        // add the buttons
        builder.setPositiveButton("Oznacz jako wykonane", (dialog, which) -> {
            for (NotDoneEventModel notDoneEventModel : listOfNotDoneEvents) {
                for (LocalDate date : notDoneEventModel.getListOdNotDoneDays()) {
                    PrzypominajkaDatabaseHelper.updateEventMadeColumn(notDoneEventModel.getEventModel().getEventName(), true, date);
                }
            }
        });
        builder.setNeutralButton("Przypomnij mi później", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlarmManager alarmManager = (AlarmManager) MyPrzypominajkaApp.get().getApplicationContext().getSystemService(Context.ALARM_SERVICE);

                DateTime alarmTime = new DateTime(DateTime.now().getYear(), DateTime.now().getMonthOfYear(),
                        DateTime.now().getDayOfMonth(), DateTime.now().getHourOfDay(), DateTime.now().getMinuteOfHour())
                        .withZoneRetainFields(DateTimeZone.forID(TimeZone.getDefault().getID()));

                alarmTime = alarmTime.plusHours(2);

                int id = ThreadLocalRandom.current().nextInt(10000, 11000 + 1);
                Intent intent = new Intent(MyPrzypominajkaApp.get().getApplicationContext(), AlarmDialogBroadcast.class);
                intent.putExtra("ID", id);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(MyPrzypominajkaApp.get().getApplicationContext(),
                        id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                alarmManager.setExact(
                        AlarmManager.RTC, alarmTime.getMillis(), pendingIntent);
            }
        });
        builder.setNegativeButton("Ignoruj", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
