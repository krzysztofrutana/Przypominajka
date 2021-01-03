package com.example.przypominajka.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.przypominajka.R;
import com.example.przypominajka.databases.PrzypominajkaDatabaseHelper;
import com.example.przypominajka.databases.entities.EventModel;
import com.example.przypominajka.databases.entities.NotificationModel;
import com.example.przypominajka.models.EventModelProperties;
import com.example.przypominajka.utils.MyPrzypominajkaApp;
import com.example.przypominajka.broadcasts.ReminderBroadcast;
import com.example.przypominajka.databases.viewModels.EventsViewModel;
import com.example.przypominajka.databases.viewModels.NotificationViewModel;
import com.example.przypominajka.databases.viewModels.SettingsViewModel;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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


    private Button buttonSaveEvent;

    //View models
    private final SettingsViewModel settingsViewModel = new SettingsViewModel(MyPrzypominajkaApp.get());
    private final EventsViewModel eventsViewModel = new EventsViewModel(MyPrzypominajkaApp.get());
    private final NotificationViewModel notificationViewModel = new NotificationViewModel(MyPrzypominajkaApp.get());

    // protection
    private boolean canBeSave;

    final Context context = this;

    private static final String TAG = "AddNewEventActivity";

    // Replace asyncTask by ExecutorService and custom alertDialog
    public static final ExecutorService addNewEventActivityExecutor =
            Executors.newFixedThreadPool(2);
    AlertDialog dialog;

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

        // protection before save event with wrong name, without set color etc.
        canBeSave = false;

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

        buttonSaveEvent = findViewById(R.id.buttonSaveAndStartEvent);
        buttonSaveEvent.setOnClickListener(v -> {
            if (canBeSave) {
                if (checkIfNotNull()) {
                    addNewEventActivityExecutor.submit(this::saveEvent);
                }
            } else {
                Toast.makeText(getApplicationContext(), "Pola nie są poprawnie wypełnione", Toast.LENGTH_LONG).show();
            }
        });
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

        eventNameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 0) {
                    buttonSaveEvent.setEnabled(false);
                }
                EventModel event = eventsViewModel.findByEventName(s.toString().replaceAll(" ", "_"));
                if (event != null) {
                    setEventNameFieldAndHint(true, EVENT_EXIST);
                    canBeSave = false;
                } else {
                    setEventNameFieldAndHint(false, "");
                    canBeSave = true;
                }
                if (s.toString().length() >= 1)
                    try {
                        Double num = Double.parseDouble(s.toString());
                        setEventNameFieldAndHint(true, "Nazwa nie może być liczbą");
                        canBeSave = false;
                    } catch (NumberFormatException e) {
                        char c = s.toString().charAt(0);
                        boolean isDigit = (c >= '0' && c <= '9');
                        if (isDigit) {
                            setEventNameFieldAndHint(true, "Nazwa nie może zaczynać sie cyfrą");
                            canBeSave = false;
                        } else {
                            setEventNameFieldAndHint(false, "");
                            canBeSave = true;
                        }
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
        final LinearLayout linearLayoutStartDateSeection = findViewById(R.id.LL_start_date);
        linearLayoutOneTimeSection.setVisibility(LinearLayout.GONE);
        linearLayoutCustomSection.setVisibility(LinearLayout.GONE);
        linearLayoutMonthSection.setVisibility(LinearLayout.GONE);
        rg.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButtonCurrentDay) {
                linearLayoutMonthSection.setVisibility(LinearLayout.VISIBLE);
                linearLayoutCustomSection.setVisibility(LinearLayout.GONE);
                linearLayoutOneTimeSection.setVisibility(LinearLayout.GONE);
                linearLayoutStartDateSeection.setVisibility(LinearLayout.VISIBLE);
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
                linearLayoutStartDateSeection.setVisibility(LinearLayout.VISIBLE);
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
                linearLayoutStartDateSeection.setVisibility(LinearLayout.GONE);
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
                customTimeType = 1;
            }
        });
        customTimeWeekChechbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                customTimeDayChechbox.setChecked(false);
                customTimeMonthChechbox.setChecked(false);
                customTimeType = 2;
            }
        });
        customTimeMonthChechbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                customTimeDayChechbox.setChecked(false);
                customTimeWeekChechbox.setChecked(false);
                customTimeType = 3;
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
                } else if (Integer.parseInt(s.toString()) >= 2 && Integer.parseInt(s.toString()) <= 4) {
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


    /**
     * get everything field, make new Event object, seve event to database and create first notification
     */
    @SuppressLint("StaticFieldLeak")
    private void saveEvent() {
        runOnUiThread(() -> dialog = showProgressDialog());


        EventModelProperties eventModelProperties = new EventModelProperties();

        if (itsMonthInterval) {
            eventModelProperties = getEventModelPropertiesForMonthInterval(eventModelProperties);
            if (eventModelProperties == null) {
                showToastMessage("Nie udało się przygotować wydarzenia");
                runOnUiThread(() -> dialog.dismiss());
                return;
            }
        } else if (itsCustomTimeInterval) {
            eventModelProperties = getEventModelPropertiesForCustomTimeInterval(eventModelProperties);
            if (eventModelProperties == null) {
                showToastMessage("Nie udało się przygotować wydarzenia");
                runOnUiThread(() -> dialog.dismiss());
                return;
            }
        } else if (itsOneTimeEvent) {
            eventModelProperties = getEventModelPropertiesForOneTimeEvent(eventModelProperties);
            if (eventModelProperties == null) {
                showToastMessage("Nie udało się przygotować wydarzenia");
                runOnUiThread(() -> dialog.dismiss());
                return;
            }
        }

        EventModel newEvent = new EventModel(eventNameField.getText().toString(),
                eventDiscriptionField.getText().toString(),
                eventColorNumber,
                eventModelProperties.isItsMonthInterval(),
                eventModelProperties.getMonthNumberOfRepeats(),
                eventModelProperties.isItsCustomTimeInterval(),
                customTimeType,
                customTimeRepeatAllTimeChechbox.isChecked(),
                eventModelProperties.getCustomTimeNumberOfRepeats(),
                eventModelProperties.isItsOneTimeEvent(),
                eventModelProperties.getOneTimeDate(),
                eventModelProperties.getTimeInterval(),
                eventModelProperties.getStartDateLocalData(),
                eventModelProperties.isItsEventDefaultTime(),
                eventModelProperties.getEventTimeInMillis());

        long result;
        try {
            result = eventsViewModel.insertEvent(newEvent);
        } catch (Exception e) {
            Log.d(TAG, "AddNewEvent: InsertEvent: " + Objects.requireNonNull(e.getMessage()));
            showToastMessage("Nie udało się dodać wydarzenia do bazy");
            runOnUiThread(() -> dialog.dismiss());
            return;
        }

        if (result != -1) {
            // if event insert was successful set first alarm for next date
            DateTime nearestAlarmDateTime = new DateTime(
                    eventModelProperties.getStartDateLocalData().getYear(),
                    eventModelProperties.getStartDateLocalData().getMonthOfYear(),
                    eventModelProperties.getStartDateLocalData().getDayOfMonth(),
                    eventModelProperties.getEventTIme().getHourOfDay(),
                    eventModelProperties.getEventTIme().getMinuteOfHour())
                    .withZoneRetainFields(DateTimeZone.forID(TimeZone.getDefault().getID()));

            if (nearestAlarmDateTime.getMillis() < DateTime.now().getMillis()) {
                nearestAlarmDateTime = nearestAlarmDateTime.plusDays(1);
            }

            boolean notificationCreated = createNotification(nearestAlarmDateTime, newEvent.getEventName());

            if (notificationCreated) {
                showToastMessage("Dodawanie wydarzenia powiodło się.");
                runOnUiThread(() -> {
                    AddNewEventActivity.this.finish();
                    dialog.dismiss();
                });
            } else {
                eventsViewModel.deleteEvent(newEvent);
                showToastMessage("Wystąpił problem podczas dodawania zdarzenia");
                Log.d(TAG, "AddNewEvent: Dodawanie wydarzenia zwróciło " + result);
                runOnUiThread(() -> dialog.dismiss());
            }
        }
    }

    /**
     * method to check everything field to avoid empty or null values
     *
     * @return false - if something is wrong and show toast message with information,
     * true - if everything is good
     */
    private boolean checkIfNotNull() {
        RadioButton itsMonthIntervalRadioButton = findViewById(R.id.radioButtonCurrentDay);
        RadioButton itsCustomTimeIntervalRadioButton = findViewById(R.id.radioButtonJumpDay);
        RadioButton itsOneTimeEventRadioButton = findViewById(R.id.radioButtonOneTime);

        if (eventNameField.getText().toString().equals("")) {
            showToastMessage("Nie podano nazwy");
            return false;
        } else if (eventColorNumber == 0xE0E0E0) {
            showToastMessage("Nie wybrano koloru");
            return false;
        } else if (itsMonthInterval) {
            if (monthNumberOfRepeatsField.getText().toString().equals("")) {
                showToastMessage("Nie wpisano liczby powtorzeń");
                return false;
            } else if (monthWhichDayField.getText().toString().equals("")) {
                showToastMessage("Nie wpisano dnia miesiąca");
                return false;
            } else if (!monthDefaultTimeCheckbox.isChecked()) {
                if (monthSectionTimeOfEventField.getText().toString().equals("wybierz godzinę")) {
                    showToastMessage("Nie wybrano godziny");
                    return false;
                }
            }
        } else if (itsCustomTimeInterval) {
            if (!customTimeRepeatAllTimeChechbox.isChecked()) {
                if (customTimeNumberOfRepeatsField.getText().toString().equals("")) {
                    showToastMessage("Nie wpisano liczby powtorzeń");
                    return false;
                }
            } else if (customTimeIntervalField.getText().toString().equals("")) {
                showToastMessage("Nie wpisano liczby dni/tygodni/miesięcy");
                return false;
            } else if (!customTimeDefaultTimeCheckbox.isChecked()) {
                if (customTimeTimeOfEventField.getText().toString().equals("wybierz godzinę")) {
                    showToastMessage("Nie wybrano godziny");
                    return false;
                }
            } else if (customTimeType == 0) {
                showToastMessage("Nie wybrano typu zdarzenia (dni, tygodni, miesięcy)");
                return false;
            }
        } else if (itsOneTimeEvent) {
            if (oneTimeEventDateField.getText().toString().equals("")) {
                showToastMessage("Nie wybrano daty zdarzenia");
                return false;
            } else if (!oneTimeDefaultTimeCheckbox.isChecked()) {
                if (oneTimeEventTimeField.getText().toString().equals("wybierz godzinę")) {
                    showToastMessage("Nie wybrano godziny");
                    return false;
                }
            } else if (!itsOneTimeEventRadioButton.isChecked() && startEventDate.equals(context.getString(R.string.click_to_choose))) {
                showToastMessage("Nie wybrano daty rozpoczęcia zdarzenia");
                return false;
            }
        } else if (!itsMonthIntervalRadioButton.isChecked() && !itsCustomTimeIntervalRadioButton.isChecked() && !itsOneTimeEventRadioButton.isChecked()) {
            showToastMessage("Nie wybrano typu wydarzenia");
            return false;
        }
        return true;
    }

    /**
     * Set text field and hint on red color if isWrong is true and show hint text
     *
     * @param isWrong  - value is correct or not
     * @param hintText - text for red hint error field
     */
    private void setEventNameFieldAndHint(boolean isWrong, String hintText) {
        if (isWrong) {
            ColorStateList colorStateList = ColorStateList.valueOf(Color.RED);
            eventNameField.setTextColor(Color.RED);
            eventNameField.setLinkTextColor(Color.RED);
            eventNameField.setBackgroundTintList(colorStateList);
            eventNameWarningHintField.setText(hintText);
            eventNameWarningHintField.setTextColor(Color.RED);
        } else {
            ColorStateList colorStateList = ContextCompat.getColorStateList(getApplicationContext(), R.color.colorAccent);
            eventNameField.setBackgroundTintList(colorStateList);
            eventNameField.setTextColor(Color.GRAY);
            eventNameWarningHintField.setText(hintText);
            buttonSaveEvent.setEnabled(true);
        }
    }

    /**
     * Solution wrote by Kishan Donga from stackoverflow and working great if it's run on UI thread
     *
     * @return custom alert dialog with two text view and indeterminate progress bar
     */
    private AlertDialog showProgressDialog() {

        int llPadding = 30;
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(llPadding, llPadding, llPadding, llPadding);
        ll.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        ll.setLayoutParams(llParam);

        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);
        progressBar.setPadding(0, 0, llPadding, 0);
        progressBar.setLayoutParams(llParam);

        llParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        TextView tvTextUpper = new TextView(this);
        tvTextUpper.setText("\nProszę czekać!\n ");
        tvTextUpper.setTextColor(Color.parseColor("#000000"));
        tvTextUpper.setTextSize(18);
        tvTextUpper.setLayoutParams(llParam);

        TextView tvTextBottom = new TextView(this);
        tvTextBottom.setText("\n  Trwa dodawanie wydarzenia. \n");
        tvTextBottom.setTextColor(Color.parseColor("#000000"));
        tvTextBottom.setTextSize(16);
        tvTextBottom.setLayoutParams(llParam);

        ll.addView(tvTextUpper);
        ll.addView(progressBar);
        ll.addView(tvTextBottom);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setView(ll);

        AlertDialog dialog = builder.create();
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(layoutParams);
        }
        return dialog;
    }

    /**
     * Show toast message running on UI thread.
     *
     * @param text - toast message
     */
    private void showToastMessage(String text) {
        runOnUiThread(() -> Toast.makeText(context, text, Toast.LENGTH_LONG).show());
    }


    /**
     * Set necessary properties for new eventModel object with month interval
     *
     * @param eventModelProperties - not set eventModelProperties object
     * @return eventModelProperties with values necessary for event month interval
     */
    private EventModelProperties getEventModelPropertiesForMonthInterval(EventModelProperties eventModelProperties) {
        LocalTime defaultTime = new LocalTime(settingsViewModel.getDefaultTime(), DateTimeZone.forID("UCT"));
        LocalDate startDateLocalData = LocalDate.parse(startEventDate, DateTimeFormat.forPattern("dd.MM.YYYY"));

        int numberOfRepeats;
        if (monthNumberOfRepeatsField.getText().toString().length() > 0) {
            numberOfRepeats = Integer.parseInt(monthNumberOfRepeatsField.getText().toString());
        } else {
            showToastMessage("Liczba powtórzeń wynosi 0, proszę użyć typu jednorazowe wydarzenie");
            return null;
        }

        int timeInterval = Integer.parseInt(monthWhichDayField.getText().toString());
        if (timeInterval != 0 && timeInterval < 31) {
            showToastMessage("Nieporpawna liczba oznaczająca dzień miesiąca");
            runOnUiThread(() -> dialog.dismiss());
            return null;
        }

        if (monthDefaultTimeCheckbox.isChecked()) {
            eventModelProperties.setPropertiesForMonthIntervalEvent(numberOfRepeats, true, timeInterval, startDateLocalData, defaultTime);
        } else {
            TextView monthTime = findViewById(R.id.textMonthSectionTimeOfEvent);
            if (monthTime.getText().toString().equals("wybierz godzinę")) {
                showToastMessage("Nie wybrano godziny");
                runOnUiThread(() -> dialog.dismiss());
                return null;
            } else {
                LocalTime eventTime = LocalTime.parse(monthTime.getText().toString());
                eventTime = new LocalTime(eventTime, DateTimeZone.forID("UCT"));
                eventModelProperties.setPropertiesForMonthIntervalEvent(numberOfRepeats, false, timeInterval, startDateLocalData, eventTime);
            }
        }
        return eventModelProperties;
    }

    /**
     * Set necessary properties for new eventModel object with custom time interval
     *
     * @param eventModelProperties - not set eventModelProperties object
     * @return eventModelProperties with values necessary for event custom time interval
     */
    private EventModelProperties getEventModelPropertiesForCustomTimeInterval(EventModelProperties eventModelProperties) {
        LocalTime defaultTime = new LocalTime(settingsViewModel.getDefaultTime(), DateTimeZone.forID("UCT"));
        LocalDate startDateLocalData = LocalDate.parse(startEventDate, DateTimeFormat.forPattern("dd.MM.YYYY"));

        int timeInterval = Integer.parseInt(customTimeIntervalField.getText().toString());
        if (timeInterval == 0) {
            showToastMessage("Wartość ilości powtórzeń nie może być 0");
            runOnUiThread(() -> dialog.dismiss());
            return null;
        }

        int customTimeNumberOfRepeats = 0;
        if (customTimeNumberOfRepeatsField.getText().toString().length() > 0) {
            customTimeNumberOfRepeats = Integer.parseInt(customTimeNumberOfRepeatsField.getText().toString());
        } else {
            if (!customTimeRepeatAllTimeChechbox.isChecked()) {
                showToastMessage("Proszę wpisać liczbe powtórzeń");
                runOnUiThread(() -> dialog.dismiss());
                return null;
            }
        }
        if (customTimeDefaultTimeCheckbox.isChecked()) {
            eventModelProperties.setPropertiesForCustomTimeIntervalEvent(customTimeNumberOfRepeats, true, timeInterval, startDateLocalData, defaultTime);
        } else {
            TextView customTime = findViewById(R.id.textCustomTimeEventTime);
            if (customTime.getText().toString().equals("wybierz godzinę")) {
                showToastMessage("Nie wybrano godziny");
                runOnUiThread(() -> dialog.dismiss());
                return null;
            } else {
                LocalTime eventTime = LocalTime.parse(customTime.getText().toString());
                eventTime = new LocalTime(eventTime, DateTimeZone.forID("UCT"));
                eventModelProperties.setPropertiesForCustomTimeIntervalEvent(customTimeNumberOfRepeats, false, timeInterval, startDateLocalData, eventTime);
            }
        }
        return eventModelProperties;
    }

    /**
     * Set necessary properties for new one time eventModel object
     *
     * @param eventModelProperties - not set eventModelProperties object
     * @return eventModelProperties with values necessary for one time event
     */
    private EventModelProperties getEventModelPropertiesForOneTimeEvent(EventModelProperties eventModelProperties) {
        LocalTime defaultTime = new LocalTime(settingsViewModel.getDefaultTime(), DateTimeZone.forID("UCT"));
        LocalDate oneTimeEventDate;
        if (!oneTimeDate.equals("")) {
            oneTimeEventDate = LocalDate.parse(oneTimeDate, DateTimeFormat.forPattern("dd.MM.YYYY"));
        } else {
            showToastMessage("Nie wybrano daty wydarzenia");
            runOnUiThread(() -> dialog.dismiss());
            return null;
        }
        if (oneTimeDefaultTimeCheckbox.isChecked()) {
            eventModelProperties.setPropertiesForOneTimeEvent(oneTimeEventDate, true, defaultTime);
        } else {
            TextView oneTimeTime = findViewById(R.id.textOneTimeTimeOfEvent);
            if (oneTimeTime.getText().toString().equals("wybierz godzinę")) {
                showToastMessage("Nie wybrano godziny");
                runOnUiThread(() -> dialog.dismiss());
                return null;
            } else {
                LocalTime eventTime = LocalTime.parse(oneTimeTime.getText().toString());
                eventTime = new LocalTime(eventTime, DateTimeZone.forID("UCT"));
                eventModelProperties.setPropertiesForOneTimeEvent(oneTimeEventDate, false, eventTime);
            }
        }
        return eventModelProperties;
    }

    /**
     * get unique id for pending intent
     *
     * @param dateTime  - time when notification must be notify
     * @param eventName - name of event to get unique event ID from database
     * @return unique eventID or -1 if event doesn't exist or something went wrong
     */
    private int getNotifyIdForNotification(DateTime dateTime, String eventName) {
        int year = dateTime.getYear();
        String yearShortString = Integer.toString(year).substring(2);
        int yearShort = Integer.parseInt(yearShortString);
        int eventID = eventsViewModel.getEventID(eventName);
        if (eventID != -1) {
            return Integer.parseInt(eventID +
                    String.valueOf(dateTime.getDayOfYear()) + yearShort);
        } else {
            return eventID;
        }
    }

    /**
     * Create notification for new inserted event, set alarm end insert imformation about notification to database.
     *
     * @param eventTime - time of first notification to set alarm
     * @param eventName - name of event to set notification text
     * @return true if notification created. otherwise false
     */
    private boolean createNotification(DateTime eventTime, String eventName) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent alarmIntent;

        int notifyAndPendingIntentID = getNotifyIdForNotification(eventTime, eventName);
        if (notifyAndPendingIntentID != -1) {
            Intent notificationIntent = new Intent(getApplicationContext(), ReminderBroadcast.class);
            notificationIntent.putExtra("NOTIFY_TEXT", eventName);
            notificationIntent.putExtra("ID", notifyAndPendingIntentID);

            alarmIntent = PendingIntent.getBroadcast(getApplicationContext(),
                    notifyAndPendingIntentID,
                    notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, eventTime.getMillis(), alarmIntent);


        } else {
            Log.d(TAG, "Problem z pobraniem ID wydarzenia z bazy");
            showToastMessage("Wystąpił problem z tworzeniem powiadomienia");
            runOnUiThread(() -> dialog.dismiss());
            return false;
        }

        PrzypominajkaDatabaseHelper.updateNotificationCreatedColumn(eventNameField.getText().toString(),
                true, eventTime.toLocalDate());

        Log.d(TAG, "AddNewEvent: Stworzono powiadomienie dla " + eventNameField.getText().toString() + ", Data:  " + eventTime.toString());

        NotificationModel newNotification = new NotificationModel(
                eventNameField.getText().toString(),
                notifyAndPendingIntentID,
                eventTime.toLocalTime(),
                eventTime.toLocalDate(),
                false);

        long resultInsertNotification = notificationViewModel.insertNotification(newNotification);
        if (resultInsertNotification != -1) {
            Log.d(TAG, "AddNewEvent: Dodano powiadomienie dla " + eventNameField.getText().toString() + " " + eventTime.toLocalDate().toString()
                    + " " + eventTime.toLocalTime().toString());
            return true;
        } else {
            Log.d(TAG, "AddNewEvent: Nie udało się dodać informacji o powiadomieniu");
            runOnUiThread(() -> dialog.dismiss());
            return false;
        }
    }
}