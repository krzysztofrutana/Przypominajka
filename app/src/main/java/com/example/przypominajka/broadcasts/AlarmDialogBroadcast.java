package com.example.przypominajka.broadcasts;

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
import com.example.przypominajka.databases.entities.EventModel;
import com.example.przypominajka.databases.repositories.EventsRepository;
import com.example.przypominajka.utils.CheckNotDoneEvent;

import org.joda.time.LocalDate;

//TODO its don't work correctly, to fix.

public class AlarmDialogBroadcast extends BroadcastReceiver {

    final String ID = "notifyPrzypominajkaNotMadeEvents";
    final String channelName = "PrzypominajkaChannel";

    private int notifyID;

    private CheckNotDoneEvent moveNotDoneEvent = new CheckNotDoneEvent();

    private static final String TAG = "AlarmDialogBroadcast";

    @Override
    public void onReceive(Context context, Intent intent) {
        notifyID = intent.getIntExtra("ID", 11000);

        Log.d(TAG, "onRecive: uruchomiono notifikacje");
        Intent intentMainActivity = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                12000, intentMainActivity,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ID)
                .setSmallIcon(R.drawable.ic_baseline_alarm_24)
                .setContentTitle("Niezrealizowane wydarzenia")
                .setContentText("Masz oczkujące na decyzję niezrealizowane wydarzenia. Kliknij by zobaczyć.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
            manager.notify(notifyID, builder.build());
        } else {
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            notificationManagerCompat.notify(notifyID, builder.build());
        }
    }
}
