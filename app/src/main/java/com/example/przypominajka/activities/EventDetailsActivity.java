package com.example.przypominajka.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;

import android.annotation.SuppressLint;
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

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;


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

    private int timeIntervalOfRepeat;
    private long startDate;

    TextView eventNameView;
    TextView eventDiscriptionView;
    Button colorButtonView;
    TextView eventTypeViewPart1;
    TextView eventTypeViewPart2;
    TextView eventTypeViewPart3;
    TextView startDateView;


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
                boolean deleteEvent = przypominajkaDatabaseHelper.deleteEvent(eventName);
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
            String eventTypeMonthIntervalPart3 = "Przez " + monthNumberOfRepeats + " miesięcy";
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
        startDateView.setText(new LocalDate(startDate).toString(DateTimeFormat.forPattern("dd.MM.YYYY")));

    }

    // prepare information about event
    @SuppressLint({"ShowToast"})
    private void getInformationAboutEvent() {
        Cursor cursor = przypominajkaDatabaseHelper.getEvent(eventName);
        if (cursor.getCount() == 0) {
            Toast.makeText(this, "Nie udało się uzyskać informacji o zdarzeniu", Toast.LENGTH_LONG);
        }
        cursor.moveToFirst();
        eventDiscription = cursor.getString(2);
        eventColor = cursor.getInt(3);
        monthInterval = cursor.getInt(4) == 1;
        monthNumberOfRepeats = cursor.getInt(5);
        shortTimeInterval = cursor.getInt(6) == 1;
        shortTimeType = cursor.getInt(7);
        shortTimeRepeatAllTime = cursor.getInt(8) == 1;
        shortTimeNumberOfRepeats = cursor.getInt(9);
        oneTimeEvent = cursor.getInt(10) == 1;
        oneTimeEventDate = cursor.getLong(11);
        timeIntervalOfRepeat = cursor.getInt(12);
        startDate = cursor.getLong(13);
    }
}
