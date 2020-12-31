package com.example.przypominajka.utils.databaseBackupAndRestore.BackupDatabase;

import android.app.Activity;
import android.content.Context;

import com.example.przypominajka.databases.PrzypominajkaDatabase;
import com.example.przypominajka.utils.DriveServiceHelper;
import com.example.przypominajka.utils.MyPrzypominajkaApp;
import com.example.przypominajka.utils.Permissions;
import com.example.przypominajka.databases.viewModels.SettingsViewModel;
import com.google.android.gms.tasks.Task;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.File;

import java.util.Collections;

public class RemoteBackupDatabase {

    private static final String DB_NAME = "Przypominajka.db";

    private final Context context;
    private final Activity activity;
    private final SettingsViewModel settingsViewModel = new SettingsViewModel(MyPrzypominajkaApp.get());
    DriveServiceHelper driveServiceHelper;

    public RemoteBackupDatabase(Context context, Activity activity, DriveServiceHelper driveServiceHelper) {
        this.context = context;
        this.activity = activity;
        this.driveServiceHelper = driveServiceHelper;
    }

    public void createBackup(String fileName) {
        Permissions.verifyStoragePermissions(activity);
        PrzypominajkaDatabase przypominajkaDatabase = PrzypominajkaDatabase.getDatabase(context);
        przypominajkaDatabase.close();

        java.io.File dbfile = context.getDatabasePath(DB_NAME);


        File backup = new File()
                .setParents(Collections.singletonList("root"))
                .setMimeType("application/octet-stream")
                .setName(fileName);

        FileContent mediaContent = new FileContent("application/octet-stream", dbfile);
        Task<String> fileIDTask = driveServiceHelper.createFile(backup, mediaContent);
        String fileID = fileIDTask.getResult();
        String actualRemoteBackupFileName = settingsViewModel.getRemoteBackupFileName();
        actualRemoteBackupFileName = actualRemoteBackupFileName + "|" + fileID;
        settingsViewModel.updateRemoteBackupFileName(actualRemoteBackupFileName);

    }
}
