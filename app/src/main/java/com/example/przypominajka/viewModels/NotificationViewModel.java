package com.example.przypominajka.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.przypominajka.databases.entities.NotificationModel;
import com.example.przypominajka.databases.repositories.NotificationRepository;

import java.util.List;

// ViewModel Layer for event model and notification repository
public class NotificationViewModel extends AndroidViewModel {

    private LiveData<List<NotificationModel>> allNotification;
    private NotificationRepository notificationRepository;

    public NotificationViewModel(@NonNull Application application) {
        super(application);
        notificationRepository = new NotificationRepository(application);
        allNotification = notificationRepository.getAll();
    }

    public LiveData<List<NotificationModel>> getAllNotification() {
        return allNotification;
    }

    public long insertNotification(NotificationModel notificationModel) {
        return notificationRepository.insertNotification(notificationModel);
    }

    public int delete(NotificationModel notificationModel) {
        return notificationRepository.delete(notificationModel);
    }

    public NotificationModel findByEventName(String notificationEventName) {
        return notificationRepository.findByEventName(notificationEventName);
    }

    public LiveData<List<NotificationModel>> getNoCompletedNotification(String notificationEventName) {
        return notificationRepository.getNoCompletedNotification(notificationEventName);
    }

    public int updateNotificationCompleted(String notificationName, long notificationDate, boolean notificationCompleted) {
        return notificationRepository.updateNotificationCompleted(notificationName, notificationDate, notificationCompleted);
    }

    public LiveData<List<NotificationModel>> checkNotificationCreatedButNotNotify(String notificationName, long notificationDate, long notificationTime) {
        return notificationRepository.checkNotificationCreatedButNotNotify(notificationName, notificationDate, notificationTime);
    }

    public List<NotificationModel> checkNotificationCreatedButNotNotifyList(String notificationName, long notificationDate, long notificationTime) {
        return notificationRepository.checkNotificationCreatedButNotNotifyList(notificationName, notificationDate, notificationTime);
    }
}
