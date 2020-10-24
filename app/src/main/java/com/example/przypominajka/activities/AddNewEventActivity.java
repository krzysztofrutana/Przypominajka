package com.example.przypominajka.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.przypominajka.models.Event;
import com.example.przypominajka.databases.PrzypominajkaDatabaseHelper;
import com.example.przypominajka.R;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import java.util.Calendar;

import yuku.ambilwarna.AmbilWarnaDialog;

public class AddNewEventActivity extends AppCompatActivity {

    PrzypominajkaDatabaseHelper przypominajkaDatabaseHelper = new PrzypominajkaDatabaseHelper(this);

    // Event name section
    private EditText eventNameField;
    private TextView eventNameWarningHintField;

    // Event discription section
    private EditText eventDiscriptionField;

    // Event color section
    private int eventColorNumber;
    private int defaultColor;
    private Button chooseColorButton;

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
    private TextView monthIntervalTimeField;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new);
        // show toolbar
        Toolbar toolbar = findViewById(R.id.toolbarAddNewActivity);
        setSupportActionBar(toolbar);
        // and back button
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

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
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
                Cursor cursor = przypominajkaDatabaseHelper.getEvent(s.toString().replaceAll(" ", "_"));
                if (cursor.getCount() != 0) {
                    ColorStateList colorStateList = ColorStateList.valueOf(Color.RED);
                    eventNameField.setTextColor(Color.RED);
                    eventNameField.setLinkTextColor(Color.RED);
                    if (Integer.valueOf(android.os.Build.VERSION.SDK) >= 21) {
                        eventNameField.setBackgroundTintList(colorStateList);
                    }
                    eventNameWarningHintField.setText("Wydarzenie już istnieje");
                    eventNameWarningHintField.setTextColor(Color.RED);
                } else {
                    ColorStateList colorStateList = ContextCompat.getColorStateList(context, R.color.colorAccent);
                    if (Integer.valueOf(android.os.Build.VERSION.SDK) >= 21) {
                        eventNameField.setBackgroundTintList(colorStateList);
                    }
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
        chooseColorButton = findViewById(R.id.buttonChooseColor);
        Drawable unwrappedDrawable = AppCompatResources.getDrawable(AddNewEventActivity.this, R.drawable.button_corner_radius);
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
                Drawable unwrappedDrawable = AppCompatResources.getDrawable(AddNewEventActivity.this, R.drawable.button_corner_radius);
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
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioButtonCurrentDay:
                        linearLayoutMonthSection.setVisibility(LinearLayout.VISIBLE);
                        linearLayoutCustomSection.setVisibility(LinearLayout.GONE);
                        linearLayoutOneTimeSection.setVisibility(LinearLayout.GONE);
                        itsMonthInterval = true;
                        itsCustomTimeInterval = false;
                        itsOneTimeEvent = false;
                        break;
                    case R.id.radioButtonJumpDay:
                        linearLayoutCustomSection.setVisibility(LinearLayout.VISIBLE);
                        linearLayoutMonthSection.setVisibility(LinearLayout.GONE);
                        linearLayoutOneTimeSection.setVisibility(LinearLayout.GONE);
                        itsMonthInterval = false;
                        itsCustomTimeInterval = true;
                        itsOneTimeEvent = false;
                        break;
                    case R.id.radioButtonOneTime:
                        linearLayoutCustomSection.setVisibility(LinearLayout.GONE);
                        linearLayoutMonthSection.setVisibility(LinearLayout.GONE);
                        linearLayoutOneTimeSection.setVisibility(LinearLayout.VISIBLE);
                        itsMonthInterval = false;
                        itsCustomTimeInterval = false;
                        itsOneTimeEvent = true;
                        break;
                }
            }
        });

    }

    private void monthIntervalSectionProperties() {
        monthNumberOfRepeatsField = findViewById(R.id.editMonthSectionNumerOfRepeats);
        monthWhichDayField = findViewById(R.id.editMonthSectionWhichDay);

        monthDefaultTimeCheckbox = findViewById(R.id.checkboxMonthSectionlDefaultTime);
        monthIntervalTimeField = findViewById(R.id.textMonthSectionTimeOfEvent);
        monthDefaultTimeCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    monthIntervalTimeField.setVisibility(LinearLayout.GONE);
                } else {
                    monthIntervalTimeField.setVisibility(LinearLayout.VISIBLE);
                }
            }
        });

        monthIntervalTimeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Use the current time as the default values for the picker
                final Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                // Create a new instance of c
                timePickerDialogitsMonthInterval = new TimePickerDialog(AddNewEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String time = selectedHour + ":" + selectedMinute;
                        monthIntervalTimeField.setText(time);
                        monthIntervalTimeField.setTextSize(18);
                        monthDefaultTimeCheckbox.forceLayout();
                    }
                }, hour, minute, true);//Yes 24 hour time
                timePickerDialogitsMonthInterval.setTitle("Wybierz godzinę");
                timePickerDialogitsMonthInterval.show();
            }
        });

    }

    private void customTimeSectionProperties() {

        customTimeRepeatAllTimeChechbox = findViewById(R.id.checkboxCustomTimeRepeatsAllways);
        customTimeNumberOfRepeatsField = findViewById(R.id.editCustomTimeNumberOfRepeats);
        customTimeIntervalField = findViewById(R.id.editTextCustomTimeInterval);
        customTimeDayChechbox = findViewById(R.id.checkBoxCustomTimeDay);
        customTimeWeekChechbox = findViewById(R.id.checkBoxCustomTimeWeek);
        customTimeMonthChechbox = findViewById(R.id.checkBoxCustomTimeMonth);
        customTimeDayChechbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    customTimeWeekChechbox.setChecked(false);
                    customTimeMonthChechbox.setChecked(false);
                }
            }
        });
        customTimeWeekChechbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    customTimeDayChechbox.setChecked(false);
                    customTimeMonthChechbox.setChecked(false);
                }
            }
        });
        customTimeMonthChechbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    customTimeDayChechbox.setChecked(false);
                    customTimeWeekChechbox.setChecked(false);
                }
            }
        });
        customTimeRepeatAllTimeChechbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    customTimeNumberOfRepeatsField.setVisibility(LinearLayout.GONE);
                } else {
                    customTimeNumberOfRepeatsField.setVisibility(LinearLayout.VISIBLE);
                }
            }
        });
        customTimeDefaultTimeCheckbox = findViewById(R.id.checkboxCustomTimeDefaultTime);
        customTimeTimeOfEventField = findViewById(R.id.textCustomTimeEventTime);
        customTimeDefaultTimeCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    customTimeTimeOfEventField.setVisibility(LinearLayout.GONE);
                } else {
                    customTimeTimeOfEventField.setVisibility(LinearLayout.VISIBLE);
                }
            }
        });

        customTimeTimeOfEventField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Use the current time as the default values for the picker
                final Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                // Create a new instance of c
                timePickerDialogCustomTime = new TimePickerDialog(AddNewEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String minute = "";
                        if (selectedMinute < 10) {
                            minute = "0" + selectedMinute;
                        } else {
                            minute = String.valueOf(selectedMinute);
                        }
                        String time = selectedHour + ":" + minute;
                        customTimeTimeOfEventField.setText(time);
                        customTimeTimeOfEventField.setTextSize(18);
                        customTimeDefaultTimeCheckbox.forceLayout(); // update view after resize textView


                    }
                }, hour, minute, true);//Yes 24 hour time
                timePickerDialogCustomTime.setTitle("Wybierz godzinę");
                timePickerDialogCustomTime.show();
            }
        });
    }

    private void oneTimeSectionProperties() {

        oneTimeDate = "";

        oneTimeDefaultTimeCheckbox = findViewById(R.id.checkboxOneTimeDefaultTime);
        oneTimeEventTimeField = findViewById(R.id.editOneTimeTimeOfEvent);
        oneTimeDefaultTimeCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    oneTimeEventTimeField.setVisibility(LinearLayout.GONE);
                } else {
                    oneTimeEventTimeField.setVisibility(LinearLayout.VISIBLE);
                }
            }
        });

        oneTimeEventDateField = findViewById(R.id.textViewOneTimeSectionSetDate);
        // show date picker dialog to set date
        oneTimeEventDateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialogOneTime = new DatePickerDialog(AddNewEventActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        dateSetListenerOneTime, year, month, day);
                datePickerDialogOneTime.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialogOneTime.show();
            }
        });

        dateSetListenerOneTime = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String choosenData = dayOfMonth + "." + month + "." + year;
                oneTimeEventDateField.setText(choosenData);
                oneTimeDate = choosenData;
            }
        };

        oneTimeEventTimeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Use the current time as the default values for the picker
                final Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                // Create a new instance of c
                timePickerDialogOneTime = new TimePickerDialog(AddNewEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String time = selectedHour + ":" + selectedMinute;
                        oneTimeEventTimeField.setText(time);
                        oneTimeEventTimeField.setTextSize(18);
                        oneTimeDefaultTimeCheckbox.forceLayout();
                    }
                }, hour, minute, true);//Yes 24 hour time
                timePickerDialogOneTime.setTitle("Wybierz godzinę");
                timePickerDialogOneTime.show();
            }
        });
    }

    private void startDateSectionProperties() {

        // show date picker dialog to set date
        displayEventDateField = findViewById(R.id.textViewSetStartDate);
        displayEventDateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(AddNewEventActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        dateSetListenerStartEventDate, year, month, day);
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.show();
            }
        });

        dateSetListenerStartEventDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String choosenData = dayOfMonth + "." + month + "." + year;
                displayEventDateField.setText(choosenData);
                startEventDate = choosenData;
            }
        };
    }


    // get everything field, make new Event object and seve event to database
    public void buttonSaveNewEvent(View view) {

        if (eventNameWarningHintField.getText().toString() == "Wydarzenie już istnieje") {
            Toast.makeText(this, "Podaj inną nazwę wydarzenia", Toast.LENGTH_LONG).show();
            ;
            return;
        }

        LocalDate startDateLocalData = LocalDate.parse(startEventDate, DateTimeFormat.forPattern("dd.MM.YYYY"));
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


        try {


            Event newEvent = new Event(eventNameField.getText().toString(),
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
                    timeIntervalNumber, startDateLocalData);
            boolean addEvent = przypominajkaDatabaseHelper.insertEvent(newEvent);
            if (addEvent) {
                Toast.makeText(this, "Dodawanie zdarzenia powiodło się", Toast.LENGTH_SHORT).show();
                this.finish();
            } else {
                Toast.makeText(this, "Wystąpił problem podczas dodawania zdarzenia", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }
}