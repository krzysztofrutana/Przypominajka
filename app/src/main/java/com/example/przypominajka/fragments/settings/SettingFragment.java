package com.example.przypominajka.fragments.settings;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.przypominajka.R;
import com.example.przypominajka.activities.AddNewEventActivity;
import com.example.przypominajka.databases.PrzypominajkaDatabaseHelper;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Calendar;

public class SettingFragment extends Fragment {

    View view;

    PrzypominajkaDatabaseHelper przypominajkaDatabaseHelper;

    private TextView defaultTimeField;
    private String defaultTime;

    TimePickerDialog timePickerDefaultTime;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.view = view;
        przypominajkaDatabaseHelper = new PrzypominajkaDatabaseHelper(requireContext());
        defaultTimeField = view.findViewById(R.id.textSettingsDefaultTime);
        if (przypominajkaDatabaseHelper.getDefaultTime() == 0) {
            defaultTimeField.setText("Wybierz godzinę");
        } else {
            LocalTime time = new LocalTime(przypominajkaDatabaseHelper.getDefaultTime());
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
                        String time = selectedHour + ":" + selectedMinute;
                        defaultTimeField.setText(time);
                        defaultTimeField.setTextSize(18);
                        LocalTime date = new LocalTime(selectedHour, selectedMinute);
                        przypominajkaDatabaseHelper.updateDefaultTimeInSettings(date.getMillisOfDay());
                    }
                }, hour, minute, true);//Yes 24 hour time
                timePickerDefaultTime.setTitle("Wybierz godzinę");
                timePickerDefaultTime.show();
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }

}