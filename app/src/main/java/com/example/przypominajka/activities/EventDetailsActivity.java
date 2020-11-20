package com.example.przypominajka.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.przypominajka.R;
import com.example.przypominajka.databases.entities.EventModel;
import com.example.przypominajka.databases.entities.NotificationModel;
import com.example.przypominajka.utils.MyPrzypominajkaApp;
import com.example.przypominajka.utils.ReminderBroadcast;
import com.example.przypominajka.viewModels.EventsViewModel;
import com.example.przypominajka.viewModels.NotificationViewModel;

import org.joda.time.format.DateTimeFormat;

import java.util.List;


public class EventDetailsActivity extends AppCompatActivity {

    //Strings
    private static final String ABSENCE = MyPrzypominajkaApp.get().getResources().getString(R.string.EVA_absence);
    private static final String ALWAYS_REMIND_WHAT = MyPrzypominajkaApp.get().getResources().getString(R.string.EVA_always_remind_what);
    private static final String REMIND_WHAT = MyPrzypominajkaApp.get().getResources().getString(R.string.EVA_remind_what);
    private static final String ONE_TIME = MyPrzypominajkaApp.get().getResources().getString(R.string.EVA_one_time);

    private String eventName;

    private EventModel event = new EventModel();

    private TextView eventNameView;
    private TextView eventDiscriptionView;
    private Button colorButtonView;
    private TextView eventTypeViewPart1;
    private TextView eventTypeViewPart2;
    private TextView eventTypeViewPart3;
    private TextView eventTimeView;
    private TextView startDateView;

    private final NotificationViewModel notificationViewModel = new NotificationViewModel(getApplication());
    private final EventsViewModel eventsViewModel = new EventsViewModel(getApplication());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        // setting toolbar with back button and delete event button
        Toolbar toolbar = findViewById(R.id.toolbarEventDetails);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);


        // get name of event
        eventName = getIntent().getStringExtra("EVENT_NAME");

        // set everything necessary views
        eventNameView = findViewById(R.id.eventNameDetails);
        eventDiscriptionView = findViewById(R.id.eventDiscriptionDetails);
        colorButtonView = findViewById(R.id.buttonColorDetails);
        eventTypeViewPart1 = findViewById(R.id.textEventTypePart1);
        eventTypeViewPart2 = findViewById(R.id.textEventTypePart2);
        eventTypeViewPart3 = findViewById(R.id.textEventTypePart3);
        eventTimeView = findViewById(R.id.textViewEventTime);
        startDateView = findViewById(R.id.textViewSetDate);

        // run methods
        getInformationAboutEvent();
        setInformationAboutEvent();

    }

    // needed to delete button on toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_event_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("ShowToast")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete_event) {
            // deleting event
            // TODO , i don't know why, but this dont work correctly, notify still is being send to BroadcastReceiver class\
            try {
                List<NotificationModel> notificationList = notificationViewModel.getNoCompletedNotification(eventName).getValue();
                int result = eventsViewModel.deleteEvent(eventsViewModel.findByEventName(eventName));
                if (result > 0) {
                    assert notificationList != null;
                    for (NotificationModel notification : notificationList) {
                        int resultDelete = notificationViewModel.delete(notification);
                        if (resultDelete > 0) {
                            Intent notificationIntent = new Intent(getApplicationContext(), ReminderBroadcast.class);
                            notificationIntent.putExtra("NOTIFY_TEXT", eventName);
                            notificationIntent.putExtra("ID", notification.getNotificationID());

                            PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(),
                                    notification.getNotificationID(),
                                    notificationIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);

                            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                            alarmManager.cancel(alarmIntent);

                            NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                            manager.cancel(notification.getNotificationID());
                        }
                    }
                    Toast.makeText(this, "Zdarzenie usunięte pomyślnie", Toast.LENGTH_LONG).show();
                    this.finish();
                    return true;
                } else {
                    Toast.makeText(this, "Problem przy usuwanie zdarzenia", Toast.LENGTH_LONG).show();
                    Log.w("Delete_event", "Problem przy usuwanie zdarzenia");
                    return false;
                }
            } catch (Exception e) {
                Toast.makeText(this, "Problem przy usuwanie zdarzenia", Toast.LENGTH_LONG).show();
                Log.w("Delete_event", "Problem przy usuwanie zdarzenia " + e.getMessage());
                return false;
            }
            // back button on toolbar action
        } else if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    // set information in views
    private void setInformationAboutEvent() {

        eventNameView.setText(eventName.replaceAll("_", " "));
        if (event.getEventDiscription().length() == 0) {
            eventDiscriptionView.setText(ABSENCE);
        } else {
            eventDiscriptionView.setText(event.getEventDiscription());
        }
        Drawable unwrappedDrawable = AppCompatResources.getDrawable(EventDetailsActivity.this, R.drawable.button_corner_radius_add_new_activity);
        assert unwrappedDrawable != null;
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, event.getEventColor());
        colorButtonView.setBackground(wrappedDrawable);
        if (event.getItsMonthInterval()) {
            String eventTypeMonthIntervalPart1 = "Przypominaj " + event.getTimeInterval();
            String eventTypeMonthIntervalPart2 = " dnia miesiąca";
            String eventTypeMonthIntervalPart3;
            if (event.getMonthNumberOfRepeats() == 1) {
                eventTypeMonthIntervalPart3 = "jeden raz";
            } else if (event.getMonthNumberOfRepeats() >= 2 && event.getMonthNumberOfRepeats() <= 4) {
                eventTypeMonthIntervalPart3 = "przez " + event.getMonthNumberOfRepeats() + " miesiące";
            } else {
                eventTypeMonthIntervalPart3 = "przez " + event.getMonthNumberOfRepeats() + " miesięcy";
            }

            eventTypeViewPart1.setText(eventTypeMonthIntervalPart1);
            eventTypeViewPart2.setText(eventTypeMonthIntervalPart2);
            eventTypeViewPart3.setText(eventTypeMonthIntervalPart3);
        } else if (event.getItCustomTimeInterval()) {
            String eventTypeShortInterval;
            if (event.getItsCustomTimeRepeatsAllTime()) {
                eventTypeViewPart1.setText(ALWAYS_REMIND_WHAT);
            } else {
                eventTypeViewPart1.setText(REMIND_WHAT);
            }
            if (event.getCustomTimeType() == 1) {
                if (event.getTimeInterval() == 1) {
                    if (event.getItsCustomTimeRepeatsAllTime()) {
                        eventTypeShortInterval = event.getTimeInterval() + " dzień";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        eventTypeViewPart3.setVisibility(LinearLayout.GONE);
                    } else {
                        eventTypeShortInterval = event.getTimeInterval() + " dzień";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        String eventTypeShortIntervalPart3 = event.getCustomTimeNumberOfRepeats() + " razy";
                        eventTypeViewPart3.setText(eventTypeShortIntervalPart3);
                    }


                } else {
                    if (event.getItsCustomTimeRepeatsAllTime()) {
                        eventTypeShortInterval = event.getTimeInterval() + " dni";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        eventTypeViewPart3.setVisibility(LinearLayout.GONE);
                    } else {
                        eventTypeShortInterval = event.getTimeInterval() + " dni";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        String eventTypeShortIntervalPart3 = event.getCustomTimeNumberOfRepeats() + " razy";
                        eventTypeViewPart3.setText(eventTypeShortIntervalPart3);
                    }

                }
            } else if (event.getCustomTimeType() == 2) {
                if (event.getTimeInterval() == 1) {
                    if (event.getItsCustomTimeRepeatsAllTime()) {
                        eventTypeShortInterval = event.getTimeInterval() + " tydzień";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        eventTypeViewPart3.setVisibility(LinearLayout.GONE);
                    } else {
                        eventTypeShortInterval = event.getTimeInterval() + " tydzień";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        String eventTypeShortIntervalPart3 = event.getCustomTimeNumberOfRepeats() + " razy";
                        eventTypeViewPart3.setText(eventTypeShortIntervalPart3);
                    }
                } else if (event.getTimeInterval() >= 2 && event.getTimeInterval() <= 4) {
                    if (event.getItsCustomTimeRepeatsAllTime()) {
                        eventTypeShortInterval = event.getTimeInterval() + " tygodnie";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        eventTypeViewPart3.setVisibility(LinearLayout.GONE);
                    } else {
                        eventTypeShortInterval = event.getTimeInterval() + " tygodnie";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        String eventTypeShortIntervalPart3 = event.getCustomTimeNumberOfRepeats() + " razy";
                        eventTypeViewPart3.setText(eventTypeShortIntervalPart3);
                    }
                } else {
                    if (event.getItsCustomTimeRepeatsAllTime()) {
                        eventTypeShortInterval = event.getTimeInterval() + " tygodnii";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        eventTypeViewPart3.setVisibility(LinearLayout.GONE);
                    } else {
                        eventTypeShortInterval = event.getTimeInterval() + " tygodnii";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        String eventTypeShortIntervalPart3 = event.getCustomTimeNumberOfRepeats() + " razy";
                        eventTypeViewPart3.setText(eventTypeShortIntervalPart3);
                    }
                }
            } else if (event.getCustomTimeType() == 3) {
                if (event.getTimeInterval() == 1) {
                    if (event.getItsCustomTimeRepeatsAllTime()) {
                        eventTypeShortInterval = event.getTimeInterval() + " miesiąc";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        eventTypeViewPart3.setVisibility(LinearLayout.GONE);
                    } else {
                        eventTypeShortInterval = event.getTimeInterval() + " miesiąc";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        String eventTypeShortIntervalPart3 = event.getCustomTimeNumberOfRepeats() + " razy";
                        eventTypeViewPart3.setText(eventTypeShortIntervalPart3);
                    }
                } else if (event.getTimeInterval() >= 2 && event.getTimeInterval() <= 4) {
                    if (event.getItsCustomTimeRepeatsAllTime()) {
                        eventTypeShortInterval = event.getTimeInterval() + " miesiące";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        eventTypeViewPart3.setVisibility(LinearLayout.GONE);
                    } else {
                        eventTypeShortInterval = event.getTimeInterval() + " miesiące";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        String eventTypeShortIntervalPart3 = event.getCustomTimeNumberOfRepeats() + " razy";
                        eventTypeViewPart3.setText(eventTypeShortIntervalPart3);
                    }
                } else {
                    if (event.getItsCustomTimeRepeatsAllTime()) {
                        eventTypeShortInterval = event.getTimeInterval() + " miesięcy";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        eventTypeViewPart3.setVisibility(LinearLayout.GONE);
                    } else {
                        eventTypeShortInterval = event.getTimeInterval() + " miesięcy";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        String eventTypeShortIntervalPart3 = event.getCustomTimeNumberOfRepeats() + " razy";
                        eventTypeViewPart3.setText(eventTypeShortIntervalPart3);
                    }
                }
            }
        } else if (event.getItsOneTimeEvent()) {
            eventTypeViewPart1.setText(ONE_TIME);
            eventTypeViewPart2.setText(event.getOneTimeEventDate().toString(DateTimeFormat.forPattern("dd.MM.YYYY")));
        }
        eventTimeView.setText(event.getEventTime().toString(DateTimeFormat.forPattern("HH:mm")));
        startDateView.setText(event.getStartDate().toString(DateTimeFormat.forPattern("dd.MM.YYYY")));

    }

    // prepare information about event
    @SuppressLint({"ShowToast"})
    private void getInformationAboutEvent() {
        EventModel event = eventsViewModel.findByEventName(eventName);
        if (event == null) {
            Toast.makeText(this, "Nie udało się uzyskać informacji o zdarzeniu", Toast.LENGTH_LONG);
        } else {
            this.event = event;
        }
    }
}
