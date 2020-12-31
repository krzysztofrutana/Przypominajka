package com.example.przypominajka.databases.entities;

import android.content.ContentValues;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.OnConflictStrategy;
import androidx.room.PrimaryKey;
import androidx.sqlite.db.SupportSQLiteDatabase;

//Template to create table contains current event days
public class EventsTimeTemplateModel {

    public static final String TEMPLATE_TABLE_NAME = "EVENTS_TIME_TEMPLATE_TABLE";
    public static final String TEMPLATE_TABLE_COL_ID = "_id";
    public static final String TEMPLATE_TABLE_COL_DAY = "DAY";
    public static final String TEMPLATE_TABLE_COL_NOTIFICATION_CREATED = "NOTIFICATION_CREATED";
    public static final String TEMPLATE_TABLE_NAME_PLACEHOLDER = ":tablename:";
    public static final String TEMPLATE_TABLE_COL_EVENT_MADE = "EVENT_MADE";
    public static final String TEMPLATE_TABLE_NAME_CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
            + TEMPLATE_TABLE_NAME_PLACEHOLDER +
            "(" +
            TEMPLATE_TABLE_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            TEMPLATE_TABLE_COL_DAY + " REAL, " +
            TEMPLATE_TABLE_COL_EVENT_MADE + " INTEGER DEFAULT 0, " +
            TEMPLATE_TABLE_COL_NOTIFICATION_CREATED + " INTEGER DEFAULT 0);";


    long dayDate;
    int isCreated;

    public EventsTimeTemplateModel(long dayDate, int isCreated) {
        this.dayDate = dayDate;
        this.isCreated = isCreated;
    }


    @Ignore
    public static Long insertRow(SupportSQLiteDatabase sdb, String tableName, long dayDate, int isCreated, int isMade) {
        ContentValues cv = new ContentValues();
        cv.put(TEMPLATE_TABLE_COL_DAY, dayDate);
        cv.put(TEMPLATE_TABLE_COL_NOTIFICATION_CREATED, isCreated);
        cv.put(TEMPLATE_TABLE_COL_EVENT_MADE, isMade);
        return sdb.insert(tableName, OnConflictStrategy.IGNORE, cv);
    }

}
