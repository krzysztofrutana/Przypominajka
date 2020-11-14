package com.example.przypominajka.databases;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.przypominajka.databases.entities.EventModel;
import com.example.przypominajka.databases.entities.NotificationModel;
import com.example.przypominajka.databases.entities.SettingsModel;
import com.example.przypominajka.databases.interfaces.EventsDAO;
import com.example.przypominajka.databases.interfaces.NotificationDAO;
import com.example.przypominajka.databases.interfaces.SettingsDAO;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// abstract class to create instance of Room database
@Database(entities = {EventModel.class, NotificationModel.class, SettingsModel.class}, version = 1)
public abstract class PrzypominajkaDatabase extends RoomDatabase {

    public abstract EventsDAO eventsDAO();

    public abstract NotificationDAO notificationDAO();

    public abstract SettingsDAO settingsDAO();

    private static final String DB_NAME = "Przypominajka.db";

    private static PrzypominajkaDatabase instance;

    // creating ExecutorService to use in repositories class to run method on separated thread
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static PrzypominajkaDatabase getDatabase(final Context context) {
        if (instance == null) {
            synchronized (PrzypominajkaDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            PrzypominajkaDatabase.class, DB_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }

    public void deleteInstanceOfDatabase() {
        instance = null;
    }
}
