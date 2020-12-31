package com.example.przypominajka.databases.interfaces;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.przypominajka.databases.entities.NotificationModel;

import java.util.List;

//DAO for NotificationModel class for Room Database
//INSERT, UPDATE and DELETE method return long/int value to check results of query
//checkNotificationCreatedButNotNotify method in two version, returns normal List or LiveData list
@Dao
public interface NotificationDAO {

    @Insert
    long insertNotification(NotificationModel notification);

    @Update
    int update(NotificationModel notification);

    @Delete
    int delete(NotificationModel notification);

    @Query("SELECT * FROM NOTIFICATIONS")
    LiveData<List<NotificationModel>> getAll();

    @Query("SELECT * FROM NOTIFICATIONS WHERE NOTIFICATION_EVENT_NAME LIKE :notificationEventName")
    NotificationModel findByEventName(String notificationEventName);

    @Query("SELECT * FROM  NOTIFICATIONS  WHERE  NOTIFICATION_EVENT_NAME LIKE :notificationEventName  AND  NOTIFICATION_COMPLETED  = 0")
    LiveData<List<NotificationModel>> getNoCompletedNotificationLiveData(String notificationEventName);

    @Query("SELECT * FROM  NOTIFICATIONS  WHERE  NOTIFICATION_EVENT_NAME LIKE :notificationEventName  AND  NOTIFICATION_COMPLETED  = 0")
    List<NotificationModel> getNoCompletedNotificationList(String notificationEventName);

    @Query("UPDATE NOTIFICATIONS SET NOTIFICATION_COMPLETED  = :notificationCompleted WHERE NOTIFICATION_EVENT_NAME LIKE :notificationName " +
            " AND NOTIFICATION_DATE LIKE :notificationDate")
    int updateNotificationCompleted(String notificationName, long notificationDate, boolean notificationCompleted);

    @Query("SELECT * FROM NOTIFICATIONS WHERE NOTIFICATION_EVENT_NAME LIKE :notificationName AND  NOTIFICATION_DATE LIKE :notificationDate" +
            " AND  NOTIFICATION_TIME LIKE :notificationTime AND NOTIFICATION_COMPLETED = 0")
    LiveData<List<NotificationModel>> checkNotificationCreatedButNotNotify(String notificationName, long notificationDate, long notificationTime);

    @Query("SELECT * FROM NOTIFICATIONS WHERE NOTIFICATION_EVENT_NAME LIKE :notificationName AND  NOTIFICATION_DATE LIKE :notificationDate" +
            " AND  NOTIFICATION_TIME LIKE :notificationTime AND NOTIFICATION_COMPLETED = 0")
    List<NotificationModel> checkNotificationCreatedButNotNotifyList(String notificationName, long notificationDate, long notificationTime);

}
