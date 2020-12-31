package com.example.przypominajka.databases.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.przypominajka.databases.PrzypominajkaDatabase;
import com.example.przypominajka.databases.entities.NotificationModel;
import com.example.przypominajka.databases.interfaces.NotificationDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

//Repository layer for Notification class using Room Database
//INSERT, UPDATE and DELETE method return long/int value to check results of query
//all methods are run on a separate thread
//getAll return LiveData, so automatically use separated thread
public class NotificationRepository {

    private NotificationDAO notificationDAO;
    private LiveData<List<NotificationModel>> allNotification;

    public NotificationRepository(Application application) {
        PrzypominajkaDatabase db = PrzypominajkaDatabase.getDatabase(application);
        notificationDAO = db.notificationDAO();
        allNotification = notificationDAO.getAll();
    }

    public long insertNotification(final NotificationModel notificationModel) {
        long results = -1;
        try {
            results = PrzypominajkaDatabase.databaseWriteExecutor.submit(() -> notificationDAO.insertNotification(notificationModel)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return results;
    }

    public int updateNotification(final NotificationModel notificationModel) {
        int results = -1;
        try {
            results = PrzypominajkaDatabase.databaseWriteExecutor.submit(() -> notificationDAO.update(notificationModel)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return results;
    }

    public int delete(final NotificationModel notificationModel) {
        int results = -1;
        try {
            results = PrzypominajkaDatabase.databaseWriteExecutor.submit(() -> notificationDAO.delete(notificationModel)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return results;

    }

    public LiveData<List<NotificationModel>> getAll() {
        return allNotification;
    }

    public NotificationModel findByEventName(String notificationEventName) {
        NotificationModel notificationModel = new NotificationModel();
        try {
            notificationModel = PrzypominajkaDatabase.databaseWriteExecutor.submit(() -> notificationDAO.findByEventName(notificationEventName)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return notificationModel;
    }

    public LiveData<List<NotificationModel>> getNoCompletedNotificationLiveData(String notificationEventName) {
        return notificationDAO.getNoCompletedNotificationLiveData(notificationEventName);
    }

    public List<NotificationModel> getNoCompletedNotificationList(String notificationEventName) {
        List<NotificationModel> notificationModelList = new ArrayList<>();
        try {
            notificationModelList = PrzypominajkaDatabase.databaseWriteExecutor.submit(() -> notificationDAO.getNoCompletedNotificationList(notificationEventName)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return notificationModelList;
    }

    public int updateNotificationCompleted(final String notificationName, final long notificationDate, final boolean notificationCompleted) {
        int results = -1;
        try {
            results = PrzypominajkaDatabase.databaseWriteExecutor.submit(() -> notificationDAO.updateNotificationCompleted(notificationName,
                    notificationDate, notificationCompleted)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return results;

    }

    public LiveData<List<NotificationModel>> checkNotificationCreatedButNotNotify(String notificationName, long notificationDate, long notificationTime) {
        return notificationDAO.checkNotificationCreatedButNotNotify(notificationName, notificationDate, notificationTime);
    }

    public List<NotificationModel> checkNotificationCreatedButNotNotifyList(String notificationName, long notificationDate, long notificationTime) {
        List<NotificationModel> notificationCreatedButNotNotify = new ArrayList<>();
        try {
            notificationCreatedButNotNotify = Executors.newSingleThreadExecutor().submit(() ->
                    notificationDAO.checkNotificationCreatedButNotNotifyList(notificationName, notificationDate, notificationTime)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return notificationCreatedButNotNotify;
    }
}
