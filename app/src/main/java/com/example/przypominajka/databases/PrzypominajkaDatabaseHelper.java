package com.example.przypominajka.databases;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.util.Log;

import com.example.przypominajka.models.Event;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

public class PrzypominajkaDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "przypominajka";


    private static final String TABLE_NAME = "EVENTS"; // main table name

    private static final String EVENT_NAME = "EVENT_NAME"; // strings
    private static final String EVENT_DISCRIPTION = "EVENT_DISCRIPTION"; // strings


    private static final String EVENT_COLOR = "EVENT_COLOR";//int

    private static final String MONTH_INTERVAL = "MONTH_INTERVAL"; // boolean
    private static final String MONTH_NUMBER_OF_REPEATS = "MONTH_NUMBER_OF_REPEATS"; // int

    private static final String SHORT_TIME_INTERVAL = "SHORT_TIME_INTERVAL"; // boolean
    private static final String SHORT_TIME_TYPE = "SHORT_TIME_TYPE"; // type (0 - none, 1 - day,2 - week,3 - month)
    private static final String SHORT_TIME_REPEATS_ALL_TIME = "SHORT_TIME_REPEATS_ALL_TIME";// boolean
    private static final String SHORT_TIME_NUMBER_OF_REPEATS = "SHORT_TIME_NUMBER_OF_REPEATS"; // int

    private static final String ONE_TIME = "ONE_TIME"; // boolean
    private static final String ONE_TIME_DATE = "ONE_TIME_DATE"; // date

    private static final String TIME_INTERVAL = "TIME_INTERVAL"; // int

    private static final String START_DATE = "START_DATE"; // date

    private static final int DB_VERSION = 3;

    public PrzypominajkaDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + EVENT_NAME + " TEXT, "
                + EVENT_DISCRIPTION + " TEXT, "
                + EVENT_COLOR + " INTEGER, "
                + MONTH_INTERVAL + " INTEGER DEFAULT 0, "
                + MONTH_NUMBER_OF_REPEATS + " INTEGER DEFAULT 0, "
                + SHORT_TIME_INTERVAL + " INTEGER DEFAULT 0, "
                + SHORT_TIME_TYPE + " INTEGER DEFAULT 0, "
                + SHORT_TIME_REPEATS_ALL_TIME + " INTEGER DEFAULT 0, "
                + SHORT_TIME_NUMBER_OF_REPEATS + " INTEGER DEFAULT 0, "
                + ONE_TIME + " INTEGER DEFAULT 0, "
                + ONE_TIME_DATE + " REAL DEFAULT 0, "
                + TIME_INTERVAL + " INTEGER DEFAULT 0, "
                + START_DATE + " REAL DEFAULT 0);";
        db.execSQL(query);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertEvent(Event event) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(EVENT_NAME, event.getEventName());
            contentValues.put(EVENT_DISCRIPTION, event.getEventDiscription());
            contentValues.put(EVENT_COLOR, event.getEventColor());
            contentValues.put(MONTH_INTERVAL, event.getItsMonthInterval());
            contentValues.put(MONTH_NUMBER_OF_REPEATS, event.getMonthNumberOfRepeats());
            contentValues.put(SHORT_TIME_INTERVAL, event.getItsShortTimeInterval());
            contentValues.put(SHORT_TIME_TYPE, event.getShortTimeType());
            contentValues.put(SHORT_TIME_REPEATS_ALL_TIME, event.getItsShortTimeRepeatsAllTime());
            contentValues.put(SHORT_TIME_NUMBER_OF_REPEATS, event.getShortTimeNumberOfRepeats());
            contentValues.put(ONE_TIME, event.getItsOneTimeEvent());
            contentValues.put(ONE_TIME_DATE, event.getOneTimeEventDateInMillis());
            contentValues.put(TIME_INTERVAL, event.getTimeInterval());
            contentValues.put(START_DATE, event.getStartDateInMillis());
            Log.d("Insert to table", String.valueOf(event.getOneTimeEventDateInMillis()));
            long result = db.insert(TABLE_NAME, null, contentValues);
            if (result == -1) {
                return false;
            } else {
                try {
                    Log.d("insertEvent", "Tworzenie tabeli");
                    String query = "CREATE TABLE " + "\"" + event.getEventName() + "\"" + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + "DAY NUMERIC);";
                    db.execSQL(query);
                    if (event.getItsMonthInterval()) {
                        boolean resultFillTable = fillTableDayOfMonth(event.getEventName(), event.getTimeInterval(), event.getStartDate(), event.getMonthNumberOfRepeats());
                        if (resultFillTable) {
                            Log.d("Uzupełnianie tabeli", "Tworzenie udane");
                        } else {
                            return false;
                        }
                    } else if (event.getItsShortTimeInterval()) {
                        boolean resultJumpDay = fillTableJumpDay(event.getEventName(), event.getTimeInterval(), event.getStartDate(),
                                event.getShortTimeType(), event.getItsShortTimeRepeatsAllTime(), event.getShortTimeNumberOfRepeats());
                        if (resultJumpDay) {
                            Log.d("Uzupełnianie tabeli", "Tworzenie udane");
                        } else {
                            return false;
                        }
                    }

                } catch (Exception e) {
                    Log.w("SQLite", "Problem z utworzeniem tabeli " + e.getMessage());
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            Log.w("insertEvent", e.getMessage());
            return false;
        }
    }

    public boolean fillTableDayOfMonth(String tableName, int dayOfMonth, LocalDate startDate, int monthNumberOfRepeats) {
        SQLiteDatabase db = this.getWritableDatabase();
        LocalDate newDate = startDate;
        LocalDate tempDate = new LocalDate(startDate.getYear(), startDate.getMonthOfYear(), dayOfMonth);
        if (startDate.toDateTimeAtStartOfDay().getMillis() < tempDate.toDateTimeAtStartOfDay().getMillis()) {
            long tempDateAsMillis = tempDate.toDateTimeAtStartOfDay().getMillis();
            ContentValues contentValuesCurrentDate = new ContentValues();
            contentValuesCurrentDate.put("DAY", tempDateAsMillis);
            long resultCurrentDate = db.insert("\"" + tableName + "\"", null, contentValuesCurrentDate);
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
                long result = db.insert("\"" + tableName + "\"", null, contentValues);
                if (result == -1) {
                    return false;
                }
            } catch (Exception e) {
                Log.w("SQLite", "Problem z wypełnieniem tabeli " + e.getMessage());
                return false;
            }
        }
        return true;
    }

    public boolean fillTableJumpDay(String tableName, int timeInterval, LocalDate startDate,
                                    int shortTimeType, boolean shortTimeRepeatsAllTime,
                                    int shortTimeNumberOfRepeats) {
        SQLiteDatabase db = this.getWritableDatabase();

        LocalDate newDate = startDate;
        int howManyRepeats;
        if (shortTimeRepeatsAllTime) {
            howManyRepeats = 1000; // for now its 1000, its big number enough for the least two years, TODO in future
        } else {
            howManyRepeats = shortTimeNumberOfRepeats;
        }
        for (int i = 0; i < howManyRepeats; i++) {
            try {
                long newDateAsMillis = newDate.toDateTimeAtStartOfDay().getMillis();
                ContentValues contentValues = new ContentValues();
                contentValues.put("DAY", newDateAsMillis);
                long result = db.insert("\"" + tableName + "\"", null, contentValues);
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
                            Log.w("fillTableJumpDay", "Problem z wypełnieniem tabeli " + tableName);
                            break;
                        }
                    }

                }
            } catch (Exception e) {
                Log.w("SQLite", "Problem z wypełnieniem tabeli " + e.getMessage());
                return false;
            }

        }
        return true;
    }

    public boolean deleteEvent(String eventName) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("DROP TABLE " + "\"" + eventName + "\"");
            db.execSQL("DELETE FROM " + "\"" + TABLE_NAME + "\"" + " WHERE " + EVENT_NAME + " = " + "\"" + eventName + "\"");
            String queryEvent = "SELECT * FROM " + "\"" + TABLE_NAME + "\"" + " WHERE " + EVENT_NAME + " = " + "\"" + eventName + "\"";
            String queryTable = "select DISTINCT tbl_name from sqlite_master where tbl_name = " + "\"" + eventName + "\"";
            Cursor cursorEvent = db.rawQuery(queryEvent, null);
            boolean isInTable = cursorEvent.getCount() == 0;
            cursorEvent.close();
            Cursor cursorTable = db.rawQuery(queryTable, null);
            boolean isTable = cursorTable.getCount() == 0;
            cursorTable.close();
            return isInTable && isTable;
        } catch (Exception e) {
            Log.w("SQLite", "Problem z usunięciem elementuz bazy " + e.getMessage());
            return false;
        }
    }

    public Cursor getEvent(String eventName) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "SELECT * FROM " + "\"" + TABLE_NAME + "\"" + " WHERE " + EVENT_NAME + " = " + "\"" + eventName + "\"";
            return db.rawQuery(query, null);
        } catch (Exception e) {
            Log.w("SQLite", "Problem z zapytaniem do bazy danych " + e.getMessage());
            return null;
        }

    }


    public boolean checkTableForCurrentDate(LocalDate currentDate, String tableName) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor eventName = getEvent(tableName);
            eventName.moveToFirst();
            while (!eventName.isAfterLast()) {
                boolean tempOneTimeEvent = eventName.getInt(10) == 1;
                if (tempOneTimeEvent) {
                    long oneTimeEventDate = eventName.getLong(11);
                    eventName.close();
                    if (new LocalDate(oneTimeEventDate).equals(currentDate)) {
                        return true;
                    } else {
                        return false;
                    }
                }
                eventName.moveToNext();
            }
            eventName.close();
            long currentDateAsMillis = currentDate.toDateTimeAtStartOfDay().getMillis();
            String query = "SELECT DAY FROM " + "\"" + tableName + "\"" + " WHERE DAY = " + "'" + currentDateAsMillis + "'";
            @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, null);
            boolean isInTable = cursor.getCount() != 0;
            cursor.close();

            return isInTable;
        } catch (Exception e) {
            Log.w("SQLite", "Problem z zapytaniem do bazy danych " + e.getMessage());
            return false;
        }

    }


    public List<Event> getAllEvent() {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "SELECT * FROM " + "\"" + TABLE_NAME + "\"";
            @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, null);
            List<Event> eventsList = new ArrayList<>();
            if (cursor.getCount() == 0) {
                return eventsList;
            }
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                boolean tempMonthInterval = cursor.getInt(4) == 1;
                boolean tempShortTimeInterval = cursor.getInt(6) == 1;
                boolean tempShortTimeRepeatAllTime = cursor.getInt(8) == 1;
                ;
                boolean tempOneTimeEvent = cursor.getInt(10) == 1;
                long oneTimeEventDate = cursor.getLong(11);
                long tempStartDate = cursor.getLong(13);
                Event tempEvent = new Event(cursor.getString(1), cursor.getString(2)
                        , cursor.getInt(3), tempMonthInterval, cursor.getInt(5),
                        tempShortTimeInterval, cursor.getInt(7),
                        tempShortTimeRepeatAllTime, cursor.getInt(9),
                        tempOneTimeEvent, new LocalDate(oneTimeEventDate),
                        cursor.getInt(12), new LocalDate(tempStartDate));
                eventsList.add(tempEvent);
                cursor.moveToNext();

            }
            cursor.close();
            return eventsList;
        } catch (Exception e) {
            Log.w("SQLite", "Problem z zapytaniem do bazy danych " + e.getMessage());
            return null;
        }

    }

    public ArrayList<Event> getEventForCurrentDay(LocalDate currentDay) {

        try {
            ArrayList<Event> eventsForCurrentDay = new ArrayList<>();

            List<Event> allEvents = getAllEvent();

            if (allEvents.size() > 0) {
                eventsForCurrentDay.clear();
                for (int i = 0; i < allEvents.size(); i++) {

                    Event tempEvent = allEvents.get(i);
                    // first check if its one time event
                    // check if event is today
                    boolean isEventToday = checkTableForCurrentDate(currentDay,
                            tempEvent.getEventName());
                    if (isEventToday) {
                        // if yes add to array
                        eventsForCurrentDay.add(tempEvent);
                    }
                }

            }
            return eventsForCurrentDay;
        } catch (Exception e) {
            Log.w("SQLite", "Problem z zapytaniem do bazy danych " + e.getMessage());
            return null;
        }

    }
}
