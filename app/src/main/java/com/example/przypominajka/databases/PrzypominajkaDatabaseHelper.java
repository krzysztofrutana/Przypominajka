package com.example.przypominajka.databases;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.CaptivePortal;
import android.util.EventLog;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.room.OnConflictStrategy;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.przypominajka.databases.entities.EventModel;
import com.example.przypominajka.databases.entities.EventsTimeTemplateModel;
import com.example.przypominajka.databases.repositories.EventsRepository;
import com.example.przypominajka.utils.MyPrzypominajkaApp;
import com.example.przypominajka.viewModels.EventsViewModel;
import com.example.przypominajka.viewModels.SettingsViewModel;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

//only the methods left in this class that handle manually created tables containing the days of the event
public class PrzypominajkaDatabaseHelper {

    public static boolean insertEvent(EventModel eventModel) {
        SupportSQLiteDatabase sdb = PrzypominajkaDatabase.getDatabase(MyPrzypominajkaApp.get()).getOpenHelper().getWritableDatabase();
        try {
            if (!eventModel.getItsOneTimeEvent()) {
                Log.d("insertEvent", "Tworzenie tabeli");
                sdb.execSQL(EventsTimeTemplateModel.TEMPLATE_TABLE_NAME_CREATE_SQL.replace(EventsTimeTemplateModel.TEMPLATE_TABLE_NAME_PLACEHOLDER,
                        eventModel.getEventName().replace(" ", "_")));
                if (eventModel.getItsMonthInterval()) {
                    boolean resultFillTable = fillTableDayOfMonth(eventModel.getEventName(), eventModel.getTimeInterval(), eventModel.getStartDate(), eventModel.getMonthNumberOfRepeats());
                    if (resultFillTable) {
                        Log.d("SQLite insertEvent", "Tworzenie udane");
                        return true;
                    } else {
                        Log.d("SQLite insertEvent", "Tworzenie nieudane");
                        return false;
                    }
                } else if (eventModel.getItCustomTimeInterval()) {
                    boolean resultJumpDay = fillTableJumpDay(eventModel.getEventName(), eventModel.getTimeInterval(), eventModel.getStartDate(),
                            eventModel.getCustomTimeType(), eventModel.getItsCustomTimeRepeatsAllTime(), eventModel.getCustomTimeNumberOfRepeats(),
                            eventModel.getEventTimeDefault(), eventModel.getEventTime());
                    if (resultJumpDay) {
                        Log.d("SQLite insertEvent", "Tworzenie udane");
                        return true;
                    } else {
                        Log.d("SQLite insertEvent", "Tworzenie nieudane");
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            Log.w("SQLite insertEvent", "Problem z utworzeniem tabeli " + e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean fillTableDayOfMonth(String tableName, int dayOfMonth, LocalDate startDate, int monthNumberOfRepeats) {
        try {
            SupportSQLiteDatabase sdb = PrzypominajkaDatabase.getDatabase(MyPrzypominajkaApp.get()).getOpenHelper().getWritableDatabase();
            LocalDate newDate = startDate;
            LocalDate tempDate = new LocalDate(startDate.getYear(), startDate.getMonthOfYear(), dayOfMonth);
            if (startDate.toDateTimeAtStartOfDay().getMillis() < tempDate.toDateTimeAtStartOfDay().getMillis()) {
                long tempDateAsMillis = tempDate.toDateTimeAtStartOfDay().getMillis();
                ContentValues contentValuesCurrentDate = new ContentValues();
                contentValuesCurrentDate.put("DAY", tempDateAsMillis);
                long resultCurrentDate = sdb.insert("'" + tableName + "'", OnConflictStrategy.IGNORE, contentValuesCurrentDate);
                if (resultCurrentDate == -1) {
                    return false;
                }
            }
            for (int i = 0; i < monthNumberOfRepeats; i++) {
                int day;
                if (dayOfMonth == 31) {
                    LocalDate tempLocalDate = new LocalDate(newDate.getYear(), newDate.getMonthOfYear() + 1, 1);
                    day = tempLocalDate.dayOfMonth().getMaximumValue();
                    newDate = new LocalDate(newDate.getYear(), newDate.getMonthOfYear() + 1, day);
                } else if (newDate.getMonthOfYear() == 0) {
                    LocalDate tempLocalDate = new LocalDate(newDate.getYear(), newDate.getMonthOfYear() + 1, 1);
                    day = tempLocalDate.dayOfMonth().getMaximumValue();
                    newDate = new LocalDate(newDate.getYear(), newDate.getMonthOfYear() + 1, day);
                } else {
                    newDate = newDate.plusMonths(1);
                    newDate = new LocalDate(newDate.getYear(), newDate.getMonthOfYear(), dayOfMonth);
                }
                try {
                    long localDataToMillis = newDate.toDateTimeAtStartOfDay().getMillis();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("DAY", localDataToMillis);
                    long result = sdb.insert("'" + tableName + "'", OnConflictStrategy.IGNORE, contentValues);
                    if (result == -1) {
                        return false;
                    }
                } catch (Exception e) {
                    Log.w("SQLite fillTableDayOfMonth", "Problem z wypełnieniem tabeli " + e.getMessage());
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            Log.w(" SQLite fillTableDayOfMonth", e.getMessage());
            return false;
        }

    }

    public static boolean fillTableJumpDay(String tableName, int timeInterval, LocalDate startDate,
                                           int shortTimeType, boolean shortTimeRepeatsAllTime,
                                           int shortTimeNumberOfRepeats, boolean itsEventTimeDefault, LocalTime eventTime) {
        try {
            SupportSQLiteDatabase sdb = PrzypominajkaDatabase.getDatabase(MyPrzypominajkaApp.get()).getOpenHelper().getWritableDatabase();
            LocalDate newDate;
            if (itsEventTimeDefault) {
                SettingsViewModel settingsViewModel = new SettingsViewModel(MyPrzypominajkaApp.get());
                long defaultTime = settingsViewModel.getDefaultTime();
                long currentTime = LocalTime.now(DateTimeZone.forID(TimeZone.getDefault().getID())).getMillisOfDay();
                if (defaultTime > currentTime) {
                    newDate = startDate;
                } else {
                    newDate = startDate.plusDays(1);
                }
            } else {
                long eventTimeInMillis = eventTime.getMillisOfDay();
                long currentTime = LocalTime.now(DateTimeZone.forID(TimeZone.getDefault().getID())).getMillisOfDay();
                if (eventTimeInMillis > currentTime) {
                    newDate = startDate;
                } else {
                    newDate = startDate.plusDays(1);
                }
            }
            int howManyRepeats;
            if (shortTimeRepeatsAllTime) {
                howManyRepeats = 500; // for now its 500, its big number enough for the one and half year, TODO in future, maybe new service for this in background?
            } else {
                howManyRepeats = shortTimeNumberOfRepeats;
            }
            for (int i = 0; i < howManyRepeats; i++) {
                try {
                    long newDateAsMillis = newDate.toDateTimeAtStartOfDay().getMillis();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("DAY", newDateAsMillis);
                    long result = sdb.insert("'" + tableName + "'", OnConflictStrategy.IGNORE, contentValues);
                    if (result == -1) {
                        return false;
                    } else {
                        {
                            if (shortTimeType == 1) {
                                newDate = newDate.plusDays(timeInterval);
                            } else if (shortTimeType == 2) {
                                newDate = newDate.plusWeeks(timeInterval);
                            } else if (shortTimeType == 3) {
                                newDate = newDate.plusMonths(timeInterval);
                            } else {
                                Log.w("SQLite fillTableJumpDay", "Problem z wypełnieniem tabeli " + tableName);
                                break;
                            }
                        }

                    }
                } catch (Exception e) {
                    Log.w("SQLite fillTableJumpDay", "Problem z wypełnieniem tabeli " + e.getMessage());
                    return false;
                }

            }
            return true;
        } catch (Exception e) {
            Log.w("SQLite fillTableJumpDay", e.getMessage());
            return false;
        }

    }


    public static boolean checkTableForCurrentDate(LocalDate currentDate, EventModel eventModel) {
        try {
            SupportSQLiteDatabase sdb = PrzypominajkaDatabase.getDatabase(MyPrzypominajkaApp.get()).getOpenHelper().getWritableDatabase();
            String tableName = eventModel.getEventName().replace(" ", "_");
            if (eventModel.getItsOneTimeEvent()) {
                long oneTimeEventDate = eventModel.getOneTimeEventDateInMillis();
                return new LocalDate(oneTimeEventDate).equals(currentDate);
            }
            long currentDateAsMillis = currentDate.toDateTimeAtStartOfDay().getMillis();
            String query = "SELECT DAY FROM " + "\"" + tableName + "\"" + " WHERE DAY = " + "\"" + currentDateAsMillis + "\"";
            @SuppressLint("Recycle") Cursor cursor = sdb.query(query, null);
            boolean isInTable = cursor.getCount() != 0;
            cursor.close();
            return isInTable;
        } catch (Exception e) {
            Log.w("SQLite checkTableForCurrentDate", "Problem z zapytaniem do bazy danych " + e.getMessage());
            return false;
        }

    }

    @SuppressLint("LongLogTag")
    public static ArrayList<EventModel> getEventForCurrentDay(LocalDate currentDay) {
        try {
            ArrayList<EventModel> eventsForCurrentDay = new ArrayList<>();

            List<EventModel> allEvents = new ArrayList<>();
            EventsViewModel eventsViewModel = new EventsViewModel(MyPrzypominajkaApp.get());
            allEvents = eventsViewModel.getAllEventsList();

            if (allEvents == null) {
                Log.w("SQLite setCurrentMonth", "Wystąpił problem z pobraniem wydarzeń z  bazy danych");
                return null;
            }

            if (allEvents.size() > 0) {
                eventsForCurrentDay.clear();
                for (int i = 0; i < allEvents.size(); i++) {

                    EventModel tempEvent = allEvents.get(i);
                    // first check if its one time event
                    // check if event is today
                    boolean isEventToday = PrzypominajkaDatabaseHelper.checkTableForCurrentDate(currentDay, tempEvent);
                    if (isEventToday) {
                        // if yes add to array
                        eventsForCurrentDay.add(tempEvent);
                    }
                }
            }
            return eventsForCurrentDay;
        } catch (Exception e) {
            Log.w("SQLite getEventForCurrentDay", "Problem z zapytaniem do bazy danych " + e.getMessage());
            return null;
        }
    }

    //setting a value that informs whether the notification has been created
    @SuppressLint("LongLogTag")
    public static void updateNotificationCreatedColumn(String eventName, boolean isCreated, LocalDate date) {
        try {
            SupportSQLiteDatabase sdb = PrzypominajkaDatabase.getDatabase(MyPrzypominajkaApp.get()).getOpenHelper().getWritableDatabase();
            eventName = eventName.replace(" ", "_");
            long dateAsMillis = date.toDateTimeAtStartOfDay().getMillis();
            String query;
            if (isCreated) {
                query = "UPDATE " + "\"" + eventName + "\"" + " SET NOTIFICATION_CREATED  = " + "\"" + 1
                        + "\"" + " WHERE DAY = " + "\"" + dateAsMillis + "\"";
            } else {
                query = "UPDATE " + "\"" + eventName + "\"" + " SET NOTIFICATION_CREATED  = " + "\"" + 0
                        + "\"" + " WHERE DAY = " + "\"" + dateAsMillis + "\"";
            }
            sdb.execSQL(query);
        } catch (Exception e) {
            Log.w("SQLite updateNotificationCreatedColumn", "Problem z zapytaniem do bazy danych " + e.getMessage());
        }
    }


    // check event table for current date and check if the notification has been created
    @SuppressLint("LongLogTag")
    public static boolean checkIfNotificationIsCreated(String eventName, LocalDate date) {
        try {
            SupportSQLiteDatabase sdb = PrzypominajkaDatabase.getDatabase(MyPrzypominajkaApp.get()).getOpenHelper().getWritableDatabase();
            long dateInMillis = date.toDateTimeAtStartOfDay().getMillis();
            String query = "SELECT NOTIFICATION_CREATED FROM " + "\"" + eventName + "\"" +
                    " WHERE DAY  = " + "\"" + dateInMillis + "\"";
            @SuppressLint("Recycle") Cursor cursor = sdb.query(query, null);
            cursor.moveToFirst();
            boolean isCreated = cursor.getInt(cursor.getColumnIndex("NOTIFICATION_CREATED")) == 1;
            cursor.close();
            return isCreated;
        } catch (Exception e) {
            Log.w("SQLite checkIfNotificationIsCreated", "Problem z zapytaniem do bazy danych " + e.getMessage());
            return false;
        }
    }
}
