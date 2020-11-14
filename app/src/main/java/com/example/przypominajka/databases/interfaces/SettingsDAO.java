package com.example.przypominajka.databases.interfaces;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.przypominajka.databases.entities.SettingsModel;

//DAO for NotificationModel class for Room Database
//INSERT, UPDATE and DELETE method return long/int value to check results of query
@Dao
public interface SettingsDAO {

    @Insert
    long insertSettings(SettingsModel settingsModel);

    @Delete
    int delete(SettingsModel settingsModel);

    @Update
    int update(SettingsModel settingsModel);

    @Query("UPDATE SETTINGS SET DEFAULT_TIME = :defaultTime")
    int updateDefaultTime(long defaultTime);

    @Query("SELECT DEFAULT_TIME FROM SETTINGS")
    long getDefaultTime();

    @Query("UPDATE SETTINGS SET NOTIFY_CHECK_INTERVAL = :intervalTime")
    int updateCheckEventInterval(long intervalTime);

    @Query("SELECT NOTIFY_CHECK_INTERVAL FROM SETTINGS")
    long getCheckEventInterval();

    @Query("SELECT * FROM SETTINGS")
    LiveData<SettingsModel> getSettings();

    @Query("SELECT COUNT(DEFAULT_TIME) FROM SETTINGS")
    LiveData<Integer> getRowCount();


}
