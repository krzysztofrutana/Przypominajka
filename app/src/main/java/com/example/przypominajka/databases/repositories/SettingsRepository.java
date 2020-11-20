package com.example.przypominajka.databases.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.przypominajka.databases.PrzypominajkaDatabase;
import com.example.przypominajka.databases.entities.SettingsModel;
import com.example.przypominajka.databases.interfaces.SettingsDAO;

import java.util.concurrent.ExecutionException;

//Repository layer for EventModel class using Room Database
//INSERT, UPDATE and DELETE method return long/int value to check results of query
//all methods are run on a separate thread
//getSettings return LiveData, so automatically use separated thread
public class SettingsRepository {

    private SettingsDAO settingsDAO;
    private LiveData<SettingsModel> settingsModel;

    public SettingsRepository(Application application) {
        PrzypominajkaDatabase db = PrzypominajkaDatabase.getDatabase(application);
        settingsDAO = db.settingsDAO();
        settingsModel = settingsDAO.getSettings();
    }

    public LiveData<SettingsModel> getSettings() {
        return settingsModel;
    }

    public long insertSettings(final SettingsModel settingsModel) {
        long results = -1;
        try {
            results = PrzypominajkaDatabase.databaseWriteExecutor.submit(() -> settingsDAO.insertSettings(settingsModel)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return results;
    }

    public int delete(final SettingsModel settingsModel) {
        int results = -1;
        try {
            results = PrzypominajkaDatabase.databaseWriteExecutor.submit(() -> settingsDAO.delete(settingsModel)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return results;
    }

    public int update(final SettingsModel settingsModel) {
        int results = -1;
        try {
            results = PrzypominajkaDatabase.databaseWriteExecutor.submit(() -> settingsDAO.update(settingsModel)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return results;
    }

    public int updateDefaultTime(final long defaultTime) {
        int results = -1;
        try {
            results = PrzypominajkaDatabase.databaseWriteExecutor.submit(() -> settingsDAO.updateDefaultTime(defaultTime)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return results;
    }

    public long getDefaultTime() {
        long defaultTime = -1;
        try {
            defaultTime = PrzypominajkaDatabase.databaseWriteExecutor.submit(() -> settingsDAO.getDefaultTime()).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return defaultTime;
    }

    public int updateCheckEventInterval(final long intervalTime) {
        int results = -1;
        try {
            results = PrzypominajkaDatabase.databaseWriteExecutor.submit(() -> settingsDAO.updateCheckEventInterval(intervalTime)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return results;
    }

    public long getCheckEventInterval() {
        long results = -1;
        try {
            results = PrzypominajkaDatabase.databaseWriteExecutor.submit(() -> settingsDAO.getCheckEventInterval()).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return results;
    }

    public LiveData<Integer> getRowCount() {
        return settingsDAO.getRowCount();
    }

    public LiveData<String> getLocalBackupLocationLiveData() {
        return settingsDAO.getLocalBackupLocationLiveData();
    }

    public String getLocalBackupLocation() {
        String results = "";
        try {
            results = PrzypominajkaDatabase.databaseWriteExecutor.submit(() -> settingsDAO.getLocalBackupLocation()).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return results;
    }

    public int updateLocalBackupLocation(final String location) {
        int results = -1;
        try {
            results = PrzypominajkaDatabase.databaseWriteExecutor.submit(() -> settingsDAO.updateLocalBackupLocation(location)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return results;
    }

    public int updateRemoteBackupFileName(String backupName) {
        int results = -1;
        try {
            results = PrzypominajkaDatabase.databaseWriteExecutor.submit(() -> settingsDAO.updateRemoteBackupFileName(backupName)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return results;
    }

    public String getRemoteBackupFileName() {
        String results = "";
        try {
            results = PrzypominajkaDatabase.databaseWriteExecutor.submit(() -> settingsDAO.getRemoteBackupFileName()).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return results;
    }
}
