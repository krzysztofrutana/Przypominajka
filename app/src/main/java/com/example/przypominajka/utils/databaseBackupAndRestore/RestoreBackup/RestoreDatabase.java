package com.example.przypominajka.utils.databaseBackupAndRestore.RestoreBackup;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.example.przypominajka.databases.PrzypominajkaDatabase;
import com.example.przypominajka.utils.MyPrzypominajkaApp;
import com.example.przypominajka.utils.Permissions;
import com.example.przypominajka.databases.viewModels.EventsViewModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

public class RestoreDatabase {

    private static final String DB_NAME = "Przypominajka.db";

    private final Context context;
    private final Activity activity;

    public RestoreDatabase(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;

    }

    public boolean restoreBackup(InputStream inputStreamNewDB) {
        Permissions.verifyStoragePermissions(activity);
        PrzypominajkaDatabase przypominajkaDatabase = PrzypominajkaDatabase.getDatabase(context);
        przypominajkaDatabase.close();

        File oldDB = context.getDatabasePath(DB_NAME);

        if (inputStreamNewDB != null) {
            try {
                FileChannel fromChannel = null;
                FileChannel toChannel = null;
                try {
                    fromChannel = ((FileInputStream) inputStreamNewDB).getChannel();
                    toChannel = new FileOutputStream(oldDB).getChannel();
                    fromChannel.transferTo(0, fromChannel.size(), toChannel);
                } finally {
                    try {
                        if (fromChannel != null) {
                            fromChannel.close();
                        }
                    } finally {
                        if (toChannel != null) {
                            toChannel.close();
                        }
                    }
                }
                boolean results = validateDB();
                return results;
            } catch (IOException e) {
                Log.d("restoreBackup", "Problem z odtworzeniem bazy: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } else {
            Log.d("restoreBackup", "Plik nie istnieje");
            return false;
        }
    }

    private boolean validateDB() {
        EventsViewModel eventsViewModel = new EventsViewModel(MyPrzypominajkaApp.get());
        if (eventsViewModel.getAllEventsList().size() > 0) {
            return true;
        } else {
            return false;
        }
    }
}
