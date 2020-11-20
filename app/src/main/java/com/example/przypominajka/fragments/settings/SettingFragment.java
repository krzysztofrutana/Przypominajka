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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.przypominajka.R;
import com.example.przypominajka.activities.LocalBackupSettings;
import com.example.przypominajka.activities.RemoteBackupSettings;
import com.example.przypominajka.utils.DriveServiceHelper;
import com.example.przypominajka.utils.MyPrzypominajkaApp;
import com.example.przypominajka.utils.Permissions;
import com.example.przypominajka.utils.databaseBackupAndRestore.BackupDatabase.LocalBackupDatabase;
import com.example.przypominajka.utils.databaseBackupAndRestore.BackupDatabase.RemoteBackupDatabase;
import com.example.przypominajka.utils.databaseBackupAndRestore.RestoreBackup.RestoreDatabase;
import com.example.przypominajka.viewModels.SettingsViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;

import org.joda.time.DateTimeZone;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

import java.io.IOException;
import java.io.InputStream;

import static com.example.przypominajka.utils.DriveServiceHelper.getGoogleDriveService;


public class SettingFragment extends Fragment {

    View view;

    // Fields
    private TextView defaultTimeField;
    private TextView eventRefreshIntervalField;
    private TextView currentBackupSettingsField;
    private Spinner backupLocalizationSpinner;
    private Spinner restoreLocalizationSpinner;
    private ImageButton showMakeBackupSectionButton;
    private ImageButton showRestoreBackupSectionButton;
    private LinearLayout backupSectionLL;
    private LinearLayout restoreSectionLL;
    private boolean makeBackupSectionIsShow = false;
    private boolean restoreBackupSectionIsShow = false;

    // PickerTime to fields
    TimePickerDialog timePickerDefaultTime;
    TimePickerDialog timePickerCheckInterval;

    // Request code to use in onActivityResult and startActivityForResults
    private static final int PATH_REQUEST_CODE_LOCAL = 2;
    private static final int PATH_REQUEST_CODE_REMOTE = 3;

    // Instance of Google Drive API helper class
    private DriveServiceHelper mDriveServiceHelper;

    // Used to get
    private static final String APP_NAME = "Przypominajka";

    //String used in this fragment
    private static final String DO_CONFIGURATION = MyPrzypominajkaApp.get().getResources().getString(R.string.SF_do_configuration);
    private static final String CHOOSE_TIME = MyPrzypominajkaApp.get().getResources().getString(R.string.SF_choose_configuration);
    private static final String LOGGED_IN_AS = MyPrzypominajkaApp.get().getResources().getString(R.string.SF_logged_in_as);

    private final SettingsViewModel settingsViewModel = new SettingsViewModel(MyPrzypominajkaApp.get());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.view = view;
        /*
        TIME SETTINGS SECTION
         */
        defaultTimeField = view.findViewById(R.id.textSettingsDefaultTime);
        if (settingsViewModel.getDefaultTime() == 0) {
            defaultTimeField.setText(CHOOSE_TIME);
        } else {
            LocalTime time = new LocalTime(settingsViewModel.getDefaultTime(), DateTimeZone.forID("UCT"));
            defaultTimeField.setText(time.toString(DateTimeFormat.forPattern("HH:mm")));
        }

        defaultTimeField.setOnClickListener(viewDefaultTimeField -> {
            int hour = 8;
            int minute = 0;
            timePickerDefaultTime = new TimePickerDialog(getContext(), (timePicker, selectedHour, selectedMinute) -> {
                String minuteDefaultTime;
                if (selectedMinute < 10) {
                    minuteDefaultTime = "0" + selectedMinute;
                } else {
                    minuteDefaultTime = String.valueOf(selectedMinute);
                }
                String time = selectedHour + ":" + minuteDefaultTime;
                defaultTimeField.setText(time);
                defaultTimeField.setTextSize(18);
                LocalTime date = new LocalTime(selectedHour, selectedMinute);
                int result = settingsViewModel.updateDefaultTime(date.getMillisOfDay());
                if (result > 0) {
                    Log.d("SettingFragment onClick Default Time", "Aktualizacja domyślnego czasu udana");
                } else {
                    Log.d("SettingFragment onClick Default Time", "Aktualizacja domyślnego czasu nieudana");
                }
            }, hour, minute, DateFormat.is24HourFormat(requireContext()));//Yes 24 hour time
            timePickerDefaultTime.setTitle("Wybierz godzinę");
            timePickerDefaultTime.show();
        });

        eventRefreshIntervalField = view.findViewById(R.id.textCheckEventInerval);
        if (settingsViewModel.getCheckEventInterval() == 0) {
            eventRefreshIntervalField.setText(CHOOSE_TIME);
        } else {
            LocalTime time = new LocalTime(settingsViewModel.getCheckEventInterval(), DateTimeZone.forID("UCT"));
            eventRefreshIntervalField.setText(time.toString(DateTimeFormat.forPattern("HH:mm")));
        }
        eventRefreshIntervalField.setOnClickListener(viewEventRefreshIntervalField -> {
            int hour = 6;
            int minute = 0;
            timePickerCheckInterval = new TimePickerDialog(getContext(), (timePicker, selectedHour, selectedMinute) -> {
                String minuteCheckTimeInterval;
                if (selectedMinute < 10) {
                    minuteCheckTimeInterval = "0" + selectedMinute;
                } else {
                    minuteCheckTimeInterval = String.valueOf(selectedMinute);
                }
                String time = selectedHour + ":" + minuteCheckTimeInterval;
                eventRefreshIntervalField.setText(time);
                eventRefreshIntervalField.setTextSize(18);
                LocalTime date = new LocalTime(selectedHour, selectedMinute);
                int result = settingsViewModel.updateCheckEventInterval(date.getMillisOfDay());
                if (result > 0) {
                    Log.d("SettingFragment onClick Interval Time", "Aktualizacja domyślnego czasu udana");
                } else {
                    Log.d("SettingFragment onClick Interval Time", "Aktualizacja domyślnego czasu nieudana");
                }
            }, hour, minute, true);
            timePickerCheckInterval.setTitle("Wybierz czas");
            timePickerCheckInterval.show();
        });


        /*
        BACKUP SETTINGS SECTION
         */


        Button createBackupButton = view.findViewById(R.id.create_backup);
        createBackupButton.setOnClickListener(v -> {
            try {
                createBackup();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("SettingFragment createBackupButton", "Problem z stworzeniem backupu " + e.getMessage());
            }
        });

        Button restoreBackupButton = view.findViewById(R.id.restore_backup);
        restoreBackupButton.setOnClickListener(v -> restoreBackup());

        Button configureBackup = view.findViewById(R.id.configure_backup);
        configureBackup.setOnClickListener(v -> setBackupSettings());

        backupLocalizationSpinner = view.findViewById(R.id.create_backup_spinner);
        restoreLocalizationSpinner = view.findViewById(R.id.restore_backup_spinner);

        showMakeBackupSectionButton = view.findViewById(R.id.button_settings_show_backup_section);
        backupSectionLL = view.findViewById(R.id.linear_layout_settings_backup_section);
        showMakeBackupSectionButton.setOnClickListener(v -> {
            if (!makeBackupSectionIsShow) {
                backupSectionLL.setVisibility(LinearLayout.VISIBLE);
                makeBackupSectionIsShow = true;
                showMakeBackupSectionButton.setRotation(180);
                updateUI();
            } else {
                backupSectionLL.setVisibility(LinearLayout.GONE);
                makeBackupSectionIsShow = false;
                showMakeBackupSectionButton.setRotation(0);
            }
        });

        showRestoreBackupSectionButton = view.findViewById(R.id.button_settings_show_restore_section);
        restoreSectionLL = view.findViewById(R.id.linear_layout_restore_backup_section);
        showRestoreBackupSectionButton.setOnClickListener(v -> {
            if (!restoreBackupSectionIsShow) {
                restoreSectionLL.setVisibility(LinearLayout.VISIBLE);
                restoreBackupSectionIsShow = true;
                showRestoreBackupSectionButton.setRotation(180);
            } else {
                restoreSectionLL.setVisibility(LinearLayout.GONE);
                restoreBackupSectionIsShow = false;
                showRestoreBackupSectionButton.setRotation(0);
            }
        });


        settingsViewModel.getLocalBackupLocationLiveData().observe(getViewLifecycleOwner(), s -> {
            if (backupLocalizationSpinner.getSelectedItem().toString().equals("Na telefonie")) {
                if (s.equals("")) {
                    currentBackupSettingsField.setText(DO_CONFIGURATION);
                } else {
                    String[] paths = s.split("\\|");
                    currentBackupSettingsField.setText(paths[0]);
                }
            }
        });

        currentBackupSettingsField = view.findViewById(R.id.current_backup_settings);
        backupLocalizationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateUI();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateUI();
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        updateUI();

    }


    private void createBackup() throws IOException {
        if (currentBackupSettingsField.getText() == "") {
            currentBackupSettingsField.setText(DO_CONFIGURATION);
        } else {
            if (backupLocalizationSpinner.getSelectedItem().toString().equals("Na telefonie")) {
                Permissions.verifyStoragePermissions(getActivity());
                LocalBackupDatabase localBackupDatabase = new LocalBackupDatabase(getContext(), getActivity());
                localBackupDatabase.createBackup();
            } else if (backupLocalizationSpinner.getSelectedItem().toString().equals("Dysk Google")) {
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(MyPrzypominajkaApp.get());
                boolean temp = GoogleSignIn.hasPermissions(account, new Scope(Scopes.DRIVE_FILE), new Scope(Scopes.EMAIL));
                if (account != null && temp) {
                    mDriveServiceHelper = new DriveServiceHelper(getGoogleDriveService(MyPrzypominajkaApp.get(), account, APP_NAME));
                    String fileName = settingsViewModel.getRemoteBackupFileName();
                    if (!fileName.equals("")) {
                        RemoteBackupDatabase remoteBackupDatabase = new RemoteBackupDatabase(getContext(), getActivity(), mDriveServiceHelper);
                        remoteBackupDatabase.createBackup(fileName);
                    }

                }
            }
        }
    }

    private void restoreBackup() {
        if (currentBackupSettingsField.getText().toString().equals(DO_CONFIGURATION) || currentBackupSettingsField.getText().toString().equals("")) {
            Toast.makeText(getContext(), "Najpierw przeprowadź konfigurację", Toast.LENGTH_LONG).show();
        } else {
            int code = 0;
            if (restoreLocalizationSpinner.getSelectedItem().toString().equals("Telefon")) {
                code = PATH_REQUEST_CODE_LOCAL;
            } else if (restoreLocalizationSpinner.getSelectedItem().toString().equals("Dysk Google")) {
                code = PATH_REQUEST_CODE_REMOTE;
            }
            if (code == 2 || code == 3) {
                Permissions.verifyStoragePermissions(getActivity());
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/octet-stream");
                startActivityForResult(Intent.createChooser(intent, "Wybierz plik"), code);
            }
        }
    }

    private void setBackupSettings() {
        if (backupLocalizationSpinner.getSelectedItem().toString().equals("Dysk Google")) {
            Intent localSettings = new Intent(getContext(), RemoteBackupSettings.class);
            startActivity(localSettings);
        } else if (backupLocalizationSpinner.getSelectedItem().toString().equals("Na telefonie")) {
            Permissions.verifyStoragePermissions(getActivity());
            Intent localSettings = new Intent(getContext(), LocalBackupSettings.class);
            startActivity(localSettings);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PATH_REQUEST_CODE_LOCAL || requestCode == PATH_REQUEST_CODE_REMOTE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri fileUri = data.getData();
                try {
                    assert fileUri != null;
                    InputStream inputStream = requireContext().getContentResolver().openInputStream(fileUri);
                    if (validFilDatabase(fileUri)) {
                        RestoreDatabase restoreDatabase = new RestoreDatabase(getContext(), getActivity());
                        boolean result = restoreDatabase.restoreBackup(inputStream);
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
    }

    private boolean validFilDatabase(Uri fileUri) {
        ContentResolver cr = requireContext().getContentResolver();
        String mime = cr.getType(fileUri);
        return "application/octet-stream".equals(mime);
    }

    private void updateUI() {
        if (backupLocalizationSpinner.getSelectedItem().toString().equals("Na telefonie")) {
            String localBackupSettings = settingsViewModel.getLocalBackupLocation();
            if (!localBackupSettings.equals("")) {
                String[] paths = localBackupSettings.split("\\|");
                currentBackupSettingsField.setText(paths[0]);
            } else {
                currentBackupSettingsField.setText(DO_CONFIGURATION);
            }
        } else if (backupLocalizationSpinner.getSelectedItem().toString().equals("Dysk Google")) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(MyPrzypominajkaApp.get());

            boolean temp = GoogleSignIn.hasPermissions(account, new Scope(Scopes.DRIVE_FILE), new Scope(Scopes.EMAIL));
            if (account != null && temp) {
                mDriveServiceHelper = new DriveServiceHelper(getGoogleDriveService(MyPrzypominajkaApp.get(), account, APP_NAME));
                String tempText = LOGGED_IN_AS + " " + account.getDisplayName();
                currentBackupSettingsField.setText(tempText);
            } else {
                currentBackupSettingsField.setText(DO_CONFIGURATION);
            }
        }
    }
}