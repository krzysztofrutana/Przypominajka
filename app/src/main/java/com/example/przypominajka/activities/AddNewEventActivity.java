package com.example.przypominajka.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.przypominajka.R;
import com.example.przypominajka.databases.PrzypominajkaDatabaseHelper;
import com.example.przypominajka.databases.entities.EventModel;
import com.example.przypominajka.databases.entities.NotificationModel;
import com.example.przypominajka.utils.MyPrzypominajkaApp;
import com.example.przypominajka.utils.ReminderBroadcast;
import com.example.przypominajka.viewModels.EventsViewModel;
import com.example.przypominajka.viewModels.NotificationViewModel;
import com.example.przypominajka.viewModels.SettingsViewModel;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;

import yuku.ambilwarna.AmbilWarnaDialog;

public class AddNewEventActivity extends AppCompatActivity {

    //Strings
    private static final String EVENT_EXIST = MyPrzypominajkaApp.get().getResources().getString(R.string.ANA_event_exist);
    private static final String[] checkboxtexts = {"dzień", "dni", "tydzień", "tygodnie", "tygodni", "miesiąc", "miesiące", "miesięcy",};

    // Event name section
    private EditText eventNameField;
    private TextView eventNameWarningHintField;

    // Event discription section
    private EditText eventDiscriptionField;

    // Event color section
    private int eventColorNumber;
    private int defaultColor;

    // timeInterval and start date section
    private int timeIntervalNumber;
    private String startEventDate;
    private TextView displayEventDateField;
    private DatePickerDialog.OnDateSetListener dateSetListenerStartEventDate;

    // month interval section
    private boolean itsMonthInterval;
    private EditText monthNumberOfRepeatsField;
    private EditText monthWhichDayField;
    private CheckBox monthDefaultTimeCheckbox;
    private TextView monthSectionTimeOfEventField;
    private TimePickerDialog timePickerDialogitsMonthInterval;

    // short time interval section
    private boolean itsCustomTimeInterval;
    private int customTimeType; // type (0 - none, 1 - day,2 - week,3 - month)
    private CheckBox customTimeRepeatAllTimeChechbox;
    private EditText customTimeNumberOfRepeatsField;
    private EditText customTimeIntervalField;
    private CheckBox customTimeDayChechbox;
    private CheckBox customTimeWeekChechbox;
    private CheckBox customTimeMonthChechbox;
    private CheckBox customTimeDefaultTimeCheckbox;
    private TextView customTimeTimeOfEventField;
    private TimePickerDialog timePickerDialogCustomTime;


    // one time interval section
    private boolean itsOneTimeEvent;
    private TextView oneTimeEventDateField;
    private String oneTimeDate;
    private CheckBox oneTimeDefaultTimeCheckbox;
    private TextView oneTimeEventTimeField;
    private DatePickerDialog.OnDateSetListener dateSetListenerOneTime;
    private TimePickerDialog timePickerDialogOneTime;

    LocalDate startDateLocalData;

    private boolean itsEventDefaultTime;

    //View models
    private SettingsViewModel settingsViewModel;
    private EventsViewModel eventsViewModel;
    private NotificationViewModel notificationViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new);
        // show toolbar
        Toolbar toolbar = findViewById(R.id.toolbarAddNewActivity);
        setSupportActionBar(toolbar);
        // and back button
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        eventsViewModel = new ViewModelProvider(this).get(EventsViewModel.class);
        notificationViewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
        // event name
        eventNameSectionProperties();
        ///////////////////////////////////////////////////////////////////////////////////////////


        // event discription
        eventDiscriptionField = findViewById(R.id.editTextEventDiscription);
        ////////////////////////////////////////////////////////////////////////////////////////////

        // event color
        chooseColorButtonProperties();
        ////////////////////////////////////////////////////////////////////////////////////////////

        // radio group
        radioGropuButtonProperties();
        ////////////////////////////////////////////////////////////////////////////////////////////

        // month interval section
        monthIntervalSectionProperties();
        ////////////////////////////////////////////////////////////////////////////////////////////

        // custom time section
        customTimeSectionProperties();
        ////////////////////////////////////////////////////////////////////////////////////////////

        // one time section
        oneTimeSectionProperties();
        ////////////////////////////////////////////////////////////////////////////////////////////

        // set start date
        startDateSectionProperties();
        ////////////////////////////////////////////////////////////////////////////////////////////

    }

    // back to previous window when back button on toolbar is clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void eventNameSectionProperties() {
        eventNameField = findViewById(R.id.editTextEventName);
        eventNameWarningHintField = findViewById(R.id.text_hint);


        final Context context = this;
        eventNameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                EventModel event = eventsViewModel.findByEventName(s.toString().replaceAll(" ", "_"));
                if (event != null) {
                    ColorStateList colorStateList = ColorStateList.valueOf(Color.RED);
                    eventNameField.setTextColor(Color.RED);
                    eventNameField.setLinkTextColor(Color.RED);
                    eventNameField.setBackgroundTintList(colorStateList);
                    eventNameWarningHintField.setText(EVENT_EXIST);
                    eventNameWarningHintField.setTextColor(Color.RED);
                } else {
                    ColorStateList colorStateList = ContextCompat.getColorStateList(context, R.color.colorAccent);
                    eventNameField.setBackgroundTintList(colorStateList);
                    eventNameField.setTextColor(Color.GRAY);
                    eventNameWarningHintField.setText("");
                }
            }
        });
    }

    private void chooseColorButtonProperties() {
        defaultColor = 0xE0E0E0;
        eventColorNumber = defaultColor;

        // reset button background when activity starts working
        Button chooseColorButton = findViewById(R.id.buttonChooseColor);
        Drawable unwrappedDrawable = AppCompatResources.getDrawable(AddNewEventActivity.this, R.drawable.button_corner_radius_add_new_activity);
        assert unwrappedDrawable != null;
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, 0xE0E0E0);
        chooseColorButton.setBackground(wrappedDrawable);

    }

    // show color picker dialog to choose color of event
    public void buttonChooseColor_onClick(final View view) {
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(AddNewEventActivity.this, eventColorNumber, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                defaultColor = color;
                eventColorNumber = color;
                Drawable unwrappedDrawable = AppCompatResources.getDrawable(AddNewEventActivity.this, R.drawable.button_corner_radius_add_new_activity);
                assert unwrappedDrawable != null;
                Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
                DrawableCompat.setTint(wrappedDrawable, eventColorNumber);
                view.setBackground(wrappedDrawable);
                Button button = findViewById(R.id.buttonChooseColor);
                button.setText("");

            }
        });
        colorPicker.show();
    }

    private void radioGropuButtonProperties() {

        // set radio buttons function
        RadioGroup rg = findViewById(R.id.radioGroupAddNewEvent);
        final LinearLayout linearLayoutMonthSection = findViewById(R.id.linearLayoutMonthSection);
        final LinearLayout linearLayoutCustomSection = findViewById(R.id.linearLayoutCustomTimeSection);
        final LinearLayout linearLayoutOneTimeSection = findViewById(R.id.linearLayoutOneTimeSection);
        linearLayoutOneTimeSection.setVisibility(LinearLayout.GONE);
        linearLayoutCustomSection.setVisibility(LinearLayout.GONE);
        linearLayoutMonthSection.setVisibility(LinearLayout.GONE);
        rg.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButtonCurrentDay) {
                linearLayoutMonthSection.setVisibility(LinearLayout.VISIBLE);
                linearLayoutCustomSection.setVisibility(LinearLayout.GONE);
                linearLayoutOneTimeSection.setVisibility(LinearLayout.GONE);
                itsMonthInterval = true;
                itsCustomTimeInterval = false;
                itsOneTimeEvent = false;
                monthDefaultTimeCheckbox.setChecked(true);
                customTimeDefaultTimeCheckbox.setChecked(false);
                oneTimeDefaultTimeCheckbox.setChecked(false);
            } else if (checkedId == R.id.radioButtonJumpDay) {
                linearLayoutCustomSection.setVisibility(LinearLayout.VISIBLE);
                linearLayoutMonthSection.setVisibility(LinearLayout.GONE);
                linearLayoutOneTimeSection.setVisibility(LinearLayout.GONE);
                itsMonthInterval = false;
                itsCustomTimeInterval = true;
                itsOneTimeEvent = false;
                monthDefaultTimeCheckbox.setChecked(false);
                customTimeDefaultTimeCheckbox.setChecked(true);
                oneTimeDefaultTimeCheckbox.setChecked(false);
            } else if (checkedId == R.id.radioButtonOneTime) {
                linearLayoutCustomSection.setVisibility(LinearLayout.GONE);
                linearLayoutMonthSection.setVisibility(LinearLayout.GONE);
                linearLayoutOneTimeSection.setVisibility(LinearLayout.VISIBLE);
                itsMonthInterval = false;
                itsCustomTimeInterval = false;
                itsOneTimeEvent = true;
                monthDefaultTimeCheckbox.setChecked(false);
                customTimeDefaultTimeCheckbox.setChecked(false);
                oneTimeDefaultTimeCheckbox.setChecked(true);
            }
        });

    }

    private void monthIntervalSectionProperties() {
        monthNumberOfRepeatsField = findViewById(R.id.editMonthSectionNumerOfRepeats);
        monthWhichDayField = findViewById(R.id.editMonthSectionWhichDay);

        monthDefaultTimeCheckbox = findViewById(R.id.checkboxMonthSectionlDefaultTime);
        monthSectionTimeOfEventField = findViewById(R.id.textMonthSectionTimeOfEvent);
        monthDefaultTimeCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                monthSectionTimeOfEventField.setVisibility(LinearLayout.GONE);
            } else {
                monthSectionTimeOfEventField.setVisibility(LinearLayout.VISIBLE);
            }
        });

        monthSectionTimeOfEventField.setOnClickListener(view -> {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            // Create a new instance of c
            timePickerDialogitsMonthInterval = new TimePickerDialog(AddNewEventActivity.this, (timePicker, selectedHour, selectedMinute) -> {
                String minuteStringMonthSection;
                if (selectedMinute < 10) {
                    minuteStringMonthSection = "0" + selectedMinute;
                } else {
                    minuteStringMonthSection = String.valueOf(selectedMinute);
                }
                String time = selectedHour + ":" + minuteStringMonthSection;
                monthSectionTimeOfEventField.setText(time);
                monthSectionTimeOfEventField.setTextSize(18);
                monthDefaultTimeCheckbox.forceLayout();
            }, hour, minute, DateFormat.is24HourFormat(AddNewEventActivity.this));//Yes 24 hour time
            timePickerDialogitsMonthInterval.setTitle("Wybierz godzinę");
            timePickerDialogitsMonthInterval.show();
        });

    }

    private void customTimeSectionProperties() {

        customTimeRepeatAllTimeChechbox = findViewById(R.id.checkboxCustomTimeRepeatsAllways);
        customTimeNumberOfRepeatsField = findViewById(R.id.editCustomTimeNumberOfRepeats);
        customTimeIntervalField = findViewById(R.id.editTextCustomTimeInterval);

        customTimeDayChechbox = findViewById(R.id.checkBoxCustomTimeDay);
        customTimeWeekChechbox = findViewById(R.id.checkBoxCustomTimeWeek);
        customTimeMonthChechbox = findViewById(R.id.checkBoxCustomTimeMonth);
        customTimeDayChechbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                customTimeWeekChechbox.setChecked(false);
                customTimeMonthChechbox.setChecked(false);
            }
        });
        customTimeWeekChechbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                customTimeDayChechbox.setChecked(false);
                customTimeMonthChechbox.setChecked(false);
            }
        });
        customTimeMonthChechbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                customTimeDayChechbox.setChecked(false);
                customTimeWeekChechbox.setChecked(false);
            }
        });

        customTimeIntervalField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals(String.valueOf(1))) {
                    customTimeDayChechbox.setText(checkboxtexts[0]);
                    customTimeWeekChechbox.setText(checkboxtexts[2]);
                    customTimeMonthChechbox.setText(checkboxtexts[5]);
                } else if (s.toString().equals(String.valueOf(2)) && s.toString().equals(String.valueOf(4))) {
                    customTimeDayChechbox.setText(checkboxtexts[1]);
                    customTimeWeekChechbox.setText(checkboxtexts[3]);
                    customTimeMonthChechbox.setText(checkboxtexts[6]);
                } else {
                    customTimeDayChechbox.setText(checkboxtexts[1]);
                    customTimeWeekChechbox.setText(checkboxtexts[4]);
                    customTimeMonthChechbox.setText(checkboxtexts[7]);
                }
            }
        });

        customTimeRepeatAllTimeChechbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                customTimeNumberOfRepeatsField.setVisibility(LinearLayout.GONE);
            } else {
                customTimeNumberOfRepeatsField.setVisibility(LinearLayout.VISIBLE);
            }
        });
        customTimeDefaultTimeCheckbox = findViewById(R.id.checkboxCustomTimeDefaultTime);
        customTimeTimeOfEventField = findViewById(R.id.textCustomTimeEventTime);
        customTimeDefaultTimeCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                customTimeTimeOfEventField.setVisibility(LinearLayout.GONE);
            } else {
                customTimeTimeOfEventField.setVisibility(LinearLayout.VISIBLE);
            }
        });

        customTimeTimeOfEventField.setOnClickListener(view -> {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            // Create a new instance of c
            timePickerDialogCustomTime = new TimePickerDialog(AddNewEventActivity.this, (timePicker, selectedHour, selectedMinute) -> {
                String minuteCustomTimeSection;
                if (selectedMinute < 10) {
                    minuteCustomTimeSection = "0" + selectedMinute;
                } else {
                    minuteCustomTimeSection = String.valueOf(selectedMinute);
                }
                String time = selectedHour + ":" + minuteCustomTimeSection;
                customTimeTimeOfEventField.setText(time);
                customTimeTimeOfEventField.setTextSize(18);
                customTimeDefaultTimeCheckbox.forceLayout(); // update view after resize textView


            }, hour, minute, DateFormat.is24HourFormat(AddNewEventActivity.this)); //Yes 24 hour time
            timePickerDialogCustomTime.setTitle("Wybierz godzinę");
            timePickerDialogCustomTime.show();
        });
    }

    private void oneTimeSectionProperties() {

        oneTimeDate = "";

        oneTimeDefaultTimeCheckbox = findViewById(R.id.checkboxOneTimeDefaultTime);
        oneTimeEventTimeField = findViewById(R.id.textOneTimeTimeOfEvent);
        oneTimeDefaultTimeCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                oneTimeEventTimeField.setVisibility(LinearLayout.GONE);
            } else {
                oneTimeEventTimeField.setVisibility(LinearLayout.VISIBLE);
            }
        });

        oneTimeEventDateField = findViewById(R.id.textViewOneTimeSectionSetDate);
        // show date picker dialog to set date
        oneTimeEventDateField.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialogOneTime = new DatePickerDialog(AddNewEventActivity.this,
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    dateSetListenerOneTime, year, month, day);
            Objects.requireNonNull(datePickerDialogOneTime.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            datePickerDialogOneTime.show();
        });

        dateSetListenerOneTime = (view, year, month, dayOfMonth) -> {
            month = month + 1;
            String choosenData = dayOfMonth + "." + month + "." + year;
            oneTimeEventDateField.setText(choosenData);
            oneTimeDate = choosenData;
        };

        oneTimeEventTimeField.setOnClickListener(view -> {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            // Create a new instance of c
            timePickerDialogOneTime = new TimePickerDialog(AddNewEventActivity.this, (timePicker, selectedHour, selectedMinute) -> {
                String minuteOneTimeSection;
                if (selectedMinute < 10) {
                    minuteOneTimeSection = "0" + selectedMinute;
                } else {
                    minuteOneTimeSection = String.valueOf(selectedMinute);
                }
                String time = selectedHour + ":" + minuteOneTimeSection;
                oneTimeEventTimeField.setText(time);
                oneTimeEventTimeField.setTextSize(18);
                oneTimeDefaultTimeCheckbox.forceLayout();
            }, hour, minute, DateFormat.is24HourFormat(AddNewEventActivity.this));//Yes 24 hour time
            timePickerDialogOneTime.setTitle("Wybierz godzinę");
            timePickerDialogOneTime.show();
        });
    }

    private void startDateSectionProperties() {

        // show date picker dialog to set date
        displayEventDateField = findViewById(R.id.textViewSetStartDate);
        displayEventDateField.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(AddNewEventActivity.this,
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    dateSetListenerStartEventDate, year, month, day);
            Objects.requireNonNull(datePickerDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            datePickerDialog.show();
        });

        dateSetListenerStartEventDate = (view, year, month, dayOfMonth) -> {
            month = month + 1;
            String choosenData = dayOfMonth + "." + month + "." + year;
            displayEventDateField.setText(choosenData);
            startEventDate = choosenData;
        };
    }


    // get everything field, make new Event object and seve event to database
    // make new AsyncTask class with split saveEvent method to do this with ProgressDialog
    // AsyncTask in API 30 is deprecated so TODO for new API

    // using DateTimeZone as Universal repair bugs with Time Zone when set Alarm on current time
    public void buttonSaveNewEvent(View view) {

        SaveEventTask task = new SaveEventTask(this);
        task.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private final class SaveEventTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog;
        Context context;
        long result = -1;
        EventModel newEvent;
        LocalTime time;

        public SaveEventTask(Context context) {
            this.context = context;
            dialog = new ProgressDialog(context);
        }

        @Override
        protected void onPreExecute() {
            dialog.setTitle("Tworzenie wydarzenia");
            dialog.setMessage("Proszę czekać");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();

            if (eventNameWarningHintField.getText().toString().equals("Wydarzenie już istnieje")) {
                Toast.makeText(context, "Podaj inną nazwę wydarzenia", Toast.LENGTH_LONG).show();
                return;
            }

            startDateLocalData = LocalDate.parse(startEventDate, DateTimeFormat.forPattern("dd.MM.YYYY"));
            LocalDate oneTimeEventDate;
            if (oneTimeDate.equals("")) {
                oneTimeEventDate = null;
            } else {
                oneTimeEventDate = LocalDate.parse(oneTimeDate, DateTimeFormat.forPattern("dd.MM.YYYY"));
            }

            if (itsCustomTimeInterval) {
                timeIntervalNumber = Integer.parseInt(customTimeIntervalField.getText().toString());
            }
            if (itsMonthInterval) {
                timeIntervalNumber = Integer.parseInt(monthWhichDayField.getText().toString());
            }
            if (itsOneTimeEvent) {
                timeIntervalNumber = 0;
            }
            if (customTimeDayChechbox.isChecked()) {
                customTimeType = 1;
            } else if (customTimeWeekChechbox.isChecked()) {
                customTimeType = 2;
            } else if (customTimeMonthChechbox.isChecked()) {
                customTimeType = 3;
            }
            int monthNumberOfRepeats;
            if (monthNumberOfRepeatsField.getText().toString().length() > 0) {
                monthNumberOfRepeats = Integer.parseInt(monthNumberOfRepeatsField.getText().toString());
            } else {
                monthNumberOfRepeats = 0;
            }

            int shortTimeNumberOfRepeats;
            if (customTimeNumberOfRepeatsField.getText().toString().length() > 0) {
                shortTimeNumberOfRepeats = Integer.parseInt(customTimeNumberOfRepeatsField.getText().toString());
            } else {
                shortTimeNumberOfRepeats = 0;
            }


            time = new LocalTime(settingsViewModel.getDefaultTime(), DateTimeZone.forID("UCT"));
            long eventTime = time.getMillisOfDay();
            if (itsMonthInterval) {
                if (monthDefaultTimeCheckbox.isChecked()) {
                    itsEventDefaultTime = true;
                    eventTime = time.getMillisOfDay();
                } else {
                    TextView monthTime = findViewById(R.id.textMonthSectionTimeOfEvent);
                    if (monthTime.getText().toString().equals("wybierz godzinę")) {
                        Toast.makeText(context, "Nie wybrano godziny", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        time = LocalTime.parse(monthTime.getText().toString());
                        time = new LocalTime(time, DateTimeZone.forID("UCT"));
                        eventTime = time.getMillisOfDay();
                    }
                }
            } else if (itsCustomTimeInterval) {
                if (customTimeDefaultTimeCheckbox.isChecked()) {
                    itsEventDefaultTime = true;
                    eventTime = time.getMillisOfDay();
                } else {
                    TextView customTime = findViewById(R.id.textCustomTimeEventTime);
                    if (customTime.getText().toString().equals("wybierz godzinę")) {
                        Toast.makeText(context, "Nie wybrano godziny", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        time = LocalTime.parse(customTime.getText().toString());
                        time = new LocalTime(time, DateTimeZone.forID("UCT"));
                        eventTime = time.getMillisOfDay();
                    }
                }
            } else if (itsOneTimeEvent) {
                if (oneTimeDefaultTimeCheckbox.isChecked()) {
                    itsEventDefaultTime = true;
                    eventTime = time.getMillisOfDay();
                } else {
                    TextView oneTimeTime = findViewById(R.id.textOneTimeTimeOfEvent);
                    if (oneTimeTime.getText().toString().equals("wybierz godzinę")) {
                        Toast.makeText(context, "Nie wybrano godziny", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        time = LocalTime.parse(oneTimeTime.getText().toString());
                        time = new LocalTime(time, DateTimeZone.forID(TimeZone.getDefault().getID()));
                        eventTime = time.getMillisOfDay();
                    }
                }
            }
            boolean checkIfItsCorrect = checkIfNotNull();
            if (!checkIfItsCorrect) {
                return;
            }
            newEvent = new EventModel(eventNameField.getText().toString(),
                    eventDiscriptionField.getText().toString(),
                    eventColorNumber,
                    itsMonthInterval,
                    monthNumberOfRepeats,
                    itsCustomTimeInterval,
                    customTimeType,
                    customTimeRepeatAllTimeChechbox.isChecked(),
                    shortTimeNumberOfRepeats,
                    itsOneTimeEvent,
                    oneTimeEventDate,
                    timeIntervalNumber, startDateLocalData,
                    itsEventDefaultTime,
                    eventTime);

            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                result = eventsViewModel.insertEvent(newEvent);
            } catch (Exception e) {
                Log.d("doInBackground", Objects.requireNonNull(e.getMessage()));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (result != -1) {
                // if event insert was successful set first alarm for nearest date
                DateTime tempEventTime;

                tempEventTime = new DateTime(startDateLocalData.getYear(), startDateLocalData.getMonthOfYear(),
                        startDateLocalData.getDayOfMonth(), time.getHourOfDay(), time.getMinuteOfHour())
                        .withZoneRetainFields(DateTimeZone.forID(TimeZone.getDefault().getID()));

                if (tempEventTime.getMillis() < DateTime.now().getMillis()) {
                    tempEventTime = tempEventTime.plusDays(1);
                }

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                PendingIntent alarmIntent;

                // Event ID from table is a unique ID for Intent and Pending Intent, two event cannot have this same ID
                // so this value is perfect for this
                int year = tempEventTime.getYear();
                String yearShortString = Integer.toString(year).substring(2);
                int yearShort = Integer.parseInt(yearShortString);
                int eventID = eventsViewModel.getEventID(newEvent.getEventName());

                int notifyAndPendingIntentID;
                if (eventID != -1) {
                    notifyAndPendingIntentID = Integer.parseInt(String.valueOf(eventID) +
                            String.valueOf(tempEventTime.getDayOfYear()) + String.valueOf(yearShort));
                    Intent notificationIntent = new Intent(getApplicationContext(), ReminderBroadcast.class);
                    notificationIntent.putExtra("NOTIFY_TEXT", eventNameField.getText().toString());
                    notificationIntent.putExtra("ID", notifyAndPendingIntentID);

                    alarmIntent = PendingIntent.getBroadcast(getApplicationContext(),
                            notifyAndPendingIntentID,
                            notificationIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                } else {
                    Log.d("AddNewEvent", "Problem z pobraniem ID wydarzenia z bazy");
                    return;
                }
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, tempEventTime.getMillis(), alarmIntent);
                PrzypominajkaDatabaseHelper.updateNotificationCreatedColumn(eventNameField.getText().toString(), true,
                        new LocalDate(tempEventTime.getYear(), tempEventTime.getMonthOfYear(), tempEventTime.getDayOfMonth()));

                Log.d("AddNewEvent", "Stworzono powiadomienie dla " + eventNameField.getText().toString() + " o godzinie " + tempEventTime.toString());

                NotificationModel newNotification = new NotificationModel(eventNameField.getText().toString(), notifyAndPendingIntentID,
                        new LocalTime(tempEventTime.getHourOfDay(), tempEventTime.getMinuteOfHour()), tempEventTime.toLocalDate(), false);
                long result = notificationViewModel.insertNotification(newNotification);
                if (result != -1) {
                    Log.d("AddNewEvent", "Dodano powiadomienie dla " + eventNameField.getText().toString() + " " + tempEventTime.toLocalDate().toString()
                            + " " + new LocalTime(tempEventTime.getHourOfDay(), tempEventTime.getMinuteOfHour()).toString());
                    Toast.makeText(context, "Dodawanie zdarzenia powiodło się", Toast.LENGTH_SHORT).show();
                    AddNewEventActivity.this.finish();
                } else {
                    Log.d("AddNewEvent", "Nie udało się dodać informacji o powiadomieniu");
                }
            } else {
                Toast.makeText(context, "Wystąpił problem podczas dodawania zdarzenia", Toast.LENGTH_SHORT).show();
                Log.d("AddNewActivity InsertEvent", "Dodawanie wydarzenia zwróciło " + String.valueOf(result));
            }

            super.onPostExecute(aVoid);
            if (dialog.isShowing())
                dialog.dismiss();
        }
    }

    // method to check everything field to avoid empty or null values
    private boolean checkIfNotNull() {
        if (eventNameField.getText().toString().equals("")) {
            Toast.makeText(this, "Nie podano nazwy", Toast.LENGTH_LONG).show();
            return false;
        } else if (eventColorNumber == 0xE0E0E0) {
            Toast.makeText(this, "Nie wybrano koloru", Toast.LENGTH_LONG).show();
            return false;
        } else if (itsMonthInterval) {
            if (monthNumberOfRepeatsField.getText().toString().equals("")) {
                Toast.makeText(this, "Nie wpisano liczby powtorzeń", Toast.LENGTH_LONG).show();
                return false;
            } else if (monthWhichDayField.getText().toString().equals("")) {
                Toast.makeText(this, "Nie wpisano dnia miesiąca", Toast.LENGTH_LONG).show();
                return false;
            } else if (!monthDefaultTimeCheckbox.isChecked()) {
                if (monthSectionTimeOfEventField.getText().toString().equals("wybierz godzinę")) {
                    Toast.makeText(this, "Nie wybrano godziny", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        } else if (itsCustomTimeInterval) {
            if (!customTimeRepeatAllTimeChechbox.isChecked()) {
                if (customTimeNumberOfRepeatsField.getText().toString().equals("")) {
                    Toast.makeText(this, "Nie wpisano liczby powtorzeń", Toast.LENGTH_LONG).show();
                    return false;
                }
            } else if (customTimeIntervalField.getText().toString().equals("")) {
                Toast.makeText(this, "Nie wpisano liczby dni/tygodni/miesięcy", Toast.LENGTH_LONG).show();
                return false;
            } else if (!customTimeDefaultTimeCheckbox.isChecked()) {
                if (customTimeTimeOfEventField.getText().toString().equals("wybierz godzinę")) {
                    Toast.makeText(this, "Nie wybrano godziny", Toast.LENGTH_LONG).show();
                    return false;
                }
            } else if (customTimeType == 0) {
                Toast.makeText(this, "Nie wybrano typu zdarzenia (dni, tygodni, miesięcy)", Toast.LENGTH_LONG).show();
                return false;
            }
        } else if (itsOneTimeEvent) {
            if (oneTimeEventDateField.getText().toString().equals("")) {
                Toast.makeText(this, "Nie wybrano daty zdarzenia", Toast.LENGTH_LONG).show();
                return false;
            } else if (!oneTimeDefaultTimeCheckbox.isChecked()) {
                if (oneTimeEventTimeField.getText().toString().equals("wybierz godzinę")) {
                    Toast.makeText(this, "Nie wybrano godziny", Toast.LENGTH_LONG).show();
                    return false;
                }
            } else if (startDateLocalData == null) {
                Toast.makeText(this, "Nie wybrano daty rozpoczęcia zdarzenia", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }
}