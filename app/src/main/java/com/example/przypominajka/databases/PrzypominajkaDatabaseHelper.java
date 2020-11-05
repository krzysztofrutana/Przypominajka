package com.example.przypominajka.databases;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.util.Log;

import com.example.przypominajka.models.Event;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;

public class PrzypominajkaDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "przypominajka";


    private static final String TABLE_EVENTS = "EVENTS"; // main table name

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
    private static final String EVENT_TIME_DEFAULT = "EVENT_TIME_DEFAULT";
    private static final String EVENT_TIME = "EVENT_TIME";

    private static final String TABLE_SETTINGS = "SETTINGS";
    private static final String DEFAULT_TIME = "DEFAULT_TIME";
    private static final String NOTIFY_CHECK_INTERVAL = "NOTIFY_CHECK_INTERVAL";

    private static final String TABLE_NOTIFICATIONS = "NOTIFICATIONS";
    private static final String NOTIFICATION_EVENT_NAME = "NOTIFICATION_EVENT_NAME";
    private static final String NOTIFICATION_DATE = "NOTIFICATION_DATE";
    private static final String NOTIFICATION_TIME = "NOTIFICATION_TIME";
    private static final String NOTIFICATION_COMPLETED = "NOTIFICATION_COMPLETED";


    private static final int DB_VERSION = 6;

    Context context;

    public PrzypominajkaDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            String queryEventTable = "CREATE TABLE " + TABLE_EVENTS + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
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
                    + EVENT_TIME_DEFAULT + " INTEGER DEFAULT 0, "
                    + EVENT_TIME + " REAL DEFAULT 0, "
                    + START_DATE + " REAL DEFAULT 0);";
            db.execSQL(queryEventTable);

            String queryCreateSettingTable = "CREATE TABLE " + TABLE_SETTINGS + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + DEFAULT_TIME + " REAL DEFAULT 0, "
                    + NOTIFY_CHECK_INTERVAL + " REAL DEFAULT 0);";
            db.execSQL(queryCreateSettingTable);
            LocalTime defaultTime = new LocalTime(28800000, DateTimeZone.forID("Etc/Universal"));
            LocalDate localDate = LocalDate.now();
            DateTime dateTimeDefault = localDate.toDateTime(defaultTime, DateTimeZone.forID("Etc/Universal"));
            long defaultTimeInMillis = dateTimeDefault.getMillisOfDay();

            LocalTime intervalTime = new LocalTime(900000, DateTimeZone.forID("Etc/Universal"));
            DateTime dateTimeInterval = localDate.toDateTime(intervalTime, DateTimeZone.forID("Etc/Universal"));
            long intervalTimeInMillis = dateTimeInterval.getMillisOfDay();

            ContentValues contentValues = new ContentValues();
            contentValues.put(DEFAULT_TIME, defaultTimeInMillis);
            contentValues.put(NOTIFY_CHECK_INTERVAL, intervalTimeInMillis);

            long result = db.insert(TABLE_SETTINGS, null, contentValues);
            if (result == -1) {
                Log.d("SQLite onCreate", "Problem z utworzeniem tabeli ustawień");
            } else {
                Log.d("SQLite onCreate", "Tabela ustawień utworzona");
            }

            String queryNotificationTable = "CREATE TABLE " + TABLE_NOTIFICATIONS + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + NOTIFICATION_EVENT_NAME + " TEXT, "
                    + NOTIFICATION_DATE + " REAL DEFAULT 0, "
                    + NOTIFICATION_TIME + " REAL DEFAULT 0, "
                    + NOTIFICATION_COMPLETED + " INTEGER DEFAULT 0);";
            db.execSQL(queryNotificationTable);
        } catch (Exception e) {
            Log.d("SQLite onCreate", "Problem z tworzeniem tabel " + e.getMessage());
        }


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 6) {
            onCreate(db);
        }
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
            contentValues.put(SHORT_TIME_INTERVAL, event.getItCustomTimeInterval());
            contentValues.put(SHORT_TIME_TYPE, event.getCustomTimeType());
            contentValues.put(SHORT_TIME_REPEATS_ALL_TIME, event.getItsCustomTimeRepeatsAllTime());
            contentValues.put(SHORT_TIME_NUMBER_OF_REPEATS, event.getCustomTimeNumberOfRepeats());
            contentValues.put(ONE_TIME, event.getItsOneTimeEvent());
            contentValues.put(ONE_TIME_DATE, event.getOneTimeEventDateInMillis());
            contentValues.put(TIME_INTERVAL, event.getTimeInterval());
            contentValues.put(EVENT_TIME_DEFAULT, event.getEventTimeDefault());
            contentValues.put(EVENT_TIME, event.getEventTimeInMillis());
            contentValues.put(START_DATE, event.getStartDateInMillis());
            long result = db.insert(TABLE_EVENTS, null, contentValues);
            if (result == -1) {
                return false;
            } else {
                try {
                    if (!event.getItsOneTimeEvent()) {
                        Log.d("insertEvent", "Tworzenie tabeli");
                        String query = "CREATE TABLE " + "\"" + event.getEventName() + "\"" + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                                + "DAY REAL, " +
                                "NOTIFICATION_CREATED INTEGER DEFAULT 0);";
                        db.execSQL(query);
                        if (event.getItsMonthInterval()) {
                            boolean resultFillTable = fillTableDayOfMonth(event.getEventName(), event.getTimeInterval(), event.getStartDate(), event.getMonthNumberOfRepeats());
                            if (resultFillTable) {
                                Log.d("SQLite insertEvent", "Tworzenie udane");
                            } else {
                                Log.d("SQLite insertEvent", "Tworzenie nieudane");
                                return false;
                            }
                        } else if (event.getItCustomTimeInterval()) {
                            boolean resultJumpDay = fillTableJumpDay(event.getEventName(), event.getTimeInterval(), event.getStartDate(),
                                    event.getCustomTimeType(), event.getItsCustomTimeRepeatsAllTime(), event.getCustomTimeNumberOfRepeats(),
                                    event.getEventTimeDefault(), event.getEventTime());
                            if (resultJumpDay) {
                                Log.d("SQLite insertEvent", "Tworzenie udane");
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
            }
            return true;
        } catch (Exception e) {
            Log.w("SQLite insertEvent", e.getMessage());
            return false;
        }
    }

    public boolean fillTableDayOfMonth(String tableName, int dayOfMonth, LocalDate startDate, int monthNumberOfRepeats) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            LocalDate newDate = startDate;
            LocalDate tempDate = new LocalDate(startDate.getYear(), startDate.getMonthOfYear(), dayOfMonth);
            if (startDate.toDateTimeAtStartOfDay().getMillis() < tempDate.toDateTimeAtStartOfDay().getMillis()) {
                long tempDateAsMillis = tempDate.toDateTimeAtStartOfDay().getMillis();
                ContentValues contentValuesCurrentDate = new ContentValues();
                contentValuesCurrentDate.put("DAY", tempDateAsMillis);
                long resultCurrentDate = db.insert("'" + tableName + "'", null, contentValuesCurrentDate);
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
                    long result = db.insert("'" + tableName + "'", null, contentValues);
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

    public boolean fillTableJumpDay(String tableName, int timeInterval, LocalDate startDate,
                                    int shortTimeType, boolean shortTimeRepeatsAllTime,
                                    int shortTimeNumberOfRepeats, boolean itsEventTimeDefault, LocalTime eventTime) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            LocalDate newDate;
            if (itsEventTimeDefault) {
                long defaultTime = getDefaultTime();
                long currentTime = LocalTime.now().getMillisOfDay();
                if (defaultTime > currentTime) {
                    newDate = startDate;
                } else {
                    newDate = startDate.plusDays(1);
                }
            } else {
                long eventTimeInMillis = eventTime.getMillisOfDay();
                long currentTime = LocalTime.now().getMillisOfDay();
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
                    long result = db.insert("'" + tableName + "'", null, contentValues);
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

    public boolean deleteEvent(String eventName) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("DROP TABLE " + "\"" + eventName + "\"");
            db.execSQL("DELETE FROM " + "\"" + TABLE_EVENTS + "\"" + " WHERE " + EVENT_NAME + " = " + "\"" + eventName + "\"");
            String queryEvent = "SELECT * FROM " + "\"" + TABLE_EVENTS + "\"" + " WHERE " + EVENT_NAME + " = " + "\"" + eventName + "\"";
            String queryTable = "select DISTINCT tbl_name from sqlite_master where tbl_name = " + "\"" + eventName + "\"";
            Cursor cursorEvent = db.rawQuery(queryEvent, null);
            boolean isInTable = cursorEvent.getCount() == 0;
            cursorEvent.close();
            Cursor cursorTable = db.rawQuery(queryTable, null);
            boolean isTable = cursorTable.getCount() == 0;
            cursorTable.close();
            return isInTable && isTable;
        } catch (Exception e) {
            Log.w("SQLite deleteEvent", "Problem z usunięciem elementuz bazy " + e.getMessage());
            return false;
        }
    }

    public Event getEvent(String eventName) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "SELECT * FROM " + "\"" + TABLE_EVENTS + "\"" + " WHERE " + EVENT_NAME + " = " + "\"" + eventName + "\"";
            @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, null);
            cursor.moveToFirst();
            boolean tempMonthInterval = cursor.getInt(cursor.getColumnIndex(MONTH_INTERVAL)) == 1;
            boolean tempShortTimeInterval = cursor.getInt(cursor.getColumnIndex(SHORT_TIME_INTERVAL)) == 1;
            boolean tempShortTimeRepeatAllTime = cursor.getInt(cursor.getColumnIndex(SHORT_TIME_REPEATS_ALL_TIME)) == 1;
            boolean tempOneTimeEvent = cursor.getInt(cursor.getColumnIndex(ONE_TIME)) == 1;
            long oneTimeEventDate = cursor.getLong(cursor.getColumnIndex(ONE_TIME_DATE));
            boolean tempItsDefaultTime = cursor.getInt(cursor.getColumnIndex(EVENT_TIME_DEFAULT)) == 1;
            long tempEventTime = cursor.getLong(cursor.getColumnIndex(EVENT_TIME));
            long tempStartDate = cursor.getLong(cursor.getColumnIndex(START_DATE));
            Event tempEvent = new Event(cursor.getString(cursor.getColumnIndex(EVENT_NAME)), cursor.getString(cursor.getColumnIndex(EVENT_DISCRIPTION))
                    , cursor.getInt(cursor.getColumnIndex(EVENT_COLOR)), tempMonthInterval, cursor.getInt(cursor.getColumnIndex(MONTH_NUMBER_OF_REPEATS)),
                    tempShortTimeInterval, cursor.getInt(cursor.getColumnIndex(SHORT_TIME_TYPE)),
                    tempShortTimeRepeatAllTime, cursor.getInt(cursor.getColumnIndex(SHORT_TIME_NUMBER_OF_REPEATS)),
                    tempOneTimeEvent, new LocalDate(oneTimeEventDate), tempItsDefaultTime,
                    tempEventTime,
                    cursor.getInt(cursor.getColumnIndex(TIME_INTERVAL)), new LocalDate(tempStartDate));
            cursor.close();
            return tempEvent;
        } catch (Exception e) {
            Log.w("SQLite getEvent", "Problem z zapytaniem do bazy danych " + e.getMessage());
            return null;
        }

    }


    public boolean checkTableForCurrentDate(LocalDate currentDate, String tableName) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Event event = getEvent(tableName);
            if (event.getItsOneTimeEvent()) {
                long oneTimeEventDate = event.getOneTimeEventDateInMillis();
                return new LocalDate(oneTimeEventDate).equals(currentDate);
            }
            long currentDateAsMillis = currentDate.toDateTimeAtStartOfDay().getMillis();
            String query = "SELECT DAY FROM " + "\"" + tableName + "\"" + " WHERE DAY = " + "\"" + currentDateAsMillis + "\"";
            @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, null);
            boolean isInTable = cursor.getCount() != 0;
            cursor.close();
            return isInTable;
        } catch (Exception e) {
            Log.w("SQLite checkTableForCurrentDate", "Problem z zapytaniem do bazy danych " + e.getMessage());
            return false;
        }

    }


    public List<Event> getAllEvent() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String query = "SELECT * FROM " + "\"" + TABLE_EVENTS + "\"";
            @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, null);
            List<Event> eventsList = new ArrayList<>();
            if (cursor.getCount() == 0) {
                return eventsList;
            }
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                boolean tempMonthInterval = cursor.getInt(cursor.getColumnIndex(MONTH_INTERVAL)) == 1;
                boolean tempShortTimeInterval = cursor.getInt(cursor.getColumnIndex(SHORT_TIME_INTERVAL)) == 1;
                boolean tempShortTimeRepeatAllTime = cursor.getInt(cursor.getColumnIndex(SHORT_TIME_REPEATS_ALL_TIME)) == 1;
                boolean tempOneTimeEvent = cursor.getInt(cursor.getColumnIndex(ONE_TIME)) == 1;
                long oneTimeEventDate = cursor.getLong(cursor.getColumnIndex(ONE_TIME_DATE));
                boolean tempItsDefaultTime = cursor.getInt(cursor.getColumnIndex(EVENT_TIME_DEFAULT)) == 1;
                long tempEventTime = cursor.getLong(cursor.getColumnIndex(EVENT_TIME));
                long tempStartDate = cursor.getLong(cursor.getColumnIndex(START_DATE));
                Event tempEvent = new Event(cursor.getString(cursor.getColumnIndex(EVENT_NAME)), cursor.getString(cursor.getColumnIndex(EVENT_DISCRIPTION))
                        , cursor.getInt(cursor.getColumnIndex(EVENT_COLOR)), tempMonthInterval, cursor.getInt(cursor.getColumnIndex(MONTH_NUMBER_OF_REPEATS)),
                        tempShortTimeInterval, cursor.getInt(cursor.getColumnIndex(SHORT_TIME_TYPE)),
                        tempShortTimeRepeatAllTime, cursor.getInt(cursor.getColumnIndex(SHORT_TIME_NUMBER_OF_REPEATS)),
                        tempOneTimeEvent, new LocalDate(oneTimeEventDate), tempItsDefaultTime,
                        tempEventTime,
                        cursor.getInt(cursor.getColumnIndex(TIME_INTERVAL)), new LocalDate(tempStartDate));
                eventsList.add(tempEvent);

                cursor.moveToNext();

            }
            cursor.close();
            return eventsList;
        } catch (Exception e) {
            Log.w("SQLite getAllEvent", "Problem z zapytaniem do bazy danych " + e.getMessage());
            return null;
        }
    }

    @SuppressLint("LongLogTag")
    public ArrayList<Event> getEventForCurrentDay(LocalDate currentDay) {
        try {
            ArrayList<Event> eventsForCurrentDay = new ArrayList<>();

            List<Event> allEvents = getAllEvent();
            if (allEvents == null) {
                Log.w("SQLite setCurrentMonth", "Wystąpił problem z pobraniem wydarzeń z  bazy danych");
                return null;
            }

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
            Log.w("SQLite getEventForCurrentDay", "Problem z zapytaniem do bazy danych " + e.getMessage());
            return null;
        }
    }

    // update default time value
    @SuppressLint("LongLogTag")
    public void updateDefaultTimeInSettings(long defaultTime) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String query = "UPDATE " + "\"" + TABLE_SETTINGS + "\"" + " SET " + "\"" + DEFAULT_TIME + "\"" + " = " + "\"" + defaultTime + "\"";
            db.execSQL(query);
        } catch (Exception e) {
            Log.w("SQLite updateDefaultTimeInSettings", "Problem z zapytaniem do bazy danych " + e.getMessage());
        }
    }

    // get value of default time (can be change in settings), for now it is 8:00 AM
    public long getDefaultTime() {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "SELECT " + "\"" + DEFAULT_TIME + "\"" + " FROM " + "\"" + TABLE_SETTINGS + "\"";
            long defaultTime = 28800000;
            @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, null);
            if (cursor.getCount() == 0) {
                updateDefaultTimeInSettings(defaultTime);
            } else if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                defaultTime = cursor.getLong(cursor.getColumnIndex(DEFAULT_TIME));
                cursor.close();
            }
            return defaultTime;
        } catch (Exception e) {
            Log.w("SQLite getDefaultTime", "Problem z zapytaniem do bazy danych " + e.getMessage());
            return -1;
        }
    }

    // get value of job service start time interval (can be change in settings), for now it is 0:15
    @SuppressLint("LongLogTag")
    public long getCheckEventInterval() {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "SELECT " + "\"" + NOTIFY_CHECK_INTERVAL + "\"" + " FROM " + "\"" + TABLE_SETTINGS + "\"";
            long intervalTime = 900000;
            @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, null);
            if (cursor.getCount() == 0) {
                updateCheckEventInterval(intervalTime);
            } else if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                intervalTime = cursor.getLong(cursor.getColumnIndex(NOTIFY_CHECK_INTERVAL));
                cursor.close();
            }
            return intervalTime;
        } catch (Exception e) {
            Log.w("SQLite getCheckEventInterval", "Problem z zapytaniem do bazy danych " + e.getMessage());
            return -1;
        }
    }

    // update of job service start time interval
    public void updateCheckEventInterval(long intervalTime) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String query = "UPDATE " + "\"" + TABLE_SETTINGS + "\"" + " SET " + "\"" + NOTIFY_CHECK_INTERVAL + "\"" + " = "
                    + "\"" + intervalTime + "\"";
            db.execSQL(query);
        } catch (Exception e) {
            Log.w("SQLite updateCheckEventInterval", "Problem z zapytaniem do bazy danych " + e.getMessage());
        }
    }

    //setting a value that informs whether the notification has been created
    @SuppressLint("LongLogTag")
    public void updateNotificationCreatedColumn(String eventName, boolean isCreated, LocalDate date) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
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
            db.execSQL(query);
        } catch (Exception e) {
            Log.w("SQLite updateNotificationCreatedColumn", "Problem z zapytaniem do bazy danych " + e.getMessage());
        }
    }

    // for now uneccessary, but in future maybe will be use for something
    public LocalTime getEventTime(String eventName) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            LocalTime eventTime;

            String queryItsDefault = "SELECT EVENT_TIME_DEFAULT FROM " + "\"" + TABLE_EVENTS + "\"" +
                    " WHERE " + "\"" + EVENT_NAME + "\"" + " = " + "\"" + eventName + "\"";
            @SuppressLint("Recycle") Cursor cursorItsDefault = db.rawQuery(queryItsDefault, null);
            cursorItsDefault.moveToFirst();
            if (cursorItsDefault.getInt(cursorItsDefault.getColumnIndex("EVENT_TIME_DEFAULT")) == 1) {
                eventTime = new LocalTime(getDefaultTime());
                cursorItsDefault.close();
            } else {
                String queryEventTime = "SELECT " + "\"" + EVENT_TIME + "\"" + " FROM " + "\"" + TABLE_EVENTS + "\"" +
                        " WHERE " + "\"" + EVENT_NAME + "\"" + " = " + "\"" + eventName + "\"";
                @SuppressLint("Recycle") Cursor cursorEventTime = db.rawQuery(queryEventTime, null);
                cursorEventTime.moveToFirst();
                eventTime = new LocalTime(cursorEventTime.getLong(cursorEventTime.getColumnIndex(EVENT_TIME)));
                cursorEventTime.close();
            }
            return eventTime;
        } catch (Exception e) {
            Log.w("SQLite getEventTime", "Problem z zapytaniem do bazy danych " + e.getMessage());
            return null;
        }
    }

    // check event table for current date and check if the notification has been created
    @SuppressLint("LongLogTag")
    public boolean checkIfNotificationIsCreated(String eventName, LocalDate date) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            long dateInMillis = date.toDateTimeAtStartOfDay().getMillis();
            String query = "SELECT NOTIFICATION_CREATED FROM " + "\"" + eventName + "\"" +
                    " WHERE DAY  = " + "\"" + dateInMillis + "\"";
            @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, null);
            cursor.moveToFirst();
            boolean isCreated = cursor.getInt(cursor.getColumnIndex("NOTIFICATION_CREATED")) == 1;
            cursor.close();
            return isCreated;
        } catch (Exception e) {
            Log.w("SQLite checkIfNotificationIsCreated", "Problem z zapytaniem do bazy danych " + e.getMessage());
            return false;
        }
    }

    // get event ID from TABLE_EVENTS
    public int getEventId(String eventName) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            eventName = eventName.replace(" ", "_");
            String query = "SELECT _id FROM " + "\"" + TABLE_EVENTS + "\"" + " WHERE " + "\"" +
                    EVENT_NAME + "\"" + " = " + "\"" + eventName + "\"";
            @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, null);
            cursor.moveToFirst();
            int id;
            if (cursor.getCount() > 0) {
                id = cursor.getInt(cursor.getColumnIndex("_id"));
            } else {
                id = -1;
            }
            cursor.close();
            return id;
        } catch (Exception e) {
            Log.w("SQLite getEventId", "Problem z zapytaniem do bazy danych " + e.getMessage());
            return -1;
        }
    }

    // for TABLE_NOTIFICATION, insert information about created notification (name of event/events, date, time and information about display)
    public boolean insertNotification(String eventName, LocalTime eventNotificationTime, LocalDate eventNotificationDate, boolean notificationCompleted) {
        try {
            eventName = eventName.replace(" ", "_");
            long timeInMillis = eventNotificationTime.getMillisOfDay();
            long dateInMillis = eventNotificationDate.toDateTimeAtStartOfDay().getMillis();
            int notificationCompl;
            if (notificationCompleted) {
                notificationCompl = 1;
            } else {
                notificationCompl = 0;
            }
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(NOTIFICATION_EVENT_NAME, eventName);
            contentValues.put(NOTIFICATION_TIME, timeInMillis);
            contentValues.put(NOTIFICATION_DATE, dateInMillis);
            contentValues.put(NOTIFICATION_COMPLETED, notificationCompl);
            float result = db.insert(TABLE_NOTIFICATIONS, null, contentValues);
            return result != -1;
        } catch (Exception e) {
            Log.w("SQLite insertNotification", "Problem z umieszczeniem notifikacji " + e.getMessage());
            return false;
        }
    }

    // update information about showing notification (run only from RemindBroadcast for now, when notification is displayed)
    public void updateNotificationCompleted(String eventName, boolean notificationCompleted, LocalDate eventDate) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            long eventDateInMillis = eventDate.toDateTimeAtStartOfDay().getMillis();
            eventName = eventName.replace(" ", "_");
            String query;
            if (notificationCompleted) {
                query = "UPDATE " + "\"" + TABLE_NOTIFICATIONS + "\"" + " SET " + "\"" + NOTIFICATION_COMPLETED + "\"" + " = " + "\"" + 1
                        + "\"" + " WHERE " + "\"" + NOTIFICATION_EVENT_NAME + "\"" + " = " + "\"" + eventName + "\""
                        + " AND " + "\"" + NOTIFICATION_DATE + "\"" + " = " + "\"" + eventDateInMillis + "\"";
            } else {
                query = "UPDATE " + "\"" + TABLE_NOTIFICATIONS + "\"" + " SET " + "\"" + NOTIFICATION_COMPLETED + "\"" + " = " + "\"" + 0
                        + "\"" + " WHERE " + "\"" + NOTIFICATION_EVENT_NAME + "\"" + " = " + "\"" + eventName + "\""
                        + " AND " + "\"" + NOTIFICATION_DATE + "\"" + " = " + "\"" + eventDateInMillis + "\"";
            }
            db.execSQL(query);
        } catch (Exception e) {
            Log.w("SQLite updateNotificationCompleted", "Problem z zaktualizowaniem wpisu " + e.getMessage());
        }
    }

    // looking for notification in TABLE_NOTIFICATION
    public boolean checkNotificationCreatedButNotNotify(String alarmName, LocalDate date, LocalTime time) {
        try {
            alarmName = alarmName.replace(" ", "_");
            SQLiteDatabase db = this.getReadableDatabase();
            long dateInMillis = date.toDateTimeAtStartOfDay().getMillis();
            long timeInMillis = time.getMillisOfDay();
            String query = "SELECT * FROM " + "\"" + TABLE_NOTIFICATIONS + "\"" +
                    " WHERE " + "\"" + NOTIFICATION_EVENT_NAME + "\"" + " = " + "\"" + alarmName + "\""
                    + " AND " + "\"" + NOTIFICATION_DATE + "\"" + " = " + "\"" + dateInMillis + "\""
                    + " AND " + "\"" + NOTIFICATION_TIME + "\"" + " = " + "\"" + timeInMillis + "\""
                    + " AND " + "\"" + NOTIFICATION_COMPLETED + "\"" + " = 0";
            @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, null);
            boolean isCreated;
            if (cursor.getCount() > 0) {
                isCreated = true;
                cursor.close();
            } else {
                isCreated = false;
                cursor.close();
            }
            return isCreated;
        } catch (Exception e) {
            Log.w("SQLite checkIfNotificationIsCreated", "Problem z zapytaniem do bazy danych " + e.getMessage());
            return false;
        }
    }

    public void deleteNotification(String notificationName) {
        try {
            notificationName = notificationName.replace(" ", "_");
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "DELETE FROM " + "\"" + TABLE_NOTIFICATIONS + "\"" +
                    " WHERE " + "\"" + NOTIFICATION_EVENT_NAME + "\"" + " = " + "\"" + notificationName + "\"";
            db.execSQL(query);

        } catch (Exception e) {
            Log.w("SQLite checkIfNotificationIsCreated", "Problem z zapytaniem do bazy danych " + e.getMessage());
        }
    }
}
