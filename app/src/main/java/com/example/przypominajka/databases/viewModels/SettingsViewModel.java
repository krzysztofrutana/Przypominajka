package com.example.przypominajka.databases.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.przypominajka.databases.entities.SettingsModel;
import com.example.przypominajka.databases.repositories.SettingsRepository;

// ViewModel Layer for event model and event repository
public class SettingsViewModel extends AndroidViewModel {


    private LiveData<SettingsModel> settings;
    private SettingsRepository settingsRepository;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        settingsRepository = new SettingsRepository(application);
        settings = settingsRepository.getSettings();
    }

    public LiveData<SettingsModel> getSettings() {
        return settings;
    }

    public long insertSettings(SettingsModel settingsModel) {
        return settingsRepository.insertSettings(settingsModel);
    }

    public int delete(SettingsModel settingsModel) {
        return settingsRepository.delete(settingsModel);
    }

    public int update(SettingsModel settingsModel) {
        return settingsRepository.update(settingsModel);
    }

    public int updateDefaultTime(long defaultTime) {
        return settingsRepository.updateDefaultTime(defaultTime);
    }

    public long getDefaultTime() {
        return settingsRepository.getDefaultTime();
    }

    public int updateCheckEventInterval(long intervalTime) {
        return settingsRepository.updateCheckEventInterval(intervalTime);
    }

    public long getCheckEventInterval() {
        return settingsRepository.getCheckEventInterval();
    }

    public LiveData<Integer> getRowCount() {
        return settingsRepository.getRowCount();
    }

    public LiveData<String> getLocalBackupLocationLiveData() {
        return settingsRepository.getLocalBackupLocationLiveData();
    }

    public String getLocalBackupLocation() {
        return settingsRepository.getLocalBackupLocation();
    }

    public int updateLocalBackupLocation(String location) {
        return settingsRepository.updateLocalBackupLocation(location);
    }

    public int updateRemoteBackupFileName(String backupName) {
        return settingsRepository.updateRemoteBackupFileName(backupName);
    }

    public String getRemoteBackupFileName() {
        return settingsRepository.getRemoteBackupFileName();
    }
}
