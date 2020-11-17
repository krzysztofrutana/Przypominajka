package com.example.przypominajka.databases.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

//Entity for Room Database to create table contains settings
//This class have all necessary method for Settings class
@Entity(tableName = "SETTINGS")
public class SettingsModel {

    private static final String DEFAULT_TIME = "DEFAULT_TIME";
    private static final String NOTIFY_CHECK_INTERVAL = "NOTIFY_CHECK_INTERVAL";
    private static final String LOCAL_BACKUP_LOCATION = "LOCAL_BACKUP_LOCATION";

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = DEFAULT_TIME)
    public long defaultTime;

    @ColumnInfo(name = NOTIFY_CHECK_INTERVAL)
    public long notifyCheckInterval;

    @ColumnInfo(name = LOCAL_BACKUP_LOCATION)
    public String localBackupLocation;

    public SettingsModel() {
    }

    @Ignore
    public SettingsModel(long defaultTime, long notifyCheckInterval, String localBackupLocation) {
        this.defaultTime = defaultTime;
        this.notifyCheckInterval = notifyCheckInterval;
        this.localBackupLocation = localBackupLocation;
    }

    public long getDefaultTime() {
        return defaultTime;
    }

    public long getNotifyCheckInterval() {
        return notifyCheckInterval;
    }

    public String getLocalBackupLocation() {
        return localBackupLocation;
    }
}
