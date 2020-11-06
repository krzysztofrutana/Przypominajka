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
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.przypominajka.databases.PrzypominajkaDatabaseHelper;
import com.example.przypominajka.R;
import com.example.przypominajka.models.Event;
import com.example.przypominajka.models.Notification;
import com.example.przypominajka.utils.ReminderBroadcast;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

import java.util.List;
import java.util.TimeZone;


public class EventDetailsActivity extends AppCompatActivity {

    PrzypominajkaDatabaseHelper przypominajkaDatabaseHelper;

    String eventName;

    private String eventDiscription;
    private int eventColor;

    private boolean monthInterval;
    private int monthNumberOfRepeats;

    private boolean shortTimeInterval;
    private int shortTimeType; // type (0 - none, 1 - day,2 - week,3 - month)
    private boolean shortTimeRepeatAllTime;
    private int shortTimeNumberOfRepeats;

    private boolean oneTimeEvent;
    private long oneTimeEventDate;

    private long eventTime;

    private int timeIntervalOfRepeat;
    private long startDate;

    private long alarmSet;

    TextView eventNameView;
    TextView eventDiscriptionView;
    Button colorButtonView;
    TextView eventTypeViewPart1;
    TextView eventTypeViewPart2;
    TextView eventTypeViewPart3;
    TextView eventTimeView;
    TextView startDateView;
    TextView alarmSetView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        // setting toolbar with back button and delete event button
        Toolbar toolbar = findViewById(R.id.toolbarEventDetails);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);


        // get name of event
        eventName = getIntent().getStringExtra("EVENT_NAME");

        // set everything necessary views
        przypominajkaDatabaseHelper = new PrzypominajkaDatabaseHelper(this);
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
        switch (item.getItemId()) {
            case R.id.delete_event:
                // deleting event
                // TODO , i don't know why, but this dont work correctly, notify still is being send to BroadcastReceiver class
                List<Notification> notificationList = przypominajkaDatabaseHelper.getNoCompletedNotification(eventName);
                boolean deleteEvent = przypominajkaDatabaseHelper.deleteEvent(eventName);
                przypominajkaDatabaseHelper.deleteNotification(eventName);
                for (Notification notification : notificationList) {

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

                if (deleteEvent) {
                    Toast.makeText(this, "Zdarzenie usunięte pomyślnie", Toast.LENGTH_LONG).show();
                    super.onBackPressed();
                    return true;
                } else {
                    Toast.makeText(this, "Problem przy usuwanie zdarzenia", Toast.LENGTH_LONG).show();
                    return true;
                }
                // back button on toolbar action
            case android.R.id.home:
                super.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // set information in views
    private void setInformationAboutEvent() {

        eventNameView.setText(eventName.replaceAll("_", " "));
        if (eventDiscription.length() == 0) {
            eventDiscriptionView.setText("Brak");
        } else {
            eventDiscriptionView.setText(eventDiscription);
        }
        Drawable unwrappedDrawable = AppCompatResources.getDrawable(EventDetailsActivity.this, R.drawable.button_corner_radius);
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, eventColor);
        colorButtonView.setBackground(wrappedDrawable);
        if (monthInterval) {
            String eventTypeMonthIntervalPart1 = "Przypominaj " + timeIntervalOfRepeat;
            String eventTypeMonthIntervalPart2 = " dnia miesiąca";
            String eventTypeMonthIntervalPart3;
            if (monthNumberOfRepeats == 1) {
                eventTypeMonthIntervalPart3 = "jeden raz";
            } else if (monthNumberOfRepeats >= 2 && monthNumberOfRepeats <= 4) {
                eventTypeMonthIntervalPart3 = "przez " + monthNumberOfRepeats + " miesiące";
            } else {
                eventTypeMonthIntervalPart3 = "przez " + monthNumberOfRepeats + " miesięcy";
            }

            eventTypeViewPart1.setText(eventTypeMonthIntervalPart1);
            eventTypeViewPart2.setText(eventTypeMonthIntervalPart2);
            eventTypeViewPart3.setText(eventTypeMonthIntervalPart3);
        } else if (shortTimeInterval) {
            String eventTypeShortInterval = "";
            if (shortTimeRepeatAllTime) {
                eventTypeViewPart1.setText("Przypominaj zawsze co");
            } else {
                eventTypeViewPart1.setText("Przypominaj co");
            }
            if (shortTimeType == 1) {
                if (timeIntervalOfRepeat == 1) {
                    if (shortTimeRepeatAllTime) {
                        eventTypeShortInterval = timeIntervalOfRepeat + " dzień";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        eventTypeViewPart3.setVisibility(LinearLayout.GONE);
                    } else {
                        eventTypeShortInterval = timeIntervalOfRepeat + " dzień";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        String eventTypeShortIntervalPart3 = shortTimeNumberOfRepeats + " razy";
                        eventTypeViewPart3.setText(eventTypeShortIntervalPart3);
                    }


                } else {
                    if (shortTimeRepeatAllTime) {
                        eventTypeShortInterval = timeIntervalOfRepeat + " dni";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        eventTypeViewPart3.setVisibility(LinearLayout.GONE);
                    } else {
                        eventTypeShortInterval = timeIntervalOfRepeat + " dni";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        String eventTypeShortIntervalPart3 = shortTimeNumberOfRepeats + " razy";
                        eventTypeViewPart3.setText(eventTypeShortIntervalPart3);
                    }

                }
            } else if (shortTimeType == 2) {
                if (timeIntervalOfRepeat == 1) {
                    if (shortTimeRepeatAllTime) {
                        eventTypeShortInterval = timeIntervalOfRepeat + " tydzień";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        eventTypeViewPart3.setVisibility(LinearLayout.GONE);
                    } else {
                        eventTypeShortInterval = timeIntervalOfRepeat + " tydzień";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        String eventTypeShortIntervalPart3 = shortTimeNumberOfRepeats + " razy";
                        eventTypeViewPart3.setText(eventTypeShortIntervalPart3);
                    }
                } else if (timeIntervalOfRepeat >= 2 && timeIntervalOfRepeat <= 4) {
                    if (shortTimeRepeatAllTime) {
                        eventTypeShortInterval = timeIntervalOfRepeat + " tygodnie";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        eventTypeViewPart3.setVisibility(LinearLayout.GONE);
                    } else {
                        eventTypeShortInterval = timeIntervalOfRepeat + " tygodnie";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        String eventTypeShortIntervalPart3 = shortTimeNumberOfRepeats + " razy";
                        eventTypeViewPart3.setText(eventTypeShortIntervalPart3);
                    }
                } else {
                    if (shortTimeRepeatAllTime) {
                        eventTypeShortInterval = timeIntervalOfRepeat + " tygodnii";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        eventTypeViewPart3.setVisibility(LinearLayout.GONE);
                    } else {
                        eventTypeShortInterval = timeIntervalOfRepeat + " tygodnii";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        String eventTypeShortIntervalPart3 = shortTimeNumberOfRepeats + " razy";
                        eventTypeViewPart3.setText(eventTypeShortIntervalPart3);
                    }
                }
            } else if (shortTimeType == 3) {
                if (timeIntervalOfRepeat == 1) {
                    if (shortTimeRepeatAllTime) {
                        eventTypeShortInterval = timeIntervalOfRepeat + " miesiąc";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        eventTypeViewPart3.setVisibility(LinearLayout.GONE);
                    } else {
                        eventTypeShortInterval = timeIntervalOfRepeat + " miesiąc";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        String eventTypeShortIntervalPart3 = shortTimeNumberOfRepeats + " razy";
                        eventTypeViewPart3.setText(eventTypeShortIntervalPart3);
                    }
                } else if (timeIntervalOfRepeat >= 2 && timeIntervalOfRepeat <= 4) {
                    if (shortTimeRepeatAllTime) {
                        eventTypeShortInterval = timeIntervalOfRepeat + " miesiące";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        eventTypeViewPart3.setVisibility(LinearLayout.GONE);
                    } else {
                        eventTypeShortInterval = timeIntervalOfRepeat + " miesiące";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        String eventTypeShortIntervalPart3 = shortTimeNumberOfRepeats + " razy";
                        eventTypeViewPart3.setText(eventTypeShortIntervalPart3);
                    }
                } else {
                    if (shortTimeRepeatAllTime) {
                        eventTypeShortInterval = timeIntervalOfRepeat + " miesięcy";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        eventTypeViewPart3.setVisibility(LinearLayout.GONE);
                    } else {
                        eventTypeShortInterval = timeIntervalOfRepeat + " miesięcy";
                        eventTypeViewPart2.setText(eventTypeShortInterval);
                        String eventTypeShortIntervalPart3 = shortTimeNumberOfRepeats + " razy";
                        eventTypeViewPart3.setText(eventTypeShortIntervalPart3);
                    }
                }
            }
        } else if (oneTimeEvent) {
            eventTypeViewPart1.setText("Jednorazowo");
            eventTypeViewPart2.setText(new LocalDate(oneTimeEventDate).toString(DateTimeFormat.forPattern("dd.MM.YYYY")));
        }
        eventTimeView.setText(new LocalTime(eventTime, DateTimeZone.forID("UTC")).toString(DateTimeFormat.forPattern("HH:mm")));
        startDateView.setText(new LocalDate(startDate).toString(DateTimeFormat.forPattern("dd.MM.YYYY")));


    }

    // prepare information about event
    @SuppressLint({"ShowToast"})
    private void getInformationAboutEvent() {
        Event event = przypominajkaDatabaseHelper.getEvent(eventName);
        if (event == null) {
            Toast.makeText(this, "Nie udało się uzyskać informacji o zdarzeniu", Toast.LENGTH_LONG);
        }
        eventDiscription = event.getEventDiscription();
        eventColor = event.getEventColor();
        monthInterval = event.getItsMonthInterval();
        monthNumberOfRepeats = event.getMonthNumberOfRepeats();
        shortTimeInterval = event.getItCustomTimeInterval();
        shortTimeType = event.getCustomTimeType();
        shortTimeRepeatAllTime = event.getItsCustomTimeRepeatsAllTime();
        shortTimeNumberOfRepeats = event.getCustomTimeNumberOfRepeats();
        oneTimeEvent = event.getItsOneTimeEvent();
        oneTimeEventDate = event.getOneTimeEventDateInMillis();
        timeIntervalOfRepeat = event.getTimeInterval();
        if (event.getEventTimeDefault()) {
            eventTime = przypominajkaDatabaseHelper.getDefaultTime();
        } else {
            eventTime = event.getEventTimeInMillis();
        }
        startDate = event.getStartDateInMillis();
    }
}
