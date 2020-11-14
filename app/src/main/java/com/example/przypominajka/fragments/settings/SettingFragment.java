package com.example.przypominajka.fragments.settings;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.przypominajka.R;
import com.example.przypominajka.activities.AddNewEventActivity;
import com.example.przypominajka.databases.PrzypominajkaDatabaseHelper;
import com.example.przypominajka.utils.MyPrzypominajkaApp;
import com.example.przypominajka.viewModels.SettingsViewModel;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Calendar;
import java.util.TimeZone;

public class SettingFragment extends Fragment {

    View view;

    PrzypominajkaDatabaseHelper przypominajkaDatabaseHelper;

    private TextView defaultTimeField;
    private String defaultTime;

    private TextView eventRefreshIntervalField;
    private String eventRefreshInterval;

    TimePickerDialog timePickerDefaultTime;
    TimePickerDialog timePickerCheckInterval;

    private SettingsViewModel settingsViewModel = new SettingsViewModel(MyPrzypominajkaApp.get());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.view = view;
        defaultTimeField = view.findViewById(R.id.textSettingsDefaultTime);
        if (settingsViewModel.getDefaultTime() == 0) {
            defaultTimeField.setText("wybierz godzinę");
        } else {
            LocalTime time = new LocalTime(settingsViewModel.getDefaultTime(), DateTimeZone.forID("UCT"));
            defaultTimeField.setText(time.toString(DateTimeFormat.forPattern("HH:mm")));
        }
        defaultTimeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Use the current time as the default values for the picker
                final Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                // Create a new instance of c
                timePickerDefaultTime = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String minute = "";
                        if (selectedMinute < 10) {
                            minute = "0" + selectedMinute;
                        } else {
                            minute = String.valueOf(selectedMinute);
                        }
                        String time = selectedHour + ":" + minute;
                        defaultTimeField.setText(time);
                        defaultTimeField.setTextSize(18);
                        LocalTime date = new LocalTime(selectedHour, selectedMinute);
                        int result = settingsViewModel.updateDefaultTime(date.getMillisOfDay());
                        if (result > 0) {
                            Log.d("SettingFragment onClick Default Time", "Aktualizacja domyślnego czasu udana");
                        } else {
                            Log.d("SettingFragment onClick Default Time", "Aktualizacja domyślnego czasu nieudana");
                        }
                    }
                }, hour, minute, DateFormat.is24HourFormat(requireContext()));//Yes 24 hour time
                timePickerDefaultTime.setTitle("Wybierz godzinę");
                timePickerDefaultTime.show();
            }
        });

        eventRefreshIntervalField = view.findViewById(R.id.textCheckEventInerval);
        if (settingsViewModel.getCheckEventInterval() == 0) {
            eventRefreshIntervalField.setText("wybierz czas");
        } else {
            LocalTime time = new LocalTime(settingsViewModel.getCheckEventInterval(), DateTimeZone.forID("UCT"));
            eventRefreshIntervalField.setText(time.toString(DateTimeFormat.forPattern("HH:mm")));
        }
        eventRefreshIntervalField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Use the current time as the default values for the picker
                final Calendar c = Calendar.getInstance();
                int hour = 6;
                int minute = 0;
                // Create a new instance of c
                timePickerCheckInterval = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String minute = "";
                        if (selectedMinute < 10) {
                            minute = "0" + selectedMinute;
                        } else {
                            minute = String.valueOf(selectedMinute);
                        }
                        String time = selectedHour + ":" + minute;
                        eventRefreshIntervalField.setText(time);
                        eventRefreshIntervalField.setTextSize(18);
                        LocalTime date = new LocalTime(selectedHour, selectedMinute);
                        int result = settingsViewModel.updateCheckEventInterval(date.getMillisOfDay());
                        if (result > 0) {
                            Log.d("SettingFragment onClick Interval Time", "Aktualizacja domyślnego czasu udana");
                        } else {
                            Log.d("SettingFragment onClick Interval Time", "Aktualizacja domyślnego czasu nieudana");
                        }
                    }
                }, hour, minute, true);
                timePickerCheckInterval.setTitle("Wybierz czas");
                timePickerCheckInterval.show();
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }

}