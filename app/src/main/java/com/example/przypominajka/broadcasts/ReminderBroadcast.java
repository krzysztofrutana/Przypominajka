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
import com.example.przypominajka.databases.viewModels.NotificationViewModel;
import com.example.przypominajka.utils.MyPrzypominajkaApp;

import org.joda.time.LocalDate;

public class ReminderBroadcast extends BroadcastReceiver {

    final String ID = "notifyPrzypominajka";
    final String channelName = "PrzypominajkaChannel";
    int notifyID = 0;
    private NotificationViewModel notificationViewModel = new NotificationViewModel(MyPrzypominajkaApp.get());
    private static final String TAG = "RemindBroadcast";


    @Override
    public void onReceive(Context context, Intent intent) {

        String text = intent.getStringExtra("NOTIFY_TEXT");
        notifyID = intent.getIntExtra("ID", 100);
        Log.d(TAG, "OnRecive: text : " + text + " notifyID: " + notifyID);

        // if text is null not notify this notification
        if (text == null) {
            return;
        }

        // create pending intent which start MainActivity after click on notification
        Intent intentMainActivity = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                (notifyID - (2 * notifyID)) - 1000, intentMainActivity,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // one or more event must have other title
        String title;
        if (intent.getIntExtra("MANY", 1) == 1) {
            title = "Zbliżające się wydarzenie";
        } else {
            title = "Zbliżające się wydarzenia";
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ID)
                .setSmallIcon(R.drawable.ic_baseline_alarm_24)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // update information about notification in table, this is protection against notify this same notification two times
        int result = notificationViewModel.updateNotificationCompleted(text, LocalDate.now().toDateTimeAtStartOfDay().getMillis(), true);
        if (result > 0) {
            Log.d(TAG, "OnRecive: Informacja w wydarzeniu " + text + " zaktualizowana");
        } else {
            Log.d(TAG, "OnRecive: Nie udało się zaktualizować kolumny o notyfikacji w wydarzeniu " + text);
        }

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
