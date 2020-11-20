package com.example.przypominajka.utils.databaseBackupAndRestore.BackupDatabase;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.przypominajka.databases.PrzypominajkaDatabase;
import com.example.przypominajka.utils.MyPrzypominajkaApp;
import com.example.przypominajka.utils.Permissions;
import com.example.przypominajka.viewModels.SettingsViewModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LocalBackupDatabase {

    private static final String DB_NAME = "Przypominajka.db";

    private final Context context;
    private final String[] pathToCopy;
    private final Activity activity;

    public LocalBackupDatabase(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        SettingsViewModel settingsViewModel = new SettingsViewModel(MyPrzypominajkaApp.get());
        pathToCopy = settingsViewModel.getLocalBackupLocation().split("\\|");
    }

    public void createBackup() throws IOException {
        Permissions.verifyStoragePermissions(activity);
        PrzypominajkaDatabase przypominajkaDatabase = PrzypominajkaDatabase.getDatabase(context);
        przypominajkaDatabase.close();

        File dbfile = context.getDatabasePath(DB_NAME);

        File savefile = new File(pathToCopy[0]);
        if (savefile.exists()) {
            Toast.makeText(context, "Plik już istnieje. Zastępuje już istniejący", Toast.LENGTH_LONG).show();
            savefile.delete();
        }
        try {
            if (savefile.createNewFile()) {
                int buffersize = 8 * 1024;
                byte[] buffer = new byte[buffersize];
                int bytes_read = buffersize;
                OutputStream savedb = new FileOutputStream(pathToCopy[0]);
                InputStream indb = new FileInputStream(dbfile);
                while ((bytes_read = indb.read(buffer, 0, buffersize)) > 0) {
                    savedb.write(buffer, 0, bytes_read);
                }
                savedb.flush();
                indb.close();
                savedb.close();
                Toast.makeText(context, "Kopia zapasowa wykonana", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("LocalBackupSettings createBackup", "Wystąpił problem przy twórzeniu kopii: " + e.getMessage());
        }

    }
}
