package com.example.przypominajka.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
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
import android.widget.Toast;

import com.example.przypominajka.models.Event;
import com.example.przypominajka.databases.PrzypominajkaDatabaseHelper;
import com.example.przypominajka.R;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import java.util.Calendar;

import yuku.ambilwarna.AmbilWarnaDialog;

public class AddNewEventActivity extends AppCompatActivity {

    PrzypominajkaDatabaseHelper przypominajkaDatabaseHelper;
    private EditText eventName;
    private EditText eventDiscription;
    private int eventColor;

    private boolean monthInterval;
    private EditText monthNumberOfRepeatsField;
    private EditText monthWhichDayField;

    private boolean shortTimeInterval;
    private int shortTimeType; // type (0 - none, 1 - day,2 - week,3 - month)
    private CheckBox shortTimeRepeatAllTime;
    private EditText shortTimeNumberOfRepeatsField;
    private EditText shortTimeIntervalNumber;
    private CheckBox shortTimeDay;
    private CheckBox shortTimeWeek;
    private CheckBox shortTimeMonth;

    private boolean oneTimeEvent;
    private TextView oneTimeEventDateField;
    private String oneTimeDate;

    private int timeInterval;

    private String startDate;
    private TextView displayDate;

    private DatePickerDialog.OnDateSetListener dateSetListener;

    private DatePickerDialog.OnDateSetListener dateSetListenerOneTime;

    private int mDefaultColor;

    private Button buttonChooseColor;

    private TextView textWarningHint;

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

        mDefaultColor = 0xE0E0E0;
        oneTimeDate = "";
        // initialize necessary elements
        przypominajkaDatabaseHelper = new PrzypominajkaDatabaseHelper(this);
        eventName = findViewById(R.id.editTextEventName);
        eventDiscription = findViewById(R.id.editTextEventDiscription);

        monthNumberOfRepeatsField = findViewById(R.id.editNumberOfMonth);
        monthWhichDayField = findViewById(R.id.editWhichDay);

        shortTimeRepeatAllTime = findViewById(R.id.checkboxJumpDayAllways);
        shortTimeNumberOfRepeatsField = findViewById(R.id.editJumpDayHowLongCount);
        shortTimeIntervalNumber = findViewById(R.id.editTextShortTimeInterval);
        shortTimeDay = findViewById(R.id.checkBoxDay);
        shortTimeWeek = findViewById(R.id.checkBoxWeek);
        shortTimeMonth = findViewById(R.id.checkBoxMonth);
        shortTimeDay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    shortTimeWeek.setChecked(false);
                    shortTimeMonth.setChecked(false);
                }
            }
        });
        shortTimeWeek.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    shortTimeDay.setChecked(false);
                    shortTimeMonth.setChecked(false);
                }
            }
        });
        shortTimeMonth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    shortTimeDay.setChecked(false);
                    shortTimeWeek.setChecked(false);
                }
            }
        });
        shortTimeRepeatAllTime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    shortTimeNumberOfRepeatsField.setVisibility(LinearLayout.GONE);
                } else {
                    shortTimeNumberOfRepeatsField.setVisibility(LinearLayout.VISIBLE);
                }
            }
        });


        textWarningHint = findViewById(R.id.text_hint);

        // reset button background when activity starts working
        buttonChooseColor = findViewById(R.id.buttonChooseColor);
        Drawable unwrappedDrawable = AppCompatResources.getDrawable(AddNewEventActivity.this, R.drawable.button_corner_radius);
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, 0xE0E0E0);
        buttonChooseColor.setBackground(wrappedDrawable);


        // show date picker dialog to set date
        displayDate = findViewById(R.id.textViewSetDate);
        displayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(AddNewEventActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        dateSetListener, year, month, day);
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.show();
            }
        });

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String choosenData = dayOfMonth + "." + month + "." + year;
                displayDate.setText(choosenData);
                startDate = choosenData;
            }
        };

        // show date picker dialog to set date
        oneTimeEventDateField = findViewById(R.id.textViewOneTimeSetDate);
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

        // set radio buttons function
        RadioGroup rg = findViewById(R.id.radioGroupAddNewEvent);
        final LinearLayout linearLayoutlinearLayoutCMonthInterval = findViewById(R.id.linearLayoutCMonthInterval);
        final LinearLayout linearLayoutShortInterval = findViewById(R.id.linearLayoutShortInterval);
        final LinearLayout linearLayoutOneTime = findViewById(R.id.linearLayoutOneTimeEvent);
        linearLayoutOneTime.setVisibility(LinearLayout.GONE);
        linearLayoutShortInterval.setVisibility(LinearLayout.GONE);
        linearLayoutlinearLayoutCMonthInterval.setVisibility(LinearLayout.GONE);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioButtonCurrentDay:
                        linearLayoutlinearLayoutCMonthInterval.setVisibility(LinearLayout.VISIBLE);
                        linearLayoutShortInterval.setVisibility(LinearLayout.GONE);
                        linearLayoutOneTime.setVisibility(LinearLayout.GONE);
                        monthInterval = true;
                        shortTimeInterval = false;
                        oneTimeEvent = false;
                        break;
                    case R.id.radioButtonJumpDay:
                        linearLayoutShortInterval.setVisibility(LinearLayout.VISIBLE);
                        linearLayoutlinearLayoutCMonthInterval.setVisibility(LinearLayout.GONE);
                        linearLayoutOneTime.setVisibility(LinearLayout.GONE);
                        monthInterval = false;
                        shortTimeInterval = true;
                        oneTimeEvent = false;
                        break;
                    case R.id.radioButtonOneTime:
                        linearLayoutShortInterval.setVisibility(LinearLayout.GONE);
                        linearLayoutlinearLayoutCMonthInterval.setVisibility(LinearLayout.GONE);
                        linearLayoutOneTime.setVisibility(LinearLayout.VISIBLE);
                        monthInterval = false;
                        shortTimeInterval = false;
                        oneTimeEvent = true;
                        break;
                }
            }
        });

        final Context contet = this;
        eventName.addTextChangedListener(new TextWatcher() {
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
                    eventName.setTextColor(Color.RED);
                    eventName.setLinkTextColor(Color.RED);
                    if (Integer.valueOf(android.os.Build.VERSION.SDK) >= 21) {
                        eventName.setBackgroundTintList(colorStateList);
                    }
                    textWarningHint.setText("Wydarzenie już istnieje");
                    textWarningHint.setTextColor(Color.RED);
                } else {
                    ColorStateList colorStateList = ContextCompat.getColorStateList(contet, R.color.colorAccent);
                    if (Integer.valueOf(android.os.Build.VERSION.SDK) >= 21) {
                        eventName.setBackgroundTintList(colorStateList);
                    }
                    eventName.setTextColor(Color.GRAY);
                    textWarningHint.setText("");
                }
            }
        });
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

    // show color picker dialog to choose color of event
    public void buttonChooseColor_onClick(final View view) {
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, mDefaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                mDefaultColor = color;
                eventColor = color;
                Drawable unwrappedDrawable = AppCompatResources.getDrawable(AddNewEventActivity.this, R.drawable.button_corner_radius);
                Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
                DrawableCompat.setTint(wrappedDrawable, mDefaultColor);
                view.setBackground(wrappedDrawable);
                Button button = findViewById(R.id.buttonChooseColor);
                button.setText("");

            }
        });
        colorPicker.show();
    }


    // get everything field, make new Event object and seve event to database
    public void buttonSaveNewEvent(View view) {

        if (textWarningHint.getText().toString() == "Wydarzenie już istnieje") {
            Toast.makeText(this, "Podaj inną nazwę wydarzenia", Toast.LENGTH_LONG).show();
            ;
            return;
        }

        LocalDate startDateLocalData = LocalDate.parse(startDate, DateTimeFormat.forPattern("dd.MM.YYYY"));
        LocalDate oneTimeEventDate;
        if (oneTimeDate.equals("")) {
            oneTimeEventDate = null;
        } else {
            oneTimeEventDate = LocalDate.parse(oneTimeDate, DateTimeFormat.forPattern("dd.MM.YYYY"));
        }

        if (shortTimeInterval) {
            timeInterval = Integer.parseInt(shortTimeIntervalNumber.getText().toString());
        }
        if (monthInterval) {
            timeInterval = Integer.parseInt(monthWhichDayField.getText().toString());
        }
        if (oneTimeEvent) {
            timeInterval = 0;
        }
        if (shortTimeDay.isChecked()) {
            shortTimeType = 1;
        } else if (shortTimeWeek.isChecked()) {
            shortTimeType = 2;
        } else if (shortTimeMonth.isChecked()) {
            shortTimeType = 3;
        }
        int monthNumberOfRepeats;
        if (monthNumberOfRepeatsField.getText().toString().length() > 0) {
            monthNumberOfRepeats = Integer.parseInt(monthNumberOfRepeatsField.getText().toString());
        } else {
            monthNumberOfRepeats = 0;
        }

        int shortTimeNumberOfRepeats;
        if (shortTimeNumberOfRepeatsField.getText().toString().length() > 0) {
            shortTimeNumberOfRepeats = Integer.parseInt(shortTimeNumberOfRepeatsField.getText().toString());
        } else {
            shortTimeNumberOfRepeats = 0;
        }


        try {


            Event newEvent = new Event(eventName.getText().toString(),
                    eventDiscription.getText().toString(),
                    eventColor,
                    monthInterval,
                    monthNumberOfRepeats,
                    shortTimeInterval,
                    shortTimeType,
                    shortTimeRepeatAllTime.isChecked(),
                    shortTimeNumberOfRepeats,
                    oneTimeEvent,
                    oneTimeEventDate,
                    timeInterval, startDateLocalData);
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