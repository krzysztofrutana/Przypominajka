package com.example.przypominajka.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.przypominajka.R;
import com.example.przypominajka.activities.MainActivity;
import com.example.przypominajka.databases.PrzypominajkaDatabaseHelper;

import org.joda.time.LocalDate;

public class ReminderBroadcast extends BroadcastReceiver {

    final String ID = "notifyPrzypominajka";
    final String channelName = "PrzypominajkaChannel";
    int notifyID = 0;
    PrzypominajkaDatabaseHelper przypominajkaDatabaseHelper;

    @Override
    public void onReceive(Context context, Intent intent) {
        przypominajkaDatabaseHelper = new PrzypominajkaDatabaseHelper(context);

        String text = intent.getStringExtra("NOTIFY_TEXT");
        notifyID = intent.getIntExtra("ID", 100);

        Log.d("onReceive", "text : " + text + " notifyID: " + notifyID);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intentMainActivity = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                (notifyID - (2 * notifyID)) - 1000, intentMainActivity,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ID)
                .setSmallIcon(R.drawable.ic_baseline_alarm_24)
                .setContentTitle("Zbliżające się wydarzenia")
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        przypominajkaDatabaseHelper.updateNotificationCompleted(text, true, LocalDate.now());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
            manager.notify(notifyID, builder.build());
        } else {
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

            notificationManagerCompat.notify(notifyID, builder.build());
        }

    }
}
