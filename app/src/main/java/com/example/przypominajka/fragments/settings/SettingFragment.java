package com.example.przypominajka.fragments.settings;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.przypominajka.R;
import com.example.przypominajka.activities.LocalBackupSettings;
import com.example.przypominajka.utils.FileUtil;
import com.example.przypominajka.utils.MyPrzypominajkaApp;
import com.example.przypominajka.utils.Permissions;
import com.example.przypominajka.utils.databaseBackupAndRestore.LocalBackupDatabase;
import com.example.przypominajka.utils.databaseBackupAndRestore.RestoreLocalDatabase;
import com.example.przypominajka.viewModels.SettingsViewModel;

import org.joda.time.DateTimeZone;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;


public class SettingFragment extends Fragment {

    View view;

    private TextView defaultTimeField;

    private TextView eventRefreshIntervalField;

    private TextView currentBackupSettingsField;

    private Button createBackupButton;
    private Button restoreBackupButton;
    private Button configureBackup;

    private Spinner backupLocalizationSpinner;
    private Spinner restoreLocalizationSpinner;

    TimePickerDialog timePickerDefaultTime;
    TimePickerDialog timePickerCheckInterval;

    private static final int PATH_REQUEST_CODE = 2;

    private String fileToRestorePath;

    private final SettingsViewModel settingsViewModel = new SettingsViewModel(MyPrzypominajkaApp.get());

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

        currentBackupSettingsField = view.findViewById(R.id.current_backup_settings);
        settingsViewModel.getLocalBackupLocationLiveData().observe(getViewLifecycleOwner(), s -> {
            if (s.equals("") || s == null) {
                currentBackupSettingsField.setText("Przeprowadź konfigurację");
            } else {
                String[] paths = s.split("\\|");
                currentBackupSettingsField.setText(paths[0]);
            }
        });
        createBackupButton = view.findViewById(R.id.create_backup);
        createBackupButton.setOnClickListener(v -> {
            try {
                createBackup();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("SettingFragment createBackupButton", "Problem z stworzeniem backupu " + e.getMessage());
            }
        });

        restoreBackupButton = view.findViewById(R.id.restore_backup);
        restoreBackupButton.setOnClickListener(v -> restoreBackup());

        configureBackup = view.findViewById(R.id.configure_backup);
        configureBackup.setOnClickListener(v -> setBackupSettings());

        backupLocalizationSpinner = view.findViewById(R.id.create_backup_spinner);
        restoreLocalizationSpinner = view.findViewById(R.id.restore_backup_spinner);

        super.onViewCreated(view, savedInstanceState);
    }

    private void createBackup() throws IOException {
        if (currentBackupSettingsField.getText() == "") {
            Toast.makeText(requireContext(), "Najpierw przeprwoadź konfigurację", Toast.LENGTH_SHORT).show();
        } else {
            if (backupLocalizationSpinner.getSelectedItem().toString().equals("Na telefonie")) {
                Permissions.verifyStoragePermissions(getActivity());
                LocalBackupDatabase localBackupDatabase = new LocalBackupDatabase(getContext(), getActivity());
                localBackupDatabase.createBackup();
            }
        }
    }

    private void restoreBackup() {
        if (restoreLocalizationSpinner.getSelectedItem().toString().equals("Telefon")) {
            Permissions.verifyStoragePermissions(getActivity());
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setType("*/*");
            startActivityForResult(Intent.createChooser(intent, "Wybierz plik"), PATH_REQUEST_CODE);
        }
    }

    private void setBackupSettings() {
        if (backupLocalizationSpinner.getSelectedItem().toString().equals("Dysk Google")) {
            Log.d("ConfigureGoogleDriveBackup", "Message");
        } else if (backupLocalizationSpinner.getSelectedItem().toString().equals("Na telefonie")) {
            Permissions.verifyStoragePermissions(getActivity());
            Intent localSettings = new Intent(getContext(), LocalBackupSettings.class);
            startActivity(localSettings);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PATH_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            try {
                assert fileUri != null;
                InputStream inputStream = getContext().getContentResolver().openInputStream(fileUri);
                if (validFile(fileUri)) {
                    RestoreLocalDatabase restoreLocalDatabase = new RestoreLocalDatabase(getContext(), getActivity());
                    boolean result = restoreLocalDatabase.restoreBackup(inputStream);
                    if (result) {
                        Toast.makeText(getContext(), "Przywracanie bazy danych powiodło się", Toast.LENGTH_LONG).show();
                        Log.d("SettingsFramgnent localRestore", "Przywracanie powiodło się");
                    } else {
                        Toast.makeText(getContext(), "Przywracanie bazy danych nie powiodło się", Toast.LENGTH_LONG).show();
                        Log.d("SettingsFramgnent localRestore", "Przywracanie nie powiodło się");
                    }
                } else {
                    Toast.makeText(getContext(), "Wybrano niewłaściwy plik", Toast.LENGTH_LONG).show();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean validFile(Uri fileUri) {
        ContentResolver cr = getContext().getContentResolver();
        String mime = cr.getType(fileUri);
        return "application/octet-stream".equals(mime);
    }
}