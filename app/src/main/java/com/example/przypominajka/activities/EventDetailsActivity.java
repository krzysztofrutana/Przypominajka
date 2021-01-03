package com.example.przypominajka.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.przypominajka.R;
import com.example.przypominajka.databases.PrzypominajkaDatabaseHelper;
import com.example.przypominajka.databases.entities.EventModel;
import com.example.przypominajka.databases.entities.NotificationModel;
import com.example.przypominajka.databases.repositories.SettingsRepository;
import com.example.przypominajka.utils.MyPrzypominajkaApp;
import com.example.przypominajka.broadcasts.ReminderBroadcast;
import com.example.przypominajka.databases.viewModels.EventsViewModel;
import com.example.przypominajka.databases.viewModels.NotificationViewModel;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import yuku.ambilwarna.AmbilWarnaDialog;


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
    private TextView eventTypeView;
    private TextView eventTimeView;
    private TextView startDateView;
    private TextView nextDateView;

    private final NotificationViewModel notificationViewModel = new NotificationViewModel(getApplication());
    private final EventsViewModel eventsViewModel = new EventsViewModel(getApplication());

    private int previousColor;

    private boolean isEdited = false;

    private DatePickerDialog.OnDateSetListener dateSetListener;

    private LocalDate newDateForMoveEvent;

    private static final String TAG = "EventDetailsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        // setting toolbar with back button and delete event button
        Toolbar toolbar = findViewById(R.id.toolbar_event_details);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);


        // get name of event
        eventName = getIntent().getStringExtra("EVENT_NAME");

        // set everything necessary views
        eventNameView = findViewById(R.id.label_event_name_text);
        eventDiscriptionView = findViewById(R.id.label_event_discription_text);
        colorButtonView = findViewById(R.id.button_event_color);
        eventTypeView = findViewById(R.id.label_event_type_text);
        eventTimeView = findViewById(R.id.label_event_time_text);
        startDateView = findViewById(R.id.label_event_start_date_text);
        nextDateView = findViewById(R.id.label_next_event_day_text);

        Button changeColorButton = findViewById(R.id.button_change_color);
        changeColorButton.setOnClickListener(v -> {
            AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(EventDetailsActivity.this, event.getEventColor(), new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onCancel(AmbilWarnaDialog dialog) {
                }

                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    previousColor = event.getEventColor();
                    event.eventColor = color;
                    if (previousColor != event.getEventColor()) {
                        isEdited = true;
                        Drawable unwrappedDrawable = AppCompatResources.getDrawable(EventDetailsActivity.this, R.drawable.button_corner_radius_add_new_activity);
                        assert unwrappedDrawable != null;
                        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
                        DrawableCompat.setTint(wrappedDrawable, event.getEventColor());
                        colorButtonView.setBackground(wrappedDrawable);
                    }
                }
            });
            colorPicker.show();
        });

        Button changeTimeButton = findViewById(R.id.button_change_time);
        changeTimeButton.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog;
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            // Create a new instance of c
            timePickerDialog = new TimePickerDialog(EventDetailsActivity.this, (timePicker, selectedHour, selectedMinute) -> {
                String minuteStringMonthSection;
                if (selectedMinute < 10) {
                    minuteStringMonthSection = "0" + selectedMinute;
                } else {
                    minuteStringMonthSection = String.valueOf(selectedMinute);
                }
                String timeStr = selectedHour + ":" + minuteStringMonthSection;
                eventTimeView.setText(timeStr);
                eventTimeView.setTextSize(18);
                LocalTime time = new LocalTime(selectedHour, selectedMinute);
                time = new LocalTime(time, DateTimeZone.forID("UCT"));
                event.eventTime = time.getMillisOfDay();
                if (event.getEventTimeDefault()) {
                    SettingsRepository settingsRepository = new SettingsRepository(MyPrzypominajkaApp.get());
                    if (event.getEventTimeInMillis() != settingsRepository.getDefaultTime()) {
                        event.itsEventDefaultTime = false;
                    }
                } else {
                    SettingsRepository settingsRepository = new SettingsRepository(MyPrzypominajkaApp.get());
                    if (event.getEventTimeInMillis() == settingsRepository.getDefaultTime()) {
                        event.itsEventDefaultTime = true;
                    }
                }
                isEdited = true;
            }, hour, minute, DateFormat.is24HourFormat(EventDetailsActivity.this));//Yes 24 hour time
            timePickerDialog.setTitle("Wybierz godzinę");
            timePickerDialog.show();
        });

        Button moveEventButton = findViewById(R.id.button_move_event);
        moveEventButton.setOnClickListener(v -> {

            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(EventDetailsActivity.this,
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    dateSetListener, year, month, day);
            Objects.requireNonNull(datePickerDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            datePickerDialog.show();

        });

        dateSetListener = (view, year, month, dayOfMonth) -> {
            month = month + 1;
            String choosenData = dayOfMonth + "." + month + "." + year;
            newDateForMoveEvent = LocalDate.parse(choosenData, DateTimeFormat.forPattern("dd.MM.YYYY"));
            PrzypominajkaDatabaseHelper.moveEventInTime(eventName, new LocalDate(PrzypominajkaDatabaseHelper.getNextDayOfEvent(eventName)), newDateForMoveEvent);
            nextDateView.setText(getNextDayDateOfEvent());
        };

        Button endEventButton = findViewById(R.id.button_end_event);
        endEventButton.setOnClickListener(v -> {
            PrzypominajkaDatabaseHelper.deleteAllDaysFromDate(eventName, LocalDate.now());
            nextDateView.setText(getNextDayDateOfEvent());
        });

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

    @Override
    protected void onDestroy() {
        // update event in events table if was changed
        if (isEdited)
            eventsViewModel.updateEvent(event);
        super.onDestroy();
    }

    @SuppressLint("ShowToast")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete_event) {
            // deleting event
            try {
                // use % symbol with LIKE in SQL query to find all row with event name in text
                String nameToSearch = "%" + eventName + "%";
                List<NotificationModel> notificationList = notificationViewModel.getNoCompletedNotificationList(nameToSearch);

                int result = eventsViewModel.deleteEvent(eventsViewModel.findByEventName(eventName));

                if (result > 0) {
                    PrzypominajkaDatabaseHelper.deleteEvent(eventName);
                    assert notificationList != null;
                    for (NotificationModel notification : notificationList) {
                        // notification.getNotificationID() < 0 means its notification with default time, this notification may have more than one event in text
                        if (notification.getNotificationID() < 0) {
                            int indexOfEventName = notification.getNotificationName().indexOf(eventName); // find index of event name text in notification text
                            // if > 0 so not at the beginning of the text
                            if (indexOfEventName > 0) {
                                String textToRemove = ", " + eventName; // if not at the beginning so must have ', ' before.
                                notification.notificationEventName = notification.notificationEventName.replace(textToRemove, "");
                                notificationViewModel.updateNotification(notification); // update notification with changed notification text
                            } else {
                                // if length of notification is greater than event name, so have more than one event in text
                                if (notification.getNotificationName().length() > eventName.length()) {
                                    String textToRemove = eventName + ", "; // if it at the beginning of text or in the middle must have ', ' after name
                                    notification.notificationEventName = notification.notificationEventName.replace(textToRemove, "");
                                    notificationViewModel.updateNotification(notification);
                                    // if event name is on the end of notification text
                                } else if (notification.getNotificationName().endsWith(eventName)) {
                                    notification.notificationEventName = notification.notificationEventName.replace(eventName, "");
                                    notificationViewModel.updateNotification(notification);
                                    // if notification text its event name
                                } else if (notification.getNotificationName().length() == eventName.length()) {

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

                                    notificationViewModel.delete(notification);
                                }
                            }
                        } else {
                            // if notification id > 0, so it is event with custom time, notification for this event is created individually for each event
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
                    }
                    Toast.makeText(this, "Zdarzenie usunięte pomyślnie", Toast.LENGTH_LONG).show();
                    this.finish();
                    return true;
                } else {
                    Toast.makeText(this, "Problem przy usuwanie zdarzenia", Toast.LENGTH_LONG).show();
                    Log.w(TAG, "Delete Event: Problem przy usuwaniu zdarzenia");
                    return false;
                }
            } catch (Exception e) {
                Toast.makeText(this, "Problem przy usuwanie zdarzenia", Toast.LENGTH_LONG).show();
                Log.w(TAG, "Delete Event: Problem przy usuwanie zdarzenia " + e.getMessage());
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
            String eventTypeMonthText;
            if (event.getMonthNumberOfRepeats() == 1) {
                eventTypeMonthText = String.format("Przypominaj %s dnia miesiąca jednorazowo", event.getTimeInterval());
            } else if (event.getMonthNumberOfRepeats() >= 2 && event.getMonthNumberOfRepeats() <= 4) {
                eventTypeMonthText = String.format("Przypominaj %s dnia miesiąca przez %2s miesiące", event.getTimeInterval(), event.getMonthNumberOfRepeats());
            } else {
                eventTypeMonthText = String.format("Przypominaj %s dnia miesiąca przez %2s miesięcy", event.getTimeInterval(), event.getMonthNumberOfRepeats());
            }
            eventTypeView.setText(eventTypeMonthText);
        } else if (event.getItCustomTimeInterval()) {
            String eventTypeCustomTimeInterval;
            if (event.getItsCustomTimeRepeatsAllTime()) {
                eventTypeCustomTimeInterval = ALWAYS_REMIND_WHAT;
            } else {
                eventTypeCustomTimeInterval = REMIND_WHAT;
            }
            if (event.getCustomTimeType() == 1) {
                if (event.getTimeInterval() == 1) {
                    if (event.getItsCustomTimeRepeatsAllTime()) {
                        eventTypeCustomTimeInterval = String.format("%s %2s dzień", eventTypeCustomTimeInterval, event.getTimeInterval());
                    } else {
                        eventTypeCustomTimeInterval = String.format("%s %2s dzień %3s razy", eventTypeCustomTimeInterval, event.getTimeInterval(), event.getCustomTimeNumberOfRepeats());
                    }
                } else {
                    if (event.getItsCustomTimeRepeatsAllTime()) {
                        eventTypeCustomTimeInterval = String.format("%s %2s dni", eventTypeCustomTimeInterval, event.getTimeInterval());
                    } else {
                        eventTypeCustomTimeInterval = String.format("%s %2s dni %3s razy", eventTypeCustomTimeInterval, event.getTimeInterval(), event.getCustomTimeNumberOfRepeats());
                    }
                }
            } else if (event.getCustomTimeType() == 2) {
                if (event.getTimeInterval() == 1) {
                    if (event.getItsCustomTimeRepeatsAllTime()) {
                        eventTypeCustomTimeInterval = String.format("%s %2s tydzień", eventTypeCustomTimeInterval, event.getTimeInterval());
                    } else {
                        eventTypeCustomTimeInterval = String.format("%s %2s tydzień %3s razy", eventTypeCustomTimeInterval, event.getTimeInterval(), event.getCustomTimeNumberOfRepeats());
                    }
                } else if (event.getTimeInterval() >= 2 && event.getTimeInterval() <= 4) {
                    if (event.getItsCustomTimeRepeatsAllTime()) {
                        eventTypeCustomTimeInterval = String.format("%s %2s tygodnie", eventTypeCustomTimeInterval, event.getTimeInterval());
                    } else {
                        eventTypeCustomTimeInterval = String.format("%s %2s tygodnie %3s razy", eventTypeCustomTimeInterval, event.getTimeInterval(), event.getCustomTimeNumberOfRepeats());
                    }
                } else {
                    if (event.getItsCustomTimeRepeatsAllTime()) {
                        eventTypeCustomTimeInterval = String.format("%s %2s tygodnii", eventTypeCustomTimeInterval, event.getTimeInterval());
                    } else {
                        eventTypeCustomTimeInterval = String.format("%s %2s tygodnii %3s razy", eventTypeCustomTimeInterval, event.getTimeInterval(), event.getCustomTimeNumberOfRepeats());
                    }
                }
            } else if (event.getCustomTimeType() == 3) {
                if (event.getTimeInterval() == 1) {
                    if (event.getItsCustomTimeRepeatsAllTime()) {
                        eventTypeCustomTimeInterval = String.format("%s %2s miesiąc", eventTypeCustomTimeInterval, event.getTimeInterval());
                    } else {
                        eventTypeCustomTimeInterval = String.format("%s %2s miesiąc %3s razy", eventTypeCustomTimeInterval, event.getTimeInterval(), event.getCustomTimeNumberOfRepeats());
                    }
                } else if (event.getTimeInterval() >= 2 && event.getTimeInterval() <= 4) {
                    if (event.getItsCustomTimeRepeatsAllTime()) {
                        eventTypeCustomTimeInterval = String.format("%s %2s miesiące", eventTypeCustomTimeInterval, event.getTimeInterval());
                    } else {
                        eventTypeCustomTimeInterval = String.format("%s %2s miesiące %3s razy", eventTypeCustomTimeInterval, event.getTimeInterval(), event.getCustomTimeNumberOfRepeats());
                    }
                } else {
                    if (event.getItsCustomTimeRepeatsAllTime()) {
                        eventTypeCustomTimeInterval = String.format("%s %2s miesięcy", eventTypeCustomTimeInterval, event.getTimeInterval());
                    } else {
                        eventTypeCustomTimeInterval = String.format("%s %2s miesięcy %3s razy", eventTypeCustomTimeInterval, event.getTimeInterval(), event.getCustomTimeNumberOfRepeats());
                    }
                }
            }
            eventTypeView.setText(eventTypeCustomTimeInterval);
        } else if (event.getItsOneTimeEvent()) {
            String eventTypeOneTime = String.format("%s %2s", ONE_TIME, event.getOneTimeEventDate().toString(DateTimeFormat.forPattern("dd.MM.YYYY")));
            eventTypeView.setText(eventTypeOneTime);
        }
        eventTimeView.setText(event.getEventTime().toString(DateTimeFormat.forPattern("HH:mm")));
        startDateView.setText(event.getStartDate().toString(DateTimeFormat.forPattern("dd.MM.YYYY")));
        nextDateView.setText(getNextDayDateOfEvent());

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

    private String getNextDayDateOfEvent() {
        long nextDayDate;
        nextDayDate = PrzypominajkaDatabaseHelper.getNextDayOfEvent(eventName);
        if (nextDayDate != 0) {
            return new LocalDate(nextDayDate, DateTimeZone.forID(TimeZone.getDefault().getID())).toString(DateTimeFormat.forPattern("dd.MM.YYYY"));
        } else {
            return "Brak kolejnych przypomnień";
        }
    }
}
